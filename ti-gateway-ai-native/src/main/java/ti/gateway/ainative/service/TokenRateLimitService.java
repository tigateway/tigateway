package ti.gateway.ainative.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ti.gateway.ainative.model.TokenUsage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token限流服务
 * 
 * 提供基于Token的请求限流功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Slf4j
@Service
@ConditionalOnClass(ReactiveRedisTemplate.class)
public class TokenRateLimitService {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    private final ConcurrentHashMap<String, RateLimitInfo> localCache = new ConcurrentHashMap<>();

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String TOKEN_USAGE_PREFIX = "token_usage:";
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    /**
     * 检查限流
     */
    public Mono<Boolean> checkRateLimit(String userId, int requestsPerMinute, int tokensPerMinute) {
        String currentMinute = getCurrentMinute();
        String rateLimitKey = RATE_LIMIT_PREFIX + userId + ":" + currentMinute;
        String tokenUsageKey = TOKEN_USAGE_PREFIX + userId + ":" + currentMinute;

        return Mono.zip(
            checkRequestRateLimit(rateLimitKey, requestsPerMinute),
            checkTokenRateLimit(tokenUsageKey, tokensPerMinute)
        ).map(tuple -> tuple.getT1() && tuple.getT2());
    }

    /**
     * 检查请求频率限流
     */
    private Mono<Boolean> checkRequestRateLimit(String key, int limit) {
        return redisTemplate.opsForValue()
            .increment(key)
            .flatMap(count -> {
                if (count == 1) {
                    // 设置过期时间
                    return redisTemplate.expire(key, WINDOW_DURATION)
                        .then(Mono.just(count <= limit));
                } else {
                    return Mono.just(count <= limit);
                }
            })
            .onErrorResume(throwable -> {
                log.error("Error checking request rate limit for key: {}", key, throwable);
                // 使用本地缓存作为降级
                return checkLocalRateLimit(key, limit);
            });
    }

    /**
     * 检查Token使用量限流
     */
    private Mono<Boolean> checkTokenRateLimit(String key, int limit) {
        return redisTemplate.opsForValue()
            .get(key)
            .map(currentUsage -> {
                int usage = currentUsage != null ? Integer.parseInt(currentUsage) : 0;
                return usage < limit;
            })
            .defaultIfEmpty(true)
            .onErrorResume(throwable -> {
                log.error("Error checking token rate limit for key: {}", key, throwable);
                return Mono.just(true); // 降级时允许通过
            });
    }

    /**
     * 记录Token使用量
     */
    public Mono<Boolean> recordTokenUsage(String userId, TokenUsage tokenUsage) {
        String currentMinute = getCurrentMinute();
        String tokenUsageKey = TOKEN_USAGE_PREFIX + userId + ":" + currentMinute;

        int tokens = tokenUsage.getActualTokens() != null ? 
            tokenUsage.getActualTokens() : 
            tokenUsage.getEstimatedTokens();

        return redisTemplate.opsForValue()
            .increment(tokenUsageKey, tokens)
            .flatMap(count -> {
                if (count == tokens) {
                    // 设置过期时间
                    return redisTemplate.expire(tokenUsageKey, WINDOW_DURATION)
                        .then(Mono.just(true));
                } else {
                    return Mono.just(true);
                }
            })
            .onErrorResume(throwable -> {
                log.error("Error recording token usage for user: {}", userId, throwable);
                return Mono.just(false);
            });
    }

    /**
     * 获取用户Token使用统计
     */
    public Mono<TokenUsageStats> getUserTokenStats(String userId) {
        String currentMinute = getCurrentMinute();
        String tokenUsageKey = TOKEN_USAGE_PREFIX + userId + ":" + currentMinute;

        return redisTemplate.opsForValue()
            .get(tokenUsageKey)
            .map(usage -> {
                int currentUsage = usage != null ? Integer.parseInt(usage) : 0;
                return new TokenUsageStats(userId, currentUsage, currentMinute);
            })
            .defaultIfEmpty(new TokenUsageStats(userId, 0, currentMinute))
            .onErrorResume(throwable -> {
                log.error("Error getting token stats for user: {}", userId, throwable);
                return Mono.just(new TokenUsageStats(userId, 0, currentMinute));
            });
    }

    /**
     * 重置用户限流
     */
    public Mono<Boolean> resetUserRateLimit(String userId) {
        String currentMinute = getCurrentMinute();
        String rateLimitKey = RATE_LIMIT_PREFIX + userId + ":" + currentMinute;
        String tokenUsageKey = TOKEN_USAGE_PREFIX + userId + ":" + currentMinute;

        return Mono.zip(
            redisTemplate.delete(rateLimitKey),
            redisTemplate.delete(tokenUsageKey)
        ).map(tuple -> tuple.getT1() > 0 || tuple.getT2() > 0)
         .onErrorResume(throwable -> {
             log.error("Error resetting rate limit for user: {}", userId, throwable);
             return Mono.just(false);
         });
    }

    /**
     * 本地缓存限流检查（降级方案）
     */
    private Mono<Boolean> checkLocalRateLimit(String key, int limit) {
        return Mono.fromCallable(() -> {
            RateLimitInfo info = localCache.computeIfAbsent(key, k -> new RateLimitInfo());
            
            long now = System.currentTimeMillis();
            if (now - info.getLastReset() > WINDOW_DURATION.toMillis()) {
                info.reset();
            }
            
            return info.incrementAndCheck(limit);
        });
    }

    /**
     * 获取当前分钟
     */
    private String getCurrentMinute() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    }

    /**
     * 限流信息
     */
    private static class RateLimitInfo {
        private int count = 0;
        private long lastReset = System.currentTimeMillis();

        public boolean incrementAndCheck(int limit) {
            count++;
            return count <= limit;
        }

        public void reset() {
            count = 0;
            lastReset = System.currentTimeMillis();
        }

        public long getLastReset() {
            return lastReset;
        }
    }

    /**
     * Token使用统计
     */
    public static class TokenUsageStats {
        private final String userId;
        private final int currentUsage;
        private final String timeWindow;

        public TokenUsageStats(String userId, int currentUsage, String timeWindow) {
            this.userId = userId;
            this.currentUsage = currentUsage;
            this.timeWindow = timeWindow;
        }

        public String getUserId() {
            return userId;
        }

        public int getCurrentUsage() {
            return currentUsage;
        }

        public String getTimeWindow() {
            return timeWindow;
        }

        @Override
        public String toString() {
            return "TokenUsageStats{" +
                    "userId='" + userId + '\'' +
                    ", currentUsage=" + currentUsage +
                    ", timeWindow='" + timeWindow + '\'' +
                    '}';
        }
    }
}
