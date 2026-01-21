package com.mytube.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mytube.user.domain.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}

