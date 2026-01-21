package com.mytube.video.controller;

import com.mytube.api.video.VideoServiceApi;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mytube.api.search.SearchIndexServiceApi;
import com.mytube.api.search.VideoIndexDTO;
import com.mytube.common.po.dao.VideoDAO;
import com.mytube.common.redis.RedisUtil;
import com.mytube.common.security.CurrentUser;
import com.mytube.common.web.CustomResponse;
import com.mytube.video.mapper.VideoMapper;
import com.mytube.video.service.UserVideoService;
import com.mytube.video.service.VideoQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.apache.dubbo.config.annotation.DubboReference;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

@RestController
@RequestMapping("/video")
public class VideoController {
    @Autowired
    private VideoServiceApi videoServiceApi;
    @Autowired
    private VideoQueryService videoQueryService;
    @Autowired
    private UserVideoService userVideoService;
    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private RedisUtil redisUtil;
    @DubboReference
    private SearchIndexServiceApi searchIndexServiceApi;
    @PostMapping("/change/status")
    public CustomResponse<String> changeStatus(@RequestParam("vid") Integer vid,
                                               @RequestParam("status") Integer status) {
        if (vid == null || status == null) {
            return CustomResponse.error(400, "Missing parameters");
        }
        if (status != 1 && status != 2 && status != 3) {
            return CustomResponse.error(400, "Invalid status");
        }
        QueryWrapper<VideoDAO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid).ne("status", 3);
        VideoDAO video = videoMapper.selectOne(queryWrapper);
        if (video == null) {
            return CustomResponse.error(404, "Video not found");
        }
        if (status == 1 || status == 2) {
            String role = currentUser.getRole();
            if (!"admin".equals(role)) {
                return CustomResponse.error(403, "Admin only");
            }
            Integer lastStatus = video.getStatus();
            UpdateWrapper<VideoDAO> wrapper = new UpdateWrapper<>();
            wrapper.eq("vid", vid).set("status", status);
            if (status == 1) {
                wrapper.set("upload_date", new Date());
            }
            int updated = videoMapper.update(null, wrapper);
            if (updated > 0) {
                if (lastStatus != null) {
                    redisUtil.delMember("video_status:" + lastStatus, vid);
                }
                redisUtil.addMember("video_status:" + status, vid);
                if (status == 1) {
                    redisUtil.zset("user_video_upload:" + video.getUid(), video.getVid());
                } else {
                    redisUtil.zsetDelMember("user_video_upload:" + video.getUid(), video.getVid());
                }
                redisUtil.delValue("video:" + vid);
                try {
                    VideoIndexDTO dto = new VideoIndexDTO();
                    dto.setVid(video.getVid());
                    dto.setUid(video.getUid());
                    dto.setTitle(video.getTitle());
                    dto.setMcId(video.getMc_id());
                    dto.setScId(video.getSc_id());
                    dto.setTags(video.getTags());
                    dto.setStatus(status);
                    searchIndexServiceApi.upsertVideo(dto);
                } catch (Exception ignored) {}
                return CustomResponse.ok("ok");
            }
            return CustomResponse.error(500, "Update failed");
        }

