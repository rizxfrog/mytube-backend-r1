package com.mytube.danmu.controller;

import com.mytube.common.redis.RedisUtil;
import com.mytube.common.security.CurrentUser;
import com.mytube.common.web.CustomResponse;
import com.mytube.danmu.domain.Danmu;
import com.mytube.danmu.service.DanmuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
public class DanmuController {
    @Autowired
    private DanmuService danmuService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/danmu-list/{vid}")
    public CustomResponse<List<Danmu>> list(@PathVariable("vid") Integer vid) {
        Set<Object> idset = redisUtil.getMembers("danmu_idset:" + vid);
        return CustomResponse.ok(danmuService.getDanmuListByIdset(idset));
    }

    @PostMapping("/danmu/delete")
    public CustomResponse<String> delete(@RequestParam("id") Integer id) {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "Not logged in");
        }
        CustomResponse response = danmuService.deleteDanmu(id, uid.intValue(), currentUser.isAdmin());
        if (response.getCode() != 0) {
            return response;
        }
        return CustomResponse.ok("ok");
    }
}
