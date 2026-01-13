package ti.gateway.kubernetes.ratelimit;

import java.util.HashMap;
import java.util.Map;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@SuppressWarnings("unused")
class DefaultRateLimiter extends AbstractRateLimiter<RateLimiterProperties> {
    @SuppressWarnings("unused")
    private static final String CONFIGURATION_PROPERTY_NAME = "rate-limiter";
    private static final RateLimiterProperties DEFAULT_CONFIG = new RateLimiterProperties();
    private final RequestCounterFactory requestCounterFactory;
    static final String MISSING_KEY = "MISSING_RATE_LIMIT_KEY";

    public DefaultRateLimiter(RequestCounterFactory requestCounterFactory, ConfigurationService configurationService) {
        super(RateLimiterProperties.class, "rate-limiter", configurationService);
        this.requestCounterFactory = requestCounterFactory;
    }

    public Mono<Response> isAllowed(String routeId, String id) {
        if ("MISSING_RATE_LIMIT_KEY".equals(id)) {
            return Mono.just(new Response(false, Map.of()));
        } else {
            RateLimiterProperties config = (RateLimiterProperties) this.getConfig().getOrDefault(routeId, DEFAULT_CONFIG);
            return this.requestCounterFactory.createOfGet(routeId, id, config.getLimit(), config.getDuration()).flatMap((requestCounter) -> {
                return requestCounter.consume(id);
            }).map(this::toResponse);
        }
    }

    private Response toResponse(ConsumeResponse consumeResponse) {
        Map<String, String> headers = new HashMap<>();
        if (consumeResponse.isAllowed()) {
            headers.put("X-Remaining", String.valueOf(consumeResponse.getRemainingRequests()));
        } else {
            headers.put("X-Retry-In", String.valueOf(consumeResponse.getRetryDelayMs()));
        }

        return new Response(consumeResponse.isAllowed(), headers);
    }
}
