package com.mytube.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mytube.comment.domain.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {}

