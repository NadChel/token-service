package com.example.tokenservice.util;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.web.server.MatcherSecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

public class WebFilterFactory {
    private WebFilterFactory() {
    }

    public static WebFilterChainProxy noOpWebFilterChainProxy() {
        return new WebFilterChainProxy(
                new MatcherSecurityWebFilterChain(
                        exchange -> ServerWebExchangeMatcher.MatchResult.match(),
                        List.of(noOpWebFilter())
                ));
    }

    public static WebFilter noOpWebFilter() {
        return (exchange, chain) -> chain.filter(exchange);
    }

    public static WebFilter exceptionHandlingWebFilter(Class<? extends Throwable> throwableClass,
                                                       HttpStatus status) {
        return exceptionHandlingWebFilter(throwableClass, status, Throwable::getMessage);
    }

    public static <T extends Throwable> WebFilter exceptionHandlingWebFilter(Class<T> throwableClass,
                                                                             HttpStatus status,
                                                                             Function<T, String> responseBodyValueFunction) {
        return (exchange, chain) -> chain.filter(exchange)
                .onErrorResume(throwableClass,
                        t -> writeResponse(exchange, status, responseBodyValueFunction.apply(t)));
    }

    private static Mono<Void> writeResponse(ServerWebExchange exchange,
                                            HttpStatus status,
                                            String responseBody) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance
                .wrap(responseBody.getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }
}
