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
 * 文件基本信息表
 * </p>
 *
 * @author author
 * @since 2025-07-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("files")
public class Files implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    @TableId(value = "file_id", type = IdType.AUTO)
    private Long fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件扩展名
     */
    private String fileExtension;

    /**
     * 文件MD5哈希值
     */
    private String md5Hash;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 上传用户ID
     */
    private Long uploadUserId;

    /**
     * 是否为目录(0-文件,1-目录)
     */
    private Boolean isDirectory;

    /**
     * 父目录ID
     */
    private Long parentId;

    /**
     * 状态(0-已删除,1-正常)
     */
    private Boolean status;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 缩略图路径
     */
    private String thumbnailPath;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 查看次数
     */
    private Integer viewCount;


}
