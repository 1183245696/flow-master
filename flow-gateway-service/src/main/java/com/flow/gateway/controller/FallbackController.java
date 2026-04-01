package com.flow.gateway.controller;

import com.flow.base.response.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @RequestMapping("/fallback")
    public Mono<R<Void>> fallback() {
        return Mono.just(R.fail(503, "服务暂时不可用，请稍后重试"));
    }
}
