-- 添加收藏数字段和索引
-- 创建时间: 2026-02-18
-- 版本: V3

-- 添加收藏数字段
ALTER TABLE t_novel ADD COLUMN favorite_count INT DEFAULT 0 COMMENT '收藏数' AFTER word_count;

-- 添加收藏数字段索引
CREATE INDEX IF NOT EXISTS idx_favorite_count ON t_novel(favorite_count);

-- 添加收藏数+状态复合索引（用于按收藏数筛选并按状态排序）
CREATE INDEX IF NOT EXISTS idx_status_favorite_count ON t_novel(status, favorite_count DESC);

-- 添加收藏数+更新时间复合索引（用于按收藏数排序后按更新时间排序）
CREATE INDEX IF NOT EXISTS idx_favorite_count_update_time ON t_novel(favorite_count DESC, latest_update_time DESC);
