# Novel Reader 后端API文档

**生成时间**: 2026-02-18  
**版本**: v1.1  
**基础路径**: `/api/crawler`

---

## 📋 接口清单

| 序号 | 接口路径 | 方法 | 描述 | 状态 |
|------|---------|------|------|------|
| 1 | `/health` | GET | 健康检查 | ✅ 已实现 |
| 2 | `/trigger` | POST | 触发所有爬虫 | ✅ 已实现 |
| 3 | `/trigger/{platform}` | POST | 触发指定平台爬虫 | ✅ 已实现 |
| 4 | `/test/{platform}` | GET | 测试爬虫（调试用） | ✅ 已实现 |
| 5 | `/status/{platform}` | GET | 获取平台状态 | ✅ 已实现 |
| 6 | `/novels` | GET | 获取所有小说（旧接口，兼容） | ✅ 已实现 |
| 7 | `/novels/page` | GET | 分页查询小说（新接口，支持筛选） | ✅ 已实现 |
| 8 | `/novels/platform/{platform}` | GET | 根据平台获取小说 | ✅ 已实现 |
| 9 | `/novels/tags` | GET | 获取所有标签 | ✅ 已实现 |
| 10 | `/novels/tags/platform/{platform}` | GET | 获取指定平台的标签 | ✅ 已实现 |
| 11 | `/configs` | GET | 获取所有爬虫配置 | ✅ 已实现 |
| 12 | `/configs/{id}` | GET | 根据ID获取配置 | ✅ 已实现 |
| 13 | `/configs/{id}` | PUT | 更新配置 | ✅ 已实现 |
| 14 | `/configs/platform/{platform}` | GET | 根据平台获取配置 | ✅ 已实现 |
| 15 | `/crawlers` | GET | 获取所有爬虫 | ✅ 已实现 |
| 16 | `/tasks` | GET | 获取任务列表（分页） | ✅ 已实现 |
| 17 | `/tasks/{id}` | GET | 获取任务详情 | ✅ 已实现 |
| 18 | `/tasks` | POST | 创建任务 | ✅ 已实现 |
| 19 | `/tasks/{id}` | PUT | 更新任务 | ✅ 已实现 |

---

## 📊 数据模型

### Novel（小说实体）

```json
{
  "id": 1,
  "platform": "ciweimao",
  "novelId": "123456",
  "title": "修仙从系统开始",
  "author": "张三",
  "description": "一个修仙的故事",
  "coverUrl": "https://example.com/cover.jpg",
  "latestChapterTitle": "第100章 大结局",
  "tags": "[\"玄幻\", \"修仙\"]",
  "wordCount": 1000000,
  "latestUpdateTime": "2026-02-18T10:00:00",
  "firstChaptersSummary": "前3章概括内容...",
  "lastCrawlTime": "2026-02-18T09:00:00",
  "crawlCount": 10,
  "status": 1,
  "deleted": 0,
  "createdAt": "2026-02-15T10:00:00",
  "updatedAt": "2026-02-18T10:00:00"
}
```

**字段说明**：
- `platform`: 平台标识（ciweimao/sf/ciyuanji/qidian）
- `status`: 状态（0-停更, 1-连载, 2-完结）
- `deleted`: 是否删除（0-否, 1-是）
- `tags`: JSON数组字符串格式

### CrawlerConfig（爬虫配置实体）

```json
{
  "id": 1,
  "platform": "ciweimao",
  "baseUrl": "https://mip.ciweimao.com",
  "enabled": 1,
  "tags": "[\"玄幻\", \"修仙\", \"都市\"]",
  "crawlInterval": 7200,
  "maxRetry": 3,
  "lastCrawlTime": "2026-02-18T09:00:00",
  "lastSuccessCrawlTime": "2026-02-18T09:30:00",
  "isRunning": 0,
  "runningTaskId": null,
  "crawlCount": 10,
  "failCount": 1,
  "lastErrorMessage": null,
  "createdAt": "2026-02-15T10:00:00",
  "updatedAt": "2026-02-18T10:00:00"
}
```

