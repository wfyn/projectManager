package com.sudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_file_task")
public class FileTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String repoPath;

    private String sourcePath;

    private String commitHash;

    private String generatedPath;

    private String status;

    private Date createdAt;

    private Date processedAt;

    private String errorInfo;

    @TableField(exist = false)
    private List<FileDetail> fileDetails;
}