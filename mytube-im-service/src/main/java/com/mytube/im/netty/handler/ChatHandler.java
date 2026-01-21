package com.mytube.im.netty.handler;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mytube.api.user.UserServiceApi;
import com.mytube.common.redis.RedisUtil;
import com.mytube.im.domain.ChatDetailed;
import com.mytube.im.mapper.ChatDetailedMapper;
import com.mytube.im.model.IMResponse;
import com.mytube.im.netty.state.ImState;
import com.mytube.im.service.ChatService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class ChatHandler {
    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatDetailedMapper chatDetailedMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;
    @DubboReference
    private UserServiceApi userServiceApi;

    public void send(ChannelHandlerContext ctx, TextWebSocketFrame tx) {
        try {
            ChatDetailed chatDetailed = JSONObject.parseObject(tx.text(), ChatDetailed.class);
            Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
            chatDetailed.setUserId(userId);
            chatDetailed.setUserDel(0);
            chatDetailed.setAnotherDel(0);
            chatDetailed.setWithdraw(0);
            chatDetailed.setTime(new java.util.Date());
            chatDetailedMapper.insert(chatDetailed);
            redisUtil.zset("chat_detailed_zset:" + userId + ":" + chatDetailed.getAnotherId(), chatDetailed.getId());
            redisUtil.zset("chat_detailed_zset:" + chatDetailed.getAnotherId() + ":" + userId, chatDetailed.getId());
            boolean online = chatService.updateChat(userId, chatDetailed.getAnotherId());

            Map<String, Object> map = new HashMap<>();
            map.put("type", "receive");
            map.put("online", online);
            map.put("detail", chatDetailed);
            CompletableFuture<Void> chatFuture = CompletableFuture.runAsync(() -> {
                map.put("chat", chatService.getChat(userId, chatDetailed.getAnotherId()));
            }, taskExecutor);
            CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                map.put("user", userServiceApi.getUserInfo(userId.longValue()));
            }, taskExecutor);
            chatFuture.join();
            userFuture.join();

            Set<Channel> from = ImState.userChannel.get(userId);
            if (from != null) {
                for (Channel channel : from) {
                    channel.writeAndFlush(IMResponse.message("whisper", map));
                }
            }
            Set<Channel> to = ImState.userChannel.get(chatDetailed.getAnotherId());
            if (to != null) {
                for (Channel channel : to) {
                    channel.writeAndFlush(IMResponse.message("whisper", map));
                }
            }
        } catch (Exception e) {
            log.error("send chat failed {}", e.getMessage());
            ctx.channel().writeAndFlush(IMResponse.error("Send failed"));
        }
    }

    public void withdraw(ChannelHandlerContext ctx, TextWebSocketFrame tx) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(tx.text());
            Integer id = jsonObject.getInteger("id");
            Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

            ChatDetailed chatDetailed = chatDetailedMapper.selectById(id);
            if (chatDetailed == null) {
                ctx.channel().writeAndFlush(IMResponse.error("Not found"));
                return;
            }
            if (!Objects.equals(chatDetailed.getUserId(), userId)) {
                ctx.channel().writeAndFlush(IMResponse.error("No permission"));
                return;
            }
            long diff = System.currentTimeMillis() - chatDetailed.getTime().getTime();
            if (diff > 120000) {
                ctx.channel().writeAndFlush(IMResponse.error("Too late to withdraw"));
                return;
            }
            UpdateWrapper<ChatDetailed> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id).setSql("withdraw = 1");
            chatDetailedMapper.update(null, updateWrapper);

            Map<String, Object> map = new HashMap<>();
            map.put("type", "withdraw");
            map.put("sendId", chatDetailed.getUserId());
            map.put("acceptId", chatDetailed.getAnotherId());
            map.put("id", id);

            Set<Channel> from = ImState.userChannel.get(userId);
            if (from != null) {
                for (Channel channel : from) {
                    channel.writeAndFlush(IMResponse.message("whisper", map));
                }
            }
            Set<Channel> to = ImState.userChannel.get(chatDetailed.getAnotherId());
            if (to != null) {
                for (Channel channel : to) {
                    channel.writeAndFlush(IMResponse.message("whisper", map));
                }
            }
        } catch (Exception e) {
            log.error("withdraw chat failed {}", e.getMessage());
            ctx.channel().writeAndFlush(IMResponse.error("Withdraw failed"));
        }
    }
}
