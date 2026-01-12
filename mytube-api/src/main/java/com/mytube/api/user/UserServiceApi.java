package com.mytube.api.user;

public interface UserServiceApi {
    String register(String username, String password);
    String login(String username, String password);
    String adminLogin(String username, String password);
    String logout(Long uid);
    Object getPersonalInfo(Long uid);
    Object getUserInfo(Long uid);
    String updateProfile(Long uid, String nickname, String description, Integer gender);
    String updateAvatar(Long uid, byte[] fileBytes, String filename);
    String updatePassword(Long uid, String pw, String npw);
}

