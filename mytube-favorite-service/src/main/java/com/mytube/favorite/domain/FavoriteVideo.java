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
public class FavoriteVideo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer fid;
    private Integer vid;
    private Integer uid;
    private Date createTime;
}
