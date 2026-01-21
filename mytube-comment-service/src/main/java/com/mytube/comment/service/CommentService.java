package com.mytube.comment.service;

import com.mytube.comment.domain.Comment;
import com.mytube.comment.domain.CommentTree;
import com.mytube.common.web.CustomResponse;

import java.util.List;

public interface CommentService {
    List<CommentTree> getCommentTreeByVid(Integer vid, Long offset, Integer type);

    CommentTree sendComment(Integer vid, Integer uid, Integer rootId, Integer parentId, Integer toUserId, String content);

    CustomResponse deleteComment(Integer id, Integer uid, boolean isAdmin);

    List<Comment> getChildCommentsByRootId(Integer rootId, Integer vid, Long start, Long stop);

    List<Comment> getRootCommentsByVid(Integer vid, Long offset, Integer type);

    CommentTree getMoreCommentsById(Integer id);

    void updateLikeAndDisLike(Integer id, boolean addLike);

    void updateComment(Integer id, String column, boolean increase, Integer count);
}
