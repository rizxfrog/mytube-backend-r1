package com.mytube.comment.service.impl;

import com.mytube.comment.service.CommentService;
import com.mytube.comment.service.UserCommentService;
import com.mytube.common.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class UserCommentServiceImpl implements UserCommentService {
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Autowired
    private CommentService commentService;

    @Override
    public Map<String, Object> getUserLikeAndDislike(Integer uid) {
        Map<String, Object> map = new HashMap<>();
        CompletableFuture<Void> userLikeFuture = CompletableFuture.runAsync(() -> {
            Object userLike = redisUtil.getMembers("user_like_comment:" + uid);
            map.put("userLike", userLike == null ? Collections.emptySet() : userLike);
        }, taskExecutor);
        CompletableFuture<Void> userDislikeFuture = CompletableFuture.runAsync(() -> {
            Object userDislike = redisUtil.getMembers("user_dislike_comment:" + uid);
            map.put("userDislike", userDislike == null ? Collections.emptySet() : userDislike);
        }, taskExecutor);
        userDislikeFuture.join();
        userLikeFuture.join();
        return map;
    }

    @Override
    public void userSetLikeOrUnlike(Integer uid, Integer id, boolean isLike, boolean isSet) {
        Boolean likeExist = redisUtil.isMember("user_like_comment:" + uid, id);
        Boolean dislikeExist = redisUtil.isMember("user_dislike_comment:" + uid, id);

        if (isLike && isSet) {
            if (Boolean.TRUE.equals(likeExist)) {
                return;
            }
            if (Boolean.TRUE.equals(dislikeExist)) {
                CompletableFuture.runAsync(() -> redisUtil.delMember("user_dislike_comment:" + uid, id), taskExecutor);
                CompletableFuture.runAsync(() -> commentService.updateLikeAndDisLike(id, true), taskExecutor);
            } else {
                CompletableFuture.runAsync(() -> commentService.updateComment(id, "love", true, 1), taskExecutor);
            }
            redisUtil.addMember("user_like_comment:" + uid, id);
        } else if (isLike) {
            if (!Boolean.TRUE.equals(likeExist)) {
                return;
            }
            CompletableFuture.runAsync(() -> redisUtil.delMember("user_like_comment:" + uid, id), taskExecutor);
            CompletableFuture.runAsync(() -> commentService.updateComment(id, "love", false, 1), taskExecutor);
        } else if (isSet) {
            if (Boolean.TRUE.equals(dislikeExist)) {
                return;
            }
            if (Boolean.TRUE.equals(likeExist)) {
                CompletableFuture.runAsync(() -> redisUtil.delMember("user_like_comment:" + uid, id), taskExecutor);
                CompletableFuture.runAsync(() -> commentService.updateLikeAndDisLike(id, false), taskExecutor);
            } else {
                CompletableFuture.runAsync(() -> commentService.updateComment(id, "bad", true, 1), taskExecutor);
            }
            redisUtil.addMember("user_dislike_comment:" + uid, id);
        } else {
            if (!Boolean.TRUE.equals(dislikeExist)) {
                return;
            }
            CompletableFuture.runAsync(() -> redisUtil.delMember("user_dislike_comment:" + uid, id), taskExecutor);
            CompletableFuture.runAsync(() -> commentService.updateComment(id, "bad", false, 1), taskExecutor);
        }
    }
}
