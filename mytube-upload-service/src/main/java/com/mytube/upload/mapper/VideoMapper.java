package com.mytube.upload.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mytube.common.po.dao.VideoDAO;
import com.mytube.upload.domain.Video;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VideoMapper extends BaseMapper<VideoDAO> {}
