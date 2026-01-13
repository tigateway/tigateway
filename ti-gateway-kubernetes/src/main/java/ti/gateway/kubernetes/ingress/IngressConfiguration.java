package ti.gateway.kubernetes.ingress;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Kubernetes Ingress Configuration
 * 配置Kubernetes客户端和API
 */
@Configuration
@ConditionalOnProperty(
    value = "spring.cloud.gateway.kubernetes.ingress.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class IngressConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(IngressConfiguration.class);

    /**
     * 配置Kubernetes API客户端
     */
    @Bean
    @Primary
    public ApiClient kubernetesApiClient() {
        try {
            logger.info("Initializing Kubernetes API client...");
            ApiClient client = Config.defaultClient();
            // 设置连接超时
            client.setConnectTimeout(30000);
            client.setReadTimeout(60000);
            logger.info("Kubernetes API client initialized successfully");
            return client;
        } catch (Exception e) {
            logger.warn("Failed to initialize Kubernetes API client from default config, using fallback: {}", e.getMessage());
            // 如果无法连接到Kubernetes集群，返回一个默认的客户端
            ApiClient client = new ApiClient();
            client.setBasePath("http://localhost:8080");
            client.setConnectTimeout(30000);
            client.setReadTimeout(60000);
            logger.info("Using fallback Kubernetes API client");
            return client;
        }
    }

    /**
     * 配置Networking V1 API
     */
    @Bean
    public NetworkingV1Api networkingV1Api(ApiClient apiClient) {
        logger.info("Creating NetworkingV1Api bean");
        return new NetworkingV1Api(apiClient);
    }
}
