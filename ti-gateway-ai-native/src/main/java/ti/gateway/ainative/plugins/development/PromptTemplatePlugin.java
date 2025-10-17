package ti.gateway.ainative.plugins.development;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ti.gateway.ainative.config.AiNativeProperties;
import ti.gateway.ainative.model.LlmRequest;
import ti.gateway.ainative.service.PromptTemplateService;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 提示词模板插件
 * 
 * 提供提示词模板管理和应用功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class PromptTemplatePlugin extends AbstractGatewayFilterFactory<PromptTemplatePlugin.Config> {

    @Autowired
    private PromptTemplateService promptTemplateService;

    @Autowired
    private AiNativeProperties aiNativeProperties;

    public PromptTemplatePlugin() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!aiNativeProperties.getAiDevelopment().getPromptTemplate().isEnabled()) {
                return chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 检查是否为LLM请求
            if (!isLlmRequest(request)) {
                return chain.filter(exchange);
            }

            // 检查是否指定了模板
            String templateName = request.getHeaders().getFirst("X-Prompt-Template");
            if (templateName == null || templateName.isEmpty()) {
                return chain.filter(exchange);
            }

            // 应用提示词模板
            return applyPromptTemplate(request, response, templateName, config)
                .flatMap(modifiedRequest -> {
                    ServerHttpRequest.Builder builder = request.mutate();
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .switchIfEmpty(chain.filter(exchange));
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
               "application/json".equals(contentType);
    }

    /**
     * 应用提示词模板
     */
    private Mono<ServerHttpRequest> applyPromptTemplate(ServerHttpRequest request, 
                                                       ServerHttpResponse response, 
                                                       String templateName, 
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
                    
                    // 应用模板
                    return promptTemplateService.applyTemplate(templateName, llmRequest, config.getVariables())
                        .map(template -> {
                            // 创建修改后的请求
                            String modifiedBody = serializeLlmRequest(template);
                            DataBuffer modifiedBuffer = bufferFactory.wrap(modifiedBody.getBytes(StandardCharsets.UTF_8));
                            
                            return request.mutate()
                                .header("Content-Length", String.valueOf(modifiedBody.length()))
                                .build();
                        });
                        
                } catch (Exception e) {
                    log.error("Error applying prompt template: {}", templateName, e);
                    return Mono.empty();
                } finally {
                    // DataBuffer会自动释放，不需要手动调用release()
                }
            });
    }

    /**
     * 解析LLM请求
     */
    private LlmRequest parseLlmRequest(String requestBody) {
        // 这里应该使用Jackson或其他JSON库解析
        // 为了简化，这里返回一个模拟对象
        LlmRequest request = new LlmRequest();
        // 实际实现中应该解析JSON
        return request;
    }

    /**
     * 序列化LLM请求
     */
    private String serializeLlmRequest(LlmRequest request) {
        // 这里应该使用Jackson或其他JSON库序列化
        // 为了简化，这里返回一个模拟JSON
        return "{}";
    }

    /**
     * 配置类
     */
    public static class Config {
        private boolean enabled = true;
        private String templatePath = "/templates";
        private List<String> variables = List.of();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getTemplatePath() {
            return templatePath;
        }

        public void setTemplatePath(String templatePath) {
            this.templatePath = templatePath;
        }

        public List<String> getVariables() {
            return variables;
        }

        public void setVariables(List<String> variables) {
            this.variables = variables;
        }
    }
}
