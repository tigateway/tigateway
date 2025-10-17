package ti.gateway.ainative.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * AI原生网关配置属性
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "tigateway.ai")
public class AiNativeProperties {

    /**
     * 是否启用AI原生功能
     */
    private boolean enabled = true;

    /**
     * AI开发插件配置
     */
    private AiDevelopment aiDevelopment = new AiDevelopment();

    /**
     * AI安全防护配置
     */
    private AiSecurity aiSecurity = new AiSecurity();

    /**
     * 多模型适配配置
     */
    private MultiModel multiModel = new MultiModel();

    /**
     * 可观测性配置
     */
    private Observability observability = new Observability();

    /**
     * AI开发插件配置
     */
    @Data
    public static class AiDevelopment {
        /**
         * LLM缓存配置
         */
        private LlmCache llmCache = new LlmCache();

        /**
         * 提示词模板配置
         */
        private PromptTemplate promptTemplate = new PromptTemplate();

        /**
         * 提示词装饰器配置
         */
        private PromptDecorator promptDecorator = new PromptDecorator();

        /**
         * 请求转换配置
         */
        private RequestTransformation requestTransformation = new RequestTransformation();

        /**
         * 响应转换配置
         */
        private ResponseTransformation responseTransformation = new ResponseTransformation();

        /**
         * 向量检索配置
         */
        private VectorRetrieval vectorRetrieval = new VectorRetrieval();
    }

    /**
     * AI安全防护配置
     */
    @Data
    public static class AiSecurity {
        /**
         * 内容审核配置
         */
        private ContentReview contentReview = new ContentReview();

        /**
         * Token限流配置
         */
        private TokenRateLimit tokenRateLimit = new TokenRateLimit();

        /**
         * Token配额配置
         */
        private TokenQuota tokenQuota = new TokenQuota();
    }

    /**
     * 多模型适配配置
     */
    @Data
    public static class MultiModel {
        /**
         * AI代理配置
         */
        private AiProxy aiProxy = new AiProxy();

        /**
         * 支持的模型列表
         */
        private List<ModelConfig> models = List.of();
    }

    /**
     * 可观测性配置
     */
    @Data
    public static class Observability {
        /**
         * AI统计配置
         */
        private AiStatistics aiStatistics = new AiStatistics();

        /**
         * LLM访问日志配置
         */
        private LlmAccessLog llmAccessLog = new LlmAccessLog();

        /**
         * Token消费观测配置
         */
        private TokenConsumptionObservation tokenConsumptionObservation = new TokenConsumptionObservation();

        /**
         * 可用性告警配置
         */
        private AvailabilityAlert availabilityAlert = new AvailabilityAlert();
    }

    // 内部配置类
    @Data
    public static class LlmCache {
        private boolean enabled = true;
        private int maxSize = 1000;
        private long ttl = 3600; // seconds
        private String cacheType = "redis"; // redis, memory
    }

    @Data
    public static class PromptTemplate {
        private boolean enabled = true;
        private String templatePath = "/templates";
        private Map<String, String> templates = Map.of();
    }

    @Data
    public static class PromptDecorator {
        private boolean enabled = true;
        private List<String> decorators = List.of();
    }

    @Data
    public static class RequestTransformation {
        private boolean enabled = true;
        private Map<String, String> transformations = Map.of();
    }

    @Data
    public static class ResponseTransformation {
        private boolean enabled = true;
        private Map<String, String> transformations = Map.of();
    }

    @Data
    public static class VectorRetrieval {
        private boolean enabled = true;
        private String vectorDatabase = "redis";
        private String indexName = "ai_vectors";
        private int topK = 10;
    }

    @Data
    public static class ContentReview {
        private boolean enabled = true;
        private String provider = "alibaba"; // alibaba, third-party
        private Map<String, String> config = Map.of();
    }

    @Data
    public static class TokenRateLimit {
        private boolean enabled = true;
        private int requestsPerMinute = 100;
        private int tokensPerMinute = 10000;
    }

    @Data
    public static class TokenQuota {
        private boolean enabled = true;
        private long dailyQuota = 100000;
        private long monthlyQuota = 3000000;
    }

    @Data
    public static class AiProxy {
        private boolean enabled = true;
        private int maxRetries = 3;
        private long timeout = 30000; // milliseconds
        private boolean fallbackEnabled = true;
    }

    @Data
    public static class ModelConfig {
        private String name;
        private String provider;
        private String endpoint;
        private String apiKey;
        private Map<String, Object> parameters = Map.of();
    }

    @Data
    public static class AiStatistics {
        private boolean enabled = true;
        private int collectionInterval = 30; // seconds
    }

    @Data
    public static class LlmAccessLog {
        private boolean enabled = true;
        private String logLevel = "INFO";
        private boolean includeRequest = true;
        private boolean includeResponse = false;
    }

    @Data
    public static class TokenConsumptionObservation {
        private boolean enabled = true;
        private int observationInterval = 60; // seconds
    }

    @Data
    public static class AvailabilityAlert {
        private boolean enabled = true;
        private double threshold = 0.95; // 95% availability threshold
        private List<String> alertChannels = List.of("email", "webhook");
    }
}
