package com.mytube.video.provider;

import com.mytube.api.video.UserVideoServiceApi;
import com.mytube.video.service.UserVideoService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class UserVideoServiceProvider implements UserVideoServiceApi {
    @Autowired
    private UserVideoService userVideoService;

    @Override
    public void setCollect(Long uid, Long vid, boolean collect) {
        if (uid == null || vid == null) {
            return;
        }
        userVideoService.collectOrCancel(uid, vid, collect);
    }
}
