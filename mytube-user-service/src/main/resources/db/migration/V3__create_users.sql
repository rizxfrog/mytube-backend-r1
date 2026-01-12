CREATE TABLE IF NOT EXISTS "admin_users"
(
    uid         bigserial PRIMARY KEY,
    username    VARCHAR(50)      NOT NULL,
    password    VARCHAR(255)     NOT NULL,
    nickname    VARCHAR(32)      NOT NULL,
    avatar      VARCHAR(500),
    background  VARCHAR(500),
    gender      SMALLINT         NOT NULL DEFAULT 2,
    description VARCHAR(100),
    position    INTEGER          NOT NULL DEFAULT 0,
    state       SMALLINT         NOT NULL DEFAULT 0,
    create_date TIMESTAMP        NOT NULL,
    delete_date TIMESTAMP,
    CONSTRAINT user_username_unique UNIQUE (username),
    CONSTRAINT user_nickname_unique UNIQUE (nickname)
);

COMMENT ON TABLE "admin_users" IS '管理员用户表';
COMMENT ON COLUMN "admin_users".uid IS '用户ID';
COMMENT ON COLUMN "admin_users".username IS '用户账号';
COMMENT ON COLUMN "admin_users".password IS '用户密码';
COMMENT ON COLUMN "admin_users".nickname IS '用户昵称';
COMMENT ON COLUMN "admin_users".avatar IS '用户头像url';
COMMENT ON COLUMN "admin_users".background IS '主页背景图url';
COMMENT ON COLUMN "admin_users".gender IS '性别 0女 1男 2未知';
COMMENT ON COLUMN "admin_users".description IS '个性签名';
COMMENT ON COLUMN "admin_users".position IS '职位: 1管理员 2审核员 3运营 4行政';
COMMENT ON COLUMN "admin_users".state IS '状态 0正常 1停职 2离职';
COMMENT ON COLUMN "admin_users".create_date IS '创建时间';
COMMENT ON COLUMN "admin_users".delete_date IS '注销时间';