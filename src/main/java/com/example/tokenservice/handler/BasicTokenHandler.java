package com.example.tokenservice.handler;

import com.example.tokenservice.data.dto.UserDto;
import com.example.tokenservice.data.entity.User;
import com.example.tokenservice.mapper.UserMapper;
import com.example.tokenservice.service.token.TokenService;
import com.example.tokenservice.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class BasicTokenHandler implements TokenHandler {
    private final UserService userService;
    private final TokenService tokenService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public BasicTokenHandler(UserService userService,
                             TokenService tokenService,
                             ReactiveAuthenticationManager authenticationManager,
                             UserMapper userMapper) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
    }

    @Override
    @Operation(requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserDto.class)), required = true))
    @ApiResponse(responseCode = "201", content = @Content(mediaType = "text/plain"))
    public Mono<ServerResponse> signUp(ServerRequest request) {
        return request.bodyToMono(UserDto.class)
                .map(userMapper::toUser)
                .map(userService::encodePassword)
                .map(userService::addDefaultRoles)
                .map(userService::save)
                .map(this::toAuthenticatedUpat)
                .map(tokenService::generateTokenFor)
                .transform(jwt -> ServerResponse.status(HttpStatus.CREATED).body(jwt, String.class));
    }

    private UsernamePasswordAuthenticationToken toAuthenticatedUpat(User user) {
        return UsernamePasswordAuthenticationToken.authenticated(
                user.getUsername(), user.getPassword(), user.getAuthorities());
    }

    @Override
    @Operation(requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserDto.class)), required = true))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "text/plain"))
    public Mono<ServerResponse> logIn(ServerRequest request) {
        return request.bodyToMono(UserDto.class)
                .map(this::toUnauthenticatedUpat)
                .flatMap(authenticationManager::authenticate)
                .map(tokenService::generateTokenFor)
                .transform(jwt -> ServerResponse.status(HttpStatus.OK).body(jwt, String.class));
    }

    private UsernamePasswordAuthenticationToken toUnauthenticatedUpat(UserDto userDto) {
        return UsernamePasswordAuthenticationToken.unauthenticated(
                userDto.getUsername(), userDto.getPassword());
    }
}
