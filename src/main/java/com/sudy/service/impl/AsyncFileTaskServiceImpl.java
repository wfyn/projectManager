package com.sudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sudy.dto.FileAddDTO;
import com.sudy.entity.FileDetail;
import com.sudy.entity.FileTask;
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

import java.io.IOException;
import java.nio.file.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AsyncFileTaskServiceImpl implements AsyncFileTaskService {

    private final FileTaskMapper fileTaskMapper;
    private final FileDetailMapper fileDetailMapper;
    @Autowired
    private GitRepositoryMapper gitRepositoryMapper;

    @Autowired
    public AsyncFileTaskServiceImpl(FileTaskMapper fileTaskMapper, FileDetailMapper fileDetailMapper) {
        this.fileTaskMapper = fileTaskMapper;
        this.fileDetailMapper = fileDetailMapper;
    }

    @Override
    @Transactional
    public FileTask createTask(FileAddDTO fileAddDTO) {
        // 创建任务记录
        FileTask task = new FileTask();
        task.setRepoPath(fileAddDTO.getRepoPath());
        task.setSourcePath(fileAddDTO.getSourcePath());
        task.setCommitHash(fileAddDTO.getCommitHash());
        task.setGeneratedPath(fileAddDTO.getGeneratedPath());
        task.setStatus("PENDING");
        task.setCreatedAt(new Date());
        fileTaskMapper.insert(task);

        // 获取变更文件列表
        List<String> changedFiles;
        try {
            changedFiles = GitUtil.getChangedFilesViaCLI(fileAddDTO.getRepoPath(), fileAddDTO.getCommitHash());
        } catch (Exception e) {
            log.error("获取变更文件失败", e);
            updateTaskStatus(task.getId(), "FAILED", "获取变更文件失败: " + e.getMessage());
            return task;
        }

        // 保存文件详情
        saveFileDetails(task.getId(), changedFiles);

        // 异步执行任务
        executeTask(task.getId());

        return task;
    }

    @Async("fileTaskExecutor")
    @Override
    public void executeTask(Long taskId) {
        // 更新任务状态为运行中
        updateTaskStatus(taskId, "RUNNING", null);

        FileTask task = fileTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }

        try {
            // 获取文件详情
            List<FileDetail> details = fileDetailMapper.selectList(
                    new QueryWrapper<FileDetail>().eq("task_id", taskId)
            );

            // 确保输出目录存在
            Path outputDir = Paths.get(task.getGeneratedPath());
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            int successCount = 0;
            int failedCount = 0;

            // 处理每个文件
            for (FileDetail detail : details) {
                try {
                    Path sourceFile = Paths.get(task.getSourcePath(), detail.getFileCompiledPath());
                    Path destFile = outputDir.resolve(detail.getFileCompiledPath());

                    if (Files.exists(sourceFile)) {
                        // 创建目标目录
                        Files.createDirectories(destFile.getParent());

                        // 复制文件
                        Files.copy(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING);
                        successCount++;
                    } else {
                        log.warn("源文件不存在: {}", sourceFile);
                        failedCount++;
                    }
                } catch (Exception e) {
                    log.error("处理文件失败: {}", detail.getFileSourcePath(), e);
                    failedCount++;
                }
            }

            // 更新任务状态
            String result = String.format("处理完成! 成功: %d, 失败: %d", successCount, failedCount);
            updateTaskStatus(taskId, "SUCCESS", result);

        } catch (Exception e) {
            log.error("任务执行失败: {}", taskId, e);
            updateTaskStatus(taskId, "FAILED", "任务执行失败: " + e.getMessage());
        }
    }

    /**
     * 保存文件详情
     */

    private void saveFileDetails(Long taskId, List<String> changedFiles) {
        List<FileDetail> details = changedFiles.stream().map(filePath -> {
            FileDetail detail = new FileDetail();
            detail.setTaskId(taskId);
            detail.setFileSourcePath(filePath);
            detail.setFileCompiledPath(resolveCompiledPath(filePath));
            detail.setFileType(determineFileType(filePath));
            return detail;
        }).collect(Collectors.toList());

        details.forEach(fileDetailMapper::insert);
    }

    /**
     * 解析编译后文件路径
     */

    private String resolveCompiledPath(String filePath) {
        String normalizedPath = filePath.replace("\\", "/");

        if (normalizedPath.contains("src/main/java/") && normalizedPath.endsWith(".java")) {
            return normalizedPath
                    .replaceFirst("^.*src/main/java/", "")
                    .replace(".java", ".class");
        } else if (normalizedPath.contains("src/main/webapp/")) {
            return normalizedPath.replaceFirst("^.*src/main/webapp/", "");
        } else if (normalizedPath.endsWith(".properties") || normalizedPath.contains("src/main/resources/")) {
            return normalizedPath.replaceFirst("^.*src/main/resources/", "");
        }

        return normalizedPath;
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
}