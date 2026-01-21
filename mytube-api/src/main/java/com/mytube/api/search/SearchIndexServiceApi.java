package com.mytube.api.search;

public interface SearchIndexServiceApi {
    void upsertVideo(VideoIndexDTO video);
    void deleteVideo(Long vid);
}
