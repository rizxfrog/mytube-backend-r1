package com.mytube.user.controller;

import com.mytube.common.security.CurrentUser;
import com.mytube.common.web.CustomResponse;
import com.mytube.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private CurrentUser currentUser;

    @PostMapping("/user/info/update")
    public CustomResponse updateUserInfo(@RequestParam("nickname") String nickname,
                                         @RequestParam("description") String desc,
                                         @RequestParam("gender") Integer gender) {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        try {
            return userService.updateUserInfo(uid, nickname, desc, gender);
        } catch (Exception e) {
            CustomResponse customResponse = new CustomResponse();
            customResponse.setCode(500);
            customResponse.setMessage("Update failed");
            return customResponse;
        }
    }

    @PostMapping("/user/avatar/update")
    public CustomResponse updateUserAvatar(@RequestParam("file") MultipartFile file) {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        try {
            return userService.updateUserAvatar(uid, file);
        } catch (Exception e) {
            return new CustomResponse(500, "Avatar update failed", null);
        }
    }

    @GetMapping("/user/info/get-one")
    public CustomResponse getOneUserInfo(@RequestParam("uid") Long uid) {
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(userService.getUserById(uid));
        return customResponse;
    }
}
