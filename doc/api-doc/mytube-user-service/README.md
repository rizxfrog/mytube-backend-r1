# MyTube User Service API

## Overview
Base URL is the user service itself. Responses use `CustomResponse`: `{ "code": 0, "message": "ok", "data": ... }`.
Endpoints that require login expect `Authorization: Bearer <token>`.

## Account
- `POST /user/account/register`
  - Body (JSON): `{ "username": "u", "password": "p", "confirmedPassword": "p" }`
  - Result: `data` is empty, `message` is `Register success` on success.
- `POST /user/account/login`
  - Body (JSON): `{ "username": "u", "password": "p" }`
  - Result: `data.token` and `data.user`.
- `POST /admin/account/login`
  - Body (JSON): `{ "username": "admin", "password": "p" }`
  - Result: `data.token` and `data.user` (admin only).
- `GET /user/account/logout` (auth)
- `GET /admin/account/logout` (auth)
- `POST /user/password/update` (auth)
  - Params: `pw`, `npw`

## Profile
- `GET /user/personal/info` (auth)
  - Result: current user profile.
- `GET /admin/personal/info` (auth)
  - Result: current admin profile.
- `GET /user/info/get-one`
  - Params: `uid`
  - Result: public user profile.
- `POST /user/info/update` (auth)
  - Params: `nickname`, `description`, `gender`
- `POST /user/avatar/update` (auth, multipart)
  - Form: `file` (image)
  - Result: `data` is the stored avatar key.

## User DTO (partial)
Example `data` shape for profile endpoints:
```json
{
  "uid": 1,
  "nickname": "user_1",
  "avatar_url": "avatar/1/1710000000.jpg",
  "bg_url": "https://...",
  "gender": 2,
  "description": "",
  "exp": 0,
  "coin": 0.0,
  "vip": 0,
  "state": 0,
  "auth": 0,
  "authMsg": null,
  "videoCount": 0,
  "followsCount": 0,
  "fansCount": 0,
  "loveCount": 0,
  "playCount": 0
}
```
