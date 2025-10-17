package ti.gateway.ainative.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ti.gateway.ainative.model.AiStatistics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI统计服务
 * 
 * 提供AI请求的统计和监控功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Slf4j
@Service
@ConditionalOnClass(ReactiveRedisTemplate.class)
public class AiStatisticsService {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    private final ConcurrentHashMap<String, AtomicLong> localCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> localDurations = new ConcurrentHashMap<>();

    private static final String STATS_PREFIX = "ai_stats:";
    private static final String REQUEST_PREFIX = "ai_request:";
    private static final Duration STATS_TTL = Duration.ofDays(7);

    /**
     * 记录请求开始
     */
    public Mono<Boolean> recordRequestStart(AiStatistics statistics) {
        String requestKey = REQUEST_PREFIX + statistics.getRequestId();
        
        return redisTemplate.opsForValue()
            .set(requestKey, serializeStatistics(statistics), STATS_TTL)
            .doOnNext(success -> {
                if (success) {
                    log.debug("Recorded request start: {}", statistics.getRequestId());
                } else {
                    log.warn("Failed to record request start: {}", statistics.getRequestId());
                }
            })
            .onErrorResume(throwable -> {
                log.error("Error recording request start: {}", statistics.getRequestId(), throwable);
                return Mono.just(false);
            });
    }

    /**
     * 记录请求结束
     */
    public Mono<Boolean> recordRequestEnd(AiStatistics statistics) {
        String requestKey = REQUEST_PREFIX + statistics.getRequestId();
        
        return redisTemplate.opsForValue()
            .get(requestKey)
            .flatMap(existingStats -> {
                // 更新统计信息
                AiStatistics updatedStats = mergeStatistics(existingStats, statistics);
                
                // 保存更新后的统计信息
                return redisTemplate.opsForValue()
                    .set(requestKey, serializeStatistics(updatedStats), STATS_TTL)
                    .flatMap(success -> {
                        if (success) {
                            // 更新聚合统计
                            return updateAggregateStats(updatedStats);
                        } else {
                            return Mono.just(false);
                        }
                    });
            })
            .doOnNext(success -> {
                if (success) {
                    log.debug("Recorded request end: {}", statistics.getRequestId());
                } else {
                    log.warn("Failed to record request end: {}", statistics.getRequestId());
                }
            })
            .onErrorResume(throwable -> {
                log.error("Error recording request end: {}", statistics.getRequestId(), throwable);
                return Mono.just(false);
            });
    }

    /**
     * 更新聚合统计
     */
    private Mono<Boolean> updateAggregateStats(AiStatistics statistics) {
        String currentHour = getCurrentHour();
        
        return Mono.zip(
            incrementCounter("total_requests", currentHour),
            incrementCounter("successful_requests", currentHour, statistics.getSuccess()),
            incrementCounter("failed_requests", currentHour, !statistics.getSuccess()),
            recordDuration("avg_duration", currentHour, statistics.getDuration())
        ).map(tuple -> tuple.getT1() && tuple.getT2() && tuple.getT3() && tuple.getT4());
    }

    /**
     * 增加计数器
     */
    private Mono<Boolean> incrementCounter(String counterName, String timeWindow) {
        return incrementCounter(counterName, timeWindow, true);
    }

    /**
     * 增加计数器（条件）
     */
    private Mono<Boolean> incrementCounter(String counterName, String timeWindow, boolean condition) {
        if (!condition) {
            return Mono.just(true);
        }

        String key = STATS_PREFIX + counterName + ":" + timeWindow;
        
        return redisTemplate.opsForValue()
            .increment(key)
            .flatMap(count -> {
                if (count == 1) {
                    return redisTemplate.expire(key, STATS_TTL)
                        .then(Mono.just(true));
                } else {
                    return Mono.just(true);
                }
            })
            .onErrorResume(throwable -> {
                log.error("Error incrementing counter: {}", counterName, throwable);
                // 使用本地计数器作为降级
                localCounters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
                return Mono.just(true);
            });
    }

