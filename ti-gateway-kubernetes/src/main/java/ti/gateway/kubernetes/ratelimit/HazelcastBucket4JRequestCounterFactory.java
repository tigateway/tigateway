package ti.gateway.kubernetes.ratelimit;

import com.hazelcast.core.HazelcastInstance;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
// import io.github.bucket4j.hazelcast.HazelcastProxyManager; // Temporarily commented out due to API changes in Bucket4j 8.x

import java.time.Duration;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Hazelcast-based Bucket4J Request Counter Factory
 * 
 * Note: Currently using local bucket implementation due to Bucket4j 8.x API changes.
 * TODO: Update to use proper Hazelcast integration when API is clarified.
 */
@Component
@SuppressWarnings({"deprecation", "unused"})
class HazelcastBucket4JRequestCounterFactory implements RequestCounterFactory {
    @SuppressWarnings("unused")
    private final HazelcastInstance hazelcastInstance;

    public HazelcastBucket4JRequestCounterFactory(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public Mono<RequestCounter> createOfGet(String routeId, String apiKey, int limit, Duration duration) {
        // Bandwidth.simple is deprecated, but we need to use it until Bucket4j API is updated
        Bandwidth bandwidth = Bandwidth.simple((long) limit, duration);
        return Mono.defer(() -> {
            return this.createBucket(routeId, apiKey, bandwidth);
        }).map(Bucket4JRequestCounter::new);
    }

    private Mono<Bucket> createBucket(String routeId, String mapName, Bandwidth bandwidth) {
        // Temporarily using local bucket due to Bucket4j 8.x API changes
        // TODO: Update to use proper Hazelcast integration when API is clarified
        // Bucket4j.builder() is deprecated, but we need to use it until API is updated
        return Mono.just(Bucket4j.builder()
                .addLimit(bandwidth)
                .build());
    }
}
