# 读书网站 - 数据库设计文档（MySQL版 - 简化版）

## 📌 设计原则

1. **规范化设计**：遵循第三范式（3NF）
2. **性能优化**：合理使用索引、外键约束
3. **扩展性**：预留字段，便于后续扩展
4. **数据完整性**：使用约束、触发器保证数据一致性
5. **软删除**：重要数据使用 `deleted` 字段标记删除

---

## 🗄️ 数据库概览

### MySQL 数据库
**数据库名称**：`novel_reader`
**字符集**：`utf8mb4`（支持emoji）
**排序规则**：`utf8mb4_unicode_ci`

### 主要表结构（8张表）
1. `t_user` - 用户表
2. `t_novel` - 小说表（含前三章AI概括）
3. `t_novel_tag` - 小说标签关联表
4. `t_favorite` - 收藏表
5. `t_category` - 收藏分类表
6. `t_favorite_category` - 收藏与分类关联表
7. `t_crawler_task` - 爬虫任务表
8. `t_crawler_config` - 爬虫配置表

---

## 📋 表结构详解

### 1. 用户表 (t_user)

```sql
CREATE TABLE t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用, 1-正常',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否, 1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

---

### 2. 小说表 (t_novel)

```sql
CREATE TABLE t_novel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '小说ID',
    platform VARCHAR(50) NOT NULL COMMENT '平台来源：ciweimao/sf/ciyuanji/qidian',
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
    status TINYINT DEFAULT 1 COMMENT '状态：0-停更, 1-连载, 2-完结',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否, 1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_platform_novel_id (platform, novel_id),
    INDEX idx_platform (platform),
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_update_time (latest_update_time DESC),
    INDEX idx_status (status),
    FULLTEXT INDEX ft_title (title),
    FULLTEXT INDEX ft_author (author)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说表';
```

**说明**：
- `first_chapters_summary`：存储前3章的综合概括（一个整体的总结）
- 示例内容：
  ```
  主角从零开始修炼，在第一章中遇到了神秘老人，获得了修炼功法。
  第二章主角突破到练气期，展现出惊人的天赋。
  第三章主角决定出山历练，踏上了修仙之路。前三章主要讲述了主角的修炼开始和早期成长。
  ```
- 只在首次抓取时生成AI概括（将前3章内容综合后生成一个概括）
- 增量更新时不再重新生成
- 不存储章节具体内容，只存储综合概括

---

### 3. 小说标签关联表 (t_novel_tag)

```sql
CREATE TABLE t_novel_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_novel_id (novel_id),
    INDEX idx_tag_name (tag_name),
    FOREIGN KEY (novel_id) REFERENCES t_novel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说标签关联表';
