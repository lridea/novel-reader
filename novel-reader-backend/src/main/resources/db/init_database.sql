-- ============================================================
-- Novel Reader 数据库初始化脚本
-- 创建时间: 2026-02-18
-- 说明: 完整的数据库初始化配置，包含所有表结构、索引和初始数据
-- ============================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS novel_reader
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE novel_reader;

-- ============================================================
-- 1. 小说表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_novel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    platform VARCHAR(50) NOT NULL COMMENT '平台来源: ciweimao/sf/ciyuanji',
    novel_id VARCHAR(100) NOT NULL COMMENT '平台内唯一ID',
    source_url VARCHAR(500) COMMENT '源网页地址',
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
    word_count BIGINT COMMENT '总字数',
    favorite_count INT DEFAULT 0 COMMENT '收藏数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    dislike_count INT DEFAULT 0 COMMENT '点踩数',
    tags TEXT COMMENT '标签列表(JSON数组)',
    user_tags TEXT COMMENT '用户标签（JSON数组格式）',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_platform_novel (platform, novel_id),
    INDEX idx_platform (platform),
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_update_time (latest_update_time),
    INDEX idx_status (status),
    INDEX idx_word_count (word_count),
    INDEX idx_platform_status_word_count (platform, status, word_count),
    INDEX idx_status_word_count_update_time (status, word_count, latest_update_time),
    INDEX idx_platform_update_time (platform, latest_update_time),
    INDEX idx_favorite_count (favorite_count),
    INDEX idx_status_favorite_count (status, favorite_count DESC),
    INDEX idx_favorite_count_update_time (favorite_count DESC, latest_update_time DESC),
    INDEX idx_comment_count (comment_count),
    INDEX idx_dislike_count (dislike_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说表';

-- ============================================================
-- 2. 爬虫配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_crawler_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    platform VARCHAR(50) NOT NULL COMMENT '平台名称',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用: 0-否, 1-是',
    tags TEXT COMMENT '标签列表(JSON数组)',
    crawl_interval INT DEFAULT 7200 COMMENT '抓取间隔(秒)',
    last_crawl_time DATETIME COMMENT '最后抓取时间',
    last_success_crawl_time DATETIME COMMENT '最后成功抓取时间',
    crawl_count INT DEFAULT 0 COMMENT '总抓取次数',
    fail_count INT DEFAULT 0 COMMENT '失败次数',
    last_error_message TEXT COMMENT '最后错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_platform (platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫配置表';

-- ============================================================
-- 3. 爬虫任务日志表
-- ============================================================
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

-- ============================================================
-- 4. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名（账号名，用于登录）',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    email VARCHAR(100) COMMENT '邮箱（可选）',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称（用于前台展示）',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER-普通用户, ADMIN-管理员',
    enabled TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_nickname (nickname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 5. 用户收藏表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    category_id BIGINT COMMENT '收藏分类ID',
    platform VARCHAR(50) COMMENT '平台',
    platform_novel_id VARCHAR(100) COMMENT '平台内小说ID',
    note VARCHAR(500) COMMENT '备注',
    latest_chapter_title VARCHAR(200) COMMENT '最新章节标题（快照）',
    latest_update_time DATETIME COMMENT '最新更新时间（快照）',
    has_update TINYINT DEFAULT 0 COMMENT '是否有更新: 0-否, 1-是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_user_novel_category (user_id, novel_id, category_id),
    INDEX idx_user_id (user_id),
    INDEX idx_novel_id (novel_id),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- ============================================================
-- 6. 收藏分类表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_favorite_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(200) COMMENT '描述',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认收藏夹: 0-否, 1-是',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_user_name (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏分类表';

-- ============================================================
-- 7. 评论表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    parent_id BIGINT COMMENT '父评论ID（NULL表示顶层评论）',
    reply_to_user_id BIGINT COMMENT '被回复的用户ID（用于回复的回复场景）',
    reply_to_comment_id BIGINT COMMENT '被回复的评论ID（用于回复的回复场景）',
    floor INT DEFAULT 1 COMMENT '楼层：1-顶层评论，2-回复评论',
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    reply_count INT DEFAULT 0 COMMENT '回复数（仅顶层评论）',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_novel_id (novel_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_user_id (user_id),
    INDEX idx_reply_to_user_id (reply_to_user_id),
    INDEX idx_reply_to_comment_id (reply_to_comment_id),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_novel_floor (novel_id, floor, created_at DESC),
    INDEX idx_novel_floor_like (novel_id, floor, deleted, like_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================================
-- 8. 评论点赞表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_comment_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    comment_id BIGINT NOT NULL COMMENT '评论ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_user_comment (user_id, comment_id),
    INDEX idx_user_id (user_id),
    INDEX idx_comment_id (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- ============================================================
-- 9. 敏感词表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_sensitive_word (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    word VARCHAR(100) NOT NULL COMMENT '敏感词',
    category VARCHAR(50) COMMENT '分类：政治、色情、暴力、广告等',
    severity TINYINT DEFAULT 1 COMMENT '严重程度：1-轻微，2-严重，3-禁止',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_word (word),
    INDEX idx_category (category),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- ============================================================
-- 10. 点踩表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_dislike (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    novel_id BIGINT NOT NULL COMMENT '书籍ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_novel_id (novel_id),
    UNIQUE KEY uk_user_novel (user_id, novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书籍点踩表';

-- ============================================================
-- 11. 标签审核表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_tag_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    novel_id BIGINT NOT NULL COMMENT '书籍ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tag VARCHAR(100) NOT NULL COMMENT '标签名称',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核, 1-已通过, 2-已拒绝',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    reviewed_by BIGINT COMMENT '审核人ID',
    reviewed_at TIMESTAMP NULL COMMENT '审核时间',
    reason VARCHAR(500) COMMENT '拒绝原因',
    
    INDEX idx_novel_id (novel_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at),
    UNIQUE KEY uk_user_novel_tag (user_id, novel_id, tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签审核表';

-- ============================================================
-- 12. 用户标签表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_user_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    novel_id BIGINT NOT NULL COMMENT '书籍ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tag VARCHAR(100) NOT NULL COMMENT '标签名称',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_novel_id (novel_id),
    INDEX idx_user_id (user_id),
    INDEX idx_tag (tag),
    UNIQUE KEY uk_user_novel_tag (user_id, novel_id, tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户标签表';

-- ============================================================
-- 13. 标签表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '标签名称',
    source VARCHAR(20) NOT NULL DEFAULT 'CRAWL' COMMENT '来源: CRAWL-爬取, USER-用户添加',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_name (name),
    INDEX idx_source (source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 初始化爬虫配置数据
INSERT INTO t_crawler_config (platform, enabled, tags, crawl_interval) VALUES
('ciweimao', 1, '["橘子"]', 7200),
('sf', 1, '["橘味"]', 7200),
('ciyuanji', 1, '["百合","变百"]', 7200)
ON DUPLICATE KEY UPDATE tags = VALUES(tags), crawl_interval = VALUES(crawl_interval);

-- 初始化敏感词数据
INSERT INTO t_sensitive_word (word, category, severity, enabled) VALUES
('暴力', '暴力', 2, 1),
('色情', '色情', 3, 1),
('毒品', '违法', 3, 1),
('赌博', '违法', 3, 1),
('诈骗', '违法', 3, 1),
('杀人', '暴力', 3, 1),
('放火', '暴力', 3, 1),
('爆炸', '暴力', 3, 1),
('恐怖', '暴力', 2, 1),
('枪支', '违法', 3, 1),
('弹药', '违法', 3, 1),
('卖淫', '色情', 3, 1),
('嫖娼', '色情', 3, 1),
('赌博网站', '广告', 2, 1),
('代开发票', '广告', 2, 1)
ON DUPLICATE KEY UPDATE enabled = VALUES(enabled);

-- ============================================================
-- 完成
-- ============================================================
