package com.mytube.api.stats;

public interface StatsServiceApi {
    String playVisitor(Integer vid);
    String loveOrNot(Long uid, Integer vid, Integer isLike, Integer isSet);
}

