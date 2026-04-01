package com.flow.gateway.config;

import com.flow.gateway.lb.GrayLoadBalancerConfig;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway 路由配置
 * - API versioning: /api/v1/** and /api/v2/** strip prefix before forwarding
 * - Circuit breaker per route with Resilience4j
 * - Gray release via custom LoadBalancer
 */
@Configuration
@LoadBalancerClients(defaultConfiguration = GrayLoadBalancerConfig.class)
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ── Auth Service ─────────────────────────────────────────────
                .route("auth-v1", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .stripPrefix(2)                                 // strip /api/v1
                                .addRequestHeader("X-API-Version", "v1")
                                .circuitBreaker(c -> c
                                        .setName("auth-cb")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://oaplatform-auth"))

                // ── User Service ─────────────────────────────────────────────
                .route("user-v1", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-API-Version", "v1")
                                .circuitBreaker(c -> c
                                        .setName("user-cb")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://oaplatform-user"))

                // ── Org Service ──────────────────────────────────────────────
                .route("org-v1", r -> r
                        .path("/api/v1/org/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-API-Version", "v1")
                                .circuitBreaker(c -> c
                                        .setName("org-cb")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://oaplatform-org"))

                // ── Workflow Service ─────────────────────────────────────────
                .route("workflow-v1", r -> r
                        .path("/api/v1/workflow/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-API-Version", "v1")
                                .circuitBreaker(c -> c
                                        .setName("workflow-cb")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://oaplatform-workflow"))

                // ── Attendance Service ───────────────────────────────────────
                .route("attendance-v1", r -> r
                        .path("/api/v1/attendance/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-API-Version", "v1")
                                .circuitBreaker(c -> c
                                        .setName("attendance-cb")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://oaplatform-attendance"))

                // ── Message Service ──────────────────────────────────────────
                .route("message-v1", r -> r
                        .path("/api/v1/messages/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-API-Version", "v1")
                                .circuitBreaker(c -> c
                                        .setName("message-cb")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://oaplatform-message"))

                // ── WebSocket (no strip, no version) ────────────────────────
                .route("message-ws", r -> r
                        .path("/ws/**")
                        .uri("lb://oaplatform-message"))

                .build();
    }
}
