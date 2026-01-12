package com.mytube.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mytube.common.po.dao.VideoDAO;
import com.mytube.video.domain.Video;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VideoMapper extends BaseMapper<VideoDAO> {}

