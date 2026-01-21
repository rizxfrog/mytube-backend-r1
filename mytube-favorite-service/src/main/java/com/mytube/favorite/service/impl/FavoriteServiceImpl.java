package com.mytube.favorite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mytube.common.redis.RedisUtil;
import com.mytube.favorite.domain.Favorite;
import com.mytube.favorite.mapper.FavoriteMapper;
import com.mytube.favorite.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class FavoriteServiceImpl implements FavoriteService {
    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    public List<Favorite> getFavorites(Integer uid, boolean isOwner) {
        String key = "favorites:" + uid;
        List<Favorite> list = redisUtil.getObject(key, List.class);
        if (list != null) {
            return list;
        }
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).ne("is_delete", 1).orderByDesc("fid");
        list = favoriteMapper.selectList(queryWrapper);
        if (list == null) {
            return Collections.emptyList();
        }
        List<Favorite> finalList = list;
        CompletableFuture.runAsync(() -> redisUtil.setExObjectValue(key, finalList), taskExecutor);
        return list;
    }

    @Override
    public Favorite addFavorite(Long uid, String title, String desc, Integer visible) {
        Favorite favorite = new Favorite(null, uid, 2, visible, null, title, desc, 0, null);
        favoriteMapper.insert(favorite);
        redisUtil.delValue("favorites:" + uid);
        return favorite;
    }

    @Override
    public Favorite updateFavorite(Integer fid, Integer uid, String title, String desc, Integer visible) {
        Favorite favorite = favoriteMapper.selectById(fid);
        if (favorite == null || !favorite.getUid().equals(uid)) {
            return null;
        }
        UpdateWrapper<Favorite> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("fid", fid).set("title", title).set("description", desc).set("visible", visible);
        favoriteMapper.update(null, updateWrapper);
        redisUtil.delValue("favorites:" + uid);
        return favorite;
    }

    @Override
    public void delFavorite(Integer fid, Integer uid) {
        UpdateWrapper<Favorite> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("fid", fid).eq("uid", uid).set("is_delete", 1);
        favoriteMapper.update(null, updateWrapper);
        redisUtil.delValue("favorites:" + uid);
    }
}
