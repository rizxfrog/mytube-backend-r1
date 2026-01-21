package com.mytube.search.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotSearch {
    private String content;
    private Double score;
    private Integer type;
}
