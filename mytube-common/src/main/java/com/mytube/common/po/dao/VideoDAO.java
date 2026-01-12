package com.mytube.common.po.dao;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.util.Date;

@Data
public class VideoDAO {
    @TableId(value = "vid", type = IdType.AUTO) // 体标注主键列名，启用主键回填：
    private Long vid;
    private Long uid;
    private String title;
    private Long type;
    private Long auth;
    private Double duration;
    private String mc_id;
    private String sc_id;
    private String tags;
    private String descr;
    private String cover_url;
    private String video_url;
    private Integer status;
    private Date upload_date;
    private Date delete_date;
}
