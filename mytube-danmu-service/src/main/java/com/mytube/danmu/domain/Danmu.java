package com.mytube.danmu.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Danmu {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer vid;
    private Integer uid;
    private String content;
    private Integer fontsize;
    private Integer mode;
    private String color;
    private Double timePoint;
    private Integer state;
    private Date createDate;
}
