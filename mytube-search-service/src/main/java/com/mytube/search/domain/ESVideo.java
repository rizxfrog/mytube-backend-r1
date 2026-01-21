package com.mytube.search.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESVideo {
    private Integer vid;
    private Integer uid;
    private String title;
    private String mcId;
    private String scId;
    private String tags;
    private Integer status;
}
