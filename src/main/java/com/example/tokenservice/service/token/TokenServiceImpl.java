package com.example.tokenservice.service.token;

import com.example.tokenservice.data.constant.JWT;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class TokenServiceImpl implements TokenService {
    @Override
    public Mono<String> generateTokenFor(Authentication authentication) {
        return Mono.fromCallable(Jwts::builder)
                .map(b -> b.setSubject(authentication.getName()))
                .map(b -> b.claim(JWT.ROLES, authentication.getAuthorities()))
                .map(b -> b.setExpiration(Date.valueOf(LocalDate.now().plusDays(DayOfWeek.values().length))))
                .map(b -> b.signWith(Keys.hmacShaKeyFor(JWT.KEY.getBytes())))
                .map(JwtBuilder::compact);
    }
}
