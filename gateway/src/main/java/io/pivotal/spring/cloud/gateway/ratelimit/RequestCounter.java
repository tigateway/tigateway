package io.pivotal.spring.cloud.gateway.ratelimit;

import reactor.core.publisher.Mono;

interface RequestCounter {
    Mono<ConsumeResponse> consume(String apiKey);
}
