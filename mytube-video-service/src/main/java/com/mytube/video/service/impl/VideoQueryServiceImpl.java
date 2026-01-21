package com.mytube.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mytube.common.po.dao.VideoDAO;
import com.mytube.common.redis.RedisUtil;
import com.mytube.video.domain.UserVideo;
import com.mytube.video.mapper.UserVideoMapper;
import com.mytube.video.mapper.VideoMapper;
import com.mytube.video.service.VideoQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VideoQueryServiceImpl implements VideoQueryService {
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private UserVideoMapper userVideoMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Integer getUserWorksCount(Long uid) {
        if (uid == null) {
            return 0;
        }
        QueryWrapper<VideoDAO> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).ne("status", 3);
        Long count = videoMapper.selectCount(wrapper);
        return count == null ? 0 : count.intValue();
    }

    @Override
    public List<VideoDAO> getUserWorks(Long uid, Long offset) {
        if (uid == null) {
            return Collections.emptyList();
        }
        QueryWrapper<VideoDAO> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).ne("status", 3).orderByDesc("upload_date").last("LIMIT 10 OFFSET " + offset);
        return videoMapper.selectList(wrapper);
    }

    @Override
    public List<VideoDAO> getUserLove(Long uid, Long offset) {
        if (uid == null) {
            return Collections.emptyList();
        }
        QueryWrapper<UserVideo> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).eq("love", 1).orderByDesc("love_time").last("LIMIT 10 OFFSET " + offset);
        List<UserVideo> list = userVideoMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> vids = list.stream().map(UserVideo::getVid).collect(Collectors.toList());
        QueryWrapper<VideoDAO> vwrapper = new QueryWrapper<>();
        vwrapper.in("vid", vids).ne("status", 3);
        return videoMapper.selectList(vwrapper);
    }

    @Override
    public List<VideoDAO> getUserPlay(Long uid, Long offset) {
        if (uid == null) {
            return Collections.emptyList();
        }
        Set<Object> set = redisUtil.zReverange("user_video_history:" + uid, offset, offset + 9);
        if (set == null || set.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> vids = set.stream().map(v -> Long.parseLong(v.toString())).collect(Collectors.toList());
        QueryWrapper<VideoDAO> vwrapper = new QueryWrapper<>();
        vwrapper.in("vid", vids).ne("status", 3);
        return videoMapper.selectList(vwrapper);
    }

    @Override
    public List<VideoDAO> getUserCollect(Long uid, Long offset) {
        if (uid == null) {
            return Collections.emptyList();
        }
        QueryWrapper<UserVideo> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).eq("collect", 1).orderByDesc("play_time").last("LIMIT 10 OFFSET " + offset);
        List<UserVideo> list = userVideoMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> vids = list.stream().map(UserVideo::getVid).collect(Collectors.toList());
        QueryWrapper<VideoDAO> vwrapper = new QueryWrapper<>();
        vwrapper.in("vid", vids).ne("status", 3);
        return videoMapper.selectList(vwrapper);
    }
}
