package com.example.tokenservice.config;

import com.example.tokenservice.handler.TokenHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

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
}
