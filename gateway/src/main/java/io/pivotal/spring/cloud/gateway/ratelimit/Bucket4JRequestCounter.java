package io.pivotal.spring.cloud.gateway.ratelimit;

import io.github.bucket4j.AsyncBucket;
import io.github.bucket4j.Bucket;

import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Mono;

class Bucket4JRequestCounter implements RequestCounter {
    private final AsyncBucket bucket;

    Bucket4JRequestCounter(Bucket bucket) {
        this.bucket = bucket.asAsync();
    }

    public Mono<ConsumeResponse> consume(String apiKey) {
        return Mono.fromFuture(this.bucket.tryConsumeAndReturnRemaining(1L)).map((consumptionProbe) -> {
            return new ConsumeResponse(consumptionProbe.isConsumed(), consumptionProbe.getRemainingTokens(), TimeUnit.NANOSECONDS.toMillis(consumptionProbe.getNanosToWaitForRefill()));
        });
    }
}
