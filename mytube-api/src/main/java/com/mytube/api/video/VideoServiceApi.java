package com.mytube.api.video;

import java.util.List;

public interface VideoServiceApi {
    List<Integer> randomVisitorVideos(int count);
    Integer getOneVideoById(Integer vid);
    List<Integer> cumulativeVisitorVideos(List<Integer> vids);
    Long getVideoOwner(Long vid);
    Object getVideoInfo(Long vid);
    Object getVideoInfoList(List<Long> vids);
}
