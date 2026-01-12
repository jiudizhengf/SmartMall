package org.example.smartmallbackend.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.token-head}")
    private String tokenHead;

    @Value("${jwt.header}")
    private String header;

    /**
     * 生成 Token
     */
    public String createToken(Long userId, String username) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);

        // 签发时间
        DateTime now = DateTime.now();
        // 过期时间
        DateTime newTime = now.offsetNew(DateField.SECOND, expiration.intValue());

        payload.put(JWTPayload.ISSUED_AT, now); // 签发时间
        payload.put(JWTPayload.EXPIRES_AT, newTime); // 过期时间
        payload.put(JWTPayload.NOT_BEFORE, now); // 生效时间

        return JWTUtil.createToken(payload, secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解析 Token 获取负载信息
     */
    public JWT parseToken(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        // 去除 Bearer 前缀
        if (token.startsWith(tokenHead)) {
            token = token.substring(tokenHead.length());
        }
        try {
            return JWTUtil.parseToken(token);
        } catch (Exception e) {
            log.error("JWT解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 校验 Token 是否有效
     * 1. 签名验证
     * 2. 是否过期
     */
    public boolean validateToken(String token) {
        JWT jwt = parseToken(token);
        if (jwt == null) {
            return false;
        }
        try {
            // 验证签名
            boolean verifyKey = jwt.setKey(secret.getBytes(StandardCharsets.UTF_8)).verify();
            // 验证时间
            boolean verifyTime = jwt.validate(0);
            return verifyKey && verifyTime;
        } catch (Exception e) {
            log.error("JWT校验异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 Token 中获取 UserID
     */
    public Long getUserId(String token) {
        JWT jwt = parseToken(token);
        if (jwt == null) return null;
        try {
            Object userId = jwt.getPayload("userId");
            return Long.valueOf(userId.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 HTTP 请求中直接获取 UserID
     * 这是一个非常实用的辅助方法
     */
    public Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader(header);
        if (StrUtil.isBlank(token)) {
            return null;
        }
        return getUserId(token);
    }
}
