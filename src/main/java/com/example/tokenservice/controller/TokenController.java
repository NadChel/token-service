package com.example.tokenservice.controller;

import com.example.tokenservice.data.dto.UserDto;
import com.example.tokenservice.data.entity.User;
import com.example.tokenservice.mapper.UserMapper;
import com.example.tokenservice.service.token.TokenService;
import com.example.tokenservice.service.user.UserService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TokenController {
    private final UserService userService;
    private final TokenService tokenService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public TokenController(UserService userService,
                           TokenService tokenService,
                           ReactiveAuthenticationManager authenticationManager,
                           UserMapper userMapper) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
    }

    @PostMapping("/signup")
    public Mono<String> signUp(@RequestBody UserDto userDto) {
        User userToSave = userMapper.toUser(userDto);
        User savedUser = userService.setDefaultsAndAndSave(userToSave);
        UsernamePasswordAuthenticationToken upat = UsernamePasswordAuthenticationToken.authenticated(
                savedUser.getUsername(), savedUser.getPassword(), savedUser.getAuthorities()
        );
        return tokenService.generateTokenFor(upat);
    }

    @PostMapping("/login")
    public Mono<String> logIn(@RequestBody User user) {
        UsernamePasswordAuthenticationToken upat = UsernamePasswordAuthenticationToken.unauthenticated(
                user.getUsername(), user.getPassword());
        return authenticationManager.authenticate(upat)
                .flatMap(tokenService::generateTokenFor);
    }
}
