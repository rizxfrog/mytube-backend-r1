package com.mytube.stats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mytube.stats.domain.Stats;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StatsMapper extends BaseMapper<Stats> {}

