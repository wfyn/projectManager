-- =============================================
-- 项目管理平台 - 数据库初始化脚本
-- 数据库: filesystem
-- 字符集: utf8mb4
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 表1: git_repository - Git仓库信息表
-- ----------------------------
DROP TABLE IF EXISTS `git_repository`;
CREATE TABLE `git_repository` (
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `repo_name`       VARCHAR(200)    NOT NULL                  COMMENT '仓库名称',
    `repo_path`       VARCHAR(500)    NOT NULL                  COMMENT '本地仓库路径',
    `remote_url`      VARCHAR(500)     DEFAULT NULL             COMMENT '远程仓库URL',
    `branch`          VARCHAR(100)     DEFAULT 'main'           COMMENT '默认分支',
    `description`     VARCHAR(500)     DEFAULT NULL             COMMENT '仓库描述',
    `webplus_version` VARCHAR(50)     DEFAULT NULL             COMMENT '当前webplus版本',
    `generated_path`  VARCHAR(500)    DEFAULT NULL             COMMENT '部署包文件生成路径',
    `source_path`     VARCHAR(500)    DEFAULT NULL             COMMENT '源文件路径（部署时默认使用）',
    `last_scan_time`  DATETIME        DEFAULT NULL             COMMENT '最后扫描时间',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_repo_path` (`repo_path`(191)),
    KEY `idx_remote_url` (`remote_url`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Git仓库信息表';

-- ----------------------------
-- 表2: git_commit_history - Git提交历史表
-- ----------------------------
DROP TABLE IF EXISTS `git_commit_history`;
CREATE TABLE `git_commit_history` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `repo_id`        BIGINT UNSIGNED NOT NULL                COMMENT '关联仓库ID',
    `commit_hash`    VARCHAR(40)     NOT NULL                COMMENT '完整提交哈希',
    `short_hash`     VARCHAR(10)    NOT NULL                COMMENT '简短提交哈希',
    `author_name`    VARCHAR(100)    DEFAULT NULL            COMMENT '作者名称',
    `author_email`   VARCHAR(200)    DEFAULT NULL            COMMENT '作者邮箱',
    `commit_time`    DATETIME        NOT NULL                COMMENT '提交时间',
    `commit_message` VARCHAR(1000)   DEFAULT NULL            COMMENT '提交信息',
    `file_changes`   INT UNSIGNED     DEFAULT 0              COMMENT '变更文件数量',
    `insertions`     INT UNSIGNED     DEFAULT 0              COMMENT '新增行数',
    `deletions`      INT UNSIGNED     DEFAULT 0              COMMENT '删除行数',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_repo_commit` (`repo_id`, `commit_hash`),
    KEY `idx_commit_time` (`commit_time`),
    KEY `idx_author` (`author_name`(50))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Git提交历史表';

-- ----------------------------
-- 表3: git_commit_files - 提交文件变更明细表
-- ----------------------------
DROP TABLE IF EXISTS `git_commit_files`;
CREATE TABLE `git_commit_files` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `commit_id`  BIGINT UNSIGNED NOT NULL                COMMENT '关联提交ID',
    `file_path`  VARCHAR(500)    NOT NULL                COMMENT '文件路径',
    `change_type` VARCHAR(20)    DEFAULT NULL            COMMENT '变更类型(ADD/MODIFY/DELETE/RENAME)',
    `old_path`   VARCHAR(500)    DEFAULT NULL            COMMENT '旧路径(重命名时使用)',
    PRIMARY KEY (`id`),
    KEY `idx_commit_id` (`commit_id`),
    KEY `idx_file_path` (`file_path`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提交文件变更明细表';

-- ----------------------------
-- 表4: t_file_task - 文件处理任务表
-- ----------------------------
DROP TABLE IF EXISTS `t_file_task`;
CREATE TABLE `t_file_task` (
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `repo_path`       VARCHAR(500)    NOT NULL                COMMENT 'Git仓库路径',
    `source_path`     VARCHAR(500)    NOT NULL                COMMENT 'target相对路径',
    `commit_hash`     VARCHAR(40)     NOT NULL                COMMENT 'Git提交commitHash',
    `generated_path`  VARCHAR(500)   NOT NULL                COMMENT '生成文件输出路径',
    `status`          ENUM('PENDING','RUNNING','SUCCESS','FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '处理状态',
    `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `processed_at`    DATETIME        DEFAULT NULL            COMMENT '处理完成时间',
    `error_info`      TEXT            DEFAULT NULL           COMMENT '错误信息',
    PRIMARY KEY (`id`),
    KEY `idx_repo_commit` (`repo_path`(191), `commit_hash`),
    KEY `idx_generated_path` (`generated_path`(191)),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Git文件变更分类表';

-- ----------------------------
-- 表5: t_file_detail - 文件处理明细表
-- ----------------------------
DROP TABLE IF EXISTS `t_file_detail`;
CREATE TABLE `t_file_detail` (
    `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id`            BIGINT UNSIGNED NOT NULL                COMMENT '任务ID',
    `file_source_path`   VARCHAR(500)    NOT NULL                COMMENT '源文件相对路径（Git返回的路径）',
    `file_compiled_path` VARCHAR(500)   NOT NULL               COMMENT '编译后文件相对路径',
    `file_type`          ENUM('JAVA_SOURCE','JSP','CLASS','PROPERTIES','XML','OTHER') NOT NULL COMMENT '文件类型',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件处理明细表';

-- ----------------------------
-- 初始化测试数据（可选）
-- ----------------------------
-- INSERT INTO `git_repository` (`repo_name`, `repo_path`, `branch`, `description`) VALUES
-- ('示例仓库', '/data/repos/example', 'main', '这是一个测试仓库');

-- ----------------------------
-- 【增量迁移】已有数据库执行此语句添加 source_path 列
-- ----------------------------
-- ALTER TABLE `git_repository` ADD COLUMN `source_path` VARCHAR(500) DEFAULT NULL COMMENT '源文件路径（部署时默认使用）' AFTER `generated_path`;

SET FOREIGN_KEY_CHECKS = 1;
