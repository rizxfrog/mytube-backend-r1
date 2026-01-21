package com.mytube.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mytube.common.redis.RedisUtil;
import com.mytube.im.domain.Chat;
import com.mytube.im.domain.MsgUnread;
import com.mytube.im.mapper.ChatMapper;
import com.mytube.im.mapper.MsgUnreadMapper;
import com.mytube.im.model.IMResponse;
import com.mytube.im.service.MsgUnreadService;
import com.mytube.im.netty.state.ImState;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class MsgUnreadServiceImpl implements MsgUnreadService {
    @Autowired
    private MsgUnreadMapper msgUnreadMapper;
    @Autowired
    private ChatMapper chatMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    public void addOneUnread(Integer uid, String column) {
        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).setSql(column + " = " + column + " + 1");
        msgUnreadMapper.update(null, updateWrapper);
        redisUtil.delValue("msg_unread:" + uid);
    }

    @Override
    public void clearUnread(Integer uid, String column) {
        QueryWrapper<MsgUnread> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).ne(column, 0);
        MsgUnread msgUnread = msgUnreadMapper.selectOne(queryWrapper);
        if (msgUnread == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", "all_read");
        Set<Channel> myChannels = ImState.userChannel.get(uid);
        if (myChannels != null) {
            for (Channel channel : myChannels) {
                channel.writeAndFlush(IMResponse.message(column, map));
            }
        }

        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).set(column, 0);
        msgUnreadMapper.update(null, updateWrapper);
        redisUtil.delValue("msg_unread:" + uid);
        if (Objects.equals(column, "whisper")) {
            UpdateWrapper<Chat> updateWrapper1 = new UpdateWrapper<>();
            updateWrapper1.eq("another_id", uid).set("unread", 0);
            chatMapper.update(null, updateWrapper1);
        }
    }

    @Override
    public void subtractWhisper(Integer uid, Integer count) {
        UpdateWrapper<MsgUnread> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid)
                .setSql("whisper = CASE WHEN whisper - " + count + " < 0 THEN 0 ELSE whisper - " + count + " END");
        msgUnreadMapper.update(null, updateWrapper);
        redisUtil.delValue("msg_unread:" + uid);
    }

    @Override
    public MsgUnread getUnread(Integer uid) {
        MsgUnread msgUnread = redisUtil.getObject("msg_unread:" + uid, MsgUnread.class);
        if (msgUnread == null) {
            msgUnread = msgUnreadMapper.selectById(uid);
            if (msgUnread != null) {
                MsgUnread finalMsgUnread = msgUnread;
                CompletableFuture.runAsync(() -> redisUtil.setExObjectValue("msg_unread:" + uid, finalMsgUnread), taskExecutor);
            } else {
                return new MsgUnread(uid, 0, 0, 0, 0, 0, 0);
            }
        }
        return msgUnread;
    }
}
