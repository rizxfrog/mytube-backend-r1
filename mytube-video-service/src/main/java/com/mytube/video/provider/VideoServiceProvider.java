package com.mytube.video.provider;

import com.mytube.api.video.VideoServiceApi;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collections;
import java.util.List;

@DubboService
public class VideoServiceProvider implements VideoServiceApi {
    @Override
    public List<Integer> randomVisitorVideos(int count) {
        return Collections.emptyList();
    }
}

