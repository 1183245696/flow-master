package com.flow.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.base.response.R;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * 权限过滤器 — @Order(-1) 最先执行
 * 1. 白名单路径直接放行
 * 2. 提取并验证 JWT (签名 + Redis 存活)
 * 3. 从 Redis 读取用户权限集合，校验当前 path+method
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionGlobalFilter implements GlobalFilter, Ordered {

    private static final String AUTH_HEADER       = "Authorization";
    private static final String BEARER_PREFIX      = "Bearer ";
    private static final String USER_PERM_PREFIX   = "perm:user:";
    private static final String TOKEN_PREFIX        = "token:access:";

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${jwt.secret:oaplatform-default-secret-key-must-be-at-least-256-bits-long!}")
    private String jwtSecret;

    /** Paths that skip auth entirely */
    @Value("${gateway.whitelist-paths:/api/v1/auth/login,/api/v1/auth/refresh,/actuator/**,/manage/**}")
    private List<String> whitelistPaths;

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path   = request.getPath().value();
        String method = request.getMethod() != null ? request.getMethod().name() : "";

        // 1. Whitelist check
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 2. Extract token
        String authHeader = request.getHeaders().getFirst(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "未认证，请先登录");
        }
        String token = authHeader.substring(BEARER_PREFIX.length());

        // 3. Validate JWT signature
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("JWT parse failed: {}", e.getMessage());
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Token 无效或已过期");
        }

        Long userId = claims.get("userId", Long.class);

        // 4. Validate token is alive in Redis
        String redisToken = redisTemplate.opsForValue().get(TOKEN_PREFIX + userId);
        if (!token.equals(redisToken)) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "Token 已失效，请重新登录");
        }

        // 5. Check permissions (from Redis permission set)
        String permKey = USER_PERM_PREFIX + userId;
        Set<String> perms = redisTemplate.opsForSet().members(permKey);

        // Permission format: "METHOD:path_pattern", e.g., "GET:/v1/users/**"
        boolean hasPermission = false;
        if (perms != null) {
            // Strip version prefix for internal permission matching
            String internalPath = stripVersionPrefix(path);
            for (String perm : perms) {
                String[] parts = perm.split(":", 2);
                if (parts.length == 2) {
                    String permMethod  = parts[0];
                    String permPattern = parts[1];
                    if ((permMethod.equals("*") || permMethod.equalsIgnoreCase(method))
                            && pathMatcher.match(permPattern, internalPath)) {
                        hasPermission = true;
                        break;
                    }
                }
            }
        }

        if (!hasPermission) {
            log.warn("User {} lacks permission for {} {}", userId, method, path);
            return writeError(exchange, HttpStatus.FORBIDDEN, "无接口请求权限");
        }

        // 6. Forward userId/username in header for downstream services
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", claims.getSubject())
                .header("X-API-Version", extractVersion(path))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isWhitelisted(String path) {
        return whitelistPaths.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    private String stripVersionPrefix(String path) {
        if (path.startsWith("/api/v")) {
            int thirdSlash = path.indexOf('/', 5);
            return thirdSlash > 0 ? path.substring(thirdSlash) : path;
        }
        return path;
    }

    private String extractVersion(String path) {
        if (path.startsWith("/api/v1/")) return "v1";
        if (path.startsWith("/api/v2/")) return "v2";
        return "v1";
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(R.fail(status.value(), message));
        } catch (JsonProcessingException e) {
            bytes = ("{\"code\":" + status.value() + ",\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
