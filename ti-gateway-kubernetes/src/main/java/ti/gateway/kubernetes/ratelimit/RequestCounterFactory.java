package ti.gateway.kubernetes.ratelimit;

import java.time.Duration;
import reactor.core.publisher.Mono;

interface RequestCounterFactory {
    Mono<RequestCounter> createOfGet(String routeId, String apiKey, int limit, Duration duration);
}
