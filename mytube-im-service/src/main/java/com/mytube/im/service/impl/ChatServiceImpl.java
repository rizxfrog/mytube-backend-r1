package com.mytube.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mytube.api.user.UserServiceApi;
import com.mytube.common.redis.RedisUtil;
import com.mytube.im.domain.Chat;
import com.mytube.im.model.IMResponse;
import com.mytube.im.netty.state.ImState;
import com.mytube.im.service.ChatDetailedService;
import com.mytube.im.service.ChatService;
import com.mytube.im.service.MsgUnreadService;
import com.mytube.im.mapper.ChatMapper;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ChatMapper chatMapper;
    @Autowired
    private MsgUnreadService msgUnreadService;
    @Autowired
    private ChatDetailedService chatDetailedService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;
    @DubboReference
    private UserServiceApi userServiceApi;

    @Override
    public Map<String, Object> createChat(Integer from, Integer to) {
        Map<String, Object> map = new HashMap<>();
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", from).eq("another_id", to);
        Chat chat = chatMapper.selectOne(queryWrapper);
        if (chat != null) {
            if (chat.getIsDeleted() != null && chat.getIsDeleted() == 1) {
                chat.setIsDeleted(0);
                chat.setLatestTime(new Date());
                chatMapper.updateById(chat);
                redisUtil.zset("chat_zset:" + to, chat.getId());
                map.put("chat", chat);
                Chat finalChat = chat;
                CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                    map.put("user", userServiceApi.getUserInfo(finalChat.getUserId().longValue()));
                }, taskExecutor);
                CompletableFuture<Void> detailFuture = CompletableFuture.runAsync(() -> {
                    map.put("detail", chatDetailedService.getDetails(from, to, 0L));
                }, taskExecutor);
                map.put("msg", "created");
                userFuture.join();
                detailFuture.join();
                return map;
            }
            map.put("msg", "exists");
            return map;
        }
        if (userServiceApi.getUserInfo(to.longValue()) == null) {
            map.put("msg", "unknown");
            return map;
        }
        chat = new Chat(null, from, to, 0, 0, new Date());
        chatMapper.insert(chat);
        redisUtil.zset("chat_zset:" + to, chat.getId());
        map.put("chat", chat);
        Chat finalChat = chat;
        CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
            map.put("user", userServiceApi.getUserInfo(finalChat.getUserId().longValue()));
        }, taskExecutor);
        CompletableFuture<Void> detailFuture = CompletableFuture.runAsync(() -> {
            map.put("detail", chatDetailedService.getDetails(from, to, 0L));
        }, taskExecutor);
        map.put("msg", "created");
        userFuture.join();
        detailFuture.join();
        return map;
    }

    @Override
    public List<Map<String, Object>> getChatListWithData(Integer uid, Long offset) {
        Set<Object> set = redisUtil.zReverange("chat_zset:" + uid, offset, offset + 9);
        if (set == null || set.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", set).eq("is_deleted", 0).orderByDesc("latest_time");
        List<Chat> chatList = chatMapper.selectList(queryWrapper);
        if (chatList == null || chatList.isEmpty()) {
            return Collections.emptyList();
        }
        Stream<Chat> chatStream = chatList.stream();
        return chatStream.parallel().map(chat -> {
            Map<String, Object> map = new HashMap<>();
            map.put("chat", chat);
            CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                map.put("user", userServiceApi.getUserInfo(chat.getUserId().longValue()));
            }, taskExecutor);
            CompletableFuture<Void> detailFuture = CompletableFuture.runAsync(() -> {
                map.put("detail", chatDetailedService.getDetails(chat.getUserId(), uid, 0L));
            }, taskExecutor);
            userFuture.join();
            detailFuture.join();
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Chat getChat(Integer from, Integer to) {
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", from).eq("another_id", to);
        return chatMapper.selectOne(queryWrapper);
    }

    @Override
    public void delChat(Integer from, Integer to) {
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", from).eq("another_id", to);
        Chat chat = chatMapper.selectOne(queryWrapper);
        if (chat == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", "remove");
        map.put("id", chat.getId());
        map.put("count", chat.getUnread());
        Set<Channel> myChannels = ImState.userChannel.get(to);
        if (myChannels != null) {
            for (Channel channel : myChannels) {
                channel.writeAndFlush(IMResponse.message("whisper", map));
            }
        }

        if (chat.getUnread() != null && chat.getUnread() > 0) {
            msgUnreadService.subtractWhisper(to, chat.getUnread());
        }
        UpdateWrapper<Chat> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", from).eq("another_id", to).setSql("is_deleted = 1").setSql("unread = 0");
        chatMapper.update(null, updateWrapper);
        redisUtil.zsetDelMember("chat_zset:" + to, chat.getId());
    }

    @Override
    public boolean updateChat(Integer from, Integer to) {
        String key = "whisper:" + to + ":" + from;
        boolean online = redisUtil.isExist(key);
        try {
            CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                QueryWrapper<Chat> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("user_id", to).eq("another_id", from);
                Chat chat1 = chatMapper.selectOne(queryWrapper1);
                UpdateWrapper<Chat> updateWrapper1 = new UpdateWrapper<>();
                updateWrapper1.eq("user_id", to)
                        .eq("another_id", from)
                        .set("is_deleted", 0)
                        .set("latest_time", new Date());
                chatMapper.update(null, updateWrapper1);
                if (chat1 != null) {
                    redisUtil.zset("chat_zset:" + from, chat1.getId());
                }
            }, taskExecutor);

            CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
                QueryWrapper<Chat> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.eq("user_id", from).eq("another_id", to);
                Chat chat2 = chatMapper.selectOne(queryWrapper2);

                if (online) {
                    if (chat2 == null) {
                        chat2 = new Chat(null, from, to, 0, 0, new Date());
                        chatMapper.insert(chat2);
                    } else {
                        UpdateWrapper<Chat> updateWrapper2 = new UpdateWrapper<>();
                        updateWrapper2.eq("id", chat2.getId())
                                .set("is_deleted", 0)
                                .set("latest_time", new Date());
                        chatMapper.update(null, updateWrapper2);
                    }
                    redisUtil.zset("chat_zset:" + to, chat2.getId());
                } else {
                    if (chat2 == null) {
                        chat2 = new Chat(null, from, to, 0, 1, new Date());
                        chatMapper.insert(chat2);
                    } else {
                        UpdateWrapper<Chat> updateWrapper2 = new UpdateWrapper<>();
                        updateWrapper2.eq("id", chat2.getId())
                                .set("is_deleted", 0)
                                .setSql("unread = unread + 1")
                                .set("latest_time", new Date());
                        chatMapper.update(null, updateWrapper2);
                    }
                    msgUnreadService.addOneUnread(to, "whisper");
                    redisUtil.zset("chat_zset:" + to, chat2.getId());
                }
            }, taskExecutor);

            future1.join();
            future2.join();
        } catch (Exception e) {
            log.error("Update chat failed {}", e.getMessage());
        }
        return online;
    }

    @Override
    public void updateWhisperOnline(Integer from, Integer to) {
        try {
            String key = "whisper:" + to + ":" + from;
            redisUtil.setValue(key, true);

            QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", from).eq("another_id", to);
            Chat chat = chatMapper.selectOne(queryWrapper);
            if (chat != null && chat.getUnread() != null && chat.getUnread() > 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", "read");
                map.put("id", chat.getId());
                map.put("count", chat.getUnread());
                Set<Channel> myChannels = ImState.userChannel.get(to);
                if (myChannels != null) {
                    for (Channel channel : myChannels) {
                        channel.writeAndFlush(IMResponse.message("whisper", map));
                    }
                }
                msgUnreadService.subtractWhisper(to, chat.getUnread());
            }
            UpdateWrapper<Chat> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("user_id", from).eq("another_id", to).set("unread", 0);
            chatMapper.update(null, updateWrapper);
        } catch (Exception e) {
            log.error("Update whisper online failed {}", e.getMessage());
        }
    }

    @Override
    public void updateWhisperOutline(Integer from, Integer to) {
        try {
            String key = "whisper:" + to + ":" + from;
            redisUtil.delValue(key);
        } catch (Exception e) {
            log.error("Update whisper outline failed {}", e.getMessage());
        }
    }
}
