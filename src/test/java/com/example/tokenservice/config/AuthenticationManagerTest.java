package com.example.tokenservice.config;

import com.example.tokenservice.data.entity.Role;
import com.example.tokenservice.data.entity.User;
import com.example.tokenservice.testUtil.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class AuthenticationManagerTest {
    SecurityConfig securityConfig = new SecurityConfig();
    @Mock
    ReactiveUserDetailsService userDetailsService;
    @Mock
    PasswordEncoder passwordEncoder;
    ReactiveAuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        authenticationManager = securityConfig.authenticationManager(userDetailsService, passwordEncoder);
    }

    @Test
    void testAuthenticate_withNonExistentUser_throws() {
        String nonExistentUsername = "mystery-man";

        given(userDetailsService.findByUsername(nonExistentUsername))
                .willReturn(Mono.error(new UsernameNotFoundException("No such user")));

        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(nonExistentUsername, null);

        StepVerifier.create(authenticationManager.authenticate(authentication))
                .expectError(UsernameNotFoundException.class)
                .verify();
    }

    @Test
    void testAuthenticate_withExistingUser_butInvalidCredentials_stillThrows() {
        String realPassword = "real_password";

        User user = new User();
        user.setUsername("minnie_m");
        user.setPassword(realPassword);

        given(userDetailsService.findByUsername(user.getUsername()))
                .willReturn(Mono.just(user));

        String fakePassword = "fake_password";
        given(passwordEncoder.matches(fakePassword, realPassword)).willReturn(false);

        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(user.getUsername(), fakePassword);

        StepVerifier.create(authenticationManager.authenticate(authentication))
                .expectError(AuthenticationException.class)
                .verify();
    }

    @ParameterizedTest
    @MethodSource("accountInvalidators")
    void testAuthenticate_withExistingUser_butIllegalAccountState_throws(Consumer<User> accountInvalidator) {
        String realPassword = "real_password";

        User user = new User();
        user.setUsername("minnie_m");
        user.setPassword(realPassword);

        String encodedPassword = "#nc0ded_pa$$word";
        List<Role> someRoles = Stream.of("role", "another_role")
                .map(Role::new).toList();
        given(userDetailsService.findByUsername(user.getUsername())).willReturn(Mono.just(
                UserUtil.cloneAndMutate(user, u -> {
                    u.setPassword(encodedPassword);
                    someRoles.forEach(u::addRole);
                    accountInvalidator.accept(u);
                }))
        );

        Authentication authentication =
                UsernamePasswordAuthenticationToken.unauthenticated(user.getUsername(), user.getPassword());
        StepVerifier.create(authenticationManager.authenticate(authentication))
                .expectError(AccountStatusException.class)
                .verify();
    }

    static List<Consumer<User>> accountInvalidators() {
        return List.of(
                u -> u.setEnabled(false)
        );
    }

    @Test
    void testAuthenticate_withExistingUser_withValidCredentials_authenticates() {
        String realPassword = "real_password";

        User user = new User();
        user.setUsername("minnie_m");
        user.setPassword(realPassword);

        String encodedPassword = "#nc0ded_pa$$word";
        List<Role> someRoles = Stream.of("role", "another_role")
                .map(Role::new).toList();
        given(userDetailsService.findByUsername(user.getUsername())).willReturn(Mono.just(
                UserUtil.cloneAndMutate(user, u -> {
                    u.setPassword(encodedPassword);
                    someRoles.forEach(u::addRole);
                }))
        );

        given(passwordEncoder.matches(realPassword, encodedPassword)).willReturn(true);

        Authentication expectedAuthentication = UsernamePasswordAuthenticationToken.authenticated(
                user, encodedPassword, someRoles
        );

        Authentication passedAuthentication =
                UsernamePasswordAuthenticationToken.unauthenticated(user.getUsername(), user.getPassword());
        StepVerifier.create(authenticationManager.authenticate(passedAuthentication))
                .expectNext(expectedAuthentication)
                .verifyComplete();
    }
}
