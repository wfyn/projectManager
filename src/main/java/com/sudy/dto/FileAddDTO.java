package com.sudy.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 文件添加数据传输对象（DTO），用于封装文件添加操作所需的参数
 */
@Data
public class FileAddDTO {

    @NotBlank(message = "仓库ID不能为空")
    String repoId;

    /**
     * 目标代码仓库的物理路径（绝对路径）
     */
    @NotBlank(message = "仓库路径不能为空")
    String repoPath;

    /**
     * 待添加文件的源路径（相对于仓库根目录的相对路径）
     */
    @NotBlank(message = "源路径不能为空")
    String sourcePath;

    /**
     * 关联的Git提交哈希值，用于版本追踪
     */
    @Pattern(regexp = "^[0-9a-f]{40}$", message = "提交哈希格式不正确")
    String commitHash;

    /**
     * 文件生成的目标路径（相对于仓库根目录的相对路径）
     */
    @NotBlank(message = "生成路径不能为空")
    String generatedPath;
}
