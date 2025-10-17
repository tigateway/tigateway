package ti.gateway.ainative.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ti.gateway.ainative.model.LlmRequest;
import ti.gateway.ainative.model.PromptTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提示词模板服务
 * 
 * 提供提示词模板的管理和应用功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class PromptTemplateService {

    @Autowired
    private ResourceLoader resourceLoader;

    private final Map<String, PromptTemplate> templateCache = new ConcurrentHashMap<>();

    /**
     * 应用提示词模板
     */
    public Mono<LlmRequest> applyTemplate(String templateName, LlmRequest request, List<String> variables) {
        return getTemplate(templateName)
            .map(template -> {
                // 应用模板到请求
                LlmRequest modifiedRequest = applyTemplateToRequest(template, request, variables);
                log.debug("Applied template '{}' to request", templateName);
                return modifiedRequest;
            })
            .onErrorResume(throwable -> {
                log.error("Error applying template '{}'", templateName, throwable);
                return Mono.just(request); // 返回原始请求
            });
    }

    /**
     * 获取模板
     */
    public Mono<PromptTemplate> getTemplate(String templateName) {
        // 先从缓存获取
        PromptTemplate cachedTemplate = templateCache.get(templateName);
        if (cachedTemplate != null) {
            return Mono.just(cachedTemplate);
        }

        // 从文件系统加载
        return loadTemplateFromFile(templateName)
            .doOnNext(template -> {
                // 缓存模板
                templateCache.put(templateName, template);
                log.debug("Loaded and cached template: {}", templateName);
            });
    }

    /**
     * 从文件加载模板
     */
    private Mono<PromptTemplate> loadTemplateFromFile(String templateName) {
        String templatePath = "classpath:templates/" + templateName + ".json";
        
        return Mono.fromCallable(() -> {
            Resource resource = resourceLoader.getResource(templatePath);
            if (!resource.exists()) {
                throw new RuntimeException("Template not found: " + templateName);
            }
            
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return parseTemplate(content);
        })
        .onErrorResume(throwable -> {
            log.error("Error loading template from file: {}", templateName, throwable);
            return Mono.error(throwable);
        });
    }

    /**
     * 解析模板内容
     */
    private PromptTemplate parseTemplate(String content) {
        // 这里应该使用Jackson解析JSON
        // 为了简化，这里返回一个模拟对象
        PromptTemplate template = new PromptTemplate();
        template.setName("default");
        template.setContent(content);
        return template;
    }

    /**
     * 应用模板到请求
     */
    private LlmRequest applyTemplateToRequest(PromptTemplate template, LlmRequest request, List<String> variables) {
        // 创建修改后的请求
        LlmRequest modifiedRequest = new LlmRequest();
        
        // 复制原始请求的属性
        modifiedRequest.setModel(request.getModel());
        modifiedRequest.setTemperature(request.getTemperature());
        modifiedRequest.setMaxTokens(request.getMaxTokens());
        modifiedRequest.setTopP(request.getTopP());
        modifiedRequest.setFrequencyPenalty(request.getFrequencyPenalty());
        modifiedRequest.setPresencePenalty(request.getPresencePenalty());
        modifiedRequest.setStream(request.getStream());
        modifiedRequest.setUser(request.getUser());
        modifiedRequest.setMetadata(request.getMetadata());
        modifiedRequest.setRequestId(request.getRequestId());
        modifiedRequest.setTimestamp(request.getTimestamp());

        // 应用模板到消息
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            // 这里应该根据模板修改消息内容
            // 为了简化，这里直接复制原始消息
            modifiedRequest.setMessages(request.getMessages());
        }

        return modifiedRequest;
    }

    /**
     * 清除模板缓存
     */
    public void clearTemplateCache() {
        templateCache.clear();
        log.info("Cleared prompt template cache");
    }

    /**
     * 获取模板缓存统计
     */
    public Map<String, Object> getTemplateCacheStats() {
        return Map.of(
            "cachedTemplates", templateCache.size(),
            "templateNames", templateCache.keySet()
        );
    }

    /**
     * 重新加载模板
     */
    public Mono<Boolean> reloadTemplate(String templateName) {
        return getTemplate(templateName)
            .map(template -> {
                templateCache.put(templateName, template);
                log.info("Reloaded template: {}", templateName);
                return true;
            })
            .onErrorResume(throwable -> {
                log.error("Error reloading template: {}", templateName, throwable);
                return Mono.just(false);
            });
    }
}
