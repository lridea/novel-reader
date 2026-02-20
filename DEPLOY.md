# Novel Reader 部署文档

## 目录
1. [服务器环境要求](#服务器环境要求)
2. [环境安装](#环境安装)
3. [数据库配置](#数据库配置)
4. [后端部署](#后端部署)
5. [前端部署](#前端部署)
6. [Nginx配置](#nginx配置)
7. [进程管理](#进程管理)
8. [常见问题](#常见问题)

---

## 服务器环境要求

- 操作系统：Ubuntu 20.04+ / CentOS 7+ / Debian 10+
- 内存：至少 2GB
- 磁盘：至少 10GB
- 已安装：MySQL 8.0+

---

## 环境安装

### 1. 安装 JDK 21

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk -y

# CentOS/RHEL
sudo yum install java-21-openjdk java-21-openjdk-devel -y

# 验证安装
java -version
```

### 2. 安装 Redis

```bash
# Ubuntu/Debian
sudo apt install redis-server -y
sudo systemctl start redis-server
sudo systemctl enable redis-server

# CentOS/RHEL
sudo yum install redis -y
sudo systemctl start redis
sudo systemctl enable redis

# 验证安装
redis-cli ping
# 应返回 PONG
```

### 3. 安装 Nginx

```bash
# Ubuntu/Debian
sudo apt install nginx -y
sudo systemctl start nginx
sudo systemctl enable nginx

# CentOS/RHEL
sudo yum install nginx -y
sudo systemctl start nginx
sudo systemctl enable nginx

# 验证安装
nginx -v
```

---

## 数据库配置

### 1. 创建数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE novel_reader CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建用户（可选，建议）
CREATE USER 'novel_reader'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON novel_reader.* TO 'novel_reader'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2. 数据库配置说明

后端使用 JPA 的 `ddl-auto: update`，首次启动时会自动创建表结构。

---

## 后端部署

### 1. 上传后端 JAR 包

将打包好的 `novel-reader-backend-1.0.0.jar` 上传到服务器：

```bash
# 创建应用目录
sudo mkdir -p /opt/novel-reader
sudo chown $USER:$USER /opt/novel-reader

# 上传文件（在本地执行）
scp novel-reader-backend-1.0.0.jar user@your-server:/opt/novel-reader/
```

### 2. 创建配置文件

```bash
# 创建配置目录
mkdir -p /opt/novel-reader/config

# 生成 JWT Secret（选择其中一种方式）
# 方式1: 使用 OpenSSL
openssl rand -base64 64

# 方式2: 使用 Python
python3 -c "import secrets; print(secrets.token_urlsafe(64))"

# 创建生产环境配置（将下面的 your-jwt-secret 替换为上面生成的值）
cat > /opt/novel-reader/config/application-prod.yml << 'EOF'
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/novel_reader?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: novel_reader
    password: your_mysql_password
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

  redis:
    host: localhost
    port: 6379

logging:
  level:
    root: INFO
    com.novelreader: INFO

jwt:
  secret: your-jwt-secret-replace-with-generated-value
  expiration: 604800000

ai:
  zhipu:
    api-key: your-zhipu-api-key
EOF
```

### 3. 创建 systemd 服务

```bash
sudo cat > /etc/systemd/system/novel-reader-backend.service << 'EOF'
[Unit]
Description=Novel Reader Backend Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/novel-reader
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar novel-reader-backend-1.0.0.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=novel-reader-backend

[Install]
WantedBy=multi-user.target
EOF

# 重载 systemd
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start novel-reader-backend

# 设置开机自启
sudo systemctl enable novel-reader-backend

# 查看状态
sudo systemctl status novel-reader-backend
```

---

## 前端部署

### 1. 上传前端静态文件

将打包好的 `dist` 目录上传到服务器：

```bash
# 创建前端目录
sudo mkdir -p /var/www/novel-reader

# 上传文件（在本地执行）
scp -r dist/* user@your-server:/var/www/novel-reader/
```

### 2. 设置权限

```bash
# Ubuntu/Debian
sudo chown -R www-data:www-data /var/www/novel-reader

# CentOS/RHEL
sudo chown -R nginx:nginx /var/www/novel-reader

sudo chmod -R 755 /var/www/novel-reader
```

---

## Nginx配置

### 1. 创建 Nginx 配置文件

```bash
sudo cat > /etc/nginx/sites-available/novel-reader << 'EOF'
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或服务器IP

    # 前端静态文件
    root /var/www/novel-reader;
    index index.html;

    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied any;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/json application/xml;
    gzip_comp_level 6;

    # 前端路由 - Vue Router History 模式
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 上传文件代理（使用 ^~ 提高优先级，避免被静态资源规则覆盖）
    location ^~ /uploads {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}
EOF

# CentOS 使用以下路径
# sudo cat > /etc/nginx/conf.d/novel-reader.conf << 'EOF'
# ... 同上配置 ...
# EOF
```

### 2. 启用配置

```bash
# Ubuntu/Debian
sudo ln -s /etc/nginx/sites-available/novel-reader /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default  # 删除默认配置（可选）

# 测试配置
sudo nginx -t

# 重载 Nginx
sudo systemctl reload nginx
```

### 3. 配置 HTTPS（推荐）

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取 SSL 证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo systemctl enable certbot.timer
```

---

## 进程管理

### 常用命令

```bash
# 后端服务
sudo systemctl start novel-reader-backend    # 启动
sudo systemctl stop novel-reader-backend     # 停止
sudo systemctl restart novel-reader-backend  # 重启
sudo systemctl status novel-reader-backend   # 查看状态
sudo journalctl -u novel-reader-backend -f   # 查看日志

# Nginx
sudo systemctl start nginx     # 启动
sudo systemctl stop nginx      # 停止
sudo systemctl restart nginx   # 重启
sudo systemctl reload nginx    # 重载配置
sudo nginx -t                  # 测试配置

# Redis
sudo systemctl start redis     # 启动
sudo systemctl stop redis      # 停止
sudo systemctl restart redis   # 重启
```

---

## 常见问题

### 1. 后端启动失败

```bash
# 检查日志
sudo journalctl -u novel-reader-backend -n 100

# 常见原因：
# - 数据库连接失败：检查数据库配置和权限
# - Redis 连接失败：检查 Redis 是否启动
# - 端口被占用：检查 8080 端口
sudo netstat -tlnp | grep 8080
```

### 2. 前端页面空白

```bash
# 检查 Nginx 配置
sudo nginx -t

# 检查文件权限
ls -la /var/www/novel-reader/

# 检查 Nginx 错误日志
sudo tail -f /var/log/nginx/error.log
```

### 3. API 请求失败

```bash
# 检查后端是否运行
curl http://127.0.0.1:8080/api/novels/page

# 检查防火墙
sudo ufw status
sudo ufw allow 80
sudo ufw allow 443
```

### 4. 数据库连接问题

```bash
# 测试数据库连接
mysql -u novel_reader -p novel_reader

# 检查 MySQL 是否运行
sudo systemctl status mysql
```

---

## 部署检查清单

- [ ] JDK 21 已安装
- [ ] Redis 已安装并运行
- [ ] MySQL 数据库已创建
- [ ] 后端 JAR 包已上传
- [ ] 后端配置文件已创建
- [ ] 后端服务已启动
- [ ] 前端静态文件已上传
- [ ] Nginx 配置已创建
- [ ] Nginx 已重载
- [ ] 网站可正常访问
- [ ] HTTPS 已配置（可选）

---

## 更新部署

### 更新后端

```bash
# 上传新的 JAR 包
scp novel-reader-backend-1.0.0.jar user@your-server:/opt/novel-reader/

# 重启服务
sudo systemctl restart novel-reader-backend
```

### 更新前端

```bash
# 上传新的静态文件
scp -r dist/* user@your-server:/var/www/novel-reader/

# 清理浏览器缓存后刷新页面即可
```
