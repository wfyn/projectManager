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
 * 文件共享链接表
 * </p>
 *
 * @author author
 * @since 2025-07-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file_share_links")
public class FileShareLinks implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分享ID
     */
    @TableId(value = "share_id", type = IdType.AUTO)
    private Long shareId;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 分享码
     */
    private String shareCode;

    /**
     * 分享人ID
     */
    private Long shareUserId;

    /**
     * 分享时间
     */
    private LocalDateTime shareTime;

    /**
     * 过期时间
     */
    private LocalDateTime expiryTime;

    /**
     * 访问密码
     */
    private String password;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 查看次数
     */
    private Integer viewCount;

    /**
     * 是否有效(0-无效,1-有效)
     */
    private Boolean isActive;

    /**
     * 权限级别(1-查看,2-下载)
     */
    private Boolean permissionLevel;


}
