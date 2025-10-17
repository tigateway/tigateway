package ti.gateway.kubernetes.ratelimit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
// import io.github.bucket4j.hazelcast.HazelcastProxyManager; // Temporarily commented out due to API changes in Bucket4j 8.x

import java.time.Duration;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
class HazelcastBucket4JRequestCounterFactory implements RequestCounterFactory {
    private final HazelcastInstance hazelcastInstance;

    public HazelcastBucket4JRequestCounterFactory(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public Mono<RequestCounter> createOfGet(String routeId, String apiKey, int limit, Duration duration) {
        Bandwidth bandwidth = Bandwidth.simple((long) limit, duration);
        return Mono.defer(() -> {
            return this.createBucket(routeId, apiKey, bandwidth);
        }).map(Bucket4JRequestCounter::new);
    }

    private Mono<Bucket> createBucket(String routeId, String mapName, Bandwidth bandwidth) {
        // Temporarily using local bucket due to Bucket4j 8.x API changes
        // TODO: Update to use proper Hazelcast integration when API is clarified
        return Mono.just(Bucket4j.builder()
                .addLimit(bandwidth)
                .build());
    }
}
