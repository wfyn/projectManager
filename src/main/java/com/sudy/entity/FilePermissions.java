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
 * 文件权限表
 * </p>
 *
 * @author author
 * @since 2025-07-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file_permissions")
public class FilePermissions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @TableId(value = "permission_id", type = IdType.AUTO)
    private Long permissionId;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 用户ID(为空表示所有用户)
     */
    private Long userId;

    /**
     * 用户组ID
     */
    private Long groupId;

    /**
     * 权限类型(1-查看,2-下载,3-编辑,4-删除,5-管理)
     */
    private Boolean permissionType;

    /**
     * 授权人
     */
    private Long grantedBy;

    /**
     * 授权时间
     */
    private LocalDateTime grantedTime;

    /**
     * 权限过期时间
     */
    private LocalDateTime expiryTime;


}
