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
 * Git仓库信息表
 * </p>
 *
 * @author author
 * @since 2025-07-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("git_repository")
public class GitRepository implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 仓库名称
     */
    private String repoName;

    /**
     * 本地仓库路径
     */
    private String repoPath;

    /**
     * 远程仓库URL
     */
    private String remoteUrl;

    /**
     * 默认分支
     */
    private String branch;

    /**
     * 仓库描述
     */
    private String description;
    /**
     * 当前webplus版本
     */

    private String webplusVersion;

    /**
     * 部署包文件生成路径
     */
    private String generatedPath;

    /**
     * 最后扫描时间
     */
    private LocalDateTime lastScanTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
