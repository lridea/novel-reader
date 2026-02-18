# JWT配置教程

**配置日期**: 2026-02-18
**配置人员**: OpenClaw
**版本**: v0.7.0

---

## 📋 概述

本项目使用JWT（JSON Web Token）进行用户认证，JWT配置在`application.yml`文件中。

---

## 🔧 配置文件位置

```
novel-reader/novel-reader-backend/src/main/resources/application.yml
```

---

## 📝 JWT配置参数

### application.yml中的JWT配置

```yaml
# JWT配置
jwt:
  secret: ${JWT_SECRET:novel-reader-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm-please-change-this-in-production}
  expiration: 604800000  # 7天（毫秒）
```

---

## 🔑 配置参数说明

### 1. jwt.secret

**说明**: JWT签名密钥，用于生成和验证JWT Token

**类型**: 字符串（String）

**要求**:
- 至少256位（32字节）的随机字符串
- 生产环境必须使用强随机密钥
- 不能泄露给任何人

**默认值**: `novel-reader-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm-please-change-this-in-production`

**推荐生成方式**:

#### 方式一：使用Linux/Unix命令生成

```bash
# 生成256位（32字节）的随机密钥（Base64编码）
openssl rand -base64 32
```

输出示例：
```
aB3xY9zK2vM5nP8qR4sT6wV1xC0yD9zF2nG5kH8jL3mN6pQ9sT2vW5xY8zB1cD4eF7gH0iJ3kL6mN9pQ2rS5tU8vW0
```

#### 方式二：使用在线生成器

访问以下网站生成JWT密钥：
- https://www.random.org/strings/
- https://www.uuidgenerator.net/

设置：
- 字符串长度：32
- 包含字符：大写字母、小写字母、数字、特殊字符

#### 方式三：使用Python脚本生成

```python
import secrets
import base64

# 生成256位（32字节）的随机密钥（Base64编码）
secret = secrets.token_bytes(32)
print(base64.b64encode(secret).decode('utf-8'))
```

---

### 2. jwt.expiration

**说明**: JWT Token过期时间（毫秒）

**类型**: 长整型（Long）

**默认值**: `604800000`（7天）

**常用配置**:

| 过期时间 | 毫秒数 | 说明 |
|---------|--------|------|
| 1小时 | 3600000 | 适用于高安全性场景 |
| 1天 | 86400000 | 适用于普通应用 |
| 7天 | 604800000 | 默认值，适用于Web应用 |
| 30天 | 2592000000 | 适用于移动应用 |

**推荐配置**:
- Web应用：7天
- 移动应用：30天
- 高安全性场景：1小时

---

## 🚀 配置步骤

### 步骤一：生成JWT密钥

```bash
# 生成256位（32字节）的随机密钥（Base64编码）
openssl rand -base64 32
```

输出示例：
```
aB3xY9zK2vM5nP8qR4sT6wV1xC0yD9zF2nG5kH8jL3mN6pQ9sT2vW5xY8zB1cD4eF7gH0iJ3kL6mN9pQ2rS5tU8vW0
```

---

### 步骤二：修改application.yml文件

编辑`application.yml`文件：

```yaml
# JWT配置
jwt:
  secret: aB3xY9zK2vM5nP8qR4sT6wV1xC0yD9zF2nG5kH8jL3mN6pQ9sT2vW5xY8zB1cD4eF7gH0iJ3kL6mN9pQ2rS5tU8vW0
  expiration: 604800000  # 7天（毫秒）
```

---

### 步骤三：使用环境变量（推荐）

为了安全起见，建议使用环境变量来配置JWT密钥：

#### 方式一：在服务器上设置环境变量

```bash
# 临时设置（当前会话有效）
export JWT_SECRET=aB3xY9zK2vM5nP8qR4sT6wV1xC0yD9zF2nG5kH8jL3mN6pQ9sT2vW5xY8zB1cD4eF7gH0iJ3kL6mN9pQ2rS5tU8vW0

# 永久设置（写入~/.bashrc或~/.bash_profile）
echo 'export JWT_SECRET=aB3xY9zK2vM5nP8qR4sT6wV1xC0yD9zF2nG5kH8jL3mN6pQ9sT2vW5xY8zB1cD4eF7gH0iJ3kL6mN9pQ2rS5tU8vW0' >> ~/.bashrc
source ~/.bashrc
```

#### 方式二：在Docker容器中设置环境变量

编辑`docker-compose.yml`文件：

