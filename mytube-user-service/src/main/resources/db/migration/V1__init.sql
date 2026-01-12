CREATE TABLE if not exists "users"  (
                        uid bigserial PRIMARY KEY,
                        username VARCHAR(50) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        nickname VARCHAR(32) NOT NULL,
                        avatar VARCHAR(500),
                        background VARCHAR(500),
                        gender SMALLINT NOT NULL DEFAULT 2,           -- 0女 1男 2未知
                        description VARCHAR(100),
                        exp INTEGER NOT NULL DEFAULT 0,
                        coin DOUBLE PRECISION NOT NULL DEFAULT 0,     -- PostgreSQL 用 double precision
                        vip SMALLINT NOT NULL DEFAULT 0,              -- 0普通用户 1月度大会员 ...
                        state SMALLINT NOT NULL DEFAULT 0,            -- 0正常 1封禁 2注销
                        role SMALLINT NOT NULL DEFAULT 0,             -- 0普通用户 1管理员 2超级管理员
                        auth SMALLINT NOT NULL DEFAULT 0,             -- 0普通 1个人 2机构
                        auth_msg VARCHAR(30),
                        create_date TIMESTAMP NOT NULL,
                        delete_date TIMESTAMP,

                        CONSTRAINT user_username_unique UNIQUE (username),
                        CONSTRAINT user_nickname_unique UNIQUE (nickname)
);

COMMENT ON TABLE "users" IS '用户表';

COMMENT ON COLUMN "users".uid IS '用户ID';
COMMENT ON COLUMN "users".username IS '用户账号';
COMMENT ON COLUMN "users".password IS '用户密码';
COMMENT ON COLUMN "users".nickname IS '用户昵称';
COMMENT ON COLUMN "users".avatar IS '用户头像url';
COMMENT ON COLUMN "users".background IS '主页背景图url';
COMMENT ON COLUMN "users".gender IS '性别 0女 1男 2未知';
COMMENT ON COLUMN "users".description IS '个性签名';
COMMENT ON COLUMN "users".exp IS '经验值';
COMMENT ON COLUMN "users".coin IS '硬币数';
COMMENT ON COLUMN "users".vip IS '会员类型 0普通用户 1月度大会员 2季度大会员 3年度大会员';
COMMENT ON COLUMN "users".state IS '状态 0正常 1封禁 2注销';
COMMENT ON COLUMN "users".role IS '角色类型 0普通用户 1管理员 2超级管理员';
COMMENT ON COLUMN "users".auth IS '官方认证 0普通 1个人认证 2机构认证';
COMMENT ON COLUMN "users".auth_msg IS '认证说明';
COMMENT ON COLUMN "users".create_date IS '创建时间';
COMMENT ON COLUMN "users".delete_date IS '注销时间';
