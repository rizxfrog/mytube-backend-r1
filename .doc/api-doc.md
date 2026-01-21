# MyTube API 文档（给前端对接）

说明：
- 所有 HTTP 接口均返回 `CustomResponse`：`{ code: number, message: string, data: any }`，`code=0` 表示成功。
- 需要登录的接口使用 `Authorization: Bearer <JWT>` 请求头。
- 端口来自各服务 `application.yml`，实际部署可能不同。
- 部分接口只提供 Query 参数或 multipart/form-data，不接受 JSON。

## 服务地址（默认）
- user-service: `http://127.0.0.1:8101`
- video-service: `http://127.0.0.1:8102`
- comment-service: `http://127.0.0.1:8103`
- favorite-service: `http://127.0.0.1:8104`
- danmu-service: `http://127.0.0.1:8105`
- im-service: `http://127.0.0.1:8106`
- search-service: `http://127.0.0.1:8107`
- upload-service: `http://127.0.0.1:8108`
- stats-service: `http://127.0.0.1:8109`
- gateway-graphql: `http://127.0.0.1:8080/graphql`

---

## 统一响应
```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

## 鉴权说明
- 登录成功返回 `token`，前端后续请求带 `Authorization: Bearer <token>`。
- `CurrentUser` 从 token 中读取 `sub` 作为 `uid`，并使用 `role` 判断 admin。

---

# User Service（8101）

### 注册
`POST /user/account/register`

- Content-Type: `application/json`
- Body:
```json
{
  "username": "string",
  "password": "string",
  "confirmedPassword": "string"
}
```
- 校验：username/password 非空、长度<=50、password==confirmedPassword
- 响应：`CustomResponse<Void>`

### 用户登录
`POST /user/account/login`

- Body(JSON):
```json
{
  "username": "string",
  "password": "string"
}
```
- 响应 `data`:
```json
{
  "token": "jwt",
  "user": { "uid": 1, "nickname": "...", "avatar_url": "...", ... }
}
```

### 管理员登录
`POST /admin/account/login`

- Body(JSON): 同登录
- role 非 admin 则返回 403

### 用户个人信息（需登录）
`GET /user/personal/info`

### 管理员个人信息（需登录且 admin）
`GET /admin/personal/info`

### 用户退出登录（需登录）
`GET /user/account/logout`

### 管理员退出登录（需登录）
`GET /admin/account/logout`

### 修改密码（需登录）
`POST /user/password/update`

- Query:
  - `pw` 旧密码
  - `npw` 新密码

### 更新用户信息（需登录）
`POST /user/info/update`

- Query:
  - `nickname`
  - `description`
  - `gender` (0/1/2)

### 更新头像（需登录）
`POST /user/avatar/update`

- Content-Type: `multipart/form-data`
- Form:
  - `file`: 图片文件

### 获取单个用户信息
`GET /user/info/get-one?uid=123`

---

# Video Service（8102）

## 视频状态/元信息
### 修改视频状态（需登录；status=1/2 admin）
`POST /video/change/status`

- Query:
  - `vid`
  - `status` (1 通过/上架, 2 审核拒绝, 3 删除)

### 更新视频元信息（需登录，作者或 admin）
`POST /video/update-meta`

- Query（至少一个）:
  - `vid` (必填)
  - `title`
  - `tags`
  - `mc_id`
  - `sc_id`
  - `descr`
  - `auth`

## 视频列表/详情
### 随机游客视频
`GET /video/random/visitor?count=11`

### 续播推荐（根据已有 vids）
`GET /video/cumulative/visitor?vids=1&vids=2`

### 获取单个视频信息
`GET /video/getone?vid=123`

## 用户作品/行为列表
### 用户作品数
`GET /video/user-works-count?uid=1`

### 用户作品列表
`GET /video/user-works?uid=1&offset=0`

### 用户点赞列表
`GET /video/user-love?uid=1&offset=0`

### 用户播放记录
`GET /video/user-play?uid=1&offset=0`

### 用户收藏列表
`GET /video/user-collect?uid=1&offset=0`

## 播放/点赞
### 记录播放（需登录）
`POST /video/play/user?vid=123`

### 点赞/取消点赞（需登录）
`POST /video/love-or-not`

- Query（支持 snake/camel 两种）:
  - `vid`
  - `is_like` 或 `isLove` (1/0 或 true/false)
  - `is_set` 或 `isSet` (1/0 或 true/false)

---

# Category（8102）
### 获取所有分类
`GET /category/getall`

---

# 视频审核（8102，admin）
### 审核待处理总数
`GET /review/video/total?vstatus=1`

### 审核分页
`GET /review/video/getpage?vstatus=1&page=1&quantity=10`

### 审核单条
`GET /review/video/getone?vid=123`

---

# Comment Service（8103）

### 获取评论树
`GET /comment/get?vid=1&offset=0&type=1`

返回 `data`:
```json
{
  "more": true,
  "comments": [ ... ]
}
```

### 获取更多回复
`GET /comment/reply/get-more?id=123`

### 发布评论（需登录）
`POST /comment/add`

- Query:
  - `vid`
  - `root_id`
  - `parent_id`
  - `to_user_id`
  - `content`

### 删除评论（需登录）
`POST /comment/delete?id=123`

### 获取用户点赞/点踩列表（需登录）
`GET /comment/get-like-and-dislike`

### 设置评论点赞/取消（需登录）
`POST /comment/love-or-not`

- Query（支持 snake/camel）:
  - `id`
  - `is_like` 或 `isLike`
  - `is_set` 或 `isSet`

### 获取 UP 的点赞信息
`GET /comment/get-up-like?uid=1` 或 `?id=1`

---

# Favorite Service（8104）

### 获取用户收藏夹（登录可省略 uid）
`GET /favorite/get-all/user?uid=1`

### 获取公开收藏夹（访客）
`GET /favorite/get-all/visitor?uid=1`

### 创建收藏夹（需登录）
`POST /favorite/create`

- Query:
  - `title`
  - `desc`
  - `visible` (0/1)

### 获取视频已被收藏的收藏夹 ID（需登录）
`GET /video/collected-fids?vid=123`

### 收藏视频（需登录）
`POST /video/collect`

- Query 方式一（单个收藏夹）:
  - `vid`
  - `fid`
- Query 方式二（批量）:
  - `vid`
  - `adds=1&adds=2`
  - `removes=3&removes=4`

### 取消收藏（需登录）
`POST /video/cancel-collect?vid=123&fid=1`

---

# Danmu Service（8105）

### 获取弹幕列表
`GET /danmu-list/{vid}`

### 删除弹幕（需登录；作者或 admin）
`POST /danmu/delete?id=123`

---

# IM Service（8106）

### 创建私信会话（需登录）
`GET /msg/chat/create/{uid}`

### 最近会话列表（需登录）
`GET /msg/chat/recent-list?offset=0`

### 删除会话（需登录）
`GET /msg/chat/delete/{uid}`

### 上线通知（需登录）
`GET /msg/chat/online?from=uid`

### 离线通知
`GET /msg/chat/outline?from=uid&to=uid`

### 获取更多聊天记录（需登录）
`GET /msg/chat-detailed/get-more?uid=targetUid&offset=0`

### 删除聊天记录（需登录）
`POST /msg/chat-detailed/delete?id=123`

### 获取未读数（需登录）
`GET /msg-unread/all`

### 清空未读（需登录）
`POST /msg-unread/clear?column=xxx`（也可用 `type`）

---

# Upload Service（8108）

### 询问分片进度
`GET /video/ask-chunk?hash=md5`

### 上传分片
`POST /video/upload-chunk`

- Query:
  - `hash`
  - `index`

### 取消上传
`GET /video/cancel-upload?hash=md5`

### 添加视频（封面 + 元信息）
`POST /video/add`

- Content-Type: `multipart/form-data`
- Form:
  - `cover` (file)
  - `hash`
  - `title`
  - `type`
  - `auth`
  - `duration`
  - `mcid`
  - `scid`
  - `tags`
  - `descr`

### 直传视频（需登录）
`POST /video/direct-upload`

- Content-Type: `multipart/form-data`
- Form:
  - `video` (file)
  - `cover` (file)
  - `title`
  - `type`
  - `duration`
  - `mcid`
  - `scid`
  - `tags` (可选)
  - `description` (可选)

---

# Search Service（8107）

### 热搜词
`GET /search/hot/get`

### 记录搜索词
`POST /search/word/add?keyword=xxx`（也可用 `text`）

### 搜索提示词
`GET /search/word/get?keyword=xxx`

### 搜索结果数量（视频 + 用户）
`GET /search/count?keyword=xxx`

### 搜索视频（仅通过审核）
`GET /search/video/only-pass?keyword=xxx&page=1`

### 搜索用户
`GET /search/user?keyword=xxx&page=1`

---

# Stats Service（8109）

### 访客播放数 +1
`POST /video/play/visitor?vid=123`

### 点赞数 +1/-1
`POST /video/love-or-not?vid=123&isLike=1&isSet=1`

说明：`isLike=1` 才会更新；`isSet=1` 表示点赞，`isSet=0` 表示取消。

---

# GraphQL Gateway（8080）

### Query
```
randomVisitorVideos(count: Int): [Int]
videoGetOne(vid: Int): Int
videoCumulativeVisitor(vids: [Int]): [Int]
uploadAskChunk(hash: String): Int
```

### Mutation
```
userRegister(username: String, password: String): String
userLogin(username: String, password: String): String
adminLogin(username: String, password: String): String
uploadChunkUrl(hash: String, index: Int): String
uploadCancel(hash: String): String
```

GraphQL 请求示例：
```json
{
  "query": "mutation($u:String!,$p:String!){ userLogin(username:$u,password:$p) }",
  "variables": { "u": "test", "p": "123456" }
}
```
