package com.example.tokenservice.service.token;

import com.example.tokenservice.data.constant.JWT;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
public class JwtTokenService implements TokenService {
    @Override
    public String generateTokenFor(Authentication authentication) {
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(JWT.ROLES, authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .setExpiration(Date.valueOf(LocalDate.now()
                        .plusDays(DayOfWeek.values().length)))
                .signWith(Keys.hmacShaKeyFor(JWT.KEY.getBytes()))
                .compact();
    }
}
