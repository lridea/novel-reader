# 读书网站 - 后端API接口设计文档

## 📌 设计原则

1. **RESTful风格**：遵循REST架构风格
2. **统一响应格式**：所有接口返回统一格式
3. **版本控制**：URL路径中包含版本号 `/api/v1`
4. **认证授权**：使用JWT Token
5. **接口文档**：自动生成OpenAPI文档

---

## 📦 统一响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 业务数据
  },
  "timestamp": 1234567890
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "参数错误",
  "data": null,
  "timestamp": 1234567890
}
```

### 错误码定义

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 🔐 认证机制

### JWT Token
- **Header**：`Authorization: Bearer <token>`
- **Token有效期**：7天
- **刷新机制**：Token过期后返回401，前端重新登录

### 公开接口（无需认证）
- POST `/api/v1/auth/register` - 用户注册
- POST `/api/v1/auth/login` - 用户登录
- GET `/api/v1/novels` - 获取小说列表
- GET `/api/v1/novels/{id}` - 获取小说详情

### 私有接口（需要认证）
- 所有收藏相关接口
- 用户信息接口
- 管理员接口

---

## 📚 API接口列表

### 1. 认证模块 (Auth)

#### 1.1 用户注册
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Test@123"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

---

#### 1.2 用户登录
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "Test@123"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "nickname": "测试用户",
      "avatarUrl": null
    }
  }
}
```

---

#### 1.3 获取当前用户信息
```http
GET /api/v1/auth/me
Authorization: Bearer <token>
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "nickname": "测试用户",
    "avatarUrl": null
  }
}
```

---

### 2. 小说模块 (Novel)

#### 2.1 获取小说列表（分页）
```http
GET /api/v1/novels?page=1&size=20&platform=ciweimao&tag=玄幻&keyword=修仙
```

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |
| platform | string | 否 | 平台过滤（多选用逗号分隔） |
| tag | string | 否 | 标签过滤（多选用逗号分隔） |
| keyword | string | 否 | 搜索关键词（书名/作者） |
| sortBy | string | 否 | 排序字段（updateTime/createdAt） |
| sortOrder | string | 否 | 排序方向（asc/desc），默认desc |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "platform": "ciweimao",
        "title": "修仙从系统开始",
        "author": "作者名",
        "coverUrl": "https://...",
        "tags": ["玄幻", "系统"],
        "description": "内容简介...",
        "latestChapterTitle": "第100章 突破",
        "latestUpdateTime": "2026-02-17T12:00:00Z",
        "isFavorited": false
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 1000,
      "totalPages": 50
    }
  }
}
```

---

#### 2.2 获取小说详情
```http
GET /api/v1/novels/{id}
Authorization: Bearer <token> (可选)
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "platform": "ciweimao",
    "title": "修仙从系统开始",
    "author": "作者名",
    "coverUrl": "https://...",
    "tags": ["玄幻", "系统"],
    "description": "完整内容简介...",
    "latestChapterTitle": "第100章 突破",
    "latestUpdateTime": "2026-02-17T12:00:00Z",
    "status": 1,
    "isFavorited": true,
    "latestChapter": {
      "id": 100,
      "chapterTitle": "第100章 突破",
      "aiSummary": "主角在突破过程中遇到了...",
      "createdAt": "2026-02-17T12:00:00Z"
    },
    "recentChapters": [
      {
        "id": 99,
        "chapterTitle": "第99章 准备",
        "createdAt": "2026-02-16T12:00:00Z"
      }
    ]
  }
}
```

---

#### 2.3 搜索小说
```http
GET /api/v1/novels/search?q=修仙&page=1&size=20
```

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | string | 是 | 搜索关键词 |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |

**响应**：同小说列表格式

---

### 3. 收藏模块 (Favorite)

#### 3.1 添加收藏
```http
POST /api/v1/favorites
Authorization: Bearer <token>
Content-Type: application/json

{
  "novelId": 1,
  "note": "很好看的小说"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "收藏成功",
  "data": {
    "id": 1,
    "novelId": 1,
    "note": "很好看的小说",
    "createdAt": "2026-02-17T12:00:00Z"
  }
}
```

---

#### 3.2 取消收藏
```http
DELETE /api/v1/favorites/{novelId}
Authorization: Bearer <token>
```

**响应**：
```json
{
  "code": 200,
  "message": "取消成功",
  "data": null
}
```

---

#### 3.3 获取收藏列表
```http
GET /api/v1/favorites?page=1&size=20&categoryId=1&sortBy=updateTime
Authorization: Bearer <token>
```

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |
| categoryId | int | 否 | 分类ID（不传则查询全部） |
| sortBy | string | 否 | 排序字段（updateTime/createdAt） |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "novel": {
          "id": 1,
          "title": "修仙从系统开始",
          "author": "作者名",
          "coverUrl": "https://...",
          "latestChapterTitle": "第100章 突破",
          "latestUpdateTime": "2026-02-17T12:00:00Z",
          "hasUpdate": true
        },
        "note": "很好看的小说",
        "categories": ["默认分类"],
        "createdAt": "2026-02-17T12:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 100,
      "totalPages": 5
    }
  }
}
```

---

#### 3.4 更新收藏备注
```http
PUT /api/v1/favorites/{novelId}/note
Authorization: Bearer <token>
Content-Type: application/json

{
  "note": "新的备注"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "note": "新的备注"
  }
}
```

