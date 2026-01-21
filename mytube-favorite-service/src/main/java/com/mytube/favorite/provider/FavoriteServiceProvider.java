package com.mytube.favorite.provider;

import com.mytube.api.favorite.FavoriteServiceApi;
import com.mytube.favorite.service.FavoriteService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class FavoriteServiceProvider implements FavoriteServiceApi {
    @Autowired
    private FavoriteService favoriteService;

    @Override
    public boolean addFavorite(Long uid, String title, String desc, Integer visible) {
        return favoriteService.addFavorite(uid, title, desc, visible) != null;
    }
}
