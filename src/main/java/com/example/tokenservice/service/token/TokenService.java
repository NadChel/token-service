package com.example.tokenservice.service.token;

import org.springframework.security.core.Authentication;

public interface TokenService {
    String generateTokenFor(Authentication authentication);
}
