package com.flow.gateway.lb;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * 灰度 LoadBalancer 配置 — 注入到每个路由的 LoadBalancer context
 * 通过 @LoadBalancerClients(defaultConfiguration = GrayLoadBalancerConfig.class) 激活
 */
public class GrayLoadBalancerConfig {

    @Bean
    public ReactorServiceInstanceLoadBalancer grayLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {

        String serviceId = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        ObjectProvider<ServiceInstanceListSupplier> supplier =
                loadBalancerClientFactory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class);

        return new GrayLoadBalancer(serviceId, supplier);
    }
}
