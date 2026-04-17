package com.sudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sudy.dto.FileAddDTO;
import com.sudy.entity.FileDetail;
import com.sudy.entity.FileTask;
import com.sudy.entity.GitRepository;
import com.sudy.mapper.FileDetailMapper;
import com.sudy.mapper.FileTaskMapper;
import com.sudy.mapper.GitRepositoryMapper;
import com.sudy.service.AsyncFileTaskService;
import com.sudy.util.GitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AsyncFileTaskServiceImpl implements AsyncFileTaskService {

    private final FileTaskMapper fileTaskMapper;
    private final FileDetailMapper fileDetailMapper;
    private final GitRepositoryMapper gitRepositoryMapper;

    @Autowired
    public AsyncFileTaskServiceImpl(FileTaskMapper fileTaskMapper,
                                    FileDetailMapper fileDetailMapper,
                                    GitRepositoryMapper gitRepositoryMapper) {
        this.fileTaskMapper = fileTaskMapper;
        this.fileDetailMapper = fileDetailMapper;
        this.gitRepositoryMapper = gitRepositoryMapper;
    }

    @Override
    @Transactional
    public FileTask createTask(FileAddDTO fileAddDTO) throws IOException {

        GitRepository gitRepository = gitRepositoryMapper.selectById(fileAddDTO.getRepoId());
        String webplusVersion = gitRepository.getWebplusVersion();
        String repoPath = gitRepository.getRepoPath();
        String sourcePath;
        if (fileAddDTO.getSourcePath() != null && !fileAddDTO.getSourcePath().trim().isEmpty()) {
            sourcePath = fileAddDTO.getSourcePath().trim();
        } else {
            sourcePath = gitRepository.getRepoPath() + "\\02src\\target\\webpluspro-" + webplusVersion;
        }
        
        // 优先使用前端传参的生成路径，否则使用仓库默认路径
        String baseGeneratedPath;
        if (fileAddDTO.getGeneratedPath() != null && !fileAddDTO.getGeneratedPath().trim().isEmpty()) {
            baseGeneratedPath = fileAddDTO.getGeneratedPath().trim();
        } else {
            baseGeneratedPath = gitRepository.getGeneratedPath();
        }
        
        // 生成唯一目录（如果重复执行，添加 v1/ROOT, v2/ROOT 等）
        String generatedPath = generateUniquePath(baseGeneratedPath, fileAddDTO.getRepoId(), fileAddDTO.getCommitHash());
        
        // 创建任务记录
        FileTask task = new FileTask();
        task.setRepoPath(repoPath);
        task.setSourcePath(sourcePath);
        task.setCommitHash(fileAddDTO.getCommitHash());
        task.setGeneratedPath(generatedPath);

        task.setStatus("PENDING");
        task.setCreatedAt(new Date());
        fileTaskMapper.insert(task);

        // 获取变更文件列表
        List<String> changedFiles;
        try {
            changedFiles = GitUtil.getChangedFilesViaCLI(repoPath, fileAddDTO.getCommitHash());
        } catch (Exception e) {
            log.error("获取变更文件失败", e);
            updateTaskStatus(task.getId(), "FAILED", "获取变更文件失败: " + e.getMessage());
            return task;
        }


        Path sourceBaseDir = Paths.get(sourcePath);
        log.info("源目录基路径: {}", sourceBaseDir);
        
        // 3. 处理每个修改文件
        for (String filePath : changedFiles) {
            // 提取编译后的相对路径
            List<String> relativePaths = findRelativePaths(filePath);
            log.info("Git文件: {}, 相对路径: {}", filePath, relativePaths);
            for (String relativePath : relativePaths) {
                FileDetail detail = new FileDetail();
                detail.setTaskId(task.getId());
                detail.setFileSourcePath(filePath);
                detail.setFileCompiledPath(relativePath);
                detail.setFileType(determineFileType(filePath));
                fileDetailMapper.insert(detail);
                log.info("记录文件详情: source={}, relative={}", filePath, relativePath);
            }
        }

        // ✅ 立即异步执行任务（通过 self 代理调用，避免 @Async 自调用失效）
        executeTask(task.getId());

        return task;
    }

    /**
     * 立即执行任务（由 createTask 通过 self 代理调用，确保 @Async 生效）
     */
    @Async("fileTaskExecutor")
    @Override
    public void executeTask(Long taskId) {
        // 更新任务状态为运行中
        updateTaskStatus(taskId, "RUNNING", null);

        FileTask task = fileTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            updateTaskStatus(taskId, "FAILED", "任务不存在");
            return;
        }

        try {
            // 获取文件详情
            List<FileDetail> details = fileDetailMapper.selectList(
                    new QueryWrapper<FileDetail>().eq("task_id", taskId)
            );

            log.info("任务 {}: 共 {} 个文件需要处理", taskId, details.size());

            // 规范化路径处理
            Path sourceBasePath = Paths.get(task.getSourcePath()).normalize();
            Path outputBasePath = Paths.get(task.getGeneratedPath()).normalize();
            
            log.info("源目录: {}", sourceBasePath);
            log.info("目标目录: {}", outputBasePath);

            // 确保输出目录存在
            if (!Files.exists(outputBasePath)) {
                Files.createDirectories(outputBasePath);
                log.info("已创建输出目录: {}", outputBasePath);
            }

            int successCount = 0;
            int failedCount = 0;
            List<String> failedFiles = new ArrayList<>();

            // 处理每个文件
            for (FileDetail detail : details) {
                try {
                    // 处理Windows路径分隔符问题
                    String compiledPath = detail.getFileCompiledPath().replace('/', File.separatorChar);

                    Path sourceFile = sourceBasePath.resolve(compiledPath).normalize();
                    Path destFile = outputBasePath.resolve(compiledPath).normalize();

                    log.debug("源文件路径: {}", sourceFile);
                    log.debug("目标文件路径: {}", destFile);

                    // 安全检查：防止路径遍历攻击
                    if (!sourceFile.startsWith(sourceBasePath) || !destFile.startsWith(outputBasePath)) {
                        throw new SecurityException("非法路径访问: " + compiledPath);
                    }

                    if (Files.exists(sourceFile)) {
                        // 创建目标目录
                        Files.createDirectories(destFile.getParent());

                        // 复制文件
                        Files.copy(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING);
                        successCount++;
                        log.info("复制成功: {} -> {}", sourceFile, destFile);
                    } else {
                        String msg = "源文件不存在: " + sourceFile;
                        log.warn(msg);
                        failedFiles.add(msg);
                        failedCount++;
                    }
                } catch (Exception e) {
                    String msg = String.format("文件处理失败: %s - %s",
                            detail.getFileCompiledPath(), e.getMessage());
                    log.error(msg, e);
                    failedFiles.add(msg);
                    failedCount++;
                }
            }

            // 更新任务状态
            String result;
            if (failedCount == 0) {
                result = String.format("成功处理 %d 个文件", successCount);
            } else {
                result = String.format("完成: 成功 %d, 失败 %d. 失败文件: %s",
                        successCount, failedCount,
                        String.join("; ", failedFiles.subList(0, Math.min(5, failedFiles.size()))));
            }
            updateTaskStatus(taskId, failedCount == 0 ? "SUCCESS" : "PARTIAL_SUCCESS", result);

        } catch (Exception e) {
            log.error("任务执行失败: {}", taskId, e);
            updateTaskStatus(taskId, "FAILED", "任务执行失败: " + e.getMessage());
        }
    }



    /**
     * 确定文件类型
     */

    private String determineFileType(String filePath) {
        if (filePath.endsWith(".java")) return "JAVA_SOURCE";
        if (filePath.endsWith(".class")) return "CLASS";
        if (filePath.endsWith(".jsp")) return "JSP";
        if (filePath.endsWith(".properties")) return "PROPERTIES";
        if (filePath.endsWith(".xml")) return "XML";
        return "OTHER";
    }

    /**
     * 更新任务状态
     */

    private void updateTaskStatus(Long taskId, String status, String message) {
        FileTask task = new FileTask();
        task.setId(taskId);
        task.setStatus(status);
        task.setProcessedAt(new Date());

        if (message != null) {
            task.setErrorInfo(message);
        }

        fileTaskMapper.updateById(task);
    }

    private List<Path> findCompiledPaths(String originalPath, Path baseDir) {
        List<Path> compiledPaths = new ArrayList<>();

        // 根据文件类型确定编译位置
        if (originalPath.contains("src/main/java/") && originalPath.endsWith(".java")) {
            // Java文件 -> 编译到classes目录
            String classPath = originalPath
                    .replaceFirst("02src/src/main/java/", "classes/")
                    .replace(".java", ".class");
            compiledPaths.add(baseDir.resolve("WEB-INF").resolve(classPath));
        } else if (originalPath.contains("src/main/webapp/")) {
            // JSP文件 -> 复制到webplus目录
            String relativePath = originalPath.replaceFirst("02src/src/main/webapp/", "");
            compiledPaths.add(baseDir.resolve(relativePath));
        } else if (originalPath.endsWith(".properties") || originalPath.contains("src/main/resources/")) {
            // 配置文件 -> 复制到resources目录
            String relativePath = originalPath
                    .replaceFirst("02src/src/main/resources/", "");
            compiledPaths.add(baseDir.resolve("classes").resolve(relativePath));
        }

        // 处理可能的内部类
        if (originalPath.contains("src/main/java/") && originalPath.endsWith(".java")) {
            String classDirPath = originalPath
                    .replaceFirst("02src/src/main/java/", "classes/")
                    .replace(".java", "");

            Path classDir = baseDir.resolve("WEB-INF").resolve(classDirPath).getParent();

            // 添加可能的内部类文件
            if (classDir != null && Files.exists(classDir)) {
                try {
                    Files.walkFileTree(classDir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (file.getFileName().toString().startsWith(classDirPath.substring(
                                    classDirPath.lastIndexOf('/') + 1) + "$") &&
                                    file.toString().endsWith(".class")) {
                                compiledPaths.add(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    System.err.println("扫描内部类时出错: " + e.getMessage());
                }
            }
        }
        return compiledPaths;
    }
    
    /**
     * 计算编译后文件的相对路径（用于存储到数据库）
     * 处理 Java 文件、JSP 文件和配置文件
     */
    private List<String> findRelativePaths(String originalPath) {
        List<String> relativePaths = new ArrayList<>();
        if (originalPath == null || originalPath.trim().isEmpty()) {
            return relativePaths;
        }
        
        String normalized = originalPath.replace("\\", "/");

        // Java文件 -> WEB-INF/classes/xxx/xxx.class
        if (normalized.contains("src/main/java/") && normalized.endsWith(".java")) {
            String classPath = normalized
                    .replaceFirst(".*src/main/java/", "WEB-INF/classes/")
                    .replace(".java", ".class");
            relativePaths.add(classPath);
        }
        // JSP文件 -> 直接复制，保持相对路径
        else if (normalized.contains("src/main/webapp/")) {
            String relativePath = normalized.replaceFirst(".*src/main/webapp/", "");
            relativePaths.add(relativePath);
        }
        // 配置文件 -> WEB-INF/classes/xxx
        else if (normalized.endsWith(".properties") || normalized.endsWith(".xml") || normalized.contains("src/main/resources/")) {
            String relativePath = normalized.replaceFirst(".*src/main/resources/", "WEB-INF/classes/");
            relativePaths.add(relativePath);
        }

        return relativePaths;
    }
    
    /**
     * 生成唯一路径：如果同一仓库+commit的任务已存在，添加 v1/, v2/ 等前缀
     */
    private String generateUniquePath(String basePath, String repoId, String commitHash) {
        // 查询该仓库+commit已执行过多少次
        Long count = Long.valueOf(fileTaskMapper.selectCount(new QueryWrapper<FileTask>()
                .eq("repo_path", getRepoPathById(repoId))
                .eq("commit_hash", commitHash)));
        
        if (count == null || count == 0) {
            return basePath;
        }
        
        // 获取基础路径的父目录和最后一级目录名
        Path base = Paths.get(basePath);
        Path parent = base.getParent();
        String dirName = base.getFileName().toString();
        
        // 生成 v1, v2, v3... 目录
        return parent.resolve("v" + (count + 1)).resolve(dirName).toString();
    }
    
    private String getRepoPathById(String repoId) {
        GitRepository repo = gitRepositoryMapper.selectById(repoId);
        return repo != null ? repo.getRepoPath() : null;
    }
}