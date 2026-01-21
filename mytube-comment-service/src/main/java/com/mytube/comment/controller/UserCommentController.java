package com.mytube.comment.controller;

import com.mytube.comment.service.UserCommentService;
import com.mytube.common.security.CurrentUser;
import com.mytube.common.web.CustomResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserCommentController {
    @Autowired
    private UserCommentService userCommentService;
    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/comment/get-like-and-dislike")
    public CustomResponse getLikeAndDislike() {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        CustomResponse response = new CustomResponse();
        response.setData(userCommentService.getUserLikeAndDislike(uid.intValue()));
        return response;
    }

    @PostMapping("/comment/love-or-not")
    public CustomResponse setLoveOrNot(@RequestParam("id") Integer id,
                                       @RequestParam(value = "is_like", required = false) Boolean isLikeSnake,
                                       @RequestParam(value = "is_set", required = false) Boolean isSetSnake,
                                       @RequestParam(value = "isLike", required = false) Boolean isLikeCamel,
                                       @RequestParam(value = "isSet", required = false) Boolean isSetCamel) {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        Boolean isLike = isLikeSnake != null ? isLikeSnake : isLikeCamel;
        Boolean isSet = isSetSnake != null ? isSetSnake : isSetCamel;
        if (isLike == null || isSet == null) {
            return CustomResponse.error(400, "Missing parameters");
        }
        userCommentService.userSetLikeOrUnlike(uid.intValue(), id, isLike, isSet);
        return new CustomResponse();
    }

    @GetMapping("/comment/get-up-like")
    public CustomResponse getUpLike(@RequestParam(value = "uid", required = false) Integer uidParam,
                                    @RequestParam(value = "id", required = false) Integer idParam) {
        Integer uid = uidParam != null ? uidParam : idParam;
        if (uid == null) {
            return CustomResponse.error(400, "Missing uid");
        }
        CustomResponse response = new CustomResponse();
        response.setData(userCommentService.getUserLikeAndDislike(uid).get("userLike"));
        return response;
    }
}
