package ti.gateway.base.storage.configmap.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.openapi.models.V1Pod;
import org.springframework.cloud.kubernetes.commons.PodUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ti.gateway.base.core.cache.AppServerStorage;
import ti.gateway.base.storage.configmap.ConfigMapAppInfoRepository;
import ti.gateway.base.storage.configmap.ConfigMapAppServerStorage;
import ti.gateway.base.storage.configmap.impl.ConfigMapAppInfoRepositoryImpl;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * ConfigMap存储自动配置
 */
@Configuration
@ConditionalOnClass({CoreV1Api.class, ApiClient.class})
@ConditionalOnProperty(
    name = "spring.cloud.gateway.storage.configmap.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(ConfigMapStorageProperties.class)
public class ConfigMapStorageAutoConfiguration {

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
     * 配置Core V1 API
     */
    @Bean
    @Primary
    public CoreV1Api coreV1Api(ApiClient apiClient) {
        return new CoreV1Api(apiClient);
    }

    /**
     * 配置ConfigMap应用信息仓库
     */
    @Bean
    public ConfigMapAppInfoRepository configMapAppInfoRepository() {
        return new ConfigMapAppInfoRepositoryImpl();
    }

    /**
     * 配置基于ConfigMap的AppServer存储
     */
    @Bean
    @Primary
    public AppServerStorage configMapAppServerStorage() {
        return new ConfigMapAppServerStorage();
    }

    /**
     * 配置PodUtils Bean（用于Kubernetes健康检查）
     */
    @Bean
    @Primary
    public PodUtils<V1Pod> podUtils() {
        return new PodUtils<V1Pod>() {
            @Override
            public boolean isInsideKubernetes() {
                return false; // 在非Kubernetes环境中运行
            }
            
            @Override
            public Supplier<V1Pod> currentPod() {
                return () -> null; // 在非Kubernetes环境中返回null
            }
        };
    }
}
