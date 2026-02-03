# 非遗传承信息管理系统

## 项目简介
非遗传承信息管理系统是一个集非物质文化遗产展示、传承人管理、活动预约、文创商城于一体的综合性平台。系统采用前后端分离架构，提供用户端和管理端双重界面，支持多媒体资源管理和数据可视化展示。

## 技术栈
- 后端：Spring Boot 3、Spring Security、JWT、MySQL 8、JPA/Hibernate
- 前端：Vue 3、Vite、TypeScript、Element Plus
- 媒体处理：ffmpeg（HLS转码）
- 容器化：Docker、Docker Compose

## 端口配置
- 后端：8080
- 前端：5173（开发环境）/ 80（Docker容器）
- MySQL：3306

## 快速启动（Docker）
1. 前置要求：安装 Docker 和 Docker Compose
2. 构建并启动：
   ```bash
   docker compose build
   docker compose up -d
   ```
3. 访问地址：
   - 前端：http://localhost:5173
   - 后端：http://localhost:8080

注意事项：
- 前端容器使用 Nginx 将 `/api` 请求代理到后端服务
- 后端镜像包含 ffmpeg 用于 HLS 转码
- 上传文件持久化在宿主机 `./uploads` 目录，映射到后端容器的 `/app/uploads`

## 本地开发

### 数据库初始化
1. 创建数据库：
   ```sql
   CREATE DATABASE heritage CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. 数据库配置：
   - MySQL 8 运行在 `localhost:3306`
   - 用户名：`root`，密码：`root`
   - 数据库名：`heritage`
3. 初始化数据（可选）：
   - 执行 `backend/src/main/resources/data/init-data.sql` 导入测试数据
   - 包含50个非遗项目、20个传承人、15个活动等丰富数据

### 后端开发
- 环境要求：JDK 17、Maven、系统PATH中安装ffmpeg
- 运行命令：
  ```bash
  cd backend
  mvn spring-boot:run
  ```
- 配置文件：`backend/src/main/resources/application.yml`
- API文档：启动后访问 http://localhost:8080/swagger-ui.html（如已配置）

### 前端开发
- 环境要求：Node.js 20+
- 运行命令：
  ```bash
  cd frontend
  npm install
  npm run dev
  ```
  访问地址：http://localhost:5173

开发服务器通过 `vite.config.ts` 将 `/api` 请求代理到 `http://localhost:8080`

### 前端构建
```bash
cd frontend
npm run build    # 生产环境构建
npm run preview  # 预览构建结果
```

## 代码检查与格式化（前端）
```bash
npm run lint      # 代码检查
npm run format    # 代码格式化
```

## 媒体与HLS
- 大文件使用分块上传并合并
- 视频文件异步转码为HLS格式，自动生成封面图
- HLS输出文件存储在配置的上传目录中
- 支持的图片格式：JPG、PNG、GIF、WEBP
- 支持的视频格式：MP4、AVI、MKV、MOV

## 配置说明
- Spring配置文件：`backend/src/main/resources/application.yml`
- 重要配置项：
  - `spring.datasource.*` - 数据库连接
  - `app.jwt.*` - JWT令牌配置
  - `app.uploadDir` - 文件上传目录
  - `app.ffmpeg.*` - FFmpeg配置
  - `app.ratelimit.*` - 限流配置

环境变量覆盖（Docker Compose）：
- `SPRING_DATASOURCE_URL` - 数据库URL
- `SPRING_DATASOURCE_USERNAME` - 数据库用户名
- `SPRING_DATASOURCE_PASSWORD` - 数据库密码
- `SERVER_PORT` - 服务端口

