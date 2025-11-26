package com.mytube.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String nickname;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}

