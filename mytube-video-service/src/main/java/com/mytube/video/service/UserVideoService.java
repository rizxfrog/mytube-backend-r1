package com.mytube.video.service;

import com.mytube.video.domain.UserVideo;

public interface UserVideoService {
    UserVideo updatePlay(Long uid, Long vid);
    UserVideo setLoveOrUnlove(Long uid, Long vid, boolean isLove, boolean isSet);
    void collectOrCancel(Long uid, Long vid, boolean isCollect);
}