**字段说明**：
- `enabled`: 是否启用（0-否, 1-是）
- `isRunning`: 是否正在运行（0-否, 1-是）
- `crawlInterval`: 抓取间隔（秒）

### CrawlerTask（爬虫任务实体）

```json
{
  "id": 1,
  "platform": "ciweimao",
  "taskType": "scheduled",
  "status": "SUCCESS",
  "startTime": "2026-02-18T09:00:00",
  "endTime": "2026-02-18T09:30:00",
  "totalCount": 500,
  "successCount": 480,
  "failCount": 20,
  "logs": "执行日志内容...",
  "errorMessage": null,
  "createdAt": "2026-02-18T09:00:00"
}
```

**字段说明**：
- `taskType`: 任务类型（manual-手动, scheduled-定时）
- `status`: 任务状态（PENDING-等待中, RUNNING-运行中, SUCCESS-成功, FAILED-失败）

---

## 📝 接口详情

### 1. 健康检查

**请求**：
```
GET /api/crawler/health
```

**响应**：
```json
{
  "status": "ok",
  "crawlers": 3,
  "timestamp": 1739853600000
}
```

---

### 2. 触发所有爬虫

**请求**：
```
POST /api/crawler/trigger
```

**响应**：
```json
{
  "success": true,
  "message": "爬虫任务已启动"
}
```

---

### 3. 触发指定平台爬虫

**请求**：
```
POST /api/crawler/trigger/{platform}
```

**路径参数**：
- `platform`: 平台标识（ciweimao/sf/ciyuanji/qidian）

**响应**：
```json
{
  "success": true,
  "message": "平台 ciweimao 爬虫任务已启动"
}
```

**错误响应**：
```json
{
  "success": false,
  "message": "平台 ciweimao 已有任务在运行"
}
```

---

### 4. 测试爬虫

**请求**：
```
GET /api/crawler/test/{platform}?tag=玄幻
```

**路径参数**：
- `platform`: 平台标识

**查询参数**：
- `tag`: 测试标签（可选，默认"动漫穿越"）

**响应**：
```json
{
  "success": true,
  "platform": "ciweimao",
  "tag": "玄幻",
  "count": 10,
  "novels": [
    {
      "novelId": "123456",
      "title": "修仙从系统开始",
      "author": "张三",
      "coverUrl": "https://example.com/cover.jpg",
      "latestChapter": "第100章 大结局",
      "updateTime": "2026-02-18T10:00:00"
    }
  ]
}
```

---

### 5. 获取平台状态

**请求**：
```
GET /api/crawler/status/{platform}
```

**路径参数**：
- `platform`: 平台标识

**响应**：
```json
{
  "exists": true,
  "platform": "ciweimao",
  "enabled": 1,
  "isRunning": false,
  "lastCrawlTime": "2026-02-18T09:00:00",
  "lastSuccessCrawlTime": "2026-02-18T09:30:00",
  "crawlCount": 10,
  "failCount": 1,
  "lastError": null
}
```

---

### 6. 获取所有小说（旧接口，兼容）

**请求**：
```
GET /api/crawler/novels
```

**响应**：
```json
[
  {
    "id": 1,
    "platform": "ciweimao",
    "novelId": "123456",
    "title": "修仙从系统开始",
    ...
  }
]
```

⚠️ **注意**: 此接口直接返回数组，不推荐用于前端分页

---

### 7. 分页查询小说（新接口，支持筛选）⭐⭐⭐

**请求**：
```
GET /api/crawler/novels/page?page=0&size=10&platform=ciweimao&keyword=修仙&status=1&tag=玄幻&wordCountMin=10w&wordCountMax=200w&sortBy=wordCount&sortOrder=desc
```

**查询参数**：
- `page`: 页码（从0开始，默认0）
- `size`: 每页数量（默认10）
- `platform`: 平台筛选（可选，ciweimao/sf/ciyuanji/qidian）
- `keyword`: 关键词搜索（可选，匹配标题或作者）
- `status`: 状态筛选（可选，0-停更, 1-连载, 2-完结）
- `tag`: 标签筛选（可选，单选）
- `wordCountMin`: 最小字数（可选，全部/10w/30w/50w/100w/200w）
- `wordCountMax`: 最大字数（可选，全部/10w/30w/50w/100w/200w）
- `sortBy`: 排序字段（可选，默认updateTime，updateTime/wordCount）
- `sortOrder`: 排序方向（可选，默认desc，asc/desc）

