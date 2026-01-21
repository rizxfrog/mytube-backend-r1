package com.mytube.im.service;

import com.mytube.im.domain.MsgUnread;

public interface MsgUnreadService {
    void addOneUnread(Integer uid, String column);
    void clearUnread(Integer uid, String column);
    void subtractWhisper(Integer uid, Integer count);
    MsgUnread getUnread(Integer uid);
}
