package ti.gateway.ainative.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ti.gateway.ainative.model.LlmResponse;

import java.time.Duration;

/**
 * LLM缓存服务
 * 
 * 提供LLM响应的缓存管理功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Slf4j
@Service
@ConditionalOnClass(ReactiveRedisTemplate.class)
public class LlmCacheService {

    @Autowired
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "llm_cache:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    /**
     * 获取缓存的响应
     */
    public Mono<LlmResponse> getCachedResponse(String cacheKey) {
        String fullKey = CACHE_PREFIX + cacheKey;
        
        return redisTemplate.opsForValue()
            .get(fullKey)
            .cast(LlmResponse.class)
            .doOnNext(response -> {
                if (response != null) {
                    log.debug("Cache hit for key: {}", fullKey);
                } else {
                    log.debug("Cache miss for key: {}", fullKey);
                }
            })
            .onErrorResume(throwable -> {
                log.error("Error getting cached response for key: {}", fullKey, throwable);
                return Mono.empty();
            });
    }

    /**
     * 缓存响应
     */
    public Mono<Boolean> cacheResponse(String cacheKey, LlmResponse response) {
        String fullKey = CACHE_PREFIX + cacheKey;
        
        return redisTemplate.opsForValue()
            .set(fullKey, response, DEFAULT_TTL)
            .doOnNext(success -> {
                if (success) {
                    log.debug("Cached response for key: {}", fullKey);
                } else {
                    log.warn("Failed to cache response for key: {}", fullKey);
                }
            })
            .onErrorResume(throwable -> {
                log.error("Error caching response for key: {}", fullKey, throwable);
                return Mono.just(false);
            });
    }

    /**
     * 缓存响应（带TTL）
     */
    public Mono<Boolean> cacheResponse(String cacheKey, LlmResponse response, Duration ttl) {
        String fullKey = CACHE_PREFIX + cacheKey;
        
        return redisTemplate.opsForValue()
            .set(fullKey, response, ttl)
            .doOnNext(success -> {
                if (success) {
                    log.debug("Cached response for key: {} with TTL: {}", fullKey, ttl);
                } else {
                    log.warn("Failed to cache response for key: {}", fullKey);
                }
            })
            .onErrorResume(throwable -> {
                log.error("Error caching response for key: {}", fullKey, throwable);
                return Mono.just(false);
            });
    }

    /**
     * 删除缓存
     */
    public Mono<Boolean> evictCache(String cacheKey) {
        String fullKey = CACHE_PREFIX + cacheKey;
        
        return redisTemplate.delete(fullKey)
            .map(count -> count > 0)
            .doOnNext(deleted -> {
                if (deleted) {
                    log.debug("Evicted cache for key: {}", fullKey);
                } else {
                    log.debug("Cache not found for key: {}", fullKey);
                }
            })
            .onErrorResume(throwable -> {
                log.error("Error evicting cache for key: {}", fullKey, throwable);
                return Mono.just(false);
            });
    }

    /**
     * 清空所有LLM缓存
     */
    public Mono<Long> clearAllCache() {
        return redisTemplate.delete(redisTemplate.keys(CACHE_PREFIX + "*"))
            .doOnNext(count -> log.info("Cleared {} LLM cache entries", count))
            .onErrorResume(throwable -> {
                log.error("Error clearing LLM cache", throwable);
                return Mono.just(0L);
            });
    }

    /**
     * 获取缓存统计信息
     */
    public Mono<CacheStats> getCacheStats() {
        return redisTemplate.keys(CACHE_PREFIX + "*")
            .count()
            .map(count -> new CacheStats(count, CACHE_PREFIX))
            .onErrorResume(throwable -> {
                log.error("Error getting cache stats", throwable);
                return Mono.just(new CacheStats(0L, CACHE_PREFIX));
            });
    }

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final long entryCount;
        private final String prefix;

        public CacheStats(long entryCount, String prefix) {
            this.entryCount = entryCount;
            this.prefix = prefix;
        }

        public long getEntryCount() {
            return entryCount;
        }

        public String getPrefix() {
            return prefix;
        }

        @Override
        public String toString() {
            return "CacheStats{" +
                    "entryCount=" + entryCount +
                    ", prefix='" + prefix + '\'' +
                    '}';
        }
    }
}
