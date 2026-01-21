package com.mytube.im.netty.handler;

import com.alibaba.fastjson2.JSON;
import com.mytube.common.redis.RedisUtil;
import com.mytube.common.security.JwtUtil;
import com.mytube.im.model.Command;
import com.mytube.im.model.IMResponse;
import com.mytube.im.netty.state.ImState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.netty.channel.ChannelHandler.Sharable;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@Sharable
public class TokenValidationHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static JwtUtil jwtUtil;
    private static RedisUtil redisUtil;

    @Autowired
    public void setDependencies(JwtUtil jwtUtil, RedisUtil redisUtil) {
        TokenValidationHandler.jwtUtil = jwtUtil;
        TokenValidationHandler.redisUtil = redisUtil;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame tx) {
        Command command = JSON.parseObject(tx.text(), Command.class);
        String token = command == null ? null : command.getContent();

        Integer uid = isValidToken(token);
        if (uid != null) {
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(uid);
            ImState.userChannel.computeIfAbsent(uid, k -> new HashSet<>()).add(ctx.channel());
            redisUtil.addMember("login_member", uid);
            ctx.pipeline().remove(TokenValidationHandler.class);
            tx.retain();
            ctx.fireChannelRead(tx);
        } else {
            ctx.channel().writeAndFlush(IMResponse.error("Login expired"));
            ctx.close();
        }
    }

    private Integer isValidToken(String token) {
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            return null;
        }
        token = token.substring(7);
        if (!jwtUtil.verifyToken(token)) {
            log.error("token expired");
            return null;
        }
        String userId = JwtUtil.getSubjectFromToken(token);
        String role = JwtUtil.getClaimFromToken(token, "role");
        String json = redisUtil.getObjectString("security:" + role + ":" + userId);
        if (json == null) {
            return null;
        }
        try {
            com.alibaba.fastjson2.JSONObject obj = com.alibaba.fastjson2.JSON.parseObject(json);
            Object uid = obj.get("uid");
            return uid == null ? null : Integer.parseInt(uid.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
