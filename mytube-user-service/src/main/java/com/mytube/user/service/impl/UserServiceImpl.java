package com.mytube.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mytube.api.video.VideoStatsServiceApi;
import com.mytube.common.po.dao.VideoStatsDAO;
import com.mytube.common.redis.RedisUtil;
import com.mytube.common.storage.ObjectStorageClient;
import com.mytube.common.web.CustomResponse;
import com.mytube.user.domain.User;
import com.mytube.user.dto.UserDTO;
import com.mytube.user.mapper.UserMapper;
import com.mytube.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ObjectStorageClient objectStorageClient;

    @DubboReference
    private VideoStatsServiceApi videoStatsServiceApi;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    public UserDTO getUserById(Long id) {
        User user = redisUtil.getObject("user:" + id, User.class);
        if (user == null) {
            user = userMapper.selectById(id);
            if (user == null) {
                return null;
            }
            User finalUser = user;
            CompletableFuture.runAsync(() -> redisUtil.setExObjectValue("user:" + finalUser.getUid(), finalUser), taskExecutor);
        }
        UserDTO userDTO = toUserDTO(user);
        if (user.getState() == 2) {
            applyClosedAccount(userDTO);
            return userDTO;
        }
        applyVideoStats(userDTO, user.getUid());
        return userDTO;
    }

    @Override
    public List<UserDTO> getUserByIdList(List<Long> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("uid", list).ne("state", 2);
        List<User> users = userMapper.selectList(queryWrapper);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().parallel().flatMap(uid -> {
            User user = users.stream()
                    .filter(u -> Objects.equals(u.getUid(), uid))
                    .findFirst()
                    .orElse(null);
            if (user == null) {
                return Stream.empty();
            }
            UserDTO userDTO = toUserDTO(user);
            applyVideoStats(userDTO, user.getUid());
            return Stream.of(userDTO);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomResponse updateUserInfo(Long uid, String nickname, String desc, Integer gender) throws IOException {
        CustomResponse customResponse = new CustomResponse();
        if (nickname == null || nickname.trim().isEmpty()) {
            customResponse.setCode(500);
            customResponse.setMessage("Nickname is required");
            return customResponse;
        }
        if (desc == null) {
            desc = "";
        }
        if (nickname.length() > 24 || desc.length() > 100) {
            customResponse.setCode(500);
            customResponse.setMessage("Input too long");
            return customResponse;
        }
        if ("Account closed".equalsIgnoreCase(nickname.trim())) {
            customResponse.setCode(500);
            customResponse.setMessage("Nickname not allowed");
            return customResponse;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("nickname", nickname).ne("uid", uid);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            customResponse.setCode(500);
            customResponse.setMessage("Nickname already taken");
            return customResponse;
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid)
                .set("nickname", nickname)
                .set("description", desc)
                .set("gender", gender);
        userMapper.update(null, updateWrapper);
        redisUtil.delValue("user:" + uid);
        return customResponse;
    }

    @Override
    public CustomResponse updateUserAvatar(Long uid, MultipartFile file) throws IOException {
        String key = "avatar/" + uid + "/" + System.currentTimeMillis() + ".jpg";
        String avatarUrl = objectStorageClient.putObject(key, file);
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).set("avatar", avatarUrl);
        userMapper.update(null, updateWrapper);
        CompletableFuture.runAsync(() -> redisUtil.delValue("user:" + uid), taskExecutor);
        return new CustomResponse(200, "OK", avatarUrl);
    }

    @Override
    public CustomResponse updateUserAvatarUrl(Long uid, String avatarUrl) {
        if (uid == null || avatarUrl == null) {
            return CustomResponse.error(400, "Invalid");
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).set("avatar", avatarUrl);
        userMapper.update(null, updateWrapper);
        CompletableFuture.runAsync(() -> redisUtil.delValue("user:" + uid), taskExecutor);
        return new CustomResponse(200, "OK", avatarUrl);
    }

    private void applyClosedAccount(UserDTO userDTO) {
        userDTO.setNickname("Account closed");
        userDTO.setAvatar_url("https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png");
        userDTO.setBg_url("https://tinypic.host/images/2023/11/15/69PB2Q5W9D2U7L.png");
        userDTO.setGender(2);
        userDTO.setDescription("-");
        userDTO.setExp(0);
        userDTO.setCoin(0.0);
        userDTO.setVip(0);
        userDTO.setAuth(0);
        userDTO.setAuthMsg(null);
        userDTO.setVideoCount(0);
        userDTO.setFollowsCount(0);
        userDTO.setFansCount(0);
        userDTO.setLoveCount(0);
        userDTO.setPlayCount(0);
    }

    private void applyVideoStats(UserDTO userDTO, Long uid) {
        if (uid == null) {
            userDTO.setVideoCount(0);
            userDTO.setLoveCount(0);
            userDTO.setPlayCount(0);
            userDTO.setFollowsCount(0);
            userDTO.setFansCount(0);
            return;
        }
        userDTO.setFollowsCount(0);
        userDTO.setFansCount(0);
        List<Long> vids = getUserVideoIds(uid.longValue());
        if (vids.isEmpty()) {
            userDTO.setVideoCount(0);
            userDTO.setLoveCount(0);
            userDTO.setPlayCount(0);
            return;
        }
        int love = 0;
        int play = 0;
        for (Long vid : vids) {
            if (vid == null) {
                continue;
            }
            VideoStatsDAO stats;
            try {
                stats = videoStatsServiceApi.getVideoStatsById(vid);
            } catch (Exception e) {
                continue;
            }
            if (stats == null) {
                continue;
            }
            Integer good = stats.getGood();
            Integer playCount = stats.getPlay();
            if (good != null) {
                love += good;
            }
            if (playCount != null) {
                play += playCount;
            }
        }
        userDTO.setVideoCount(vids.size());
        userDTO.setLoveCount(love);
        userDTO.setPlayCount(play);
    }

    private List<Long> getUserVideoIds(Long uid) {
        if (uid == null) {
            return Collections.emptyList();
        }
        List<Long> results = new ArrayList<>();
        Set<Object> members = redisUtil.zReverange("user_video_upload:" + uid, 0L, -1L);
        if (members == null || members.isEmpty()) {
            return results;
        }
        for (Object member : members) {
            Long vid = parseLong(member);
            if (vid != null) {
                results.add(vid);
            }
        }
        return results;
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number num) {
            return num.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private UserDTO toUserDTO(User user) {
        return new UserDTO(
                user.getUid(),
                user.getNickname(),
                user.getAvatar(),
                user.getBackground(),
                user.getGender(),
                user.getDescription(),
                user.getExp(),
                user.getCoin(),
                user.getVip(),
                user.getState(),
                user.getAuth(),
                user.getAuthMsg(),
                0, 0, 0, 0, 0
        );
    }
}