    /**
     * 记录持续时间
     */
    private Mono<Boolean> recordDuration(String metricName, String timeWindow, Long duration) {
        if (duration == null) {
            return Mono.just(true);
        }

        String key = STATS_PREFIX + metricName + ":" + timeWindow;
        
        return redisTemplate.opsForValue()
            .get(key)
            .flatMap(existingValue -> {
                if (existingValue != null) {
                    // 计算新的平均值
                    String[] parts = existingValue.split(":");
                    long count = Long.parseLong(parts[0]);
                    double avgDuration = Double.parseDouble(parts[1]);
                    
                    double newAvgDuration = (avgDuration * count + duration) / (count + 1);
                    String newValue = (count + 1) + ":" + newAvgDuration;
                    
                    return redisTemplate.opsForValue()
                        .set(key, newValue, STATS_TTL);
                } else {
                    // 第一个值
                    String newValue = "1:" + duration;
                    return redisTemplate.opsForValue()
                        .set(key, newValue, STATS_TTL);
                }
            })
            .defaultIfEmpty(true)
            .onErrorResume(throwable -> {
                log.error("Error recording duration: {}", metricName, throwable);
                return Mono.just(true);
            });
    }

    /**
     * 获取统计信息
     */
    public Mono<StatisticsSummary> getStatistics(String timeWindow) {
        String currentHour = timeWindow != null ? timeWindow : getCurrentHour();
        
        return Mono.zip(
            getCounterValue("total_requests", currentHour),
            getCounterValue("successful_requests", currentHour),
            getCounterValue("failed_requests", currentHour),
            getDurationValue("avg_duration", currentHour)
        ).map(tuple -> new StatisticsSummary(
            currentHour,
            tuple.getT1(),
            tuple.getT2(),
            tuple.getT3(),
            tuple.getT4()
        ));
    }

    /**
     * 获取计数器值
     */
    private Mono<Long> getCounterValue(String counterName, String timeWindow) {
        String key = STATS_PREFIX + counterName + ":" + timeWindow;
        
        return redisTemplate.opsForValue()
            .get(key)
            .map(Long::parseLong)
            .defaultIfEmpty(0L)
            .onErrorResume(throwable -> {
                log.error("Error getting counter value: {}", counterName, throwable);
                // 从本地缓存获取
                AtomicLong localCounter = localCounters.get(key);
                return Mono.just(localCounter != null ? localCounter.get() : 0L);
            });
    }

    /**
     * 获取持续时间值
     */
    private Mono<Double> getDurationValue(String metricName, String timeWindow) {
        String key = STATS_PREFIX + metricName + ":" + timeWindow;
        
        return redisTemplate.opsForValue()
            .get(key)
            .map(value -> {
                String[] parts = value.split(":");
                return Double.parseDouble(parts[1]);
            })
            .defaultIfEmpty(0.0)
            .onErrorResume(throwable -> {
                log.error("Error getting duration value: {}", metricName, throwable);
                return Mono.just(0.0);
            });
    }

    /**
     * 序列化统计信息
     */
    private String serializeStatistics(AiStatistics statistics) {
        // 这里应该使用Jackson序列化
        // 为了简化，这里返回一个简单的字符串
        return statistics.getRequestId() + ":" + statistics.getStartTime() + ":" + statistics.getSuccess();
    }

    /**
     * 合并统计信息
     */
    private AiStatistics mergeStatistics(String existingStats, AiStatistics newStats) {
        // 这里应该解析现有统计信息并合并
        // 为了简化，这里直接返回新的统计信息
        return newStats;
    }

    /**
     * 获取当前小时
     */
    private String getCurrentHour() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
    }

    /**
     * 统计摘要
     */
    public static class StatisticsSummary {
        private final String timeWindow;
        private final long totalRequests;
        private final long successfulRequests;
        private final long failedRequests;
        private final double avgDuration;

        public StatisticsSummary(String timeWindow, long totalRequests, long successfulRequests, 
                               long failedRequests, double avgDuration) {
            this.timeWindow = timeWindow;
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.avgDuration = avgDuration;
        }

        public String getTimeWindow() {
            return timeWindow;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getSuccessfulRequests() {
            return successfulRequests;
        }

        public long getFailedRequests() {
            return failedRequests;
        }

        public double getAvgDuration() {
            return avgDuration;
        }

        public double getSuccessRate() {
            return totalRequests > 0 ? (double) successfulRequests / totalRequests : 0.0;
        }

        @Override
        public String toString() {
            return "StatisticsSummary{" +
                    "timeWindow='" + timeWindow + '\'' +
                    ", totalRequests=" + totalRequests +
                    ", successfulRequests=" + successfulRequests +
                    ", failedRequests=" + failedRequests +
                    ", avgDuration=" + avgDuration +
                    ", successRate=" + getSuccessRate() +
                    '}';
        }
    }
}
