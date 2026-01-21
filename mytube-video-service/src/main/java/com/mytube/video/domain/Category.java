package com.mytube.video.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String mcId;
    private String scId;
    private String mcName;
    private String scName;
    private String descr;
    private String rcmTag;
}
