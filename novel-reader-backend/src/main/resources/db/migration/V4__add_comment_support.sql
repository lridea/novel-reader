-- V4__add_comment_support.sql

-- 添加评论数字段
ALTER TABLE t_novel ADD COLUMN comment_count INT DEFAULT 0 COMMENT '评论数' AFTER favorite_count;

-- 添加评论数字段索引
CREATE INDEX idx_comment_count ON t_novel(comment_count);

-- 创建评论表
CREATE TABLE IF NOT EXISTS t_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    parent_id BIGINT COMMENT '父评论ID（NULL表示顶层评论）',
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
    INDEX idx_created_at (created_at DESC),
    INDEX idx_novel_floor (novel_id, floor, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
