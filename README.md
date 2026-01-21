# MyTube 后端

MyTube 后端的多模块 Gradle 工程，包含公共库、Spring Boot 服务与 GraphQL 网关。

## 模块
- mytube-common
- mytube-api
- mytube-gateway-graphql
- mytube-user-service
- mytube-video-service
- mytube-comment-service
- mytube-favorite-service
- mytube-danmu-service
- mytube-im-service
- mytube-search-service
- mytube-upload-service
- mytube-stats-service

## 环境要求
- Java 23（见根工程 `build.gradle.kts` 的 toolchain）
- 本地 Gradle 安装（本仓库无 wrapper）

## 构建
```bash
gradle build
```

## 运行单个服务
```bash
gradle :mytube-video-service:bootRun
```

## 运行模块测试
```bash
gradle :mytube-video-service:test
```

## 配置
各服务模块配置文件位置：
- `src/main/resources/application.yml`
- `src/main/resources/bootstrap.yml`

数据库迁移文件位置：
- `src/main/resources/db/migration`

常用外部依赖包含 Nacos、PostgreSQL、Redis、Flyway 与 OSS 客户端。请勿提交
密钥，使用环境变量或本地覆盖配置。

## API
REST 与 GraphQL 接口列表见 `API.md`。
