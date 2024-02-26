package com.example.tokenservice.config;

import com.example.tokenservice.handler.TokenHandler;
import com.example.tokenservice.util.WebFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebFilter;

@Configuration
public class RouterConfig {
    private final TokenHandler tokenHandler;

    public RouterConfig(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> signUpRoute() {
        return RouterFunctions.route()
                .POST("/signup", tokenHandler::signUp)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> logInRoute() {
        return RouterFunctions.route()
                .POST("/login", tokenHandler::logIn)
                .build();
    }

    @Bean
    public WebFilter authenticationExceptionToUnauthorizedFilter() {
        return WebFilterFactory.exceptionHandlingWebFilter(AuthenticationException.class, HttpStatus.UNAUTHORIZED);
    }

    @Bean
    public WebFilter duplicateKeyExceptionToConflictFilter() {
        return WebFilterFactory.exceptionHandlingWebFilter(DuplicateKeyException.class, HttpStatus.CONFLICT);
    }
}
