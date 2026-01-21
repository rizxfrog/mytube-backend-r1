package com.mytube.im.controller;

import com.mytube.common.security.CurrentUser;
import com.mytube.common.web.CustomResponse;
import com.mytube.im.service.MsgUnreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MsgUnreadController {
    @Autowired
    private MsgUnreadService msgUnreadService;
    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/msg-unread/all")
    public CustomResponse<Object> all() {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        return CustomResponse.ok(msgUnreadService.getUnread(uid.intValue()));
    }

    @PostMapping("/msg-unread/clear")
    public CustomResponse<String> clear(@RequestParam(value = "column", required = false) String column,
                                        @RequestParam(value = "type", required = false) String type) {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        String resolved = column != null ? column : type;
        if (resolved == null) {
            return CustomResponse.error(400, "Missing column");
        }
        msgUnreadService.clearUnread(uid.intValue(), resolved);
        return CustomResponse.ok("ok");
    }
}
