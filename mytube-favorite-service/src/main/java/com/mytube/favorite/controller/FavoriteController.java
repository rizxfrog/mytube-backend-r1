package com.mytube.favorite.controller;

import com.mytube.common.security.CurrentUser;
import com.mytube.common.web.CustomResponse;
import com.mytube.favorite.domain.Favorite;
import com.mytube.favorite.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/favorite/get-all/user")
    public CustomResponse<List<Favorite>> getAllUserFavorites(@RequestParam(value = "uid", required = false) Integer uid) {
        Long loginUid = currentUser.requireUserId();
        if (uid == null) {
            if (loginUid == null) {
                return CustomResponse.error(401, "Not logged in");
            }
            uid = loginUid.intValue();
        }
        boolean isOwner = loginUid != null && loginUid.intValue() == uid;
        return CustomResponse.ok(favoriteService.getFavorites(uid, isOwner));
    }

    @GetMapping("/favorite/get-all/visitor")
    public CustomResponse<List<Favorite>> getAllVisitorFavorites(@RequestParam("uid") Integer uid) {
        return CustomResponse.ok(favoriteService.getFavorites(uid, false));
    }

    @PostMapping("/favorite/create")
    public CustomResponse<Favorite> createFavorite(@RequestParam("title") String title,
                                                   @RequestParam("desc") String desc,
                                                   @RequestParam("visible") Integer visible) {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        return CustomResponse.ok(favoriteService.addFavorite(uid, title, desc, visible));
    }
}
