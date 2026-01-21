package com.mytube.im.netty.state;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImState {
    public static final Map<Integer, Set<Channel>> userChannel = new ConcurrentHashMap<>();
}
