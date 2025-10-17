package ti.gateway.ainative.plugins.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ti.gateway.ainative.config.AiNativeProperties;
import ti.gateway.ainative.model.TokenUsage;
import ti.gateway.ainative.service.TokenRateLimitService;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Token限流插件
 * 
 * 提供基于Token的请求限流功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class TokenRateLimitPlugin extends AbstractGatewayFilterFactory<TokenRateLimitPlugin.Config> {

    @Autowired
    private TokenRateLimitService tokenRateLimitService;

    @Autowired
    private AiNativeProperties aiNativeProperties;

    public TokenRateLimitPlugin() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!aiNativeProperties.getAiSecurity().getTokenRateLimit().isEnabled()) {
                return chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 检查是否为AI请求
            if (!isAiRequest(request)) {
                return chain.filter(exchange);
            }

            // 获取用户标识
            String userId = getUserId(request);
            if (userId == null) {
                return writeErrorResponse(response, "User identification required");
            }

            // 检查Token限流
            return checkTokenRateLimit(userId, request, config)
                .flatMap(rateLimitResult -> {
                    if (rateLimitResult.isAllowed()) {
                        log.debug("Token rate limit check passed for user: {}", userId);
                        return chain.filter(exchange)
                            .then(Mono.fromRunnable(() -> {
                                // 记录Token使用
                                recordTokenUsageAsync(userId, request, rateLimitResult.getTokenUsage());
                            }));
                    } else {
                        log.warn("Token rate limit exceeded for user: {}", userId);
                        return writeRateLimitResponse(response, rateLimitResult);
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("Error in token rate limit check", throwable);
                    if (config.isFailOpen()) {
                        // 失败时允许通过
                        return chain.filter(exchange);
                    } else {
                        // 失败时阻止
                        return writeErrorResponse(response, "Token rate limit service error");
                    }
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
     * 获取用户标识
     */
    private String getUserId(ServerHttpRequest request) {
        // 优先从Header获取
        String userId = request.getHeaders().getFirst("X-User-ID");
        if (userId != null && !userId.isEmpty()) {
            return userId;
        }

        // 从API Key获取
        String apiKey = request.getHeaders().getFirst("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey;
        }

        // 从Authorization Header获取
        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        return null;
    }

    /**
     * 检查Token限流
     */
    private Mono<RateLimitResult> checkTokenRateLimit(String userId, ServerHttpRequest request, Config config) {
        return tokenRateLimitService.checkRateLimit(userId, config.getRequestsPerMinute(), config.getTokensPerMinute())
            .map(allowed -> {
                if (allowed) {
                    // 估算Token使用量
                    TokenUsage tokenUsage = estimateTokenUsage(request);
                    return new RateLimitResult(true, null, tokenUsage);
                } else {
                    return new RateLimitResult(false, "Rate limit exceeded", null);
                }
            });
    }

    /**
     * 估算Token使用量
     */
    private TokenUsage estimateTokenUsage(ServerHttpRequest request) {
        // 这里应该根据请求内容估算Token使用量
        // 为了简化，这里返回一个模拟值
        TokenUsage usage = new TokenUsage();
        usage.setEstimatedTokens(100);
        usage.setRequestType("completion");
        return usage;
    }

    /**
     * 异步记录Token使用
     */
    private void recordTokenUsageAsync(String userId, ServerHttpRequest request, TokenUsage tokenUsage) {
        // 这里应该异步记录Token使用情况
        log.debug("Recording token usage for user: {}, tokens: {}", userId, tokenUsage.getEstimatedTokens());
    }

    /**
     * 写入限流响应
     */
    private Mono<Void> writeRateLimitResponse(ServerHttpResponse response, RateLimitResult rateLimitResult) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("X-Rate-Limit", "exceeded");
        response.getHeaders().add("X-Rate-Limit-Reason", rateLimitResult.getReason());
        
        String errorBody = String.format(
            "{\"error\": \"Rate limit exceeded\", \"reason\": \"%s\", \"code\": \"RATE_LIMIT_EXCEEDED\"}", 
            rateLimitResult.getReason()
        );
        
        DataBuffer buffer = response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 写入错误响应
     */
    private Mono<Void> writeErrorResponse(ServerHttpResponse response, String errorMessage) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorBody = String.format(
            "{\"error\": \"%s\", \"code\": \"TOKEN_RATE_LIMIT_ERROR\"}", 
            errorMessage
        );
        
        DataBuffer buffer = response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 限流结果
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final String reason;
        private final TokenUsage tokenUsage;

        public RateLimitResult(boolean allowed, String reason, TokenUsage tokenUsage) {
            this.allowed = allowed;
            this.reason = reason;
            this.tokenUsage = tokenUsage;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getReason() {
            return reason;
        }

        public TokenUsage getTokenUsage() {
            return tokenUsage;
        }
    }

    /**
     * 配置类
     */
    public static class Config {
        private boolean enabled = true;
        private boolean failOpen = true;
        private int requestsPerMinute = 100;
        private int tokensPerMinute = 10000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isFailOpen() {
            return failOpen;
        }

        public void setFailOpen(boolean failOpen) {
            this.failOpen = failOpen;
        }

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }

        public int getTokensPerMinute() {
            return tokensPerMinute;
        }

        public void setTokensPerMinute(int tokensPerMinute) {
            this.tokensPerMinute = tokensPerMinute;
        }
    }
}
