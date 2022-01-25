package com.microservice.ratelimit.exception;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;


@Component
@Order(-2)
public class ExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(ex);
        DataBuffer buf = exchange.getResponse().bufferFactory().wrap(exceptionResponse.getExceptionResponse().getBytes(StandardCharsets.UTF_8));
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(exceptionResponse.getCode()));
        return exchange.getResponse().writeWith(Mono.just(buf));
    }
}


