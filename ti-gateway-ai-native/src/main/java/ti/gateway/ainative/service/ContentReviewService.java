package ti.gateway.ainative.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ti.gateway.ainative.config.AiNativeProperties;
import ti.gateway.ainative.model.ContentReviewResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内容审核服务
 * 
 * 提供AI内容的安全审核功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class ContentReviewService {

    @Autowired
    private AiNativeProperties aiNativeProperties;

    private final Map<String, ContentReviewProvider> providers = new ConcurrentHashMap<>();

    /**
     * 审核内容
     */
    public Mono<ContentReviewResult> reviewContent(String content, List<String> reviewTypes) {
        String provider = aiNativeProperties.getAiSecurity().getContentReview().getProvider();
        
        return getProvider(provider)
            .flatMap(reviewProvider -> reviewProvider.reviewContent(content, reviewTypes))
            .onErrorResume(throwable -> {
                log.error("Error in content review", throwable);
                return Mono.just(ContentReviewResult.blocked("Content review service error"));
            });
    }

    /**
     * 获取审核提供者
     */
    private Mono<ContentReviewProvider> getProvider(String providerName) {
        ContentReviewProvider provider = providers.get(providerName);
        if (provider != null) {
            return Mono.just(provider);
        }

        // 创建提供者
        return createProvider(providerName)
            .doOnNext(p -> providers.put(providerName, p));
    }

    /**
     * 创建审核提供者
     */
    private Mono<ContentReviewProvider> createProvider(String providerName) {
        return Mono.fromCallable(() -> {
            switch (providerName.toLowerCase()) {
                case "alibaba":
                    return new AlibabaContentReviewProvider(aiNativeProperties);
                case "third-party":
                    return new ThirdPartyContentReviewProvider(aiNativeProperties);
                default:
                    return new DefaultContentReviewProvider();
            }
        });
    }

    /**
     * 内容审核提供者接口
     */
    public interface ContentReviewProvider {
        Mono<ContentReviewResult> reviewContent(String content, List<String> reviewTypes);
    }

    /**
     * 阿里云内容审核提供者
     */
    public static class AlibabaContentReviewProvider implements ContentReviewProvider {
        private final AiNativeProperties properties;

        public AlibabaContentReviewProvider(AiNativeProperties properties) {
            this.properties = properties;
        }

        @Override
        public Mono<ContentReviewResult> reviewContent(String content, List<String> reviewTypes) {
            // 模拟阿里云内容审核
            return Mono.fromCallable(() -> {
                // 这里应该调用阿里云内容安全API
                // 为了演示，这里返回一个模拟结果
                
                // 简单的关键词检测
                if (containsSensitiveContent(content)) {
                    return ContentReviewResult.blocked("Sensitive content detected");
                }
                
                return ContentReviewResult.passed();
            });
        }

        private boolean containsSensitiveContent(String content) {
            // 简单的敏感词检测
            String[] sensitiveWords = {"暴力", "色情", "政治", "敏感"};
            String lowerContent = content.toLowerCase();
            
            for (String word : sensitiveWords) {
                if (lowerContent.contains(word)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 第三方内容审核提供者
     */
    public static class ThirdPartyContentReviewProvider implements ContentReviewProvider {
        private final AiNativeProperties properties;

        public ThirdPartyContentReviewProvider(AiNativeProperties properties) {
            this.properties = properties;
        }

        @Override
        public Mono<ContentReviewResult> reviewContent(String content, List<String> reviewTypes) {
            // 模拟第三方内容审核
            return Mono.fromCallable(() -> {
                // 这里应该调用第三方内容审核API
                // 为了演示，这里返回一个模拟结果
                
                // 基于内容长度的简单检测
                if (content.length() > 10000) {
                    return ContentReviewResult.blocked("Content too long");
                }
                
                return ContentReviewResult.passed();
            });
        }
    }

    /**
     * 默认内容审核提供者
     */
    public static class DefaultContentReviewProvider implements ContentReviewProvider {
        @Override
        public Mono<ContentReviewResult> reviewContent(String content, List<String> reviewTypes) {
            // 默认通过所有内容
            return Mono.just(ContentReviewResult.passed());
        }
    }
}
