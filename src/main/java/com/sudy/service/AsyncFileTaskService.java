package com.sudy.service;

import com.sudy.dto.FileAddDTO;
import com.sudy.entity.FileTask;

import java.io.IOException;

public interface AsyncFileTaskService {

    /**
     * 创建并执行异步文件处理任务
     */
    FileTask createTask(FileAddDTO fileAddDTO) throws IOException;

    /**
     * 执行文件处理任务
     */
    void executeTask(Long taskId);
}