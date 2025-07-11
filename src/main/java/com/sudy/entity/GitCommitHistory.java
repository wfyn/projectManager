package com.sudy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Git提交历史表
 * </p>
 *
 * @author author
 * @since 2025-07-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("git_commit_history")
public class GitCommitHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联仓库ID
     */
    private Long repoId;

    /**
     * 完整提交哈希
     */
    private String commitHash;

    /**
     * 简短提交哈希
     */
    private String shortHash;

    /**
     * 作者名称
     */
    private String authorName;

    /**
     * 作者邮箱
     */
    private String authorEmail;

    /**
     * 提交时间
     */
    private LocalDateTime commitTime;

    /**
     * 提交信息
     */
    private String commitMessage;

    /**
     * 变更文件数量
     */
    private Integer fileChanges;

    /**
     * 新增行数
     */
    private Integer insertions;

    /**
     * 删除行数
     */
    private Integer deletions;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;


}
