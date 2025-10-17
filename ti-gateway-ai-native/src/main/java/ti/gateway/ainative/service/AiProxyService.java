package ti.gateway.ainative.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ti.gateway.ainative.config.AiNativeProperties;
import ti.gateway.ainative.model.LlmRequest;
import ti.gateway.ainative.model.LlmResponse;
import ti.gateway.ainative.plugins.adaptation.AiProxyPlugin;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI代理服务
 * 
 * 提供多模型适配、协议转换、重试和Fallback功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class AiProxyService {

    @Autowired
    private AiNativeProperties aiNativeProperties;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private final Map<String, ModelProvider> providers = new ConcurrentHashMap<>();

    /**
     * 代理请求
     */
    public Mono<AiProxyPlugin.ProxyResponse> proxyRequest(LlmRequest request, String targetModel) {
        return getModelProvider(targetModel)
            .flatMap(provider -> provider.proxyRequest(request))
            .map(response -> AiProxyPlugin.ProxyResponse.success(
                serializeResponse(response), 
                targetModel
            ))
            .onErrorResume(throwable -> {
                log.error("Error proxying request to model: {}", targetModel, throwable);
                return Mono.just(AiProxyPlugin.ProxyResponse.error("Proxy request failed"));
            });
    }

    /**
     * 获取模型提供者
     */
    private Mono<ModelProvider> getModelProvider(String modelName) {
        ModelProvider provider = providers.get(modelName);
        if (provider != null) {
            return Mono.just(provider);
        }

        // 创建提供者
        return createModelProvider(modelName)
            .doOnNext(p -> providers.put(modelName, p));
    }

    /**
     * 创建模型提供者
     */
    private Mono<ModelProvider> createModelProvider(String modelName) {
        return Mono.fromCallable(() -> {
            // 根据模型名称确定提供者类型
            if (modelName.startsWith("gpt-")) {
                return new OpenAiProvider(modelName, webClientBuilder);
            } else if (modelName.startsWith("claude-")) {
                return new AnthropicProvider(modelName, webClientBuilder);
            } else if (modelName.contains("qwen")) {
                return new AlibabaProvider(modelName, webClientBuilder);
            } else {
                return new DefaultProvider(modelName, webClientBuilder);
            }
        });
    }

    /**
     * 序列化响应
     */
    private String serializeResponse(LlmResponse response) {
        // 这里应该使用Jackson序列化
        // 为了简化，这里返回一个模拟JSON
        return "{\"id\":\"" + response.getId() + "\",\"content\":\"Mock response\"}";
    }

    /**
     * 模型提供者接口
     */
    public interface ModelProvider {
        Mono<LlmResponse> proxyRequest(LlmRequest request);
    }

    /**
     * OpenAI提供者
     */
    public static class OpenAiProvider implements ModelProvider {
        private final String modelName;
        private final WebClient webClient;

        public OpenAiProvider(String modelName, WebClient.Builder webClientBuilder) {
            this.modelName = modelName;
            this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .build();
        }

        @Override
        public Mono<LlmResponse> proxyRequest(LlmRequest request) {
            return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + getApiKey())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LlmResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnNext(response -> log.debug("OpenAI request completed for model: {}", modelName));
        }

        private String getApiKey() {
            // 这里应该从配置或环境变量获取API Key
            return "sk-mock-api-key";
        }
    }

    /**
     * Anthropic提供者
     */
    public static class AnthropicProvider implements ModelProvider {
        private final String modelName;
        private final WebClient webClient;

        public AnthropicProvider(String modelName, WebClient.Builder webClientBuilder) {
            this.modelName = modelName;
            this.webClient = webClientBuilder
                .baseUrl("https://api.anthropic.com/v1")
                .build();
        }

        @Override
        public Mono<LlmResponse> proxyRequest(LlmRequest request) {
            return webClient.post()
                .uri("/messages")
                .header("x-api-key", getApiKey())
                .header("anthropic-version", "2023-06-01")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LlmResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnNext(response -> log.debug("Anthropic request completed for model: {}", modelName));
        }

        private String getApiKey() {
            // 这里应该从配置或环境变量获取API Key
            return "sk-ant-mock-api-key";
        }
    }

    /**
     * 阿里云提供者
     */
    public static class AlibabaProvider implements ModelProvider {
        private final String modelName;
        private final WebClient webClient;

        public AlibabaProvider(String modelName, WebClient.Builder webClientBuilder) {
            this.modelName = modelName;
            this.webClient = webClientBuilder
                .baseUrl("https://dashscope.aliyuncs.com/api/v1")
                .build();
        }

        @Override
        public Mono<LlmResponse> proxyRequest(LlmRequest request) {
            return webClient.post()
                .uri("/services/aigc/text-generation/generation")
                .header("Authorization", "Bearer " + getApiKey())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LlmResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnNext(response -> log.debug("Alibaba request completed for model: {}", modelName));
        }

        private String getApiKey() {
            // 这里应该从配置或环境变量获取API Key
            return "sk-mock-alibaba-api-key";
        }
    }

    /**
     * 默认提供者
     */
    public static class DefaultProvider implements ModelProvider {
        private final String modelName;
        private final WebClient webClient;

        public DefaultProvider(String modelName, WebClient.Builder webClientBuilder) {
            this.modelName = modelName;
            this.webClient = webClientBuilder.build();
        }

        @Override
        public Mono<LlmResponse> proxyRequest(LlmRequest request) {
            // 返回模拟响应
            return Mono.fromCallable(() -> {
                LlmResponse response = new LlmResponse();
                response.setId("mock-" + System.currentTimeMillis());
                response.setModel(modelName);
                response.setContent("Mock response from default provider");
                response.setProvider("default");
                response.setResponseTime(System.currentTimeMillis());
                return response;
            })
            .doOnNext(response -> log.debug("Default provider request completed for model: {}", modelName));
        }
    }
}
