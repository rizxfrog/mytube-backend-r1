package com.mytube.danmu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mytube.api.video.VideoServiceApi;
import com.mytube.common.redis.RedisUtil;
import com.mytube.common.web.CustomResponse;
import com.mytube.danmu.domain.Danmu;
import com.mytube.danmu.mapper.DanmuMapper;
import com.mytube.danmu.service.DanmuService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class DanmuServiceImpl implements DanmuService {
    @Autowired
    private DanmuMapper danmuMapper;
    @Autowired
    private RedisUtil redisUtil;
    @DubboReference
    private VideoServiceApi videoServiceApi;

    @Override
    public List<Danmu> getDanmuListByIdset(Set<Object> idset) {
        if (idset == null || idset.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<Danmu> wrapper = new QueryWrapper<>();
        wrapper.in("id", idset).eq("state", 1);
        return danmuMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public CustomResponse deleteDanmu(Integer id, Integer uid, boolean isAdmin) {
        CustomResponse response = new CustomResponse();
        QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).ne("state", 3);
        Danmu danmu = danmuMapper.selectOne(queryWrapper);
        if (danmu == null) {
            response.setCode(404);
            response.setMessage("Danmu not found");
            return response;
        }
        Long videoOwner = videoServiceApi.getVideoOwner(danmu.getVid() == null ? null : danmu.getVid().longValue());
        boolean canDelete = Objects.equals(danmu.getUid(), uid)
                || isAdmin
                || (videoOwner != null && Objects.equals(videoOwner.intValue(), uid));
        if (!canDelete) {
            response.setCode(403);
            response.setMessage("No permission");
            return response;
        }
        UpdateWrapper<Danmu> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).set("state", 3);
        danmuMapper.update(null, updateWrapper);
        redisUtil.delMember("danmu_idset:" + danmu.getVid(), id);
        return response;
    }
}
