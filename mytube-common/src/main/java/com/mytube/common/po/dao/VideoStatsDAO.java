package com.mytube.common.po.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoStatsDAO {
    @TableId
    private Long vid;
    private Integer play;
    private Integer danmu;
    private Integer good;
    private Integer bad;
    private Integer coin;
    private Integer collect;
    private Integer share;
    private Integer comment;
}
