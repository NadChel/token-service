package com.example.tokenservice.service.token;

import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

public interface TokenService {
    Mono<String> generateTokenFor(Authentication authentication);
}
