package com.mytube.im.service;

import com.mytube.im.domain.Chat;

import java.util.List;
import java.util.Map;

public interface ChatService {
    Map<String, Object> createChat(Integer from, Integer to);
    List<Map<String, Object>> getChatListWithData(Integer uid, Long offset);
    Chat getChat(Integer from, Integer to);
    void delChat(Integer from, Integer to);
    boolean updateChat(Integer from, Integer to);
    void updateWhisperOnline(Integer from, Integer to);
    void updateWhisperOutline(Integer from, Integer to);
}
