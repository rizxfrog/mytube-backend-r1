# MyTube 后端 API 文档（完整版）

## 通用约定
- 鉴权
  - Header `Authorization: Bearer <token>`；登录与注册接口开放
  - 角色：`USER` / `ADMIN`；管理员专用接口额外说明
- 响应包装
  - 统一结构：`{"code":0,"message":"ok","data":...}`；错误返回 `code!=0` 与明确 `message`
  - 常见错误码：`1001 未登录/登录过期`、`1002 权限不足`、`2001 参数错误`、`3001 资源不存在`、`5000 服务内部错误`
- 分页约定
  - 通用参数：`page`（从1开始）、`quantity`（默认10，最大50）、或 `offset`（从0开始）+`quantity`
  - 列表返回包含 `more` 或总数 `count` 字段
- 数据格式
  - 时间：`yyyy-MM-dd HH:mm`（时区 `Asia/Shanghai`）；ID 统一为数值型
  - 颜色/弹幕等使用十六进制或明确字符串约定

## 网关 GraphQL
- 查询
  - `randomVisitorVideos(count: Int): [Int!]!` 开放
    - 示例：
      - Query: `query { randomVisitorVideos(count: 11) }`
      - 响应：`[1,2,3]`
  - `videoGetOne(vid: Int!): Int` 开放
  - `videoCumulativeVisitor(vids: [Int!]!): [Int!]!` 开放
- 变更
  - `userRegister(username: String!, password: String!, confirmedPassword: String!): String` 开放
    - 示例：
      - Mutation: `mutation { userRegister(username:"u", password:"p", confirmedPassword:"p") }`
  - `userLogin(username: String!, password: String!): String` 开放
    - 示例：
      - Mutation: `mutation { userLogin(username:"u", password:"p") }`

## 用户服务 REST
- `POST /user/account/register` 开放
  - 参数：`username,password,confirmedPassword`
  - 返回：`{"code":0,"message":"ok","data":"registered"}`
- `POST /user/account/login` 开放
  - 参数：`username,password`
  - 返回：`{"code":0,"message":"ok","data":"token"}`
- `GET /user/account/logout` 需认证
  - 返回：`{"code":0,"message":"ok"}`
- `GET /user/personal/info` 需认证
  - 返回：用户信息对象（后续补充字段）
- `POST /user/info/update` 需认证
  - 参数：`nickname,description?,gender?`
  - 返回：`{"code":0,"message":"ok","data":"updated"}`
- `POST /user/password/update` 需认证
  - 参数：`pw,npw`
  - 返回：`{"code":0,"message":"ok","data":"updated"}`
- `GET /user/info/get-one` 开放
  - 参数：`uid`
  - 返回：用户信息对象

## 视频服务 REST
- `GET /video/random/visitor` 开放
  - 参数：`count?`
  - 返回：`{"code":0,"message":"ok","data":[...]}`
- `GET /video/getone` 开放
  - 参数：`vid`
  - 返回：`{"code":0,"message":"ok","data":vid}`
- `GET /video/cumulative/visitor` 开放
  - 参数：`vids`（可重复）
  - 返回：`{"code":0,"message":"ok","data":[...]}`
- 预期对齐（参考 teriteri-backend 将在后续版本补齐）
  - `POST /video/change/status`（管理员）
  - `POST /video/update-meta`（作者或管理员）
  - `GET /video/user-works-count|user-works|user-love|user-play|user-collect`

## 上传服务 REST
- `GET /video/ask-chunk` 需认证
  - 参数：`hash`
  - 返回：`{"code":0,"message":"ok","data":0}`
- `POST /video/upload-chunk` 需认证
  - 参数：`hash,index`
  - 返回：预签名上传 URL 字符串
- `GET /video/cancel-upload` 需认证
  - 参数：`hash`
- `POST /video/add` 需认证
  - 参数：`hash,title,type,auth,duration,mcid,scid,tags,descr`
  - 返回：`{"code":0,"message":"ok","data":"submitted"}`
  - 说明：当前版本返回占位; 完整版本将进行分片合并、入库、索引与封面处理

## 统计服务 REST
- `POST /video/play/visitor` 开放
  - 参数：`vid`
  - 返回：`{"code":0,"message":"ok","data":"updated"}`
- `POST /video/love-or-not` 需认证
  - 参数：`vid,isLike,isSet`
  - 返回：`{"code":0,"message":"ok","data":"updated"}`

## 评论服务 REST（计划）
- `GET /comment/get` 开放（`vid,offset,type`）
- `GET /comment/reply/get-more` 开放（`id`）
- `POST /comment/add` 需认证
- `POST /comment/delete` 需认证（本人/管理员）
- `GET /comment/get-like-and-dislike` 需认证
- `POST /comment/love-or-not` 需认证
- `GET /comment/get-up-like` 开放（`uid`）

## 收藏服务 REST（计划）
- `GET /favorite/get-all/user` 需认证（`uid`）
- `GET /favorite/get-all/visitor` 开放（`uid`）
- `POST /favorite/create` 需认证（`title,desc,visible`）
- `GET /video/collected-fids` 需认证（`vid`）
- `POST /video/collect` 需认证（`vid,adds[],removes[]`）
- `POST /video/cancel-collect` 需认证（`vid,fid`）

## 搜索服务 REST（计划）
- `GET /search/hot/get` 开放
- `POST /search/word/add` 需认证（管理员）
- `GET /search/word/get` 开放（`keyword`）
- `GET /search/count` 开放
- `GET /search/video/only-pass` 开放（`keyword,page,quantity`）
- `GET /search/user` 开放（`keyword,page,quantity`）

## 弹幕服务 HTTP + WebSocket（计划）
- HTTP
  - `GET /danmu-list/{vid}` 开放
  - `POST /danmu/delete` 需认证（本人/管理员）
- WebSocket `/ws/danmu/{vid}`
  - 客户端必须在消息体携带 `token` 与弹幕数据 `content,fontsize,mode,color,timePoint`
  - 服务端校验后广播同房间，并持久化记录与统计计数

## IM 服务 WebSocket（计划）
- 握手路径：`/im`
- 首条消息需携带 `Bearer`；服务端校验并绑定用户
- 消息类型：发送、撤回、历史拉取；维护未读数与在线列表

## 错误示例
- 未认证访问：`{"code":1001,"message":"unauthorized"}`
- 参数错误：`{"code":2001,"message":"invalid parameter: vid"}`
## 备注
- 本文档覆盖当前已实现的 REST/GraphQL 与后续计划接口，完整实现将严格对齐 `teriteri-backend` 的业务与安全策略，并补充字段、示例与错误码细节。