        Long uid = currentUser.requireUserId();
        String role = currentUser.getRole();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        if (!uid.equals(video.getUid()) && !"admin".equals(role)) {
            return CustomResponse.error(403, "No permission");
        }
        Integer lastStatus = video.getStatus();
        UpdateWrapper<VideoDAO> wrapper = new UpdateWrapper<>();
        wrapper.eq("vid", vid).set("status", 3).set("delete_date", new Date());
        int updated = videoMapper.update(null, wrapper);
        if (updated > 0) {
            if (lastStatus != null) {
                redisUtil.delMember("video_status:" + lastStatus, vid);
            }
            redisUtil.delValue("video:" + vid);
            redisUtil.zsetDelMember("user_video_upload:" + video.getUid(), video.getVid());
            try {
                searchIndexServiceApi.deleteVideo(video.getVid());
            } catch (Exception ignored) {}
            return CustomResponse.ok("ok");
        }
        return CustomResponse.error(500, "Update failed");
    }

    @PostMapping("/update-meta")
    public CustomResponse<String> updateMeta(@RequestParam("vid") Integer vid,
                                             @RequestParam(value = "title", required = false) String title,
                                             @RequestParam(value = "tags", required = false) String tags,
                                             @RequestParam(value = "mc_id", required = false) String mcId,
                                             @RequestParam(value = "sc_id", required = false) String scId,
                                             @RequestParam(value = "descr", required = false) String descr,
                                             @RequestParam(value = "auth", required = false) Integer auth) {
        if (vid == null) {
            return CustomResponse.error(400, "Missing vid");
        }
        if (title == null && tags == null && mcId == null && scId == null && descr == null && auth == null) {
            return CustomResponse.error(400, "No fields to update");
        }
        QueryWrapper<VideoDAO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid).ne("status", 3);
        VideoDAO video = videoMapper.selectOne(queryWrapper);
        if (video == null) {
            return CustomResponse.error(404, "Video not found");
        }
        Long uid = currentUser.requireUserId();
        String role = currentUser.getRole();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        if (!Objects.equals(uid, video.getUid()) && !"admin".equals(role)) {
            return CustomResponse.error(403, "No permission");
        }
        UpdateWrapper<VideoDAO> update = new UpdateWrapper<>();
        update.eq("vid", vid);
        if (title != null) {
            update.set("title", title);
        }
        if (tags != null) {
            update.set("tags", tags);
        }
        if (mcId != null) {
            update.set("mc_id", mcId);
        }
        if (scId != null) {
            update.set("sc_id", scId);
        }
        if (descr != null) {
            update.set("descr", descr);
        }
        if (auth != null) {
            update.set("auth", auth);
        }
        int updated = videoMapper.update(null, update);
        if (updated <= 0) {
            return CustomResponse.error(500, "Update failed");
        }
        redisUtil.delValue("video:" + vid);
        VideoDAO refreshed = videoMapper.selectById(vid);
        if (refreshed != null) {
            try {
                VideoIndexDTO dto = new VideoIndexDTO();
                dto.setVid(refreshed.getVid());
                dto.setUid(refreshed.getUid());
                dto.setTitle(refreshed.getTitle());
                dto.setMcId(refreshed.getMc_id());
                dto.setScId(refreshed.getSc_id());
                dto.setTags(refreshed.getTags());
                dto.setStatus(refreshed.getStatus());
                searchIndexServiceApi.upsertVideo(dto);
            } catch (Exception ignored) {}
        }
        return CustomResponse.ok("ok");
    }

    @GetMapping("/random/visitor")
    public CustomResponse<Object> randomVisitor(@RequestParam(required = false) Integer count) {
        int c = count == null || count <= 0 ? 11 : count;
        Set<Object> idSet = redisUtil.srandmember("video_status:1", c);
        if (idSet == null || idSet.isEmpty()) {
            return CustomResponse.ok(new java.util.ArrayList<>());
        }
        List<Long> ids = idSet.stream()
                .filter(Objects::nonNull)
                .map(v -> Long.parseLong(v.toString()))
                .toList();
        return CustomResponse.ok(videoServiceApi.getVideoInfoList(ids));
    }

    @GetMapping("/cumulative/visitor")
    public CustomResponse<Object> cumulativeVisitor(@RequestParam List<Integer> vids) {
        List<Integer> vidList = vids == null ? new java.util.ArrayList<>() : vids;
        Set<Object> set = redisUtil.getMembers("video_status:1");
        HashMap<String, Object> map = new HashMap<>();
        if (set == null) {
            map.put("videos", new java.util.ArrayList<>());
            map.put("vids", new java.util.ArrayList<>());
            map.put("more", false);
            return CustomResponse.ok(map);
        }
        vidList.forEach(set::remove);
        Set<Object> idSet = new java.util.HashSet<>();
        Random random = new Random();
        for (int i = 0; i < 10 && !set.isEmpty(); i++) {
            Object[] arr = set.toArray();
            int randomIndex = random.nextInt(set.size());
            idSet.add(arr[randomIndex]);
            set.remove(arr[randomIndex]);
        }
        List<Long> ids = idSet.stream()
                .filter(Objects::nonNull)
                .map(v -> Long.parseLong(v.toString()))
                .toList();
        map.put("videos", videoServiceApi.getVideoInfoList(ids));
        map.put("vids", idSet);
        map.put("more", !set.isEmpty());
        return CustomResponse.ok(map);
    }

    @GetMapping("/getone")
    public CustomResponse<Object> getOne(@RequestParam Integer vid) {
        Object data = videoServiceApi.getVideoInfo(vid == null ? null : vid.longValue());
        if (data instanceof java.util.Map<?, ?> map) {
            Object video = map.get("video");
            if (video instanceof VideoDAO dao && dao.getStatus() != null && dao.getStatus() != 1) {
                return CustomResponse.error(404, "Video not found");
            }
        }
        return CustomResponse.ok(data);
    }

    @GetMapping("/user-works-count")
    public CustomResponse<Integer> userWorksCount(@RequestParam("uid") Integer uid) {
        return CustomResponse.ok(videoQueryService.getUserWorksCount(uid == null ? null : uid.longValue()));
    }

    @GetMapping("/user-works")
    public CustomResponse<List<Object>> userWorks(@RequestParam("uid") Integer uid,
                                                  @RequestParam("offset") Long offset) {
        return CustomResponse.ok(new java.util.ArrayList<>(videoQueryService.getUserWorks(uid == null ? null : uid.longValue(), offset)));
    }

    @GetMapping("/user-love")
    public CustomResponse<List<Object>> userLove(@RequestParam("uid") Integer uid,
                                                 @RequestParam("offset") Long offset) {
        return CustomResponse.ok(new java.util.ArrayList<>(videoQueryService.getUserLove(uid == null ? null : uid.longValue(), offset)));
    }

    @GetMapping("/user-play")
    public CustomResponse<List<Object>> userPlay(@RequestParam("uid") Integer uid,
                                                 @RequestParam("offset") Long offset) {
        return CustomResponse.ok(new java.util.ArrayList<>(videoQueryService.getUserPlay(uid == null ? null : uid.longValue(), offset)));
    }

    @GetMapping("/user-collect")
    public CustomResponse<List<Object>> userCollect(@RequestParam("uid") Integer uid,
                                                    @RequestParam("offset") Long offset) {
        return CustomResponse.ok(new java.util.ArrayList<>(videoQueryService.getUserCollect(uid == null ? null : uid.longValue(), offset)));
    }

    @PostMapping("/play/user")
    public CustomResponse<String> playUser(@RequestParam("vid") Integer vid) {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        userVideoService.updatePlay(uid, vid == null ? null : vid.longValue());
        return CustomResponse.ok("ok");
    }

    @PostMapping("/love-or-not")
    public CustomResponse<String> loveOrNot(@RequestParam("vid") Integer vid,
                                            @RequestParam(value = "is_like", required = false) Integer isLikeSnake,
                                            @RequestParam(value = "is_set", required = false) Integer isSetSnake,
                                            @RequestParam(value = "isLove", required = false) Boolean isLoveCamel,
                                            @RequestParam(value = "isSet", required = false) Boolean isSetCamel) {
        boolean love = isLikeSnake != null ? isLikeSnake == 1 : (isLoveCamel != null && isLoveCamel);
        boolean set = isSetSnake != null ? isSetSnake == 1 : (isSetCamel != null && isSetCamel);
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        userVideoService.setLoveOrUnlove(uid, vid == null ? null : vid.longValue(), love, set);
        return CustomResponse.ok("ok");
    }
}
