package io.pivotal.spring.cloud.gateway.apikeys;

import reactor.core.publisher.Mono;

public interface ApiKeyValidator {
    Mono<Boolean> keyIsValid(String apiKey);
}