**字数范围说明**：
- `wordCountMin`（最小字数）：
  - 全部 = 不限制最小值
  - 10w = >= 100,000（字数 >= 10万字）
  - 30w = >= 300,000（字数 >= 30万字）
  - 50w = >= 500,000（字数 >= 50万字）
  - 100w = >= 1,000,000（字数 >= 100万字）
  - 200w = >= 2,000,000（字数 >= 200万字）

- `wordCountMax`（最大字数）：
  - 全部 = 不限制最大值
  - 10w = <= 100,000（字数 <= 10万字）
  - 30w = <= 300,000（字数 <= 30万字）
  - 50w = <= 500,000（字数 <= 50万字）
  - 100w = <= 1,000,000（字数 <= 100万字）
  - 200w = <= 2,000,000（字数 <= 200万字）

**请求示例**：

示例1：所有筛选条件
```http
GET /api/crawler/novels/page?page=0&size=20&platform=ciweimao&keyword=修仙&status=1&tag=玄幻&wordCountMin=10w&wordCountMax=200w&sortBy=wordCount&sortOrder=desc
```

示例2：仅最小字数
```http
GET /api/crawler/novels/page?wordCountMin=10w
```

示例3：仅最大字数
```http
GET /api/crawler/novels/page?wordCountMax=200w
```

示例4：字数范围（10w-200w）
```http
GET /api/crawler/novels/page?wordCountMin=10w&wordCountMax=200w
```

示例5：按字数降序排序
```http
GET /api/crawler/novels/page?sortBy=wordCount&sortOrder=desc
```

示例6：按状态筛选
```http
GET /api/crawler/novels/page?status=1
```

示例7：按标签筛选
```http
GET /api/crawler/novels/page?tag=玄幻
```

**响应**：
```json
{
  "content": [
    {
      "id": 1,
      "platform": "ciweimao",
      "novelId": "123456",
      "title": "修仙从系统开始",
      "author": "张三",
      "description": "一个修仙的故事",
      "coverUrl": "https://example.com/cover.jpg",
      "latestChapterTitle": "第100章 大结局",
      "tags": "[\"玄幻\", \"修仙\"]",
      "wordCount": 1000000,
      "latestUpdateTime": "2026-02-18T10:00:00",
      "firstChaptersSummary": "前3章概括内容...",
      "lastCrawlTime": "2026-02-18T09:00:00",
      "crawlCount": 10,
      "status": 1,
      "deleted": 0,
      "createdAt": "2026-02-15T10:00:00",
      "updatedAt": "2026-02-18T10:00:00"
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

✅ **推荐前端使用此接口**
✅ **支持多条件组合查询**
✅ **支持动态排序**

---

### 8. 根据平台获取小说

**请求**：
```
GET /api/crawler/novels/platform/{platform}
```

**路径参数**：
- `platform`: 平台标识

**响应**：
```json
[
  {
    "id": 1,
    "platform": "ciweimao",
    "novelId": "123456",
    "title": "修仙从系统开始",
    ...
  }
]
```

---

### 9. 获取所有标签 ⭐⭐

**请求**：
```
GET /api/crawler/novels/tags
```

**响应**：
```json
{
  "success": true,
  "tags": [
    {
      "tag_name": "玄幻",
      "tag_count": 1250
    },
    {
      "tag_name": "修仙",
      "tag_count": 980
    },
    {
      "tag_name": "穿越",
      "tag_count": 856
    }
  ]
}
```

**字段说明**：
- `tag_name`: 标签名称
- `tag_count`: 该标签的小说数量

**说明**：
- 标签按小说数量降序排序
- 最多返回100个标签
- 建议前端缓存标签列表（1小时）

---

### 10. 获取指定平台的标签 ⭐⭐

**请求**：
```
GET /api/crawler/novels/tags/platform/{platform}
```

**路径参数**：
- `platform`: 平台标识（ciweimao/sf/ciyuanji/qidian）

**响应**：
```json
{
  "success": true,
  "platform": "ciweimao",
  "tags": [
    {
      "tag_name": "玄幻",
      "tag_count": 450
    },
    {
      "tag_name": "修仙",
      "tag_count": 320
    },
    {
      "tag_name": "穿越",
      "tag_count": 280
    }
  ]
}
```

**字段说明**：
- `platform`: 平台标识
- `tag_name`: 标签名称
- `tag_count`: 该标签在该平台的小说数量

**说明**：
- 标签按小说数量降序排序
- 最多返回100个标签
- 建议前端缓存标签列表（1小时）

---

### 11. 获取所有爬虫配置

**请求**：
```
GET /api/crawler/configs
```

**响应**：
```json
[
  {
    "id": 1,
    "platform": "ciweimao",
    "baseUrl": "https://mip.ciweimao.com",
    "enabled": 1,
    "tags": "[\"玄幻\", \"修仙\", \"都市\"]",
    "crawlInterval": 7200,
    "maxRetry": 3,
    ...
  }
]
```

---

### 11. 获取所有爬虫配置

**请求**：
```
GET /api/crawler/configs
```

**响应**：
```json
[
  {
    "id": 1,
    "platform": "ciweimao",
    "baseUrl": "https://mip.ciweimao.com",
    "enabled": 1,
    "tags": "[\"玄幻\", \"修仙\", \"都市\"]",
    "crawlInterval": 7200,
    "maxRetry": 3,
    ...
  }
]
```

---

### 12. 根据ID获取配置

**请求**：
```
GET /api/crawler/configs/{id}
```

**路径参数**：
- `id`: 配置ID

**响应**：
```json
{
  "id": 1,
  "platform": "ciweimao",
  "baseUrl": "https://mip.ciweimao.com",
  "enabled": 1,
  ...
}
```

---

### 13. 更新配置 ⭐

**请求**：
```
PUT /api/crawler/configs/{id}
Content-Type: application/json

