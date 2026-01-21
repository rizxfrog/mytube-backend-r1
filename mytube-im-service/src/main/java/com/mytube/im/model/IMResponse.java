package com.mytube.im.model;

import com.alibaba.fastjson2.JSON;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IMResponse {
    private String type;
    private LocalDateTime time;
    private Object data;

    public static TextWebSocketFrame error(String message) {
        return new TextWebSocketFrame(JSON.toJSONString(new IMResponse("error", LocalDateTime.now(), message)));
    }

    public static TextWebSocketFrame message(String type, Object data) {
        return new TextWebSocketFrame(JSON.toJSONString(new IMResponse(type, LocalDateTime.now(), data)));
    }
}
