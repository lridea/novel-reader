-- 创建标签审核表
CREATE TABLE t_tag_audit (
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
    UNIQUE KEY uk_user_novel_tag (user_id, novel_id, tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签审核表';

-- 创建用户标签表
CREATE TABLE t_user_tag (
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

-- 添加userTags字段到t_novel表
ALTER TABLE t_novel ADD COLUMN user_tags TEXT COMMENT '用户标签（JSON数组格式）' AFTER tags;
