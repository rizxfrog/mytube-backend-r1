package com.mytube.im.service;

import java.util.Map;

public interface ChatDetailedService {
    Map<String, Object> getDetails(Integer uid, Integer aid, Long offset);
    boolean deleteDetail(Integer id, Integer uid);
}
