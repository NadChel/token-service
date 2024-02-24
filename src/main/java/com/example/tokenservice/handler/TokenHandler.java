package com.example.tokenservice.handler;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface TokenHandler {
    Mono<ServerResponse> signUp(ServerRequest request);

    Mono<ServerResponse> logIn(ServerRequest request);
}
