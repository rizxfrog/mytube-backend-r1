package com.mytube.common.po;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class VideoMeta {
    private String title;
    private int type;
    private int duration;
    private String mc_id;
    private String sc_id;
    private String tags;
    private String description;
}
