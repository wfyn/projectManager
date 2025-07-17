package com.sudy.service.impl;

import com.sudy.entity.GitRepository;
import com.sudy.mapper.GitRepositoryMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class GitRepositoryService {

    private static final Logger logger = LoggerFactory.getLogger(GitRepositoryService.class);

    @Autowired
    private GitRepositoryMapper gitRepositoryMapper;

    /**
     * 扫描指定目录下的所有Git仓库并保存到数据库
     *
     * @param rootPath 要扫描的根目录
     * @return 扫描到的Git仓库数量
     */
    public int scanAndSaveGitRepositories(String rootPath) {
        logger.info("开始扫描Git仓库，根目录: {}", rootPath);

        List<GitRepository> repositories = findGitRepositories(rootPath);

        logger.info("共找到{}个Git仓库", repositories.size());

        repositories.forEach(repo -> {
            try {
                GitRepository existing = gitRepositoryMapper.selectByRepoPath(repo.getRepoPath());
                if (existing == null) {
                    logger.debug("新增仓库: {}", repo.getRepoPath());
                    gitRepositoryMapper.insert(repo);
                    logger.info("成功添加新仓库: {} (ID: {})", repo.getRepoName(), repo.getId());
                } else {
                    logger.debug("更新已有仓库: {}", repo.getRepoPath());
                    existing.setRepoName(repo.getRepoName());
                    existing.setRemoteUrl(repo.getRemoteUrl());
                    existing.setBranch(repo.getBranch());
                    existing.setUpdateTime(LocalDateTime.now());
                    gitRepositoryMapper.updateById(existing);
                    logger.info("成功更新仓库: {} (ID: {})", existing.getRepoName(), existing.getId());
                }
            } catch (Exception e) {
                logger.error("处理仓库失败: {} - 错误: {}", repo.getRepoPath(), e.getMessage(), e);
            }
        });

        logger.info("扫描完成，共处理{}个仓库", repositories.size());
        return repositories.size();
    }

    /**
     * 查找指定目录下的所有Git仓库
     *
     * @param rootPath 要扫描的根目录
     * @return Git仓库列表
     */
    public List<GitRepository> findGitRepositories(String rootPath) {
        List<GitRepository> repositories = new ArrayList<>();
        Path startPath = Paths.get(rootPath);

        logger.debug("开始遍历目录: {}", startPath);

        try (Stream<Path> paths = Files.walk(startPath)) {
            paths.filter(path -> {
                        try {
                            boolean isDirectory = path.toFile().isDirectory();
                            boolean isAccessible = Files.isReadable(path);
                            boolean isSystemDir = isSystemProtectedDirectory(path);

                            if (!isDirectory) {
                                logger.trace("跳过非目录: {}", path);
                                return false;
                            }

                            if (!isAccessible) {
                                logger.warn("目录不可访问，跳过: {}", path);
                                return false;
                            }

                            if (isSystemDir) {
                                logger.debug("跳过系统保护目录: {}", path);
                                return false;
                            }

                            return true;
                        } catch (Exception e) {
                            logger.warn("检查目录失败: {} - 错误: {}", path, e.getMessage());
                            return false;
                        }
                    })
                    .filter(path -> {
                        boolean hasGitDir = path.resolve(".git").toFile().exists();
                        if (hasGitDir) {
                            logger.debug("发现Git仓库: {}", path);
                        }
                        return hasGitDir;
                    })
                    .forEach(gitPath -> {
                        try {
                            GitRepository repo = extractGitInfo(gitPath);
                            if (repo != null) {
                                repositories.add(repo);
                                logger.debug("成功提取仓库信息: {}", gitPath);
                            }
                        } catch (Exception e) {
                            logger.error("提取Git信息失败: {} - 错误: {}", gitPath, e.getMessage(), e);
                        }
                    });
        } catch (IOException e) {
            logger.error("遍历目录树失败: {} - 错误: {}", startPath, e.getMessage(), e);
        }

        return repositories;
    }

    /**
     * 检查是否是系统保护目录
     *
     * @param path 目录路径
     * @return 如果是系统保护目录返回true
     */
    private boolean isSystemProtectedDirectory(Path path) {
        String pathStr = path.toString().toLowerCase();
        // Windows系统保护目录
        boolean isProtected = pathStr.contains("$recycle.bin")
                || pathStr.contains("system volume information")
                || pathStr.contains("windows")
                || pathStr.contains("program files")
                || pathStr.contains("programdata")
                || pathStr.contains("appdata");

        if (isProtected) {
            logger.trace("识别为系统保护目录: {}", path);
        }

        return isProtected;
    }

    /**
     * 提取Git仓库信息
     *
     * @param gitPath Git仓库路径
     * @return GitRepository对象
     */
    private GitRepository extractGitInfo(Path gitPath) {
        logger.debug("开始提取Git仓库信息: {}", gitPath);

        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(gitPath.resolve(".git").toFile())
                    .readEnvironment()
                    .findGitDir()
                    .build();

            try (Git git = new Git(repository)) {
                GitRepository gitRepo = new GitRepository();
                gitRepo.setRepoPath(gitPath.toAbsolutePath().toString());

                // 从路径中提取仓库名称
                String repoName = gitPath.getFileName().toString();
                gitRepo.setRepoName(StringUtils.hasText(repoName) ? repoName : "未命名仓库");
                logger.trace("仓库名称: {}", gitRepo.getRepoName());

                // 获取远程URL
                String remoteUrl = repository.getConfig().getString("remote", "origin", "url");
                gitRepo.setRemoteUrl(remoteUrl);
                logger.trace("远程URL: {}", remoteUrl);

                // 获取当前分支
                String branch = repository.getBranch();
                gitRepo.setBranch(branch);
                logger.trace("当前分支: {}", branch);

                LocalDateTime now = LocalDateTime.now();
                gitRepo.setCreateTime(now);
                gitRepo.setUpdateTime(now);
                gitRepo.setLastScanTime(now);

                logger.debug("成功提取仓库信息: {}", gitPath);
                return gitRepo;
            }
        } catch (Exception e) {
            logger.error("提取Git信息失败: {} - 错误: {}", gitPath, e.getMessage(), e);
            return null;
        }
    }
}