---

### 4. 收藏分类模块 (Category)

#### 4.1 创建分类
```http
POST /api/v1/categories
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "玄幻小说",
  "description": "我喜欢的玄幻小说"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "name": "玄幻小说",
    "description": "我喜欢的玄幻小说",
    "sortOrder": 0,
    "novelCount": 0
  }
}
```

---

#### 4.2 获取分类列表
```http
GET /api/v1/categories
Authorization: Bearer <token>
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "玄幻小说",
      "description": "我喜欢的玄幻小说",
      "sortOrder": 0,
      "novelCount": 50,
      "createdAt": "2026-02-17T12:00:00Z"
    },
    {
      "id": 2,
      "name": "都市小说",
      "description": "都市题材",
      "sortOrder": 1,
      "novelCount": 30,
      "createdAt": "2026-02-17T12:00:00Z"
    }
  ]
}
```

---

#### 4.3 更新分类
```http
PUT /api/v1/categories/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "玄幻修仙",
  "description": "玄幻修仙类小说"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "name": "玄幻修仙",
    "description": "玄幻修仙类小说"
  }
}
```

---

#### 4.4 删除分类
```http
DELETE /api/v1/categories/{id}
Authorization: Bearer <token>
```

**响应**：
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

#### 4.5 将收藏添加到分类
```http
POST /api/v1/favorites/{novelId}/categories
Authorization: Bearer <token>
Content-Type: application/json

{
  "categoryIds": [1, 2]
}
```

**响应**：
```json
{
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

---

#### 4.6 从分类中移除收藏
```http
DELETE /api/v1/favorites/{novelId}/categories/{categoryId}
Authorization: Bearer <token>
```

**响应**：
```json
{
  "code": 200,
  "message": "移除成功",
  "data": null
}
```

---

### 5. 用户模块 (User)

#### 5.1 获取用户信息
```http
GET /api/v1/users/{id}
Authorization: Bearer <token>
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "nickname": "测试用户",
    "avatarUrl": null,
    "stats": {
      "favoriteCount": 50,
      "categoryCount": 5
    },
    "createdAt": "2026-02-17T12:00:00Z"
  }
}
```

---

#### 5.2 更新用户信息
```http
PUT /api/v1/users/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "nickname": "新昵称",
  "avatarUrl": "https://..."
}
```

**响应**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "nickname": "新昵称",
    "avatarUrl": "https://..."
  }
}
```

---

### 6. 爬虫管理模块 (Crawler) - 仅管理员

#### 6.1 手动触发爬虫
```http
POST /api/v1/admin/crawler/trigger
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "platform": "ciweimao",
  "tags": ["玄幻", "修仙"]
}
```

**响应**：
```json
{
  "code": 200,
  "message": "爬虫任务已启动",
  "data": {
    "taskId": 1,
    "platform": "ciweimao",
    "status": "RUNNING",
    "startTime": "2026-02-17T12:00:00Z"
  }
}
```

---

#### 6.2 查询爬虫任务状态
```http
GET /api/v1/admin/crawler/tasks?page=1&size=20&status=SUCCESS
Authorization: Bearer <admin_token>
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "platform": "ciweimao",
        "taskName": "定时抓取",
        "status": "SUCCESS",
        "startTime": "2026-02-17T12:00:00Z",
        "endTime": "2026-02-17T12:30:00Z",
        "totalCount": 500,
        "successCount": 480,
        "failedCount": 20
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 100
    }
  }
}
```

---

#### 6.3 配置爬虫参数
```http
PUT /api/v1/admin/crawler/config
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "interval": 2,
  "enabled": true,
  "tags": ["玄幻", "修仙", "都市"]
}
```

**响应**：
```json
{
  "code": 200,
  "message": "配置成功",
  "data": {
    "interval": 2,
    "enabled": true,
    "tags": ["玄幻", "修仙", "都市"]
  }
}
```

---

## 📊 统计模块 (Stats)

### 7.1 获取平台统计
```http
GET /api/v1/stats/platform
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalNovels": 10000,
    "platformStats": [
      {
        "platform": "ciweimao",
        "count": 3000
      },
      {
        "platform": "sf",
        "count": 2500
      }
    ],
    "tagStats": [
      {
        "tag": "玄幻",
        "count": 5000
      }
    ]
  }
}
```

---

## 🎯 技术实现

### Java Spring Boot Controller 示例

```java
@RestController
@RequestMapping("/api/v1/novels")
@Tag(name = "小说管理", description = "小说相关接口")
public class NovelController {

    @Autowired
    private NovelService novelService;

    @GetMapping
    @Operation(summary = "获取小说列表")
    public ApiResponse<PageResult<NovelDTO>> listNovels(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String platform,
        @RequestParam(required = false) String tag,
        @RequestParam(required = false) String keyword
    ) {
        PageResult<NovelDTO> result = novelService.listNovels(page, size, platform, tag, keyword);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取小说详情")
    public ApiResponse<NovelDetailDTO> getNovelDetail(
        @PathVariable Long id,
        @AuthenticationPrincipal User user
    ) {
        Long userId = user != null ? user.getId() : null;
        NovelDetailDTO detail = novelService.getNovelDetail(id, userId);
        return ApiResponse.success(detail);
    }
}
```

---

## 📝 下一步

后端API设计已完成，下一步将进行：
1. 前端页面设计
2. 系统架构设计
