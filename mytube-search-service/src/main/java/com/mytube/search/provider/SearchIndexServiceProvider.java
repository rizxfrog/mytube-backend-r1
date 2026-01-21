package com.mytube.search.provider;

import com.mytube.api.search.SearchIndexServiceApi;
import com.mytube.api.search.VideoIndexDTO;
import com.mytube.search.domain.ESVideo;
import com.mytube.search.util.ESUtil;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class SearchIndexServiceProvider implements SearchIndexServiceApi {
    @Autowired
    private ESUtil esUtil;

    @Override
    public void upsertVideo(VideoIndexDTO video) {
        if (video == null || video.getVid() == null) {
            return;
        }
        ESVideo esVideo = new ESVideo();
        esVideo.setVid(video.getVid().intValue());
        if (video.getUid() != null) {
            esVideo.setUid(video.getUid().intValue());
        }
        esVideo.setTitle(video.getTitle());
        esVideo.setMcId(video.getMcId());
        esVideo.setScId(video.getScId());
        esVideo.setTags(video.getTags());
        esVideo.setStatus(video.getStatus());
        esUtil.upsertVideo(esVideo);
    }

    @Override
    public void deleteVideo(Long vid) {
        if (vid == null) {
            return;
        }
        esUtil.deleteVideo(vid.intValue());
    }
}
