package com.sudy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 提交文件变更明细表
 * </p>
 *
 * @author author
 * @since 2025-07-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("git_commit_files")
public class GitCommitFiles implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联提交ID
     */
    private Long commitId;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 变更类型
     */
    private String changeType;

    /**
     * 旧路径(重命名时使用)
     */
    private String oldPath;


}