```

---

### 4. 收藏表 (t_favorite)

```sql
CREATE TABLE t_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    note VARCHAR(500) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_novel (user_id, novel_id),
    INDEX idx_user_id (user_id),
    INDEX idx_novel_id (novel_id),
    INDEX idx_created_at (user_id, created_at DESC),
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    FOREIGN KEY (novel_id) REFERENCES t_novel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';
```

---

### 5. 收藏分类表 (t_category)

```sql
CREATE TABLE t_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(200) COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否, 1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_sort_order (user_id, sort_order),
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏分类表';
```

---

### 6. 收藏与分类关联表 (t_favorite_category)

```sql
CREATE TABLE t_favorite_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    favorite_id BIGINT NOT NULL COMMENT '收藏ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_favorite_category (favorite_id, category_id),
    INDEX idx_favorite_id (favorite_id),
    INDEX idx_category_id (category_id),
    FOREIGN KEY (favorite_id) REFERENCES t_favorite(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES t_category(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏与分类关联表';
```

---

### 7. 爬虫任务表 (t_crawler_task)

```sql
CREATE TABLE t_crawler_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    platform VARCHAR(50) NOT NULL COMMENT '平台名称',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/RUNNING/SUCCESS/FAILED',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    total_count INT DEFAULT 0 COMMENT '总数',
    success_count INT DEFAULT 0 COMMENT '成功数',
    failed_count INT DEFAULT 0 COMMENT '失败数',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_platform (platform),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫任务表';
```

---

### 8. 爬虫配置表 (t_crawler_config)

```sql
CREATE TABLE t_crawler_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    platform VARCHAR(50) NOT NULL COMMENT '平台名称',
    base_url VARCHAR(255) NOT NULL COMMENT '平台基础URL',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用, 1-启用',
    tags TEXT COMMENT '标签列表（JSON数组）',
    interval_hours INT DEFAULT 2 COMMENT '抓取间隔（小时）',
    max_retry INT DEFAULT 3 COMMENT '最大重试次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_platform (platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫配置表';
```

**初始数据**：
```sql
INSERT INTO t_crawler_config (platform, base_url, enabled, tags, interval_hours, max_retry) VALUES
('ciweimao', 'https://mip.ciweimao.com/', 1, '["玄幻", "修仙", "都市", "系统"]', 2, 3),
('sf', 'https://book.sfacg.com/', 1, '["玄幻", "轻小说", "二次元"]', 2, 3),
('ciyuanji', 'https://www.ciyuanji.com/', 1, '["玄幻", "仙侠", "都市"]', 2, 3),
('qidian', 'https://www.qidian.com/', 1, '["玄幻", "仙侠", "都市", "科幻"]', 2, 3);
```

---

## 🔗 ER 图

```
t_user (用户)
  ├─ t_category (收藏分类) [1:N]
  ├─ t_favorite (收藏) [1:N]
  │     └─ t_favorite_category (收藏分类关联) [N:N]
  │
t_novel (小说)
  ├─ t_novel_tag (标签) [1:N]
  └─ t_favorite (收藏) [1:N]

t_crawler_task (爬虫任务) [独立]
t_crawler_config (爬虫配置) [独立]
```

---

## 💾 数据库初始化脚本

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS novel_reader
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE novel_reader;

-- 执行上述建表语句

-- 插入初始数据
INSERT INTO t_crawler_config (platform, base_url, enabled, tags, interval_hours, max_retry) VALUES
('ciweimao', 'https://mip.ciweimao.com/', 1, '["玄幻", "修仙", "都市", "系统"]', 2, 3),
('sf', 'https://book.sfacg.com/', 1, '["玄幻", "轻小说", "二次元"]', 2, 3),
('ciyuanji', 'https://www.ciyuanji.com/', 1, '["玄幻", "仙侠", "都市"]', 2, 3),
('qidian', 'https://www.qidian.com/', 1, '["玄幻", "仙侠", "都市", "科幻"]', 2, 3);

-- 插入管理员账号（密码：admin123，BCrypt加密后的值）
INSERT INTO t_user (username, email, password, nickname, status) VALUES
('admin', 'admin@novelreader.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 1);
```

---

## 📝 MySQL全文搜索

```sql
-- 标题搜索
SELECT * FROM t_novel
WHERE MATCH(title) AGAINST('修仙' IN BOOLEAN MODE);

-- 作者搜索
SELECT * FROM t_novel
WHERE MATCH(author) AGAINST('作者名' IN BOOLEAN MODE);
```

---

## 📝 数据变更说明

### 简化内容
1. ❌ **删除章节表（t_chapter）**：不再存储具体章节内容
2. ❌ **删除更新日志表（t_update_log）**：不再记录更新历史
3. ✅ **新增AI概括字段**：在小说表中存储前3章的AI概括
4. ✅ **简化爬虫流程**：
   - 首次抓取：抓取前3章 + AI概括
   - 增量更新：只更新小说基本信息，不抓取章节

---

## 📝 下一步

数据库设计已完成（MySQL简化版），下一步将进行：
1. 后端API设计
2. 前端页面设计
3. **爬虫功能设计**（优先级最高）
4. 爬虫后台管理界面设计
