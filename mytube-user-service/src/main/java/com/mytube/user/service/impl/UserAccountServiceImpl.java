package com.mytube.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mytube.api.favorite.FavoriteServiceApi;
import com.mytube.common.redis.RedisUtil;
import com.mytube.common.security.CurrentUser;
import com.mytube.common.security.JwtUtil;
import com.mytube.common.web.CustomResponse;
import com.mytube.user.domain.MsgUnread;
import com.mytube.user.domain.User;
import com.mytube.user.dto.UserDTO;
import com.mytube.user.mapper.MsgUnreadMapper;
import com.mytube.user.mapper.UserMapper;
import com.mytube.user.service.UserAccountService;
import com.mytube.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserAccountServiceImpl implements UserAccountService {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MsgUnreadMapper msgUnreadMapper;

    @DubboReference
    private FavoriteServiceApi favoriteServiceApi;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    @Transactional
    public CustomResponse<Void>  register(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).ne("state", 2);
        User existing = userMapper.selectOne(queryWrapper);
        if (existing != null) {
            return CustomResponse.error(403, "Username already exists");
        }
        QueryWrapper<User> lastWrapper = new QueryWrapper<>();
        lastWrapper.orderByDesc("uid").last("limit 1");
//        User lastUser = userMapper.selectOne(lastWrapper);
//        int newUserUid = lastUser == null ? 1 : lastUser.getUid() + 1;
        String encodedPassword = passwordEncoder.encode(password);
        String avatarUrl = "https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png";
        String bgUrl = "https://tinypic.host/images/2023/11/15/69PB2Q5W9D2U7L.png";
        Date now = new Date();
        User newUser = new User(
                null,
                username.trim(),
                encodedPassword,
                null,
                avatarUrl,
                bgUrl,
                2,
                "",
                0,
                0.0,
                0,
                0,
                0,
                0,
                null,
                now,
                null
        );
        userMapper.insert(newUser);
        msgUnreadMapper.insert(new MsgUnread(newUser.getUid(), 0, 0, 0, 0, 0, 0));
        boolean favoriteCreated = favoriteServiceApi.addFavorite(newUser.getUid(), "Default", "", 1);
        if (!favoriteCreated) {
            throw new IllegalStateException("Create default favorite failed");
        }
        return CustomResponse.ok();
    }

    @Override
    public CustomResponse login(String username, String password) {
        CustomResponse customResponse = new CustomResponse();
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticate;
        try {
            authenticate = authenticationProvider.authenticate(authenticationToken);
        } catch (Exception e) {
            customResponse.setCode(403);
            customResponse.setMessage("Invalid credentials");
            return customResponse;
        }

        UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
        User user = loginUser.getUser();
        redisUtil.setExObjectValue("user:" + user.getUid(), user);
        if (user.getState() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("Account locked");
            return customResponse;
        }

        String token = jwtUtil.createToken(user.getUid().toString(), "user");
        redisUtil.setExObjectValue("security:user:" + user.getUid(), user, 60L * 60 * 24 * 2, TimeUnit.SECONDS);

        UserDTO userDTO = new UserDTO();
        userDTO.setUid(user.getUid());
        userDTO.setNickname(user.getNickname());
        userDTO.setAvatar_url(user.getAvatar());
        userDTO.setBg_url(user.getBackground());
        userDTO.setGender(user.getGender());
        userDTO.setDescription(user.getDescription());
        userDTO.setExp(user.getExp());
        userDTO.setCoin(user.getCoin());
        userDTO.setVip(user.getVip());
        userDTO.setState(user.getState());
        userDTO.setAuth(user.getAuth());
        userDTO.setAuthMsg(user.getAuthMsg());

        Map<String, Object> finalMap = new HashMap<>();
        finalMap.put("token", token);
        finalMap.put("user", userDTO);
        customResponse.setMessage("Login success");
        customResponse.setData(finalMap);
        return customResponse;
    }

    @Override
    public CustomResponse adminLogin(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticate = authenticationProvider.authenticate(authenticationToken);
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
        User user = loginUser.getUser();
        CustomResponse customResponse = new CustomResponse();
        if (user.getRole() == 0) {
            customResponse.setCode(403);
            customResponse.setMessage("Not an admin");
            return customResponse;
        }
        redisUtil.setExObjectValue("user:" + user.getUid(), user);
        if (user.getState() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("Account locked");
            return customResponse;
        }
        String token = jwtUtil.createToken(user.getUid().toString(), "admin");
        redisUtil.setExObjectValue("security:admin:" + user.getUid(), user, 60L * 60 * 24 * 2, TimeUnit.SECONDS);

        UserDTO userDTO = new UserDTO();
        userDTO.setUid(user.getUid());
        userDTO.setNickname(user.getNickname());
        userDTO.setAvatar_url(user.getAvatar());
        userDTO.setBg_url(user.getBackground());
        userDTO.setGender(user.getGender());
        userDTO.setDescription(user.getDescription());
        userDTO.setExp(user.getExp());
        userDTO.setCoin(user.getCoin());
        userDTO.setVip(user.getVip());
        userDTO.setState(user.getState());
        userDTO.setAuth(user.getAuth());
        userDTO.setAuthMsg(user.getAuthMsg());

        Map<String, Object> finalMap = new HashMap<>();
        finalMap.put("token", token);
        finalMap.put("user", userDTO);
        customResponse.setMessage("Login success");
        customResponse.setData(finalMap);
        return customResponse;
    }

    @Override
    public CustomResponse personalInfo() {
        Long loginUserId = currentUser.requireUserId();
        if (loginUserId == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        UserDTO userDTO = userService.getUserById(loginUserId);
        CustomResponse customResponse = new CustomResponse();
        if (userDTO == null) {
            customResponse.setCode(404);
            customResponse.setMessage("User not found");
            return customResponse;
        }
        if (userDTO.getState() == 2) {
            customResponse.setCode(404);
            customResponse.setMessage("Account closed");
            return customResponse;
        }
        if (userDTO.getState() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("Account locked");
            return customResponse;
        }
        customResponse.setData(userDTO);
        return customResponse;
    }

    @Override
    public CustomResponse adminPersonalInfo() {
        Long loginUserId = currentUser.requireUserId();
        User user = redisUtil.getObject("user:" + loginUserId, User.class);
        if (user == null && loginUserId != null) {
            user = userMapper.selectById(loginUserId);
            User finalUser = user;
            if (finalUser != null) {
                CompletableFuture.runAsync(() -> redisUtil.setExObjectValue("user:" + finalUser.getUid(), finalUser), taskExecutor);
            }
        }
        CustomResponse customResponse = new CustomResponse();
        if (user == null) {
            customResponse.setCode(404);
            customResponse.setMessage("User not found");
            return customResponse;
        }
        if (user.getRole() == 0) {
            customResponse.setCode(403);
            customResponse.setMessage("Not an admin");
            return customResponse;
        }
        if (user.getState() == 2) {
            customResponse.setCode(404);
            customResponse.setMessage("Account closed");
            return customResponse;
        }
        if (user.getState() == 1) {
            customResponse.setCode(403);
            customResponse.setMessage("Account locked");
            return customResponse;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUid(user.getUid());
        userDTO.setNickname(user.getNickname());
        userDTO.setAvatar_url(user.getAvatar());
        userDTO.setBg_url(user.getBackground());
        userDTO.setGender(user.getGender());
        userDTO.setDescription(user.getDescription());
        userDTO.setExp(user.getExp());
        userDTO.setCoin(user.getCoin());
        userDTO.setVip(user.getVip());
        userDTO.setState(user.getState());
        userDTO.setAuth(user.getAuth());
        userDTO.setAuthMsg(user.getAuthMsg());
        customResponse.setData(userDTO);
        return customResponse;
    }

    @Override
    public void logout() {
        Long loginUserId = currentUser.requireUserId();
        if (loginUserId == null) {
            return;
        }
        redisUtil.delValue("token:user:" + loginUserId);
        redisUtil.delValue("security:user:" + loginUserId);
        redisUtil.delMember("login_member", loginUserId);
        redisUtil.deleteKeysWithPrefix("whisper:" + loginUserId + ":");
    }

    @Override
    public void adminLogout() {
        Long loginUserId = currentUser.requireUserId();
        if (loginUserId == null) {
            return;
        }
        redisUtil.delValue("token:admin:" + loginUserId);
        redisUtil.delValue("security:admin:" + loginUserId);
    }

    @Override
    public CustomResponse updatePassword(String pw, String npw) {
        CustomResponse customResponse = new CustomResponse();
        if (npw == null || npw.isEmpty()) {
            customResponse.setCode(500);
            customResponse.setMessage("Password is required");
            return customResponse;
        }
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            customResponse.setCode(401);
            customResponse.setMessage("Not logged in");
            return customResponse;
        }
        User user = userMapper.selectById(uid);
        if (user == null) {
            customResponse.setCode(404);
            customResponse.setMessage("User not found");
            return customResponse;
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getUsername(), pw);
        try {
            authenticationProvider.authenticate(authenticationToken);
        } catch (Exception e) {
            customResponse.setCode(403);
            customResponse.setMessage("Invalid password");
            return customResponse;
        }
        if (Objects.equals(pw, npw)) {
            customResponse.setCode(500);
            customResponse.setMessage("New password must differ");
            return customResponse;
        }
        String encodedPassword = passwordEncoder.encode(npw);
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", user.getUid()).set("password", encodedPassword);
        userMapper.update(null, updateWrapper);
        logout();
        adminLogout();
        return customResponse;
    }
}
