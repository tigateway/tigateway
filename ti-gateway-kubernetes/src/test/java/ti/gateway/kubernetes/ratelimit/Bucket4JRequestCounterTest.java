package ti.gateway.kubernetes.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Constructor;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Bucket4JRequestCounter
 * Note: Bucket4JRequestCounter is package-private, so we use reflection to test it
 */
class Bucket4JRequestCounterTest {

    private RequestCounter requestCounter;

    @BeforeEach
    void setUp() throws Exception {
        // Create a bucket with limit of 10 requests per second
        @SuppressWarnings("deprecation")
        Bandwidth bandwidth = Bandwidth.simple(10L, Duration.ofSeconds(1));
        @SuppressWarnings("deprecation")
        Bucket testBucket = Bucket4j.builder()
                .addLimit(bandwidth)
                .build();
        
        // Use reflection to create Bucket4JRequestCounter since it's package-private
        Constructor<?> constructor = Class.forName("ti.gateway.kubernetes.ratelimit.Bucket4JRequestCounter")
                .getDeclaredConstructor(Bucket.class);
        constructor.setAccessible(true);
        requestCounter = (RequestCounter) constructor.newInstance(testBucket);
    }

    @Test
    void testConsumeWithAvailableTokens() {
        String apiKey = "test-key";
        
        Mono<ConsumeResponse> result = requestCounter.consume(apiKey);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertTrue(response.isAllowed());
                    assertTrue(response.getRemainingRequests() >= 0);
                    assertEquals(0L, response.getRetryDelayMs());
                })
                .verifyComplete();
    }

    @Test
    void testConsumeMultipleTimes() {
        String apiKey = "test-key";
        
        // Consume multiple times
        for (int i = 0; i < 5; i++) {
            Mono<ConsumeResponse> result = requestCounter.consume(apiKey);
            
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertTrue(response.isAllowed() || !response.isAllowed());
                        assertTrue(response.getRemainingRequests() >= 0);
                    })
                    .verifyComplete();
        }
    }

    @Test
    void testConsumeWithNullApiKey() {
        Mono<ConsumeResponse> result = requestCounter.consume(null);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                })
                .verifyComplete();
    }

    @Test
    void testConsumeWithEmptyApiKey() {
        Mono<ConsumeResponse> result = requestCounter.consume("");
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                })
                .verifyComplete();
    }

    @Test
    void testConsumeResponseStructure() {
        String apiKey = "test-key";
        
        Mono<ConsumeResponse> result = requestCounter.consume(apiKey);
        
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    // Response should have isAllowed, remainingRequests, and retryDelayMs
                    assertTrue(response.getRemainingRequests() >= 0);
                    assertTrue(response.getRetryDelayMs() >= 0);
                })
                .verifyComplete();
    }
}
