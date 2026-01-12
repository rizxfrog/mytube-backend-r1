package com.mytube.video.provider;

import com.mytube.api.video.VideoStatsServiceApi;
import com.mytube.common.po.dao.VideoStatsDAO;
import com.mytube.common.redis.RedisUtil;
import com.mytube.video.mapper.VideoStatsMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@DubboService
public class VideoStatsService implements VideoStatsServiceApi {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private VideoStatsMapper videoStatsMapper;
    @Autowired
    @Qualifier("videoTaskExecutor")
    private Executor taskExecutor;


    @Override
    public VideoStatsDAO getVideoStatsById(Long vid) {
        VideoStatsDAO videoStats = redisUtil.getObject("videoStats:" + vid, VideoStatsDAO.class);
        if (videoStats == null) {
            videoStats = videoStatsMapper.selectById(vid);
            if (videoStats != null) {
                VideoStatsDAO finalVideoStats = videoStats;
                CompletableFuture.runAsync(() -> {
                    redisUtil.setExObjectValue("videoStats:" + vid, finalVideoStats);
                }, taskExecutor);
            } else {
                return null;
            }
        }
        return videoStats;
    }
}
