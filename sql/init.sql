-- =====================================================================
-- AI Knowledge Hub —— 阶段①（文件管理）数据库初始化脚本
-- 在虚拟机 MySQL (192.168.150.101:3306) 上执行：
--   mysql -h127.0.0.1 -uroot -p < init.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `knowledge_hub` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `knowledge_hub`;

-- ---------------------------------------------------------------------
-- 用户表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`      VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password_hash` VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    `nickname`      VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `email`         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar`        VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role`          VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    `storage_quota` BIGINT       NOT NULL DEFAULT 10737418240 COMMENT '存储配额(字节), 默认10GB',
    `storage_used`  BIGINT       NOT NULL DEFAULT 0 COMMENT '已用存储(字节)',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1正常 0禁用',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB COMMENT = '用户表';

-- ---------------------------------------------------------------------
-- 文件夹表（物化路径 + 父ID 双结构）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `folder` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',
    `user_id`    BIGINT        NOT NULL COMMENT '所属用户',
    `parent_id`  BIGINT        DEFAULT NULL COMMENT '父文件夹ID, 根为NULL',
    `name`       VARCHAR(100)  NOT NULL COMMENT '文件夹名',
    `path`       VARCHAR(1000) NOT NULL COMMENT '物化路径, 如 /docs/java/',
    `sort`       INT           NOT NULL DEFAULT 0 COMMENT '排序',
    `deleted`    TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_parent` (`user_id`, `parent_id`)
) ENGINE = InnoDB COMMENT = '文件夹表';

-- ---------------------------------------------------------------------
-- 分类表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `category` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL COMMENT '所属用户(0表示系统预置)',
    `name`       VARCHAR(50)  NOT NULL COMMENT '分类名, 如 Java/数据库/AI',
    `icon`       VARCHAR(50)  DEFAULT NULL COMMENT '图标',
    `sort`       INT          NOT NULL DEFAULT 0,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB COMMENT = '文件分类表';

-- ---------------------------------------------------------------------
-- 标签表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tag` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT      NOT NULL,
    `name`       VARCHAR(50) NOT NULL COMMENT '标签名',
    `color`      VARCHAR(20) DEFAULT NULL COMMENT '显示颜色',
    `source`     VARCHAR(10) NOT NULL DEFAULT 'MANUAL' COMMENT '来源: MANUAL人工/AI自动',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tag` (`user_id`, `name`)
) ENGINE = InnoDB COMMENT = '标签表';

-- ---------------------------------------------------------------------
-- 文件-标签关联表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `file_tag` (
    `file_id` BIGINT NOT NULL,
    `tag_id`  BIGINT NOT NULL,
    PRIMARY KEY (`file_id`, `tag_id`),
    KEY `idx_tag` (`tag_id`)
) ENGINE = InnoDB COMMENT = '文件标签关联表';

-- ---------------------------------------------------------------------
-- 文件表（本体存 MinIO，此处仅元数据；处理状态机见架构文档 7.1）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `file` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `user_id`        BIGINT       NOT NULL COMMENT '所属用户',
    `folder_id`      BIGINT       DEFAULT NULL COMMENT '所在文件夹',
    `name`           VARCHAR(255) NOT NULL COMMENT '文件名(含扩展名)',
    `ext`            VARCHAR(20)  DEFAULT NULL COMMENT '扩展名, 小写',
    `mime_type`      VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `size`           BIGINT       NOT NULL DEFAULT 0 COMMENT '大小(字节)',
    `storage_key`    VARCHAR(500) NOT NULL COMMENT 'MinIO 对象键',
    `sha256`         CHAR(64)     DEFAULT NULL COMMENT '文件哈希(秒传/去重)',
    `category_id`    BIGINT       DEFAULT NULL COMMENT '分类',
    `summary`        VARCHAR(2000) DEFAULT NULL COMMENT 'AI 摘要(阶段②)',
    `status`         VARCHAR(20)  NOT NULL DEFAULT 'UPLOADED' COMMENT '处理状态: UPLOADED/PARSING/.../READY/FAILED',
    `status_detail`  VARCHAR(255) DEFAULT NULL COMMENT '状态详情(失败原因等)',
    `star`           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否收藏',
    `chunk_count`    INT          NOT NULL DEFAULT 0 COMMENT '切片数(阶段②)',
    `note_id`        BIGINT       DEFAULT NULL COMMENT '关联笔记(阶段⑤)',
    `last_access_at` DATETIME     DEFAULT NULL COMMENT '最近访问时间',
    `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除(回收站)',
    `deleted_at`     DATETIME     DEFAULT NULL COMMENT '删除时间(回收站30天保留)',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_folder` (`user_id`, `folder_id`, `deleted`),
    KEY `idx_user_star` (`user_id`, `star`),
    KEY `idx_sha256` (`sha256`),
    FULLTEXT KEY `ft_name` (`name`) WITH PARSER ngram COMMENT '文件名中文全文检索'
) ENGINE = InnoDB COMMENT = '文件元数据表';

-- ---------------------------------------------------------------------
-- 文件访问记录表（最近访问）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `file_access_log` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT      NOT NULL,
    `file_id`    BIGINT      NOT NULL,
    `action`     VARCHAR(20) NOT NULL COMMENT '行为: view/download/preview',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `created_at`)
) ENGINE = InnoDB COMMENT = '文件访问记录表';
