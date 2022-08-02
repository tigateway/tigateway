package ti.gateway.kubernetes.ratelimit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.RecoveryStrategy;
import io.github.bucket4j.grid.hazelcast.Hazelcast;
import io.github.bucket4j.grid.hazelcast.HazelcastBucketBuilder;

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

    @SuppressWarnings("unchecked")
    private Mono<Bucket> createBucket(String routeId, String mapName, Bandwidth bandwidth) {
        return Mono.just(this.hazelcastInstance)
                .publishOn(Schedulers.boundedElastic())
                .map((hazelcastInstance) -> {
                    IMap<String, GridBucketState> map = hazelcastInstance.getMap(mapName);
                    return map;
                })
                .map((map) -> {
                    return ((HazelcastBucketBuilder) ((Hazelcast) Bucket4j.extension(Hazelcast.class)).builder()
                            .addLimit(bandwidth))
                            .build(map, routeId, RecoveryStrategy.RECONSTRUCT);
                });
    }
}
