package com.mytube.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mytube.api.user.UserServiceApi;
import com.mytube.comment.domain.Comment;
import com.mytube.comment.domain.CommentTree;
import com.mytube.comment.dto.UserDTO;
import com.mytube.comment.mapper.CommentMapper;
import com.mytube.comment.service.CommentService;
import com.mytube.common.redis.RedisUtil;
import com.mytube.common.web.CustomResponse;
import com.alibaba.fastjson2.JSON;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CommentMapper commentMapper;

    @DubboReference
    private UserServiceApi userServiceApi;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    public List<CommentTree> getCommentTreeByVid(Integer vid, Long offset, Integer type) {
        List<Comment> rootComments = getRootCommentsByVid(vid, offset, type);
        return rootComments.stream().parallel()
                .map(rootComment -> buildCommentTree(rootComment, 0L, 2L))
                .collect(Collectors.toList());
    }

    private CommentTree buildCommentTree(Comment comment, Long start, Long stop) {
        CommentTree tree = new CommentTree();
        tree.setId(comment.getId());
        tree.setVid(comment.getVid());
        tree.setRootId(comment.getRootId());
        tree.setParentId(comment.getParentId());
        tree.setContent(comment.getContent());
        tree.setCreateTime(comment.getCreateTime());
        tree.setLove(comment.getLove());
        tree.setBad(comment.getBad());

        tree.setUser(toUserDTO(userServiceApi.getUserInfo(comment.getUid() == null ? null : comment.getUid().longValue())));
        tree.setToUser(toUserDTO(userServiceApi.getUserInfo(comment.getToUserId() == null ? null : comment.getToUserId().longValue())));

        if (comment.getRootId() != null && comment.getRootId() == 0) {
            long count = redisUtil.zCard("comment_reply:" + comment.getId());
            tree.setCount(count);
            List<Comment> childComments = getChildCommentsByRootId(comment.getId(), comment.getVid(), start, stop);
            List<CommentTree> childTreeList = childComments.stream().parallel()
                    .map(childComment -> buildCommentTree(childComment, start, stop))
                    .collect(Collectors.toList());
            tree.setReplies(childTreeList);
        }
        return tree;
    }

    @Override
    @Transactional
    public CommentTree sendComment(Integer vid, Integer uid, Integer rootId, Integer parentId, Integer toUserId, String content) {
        if (uid == null || content == null || content.isEmpty() || content.length() > 2000) {
            return null;
        }
        Comment comment = new Comment(
                null,
                vid,
                uid,
                rootId,
                parentId,
                toUserId,
                content,
                0,
                0,
                new Date(),
                0,
                0
        );
        commentMapper.insert(comment);
        CommentTree commentTree = buildCommentTree(comment, 0L, -1L);

        CompletableFuture.runAsync(() -> {
            if (!Objects.equals(rootId, 0)) {
                redisUtil.zset("comment_reply:" + rootId, comment.getId());
            } else {
                redisUtil.zset("comment_video:" + vid, comment.getId());
            }
        }, taskExecutor);

        return commentTree;
    }

    @Override
    @Transactional
    public CustomResponse deleteComment(Integer id, Integer uid, boolean isAdmin) {
        CustomResponse customResponse = new CustomResponse();
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).ne("is_deleted", 1);
        Comment comment = commentMapper.selectOne(queryWrapper);
        if (comment == null) {
            customResponse.setCode(404);
            customResponse.setMessage("Comment not found");
            return customResponse;
        }
        if (Objects.equals(comment.getUid(), uid) || isAdmin) {
            UpdateWrapper<Comment> commentWrapper = new UpdateWrapper<>();
            commentWrapper.eq("id", comment.getId()).set("is_deleted", 1);
            commentMapper.update(null, commentWrapper);
            if (Objects.equals(comment.getRootId(), 0)) {
                redisUtil.zsetDelMember("comment_video:" + comment.getVid(), comment.getId());
                redisUtil.delValue("comment_reply:" + comment.getId());
            } else {
                redisUtil.zsetDelMember("comment_reply:" + comment.getRootId(), comment.getId());
            }
            customResponse.setCode(200);
            customResponse.setMessage("Deleted");
        } else {
            customResponse.setCode(403);
            customResponse.setMessage("No permission");
        }
        return customResponse;
    }

    @Override
    public List<Comment> getChildCommentsByRootId(Integer rootId, Integer vid, Long start, Long stop) {
        Set<Object> replyIds = redisUtil.zRange("comment_reply:" + rootId, start, stop);
        if (replyIds == null || replyIds.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.in("id", replyIds).ne("is_deleted", 1);
        return commentMapper.selectList(wrapper);
    }

    @Override
    public List<Comment> getRootCommentsByVid(Integer vid, Long offset, Integer type) {
        Set<Object> rootIdsSet;
        if (type != null && type == 1) {
            rootIdsSet = redisUtil.zReverange("comment_video:" + vid, 0L, -1L);
        } else {
            rootIdsSet = redisUtil.zReverange("comment_video:" + vid, offset, offset + 9L);
        }
        if (rootIdsSet == null || rootIdsSet.isEmpty()) {
            QueryWrapper<Comment> wrapper = new QueryWrapper<>();
            wrapper.eq("vid", vid).eq("root_id", 0).ne("is_deleted", 1).orderByDesc("create_time");
            wrapper.last("LIMIT 10 OFFSET " + offset);
            return commentMapper.selectList(wrapper);
        }
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.in("id", rootIdsSet).ne("is_deleted", 1);
        if (type != null && type == 1) {
            wrapper.orderByDesc("(love - bad)").last("LIMIT 10 OFFSET " + offset);
        } else {
            wrapper.orderByDesc("create_time");
        }
        return commentMapper.selectList(wrapper);
    }

    @Override
    public CommentTree getMoreCommentsById(Integer id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return null;
        }
        return buildCommentTree(comment, 0L, -1L);
    }

    @Override
    public void updateLikeAndDisLike(Integer id, boolean addLike) {
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        if (addLike) {
            updateWrapper.setSql("love = love + 1, bad = CASE WHEN bad - 1 < 0 THEN 0 ELSE bad - 1 END");
        } else {
            updateWrapper.setSql("bad = bad + 1, love = CASE WHEN love - 1 < 0 THEN 0 ELSE love - 1 END");
        }
        updateWrapper.eq("id", id);
        commentMapper.update(null, updateWrapper);
    }

    @Override
    public void updateComment(Integer id, String column, boolean increase, Integer count) {
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        if (increase) {
            updateWrapper.setSql(column + " = " + column + " + " + count);
        } else {
            updateWrapper.setSql(column + " = CASE WHEN " + column + " - " + count + " < 0 THEN 0 ELSE " + column + " - " + count + " END");
        }
        commentMapper.update(null, updateWrapper);
    }

    private UserDTO toUserDTO(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return JSON.parseObject(JSON.toJSONString(obj), UserDTO.class);
        } catch (Exception e) {
            return null;
        }
    }
}
