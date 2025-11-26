package com.mytube.gateway.graphql;

import com.mytube.api.video.VideoServiceApi;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class QueryResolver {
    @DubboReference
    private VideoServiceApi videoServiceApi;

    @QueryMapping
    public List<Integer> randomVisitorVideos(Integer count) {
        int c = count == null ? 11 : count;
        return videoServiceApi.randomVisitorVideos(c);
    }
}

