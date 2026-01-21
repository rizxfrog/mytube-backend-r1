package com.mytube.im.controller;

import com.mytube.common.security.CurrentUser;
import com.mytube.common.web.CustomResponse;
import com.mytube.im.service.ChatDetailedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatDetailedController {
    @Autowired
    private ChatDetailedService chatDetailedService;
    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/msg/chat-detailed/get-more")
    public CustomResponse<Object> getMore(@RequestParam(value = "uid", required = false) Integer uid,
                                          @RequestParam(value = "cid", required = false) Integer cid,
                                          @RequestParam("offset") Long offset) {
        Long loginUid = currentUser.requireUserId();
        if (loginUid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        Integer targetUid = uid != null ? uid : cid;
        if (targetUid == null) {
            return CustomResponse.error(400, "Missing uid");
        }
        return CustomResponse.ok(chatDetailedService.getDetails(targetUid, loginUid.intValue(), offset));
    }

    @PostMapping("/msg/chat-detailed/delete")
    public CustomResponse<String> delete(@RequestParam("id") Integer id) {
        Long loginUid = currentUser.requireUserId();
        if (loginUid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        if (!chatDetailedService.deleteDetail(id, loginUid.intValue())) {
            return CustomResponse.error(500, "Delete failed");
        }
        return CustomResponse.ok("ok");
    }
}
