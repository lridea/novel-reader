-- 小说查询增强功能索引优化
-- 为筛选条件添加索引，提高查询性能

USE novel_reader;

-- 添加字数字段（如果不存在）
-- 注意：如果字段已存在，此语句会报错，可以忽略
SET @exist = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'novel_reader' AND TABLE_NAME = 't_novel' AND COLUMN_NAME = 'word_count');
SET @sql = IF(@exist = 0, 'ALTER TABLE t_novel ADD COLUMN word_count BIGINT COMMENT ''总字数'' AFTER tags', 'SELECT ''word_count already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加字数字段索引
CREATE INDEX IF NOT EXISTS idx_word_count ON t_novel(word_count);

-- 添加复合索引（平台 + 状态 + 字数）
CREATE INDEX IF NOT EXISTS idx_platform_status_word_count ON t_novel(platform, status, word_count);

-- 添加复合索引（状态 + 字数 + 更新时间）
CREATE INDEX IF NOT EXISTS idx_status_word_count_update_time ON t_novel(status, word_count, latest_update_time);

-- 添加复合索引（平台 + 更新时间）
CREATE INDEX IF NOT EXISTS idx_platform_update_time ON t_novel(platform, latest_update_time);

-- 说明：
-- 1. word_count字段：如果已存在则跳过
-- 2. 索引使用CREATE INDEX IF NOT EXISTS语法，如果索引已存在则跳过
-- 3. 如果使用MySQL 8.0以下版本，可能需要先检查索引是否存在
