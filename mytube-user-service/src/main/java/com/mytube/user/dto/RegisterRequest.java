package com.mytube.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username too long")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(max = 50, message = "Password too long")
    private String password;

    @NotBlank(message = "Password is required")
    @Size(max = 50, message = "Password too long")
    private String confirmedPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmedPassword() {
        return confirmedPassword;
    }

    public void setConfirmedPassword(String confirmedPassword) {
        this.confirmedPassword = confirmedPassword;
    }
}
