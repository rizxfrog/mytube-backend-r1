# MyTube 后端 API 文档

## 概述
- 项目根目录：`f:/MyDepository/teriteri/teriteri/mytube-backend`
- 架构：Spring Boot 3 + Dubbo 3 + MyBatis‑Plus + Nacos + PostgreSQL + GraphQL + MinIO + Elasticsearch 8.19 + Redis（Lettuce）
- 模块：网关 GraphQL + 多域微服务（用户/视频/评论/收藏/弹幕/IM/搜索/上传/统计）
- 端口约定（默认，本地开发）：
  - `mytube-gateway-graphql`：`8080`
  - `mytube-user-service`：`8101`
  - `mytube-video-service`：`8102`
  - `mytube-comment-service`：`8103`
  - `mytube-favorite-service`：`8104`
  - `mytube-danmu-service`：`8105`
  - `mytube-im-service`：`8106`（Netty WebSocket 监听 `7071`）
  - `mytube-search-service`：`8107`
  - `mytube-upload-service`：`8108`
  - `mytube-stats-service`：`8109`

## 认证
- 认证方式：JWT（HS256）
- 请求头：`Authorization: Bearer <token>`
- 当前网关规则：`/graphql` 允许匿名访问；带 Token 时将解析并写入安全上下文，后续查询与鉴权将逐步收紧到业务维度

## GraphQL 网关
- 地址：`POST http://<host>:8080/graphql`
- Content-Type：`application/json`
- 入参：`{"query": "<GraphQL Query>"}` 或携带 `variables`
- 当前已提供的 Query：
  - `randomVisitorVideos(count: Int): [Int!]!`
    - 说明：为游客返回随机视频 `vid` 列表；目前为占位实现，返回空列表，待接入视频域真实逻辑（来源于旧版 `VideoController.randomVideosForVisitor`）
    - 示例：
      ```json
      {"query":"{ randomVisitorVideos(count: 11) }"}
      ```
- 后续规划的 GraphQL（将按旧版 REST 迁移）：
  - `video(id: ID!): Video`
  - `searchVideos(keyword: String!, page: Int!, size: Int!, onlyPass: Boolean = true): SearchPage!`
  - `login(username: String!, password: String!): AuthPayload`
  - `register(input: RegisterInput!): AuthPayload`
  - `changeVideoStatus(vid: ID!, status: Int!): Boolean`

## Dubbo 服务契约
- 注册中心：Nacos，地址 `nacos://127.0.0.1:8848`
- 协议：`dubbo`，端口按服务自动分配（`port: -1`）
- 接口示例：
  - `com.mytube.api.video.VideoServiceApi`
    - 方法：`List<Integer> randomVisitorVideos(int count)`
    - 说明：返回随机推荐的视频 `vid` 列表；由 `mytube-video-service` 提供实现（当前为占位）

## WebSocket 接口
- 弹幕服务（Danmu）
  - 地址：`ws://<host>:8105/ws/danmu/{vid}`
  - 连接说明：按 `vid` 分房间；当前实现将客户端发来的文本消息广播到同房间所有会话（后续将补充 JWT 校验与消息格式约束）
  - 消息体：自由文本（建议后续统一为 JSON，包含 `token` 与弹幕结构）
- IM 服务（Netty）
  - HTTP 升级：`ws://<host>:8106/im`（Netty 服务内部监听 `7071`）
  - 管线：`HttpServerCodec + ChunkedWriteHandler + HttpObjectAggregator + WebSocketServerProtocolHandler("/im")`
  - 说明：当前为握手与连接占位，后续将添加登录态校验与消息路由处理器

## 上传服务（MinIO）
- 说明：提供生成预签名上传 URL 的服务方法（当前未开放 HTTP Controller）
- 客户端配置：`minio.endpoint`、`minio.accessKey`、`minio.secretKey`
- 预签名示例（服务内方法）：
  - `presignPut(bucket, object, ttlMinutes)` → `String`（预签名 PUT URL）

## 搜索服务（Elasticsearch 8.19）
- 客户端：`co.elastic.clients:elasticsearch-java:8.19.0`
- 配置：`elasticsearch.host`、`elasticsearch.port`（默认 `localhost:9200`）
- 说明：当前仅配置客户端，索引与查询 API 将按旧版 ES 工具类迁移（视频、用户、搜索词）

## 数据库结构（PostgreSQL）
- 数据库：`mytube`
- 表：
  - `users`
    - `id SERIAL PRIMARY KEY`
    - `nickname VARCHAR(255) NOT NULL`
  - `videos`
    - `id SERIAL PRIMARY KEY`
    - `title VARCHAR(255) NOT NULL`
    - `status INTEGER NOT NULL DEFAULT 1`
  - `comments`
    - `id SERIAL PRIMARY KEY`
    - `video_id INTEGER NOT NULL`
    - `user_id INTEGER NOT NULL`
    - `content TEXT NOT NULL`
  - `favorites`
    - `id SERIAL PRIMARY KEY`
    - `user_id INTEGER NOT NULL`
    - `name VARCHAR(255) NOT NULL`
  - `stats`
    - `id SERIAL PRIMARY KEY`
    - `video_id INTEGER NOT NULL`
    - `play INTEGER NOT NULL DEFAULT 0`
    - `like INTEGER NOT NULL DEFAULT 0`
    - `danmu INTEGER NOT NULL DEFAULT 0`
- 迁移：各服务内 `src/main/resources/db/migration/V1__init.sql` 已提供初始建表

## 环境与配置
- Nacos：`spring.cloud.nacos.config.server-addr=127.0.0.1:8848`
- Dubbo：
  - `dubbo.application.name=<module-name>`
  - `dubbo.registry.address=nacos://127.0.0.1:8848`
  - `dubbo.protocol.name=dubbo`
  - `dubbo.protocol.port=-1`
- 数据源（示例）：
  - `spring.datasource.url=jdbc:postgresql://localhost:5432/mytube`
  - `spring.datasource.username=postgres`
  - `spring.datasource.password=postgres`
  - `spring.datasource.driver-class-name=org.postgresql.Driver`
- Redis（示例）：
  - `spring.redis.host=localhost`
  - `spring.redis.port=6379`
- MinIO（示例）：
  - `minio.endpoint=http://localhost:9000`
  - `minio.accessKey=minioadmin`
  - `minio.secretKey=minioadmin`
- Elasticsearch（示例）：
  - `elasticsearch.host=localhost`
  - `elasticsearch.port=9200`

## 启动步骤（本地开发）
- 启动依赖：Nacos、PostgreSQL（库 `mytube`）、Redis、MinIO、Elasticsearch
- 启动服务：
  - 网关：`gradlew :mytube-gateway-graphql:bootRun`
  - 其他服务：`gradlew :<module>:bootRun`（如 `:mytube-video-service:bootRun`）
- 测试 GraphQL：
  - 请求：`POST /graphql`
  - Body：`{"query":"{ randomVisitorVideos(count: 11) }"}`

## 版本说明
- Spring Boot：`3.3.x`
- Dubbo：`3.2.x`
- MyBatis‑Plus：`3.5.x`
- PostgreSQL 驱动：`42.7.x`
- Elasticsearch Java Client：`8.19.0`
- MinIO：`8.x`

## 变更与迁移计划
- 逐步将旧版 `teriteri-backend` 的 REST 控制器映射为 GraphQL Query/Mutation
- 将旧版 Redis 的集合/有序集合缓存策略迁移到各服务
- 为弹幕与 IM 增加 JWT 校验与消息格式约束
- 在搜索服务补齐视频与用户索引的查询/索引 API
