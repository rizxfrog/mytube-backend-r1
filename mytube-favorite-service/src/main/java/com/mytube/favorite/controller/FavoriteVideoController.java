package com.mytube.favorite.controller;

import com.mytube.common.security.CurrentUser;
import com.mytube.common.web.CustomResponse;
import com.mytube.api.video.UserVideoServiceApi;
import com.mytube.favorite.domain.Favorite;
import com.mytube.favorite.service.FavoriteService;
import com.mytube.favorite.service.FavoriteVideoService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class FavoriteVideoController {
    @Autowired
    private FavoriteVideoService favoriteVideoService;
    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private CurrentUser currentUser;
    @DubboReference
    private UserVideoServiceApi userVideoServiceApi;

    @GetMapping("/video/collected-fids")
    public CustomResponse<List<Integer>> collectedFids(@RequestParam("vid") Integer vid) {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        Set<Integer> fids = findFidsOfUserFavorites(uid.intValue());
        Set<Integer> collected = favoriteVideoService.findFidsOfCollected(vid, fids);
        return CustomResponse.ok(collected.stream().toList());
    }

    @PostMapping("/video/collect")
    public CustomResponse<String> collect(@RequestParam("vid") Integer vid,
                                          @RequestParam(value = "fid", required = false) Integer fid,
                                          @RequestParam(value = "adds", required = false) String[] addArray,
                                          @RequestParam(value = "removes", required = false) String[] removeArray) {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        Integer intUid = uid.intValue();
        if (addArray != null || removeArray != null) {
            return handleBatchCollect(intUid, vid, addArray, removeArray);
        }
        if (fid == null) {
            return CustomResponse.error(400, "Missing fid");
        }
        return CustomResponse.ok(favoriteVideoService.collectVideo(intUid, vid, fid));
    }

    @PostMapping("/video/cancel-collect")
    public CustomResponse<String> cancelCollect(@RequestParam("vid") Integer vid,
                                                @RequestParam(value = "fid", required = false) Integer fid) {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        if (fid == null) {
            return CustomResponse.error(400, "Missing fid");
        }
        Integer intUid = uid.intValue();
        Set<Integer> fids = findFidsOfUserFavorites(intUid);
        if (!fids.contains(fid)) {
            return CustomResponse.error(403, "No permission");
        }
        Set<Integer> collected = favoriteVideoService.findFidsOfCollected(vid, fids);
        favoriteVideoService.removeFromFav(intUid, vid, Set.of(fid));
        boolean isCancel = !collected.isEmpty() && collected.contains(fid) && collected.size() == 1;
        if (isCancel) {
            userVideoServiceApi.setCollect(uid, vid == null ? null : vid.longValue(), false);
        }
        return CustomResponse.ok("ok");
    }

    private CustomResponse<String> handleBatchCollect(Integer uid, Integer vid, String[] addArray, String[] removeArray) {
        Set<Integer> fids = findFidsOfUserFavorites(uid);
        Set<Integer> addSet = parseIdArray(addArray);
        Set<Integer> removeSet = parseIdArray(removeArray);
        if (!fids.containsAll(addSet) || !fids.containsAll(removeSet)) {
            return CustomResponse.error(403, "No permission");
        }
        Set<Integer> collected = favoriteVideoService.findFidsOfCollected(vid, fids);
        if (!addSet.isEmpty()) {
            favoriteVideoService.addToFav(uid, vid, addSet);
        }
        if (!removeSet.isEmpty()) {
            favoriteVideoService.removeFromFav(uid, vid, removeSet);
        }
        boolean isCollect = !addSet.isEmpty() && collected.isEmpty();
        boolean isCancel = addSet.isEmpty() && !collected.isEmpty()
                && collected.size() == removeSet.size()
                && collected.containsAll(removeSet);
        if (isCollect) {
            userVideoServiceApi.setCollect(uid.longValue(), vid == null ? null : vid.longValue(), true);
        } else if (isCancel) {
            userVideoServiceApi.setCollect(uid.longValue(), vid == null ? null : vid.longValue(), false);
        }
        return CustomResponse.ok("ok");
    }

    private Set<Integer> parseIdArray(String[] values) {
        if (values == null || values.length == 0) {
            return new HashSet<>();
        }
        return Arrays.stream(values)
                .filter(v -> v != null && !v.isBlank())
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    private Set<Integer> findFidsOfUserFavorites(Integer uid) {
        List<Favorite> list = favoriteService.getFavorites(uid, true);
        if (list == null) {
            return new HashSet<>();
        }
        return list.stream().map(Favorite::getFid).collect(Collectors.toSet());
    }
}
