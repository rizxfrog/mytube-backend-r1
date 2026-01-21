package com.mytube.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mytube.common.redis.RedisUtil;
import com.mytube.video.domain.UserVideo;
import com.mytube.video.mapper.UserVideoMapper;
import com.mytube.video.service.UserVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class UserVideoServiceImpl implements UserVideoService {
    @Autowired
    private UserVideoMapper userVideoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("videoTaskExecutor")
    private Executor taskExecutor;

    @Override
    public UserVideo updatePlay(Long uid, Long vid) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);
        if (userVideo == null) {
            userVideo = new UserVideo(null, uid, vid, 1L, 0L, 0L, 0L, 0L, new Date(), null, null);
            userVideoMapper.insert(userVideo);
        } else if (userVideo.getPlay_time() != null && System.currentTimeMillis() - userVideo.getPlay_time().getTime() <= 30000) {
            return userVideo;
        } else {
            userVideo.setPlay(userVideo.getPlay() + 1);
            userVideo.setPlay_time(new Date());
            userVideoMapper.updateById(userVideo);
        }
        CompletableFuture.runAsync(() -> redisUtil.zset("user_video_history:" + uid, vid), taskExecutor);
        return userVideo;
    }

    @Override
    public UserVideo setLoveOrUnlove(Long uid, Long vid, boolean isLove, boolean isSet) {
        String key = "love_video:" + uid;
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);
        if (userVideo == null) {
            userVideo = new UserVideo(null, uid, vid, 0L, 0L, 0L, 0L, 0L, null, null, null);
            userVideoMapper.insert(userVideo);
        }
        UpdateWrapper<UserVideo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).eq("vid", vid);
        if (isLove && isSet) {
            if (userVideo.getLove() != null && userVideo.getLove() == 1) {
                return userVideo;
            }
            userVideo.setLove(1L);
            updateWrapper.set("love", 1).set("love_time", new Date());
            redisUtil.zset(key, vid);
        } else if (isLove) {
            if (userVideo.getLove() == null || userVideo.getLove() == 0) {
                return userVideo;
            }
            userVideo.setLove(0L);
            updateWrapper.set("love", 0);
            redisUtil.zsetDelMember(key, vid);
        } else if (isSet) {
            userVideo.setUnlove(1L);
            updateWrapper.set("unlove", 1);
        } else {
            userVideo.setUnlove(0L);
            updateWrapper.set("unlove", 0);
        }
        userVideoMapper.update(null, updateWrapper);
        return userVideo;
    }

    @Override
    public void collectOrCancel(Long uid, Long vid, boolean isCollect) {
        UpdateWrapper<UserVideo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).eq("vid", vid);
        if (isCollect) {
            updateWrapper.set("collect", 1);
        } else {
            updateWrapper.set("collect", 0);
        }
        userVideoMapper.update(null, updateWrapper);
    }
}
