package ti.gateway.ainative.plugins.development;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ti.gateway.ainative.config.AiNativeProperties;
import ti.gateway.ainative.model.LlmRequest;
import ti.gateway.ainative.model.LlmResponse;
import ti.gateway.ainative.service.LlmCacheService;

import java.time.Duration;
import java.util.List;

/**
 * LLM缓存插件
 * 
 * 提供LLM响应的缓存功能，提高性能并降低成本
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class LlmCachePlugin extends AbstractGatewayFilterFactory<LlmCachePlugin.Config> {

    @Autowired
    private LlmCacheService llmCacheService;

    @Autowired
    private AiNativeProperties aiNativeProperties;

    public LlmCachePlugin() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!aiNativeProperties.getAiDevelopment().getLlmCache().isEnabled()) {
                return chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 检查是否为LLM请求
            if (!isLlmRequest(request)) {
                return chain.filter(exchange);
            }

            // 生成缓存键
            String cacheKey = generateCacheKey(request);

            // 尝试从缓存获取响应
            return llmCacheService.getCachedResponse(cacheKey)
                .flatMap(cachedResponse -> {
                    if (cachedResponse != null) {
                        log.debug("Cache hit for key: {}", cacheKey);
                        return writeCachedResponse(response, cachedResponse);
                    } else {
                        log.debug("Cache miss for key: {}", cacheKey);
                        return chain.filter(exchange)
                            .then(Mono.fromRunnable(() -> {
                                // 缓存响应（异步）
                                cacheResponseAsync(request, response, cacheKey);
                            }));
                    }
                })
                .switchIfEmpty(chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        // 缓存响应（异步）
                        cacheResponseAsync(request, response, cacheKey);
                    })));
        };
    }

    /**
     * 检查是否为LLM请求
     */
    private boolean isLlmRequest(ServerHttpRequest request) {
        String path = request.getPath().value();
        String contentType = request.getHeaders().getFirst("Content-Type");
        
        return path.contains("/ai/") || 
               path.contains("/llm/") ||
               "application/json".equals(contentType) && 
               request.getHeaders().containsKey("X-AI-Request");
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(ServerHttpRequest request) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("llm_cache:");
        keyBuilder.append(request.getPath().value());
        keyBuilder.append(":");
        keyBuilder.append(request.getMethod().name());
        
        // 添加请求体哈希（如果有）
        if (request.getHeaders().containsKey("X-Request-Hash")) {
            keyBuilder.append(":");
            keyBuilder.append(request.getHeaders().getFirst("X-Request-Hash"));
        }
        
        return keyBuilder.toString();
    }

    /**
     * 写入缓存的响应
     */
    private Mono<Void> writeCachedResponse(ServerHttpResponse response, LlmResponse cachedResponse) {
        response.getHeaders().add("X-Cache", "HIT");
        response.getHeaders().add("X-Cache-Key", cachedResponse.getCacheKey());
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(
            cachedResponse.getContent().getBytes()
        )));
    }

    /**
     * 异步缓存响应
     */
    private void cacheResponseAsync(ServerHttpRequest request, ServerHttpResponse response, String cacheKey) {
        // 这里应该实现异步缓存逻辑
        // 由于响应已经发送，需要特殊处理
        log.debug("Caching response for key: {}", cacheKey);
    }

    /**
     * 配置类
     */
    public static class Config {
        private boolean enabled = true;
        private Duration ttl = Duration.ofHours(1);
        private int maxSize = 1000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }
    }
}
