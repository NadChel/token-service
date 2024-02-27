package com.example.tokenservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Token Service API", version = "1.0",
        description = "Basic authentication server issuing JSON Web Tokens"))
public class TokenServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TokenServiceApplication.class, args);
    }
}
