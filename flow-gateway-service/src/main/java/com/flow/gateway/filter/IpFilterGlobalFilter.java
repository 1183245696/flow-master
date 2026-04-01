package com.flow.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.base.response.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * IP 黑白名单过滤器
 * - 从 Redis gateway:whitelist / gateway:blacklist 加载，每60秒刷新
 * - 白名单非空：只允许白名单 IP，其余 403
 * - 白名单为空：黑名单中的 IP 403，其余放行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpFilterGlobalFilter implements GlobalFilter, Ordered {

    private static final String REDIS_WHITELIST_KEY = "gateway:whitelist";
    private static final String REDIS_BLACKLIST_KEY = "gateway:blacklist";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // Thread-safe local caches refreshed every 60s
    private final Set<String> whitelistCache = new CopyOnWriteArraySet<>();
    private final Set<String> blacklistCache = new CopyOnWriteArraySet<>();

    @Override
    public int getOrder() {
        return -200; // Run before permission filter
    }

    @Scheduled(fixedRate = 60_000)
    public void refreshIpLists() {
        Set<String> wl = redisTemplate.opsForSet().members(REDIS_WHITELIST_KEY);
        Set<String> bl = redisTemplate.opsForSet().members(REDIS_BLACKLIST_KEY);
        whitelistCache.clear();
        blacklistCache.clear();
        if (wl != null) {
            whitelistCache.addAll(wl);
        }
        if (bl != null) {
            blacklistCache.addAll(bl);
        }
        log.debug("IP lists refreshed — whitelist: {}, blacklist: {}", whitelistCache.size(), blacklistCache.size());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = resolveClientIp(exchange.getRequest());

        if (!whitelistCache.isEmpty()) {
            // Whitelist mode: only allow listed IPs
            if (!whitelistCache.contains(clientIp)) {
                log.warn("IP [{}] blocked by whitelist policy", clientIp);
                return blockRequest(exchange, "IP 地址不在白名单中");
            }
        } else {
            // Blacklist mode
            if (blacklistCache.contains(clientIp)) {
                log.warn("IP [{}] blocked by blacklist policy", clientIp);
                return blockRequest(exchange, "IP 地址已被封禁");
            }
        }
        return chain.filter(exchange);
    }

    private String resolveClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    private Mono<Void> blockRequest(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(R.fail(403, message));
        } catch (JsonProcessingException e) {
            bytes = ("{\"code\":403,\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
