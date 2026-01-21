package com.mytube.video.service;

import com.mytube.common.po.dao.VideoDAO;

import java.util.List;

public interface VideoQueryService {
    Integer getUserWorksCount(Long uid);
    List<VideoDAO> getUserWorks(Long uid, Long offset);
    List<VideoDAO> getUserLove(Long uid, Long offset);
    List<VideoDAO> getUserPlay(Long uid, Long offset);
    List<VideoDAO> getUserCollect(Long uid, Long offset);
}
