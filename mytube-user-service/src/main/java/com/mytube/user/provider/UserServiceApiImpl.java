package com.mytube.user.provider;

import com.mytube.api.user.UserServiceApi;
import com.mytube.common.web.CustomResponse;
import com.mytube.user.service.UserAccountService;
import com.mytube.user.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Map;

@DubboService
public class UserServiceApiImpl implements UserServiceApi {
    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserService userService;
    @Autowired
    private S3Client s3Client;
    @Value("${s3.bucket}")
    private String bucket;

    @Override
    public String register(String username, String password) {
        return userAccountService.register(username, password).getMessage();
    }

    @Override
    public String login(String username, String password) {
        CustomResponse resp = userAccountService.login(username, password);
        Object data = resp.getData();
        if (data instanceof Map<?, ?> map) {
            Object token = map.get("token");
            return token == null ? "" : token.toString();
        }
        return "";
    }

    @Override
    public String adminLogin(String username, String password) {
        CustomResponse resp = userAccountService.adminLogin(username, password);
        Object data = resp.getData();
        if (data instanceof Map<?, ?> map) {
            Object token = map.get("token");
            return token == null ? "" : token.toString();
        }
        return "";
    }

    @Override
    public String logout(Long uid) {
        userAccountService.logout();
        return "ok";
    }

    @Override
    public Object getPersonalInfo(Long uid) {
        return userAccountService.personalInfo().getData();
    }

    @Override
    public Object getUserInfo(Long uid) {
        return userService.getUserById(uid);
    }

    @Override
    public Object getUserInfoList(java.util.List<Long> uids) {
        if (uids == null || uids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return userService.getUserByIdList(uids);
    }

    @Override
    public String updateProfile(Long uid, String nickname, String description, Integer gender) {
        try {
            return userService.updateUserInfo(uid, nickname, description, gender).getMessage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String updateAvatar(Long uid, byte[] fileBytes, String filename) {
        if (uid == null || fileBytes == null) {
            return "invalid";
        }
        String key = "avatar/" + uid + "/" + (filename == null ? System.currentTimeMillis() + ".jpg" : filename);
        s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), RequestBody.fromBytes(fileBytes));
        userService.updateUserAvatarUrl(uid, key);
        return key;
    }

    @Override
    public String updatePassword(Long uid, String pw, String npw) {
        return userAccountService.updatePassword(pw, npw).getMessage();
    }
}
