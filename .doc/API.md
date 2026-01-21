# MyTube Backend API Documentation

## Overview
- Base response: `{"code":0,"message":"ok","data":...}`
- `code=0` success; non-zero indicates error (e.g., `401` unauthorized, `403` forbidden, `404` not found, `500` server error).
- Auth header: `Authorization: Bearer <token>` (JWT). Roles in token: `user` or `admin`.
- Pagination: use `page` + `quantity` (1-based), or `offset` + `quantity` (0-based), depending on endpoint.

## GraphQL (gateway)
Endpoint: `/graphql`

Queries
- `randomVisitorVideos(count: Int): [Int!]!`
- `videoGetOne(vid: Int!): Int`
- `videoCumulativeVisitor(vids: [Int!]!): [Int!]!`
- `uploadAskChunk(hash: String!): Int`

Mutations
- `userRegister(username: String!, password: String!): String`
- `userLogin(username: String!, password: String!): String`
- `adminLogin(username: String!, password: String!): String`
- `uploadChunkUrl(hash: String!, index: Int!): String`
- `uploadCancel(hash: String!): String`

## User Service (REST)
- `POST /user/account/register` body: `{username,password,confirmedPassword}`
- `POST /user/account/login` body: `{username,password}`
- `POST /admin/account/login` body: `{username,password}`
- `GET /user/personal/info` auth
- `GET /admin/personal/info` auth (admin)
- `GET /user/account/logout` auth
- `GET /admin/account/logout` auth (admin)
- `POST /user/password/update` auth params: `pw,npw`
- `POST /user/info/update` auth params: `nickname,description,gender`
- `POST /user/avatar/update` auth file: `file`
- `GET /user/info/get-one` params: `uid`

## Upload Service (REST)
- `GET /video/ask-chunk` auth params: `hash` (returns uploaded chunk count)
- `POST /video/upload-chunk` auth params: `hash,index` (returns presigned URL)
- `GET /video/cancel-upload` auth params: `hash`
- `POST /video/add` auth params: `cover,hash,title,type,auth,duration,mcid,scid,tags,descr`
- `POST /video/direct-upload` auth params: `video,cover,title,type,duration,mcid,scid,tags?,description?`

## Video Service (REST)
- `GET /video/random/visitor` params: `count?`
- `GET /video/cumulative/visitor` params: `vids` (repeatable or list)
- `GET /video/getone` params: `vid`
- `GET /video/user-works-count` params: `uid`
- `GET /video/user-works` params: `uid,offset`
- `GET /video/user-love` params: `uid,offset`
- `GET /video/user-play` params: `uid,offset`
- `GET /video/user-collect` params: `uid,offset`
- `POST /video/play/user` auth params: `vid`
- `POST /video/love-or-not` auth params: `vid,is_like,is_set`
- `POST /video/change/status` admin params: `vid,status` (1 pass, 2 reject, 3 delete)
- `POST /video/update-meta` author/admin params: `vid,title?,tags?,mc_id?,sc_id?,descr?,auth?`
- Review (admin):
  - `GET /review/video/total` params: `vstatus`
  - `GET /review/video/getpage` params: `vstatus,page?,quantity?`
  - `GET /review/video/getone` params: `vid`
- Category:
  - `GET /category/getall`

## Stats Service (REST)
- `POST /video/play/visitor` params: `vid`
- `POST /video/love-or-not` params: `vid,isLike,isSet`

## Comment Service (REST)
- `GET /comment/get` params: `vid,offset,type`
- `GET /comment/reply/get-more` params: `id`
- `POST /comment/add` auth params: `vid,root_id,parent_id,to_user_id,content`
- `POST /comment/delete` auth params: `id`
- `GET /comment/get-like-and-dislike` auth
- `POST /comment/love-or-not` auth params: `id,is_like,is_set`
- `GET /comment/get-up-like` params: `id`

## Favorite Service (REST)
- `GET /favorite/get-all/user` auth
- `GET /favorite/get-all/visitor` params: `uid`
- `POST /favorite/create` auth params: `title,desc,visible`
- `GET /video/collected-fids` auth params: `vid`
- `POST /video/collect` auth params: `vid,fid`
- `POST /video/cancel-collect` auth params: `vid,fid`

## Search Service (REST)
- `GET /search/hot/get`
- `POST /search/word/add` params: `text`
- `GET /search/word/get` params: `keyword`
- `GET /search/count` params: `keyword`
- `GET /search/video/only-pass` params: `keyword`
- `GET /search/user` params: `keyword`

## Danmu Service (REST)
- `GET /danmu-list/{vid}`
- `POST /danmu/delete` params: `id`

## IM Service (REST, stubbed)
- `GET /msg/chat/create/{uid}`
- `GET /msg/chat/recent-list`
- `GET /msg/chat/delete/{uid}`
- `GET /msg/chat/online`
- `GET /msg/chat/outline`
- `GET /msg/chat-detailed/get-more` params: `cid,offset`
- `POST /msg/chat-detailed/delete` params: `id`
- `GET /msg-unread/all`
- `POST /msg-unread/clear` params: `type`

Notes
- IM endpoints are placeholders (return empty data).
- Search indexing happens on video status changes and `/video/update-meta`; reindex is available via `.doc/reindex-videos.ps1`.
