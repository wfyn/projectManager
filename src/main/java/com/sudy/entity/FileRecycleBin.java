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
 * 文件回收站
 * </p>
 *
 * @author author
 * @since 2025-07-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file_recycle_bin")
public class FileRecycleBin implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 回收站ID
     */
    @TableId(value = "bin_id", type = IdType.AUTO)
    private Long binId;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 原始路径
     */
    private String originalPath;

    /**
     * 删除时间
     */
    private LocalDateTime deletedTime;

    /**
     * 删除人
     */
    private Long deletedBy;

    /**
     * 过期时间
     */
    private LocalDateTime expiryTime;

    /**
     * 是否已恢复(0-否,1-是)
     */
    private Boolean isRestored;


}
