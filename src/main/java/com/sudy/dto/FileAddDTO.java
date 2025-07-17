package com.sudy.dto;

import lombok.Data;

/**
 * 文件添加数据传输对象（DTO），用于封装文件添加操作所需的参数
 */
@Data
public class FileAddDTO {

    String repoId;

    /**
     * 目标代码仓库的物理路径（绝对路径）
     */
    String repoPath;

    /**
     * 待添加文件的源路径（相对于仓库根目录的相对路径）
     */
    String sourcePath;

    /**
     * 关联的Git提交哈希值，用于版本追踪
     */
    String commitHash;

    /**
     * 文件生成的目标路径（相对于仓库根目录的相对路径）
     */
    String generatedPath;
}
