package com.mytube.video.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("user_video")
public class UserVideo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long uid;
    private Long vid;
    private Long play;
    private Long love;
    private Long unlove;
    private Long coin;
    private Long collect;
    private Date play_time;
    private Date love_time;
    private Date coin_time;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUid() { return uid; }
    public void setUid(Long uid) { this.uid = uid; }
    public Long getVid() { return vid; }
    public void setVid(Long vid) { this.vid = vid; }
    public Long getPlay() { return play; }
    public void setPlay(Long play) { this.play = play; }
    public Long getLove() { return love; }
    public void setLove(Long love) { this.love = love; }
    public Long getUnlove() { return unlove; }
    public void setUnlove(Long unlove) { this.unlove = unlove; }
    public Long getCoin() { return coin; }
    public void setCoin(Long coin) { this.coin = coin; }
    public Long getCollect() { return collect; }
    public void setCollect(Long collect) { this.collect = collect; }
    public Date getPlay_time() { return play_time; }
    public void setPlay_time(Date play_time) { this.play_time = play_time; }
    public Date getLove_time() { return love_time; }
    public void setLove_time(Date love_time) { this.love_time = love_time; }
    public Date getCoin_time() { return coin_time; }
    public void setCoin_time(Date coin_time) { this.coin_time = coin_time; }
}
