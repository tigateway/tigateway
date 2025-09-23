package ti.gateway.kubernetes.ingress;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.util.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

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

    /**
     * 配置Kubernetes API客户端
     */
    @Bean
    @Primary
    public ApiClient kubernetesApiClient() throws IOException {
        ApiClient client = Config.defaultClient();
        // 设置连接超时
        client.setConnectTimeout(30000);
        client.setReadTimeout(60000);
        return client;
    }

    /**
     * 配置Networking V1 API
     */
    @Bean
    public NetworkingV1Api networkingV1Api(ApiClient apiClient) {
        return new NetworkingV1Api(apiClient);
    }
}
