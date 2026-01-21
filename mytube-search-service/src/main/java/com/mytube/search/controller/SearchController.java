package com.mytube.search.controller;

import com.mytube.api.user.UserServiceApi;
import com.mytube.api.video.VideoServiceApi;
import com.mytube.common.redis.RedisUtil;
import com.mytube.common.web.CustomResponse;
import com.mytube.search.domain.HotSearch;
import com.mytube.search.util.ESUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class SearchController {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ESUtil esUtil;
    @DubboReference
    private VideoServiceApi videoServiceApi;
    @DubboReference
    private UserServiceApi userServiceApi;

    @GetMapping("/search/hot/get")
    public CustomResponse<List<HotSearch>> getHot() {
        List<RedisUtil.ZObjScore> curr = redisUtil.zReverangeWithScores("search_word", 0, 9);
        List<HotSearch> list = new ArrayList<>();
        if (curr != null) {
            for (RedisUtil.ZObjScore o : curr) {
                HotSearch word = new HotSearch();
                word.setContent(o.getMember().toString());
                word.setScore(o.getScore());
                word.setType(1);
                list.add(word);
            }
        }
        return CustomResponse.ok(list);
    }

    @PostMapping("/search/word/add")
    public CustomResponse<String> addWord(@RequestParam(value = "keyword", required = false) String keyword,
                                          @RequestParam(value = "text", required = false) String text) {
        String formatted = keyword != null ? keyword : text;
        if (formatted == null) {
            return CustomResponse.error(400, "Empty");
        }
        formatted = formatted.trim();
        if (!formatted.isEmpty()) {
            if (redisUtil.zsetExist("search_word", formatted)) {
                redisUtil.zincrby("search_word", formatted, 1);
            } else {
                redisUtil.zsetWithScore("search_word", formatted, 1);
                esUtil.addSearchWord(formatted);
            }
        }
        return CustomResponse.ok(formatted);
    }

    @GetMapping("/search/word/get")
    public CustomResponse<List<String>> getWord(@RequestParam("keyword") String keyword) throws UnsupportedEncodingException {
        String decoded = URLDecoder.decode(keyword, "UTF-8");
        if (decoded.trim().isEmpty()) {
            return CustomResponse.ok(Collections.emptyList());
        }
        return CustomResponse.ok(esUtil.getMatchingWord(decoded));
    }

    @GetMapping("/search/count")
    public CustomResponse<List<Long>> getCount(@RequestParam("keyword") String keyword) throws UnsupportedEncodingException {
        String decoded = URLDecoder.decode(keyword, "UTF-8");
        List<Long> list = new ArrayList<>();
        list.add(esUtil.getVideoCount(decoded, true));
        list.add(esUtil.getUserCount(decoded));
        return CustomResponse.ok(list);
    }

    @GetMapping("/search/video/only-pass")
    public CustomResponse<Object> searchVideo(@RequestParam("keyword") String keyword,
                                              @RequestParam(value = "page", defaultValue = "1") Integer page) throws UnsupportedEncodingException {
        String decoded = URLDecoder.decode(keyword, "UTF-8");
        List<Integer> ids = esUtil.searchVideosByKeyword(decoded, page, 30, true);
        List<Long> vids = ids.stream().map(Integer::longValue).toList();
        return CustomResponse.ok(videoServiceApi.getVideoInfoList(vids));
    }

    @GetMapping("/search/user")
    public CustomResponse<Object> searchUser(@RequestParam("keyword") String keyword,
                                             @RequestParam(value = "page", defaultValue = "1") Integer page) throws UnsupportedEncodingException {
        String decoded = URLDecoder.decode(keyword, "UTF-8");
        List<Integer> ids = esUtil.searchUsersByKeyword(decoded, page, 30);
        List<Long> uids = ids.stream().map(Integer::longValue).toList();
        return CustomResponse.ok(userServiceApi.getUserInfoList(uids));
    }
}
