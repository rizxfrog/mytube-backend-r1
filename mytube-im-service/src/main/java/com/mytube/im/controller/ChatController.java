package com.mytube.im.controller;

import com.mytube.common.redis.RedisUtil;
import com.mytube.common.security.CurrentUser;
import com.mytube.common.web.CustomResponse;
import com.mytube.im.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("/msg/chat/create/{uid}")
    public CustomResponse<Object> create(@PathVariable("uid") Integer uid) {
        Long loginUid = currentUser.requireUserId();
        if (loginUid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        Map<String, Object> result = chatService.createChat(uid, loginUid.intValue());
        CustomResponse<Object> response = new CustomResponse<>();
        Object msg = result.get("msg");
        if (Objects.equals(msg, "unknown")) {
            response.setCode(404);
            response.setMessage("User not found");
            return response;
        }
        response.setMessage(msg == null ? "ok" : msg.toString());
        response.setData(result);
        return response;
    }

    @GetMapping("/msg/chat/recent-list")
    public CustomResponse<Object> recentList(@RequestParam("offset") Long offset) {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", chatService.getChatListWithData(uid.intValue(), offset));
        map.put("more", offset + 10 < redisUtil.zCard("chat_zset:" + uid));
        return CustomResponse.ok(map);
    }

    @GetMapping("/msg/chat/delete/{uid}")
    public CustomResponse<String> delete(@PathVariable("uid") Integer uid) {
        Long loginUid = currentUser.requireUserId();
        if (loginUid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        chatService.delChat(uid, loginUid.intValue());
        return CustomResponse.ok("ok");
    }

    @GetMapping("/msg/chat/online")
    public void online(@RequestParam("from") Integer from) {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return;
        }
        chatService.updateWhisperOnline(from, uid.intValue());
    }

    @GetMapping("/msg/chat/outline")
    public void outline(@RequestParam("from") Integer from, @RequestParam("to") Integer to) {
        chatService.updateWhisperOutline(from, to);
    }
}
