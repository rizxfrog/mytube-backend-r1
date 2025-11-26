package com.mytube.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mytube.favorite.domain.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {}

