package com.mytube.im;

import com.mytube.im.netty.ImServer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ImStarter implements ApplicationRunner {
    private final ImServer server;
    public ImStarter(ImServer server) { this.server = server; }
    @Override
    public void run(ApplicationArguments args) throws Exception {
        new Thread(() -> {
            try { server.start(); } catch (Exception ignored) {}
        }).start();
    }
}

