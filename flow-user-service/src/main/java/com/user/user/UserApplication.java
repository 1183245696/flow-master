package com.user.user;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication(scanBasePackages = {"com.flow.user","com.flow.base"})
@EnableDiscoveryClient
@EnableDubbo
public class UserApplication {
    public static void main(String[] args) { SpringApplication.run(UserApplication.class, args); }
}
