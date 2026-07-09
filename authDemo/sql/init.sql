-- =====================================================
-- auth_demo 数据库建表脚本
-- 用户认证服务
-- =====================================================

CREATE DATABASE IF NOT EXISTS auth_demo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE auth_demo;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`           BIGINT       NOT NULL   COMMENT '用户ID（雪花算法）',
    `phone`        VARCHAR(20)  NOT NULL   COMMENT '手机号（登录账号）',
    `password`     VARCHAR(255) NOT NULL   COMMENT '密码（BCrypt加密）',
    `nickname`     VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`       VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `status`       TINYINT      DEFAULT 0  COMMENT '状态：0=正常 1=禁用',
    `last_login_time` DATETIME  DEFAULT NULL COMMENT '最后登录时间',
    `deleted`      TINYINT      DEFAULT 0  COMMENT '逻辑删除：0=未删除 1=已删除',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
