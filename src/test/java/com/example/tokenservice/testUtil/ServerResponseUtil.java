package com.example.tokenservice.testUtil;

import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class ServerResponseUtil {
    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    public static <T> void responseChecksOut(ServerResponse response, HttpStatus expectedStatus, T expectedBody) {
        assertSoftly(soft -> {
            soft.assertThat(response.statusCode()).isEqualTo(expectedStatus);

            Mono<T> body = (Mono<T>) ReflectionTestUtils.getField(response, "entity");
            StepVerifier.create(body)
                    .expectNext(expectedBody)
                    .verifyComplete();
        });
    }
}
