-- 添加点踩数字段到t_novel表
ALTER TABLE t_novel ADD COLUMN dislike_count INT DEFAULT 0 COMMENT '点踩数' AFTER comment_count;

-- 添加点踩数索引
CREATE INDEX idx_dislike_count ON t_novel(dislike_count);

-- 创建点踩表
CREATE TABLE t_dislike (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    novel_id BIGINT NOT NULL COMMENT '书籍ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_novel_id (novel_id),
    UNIQUE KEY uk_user_novel (user_id, novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书籍点踩表';
