package com.mytube.upload.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("video")
public class Video {
    @TableId(value = "vid", type = IdType.AUTO) // 体标注主键列名，启用主键回填：
    private Long vid;
    private Long uid;
    private String title;
    private Long type;
    private Long auth;
    private Double duration;
    private String mc_id;
    private String sc_id;
    private String tags;
    private String descr;
    private String cover_url;
    private String video_url;
    private Integer status;
    private Date upload_date;
    private Date delete_date;

    public Long getVid() { return vid; }
    public void setVid(Long vid) { this.vid = vid; }
    public Long getUid() { return uid; }
    public void setUid(Long uid) { this.uid = uid; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getType() { return type; }
    public void setType(Long type) { this.type = type; }
    public Long getAuth() { return auth; }
    public void setAuth(Long auth) { this.auth = auth; }
    public Double getDuration() { return duration; }
    public void setDuration(Double duration) { this.duration = duration; }
    public String getMc_id() { return mc_id; }
    public void setMc_id(String mc_id) { this.mc_id = mc_id; }
    public String getSc_id() { return sc_id; }
    public void setSc_id(String sc_id) { this.sc_id = sc_id; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getDescr() { return descr; }
    public void setDescr(String descr) { this.descr = descr; }
    public String getCover_url() { return cover_url; }
    public void setCover_url(String cover_url) { this.cover_url = cover_url; }
    public String getVideo_url() { return video_url; }
    public void setVideo_url(String video_url) { this.video_url = video_url; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getUpload_date() { return upload_date; }
    public void setUpload_date(Date upload_date) { this.upload_date = upload_date; }
    public Date getDelete_date() { return delete_date; }
    public void setDelete_date(Date delete_date) { this.delete_date = delete_date; }
}
