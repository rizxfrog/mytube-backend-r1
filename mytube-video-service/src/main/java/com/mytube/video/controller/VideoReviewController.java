package com.mytube.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mytube.common.security.CurrentUser;
import com.mytube.common.po.dao.VideoDAO;
import com.mytube.common.web.CustomResponse;
import com.mytube.video.mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VideoReviewController {
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/review/video/total")
    public CustomResponse<Integer> total(@RequestParam("vstatus") Integer status) {
        if (!currentUser.isAdmin()) {
            return CustomResponse.error(403, "Admin only");
        }
        QueryWrapper<VideoDAO> wrapper = new QueryWrapper<>();
        wrapper.eq("status", status);
        Long count = videoMapper.selectCount(wrapper);
        return CustomResponse.ok(count == null ? 0 : count.intValue());
    }

    @GetMapping("/review/video/getpage")
    public CustomResponse<List<VideoDAO>> getPage(@RequestParam("vstatus") Integer status,
                                                  @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                  @RequestParam(value = "quantity", defaultValue = "10") Integer quantity) {
        if (!currentUser.isAdmin()) {
            return CustomResponse.error(403, "Admin only");
        }
        int offset = (page - 1) * quantity;
        QueryWrapper<VideoDAO> wrapper = new QueryWrapper<>();
        wrapper.eq("status", status).last("LIMIT " + quantity + " OFFSET " + offset);
        return CustomResponse.ok(videoMapper.selectList(wrapper));
    }

    @GetMapping("/review/video/getone")
    public CustomResponse<Object> getOne(@RequestParam("vid") Integer vid) {
        if (!currentUser.isAdmin()) {
            return CustomResponse.error(403, "Admin only");
        }
        return CustomResponse.ok(videoMapper.selectById(vid));
    }
}
