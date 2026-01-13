package ti.gateway.kubernetes.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.cloud.gateway.support.ConfigurationService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.isNull;

/**
 * Unit tests for DefaultRateLimiter
 */
class DefaultRateLimiterTest {

    @Mock
    private RequestCounterFactory requestCounterFactory;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private RequestCounter requestCounter;

    @Mock
    private ConsumeResponse consumeResponse;

    private DefaultRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rateLimiter = new DefaultRateLimiter(requestCounterFactory, configurationService);
    }

    @Test
    void testIsAllowedWithMissingKey() {
        Mono<AbstractRateLimiter.Response> result = rateLimiter.isAllowed("route1", "MISSING_RATE_LIMIT_KEY");
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertFalse(response.isAllowed());
                    assertNotNull(response.getHeaders());
                })
                .verifyComplete();
    }

    @Test
    void testIsAllowedWithValidKey() {
        String routeId = "route1";
        String id = "user123";
        
        when(requestCounterFactory.createOfGet(eq(routeId), eq(id), anyInt(), any(Duration.class)))
                .thenReturn(Mono.just(requestCounter));
        when(requestCounter.consume(eq(id)))
                .thenReturn(Mono.just(consumeResponse));
        when(consumeResponse.isAllowed()).thenReturn(true);
        when(consumeResponse.getRemainingRequests()).thenReturn(10L);
        
        Mono<AbstractRateLimiter.Response> result = rateLimiter.isAllowed(routeId, id);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertTrue(response.isAllowed());
                    assertTrue(response.getHeaders().containsKey("X-Remaining"));
                    assertEquals("10", response.getHeaders().get("X-Remaining"));
                })
                .verifyComplete();
    }

    @Test
    void testIsAllowedWhenRateLimitExceeded() {
        String routeId = "route1";
        String id = "user123";
        
        when(requestCounterFactory.createOfGet(eq(routeId), eq(id), anyInt(), any(Duration.class)))
                .thenReturn(Mono.just(requestCounter));
        when(requestCounter.consume(eq(id)))
                .thenReturn(Mono.just(consumeResponse));
        when(consumeResponse.isAllowed()).thenReturn(false);
        when(consumeResponse.getRetryDelayMs()).thenReturn(5000L);
        
        Mono<AbstractRateLimiter.Response> result = rateLimiter.isAllowed(routeId, id);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertFalse(response.isAllowed());
                    assertTrue(response.getHeaders().containsKey("X-Retry-In"));
                    assertEquals("5000", response.getHeaders().get("X-Retry-In"));
                })
                .verifyComplete();
    }

    @Test
    void testIsAllowedWithNullRouteId() {
        // Should handle null routeId gracefully - will call createOfGet with null routeId
        when(requestCounterFactory.createOfGet(isNull(), eq("user123"), anyInt(), any(Duration.class)))
                .thenReturn(Mono.just(requestCounter));
        when(requestCounter.consume(eq("user123")))
                .thenReturn(Mono.just(consumeResponse));
        when(consumeResponse.isAllowed()).thenReturn(false);
        
        Mono<AbstractRateLimiter.Response> result = rateLimiter.isAllowed(null, "user123");
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertFalse(response.isAllowed());
                })
                .verifyComplete();
    }

    @Test
    void testIsAllowedWithNullId() {
        // Should handle null id gracefully - will call createOfGet with null id
        when(requestCounterFactory.createOfGet(eq("route1"), isNull(), anyInt(), any(Duration.class)))
                .thenReturn(Mono.just(requestCounter));
        when(requestCounter.consume(isNull()))
                .thenReturn(Mono.just(consumeResponse));
        when(consumeResponse.isAllowed()).thenReturn(false);
        
        Mono<AbstractRateLimiter.Response> result = rateLimiter.isAllowed("route1", null);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertFalse(response.isAllowed());
                })
                .verifyComplete();
    }
}
