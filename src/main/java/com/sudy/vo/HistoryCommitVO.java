package com.sudy.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Git提交历史记录值对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryCommitVO {
    private String commitHash;      // 提交哈希（完整）
    private String shortHash;      // 提交哈希（简短）
    private String commitMessage;  // 提交信息
    private String commitTime;     // 提交时间（ISO格式）
    private String commitAuthor;   // 提交作者
    private int fileChanges;       // 变更文件数量
    private List<String> changedFiles; // 变更文件列表（可选）
}