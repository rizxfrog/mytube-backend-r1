package com.mytube.stats.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mytube.common.web.CustomResponse;
import com.mytube.stats.domain.Stats;
import com.mytube.stats.mapper.StatsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/video")
public class StatsController {
    @Autowired
    private StatsMapper statsMapper;

    @PostMapping("/play/visitor")
    public CustomResponse<String> playVisitor(@RequestParam Integer vid) {
        UpdateWrapper<Stats> update = new UpdateWrapper<>();
        update.eq("video_id", vid).setSql("play = play + 1");
        int updated = statsMapper.update(null, update);
        if (updated == 0) {
            Stats stats = new Stats();
            stats.setVideoId(vid);
            stats.setPlay(1);
            stats.setLike(0);
            stats.setDanmu(0);
            statsMapper.insert(stats);
        }
        return CustomResponse.ok("updated");
    }

    @PostMapping("/love-or-not")
    public CustomResponse<String> loveOrNot(@RequestParam Integer vid,
                                            @RequestParam Integer isLike,
                                            @RequestParam Integer isSet) {
        boolean like = isLike != null && isLike == 1;
        boolean set = isSet != null && isSet == 1;
        if (!like) {
            return CustomResponse.ok("updated");
        }
        UpdateWrapper<Stats> update = new UpdateWrapper<>();
        update.eq("video_id", vid);
        if (set) {
            update.setSql("\"like\" = \"like\" + 1");
        } else {
            update.setSql("\"like\" = CASE WHEN \"like\" - 1 < 0 THEN 0 ELSE \"like\" - 1 END");
        }
        int updated = statsMapper.update(null, update);
        if (updated == 0 && set) {
            Stats stats = new Stats();
            stats.setVideoId(vid);
            stats.setPlay(0);
            stats.setLike(1);
            stats.setDanmu(0);
            statsMapper.insert(stats);
        }
        return CustomResponse.ok("updated");
    }
}
