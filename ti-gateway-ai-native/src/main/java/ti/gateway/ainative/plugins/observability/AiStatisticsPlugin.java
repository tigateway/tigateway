package ti.gateway.ainative.plugins.observability;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ti.gateway.ainative.config.AiNativeProperties;
import ti.gateway.ainative.model.AiStatistics;
import ti.gateway.ainative.service.AiStatisticsService;

import java.time.Duration;
import java.util.List;

/**
 * AI统计插件
 * 
 * 提供AI请求的统计和监控功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class AiStatisticsPlugin extends AbstractGatewayFilterFactory<AiStatisticsPlugin.Config> {

    @Autowired
    private AiStatisticsService aiStatisticsService;

    @Autowired
    private AiNativeProperties aiNativeProperties;

    public AiStatisticsPlugin() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!aiNativeProperties.getObservability().getAiStatistics().isEnabled()) {
                return chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 检查是否为AI请求
            if (!isAiRequest(request)) {
                return chain.filter(exchange);
            }

            long startTime = System.currentTimeMillis();
            String requestId = generateRequestId(request);

            // 记录请求开始
            recordRequestStart(request, requestId, startTime);

            return chain.filter(exchange)
                .doOnSuccess(result -> {
                    // 记录请求成功
                    long endTime = System.currentTimeMillis();
                    recordRequestSuccess(request, response, requestId, startTime, endTime);
                })
                .doOnError(throwable -> {
                    // 记录请求失败
                    long endTime = System.currentTimeMillis();
                    recordRequestError(request, response, requestId, startTime, endTime, throwable);
                });
        };
    }

    /**
     * 检查是否为AI请求
     */
    private boolean isAiRequest(ServerHttpRequest request) {
        String path = request.getPath().value();
        String contentType = request.getHeaders().getFirst("Content-Type");
        
        return path.contains("/ai/") || 
               path.contains("/llm/") ||
               "application/json".equals(contentType) && 
               request.getHeaders().containsKey("X-AI-Request");
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId(ServerHttpRequest request) {
        String existingId = request.getHeaders().getFirst("X-Request-ID");
        if (existingId != null && !existingId.isEmpty()) {
            return existingId;
        }
        return "ai-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    /**
     * 记录请求开始
     */
    private void recordRequestStart(ServerHttpRequest request, String requestId, long startTime) {
        AiStatistics statistics = new AiStatistics();
        statistics.setRequestId(requestId);
        statistics.setStartTime(startTime);
        statistics.setMethod(request.getMethod().name());
        statistics.setPath(request.getPath().value());
        statistics.setUserAgent(request.getHeaders().getFirst("User-Agent"));
        statistics.setClientIp(getClientIp(request));

        // 异步记录
        aiStatisticsService.recordRequestStart(statistics)
            .subscribe(
                success -> log.debug("Recorded request start: {}", requestId),
                error -> log.error("Error recording request start: {}", requestId, error)
            );
    }

    /**
     * 记录请求成功
     */
    private void recordRequestSuccess(ServerHttpRequest request, ServerHttpResponse response, 
                                    String requestId, long startTime, long endTime) {
        AiStatistics statistics = new AiStatistics();
        statistics.setRequestId(requestId);
        statistics.setEndTime(endTime);
        statistics.setDuration(endTime - startTime);
        statistics.setStatusCode(response.getStatusCode().value());
        statistics.setSuccess(true);

        // 异步记录
        aiStatisticsService.recordRequestEnd(statistics)
            .subscribe(
                success -> log.debug("Recorded request success: {}", requestId),
                error -> log.error("Error recording request success: {}", requestId, error)
            );
    }

    /**
     * 记录请求失败
     */
    private void recordRequestError(ServerHttpRequest request, ServerHttpResponse response, 
                                  String requestId, long startTime, long endTime, Throwable throwable) {
        AiStatistics statistics = new AiStatistics();
        statistics.setRequestId(requestId);
        statistics.setEndTime(endTime);
        statistics.setDuration(endTime - startTime);
        statistics.setStatusCode(response.getStatusCode() != null ? response.getStatusCode().value() : 500);
        statistics.setSuccess(false);
        statistics.setErrorMessage(throwable.getMessage());

        // 异步记录
        aiStatisticsService.recordRequestEnd(statistics)
            .subscribe(
                success -> log.debug("Recorded request error: {}", requestId),
                error -> log.error("Error recording request error: {}", requestId, error)
            );
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /**
     * 配置类
     */
    public static class Config {
        private boolean enabled = true;
        private Duration collectionInterval = Duration.ofSeconds(30);
        private List<String> metricsToCollect = List.of("requests", "duration", "errors", "tokens");

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getCollectionInterval() {
            return collectionInterval;
        }

        public void setCollectionInterval(Duration collectionInterval) {
            this.collectionInterval = collectionInterval;
        }

        public List<String> getMetricsToCollect() {
            return metricsToCollect;
        }

        public void setMetricsToCollect(List<String> metricsToCollect) {
            this.metricsToCollect = metricsToCollect;
        }
    }
}
