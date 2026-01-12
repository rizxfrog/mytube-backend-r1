package com.mytube.api.video;

import com.mytube.common.po.dao.VideoStatsDAO;

public interface VideoStatsServiceApi {
    VideoStatsDAO getVideoStatsById(Long vid);
}
