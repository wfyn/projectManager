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
 * 文件元数据表
 * </p>
 *
 * @author author
 * @since 2025-07-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("file_metadata")
public class FileMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 元数据ID
     */
    @TableId(value = "meta_id", type = IdType.AUTO)
    private Long metaId;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 元数据键
     */
    private String metaKey;

    /**
     * 元数据值
     */
    private String metaValue;


}
