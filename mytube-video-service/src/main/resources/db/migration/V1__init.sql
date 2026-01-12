-- user_video 表
CREATE TABLE if not exists user_video
(
    id        bigserial PRIMARY KEY, -- 自动序列，相当于 AUTO_INCREMENT
    uid       bigint   NOT NULL, -- 用户 UID
    vid       bigint   NOT NULL, -- 视频 ID
    play      bigint   NOT NULL DEFAULT 0,
    love      bigint  NOT NULL DEFAULT 0,
    unlove    bigint  NOT NULL DEFAULT 0,
    coin      bigint  NOT NULL DEFAULT 0,
    collect   bigint  NOT NULL DEFAULT 0,
    play_time TIMESTAMP NOT NULL,
    love_time TIMESTAMP,
    coin_time TIMESTAMP,

    CONSTRAINT uid_vid_unique UNIQUE (uid, vid)
);

COMMENT ON TABLE user_video IS '用户视频关联表';
COMMENT ON COLUMN user_video.love IS '点赞 0没赞 1已点赞';
COMMENT ON COLUMN user_video.unlove IS '不喜欢 0没点 1已不喜欢';
COMMENT ON COLUMN user_video.coin IS '投币数 0-2 默认0';
COMMENT ON COLUMN user_video.collect IS '收藏 0没收藏 1已收藏';

-- video 表
CREATE TABLE if not exists video
(
    vid         bigserial PRIMARY KEY,                  -- AUTO_INCREMENT → SERIAL
    uid         bigint          NOT NULL,           -- 投稿用户ID
    title       VARCHAR(80)      NOT NULL,
    type        bigint         NOT NULL DEFAULT 1,
    auth        bigint         NOT NULL DEFAULT 0,
    duration    DOUBLE PRECISION NOT NULL DEFAULT 0,
    mc_id       VARCHAR(20)      NOT NULL,
    sc_id       VARCHAR(20)      NOT NULL,
    tags        VARCHAR(500),                        -- 标签
    descr       VARCHAR(2000),                       -- 简介
    cover_url   VARCHAR(500)     NOT NULL,
    video_url   VARCHAR(500)     NOT NULL,
    status      SMALLINT         NOT NULL DEFAULT 0, -- 状态
    upload_date TIMESTAMP        NOT NULL,
    delete_date TIMESTAMP
);

COMMENT ON TABLE video IS '视频表';

-- video_stats 表
CREATE TABLE if not exists video_stats
(
    vid     bigserial PRIMARY KEY, -- 依旧使用视频ID作为 PK
    play    INTEGER NOT NULL DEFAULT 0,
    danmu   INTEGER NOT NULL DEFAULT 0,
    good    INTEGER NOT NULL DEFAULT 0,
    bad     INTEGER NOT NULL DEFAULT 0,
    coin    INTEGER NOT NULL DEFAULT 0,
    collect INTEGER NOT NULL DEFAULT 0,
    share   INTEGER NOT NULL DEFAULT 0,
    comment INTEGER          DEFAULT 0
);

COMMENT ON TABLE video_stats IS '视频数据统计表';

