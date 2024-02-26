package com.example.tokenservice.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.MessageFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class WebFilterFactoryTest {
    @Mock
    private ServerWebExchange exchange;
    @Mock
    private WebFilterChain chain;

    @Test
    void noOpWebFilter() {
        given(chain.filter(any())).willReturn(Mono.empty());

        WebFilter noOpWebFilter = WebFilterFactory.noOpWebFilter();
        StepVerifier.create(noOpWebFilter.filter(exchange, chain))
                .verifyComplete();

        then(chain).should().filter(exchange);
        then(chain).shouldHaveNoMoreInteractions();
        then(exchange).shouldHaveNoInteractions();
    }

    @Test
    void noOpWebFilterChainProxy() {
        given(chain.filter(any())).willReturn(Mono.empty());

        WebFilterChainProxy filterChainProxy = WebFilterFactory.noOpWebFilterChainProxy();
        StepVerifier.create(filterChainProxy.filter(exchange, chain))
                .verifyComplete();

        then(chain).should().filter(exchange);
        then(chain).shouldHaveNoMoreInteractions();
        then(exchange).shouldHaveNoInteractions();
    }

    @Test
    void testExceptionHandlingWebFilter_withDefaultBodyMapping() {
        String exceptionMessage = "Something happened!";
        given(chain.filter(exchange)).willReturn(Mono.error(new SomeException(exceptionMessage)));

        given(exchange.getResponse()).willReturn(new MockServerHttpResponse());

        HttpStatus status = HttpStatus.I_AM_A_TEAPOT;
        WebFilter webFilter =
                WebFilterFactory.exceptionHandlingWebFilter(SomeException.class, status);

        StepVerifier.create(webFilter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);

        then(chain).should().filter(captor.capture());

        ServerWebExchange newExchange = captor.getValue();
        assertThat(newExchange).extracting(ServerWebExchange::getResponse)
                .extracting(ServerHttpResponse::getStatusCode)
                .isEqualTo(status);

        StepVerifier.create(((MockServerHttpResponse) exchange.getResponse()).getBodyAsString())
                .expectNext(exceptionMessage)
                .verifyComplete();
    }

    @Test
    void testExceptionHandlingWebFilter_withCustomBodyMapping() {
        Throwable exceptionCause = new UnknownError("Nobody knows what happened");
        Throwable exception = new SomeException("Something happened!", exceptionCause);
        given(chain.filter(exchange)).willReturn(Mono.error(exception));

        given(exchange.getResponse()).willReturn(new MockServerHttpResponse());

        HttpStatus status = HttpStatus.I_AM_A_TEAPOT;

        WebFilter webFilter = WebFilterFactory.exceptionHandlingWebFilter(SomeException.class,
                status, t -> MessageFormat.format(
                        "This happened: {0}[{1}]. It was caused by: {2}[{3}]",
                        t.getClass().getSimpleName(), t.getMessage(),
                        t.getCause().getClass().getSimpleName(), t.getCause().getMessage()
                ));

        StepVerifier.create(webFilter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);

        then(chain).should().filter(captor.capture());

        ServerWebExchange newExchange = captor.getValue();
        assertThat(newExchange).extracting(ServerWebExchange::getResponse)
                .extracting(ServerHttpResponse::getStatusCode)
                .isEqualTo(status);

        StepVerifier.create(((MockServerHttpResponse) exchange.getResponse()).getBodyAsString())
                .expectNext(MessageFormat.format(
                        "This happened: {0}[{1}]. It was caused by: {2}[{3}]",
                        exception.getClass().getSimpleName(), exception.getMessage(),
                        exceptionCause.getClass().getSimpleName(), exceptionCause.getMessage()
                ))
                .verifyComplete();
    }

    static class SomeException extends RuntimeException {
        public SomeException(String exceptionMessage) {
            super(exceptionMessage);
        }

        public SomeException(String exceptionMessage, Throwable cause) {
            super(exceptionMessage, cause);
        }
    }
}