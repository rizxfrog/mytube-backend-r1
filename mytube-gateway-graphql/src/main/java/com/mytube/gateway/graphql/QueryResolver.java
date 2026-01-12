package com.mytube.gateway.graphql;

import com.mytube.api.video.VideoServiceApi;
import com.mytube.api.user.UserServiceApi;
import com.mytube.api.upload.UploadServiceApi;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class QueryResolver {
    private static final Logger log = LoggerFactory.getLogger(QueryResolver.class);
    @DubboReference
    private VideoServiceApi videoServiceApi;
    @DubboReference
    private UserServiceApi userServiceApi;
    @DubboReference
    private UploadServiceApi uploadServiceApi;

    @QueryMapping
    public List<Integer> randomVisitorVideos(@Argument("count") Integer count) {
        int c = (count == null || count <= 0) ? 11 : count;
        List<Integer> ids = videoServiceApi.randomVisitorVideos(c);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> sanitized = ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Collections.shuffle(sanitized);
        if (sanitized.size() > c) {
            return sanitized.subList(0, c);
        }
        return sanitized;
    }

    @QueryMapping
    public Integer videoGetOne(@Argument("vid") Integer vid) {
        return videoServiceApi.getOneVideoById(vid);
    }

    @QueryMapping
    public List<Integer> videoCumulativeVisitor(@Argument("vids") List<Integer> vids) {
        return videoServiceApi.cumulativeVisitorVideos(vids);
    }

    @QueryMapping
    public Integer uploadAskChunk(@Argument("hash") String hash) {
        return uploadServiceApi.askChunk(hash);
    }
}
