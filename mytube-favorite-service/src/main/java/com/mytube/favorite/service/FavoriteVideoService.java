package com.mytube.favorite.service;

import java.util.List;
import java.util.Set;

public interface FavoriteVideoService {
    List<Integer> getCollectedFids(Integer uid, Integer vid);

    String collectVideo(Integer uid, Integer vid, Integer fid);

    String cancelCollect(Integer uid, Integer vid, Integer fid);

    Set<Integer> findFidsOfCollected(Integer vid, Set<Integer> fids);

    void addToFav(Integer uid, Integer vid, Set<Integer> fids);

    void removeFromFav(Integer uid, Integer vid, Set<Integer> fids);
}