```yaml
version: '3.8'
services:
  novel-reader-backend:
    image: novel-reader-backend:latest
    environment:
      - JWT_SECRET=aB3xY9zK2vM5nP8qR4sT6wV1xC0yD9zF2nG5kH8jL3mN6pQ9sT2vW5xY8zB1cD4eF7gH0iJ3kL6mN9pQ2rS5tU8vW0
    ports:
      - "8080:8080"
```

---

### 步骤四：重启应用

```bash
# 如果使用Maven启动
mvn spring-boot:run

# 如果使用JAR包启动
java -jar novel-reader-backend.jar

# 如果使用Docker启动
docker-compose restart
```

---

## 🔐 安全建议

### 1. 密钥管理

- ✅ 使用强随机密钥（至少256位）
- ✅ 定期更换密钥（建议每3-6个月）
- ✅ 不要将密钥提交到Git仓库
- ✅ 使用环境变量或密钥管理工具
- ❌ 不要使用默认密钥
- ❌ 不要在代码中硬编码密钥

---

### 2. 过期时间

- ✅ 根据应用场景设置合理的过期时间
- ✅ Web应用建议7天
- ✅ 移动应用建议30天
- ✅ 高安全性场景建议1小时
- ❌ 不要设置过长的过期时间（如1年）

---

### 3. Token刷新机制

当前项目没有实现Token刷新机制，建议后续添加：

```java
// TODO: 实现Token刷新机制
// 1. 添加refresh_token字段
// 2. 提供刷新Token的API
// 3. 客户端定期刷新Token
```

---

## 🧪 验证配置

### 验证JWT配置是否生效

#### 1. 登录测试

```bash
# 使用curl测试登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

预期响应：
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJyb2xlIjoiQURNSU4iLCJzdWIiOiIxIiwiaWF0IjoxNzE2MDAwMDAwLCJleHAiOjE3MTY2MDQ4MDB9.xxx",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com"
  }
}
```

---

#### 2. 解码JWT Token

访问以下网站解码JWT Token：
- https://jwt.io/

验证以下内容：
- `userId`: 用户ID
- `username`: 用户名
- `role`: 用户角色
- `exp`: 过期时间
- `iat`: 签发时间

---

#### 3. 检查Token过期时间

```bash
# 使用Python脚本检查Token过期时间
import json
import base64
import datetime

# JWT Token
token = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJyb2xlIjoiQURNSU4iLCJzdWIiOiIxIiwiaWF0IjoxNzE2MDAwMDAwLCJleHAiOjE3MTY2MDQ4MDB9.xxx"

# 解码Payload
payload = token.split('.')[1]
# 补齐Base64
payload += '=' * (4 - len(payload) % 4)
decoded = base64.b64decode(payload)
data = json.loads(decoded)

# 检查过期时间
exp = data['exp']
exp_date = datetime.datetime.fromtimestamp(exp)
print(f"过期时间: {exp_date}")

# 检查是否过期
now = datetime.datetime.now()
if exp_date > now:
    print("Token未过期")
else:
    print("Token已过期")
```

---

## 📊 常见问题

### 问题1：JWT密钥不足256位

**错误信息**:
```
The specified key byte array is 256 bits which is not secure enough for 256-bit HS256 algorithm
```

**解决方法**:
- 确保密钥至少256位（32字节）
- 使用`openssl rand -base64 32`生成密钥

---

### 问题2：Token验证失败

**错误信息**:
```
JWT signature does not match locally computed signature
```

**解决方法**:
- 检查`jwt.secret`配置是否正确
- 确认前后端使用相同的密钥
- 重启应用后重新登录

---

### 问题3：Token过期时间不生效

**问题**: Token没有在配置的时间内过期

**解决方法**:
- 检查`jwt.expiration`配置是否正确（单位是毫秒）
- 重启应用后重新登录
- 确认没有缓存旧的Token

---

## 📚 相关文件

| 文件 | 说明 |
|------|------|
| `application.yml` | JWT配置文件 |
| `JwtUtil.java` | JWT工具类 |
| `JwtAuthenticationFilter.java` | JWT认证过滤器 |
| `SecurityConfig.java` | Spring Security配置 |
| `AuthService.java` | 认证服务 |
| `AuthController.java` | 认证控制器 |

---

## 🔗 相关链接

- JWT官网：https://jwt.io/
- JWT在线解码器：https://jwt.io/
- Spring Security官方文档：https://docs.spring.io/spring-security/reference/
- JJWT库文档：https://github.com/jwtk/jjwt

---

**配置完成时间**: 2026-02-18 21:30
**配置人员**: OpenClaw
**项目版本**: v0.7.0

🦞
