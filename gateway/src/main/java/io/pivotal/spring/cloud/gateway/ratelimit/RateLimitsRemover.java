package io.pivotal.spring.cloud.gateway.ratelimit;

import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
class RateLimitsRemover implements ApplicationListener<RefreshRoutesEvent> {
    private final Logger logger = LoggerFactory.getLogger(RateLimitsRemover.class);
    private final HazelcastInstance hazelcastInstance;

    RateLimitsRemover(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public void onApplicationEvent(RefreshRoutesEvent event) {
        this.logger.info("Removing Hazelcast map '{}' with rate limit information", "GLOBAL_RATE_LIMIT");
        this.hazelcastInstance.getMap("GLOBAL_RATE_LIMIT").destroy();
    }
}
