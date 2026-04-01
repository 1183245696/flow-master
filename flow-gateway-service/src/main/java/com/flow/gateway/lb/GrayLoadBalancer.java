package com.flow.gateway.lb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 灰度发布负载均衡器
 * - 请求头 X-Gray: true → 路由到 Nacos metadata 中 gray=true 的实例
 * - 无灰度标记 → 路由到非灰度实例
 */
@Slf4j
public class GrayLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final String serviceId;
    private final ObjectProvider<ServiceInstanceListSupplier> supplierObjectProvider;

    public GrayLoadBalancer(String serviceId,
                            ObjectProvider<ServiceInstanceListSupplier> supplierObjectProvider) {
        this.serviceId = serviceId;
        this.supplierObjectProvider = supplierObjectProvider;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = supplierObjectProvider
                .getIfAvailable();
        if (supplier == null) {
            return Mono.just(new EmptyResponse());
        }

        return supplier.get(request).next()
                .map(instances -> selectInstance(instances, request));
    }

    private Response<ServiceInstance> selectInstance(List<ServiceInstance> instances,
                                                      Request<?> request) {
        if (instances.isEmpty()) {
            log.warn("No instances available for service: {}", serviceId);
            return new EmptyResponse();
        }

        boolean isGray = isGrayRequest(request);

        List<ServiceInstance> candidates = instances.stream()
                .filter(i -> {
                    String grayMeta = i.getMetadata().get("gray");
                    boolean instanceIsGray = "true".equalsIgnoreCase(grayMeta);
                    return isGray == instanceIsGray;
                })
                .collect(Collectors.toList());

        // Fallback: if no matching instances, use all
        if (candidates.isEmpty()) {
            candidates = instances;
            log.debug("No {} instances for {}, falling back to all", isGray ? "gray" : "normal", serviceId);
        }

        // Random selection
        ServiceInstance chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        log.debug("Routing to instance: {}:{} [gray={}]",
                chosen.getHost(), chosen.getPort(),
                chosen.getMetadata().get("gray"));
        return new DefaultResponse(chosen);
    }

    @SuppressWarnings("unchecked")
    private boolean isGrayRequest(Request<?> request) {
        try {
            Object context = request.getContext();
            if (context instanceof org.springframework.cloud.client.loadbalancer.RequestDataContext rdc) {
                String grayHeader = rdc.getClientRequest().getHeaders()
                        .getFirst("X-Gray");
                return "true".equalsIgnoreCase(grayHeader);
            }
        } catch (Exception e) {
            log.debug("Could not read X-Gray header from request context", e);
        }
        return false;
    }
}
