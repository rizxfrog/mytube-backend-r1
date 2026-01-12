package com.mytube.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    public static final long JWT_TTL_MS = 1000L * 60 * 60 * 24 * 2;
    public static final String JWT_KEY = "bEn2xiAnG0mU2TERITERI0YOu5HzH0hE1CwJ1GOnG1tOnG6kAifAwAnchEnG";

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    public static SecretKey getSecretKey() {
        byte[] encodeKey = Base64.getDecoder().decode(JWT_KEY);
        return new SecretKeySpec(encodeKey, 0, encodeKey.length, "HmacSHA256");
    }

    public String createToken(String uid, String role) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expDate = new Date(nowMillis + JWT_TTL_MS);
        SecretKey key = getSecretKey();
        String token = Jwts.builder()
                .setSubject(uid)
                .claim("role", role)
                .signWith(key, SignatureAlgorithm.HS256)
                .setIssuedAt(now)
                .setExpiration(expDate)
                .compact();
        if (stringRedisTemplate != null) {
            stringRedisTemplate.opsForValue().set("token:" + role + ":" + uid, token, JWT_TTL_MS, TimeUnit.MILLISECONDS);
        }
        return token;
    }

    public static Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(getSecretKey()).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getSubject(String token) {
        Claims c = parseClaims(token);
        return c == null ? null : c.getSubject();
    }

    public static String getSubjectFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims == null ? null : claims.getSubject();
    }

    public static String getClaimFromToken(String token, String key) {
        Claims claims = parseClaims(token);
        if (claims == null) {
            return "";
        }
        Object value = claims.get(key);
        return value == null ? "" : value.toString();
    }

    public boolean verifyToken(String token) {
        if (!StringUtils.isNotBlank(token)) {
            return false;
        }
        Claims claims = parseClaims(token);
        if (claims == null) {
            return false;
        }
        String uid = claims.getSubject();
        String role = getClaimFromToken(token, "role");
        if (stringRedisTemplate == null) {
            return true;
        }
        String cached = stringRedisTemplate.opsForValue().get("token:" + role + ":" + uid);
        return StringUtils.equals(token, cached);
    }
}
