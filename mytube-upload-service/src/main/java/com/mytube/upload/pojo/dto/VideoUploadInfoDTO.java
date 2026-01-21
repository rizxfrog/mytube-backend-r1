package com.mytube.upload.pojo.dto;

public class VideoUploadInfoDTO {
    private Long uid;
    private String hash;
    private String title;
    private Long type;
    private Long auth;
    private Double duration;
    private String mcId;
    private String scId;
    private String tags;
    private String descr;
    private String coverUrl;

    public VideoUploadInfoDTO() {}

    public VideoUploadInfoDTO(Long uid, String hash, String title, Long type, Long auth, Double duration, String mcId, String scId, String tags, String descr, String coverUrl) {
        this.uid = uid;
        this.hash = hash;
        this.title = title;
        this.type = type;
        this.auth = auth;
        this.duration = duration;
        this.mcId = mcId;
        this.scId = scId;
        this.tags = tags;
        this.descr = descr;
        this.coverUrl = coverUrl;
    }

    public Long getUid() { return uid; }
    public void setUid(Long uid) { this.uid = uid; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getType() { return type; }
    public void setType(Long type) { this.type = type; }
    public Long getAuth() { return auth; }
    public void setAuth(Long auth) { this.auth = auth; }
    public Double getDuration() { return duration; }
    public void setDuration(Double duration) { this.duration = duration; }
    public String getMcId() { return mcId; }
    public void setMcId(String mcId) { this.mcId = mcId; }
    public String getScId() { return scId; }
    public void setScId(String scId) { this.scId = scId; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getDescr() { return descr; }
    public void setDescr(String descr) { this.descr = descr; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
}
