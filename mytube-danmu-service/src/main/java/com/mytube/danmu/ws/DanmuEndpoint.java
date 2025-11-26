package com.mytube.danmu.ws;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/danmu/{vid}")
public class DanmuEndpoint {
    private static final Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("vid") String vid) {
        sessions.computeIfAbsent(vid, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("vid") String vid) {
        Set<Session> set = sessions.get(vid);
        if (set != null) {
            set.parallelStream().forEach(s -> {
                try { s.getBasicRemote().sendText(message); } catch (Exception ignored) {}
            });
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("vid") String vid) {
        Set<Session> set = sessions.get(vid);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) sessions.remove(vid);
        }
    }
}
