package com.mytube.common.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

// 从当前 HTTP 请求头读取用户的 Authorization 字段，解析 JWT，取出 sub （用户 ID）
@Component
public class CurrentUser {
    public Long requireUserId() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (!(attrs instanceof ServletRequestAttributes)) return null;
            HttpServletRequest req = ((ServletRequestAttributes) attrs).getRequest();
            String auth = req.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) return null;
            String token = auth.substring(7);
            String sub = JwtUtil.getSubject(token);
            return sub == null ? null : Long.valueOf(sub);
        } catch (Exception e) {
            return null;
        }
    }

    public Long getUserId() {
        return requireUserId();
    }

    public String getRole() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (!(attrs instanceof ServletRequestAttributes)) return null;
            HttpServletRequest req = ((ServletRequestAttributes) attrs).getRequest();
            String auth = req.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) return null;
            String token = auth.substring(7);
            String role = JwtUtil.getClaimFromToken(token, "role");
            return role == null || role.isEmpty() ? null : role;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAdmin() {
        String role = getRole();
        return role != null && role.equalsIgnoreCase("admin");
    }
}
