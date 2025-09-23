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
        try {
            ApiClient client = Config.defaultClient();
            // 设置连接超时
            client.setConnectTimeout(30000);
            client.setReadTimeout(60000);
            return client;
        } catch (Exception e) {
            // 如果无法连接到Kubernetes集群，返回一个默认的客户端
            ApiClient client = new ApiClient();
            client.setBasePath("http://localhost:8080");
            client.setConnectTimeout(30000);
            client.setReadTimeout(60000);
            return client;
        }
    }

    /**
     * 配置Networking V1 API
     */
    @Bean
    public NetworkingV1Api networkingV1Api(ApiClient apiClient) {
        return new NetworkingV1Api(apiClient);
    }
}
