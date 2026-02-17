# Novel Reader - 读书网站

## 📌 项目概述

一个定时抓取多个小说平台最新内容的读书网站，支持标签过滤、内容展示和用户收藏管理功能。

**核心优先级**：爬虫功能是核心，必须首先实现！

---

## 📚 设计文档

详细的设计文档请查看：[docs/novel-reader-design/](docs/novel-reader-design/)

### 文档列表

1. **[需求分析](docs/novel-reader-design/01-需求分析.md)**
   - 功能需求
   - 技术选型
   - 性能要求
   - 安全要求

2. **[数据库设计（MySQL版）](docs/novel-reader-design/02-数据库设计(MySQL版).md)** ⭐
   - 10张表结构
   - MySQL全文搜索支持
   - 索引策略

3. **[后端API设计](docs/novel-reader-design/03-后端API设计.md)**
   - 30+ RESTful接口
   - JWT认证
   - 统一响应格式

4. **[前端页面设计](docs/novel-reader-design/04-前端页面设计.md)**
   - 8个页面设计
   - Vue 3 + Uni-app
   - 多端适配

5. **[系统架构设计](docs/novel-reader-design/05-系统架构设计.md)**
   - 整体架构
   - 模块划分
   - 安全设计
   - 部署架构

6. **[爬虫功能设计](docs/novel-reader-design/06-爬虫功能设计.md)** ⭐ 核心优先
   - 支持4个平台：刺猬猫、SF轻小说、次元姬、起点
   - 爬虫架构设计
   - 反爬虫策略

7. **[爬虫后台管理界面设计](docs/novel-reader-design/07-爬虫后台管理界面设计.md)** ⭐ 核心优先
   - 7个管理页面
   - Vue 3 + Element Plus
   - 完整代码示例

---

## 🎯 技术栈

### 后端
- **框架**：Spring Boot 3.2.x
- **数据库**：MySQL 8.0
- **缓存**：Redis 7
- **爬虫**：Jsoup + OkHttp
- **API文档**：SpringDoc OpenAPI 3
- **构建工具**：Maven

### 前端
- **框架**：Vue 3 + TypeScript
- **跨平台**：Uni-app（H5 + 小程序 + APP）
- **状态管理**：Pinia
- **UI组件库**：Element Plus / uView
- **HTTP客户端**：Axios
- **构建工具**：Vite

---

## 🚀 开发规范

详见：[skills/novel-reader/SKILL.md](skills/novel-reader/SKILL.md)

### 核心要求

1. ✅ **架构设计先行**：已完成所有设计文档
2. ✅ **严格使用Git版本控制**：每个功能点都要提交
3. ✅ **文档和测试**：每个功能都要输出设计文档和测试
4. ✅ **代码质量**：遵循编程规范、详细注释

---

## 📋 开发优先级

### P0（最高优先级）- 爬虫核心功能
1. ⭐ **爬虫功能**（首先实现）
   - 刺猬猫爬虫
   - SF轻小说爬虫
   - 次元姬爬虫
   - 起点爬虫
   - AI概括服务

2. ⭐ **爬虫后台管理界面**
   - 任务管理
   - 平台配置
   - 数据浏览

### P1（次要优先级）
3. 数据库搭建（MySQL）
4. 后端基础框架
5. 用户系统（注册/登录）
6. 前端基础页面
7. 小说展示功能
8. 收藏管理功能

---

## 🌐 支持平台

| 平台 | 官网 | 状态 |
|------|------|------|
| 刺猬猫 | https://mip.ciweimao.com/ | 待开发 |
| SF轻小说 | https://book.sfacg.com/ | 待开发 |
| 次元姬 | https://www.ciyuanji.com/ | 待开发 |
| 起点 | https://www.qidian.com/ | 待开发 |

---

## 📊 服务器资源

**阿里云服务器**：47.122.121.58

- CPU：2核 Intel Xeon Platinum
- 内存：3.5GB（可用3.0GB）
- 磁盘：49GB（可用41GB）

**部署资源需求**：
- Spring Boot后端：~500MB
- MySQL数据库：~300MB
- Redis缓存：~100MB
- Nginx：~50MB
- **总计：~950MB**（完全满足）

---

## 📝 开发进度

- [x] 需求分析
- [x] 数据库设计（MySQL版）
- [x] 后端API设计
- [x] 前端页面设计
- [x] 系统架构设计
- [x] 爬虫功能设计
- [x] 爬虫后台管理界面设计
- [ ] 环境搭建（Docker、MySQL、Redis）
- [ ] 爬虫功能开发（P0）
- [ ] 爬虫后台管理界面开发（P0）
- [ ] 其他功能开发（P1）

---

## 🔗 相关链接

- **GitHub仓库**：https://github.com/lridea/novel-reader
- **设计文档**：[docs/novel-reader-design/](docs/novel-reader-design/)
- **开发规范**：[skills/novel-reader/SKILL.md](skills/novel-reader/SKILL.md)

---

## 📄 许可证

MIT License

---

**请在评审设计文档后，告知是否可以开始开发爬虫功能！** 🦞
