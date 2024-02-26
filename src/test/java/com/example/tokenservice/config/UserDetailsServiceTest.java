package com.example.tokenservice.config;

import com.example.tokenservice.data.entity.User;
import com.example.tokenservice.repository.UserRepository;
import com.example.tokenservice.testUtil.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceTest {
    SecurityConfig securityConfig = new SecurityConfig();
    @Mock
    UserRepository userRepository;
    ReactiveUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = securityConfig.userDetailsService(userRepository);
    }

    @Test
    void testUserDetailsService_ifUserFetched_returnsItBack() {
        User user = new User();
        user.setUsername("scrooge_m");
        user.setPassword("password");

        given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));

        StepVerifier.create(userDetailsService.findByUsername(user.getUsername()))
                .expectNextMatches(u -> UserUtil.haveEqualFields(u, user))
                .verifyComplete();
    }

    @Test
    void testUserDetailsService_ifUserNotFetched_throwsUserNotFoundException() {
        String unknownUsername = "Unknown_user";

        given(userRepository.findByUsername(unknownUsername)).willReturn(Optional.empty());

        StepVerifier.create(userDetailsService.findByUsername(unknownUsername))
                .expectError(UsernameNotFoundException.class)
                .verify();
    }
}