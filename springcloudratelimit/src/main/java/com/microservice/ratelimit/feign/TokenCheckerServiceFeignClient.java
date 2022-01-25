package com.microservice.ratelimit.feign;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;


@ReactiveFeignClient(name = "token-check-service", url = "${tokencheckservice.url}")
public interface TokenCheckerServiceFeignClient {

    @PostMapping(value = "${apiToken.check.url}")
    public Mono<Boolean> checkIfAPIAccessTokenExist(@RequestBody String token);
}
