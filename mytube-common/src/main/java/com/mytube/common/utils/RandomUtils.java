package com.mytube.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class RandomUtils {
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    public static String createHex32(long timestamp, long userId) {
        try {
            String source = timestamp + "-" + userId;

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(source.getBytes(StandardCharsets.UTF_8));

            // 取前 16 字节（128 bit）
            byte[] shortHash = new byte[16];
            System.arraycopy(hash, 0, shortHash, 0, 16);

            return bytesToHex(shortHash);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//    public static String generateVideo
}
