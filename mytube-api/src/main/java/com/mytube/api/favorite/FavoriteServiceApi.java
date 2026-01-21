package com.mytube.api.favorite;

public interface FavoriteServiceApi {
    boolean addFavorite(Long uid, String title, String desc, Integer visible);
}
