package com.example.tokenservice.handler;

import com.example.tokenservice.data.dto.UserDto;
import com.example.tokenservice.data.entity.Role;
import com.example.tokenservice.data.entity.User;
import com.example.tokenservice.mapper.UserMapper;
import com.example.tokenservice.service.token.TokenService;
import com.example.tokenservice.service.user.UserService;
import com.example.tokenservice.testUtil.ServerResponseUtil;
import com.example.tokenservice.testUtil.UserUtil;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;
import java.util.UUID;

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
    void testSignUp() {
        UserDto userDto = new UserDto();
        userDto.setUsername("goofus_d");
        userDto.setPassword("12345");

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());

        given(userMapper.toUser(userDto)).willReturn(user);

        User userAfterPasswordEncoding = UserUtil.cloneAndMutate(user, u -> u.setPassword("encoded_pass"));
        given(userService.encodePassword(user)).willReturn(userAfterPasswordEncoding);

        User userWithDefaultRoles = UserUtil.cloneAndMutate(userAfterPasswordEncoding, u -> u.addRole(new Role(Role.USER)));
        given(userService.addDefaultRoles(userAfterPasswordEncoding)).willReturn(userWithDefaultRoles);

        UUID assignedId = UUID.randomUUID();
        User persistedUser = UserUtil.cloneAndMutate(userWithDefaultRoles, u -> u.setId(assignedId));
        given(userService.save(userWithDefaultRoles)).willReturn(persistedUser);

        String jwt = "just.imagine.its.a.JWT";
        given(tokenService.generateTokenFor(toAuthenticatedUpat(persistedUser))).willReturn(jwt);

        MockServerRequest request = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/signup"))
                .body(Mono.just(userDto));

        StepVerifier.create(tokenHandler.signUp(request))
                .assertNext(response -> ServerResponseUtil.responseChecksOut(response, HttpStatus.CREATED, jwt))
                .verifyComplete();
    }

    private UsernamePasswordAuthenticationToken toAuthenticatedUpat(User user) {
        return UsernamePasswordAuthenticationToken.authenticated(
                user.getUsername(), user.getPassword(), user.getAuthorities());
    }

    @Test
    void testLogIn() {
        UserDto userDto = new UserDto();
        userDto.setUsername("goofus_d");
        userDto.setPassword("12345");

        Authentication passedAuthentication = toUnauthenticatedUpat(userDto);
        Authentication returnedAuthentication = UsernamePasswordAuthenticationToken.authenticated(
                userDto, userDto.getUsername(), List.of(new Role("some_default_role")
                ));
        given(authenticationManager.authenticate(passedAuthentication)).willReturn(Mono.just(returnedAuthentication));

        String jwt = "json.web.token";
        given(tokenService.generateTokenFor(returnedAuthentication)).willReturn(jwt);

        MockServerRequest request = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/login"))
                .body(Mono.just(userDto));

        StepVerifier.create(tokenHandler.logIn(request))
                .assertNext(response -> ServerResponseUtil.responseChecksOut(response, HttpStatus.OK, jwt))
                .verifyComplete();
    }

    private UsernamePasswordAuthenticationToken toUnauthenticatedUpat(UserDto userDto) {
        return UsernamePasswordAuthenticationToken.unauthenticated(userDto.getUsername(), userDto.getPassword());
    }
}