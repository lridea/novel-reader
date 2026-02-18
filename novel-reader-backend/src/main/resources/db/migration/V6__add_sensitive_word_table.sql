-- V6__add_sensitive_word_table.sql

-- 创建敏感词表
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

-- 插入一些常见的敏感词（示例数据）
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
('代开发票', '广告', 2, 1);
