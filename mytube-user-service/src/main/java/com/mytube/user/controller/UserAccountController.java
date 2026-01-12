package com.mytube.user.controller;

import com.mytube.common.web.CustomResponse;
import com.mytube.user.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserAccountController {
    @Autowired
    private UserAccountService userAccountService;

    @PostMapping("/user/account/register")
    public CustomResponse register(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        String confirmedPassword = map.get("confirmedPassword");
        return userAccountService.register(username, password, confirmedPassword);
    }

    @PostMapping("/user/account/login")
    public CustomResponse login(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        return userAccountService.login(username, password);
    }

    @PostMapping("/admin/account/login")
    public CustomResponse adminLogin(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        return userAccountService.adminLogin(username, password);
    }

    @GetMapping("/user/personal/info")
    public CustomResponse personalInfo() {
        return userAccountService.personalInfo();
    }

    @GetMapping("/admin/personal/info")
    public CustomResponse adminPersonalInfo() {
        return userAccountService.adminPersonalInfo();
    }

    @GetMapping("/user/account/logout")
    public void logout() {
        userAccountService.logout();
    }

    @GetMapping("/admin/account/logout")
    public void adminLogout() {
        userAccountService.adminLogout();
    }

    @PostMapping("/user/password/update")
    public CustomResponse updatePassword(@RequestParam("pw") String pw, @RequestParam("npw") String npw) {
        return userAccountService.updatePassword(pw, npw);
    }
}