## 项目结构
```
.
├── backend/                    # Spring Boot 后端服务
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/heritage/
│   │   │   │   ├── config/    # 配置类（安全、JWT、CORS等）
│   │   │   │   ├── controller/ # REST API控制器
│   │   │   │   ├── domain/    # JPA实体类
│   │   │   │   ├── repo/      # 数据访问层
│   │   │   │   ├── service/   # 业务逻辑层
│   │   │   │   └── aspect/    # AOP切面
│   │   │   └── resources/
│   │   │       ├── application.yml  # 应用配置
│   │   │       └── data/            # 数据初始化脚本
│   │   └── test/              # 测试代码
│   ├── Dockerfile             # 后端Docker镜像
│   └── pom.xml               # Maven依赖配置
├── frontend/                  # Vue 3 前端应用
│   ├── src/
│   │   ├── api/              # API请求封装
│   │   ├── components/       # 可复用组件
│   │   ├── pages/            # 页面组件
│   │   │   └── admin/        # 后台管理页面
│   │   ├── router/           # 路由配置
│   │   ├── store/            # 状态管理
│   │   ├── App.vue           # 根组件
│   │   └── main.ts           # 入口文件
│   ├── public/               # 静态资源
│   ├── Dockerfile            # 前端Docker镜像
│   ├── nginx.conf            # Nginx配置
│   ├── package.json          # NPM依赖
│   └── vite.config.ts        # Vite配置
├── uploads/                   # 上传文件存储目录
├── docker-compose.yml        # Docker编排配置
├── pom.xml                   # 根项目Maven配置
└── README.md                 # 项目文档
```

## 功能特性

### 用户端功能
- **首页展示**：轮播图、统计数据、精选项目、新闻动态
- **非遗项目**：项目列表、详情展示、分类筛选、搜索功能
- **传承人**：传承人列表、个人简介、技艺展示
- **活动预约**：活动列表、在线预约、预约管理
- **文创商城**：商品展示、购物车、订单管理
- **资讯公告**：新闻动态、通知公告、多视图展示
- **视频展示**：视频列表、HLS播放、封面预览
- **知识图谱**：非遗项目关系可视化、交互式探索
- **用户中心**：个人信息、订单管理、预约记录、地址管理

### 管理端功能
- **仪表盘**：数据统计、图表展示、快速入口
- **项目管理**：非遗项目CRUD、分类管理、图片上传
- **传承人管理**：传承人信息维护、头像上传
- **活动管理**：活动发布、状态管理、预约审核
- **商城管理**：商品管理、分类管理、订单处理
- **内容管理**：资讯发布、留言板管理、媒体上传
- **用户管理**：用户列表、角色权限、状态管理
- **系统设置**：站点配置、轮播图管理、页面横幅

## 默认账号

### 管理员账号
- 用户名：`admin`
- 密码：`admin123`
- 权限：完整的后台管理权限

### 普通用户账号
- 用户名：`user`
- 密码：`user123`
- 权限：前台浏览、预约、购物等功能

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 确认数据库名称、用户名、密码是否正确
- 检查端口3306是否被占用

### 2. 前端无法访问后端API
- 确认后端服务已启动（端口8080）
- 检查CORS配置是否正确
- 查看浏览器控制台的网络请求错误信息

### 3. 视频上传后无法播放
- 确认ffmpeg已正确安装并在PATH中
- 检查上传目录权限
- 查看后端日志中的转码错误信息

### 4. Docker容器启动失败
- 检查Docker和Docker Compose版本
- 确认端口未被占用
- 查看容器日志：`docker compose logs`

### 5. 图片上传失败
- 检查上传目录是否存在且有写权限
- 确认文件大小未超过限制（默认5MB）
- 检查文件格式是否支持

## 性能优化建议
- 启用MySQL查询缓存
- 配置Redis缓存（可选）
- 使用CDN加速静态资源
- 启用Nginx gzip压缩
- 数据库索引优化
- 图片压缩和懒加载

## 安全建议
- 修改默认管理员密码
- 配置HTTPS证书
- 定期更新依赖包
- 启用SQL注入防护
- 配置防火墙规则
- 定期备份数据库

## 后续改进
- 添加自动化测试（后端单元/集成测试；前端单元/E2E测试）
- 实现Redis缓存层
- 添加全文搜索（Elasticsearch）
- 实现消息通知系统
- 添加数据导出功能
- 实现多语言支持
- 可选：多环境配置（.env、Spring profiles）和CI/CD流程

## 许可证
本项目仅供学习和研究使用。

## 联系方式
如有问题或建议，请提交Issue或Pull Request。


