package com.mytube.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer uid;
    private String nickname;
    private String avatar_url;
    private String bg_url;
    private Integer gender;
    private String description;
    private Integer exp;
    private Double coin;
    private Integer vip;
    private Integer state;
    private Integer auth;
    private String authMsg;
    private Integer videoCount;
    private Integer followsCount;
    private Integer fansCount;
    private Integer loveCount;
    private Integer playCount;
}
