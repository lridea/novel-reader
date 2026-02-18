-- V5__add_comment_like_table.sql

-- 创建评论点赞表
CREATE TABLE IF NOT EXISTS t_comment_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    comment_id BIGINT NOT NULL COMMENT '评论ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_user_comment (user_id, comment_id),
    INDEX idx_user_id (user_id),
    INDEX idx_comment_id (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';
