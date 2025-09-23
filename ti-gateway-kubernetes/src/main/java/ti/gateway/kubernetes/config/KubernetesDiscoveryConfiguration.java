package ti.gateway.kubernetes.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;

/**
 * Kubernetes Discovery Configuration
 * 配置Kubernetes服务发现
 */
@Configuration
public class KubernetesDiscoveryConfiguration {
    
    /**
     * 当Kubernetes发现被禁用时，提供一个简单的发现客户端
     */
    @Bean("fallbackDiscoveryClient")
    @ConditionalOnProperty(
        value = "spring.cloud.kubernetes.discovery.enabled",
        havingValue = "false",
        matchIfMissing = true
    )
    public DiscoveryClient fallbackDiscoveryClient() {
        return new SimpleDiscoveryClient(new SimpleDiscoveryProperties());
    }
}
