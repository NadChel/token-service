package com.example.tokenservice.service.token;

import com.example.tokenservice.data.constant.JWT;
import com.example.tokenservice.data.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {
    TokenService tokenService = new JwtTokenService();

    @Test
    void testGenerateTokenFor() {
        String principal = "daisy_d";
        List<GrantedAuthority> roles = List.of(new SimpleGrantedAuthority(Role.USER));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null, roles);

        String jwt = tokenService.generateTokenFor(authentication);

        Claims jwtClaims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(JWT.KEY.getBytes()))
                .build()
                .parseClaimsJws(jwt)
                .getBody();

        assertThat(jwtClaims.getSubject()).isEqualTo(principal);
        assertThat(jwtClaims.get(JWT.ROLES))
                .extracting(List.class::cast)
                .asList()
                .containsExactlyInAnyOrderElementsOf(roles.stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());
    }
}