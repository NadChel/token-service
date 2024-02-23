package com.example.tokenservice.config;

import com.example.tokenservice.handler.TokenHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

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
    public WebFilter badCredentialsToBadRequest() {
        return (exchange, next) -> next.filter(exchange)
                .onErrorResume(BadCredentialsException.class, e -> {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    DefaultDataBufferFactory defaultDataBufferFactory = new DefaultDataBufferFactory();
                    DataBuffer dataBuffer = defaultDataBufferFactory.wrap(e.getMessage().getBytes());
                    return response.writeWith(Mono.fromSupplier(() -> dataBuffer));
                });
    }
}
