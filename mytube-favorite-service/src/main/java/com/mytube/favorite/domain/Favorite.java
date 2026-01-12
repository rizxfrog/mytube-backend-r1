package com.mytube.favorite.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Integer fid;
    private Integer uid;
    private Integer type;
    private Integer visible;
    private String cover;
    private String title;
    private String description;
    private Integer isDelete;
    private Date createTime;
}
