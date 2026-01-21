package com.mytube.favorite.service;

import com.mytube.favorite.domain.Favorite;

import java.util.List;

public interface FavoriteService {
    List<Favorite> getFavorites(Integer uid, boolean isOwner);

    Favorite addFavorite(Long uid, String title, String desc, Integer visible);

    Favorite updateFavorite(Integer fid, Integer uid, String title, String desc, Integer visible);

    void delFavorite(Integer fid, Integer uid);
}
