-- 读书网站数据库初始化脚本
-- 数据库: novel_reader
-- 字符集: utf8mb4

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS novel_reader
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE novel_reader;

-- 小说表
CREATE TABLE IF NOT EXISTS t_novel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    platform VARCHAR(50) NOT NULL COMMENT '平台来源: ciweimao/sf/ciyuanji',
    novel_id VARCHAR(100) NOT NULL COMMENT '平台内唯一ID',
    title VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(100) COMMENT '作者',
    description TEXT COMMENT '简介',
    cover_url VARCHAR(500) COMMENT '封面URL',
    latest_chapter_title VARCHAR(200) COMMENT '最新章节标题',
    latest_update_time DATETIME COMMENT '最新更新时间',
    first_chapters_summary TEXT COMMENT '前3章综合概括',
    last_crawl_time DATETIME COMMENT '最后抓取时间',
    crawl_count INT DEFAULT 0 COMMENT '抓取次数',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-停更, 1-连载, 2-完结',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_platform_novel (platform, novel_id),
    INDEX idx_platform (platform),
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_update_time (latest_update_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说表';

-- 爬虫配置表
CREATE TABLE IF NOT EXISTS t_crawler_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    platform VARCHAR(50) NOT NULL COMMENT '平台名称',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用: 0-否, 1-是',
    tags TEXT COMMENT '标签列表(JSON数组)',
    crawl_interval INT DEFAULT 7200 COMMENT '抓取间隔(秒)',
    last_crawl_time DATETIME COMMENT '最后抓取时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_platform (platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫配置表';

-- 爬虫任务日志表
CREATE TABLE IF NOT EXISTS t_crawler_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    platform VARCHAR(50) NOT NULL COMMENT '平台名称',
    task_type VARCHAR(50) DEFAULT 'SCHEDULED' COMMENT '任务类型: SCHEDULED-定时任务, MANUAL-手动触发',
    status VARCHAR(20) DEFAULT 'RUNNING' COMMENT '任务状态: RUNNING-运行中, SUCCESS-成功, FAILED-失败',
    total_count INT DEFAULT 0 COMMENT '总数量',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    fail_count INT DEFAULT 0 COMMENT '失败数量',
    error_message TEXT COMMENT '错误信息',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_platform (platform),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫任务日志表';

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    email VARCHAR(100) COMMENT '邮箱',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户收藏表
CREATE TABLE IF NOT EXISTS t_user_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    category VARCHAR(50) DEFAULT 'default' COMMENT '分类名称',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_user_novel (user_id, novel_id),
    INDEX idx_user_id (user_id),
    INDEX idx_novel_id (novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 初始化爬虫配置数据
INSERT INTO t_crawler_config (platform, enabled, tags, crawl_interval) VALUES
('ciweimao', 1, '["橘子"]', 7200),
('sf', 1, '["橘味"]', 7200),
('ciyuanji', 1, '["百合","变百"]', 7200)
ON DUPLICATE KEY UPDATE tags = VALUES(tags), crawl_interval = VALUES(crawl_interval);
