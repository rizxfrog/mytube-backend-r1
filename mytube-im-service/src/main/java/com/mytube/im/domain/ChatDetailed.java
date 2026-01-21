package com.mytube.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatDetailed {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer anotherId;
    private String content;
    private Integer userDel;
    private Integer anotherDel;
    private Integer withdraw;
    private Date time;
}
