CREATE TABLE `t_file_task`
(
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `repo_path`      VARCHAR(500) NOT NULL COMMENT 'Git仓库路径',
    `source_path`    VARCHAR(500) NOT NULL COMMENT 'target相对路径',
    `commit_hash`    VARCHAR(40)  NOT NULL COMMENT 'Git提交commitHash',
    `generated_path` VARCHAR(500) NOT NULL COMMENT '生成文件输出路径',
    `status`         ENUM('PENDING','RUNNING', 'SUCCESS', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '处理状态',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `processed_at`   DATETIME COMMENT '处理完成时间',
    `error_info`     TEXT COMMENT '错误信息',
    PRIMARY KEY (`id`),
    INDEX            `idx_repo_commit` (`repo_path`(128), `commit_hash`),
    INDEX            `idx_generated_path` (`generated_path`(128)),
    INDEX            `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Git文件变更分类表';

CREATE TABLE `t_file_detail`
(
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id`        BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
    `file_source_path`    VARCHAR(500) NOT NULL COMMENT '源文件相对路径（Git返回的路径）',
    `file_compiled_path`  VARCHAR(500) NOT NULL COMMENT '编译后文件相对路径',
    `file_type`      ENUM('JAVA_SOURCE', 'JSP', 'CLASS', 'PROPERTIES', 'XML', 'OTHER') NOT NULL COMMENT '文件类型',
    PRIMARY KEY (`id`)
)