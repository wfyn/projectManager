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
 * 文件存储位置表
 * </p>
 *
 * @author author
 * @since 2025-07-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("storage_locations")
public class StorageLocations implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 存储位置ID
     */
    @TableId(value = "location_id", type = IdType.AUTO)
    private Integer locationId;

    /**
     * 存储位置名称
     */
    private String locationName;

    /**
     * 存储类型(local, s3, oss等)
     */
    private String storageType;

    /**
     * 基础路径
     */
    private String basePath;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 秘密密钥
     */
    private String secretKey;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 区域
     */
    private String region;

    /**
     * 端点URL
     */
    private String endpoint;

    /**
     * 是否默认(0-否,1-是)
     */
    private Boolean isDefault;

    /**
     * 状态(0-禁用,1-启用)
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 描述
     */
    private String description;


}
