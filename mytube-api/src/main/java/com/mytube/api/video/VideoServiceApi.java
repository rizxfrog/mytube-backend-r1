package com.mytube.api.video;

import java.util.List;

public interface VideoServiceApi {
    List<Integer> randomVisitorVideos(int count);
    Integer getOneVideoById(Integer vid);
    List<Integer> cumulativeVisitorVideos(List<Integer> vids);
}
