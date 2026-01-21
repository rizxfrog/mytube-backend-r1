package com.mytube.video.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mytube.api.user.UserServiceApi;
import com.mytube.api.video.VideoServiceApi;
import com.mytube.api.video.VideoStatsServiceApi;
import com.mytube.common.po.dao.VideoStatsDAO;
import com.mytube.common.po.dao.VideoDAO;
import com.mytube.common.redis.RedisUtil;
import com.mytube.video.mapper.VideoMapper;
import com.mytube.video.service.CategoryService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@DubboService
public class VideoServiceProvider implements VideoServiceApi {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private CategoryService categoryService;

    @DubboReference
    private UserServiceApi userServiceApi;

    @Autowired
    private VideoStatsServiceApi videoStatsServiceApi;

    @Autowired
    @Qualifier("videoTaskExecutor")
    private Executor taskExecutor;

    @Override
    public List<Integer> randomVisitorVideos(int count) {
        int c = count <= 0 ? 11 : count;
        SetOperations<String, String> ops = stringRedisTemplate.opsForSet();
        Set<String> members = ops.distinctRandomMembers("video_status:1", c);
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> ids = members.stream()
                .filter(Objects::nonNull)
                .map(s -> {
                    try { return Integer.parseInt(s); } catch (Exception e) { return null; }
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Collections.shuffle(ids);
        if (ids.size() > c) {
            return ids.subList(0, c);
        }
        return ids;
    }

    @Override
    public Integer getOneVideoById(Integer vid) {
        VideoDAO video = redisUtil.getObject("video:" + vid, VideoDAO.class);
        if (video == null) {
            QueryWrapper<VideoDAO> wrapper = new QueryWrapper<>();
            wrapper.eq("vid", vid).ne("status", 3);
            video = videoMapper.selectOne(wrapper);
            if (video == null) {
                return null;
            }
            VideoDAO finalVideo = video;
            CompletableFuture.runAsync(() -> redisUtil.setExObjectValue("video:" + vid, finalVideo), taskExecutor);
        }
        return video.getVid();
    }

    @Override
    public List<Integer> cumulativeVisitorVideos(List<Integer> vids) {
        return vids;
    }

    @Override
    public Long getVideoOwner(Long vid) {
        if (vid == null) {
            return null;
        }
        QueryWrapper<VideoDAO> wrapper = new QueryWrapper<>();
        wrapper.eq("vid", vid).ne("status", 3);
        VideoDAO video = videoMapper.selectOne(wrapper);
        return video == null ? null : video.getUid();
    }

    @Override
    public Object getVideoInfo(Long vid) {
        if (vid == null) {
            return null;
        }
        VideoDAO video = redisUtil.getObject("video:" + vid, VideoDAO.class);
        if (video == null) {
            QueryWrapper<VideoDAO> wrapper = new QueryWrapper<>();
            wrapper.eq("vid", vid).ne("status", 3);
            video = videoMapper.selectOne(wrapper);
            if (video == null) {
                return null;
            }
            VideoDAO finalVideo = video;
            CompletableFuture.runAsync(() -> redisUtil.setExObjectValue("video:" + vid, finalVideo), taskExecutor);
        }
        return buildVideoInfo(video);
    }

    @Override
    public Object getVideoInfoList(List<Long> vids) {
        if (vids == null || vids.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<VideoDAO> wrapper = new QueryWrapper<>();
        wrapper.in("vid", vids).ne("status", 3);
        List<VideoDAO> videos = videoMapper.selectList(wrapper);
        if (videos == null || videos.isEmpty()) {
            return Collections.emptyList();
        }
        return vids.stream()
                .map(id -> videos.stream().filter(v -> Objects.equals(v.getVid(), id)).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .map(this::buildVideoInfo)
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildVideoInfo(VideoDAO video) {
        Map<String, Object> map = new HashMap<>();
        map.put("video", video);
        CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
            map.put("user", userServiceApi.getUserInfo(video.getUid()));
        }, taskExecutor);
        CompletableFuture<Void> statsFuture = CompletableFuture.runAsync(() -> {
            VideoStatsDAO stats = videoStatsServiceApi.getVideoStatsById(video.getVid());
            map.put("stats", stats);
        }, taskExecutor);
        CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
            map.put("category", categoryService.getCategoryById(video.getMc_id(), video.getSc_id()));
        }, taskExecutor);
        userFuture.join();
        statsFuture.join();
        categoryFuture.join();
        return map;
    }
}
