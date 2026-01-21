package com.mytube.user.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MsgUnread {
    @TableId
    private Long uid;
    private Integer reply;
    private Integer at;
    private Integer love;
    @TableField("\"system\"")
    private Integer system;
    private Integer whisper;
    @TableField("\"dynamic\"")
    private Integer dynamic;
}

