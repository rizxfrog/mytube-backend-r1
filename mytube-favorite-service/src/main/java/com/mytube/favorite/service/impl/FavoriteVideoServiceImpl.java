package com.mytube.favorite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mytube.favorite.domain.FavoriteVideo;
import com.mytube.favorite.mapper.FavoriteVideoMapper;
import com.mytube.favorite.service.FavoriteVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteVideoServiceImpl implements FavoriteVideoService {
    @Autowired
    private FavoriteVideoMapper favoriteVideoMapper;

    @Override
    public List<Integer> getCollectedFids(Integer uid, Integer vid) {
        QueryWrapper<FavoriteVideo> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).eq("vid", vid);
        List<FavoriteVideo> list = favoriteVideoMapper.selectList(wrapper);
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(FavoriteVideo::getFid).collect(Collectors.toList());
    }

    @Override
    public String collectVideo(Integer uid, Integer vid, Integer fid) {
        FavoriteVideo fv = new FavoriteVideo(null, fid, vid, uid, null);
        favoriteVideoMapper.insert(fv);
        return "ok";
    }

    @Override
    public String cancelCollect(Integer uid, Integer vid, Integer fid) {
        QueryWrapper<FavoriteVideo> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).eq("vid", vid).eq("fid", fid);
        favoriteVideoMapper.delete(wrapper);
        return "ok";
    }

    @Override
    public Set<Integer> findFidsOfCollected(Integer vid, Set<Integer> fids) {
        if (fids == null || fids.isEmpty()) {
            return Collections.emptySet();
        }
        QueryWrapper<FavoriteVideo> wrapper = new QueryWrapper<>();
        wrapper.eq("vid", vid).in("fid", fids);
        List<FavoriteVideo> list = favoriteVideoMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        return list.stream().map(FavoriteVideo::getFid).collect(Collectors.toSet());
    }

    @Override
    public void addToFav(Integer uid, Integer vid, Set<Integer> fids) {
        if (fids == null || fids.isEmpty()) {
            return;
        }
        for (Integer fid : fids) {
            if (fid == null) {
                continue;
            }
            FavoriteVideo fv = new FavoriteVideo(null, fid, vid, uid, null);
            favoriteVideoMapper.insert(fv);
        }
    }

    @Override
    public void removeFromFav(Integer uid, Integer vid, Set<Integer> fids) {
        if (fids == null || fids.isEmpty()) {
            return;
        }
        QueryWrapper<FavoriteVideo> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid).eq("vid", vid).in("fid", fids);
        favoriteVideoMapper.delete(wrapper);
    }
}
