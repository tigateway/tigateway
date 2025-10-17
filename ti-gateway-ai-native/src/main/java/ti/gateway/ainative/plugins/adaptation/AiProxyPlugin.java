package ti.gateway.ainative.plugins.adaptation;

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
import ti.gateway.ainative.model.LlmRequest;
import ti.gateway.ainative.model.LlmResponse;
import ti.gateway.ainative.service.AiProxyService;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * AI代理插件
 * 
 * 提供多模型适配、协议转换、重试和Fallback功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class AiProxyPlugin extends AbstractGatewayFilterFactory<AiProxyPlugin.Config> {

    @Autowired
    private AiProxyService aiProxyService;

    @Autowired
    private AiNativeProperties aiNativeProperties;

    public AiProxyPlugin() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!aiNativeProperties.getMultiModel().getAiProxy().isEnabled()) {
                return chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 检查是否为AI请求
            if (!isAiRequest(request)) {
                return chain.filter(exchange);
            }

            // 获取目标模型
            String targetModel = getTargetModel(request);
            if (targetModel == null) {
                return writeErrorResponse(response, "Target model not specified");
            }

            // 执行AI代理请求
            return executeAiProxyRequest(request, response, targetModel, config)
                .flatMap(proxyResponse -> {
                    if (proxyResponse.isSuccess()) {
                        return writeProxyResponse(response, proxyResponse);
                    } else {
                        return writeErrorResponse(response, proxyResponse.getErrorMessage());
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("Error in AI proxy request", throwable);
                    if (config.isFallbackEnabled()) {
                        return executeFallbackRequest(request, response, targetModel, config);
                    } else {
                        return writeErrorResponse(response, "AI proxy service error");
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
     * 获取目标模型
     */
    private String getTargetModel(ServerHttpRequest request) {
        // 优先从Header获取
        String model = request.getHeaders().getFirst("X-Target-Model");
        if (model != null && !model.isEmpty()) {
            return model;
        }

        // 从请求体获取
        // 这里应该解析请求体获取模型信息
        // 为了简化，这里返回一个默认值
        return "gpt-3.5-turbo";
    }

    /**
     * 执行AI代理请求
     */
    private Mono<ProxyResponse> executeAiProxyRequest(ServerHttpRequest request, 
                                                    ServerHttpResponse response, 
                                                    String targetModel, 
                                                    Config config) {
        return request.getBody()
            .collectList()
            .flatMap(dataBuffers -> {
                // 合并请求体
                DataBufferFactory bufferFactory = response.bufferFactory();
                DataBuffer buffer = bufferFactory.join(dataBuffers);
                
                String requestBody = buffer.toString(StandardCharsets.UTF_8);
                
                try {
                    // 解析LLM请求
                    LlmRequest llmRequest = parseLlmRequest(requestBody);
                    
                    // 执行代理请求（带重试）
                    return executeWithRetry(llmRequest, targetModel, config)
                        .map(proxyResponse -> {
                            log.debug("AI proxy request completed for model: {}", targetModel);
                            return proxyResponse;
                        });
                        
                } catch (Exception e) {
                    log.error("Error executing AI proxy request", e);
                    return Mono.just(ProxyResponse.error("Request parsing error"));
                } finally {
                    // DataBuffer会自动释放，不需要手动调用release()
                }
            });
    }

    /**
     * 带重试的执行
     */
    private Mono<ProxyResponse> executeWithRetry(LlmRequest request, String targetModel, Config config) {
        return aiProxyService.proxyRequest(request, targetModel)
            .retry(config.getMaxRetries())
            .timeout(Duration.ofMillis(config.getTimeout()))
            .onErrorResume(throwable -> {
                log.error("AI proxy request failed after retries", throwable);
                return Mono.just(ProxyResponse.error("Request failed after retries"));
            });
    }

    /**
     * 执行Fallback请求
     */
    private Mono<Void> executeFallbackRequest(ServerHttpRequest request, 
                                            ServerHttpResponse response, 
                                            String targetModel, 
                                            Config config) {
        log.info("Executing fallback request for model: {}", targetModel);
        
        // 这里应该实现Fallback逻辑
        // 例如：切换到备用模型或返回默认响应
        return writeFallbackResponse(response, targetModel);
    }

    /**
     * 解析LLM请求
     */
    private LlmRequest parseLlmRequest(String requestBody) {
        // 这里应该使用Jackson解析JSON
        // 为了简化，这里返回一个模拟对象
        LlmRequest request = new LlmRequest();
        // 实际实现中应该解析JSON
        return request;
    }

    /**
     * 写入代理响应
     */
    private Mono<Void> writeProxyResponse(ServerHttpResponse response, ProxyResponse proxyResponse) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("X-AI-Proxy", "success");
        response.getHeaders().add("X-Target-Model", proxyResponse.getTargetModel());
        
        DataBuffer buffer = response.bufferFactory().wrap(proxyResponse.getContent().getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 写入Fallback响应
     */
    private Mono<Void> writeFallbackResponse(ServerHttpResponse response, String targetModel) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("X-AI-Proxy", "fallback");
        response.getHeaders().add("X-Target-Model", targetModel);
        
        String fallbackContent = String.format(
            "{\"error\": \"Service temporarily unavailable\", \"fallback\": true, \"model\": \"%s\"}", 
            targetModel
        );
        
        DataBuffer buffer = response.bufferFactory().wrap(fallbackContent.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 写入错误响应
     */
    private Mono<Void> writeErrorResponse(ServerHttpResponse response, String errorMessage) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorBody = String.format(
            "{\"error\": \"%s\", \"code\": \"AI_PROXY_ERROR\"}", 
            errorMessage
        );
        
        DataBuffer buffer = response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 代理响应
     */
    public static class ProxyResponse {
        private final boolean success;
        private final String content;
        private final String targetModel;
        private final String errorMessage;

        private ProxyResponse(boolean success, String content, String targetModel, String errorMessage) {
            this.success = success;
            this.content = content;
            this.targetModel = targetModel;
            this.errorMessage = errorMessage;
        }

        public static ProxyResponse success(String content, String targetModel) {
            return new ProxyResponse(true, content, targetModel, null);
        }

        public static ProxyResponse error(String errorMessage) {
            return new ProxyResponse(false, null, null, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getContent() {
            return content;
        }

        public String getTargetModel() {
            return targetModel;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * 配置类
     */
    public static class Config {
        private boolean enabled = true;
        private boolean fallbackEnabled = true;
        private int maxRetries = 3;
        private long timeout = 30000; // milliseconds
        private List<String> fallbackModels = List.of();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isFallbackEnabled() {
            return fallbackEnabled;
        }

        public void setFallbackEnabled(boolean fallbackEnabled) {
            this.fallbackEnabled = fallbackEnabled;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        public List<String> getFallbackModels() {
            return fallbackModels;
        }

        public void setFallbackModels(List<String> fallbackModels) {
            this.fallbackModels = fallbackModels;
        }
    }
}