{
  "enabled": 1,
  "tags": "[\"玄幻\", \"修仙\"]",
  "crawlInterval": 7200,
  "maxRetry": 3,
  "baseUrl": "https://mip.ciweimao.com"
}
```

**路径参数**：
- `id`: 配置ID

**请求体**（所有字段可选）：
- `enabled`: 是否启用
- `tags`: 标签列表（JSON字符串）
- `crawlInterval`: 抓取间隔（秒）
- `maxRetry`: 最大重试次数
- `baseUrl`: 基础URL

**响应**：
```json
{
  "success": true,
  "message": "配置更新成功",
  "config": {
    "id": 1,
    "platform": "ciweimao",
    "enabled": 1,
    "tags": "[\"玄幻\", \"修仙\"]",
    "crawlInterval": 7200,
    ...
  }
}
```

---

### 14. 根据平台获取配置

**请求**：
```
GET /api/crawler/configs/platform/{platform}
```

**路径参数**：
- `platform`: 平台标识

**响应**：
```json
{
  "id": 1,
  "platform": "ciweimao",
  "baseUrl": "https://mip.ciweimao.com",
  ...
}
```

---

### 15. 获取所有爬虫

**请求**：
```
GET /api/crawler/crawlers
```

**响应**：
```json
{
  "ciweimao": "CiweimaoCrawler",
  "sf": "SfCrawler",
  "ciyuanji": "CiyuanjiCrawler"
}
```

---

### 16. 获取任务列表（分页）⭐

**请求**：
```
GET /api/crawler/tasks?page=0&size=10&platform=ciweimao&status=SUCCESS
```

**查询参数**：
- `page`: 页码（从0开始，默认0）
- `size`: 每页数量（默认10）
- `platform`: 平台筛选（可选）
- `status`: 状态筛选（可选）

**响应**：
```json
{
  "content": [
    {
      "id": 1,
      "platform": "ciweimao",
      "taskType": "scheduled",
      "status": "SUCCESS",
      "startTime": "2026-02-18T09:00:00",
      "endTime": "2026-02-18T09:30:00",
      "totalCount": 500,
      "successCount": 480,
      "failCount": 20,
      "logs": "2026-02-18 09:00:00 [INFO] 开始执行爬虫任务\n...",
      "errorMessage": null,
      "createdAt": "2026-02-18T09:00:00"
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

---

### 17. 获取任务详情 ⭐

**请求**：
```
GET /api/crawler/tasks/{id}
```

**路径参数**：
- `id`: 任务ID

**响应**：
```json
{
  "id": 1,
  "platform": "ciweimao",
  "taskType": "scheduled",
  "status": "SUCCESS",
  "startTime": "2026-02-18T09:00:00",
  "endTime": "2026-02-18T09:30:00",
  "totalCount": 500,
  "successCount": 480,
  "failCount": 20,
  "logs": "2026-02-18 09:00:00 [INFO] 开始执行爬虫任务\n2026-02-18 09:00:05 [INFO] 加载平台配置\n...",
  "errorMessage": null,
  "createdAt": "2026-02-18T09:00:00"
}
```

---

### 18. 创建任务

**请求**：
```
POST /api/crawler/tasks
Content-Type: application/json

{
  "platform": "ciweimao",
  "taskType": "scheduled",
  "status": "PENDING"
}
```

**响应**：
```json
{
  "id": 1,
  "platform": "ciweimao",
  "taskType": "scheduled",
  "status": "PENDING",
  ...
}
```

---

### 19. 更新任务

**请求**：
```
PUT /api/crawler/tasks/{id}
Content-Type: application/json

{
  "status": "SUCCESS",
  "endTime": "2026-02-18T09:30:00",
  "totalCount": 500,
  "successCount": 480,
  "failCount": 20,
  "logs": "执行日志...",
  "errorMessage": null
}
```

**响应**：
```json
{
  "id": 1,
  "platform": "ciweimao",
  "taskType": "scheduled",
  "status": "SUCCESS",
  ...
}
```

---

## ✅ 接口完成状态

### 爬虫核心接口（不涉及爬虫模块）
- ✅ 健康检查
- ✅ 触发爬虫（全部/指定平台）
- ✅ 测试爬虫
- ✅ 获取平台状态
- ✅ 获取所有爬虫

### 小说管理接口
- ✅ 获取所有小说（旧接口，兼容）
- ✅ **分页查询小说（新接口，支持筛选）**
- ✅ 根据平台获取小说

### 配置管理接口
- ✅ 获取所有配置
- ✅ 根据ID获取配置
- ✅ **更新配置**
- ✅ 根据平台获取配置

### 任务管理接口（新增）⭐
- ✅ **获取任务列表（分页）**
- ✅ **获取任务详情**
- ✅ 创建任务
- ✅ 更新任务

---

## 🎯 前后端完全匹配 ✅

所有前端需要的接口已全部实现，前后端完全匹配！

| 前端页面 | 需要的接口 | 后端状态 |
|---------|----------|---------|
| Dashboard | `/novels/page`, `/configs`, `/trigger` | ✅ 已实现 |
| Novels | `/novels/page`, `/novels/platform/{platform}` | ✅ 已实现 |
| Platforms | `/configs`, `/status/{platform}`, `/trigger/{platform}`, `/configs/{id}` (PUT) | ✅ 已实现 |
| Tasks | `/tasks` | ✅ 已实现 |
| TaskDetail | `/tasks/{id}` | ✅ 已实现 |
| NovelDetail | `/novels/page` | ✅ 已实现 |

---

## 💡 使用建议

### 推荐前端使用的接口

1. **小说列表**：使用 `GET /api/crawler/novels/page`（支持分页和筛选）
2. **配置更新**：使用 `PUT /api/crawler/configs/{id}`（部分更新）
3. **任务管理**：使用 `GET /api/crawler/tasks` 和 `GET /api/crawler/tasks/{id}`

### 兼容性保留

为了保持向后兼容，保留了以下旧接口：
- `GET /api/crawler/novels`（直接返回数组）
- `GET /api/crawler/configs`（返回配置列表）

---

*文档生成时间: 2026-02-18* 🦞
