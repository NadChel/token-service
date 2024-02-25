package com.example.tokenservice.handler;

import com.example.tokenservice.data.dto.UserDto;
import com.example.tokenservice.data.entity.Role;
import com.example.tokenservice.data.entity.User;
import com.example.tokenservice.mapper.UserMapper;
import com.example.tokenservice.service.token.TokenService;
import com.example.tokenservice.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BasicTokenHandlerTest {
    @Mock
    UserService userService;
    @Mock
    TokenService tokenService;
    @Mock
    ReactiveAuthenticationManager authenticationManager;
    @Mock
    UserMapper userMapper;
    @InjectMocks
    BasicTokenHandler tokenHandler;

    @Test
    void signUp() {
        UserDto userDto = new UserDto();
        userDto.setUsername("goofus_d");
        userDto.setPassword("12345");

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());

        given(userMapper.toUser(userDto)).willReturn(user);

        given(userService.encodePassword(user)).willReturn(cloneAndMutate(user, u -> u.setPassword("encoded_pass")));

        given(userService.addDefaultRoles(user)).willReturn(cloneAndMutate(user, u -> u.addRole(new Role(Role.USER))));

        UUID assignedId = UUID.randomUUID();
        given(userService.save(user)).willReturn(cloneAndMutate(user, u -> u.setId(assignedId)));

        String jwt = "just.imagine.its.a.JWT";
        given(tokenService.generateTokenFor(toAuthenticatedUpat(user))).willReturn(jwt);

        MockServerRequest request = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/signup"))
                .body(Mono.just(userDto));

        StepVerifier.create(tokenHandler.signUp(request))
                .assertNext(response -> responseChecksOut(response, HttpStatus.CREATED, jwt))
                .verifyComplete();
    }

    private User cloneAndMutate(User user, Consumer<User> mutator) {
        User userCopy = clone(user);
        mutator.accept(userCopy);
        return userCopy;
    }

    private User clone(User user) {
        User userCopy = new User();
        userCopy.setUsername(user.getUsername());
        userCopy.setPassword(user.getPassword());
        userCopy.setId(user.getId());
        userCopy.setEnabled(user.getEnabled());
        userCopy.setAuthorities(user.getAuthorities());
        return userCopy;
    }

    private UsernamePasswordAuthenticationToken toAuthenticatedUpat(User user) {
        return UsernamePasswordAuthenticationToken.authenticated(
                user.getUsername(), user.getPassword(), user.getAuthorities());
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue", "SameParameterValue"})
    private <T> void responseChecksOut(ServerResponse response, HttpStatus expectedStatus, T expectedBody) {
        assertSoftly(soft -> {
            soft.assertThat(response.statusCode()).isEqualTo(expectedStatus);

            Mono<T> body = (Mono<T>) ReflectionTestUtils.getField(response, "entity");
            StepVerifier.create(body)
                    .expectNext(expectedBody)
                    .verifyComplete();
        });
    }

    @Test
    void logIn() {
        UserDto userDto = new UserDto();
        userDto.setUsername("goofus_d");
        userDto.setPassword("12345");

        Authentication authentication = toUnauthenticatedUpat(userDto);
        given(authenticationManager.authenticate(authentication)).willReturn(Mono.just(authentication));

        String jwt = "fake.jw.t";
        given(tokenService.generateTokenFor(authentication)).willReturn(jwt);

        MockServerRequest request = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/login"))
                .body(Mono.just(userDto));

        StepVerifier.create(tokenHandler.logIn(request))
                .assertNext(response -> responseChecksOut(response, HttpStatus.OK, jwt))
                .verifyComplete();
    }

    private UsernamePasswordAuthenticationToken toUnauthenticatedUpat(UserDto userDto) {
        return UsernamePasswordAuthenticationToken.unauthenticated(userDto.getUsername(), userDto.getPassword());
    }
}