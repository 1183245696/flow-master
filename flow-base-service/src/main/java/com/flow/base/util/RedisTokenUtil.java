package com.flow.base.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Token 工具类
 * - 生成/验证 JWT
 * - Redis 存储 access/refresh token (TTL: 8h / 7d)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTokenUtil {

    private static final String ACCESS_TOKEN_PREFIX  = "token:access:";
    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";
    private static final Duration ACCESS_TOKEN_TTL   = Duration.ofHours(8);
    private static final Duration REFRESH_TOKEN_TTL  = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.secret:oaplatform-default-secret-key-must-be-at-least-256-bits-long!}")
    private String jwtSecret;

    // ---- JWT ----

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username, String roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);

        String token = Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_TTL.toMillis()))
                .signWith(getSigningKey())
                .compact();

        // Store in Redis
        redisTemplate.opsForValue().set(ACCESS_TOKEN_PREFIX + userId, token, ACCESS_TOKEN_TTL);
        return token;
    }

    public String generateRefreshToken(Long userId) {
        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_TTL.toMillis()))
                .signWith(getSigningKey())
                .compact();

        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + userId, token, REFRESH_TOKEN_TTL);
        return token;
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateAccessToken(Long userId, String token) {
        try {
            Claims claims = parseToken(token);
            String redisToken = redisTemplate.opsForValue().get(ACCESS_TOKEN_PREFIX + userId);
            return token.equals(redisToken) && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public void invalidateToken(Long userId) {
        redisTemplate.delete(ACCESS_TOKEN_PREFIX + userId);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }
}
