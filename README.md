# 非遗传承信息管理系统

## 项目简介

非遗传承信息管理系统是一个集非物质文化遗产展示、传承人管理、活动预约、文创商城于一体的综合性平台。系统采用前后端分离架构，提供用户端和管理端双重界面，支持多媒体资源管理和数据可视化展示。

## 技术栈

**后端**
- Spring Boot 3.x
- Spring Security + JWT
- MySQL 8.0
- JPA/Hibernate
- FFmpeg（视频转码）

**前端**
- Vue 3 + TypeScript
- Vite
- Element Plus
- ECharts

## 功能模块

### 用户端
- 首页展示（轮播图、统计数据、精选内容）
- 非遗项目浏览与搜索
- 项目地图可视化
- 传承人信息展示
- 活动预约
- 文创商城（商品浏览、购物车、订单管理）
- 资讯公告
- 视频展示（HLS流媒体播放）
- 留言板（发帖、点赞、评论）
- 知识图谱（项目关系可视化）
- 个人中心（订单、预约、地址管理）

### 管理端
- 数据统计仪表盘
- 非遗项目管理
- 传承人管理
- 活动管理与预约审核
- 商城管理（商品、分类、订单）
- 内容管理（资讯、留言、视频）
- 用户管理
- 轮播图管理
- 系统设置

详细功能清单请查看：[系统功能清单.md](./系统功能清单.md)

## 环境要求

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 必需 |
| Node.js | 20+ | 必需 |
| MySQL | 8.0+ | 必需 |
| Maven | 3.6+ | 必需 |
| FFmpeg | 最新版 | 可选，用于视频转码 |

## 快速开始

### 1. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE heritage CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入初始数据
USE heritage;
SOURCE backend/src/main/resources/data/init-data.sql;

# 退出
EXIT;
```

初始数据包含：50个非遗项目、20个传承人、15个活动、25个商品等测试数据。

### 2. 配置数据库连接

如果你的MySQL用户名不是 `root` 或密码不是 `root`，需要修改配置文件。

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/heritage?useSSL=false&serverTimezone=Asia/Shanghai
    username: root        # 修改为你的用户名
    password: root        # 修改为你的密码
```

### 3. 启动后端

```bash
cd backend

# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

后端启动成功后会在 `http://localhost:8080` 运行。

### 4. 启动前端

打开新的命令行窗口：

```bash
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

前端启动成功后会在 `http://localhost:5173` 运行。

### 5. 访问系统

打开浏览器访问：`http://localhost:5173`

**默认账号**
- 管理员：`admin` / `admin123`
- 普通用户：`user` / `user123`

## Docker 部署

如果已安装 Docker 和 Docker Compose：

```bash
# 启动所有服务
docker compose up -d

# 查看日志
docker compose logs -f

# 停止服务
docker compose down
```

访问地址：
- 前端：http://localhost:5173
- 后端：http://localhost:8080

## 项目结构

```
heritage-system/
├── backend/                          # 后端服务
│   ├── src/main/
│   │   ├── java/com/heritage/
│   │   │   ├── config/              # 配置类
│   │   │   ├── controller/          # 控制器
│   │   │   ├── domain/              # 实体类
│   │   │   ├── repo/                # 数据访问层
│   │   │   ├── service/             # 业务逻辑层
│   │   │   └── HtApplication.java   # 启动类
│   │   └── resources/
│   │       ├── application.yml      # 配置文件
│   │       └── data/                # SQL脚本
│   └── pom.xml
├── frontend/                         # 前端应用
│   ├── src/
│   │   ├── api/                     # API接口
│   │   ├── components/              # 组件
│   │   ├── pages/                   # 页面
│   │   │   └── admin/               # 管理后台
│   │   ├── router/                  # 路由
│   │   ├── store/                   # 状态管理
│   │   └── main.ts
│   ├── package.json
│   └── vite.config.ts
├── uploads/                          # 上传文件目录
├── docker-compose.yml
└── README.md
```

## 配置说明

### 后端配置

文件位置：`backend/src/main/resources/application.yml`

```yaml
server:
  port: 8080                          # 后端端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/heritage
    username: root
    password: root

app:
  jwt:
    secret: your-secret-key           # JWT密钥
    expiration: 86400000              # Token过期时间（毫秒）
  uploadDir: uploads                  # 文件上传目录
  ffmpeg:
    path: ffmpeg                      # FFmpeg路径
```

### 前端配置

文件位置：`frontend/vite.config.ts`

```typescript
export default defineConfig({
  server: {
    port: 5173,                       // 前端端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 后端地址
        changeOrigin: true
      }
    }
  }
})
```

## 常见问题

### 1. 数据库连接失败

**错误**：`Communications link failure`

**解决**：
- 检查MySQL服务是否启动
- 确认数据库名称为 `heritage`
- 检查用户名密码是否正确
- 确认端口3306未被占用

### 2. 端口被占用

**错误**：`Port 8080 is already in use`

**解决**：

Windows：
```cmd
netstat -ano | findstr :8080
taskkill /PID <进程号> /F
```

Linux/Mac：
```bash
lsof -i :8080
kill -9 <进程号>
```

### 3. Maven下载依赖慢

**解决**：配置阿里云镜像

创建或编辑 `~/.m2/settings.xml`：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
```

### 4. npm安装依赖慢

**解决**：使用淘宝镜像

```bash
npm config set registry https://registry.npmmirror.com
npm install
```

### 5. JDK版本错误

**错误**：`Unsupported class file major version`

**解决**：
- 检查版本：`java -version`
- 确保使用JDK 17或更高版本

### 6. 视频上传后无法播放

**解决**：
- 安装FFmpeg：`ffmpeg -version`
- 确保FFmpeg在系统PATH中
- 检查后端日志中的转码错误

### 7. 图片上传失败

**解决**：
- 检查 `uploads/` 目录是否存在
- 确认目录有写权限
- 检查文件大小（默认限制5MB）
- 确认文件格式（支持JPG、PNG、GIF、WEBP）

## 生产环境部署

### 前端构建

```bash
cd frontend
npm run build
```

构建产物在 `dist/` 目录，可部署到Nginx等Web服务器。

### 后端打包

```bash
cd backend
mvn clean package -DskipTests
```

生成的JAR包在 `target/` 目录。

### 运行

```bash
# 直接运行
java -jar backend/target/heritage-backend.jar

# 后台运行
nohup java -jar backend/target/heritage-backend.jar > app.log 2>&1 &
```

### Nginx配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    # 上传文件
    location /files {
        alias /path/to/uploads;
    }
}
```

## 注意事项

1. 首次启动前必须先初始化数据库
2. 确保MySQL、后端、前端的端口不冲突
3. 生产环境请修改JWT密钥和管理员密码
4. 定期备份数据库
5. 视频转码功能需要安装FFmpeg

## 许可证

本项目仅供学习和研究使用。
