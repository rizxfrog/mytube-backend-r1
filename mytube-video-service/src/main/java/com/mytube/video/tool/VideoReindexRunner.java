package com.mytube.video.tool;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mytube.api.search.SearchIndexServiceApi;
import com.mytube.api.search.VideoIndexDTO;
import com.mytube.common.po.dao.VideoDAO;
import com.mytube.video.mapper.VideoMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VideoReindexRunner implements CommandLineRunner {
    @Value("${reindex.videos.enabled:false}")
    private boolean enabled;

    @Value("${reindex.videos.page-size:200}")
    private int pageSize;

    private final VideoMapper videoMapper;

    @DubboReference
    private SearchIndexServiceApi searchIndexServiceApi;

    public VideoReindexRunner(VideoMapper videoMapper) {
        this.videoMapper = videoMapper;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        int page = 0;
        while (true) {
            int offset = page * pageSize;
            QueryWrapper<VideoDAO> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1).orderByAsc("vid").last("LIMIT " + pageSize + " OFFSET " + offset);
            List<VideoDAO> batch = videoMapper.selectList(wrapper);
            if (batch == null || batch.isEmpty()) {
                break;
            }
            for (VideoDAO video : batch) {
                VideoIndexDTO dto = new VideoIndexDTO();
                dto.setVid(video.getVid());
                dto.setUid(video.getUid());
                dto.setTitle(video.getTitle());
                dto.setMcId(video.getMc_id());
                dto.setScId(video.getSc_id());
                dto.setTags(video.getTags());
                dto.setStatus(video.getStatus());
                try {
                    searchIndexServiceApi.upsertVideo(dto);
                } catch (Exception ignored) {}
            }
            page += 1;
        }
    }
}
