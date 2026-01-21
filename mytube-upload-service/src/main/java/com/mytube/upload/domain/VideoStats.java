package com.mytube.upload.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("video_stats")
public class VideoStats {
    @TableId
    private Long vid;
    private Integer play;
    private Integer danmu;
    private Integer good;
    private Integer bad;
    private Integer coin;
    private Integer collect;
    private Integer share;
    private Integer comment;

    public VideoStats() {}

    public VideoStats(Long vid, Integer play, Integer danmu, Integer good, Integer bad, Integer coin, Integer collect, Integer share, Integer comment) {
        this.vid = vid; this.play = play; this.danmu = danmu; this.good = good; this.bad = bad; this.coin = coin; this.collect = collect; this.share = share; this.comment = comment;
    }
    public Long getVid() { return vid; }
    public void setVid(Long vid) { this.vid = vid; }
    public Integer getPlay() { return play; }
    public void setPlay(Integer play) { this.play = play; }
    public Integer getDanmu() { return danmu; }
    public void setDanmu(Integer danmu) { this.danmu = danmu; }
    public Integer getGood() { return good; }
    public void setGood(Integer good) { this.good = good; }
    public Integer getBad() { return bad; }
    public void setBad(Integer bad) { this.bad = bad; }
    public Integer getCoin() { return coin; }
    public void setCoin(Integer coin) { this.coin = coin; }
    public Integer getCollect() { return collect; }
    public void setCollect(Integer collect) { this.collect = collect; }
    public Integer getShare() { return share; }
    public void setShare(Integer share) { this.share = share; }
    public Integer getComment() { return comment; }
    public void setComment(Integer comment) { this.comment = comment; }
}
