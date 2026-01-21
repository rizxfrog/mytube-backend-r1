package com.mytube.upload.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mytube.common.po.dao.VideoStatsDAO;
import com.mytube.upload.domain.VideoStats;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VideoStatsMapper extends BaseMapper<VideoStatsDAO> {}
