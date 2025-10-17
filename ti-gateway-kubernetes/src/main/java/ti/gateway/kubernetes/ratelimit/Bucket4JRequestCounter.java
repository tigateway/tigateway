package ti.gateway.kubernetes.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Mono;

class Bucket4JRequestCounter implements RequestCounter {
    private final Bucket bucket;

    Bucket4JRequestCounter(Bucket bucket) {
        this.bucket = bucket;
    }

    public Mono<ConsumeResponse> consume(String apiKey) {
        return Mono.fromCallable(() -> {
            ConsumptionProbe consumptionProbe = this.bucket.tryConsumeAndReturnRemaining(1L);
            return new ConsumeResponse(consumptionProbe.isConsumed(), consumptionProbe.getRemainingTokens(), TimeUnit.NANOSECONDS.toMillis(consumptionProbe.getNanosToWaitForRefill()));
        });
    }
}
