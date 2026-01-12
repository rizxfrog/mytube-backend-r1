package com.mytube.common.web;

public class CustomResponse<T> {
    private int code;
    private String message;
    private T data;

    public CustomResponse() {}

    public CustomResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CustomResponse<T> ok() {
        return new CustomResponse<>(0, "ok", null);
    }

    public static <T> CustomResponse<T> ok(T data) {
        return new CustomResponse<>(0, "ok", data);
    }

    public static <T> CustomResponse<T> error(int code, String message) {
        return new CustomResponse<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

