package ti.gateway.ainative.plugins.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ti.gateway.ainative.config.AiNativeProperties;
import ti.gateway.ainative.model.ContentReviewResult;
import ti.gateway.ainative.service.ContentReviewService;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 内容审核插件
 * 
 * 提供AI请求和响应的内容审核功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class ContentReviewPlugin extends AbstractGatewayFilterFactory<ContentReviewPlugin.Config> {

    @Autowired
    private ContentReviewService contentReviewService;

    @Autowired
    private AiNativeProperties aiNativeProperties;

    public ContentReviewPlugin() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!aiNativeProperties.getAiSecurity().getContentReview().isEnabled()) {
                return chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 检查是否为AI请求
            if (!isAiRequest(request)) {
                return chain.filter(exchange);
            }

            // 审核请求内容
            return reviewRequestContent(request, response, config)
                .flatMap(reviewResult -> {
                    if (reviewResult.isBlocked()) {
                        log.warn("Request blocked by content review: {}", reviewResult.getReason());
                        return writeBlockedResponse(response, reviewResult);
                    } else {
                        log.debug("Request passed content review");
                        return chain.filter(exchange)
                            .then(Mono.fromRunnable(() -> {
                                // 异步审核响应内容
                                reviewResponseContentAsync(request, response, config);
                            }));
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("Error in content review", throwable);
                    if (config.isFailOpen()) {
                        // 失败时允许通过
                        return chain.filter(exchange);
                    } else {
                        // 失败时阻止
                        return writeErrorResponse(response, "Content review error");
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
     * 审核请求内容
     */
    private Mono<ContentReviewResult> reviewRequestContent(ServerHttpRequest request, ServerHttpResponse response, Config config) {
        return request.getBody()
            .collectList()
            .flatMap(dataBuffers -> {
                // 合并请求体
                DataBufferFactory bufferFactory = response.bufferFactory();
                DataBuffer buffer = bufferFactory.join(dataBuffers);
                
                String requestBody = buffer.toString(StandardCharsets.UTF_8);
                
                try {
                    // 执行内容审核
                    return contentReviewService.reviewContent(requestBody, config.getReviewTypes())
                        .doOnNext(result -> log.debug("Content review result: {}", result));
                        
                } catch (Exception e) {
                    log.error("Error reviewing request content", e);
                    return Mono.just(ContentReviewResult.blocked("Content review error"));
                } finally {
                    // DataBuffer会自动释放，不需要手动调用release()
                }
            });
    }

    /**
     * 异步审核响应内容
     */
    private void reviewResponseContentAsync(ServerHttpRequest request, ServerHttpResponse response, Config config) {
        // 这里应该实现异步响应内容审核
        // 由于响应已经发送，需要特殊处理
        log.debug("Reviewing response content asynchronously");
    }

    /**
     * 写入被阻止的响应
     */
    private Mono<Void> writeBlockedResponse(ServerHttpResponse response, ContentReviewResult reviewResult) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("X-Content-Review", "BLOCKED");
        response.getHeaders().add("X-Content-Review-Reason", reviewResult.getReason());
        
        String errorBody = String.format(
            "{\"error\": \"Content blocked\", \"reason\": \"%s\", \"code\": \"CONTENT_BLOCKED\"}", 
            reviewResult.getReason()
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
            "{\"error\": \"%s\", \"code\": \"CONTENT_REVIEW_ERROR\"}", 
            errorMessage
        );
        
        DataBuffer buffer = response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 配置类
     */
    public static class Config {
        private boolean enabled = true;
        private boolean failOpen = true; // 失败时是否允许通过
        private List<String> reviewTypes = List.of("text", "image");
        private double confidenceThreshold = 0.8;

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

        public List<String> getReviewTypes() {
            return reviewTypes;
        }

        public void setReviewTypes(List<String> reviewTypes) {
            this.reviewTypes = reviewTypes;
        }

        public double getConfidenceThreshold() {
            return confidenceThreshold;
        }

        public void setConfidenceThreshold(double confidenceThreshold) {
            this.confidenceThreshold = confidenceThreshold;
        }
    }
}
