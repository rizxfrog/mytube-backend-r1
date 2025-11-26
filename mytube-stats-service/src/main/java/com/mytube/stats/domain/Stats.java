package com.mytube.stats.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("stats")
public class Stats {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer videoId;
    private Integer play;
    private Integer like;
    private Integer danmu;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getVideoId() { return videoId; }
    public void setVideoId(Integer videoId) { this.videoId = videoId; }
    public Integer getPlay() { return play; }
    public void setPlay(Integer play) { this.play = play; }
    public Integer getLike() { return like; }
    public void setLike(Integer like) { this.like = like; }
    public Integer getDanmu() { return danmu; }
    public void setDanmu(Integer danmu) { this.danmu = danmu; }
}

