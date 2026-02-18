-- 添加updated_at索引到t_tag_audit表
ALTER TABLE t_tag_audit ADD INDEX idx_updated_at (updated_at);

-- 修改user_tags字段类型为TEXT
ALTER TABLE t_novel MODIFY COLUMN user_tags TEXT COMMENT '用户标签（JSON数组格式）';
