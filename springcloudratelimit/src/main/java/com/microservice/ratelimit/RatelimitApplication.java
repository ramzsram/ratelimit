package com.microservice.ratelimit;

import com.microservice.ratelimit.exception.CustomException;
import com.microservice.ratelimit.feign.TokenCheckerServiceFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Optional;

@SpringBootApplication
public class RatelimitApplication {

	public static void main(String[] args) {
		SpringApplication.run(RatelimitApplication.class, args);
	}

	@Bean
	public RedisRateLimiter redisRateLimiter()
	{
		return new RedisRateLimiter(5, 5, 1);
	}

	@Bean
	public KeyResolver keyResolver()
	{
		return exchange -> Optional.ofNullable(exchange.getRequest().getHeaders().get("Authorization"))
				.flatMap(list -> list.stream().findFirst())
				.filter(authHeader -> authHeader.startsWith("Bearer "))
				.map(authHeader -> authHeader.replaceFirst("^Bearer ", ""))
				.map(Mono::just).orElseGet(Mono::empty);
	}
}

@Component
class GatewayWebFilter implements WebFilter
{

	private static final String EMPTY_KEY = "____EMPTY_KEY__";

	@Autowired
	private TokenCheckerServiceFeignClient tokenCheckerServiceFeignClient;

	@Autowired
	private KeyResolver keyResolver;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		return keyResolver.resolve(exchange)
				.defaultIfEmpty(EMPTY_KEY)
				.flatMap(key -> {
							if(EMPTY_KEY.equals(key))
							{
								return exchange.getResponse().writeWith(Mono.error(new CustomException(HttpStatus.FORBIDDEN, "Token not found", new Date())));
							}
							return tokenCheckerServiceFeignClient.checkIfAPIAccessTokenExist(key)
									.doOnError(throwable -> exchange.getResponse().writeWith(Mono.error(new CustomException(HttpStatus.SERVICE_UNAVAILABLE, "ConnectionTimedOut", new Date()))))
									.flatMap(isValid -> {
										if (!isValid) {
											exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
											return exchange.getResponse().writeWith(Mono.error(new CustomException(HttpStatus.UNAUTHORIZED, "Invalid Token", new Date())));
										}
										return chain.filter(exchange);
									});
						}
				);
	}
}
