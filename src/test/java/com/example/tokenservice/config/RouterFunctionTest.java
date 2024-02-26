package com.example.tokenservice.config;

import com.example.tokenservice.handler.TokenHandler;
import com.example.tokenservice.testUtil.ServerResponseUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RouterFunctionTest {
    @Mock
    TokenHandler tokenHandler;
    @InjectMocks
    RouterConfig routerConfig;

    @ParameterizedTest
    @MethodSource("nonMatchingSignupRequests")
    void signUpRoute_withWithNonMatchingRequests(ServerRequest nonMatchingRequest) {
        RouterFunction<ServerResponse> signUpRoute = routerConfig.signUpRoute();
        StepVerifier.create(signUpRoute.route(nonMatchingRequest))
                .verifyComplete();
    }

    static List<ServerRequest> nonMatchingSignupRequests() {
        return List.of(
                MockServerRequest.builder()
                        .method(HttpMethod.DELETE)
                        .uri(URI.create("/signup"))
                        .exchange(MockServerWebExchange.from(MockServerHttpRequest.delete("/signup")))
                        .build(),
                MockServerRequest.builder()
                        .method(HttpMethod.POST)
                        .uri(URI.create("/wrong-signup"))
                        .exchange(MockServerWebExchange.from(MockServerHttpRequest.post("/wrong-signup")))
                        .build()
        );
    }

    @Test
    void signUpRoute_withMatchingRequest() {
        ServerRequest matchingRequest = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/signup"))
                .exchange(MockServerWebExchange.from(MockServerHttpRequest.post("/signup")))
                .build();

        HttpStatus status = HttpStatus.CREATED;
        String bodyValue = "j.w.token";
        Mono<ServerResponse> response = ServerResponse.status(status)
                .body(Mono.just(bodyValue), String.class);
        given(tokenHandler.signUp(matchingRequest)).willReturn(response);

        RouterFunction<ServerResponse> signUpRoute = routerConfig.signUpRoute();
        StepVerifier.create(signUpRoute.route(matchingRequest))
                .assertNext(handlerFunction -> StepVerifier.create(handlerFunction.handle(matchingRequest))
                        .assertNext(r -> ServerResponseUtil.responseChecksOut(r, status, bodyValue))
                        .verifyComplete())
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("nonMatchingLoginRequests")
    void testLogInRoute_withWithNonMatchingRequests(ServerRequest nonMatchingRequest) {
        RouterFunction<ServerResponse> signUpRoute = routerConfig.logInRoute();
        StepVerifier.create(signUpRoute.route(nonMatchingRequest))
                .verifyComplete();
    }

    static List<ServerRequest> nonMatchingLoginRequests() {
        return List.of(
                MockServerRequest.builder()
                        .method(HttpMethod.DELETE)
                        .uri(URI.create("/login"))
                        .exchange(MockServerWebExchange.from(MockServerHttpRequest.delete("/login")))
                        .build(),
                MockServerRequest.builder()
                        .method(HttpMethod.POST)
                        .uri(URI.create("/wrong-login"))
                        .exchange(MockServerWebExchange.from(MockServerHttpRequest.post("/wrong-login")))
                        .build()
        );
    }

    @Test
    void testLogInRoute_withMatchingRequest() {
        ServerRequest matchingRequest = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/login"))
                .exchange(MockServerWebExchange.from(MockServerHttpRequest.post("/login")))
                .build();

        HttpStatus status = HttpStatus.OK;
        String bodyValue = "j.w.token";
        Mono<ServerResponse> response = ServerResponse.status(status)
                .body(Mono.just(bodyValue), String.class);
        given(tokenHandler.logIn(matchingRequest)).willReturn(response);

        RouterFunction<ServerResponse> signUpRoute = routerConfig.logInRoute();
        StepVerifier.create(signUpRoute.route(matchingRequest))
                .assertNext(handlerFunction -> StepVerifier.create(handlerFunction.handle(matchingRequest))
                        .assertNext(r -> ServerResponseUtil.responseChecksOut(r, status, bodyValue))
                        .verifyComplete())
                .verifyComplete();
    }
}