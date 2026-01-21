package com.mytube.im.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandType {
    CONNETION(100),
    CHAT_SEND(101),
    CHAT_WITHDRAW(102),
    ERROR(-1);

    private final Integer code;

    public static CommandType match(Integer code) {
        for (CommandType value : CommandType.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return ERROR;
    }
}
