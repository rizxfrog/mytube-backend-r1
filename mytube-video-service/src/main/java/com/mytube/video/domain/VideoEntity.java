package com.mytube.video.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("video")
public class VideoEntity {
    @TableId(type = IdType.AUTO)
    private Integer vid;
    private Integer uid;
    private String title;
    private String coverUrl;
    private String videoUrl;
    private Integer status;
    private Date uploadDate;
}
