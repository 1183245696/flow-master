package com.flow.auth.service;

import com.flow.base.exception.BusinessException;
import com.flow.base.util.RedisTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_PREFIX = "token:refresh:";

    private final AuthenticationManager authenticationManager;
    private final RedisTokenUtil redisTokenUtil;
    private final StringRedisTemplate redisTemplate;

    public TokenPair login(String username, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        OaUserDetails ud = (OaUserDetails) auth.getPrincipal();
        String roles = String.join(",", ud.getRoles());
        String accessToken  = redisTokenUtil.generateAccessToken(ud.getUserId(), username, roles);
        String refreshToken = redisTokenUtil.generateRefreshToken(ud.getUserId());
        return new TokenPair(accessToken, refreshToken, ud.getUserId());
    }

    public TokenPair refresh(String refreshToken) {
        Claims claims;
        try { claims = redisTokenUtil.parseToken(refreshToken); }
        catch (Exception e) { throw new BusinessException("Refresh token 无效"); }
        Long userId = Long.parseLong(claims.getSubject());
        String stored = redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
        if (!refreshToken.equals(stored)) throw new BusinessException("Refresh token 已失效，请重新登录");
        String newAccess  = redisTokenUtil.generateAccessToken(userId, claims.getSubject(), "");
        String newRefresh = redisTokenUtil.generateRefreshToken(userId);
        return new TokenPair(newAccess, newRefresh, userId);
    }

    public void logout(Long userId) {
        redisTokenUtil.invalidateToken(userId);
    }

    @Data
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;
        private final Long userId;
    }
}
