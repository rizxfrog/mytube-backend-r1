package com.mytube.comment.controller;

import com.mytube.comment.domain.CommentTree;
import com.mytube.comment.service.CommentService;
import com.mytube.common.security.CurrentUser;
import com.mytube.common.web.CustomResponse;
import com.mytube.common.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("/comment/get")
    public CustomResponse getCommentTreeByVid(@RequestParam("vid") Integer vid,
                                              @RequestParam("offset") Long offset,
                                              @RequestParam("type") Integer type) {
        CustomResponse customResponse = new CustomResponse();
        long count = redisUtil.zCard("comment_video:" + vid);
        Map<String, Object> map = new HashMap<>();
        if (offset >= count) {
            map.put("more", false);
            map.put("comments", Collections.emptyList());
        } else if (offset + 10 >= count) {
            map.put("more", false);
            map.put("comments", commentService.getCommentTreeByVid(vid, offset, type));
        } else {
            map.put("more", true);
            map.put("comments", commentService.getCommentTreeByVid(vid, offset, type));
        }
        customResponse.setData(map);
        return customResponse;
    }

    @GetMapping("/comment/reply/get-more")
    public CommentTree getMoreCommentById(@RequestParam("id") Integer id) {
        return commentService.getMoreCommentsById(id);
    }

    @PostMapping("/comment/add")
    public CustomResponse addComment(@RequestParam("vid") Integer vid,
                                     @RequestParam("root_id") Integer rootId,
                                     @RequestParam("parent_id") Integer parentId,
                                     @RequestParam("to_user_id") Integer toUserId,
                                     @RequestParam("content") String content) {
        Long uid = currentUser.requireUserId();
        CustomResponse customResponse = new CustomResponse();
        if (uid == null) {
            customResponse.setCode(401);
            customResponse.setMessage("Not logged in");
            return customResponse;
        }
        CommentTree commentTree = commentService.sendComment(vid, uid.intValue(), rootId, parentId, toUserId, content);
        if (commentTree == null) {
            customResponse.setCode(500);
            customResponse.setMessage("Send failed");
        }
        customResponse.setData(commentTree);
        return customResponse;
    }

    @PostMapping("/comment/delete")
    public CustomResponse delComment(@RequestParam("id") Integer id) {
        Long loginUid = currentUser.requireUserId();
        if (loginUid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        return commentService.deleteComment(id, loginUid.intValue(), currentUser.isAdmin());
    }
}
