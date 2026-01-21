package com.mytube.im.netty.handler;

import com.alibaba.fastjson2.JSON;
import com.mytube.im.model.Command;
import com.mytube.im.model.CommandType;
import com.mytube.im.model.IMResponse;
import com.mytube.im.netty.state.ImState;
import com.mytube.common.redis.RedisUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.netty.channel.ChannelHandler.Sharable;

import java.util.Set;

@Slf4j
@Component
@Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Autowired
    private ChatHandler chatHandler;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame tx) {
        try {
            Command command = JSON.parseObject(tx.text(), Command.class);
            switch (CommandType.match(command.getCode())) {
                case CONNETION:
                    break;
                case CHAT_SEND:
                    chatHandler.send(ctx, tx);
                    break;
                case CHAT_WITHDRAW:
                    chatHandler.withdraw(ctx, tx);
                    break;
                default:
                    ctx.channel().writeAndFlush(IMResponse.error("Unsupported code " + command.getCode()));
            }
        } catch (Exception e) {
            log.error("websocket handler error {}", e.getMessage());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Integer uid = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        Set<Channel> userChannels = ImState.userChannel.get(uid);
        if (userChannels != null) {
            userChannels.remove(ctx.channel());
            if (ImState.userChannel.get(uid).isEmpty()) {
                ImState.userChannel.remove(uid);
                redisUtil.deleteKeysWithPrefix("whisper:" + uid + ":");
                redisUtil.delMember("login_member", uid);
            }
        }
        ctx.fireChannelInactive();
    }
}
