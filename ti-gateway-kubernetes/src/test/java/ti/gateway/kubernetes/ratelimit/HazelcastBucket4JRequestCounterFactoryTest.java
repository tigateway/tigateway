package ti.gateway.kubernetes.ratelimit;

import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link HazelcastBucket4JRequestCounterFactory}.
 */
class HazelcastBucket4JRequestCounterFactoryTest {

    private HazelcastInstance hazelcastInstance;
    private HazelcastBucket4JRequestCounterFactory factory;

    @BeforeEach
    void setUp() {
        hazelcastInstance = mock(HazelcastInstance.class);
        factory = new HazelcastBucket4JRequestCounterFactory(hazelcastInstance);
    }

    @Test
    void testCreateOfGet() {
        // Setup
        String routeId = "test-route";
        String apiKey = "test-api-key";
        int limit = 100;
        Duration duration = Duration.ofSeconds(60);

        // Execute
        Mono<RequestCounter> result = factory.createOfGet(routeId, apiKey, limit, duration);

        // Verify
        StepVerifier.create(result)
                .assertNext(counter -> {
                    assertNotNull(counter);
                    assertTrue(counter instanceof Bucket4JRequestCounter);
                })
                .verifyComplete();
    }

    @Test
    void testCreateOfGetWithDifferentLimits() {
        // Test with different limit values
        String routeId = "test-route";
        String apiKey = "test-api-key";
        Duration duration = Duration.ofSeconds(60);

        // Test with limit 1
        Mono<RequestCounter> result1 = factory.createOfGet(routeId, apiKey, 1, duration);
        StepVerifier.create(result1)
                .assertNext(counter -> assertNotNull(counter))
                .verifyComplete();

        // Test with limit 1000
        Mono<RequestCounter> result2 = factory.createOfGet(routeId, apiKey, 1000, duration);
        StepVerifier.create(result2)
                .assertNext(counter -> assertNotNull(counter))
                .verifyComplete();
    }

    @Test
    void testCreateOfGetWithDifferentDurations() {
        // Test with different duration values
        String routeId = "test-route";
        String apiKey = "test-api-key";
        int limit = 100;

        // Test with 1 second
        Mono<RequestCounter> result1 = factory.createOfGet(routeId, apiKey, limit, Duration.ofSeconds(1));
        StepVerifier.create(result1)
                .assertNext(counter -> assertNotNull(counter))
                .verifyComplete();

        // Test with 1 hour
        Mono<RequestCounter> result2 = factory.createOfGet(routeId, apiKey, limit, Duration.ofHours(1));
        StepVerifier.create(result2)
                .assertNext(counter -> assertNotNull(counter))
                .verifyComplete();
    }

    @Test
    void testCreateOfGetWithNullRouteId() {
        String apiKey = "test-api-key";
        int limit = 100;
        Duration duration = Duration.ofSeconds(60);

        Mono<RequestCounter> result = factory.createOfGet(null, apiKey, limit, duration);

        StepVerifier.create(result)
                .assertNext(counter -> assertNotNull(counter))
                .verifyComplete();
    }

    @Test
    void testCreateOfGetWithNullApiKey() {
        String routeId = "test-route";
        int limit = 100;
        Duration duration = Duration.ofSeconds(60);

        Mono<RequestCounter> result = factory.createOfGet(routeId, null, limit, duration);

        StepVerifier.create(result)
                .assertNext(counter -> assertNotNull(counter))
                .verifyComplete();
    }

    @Test
    void testCreateOfGetMultipleTimes() {
        // Test that creating multiple counters works
        String routeId = "test-route";
        String apiKey = "test-api-key";
        int limit = 100;
        Duration duration = Duration.ofSeconds(60);

        Mono<RequestCounter> result1 = factory.createOfGet(routeId, apiKey, limit, duration);
        Mono<RequestCounter> result2 = factory.createOfGet(routeId, apiKey, limit, duration);

        StepVerifier.create(result1)
                .assertNext(counter -> assertNotNull(counter))
                .verifyComplete();

        StepVerifier.create(result2)
                .assertNext(counter -> assertNotNull(counter))
                .verifyComplete();
    }
}
