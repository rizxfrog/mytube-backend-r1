package com.mytube.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mytube.common.redis.RedisUtil;
import com.mytube.im.domain.ChatDetailed;
import com.mytube.im.mapper.ChatDetailedMapper;
import com.mytube.im.service.ChatDetailedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class ChatDetailedServiceImpl implements ChatDetailedService {
    @Autowired
    private ChatDetailedMapper chatDetailedMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, Object> getDetails(Integer uid, Integer aid, Long offset) {
        String key = "chat_detailed_zset:" + uid + ":" + aid;
        Map<String, Object> map = new HashMap<>();
        if (offset + 20 < redisUtil.zCard(key)) {
            map.put("more", true);
        } else {
            map.put("more", false);
        }
        Set<Object> set = redisUtil.zReverange(key, offset, offset + 19);
        if (set == null || set.isEmpty()) {
            map.put("list", Collections.emptyList());
            return map;
        }
        QueryWrapper<ChatDetailed> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", set);
        map.put("list", chatDetailedMapper.selectList(queryWrapper));
        return map;
    }

    @Override
    public boolean deleteDetail(Integer id, Integer uid) {
        try {
            ChatDetailed chatDetailed = chatDetailedMapper.selectById(id);
            if (chatDetailed == null) {
                return false;
            }
            UpdateWrapper<ChatDetailed> updateWrapper = new UpdateWrapper<>();
            if (Objects.equals(chatDetailed.getUserId(), uid)) {
                updateWrapper.eq("id", id).setSql("user_del = 1");
                chatDetailedMapper.update(null, updateWrapper);
                String key = "chat_detailed_zset:" + chatDetailed.getAnotherId() + ":" + uid;
                redisUtil.zsetDelMember(key, id);
                return true;
            }
            if (Objects.equals(chatDetailed.getAnotherId(), uid)) {
                updateWrapper.eq("id", id).setSql("another_del = 1");
                chatDetailedMapper.update(null, updateWrapper);
                String key = "chat_detailed_zset:" + chatDetailed.getUserId() + ":" + uid;
                redisUtil.zsetDelMember(key, id);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Delete chat detail failed {}", e.getMessage());
            return false;
        }
    }
}
