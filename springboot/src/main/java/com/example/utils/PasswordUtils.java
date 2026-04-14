package com.example.utils;

import org.springframework.util.DigestUtils;
import java.util.Random;

/**
 * 密码加密工具类
 */
public class PasswordUtils {

    // 生成8位随机盐值（数字）
    public static String generateSalt() {
        Random random = new Random();
        StringBuilder salt = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            salt.append(random.nextInt(10));
        }
        return salt.toString();
    }

    // MD5加盐加密：内容 + 盐值 拼接后MD5
    public static String encryptPassword(String content, String salt) {
        String raw = content + salt;
        return DigestUtils.md5DigestAsHex(raw.getBytes());
    }

    // 纯MD5加密（无盐）
    public static String pureMD5(String content) {
        return DigestUtils.md5DigestAsHex(content.getBytes());
    }
}