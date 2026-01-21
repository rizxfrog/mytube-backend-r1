package com.mytube.danmu.service;

import com.mytube.common.web.CustomResponse;
import com.mytube.danmu.domain.Danmu;

import java.util.List;
import java.util.Set;

public interface DanmuService {
    List<Danmu> getDanmuListByIdset(Set<Object> idset);

    CustomResponse deleteDanmu(Integer id, Integer uid, boolean isAdmin);
}
