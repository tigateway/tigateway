package ti.gateway.kubernetes.ratelimit;

import reactor.core.publisher.Mono;

interface RequestCounter {
    Mono<ConsumeResponse> consume(String apiKey);
}
