package ti.gateway.kubernetes;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.config.GatewayResilience4JCircuitBreakerAutoConfiguration;
import org.springframework.cloud.kubernetes.client.discovery.reactive.KubernetesInformerReactiveDiscoveryClientAutoConfiguration;
import org.springframework.cloud.kubernetes.client.KubernetesClientAutoConfiguration;
import org.springframework.cloud.kubernetes.client.KubernetesClientActuatorConfiguration;

import java.security.Security;

/**
 * TiGateway Kubernetes Application
 * 
 * Main entry point for TiGateway running in Kubernetes environment.
 * 
 * Features:
 * - Spring Cloud Gateway integration
 * - Kubernetes Ingress auto-discovery
 * - Admin UI integration
 * - MCP (Model Context Protocol) support
 * - Prometheus metrics export
 * 
 * @author TiGateway Team
 */
@SpringBootApplication(
        exclude = {
            // Exclude Resilience4J Circuit Breaker (using custom implementation)
            GatewayResilience4JCircuitBreakerAutoConfiguration.class,
            // Exclude Kubernetes auto-configurations to use custom implementations
            KubernetesInformerReactiveDiscoveryClientAutoConfiguration.class,
            KubernetesClientAutoConfiguration.class,
            KubernetesClientActuatorConfiguration.class
        },
        scanBasePackages = {
            "ti.gateway.kubernetes",
            "ti.gateway.admin",
            "ti.gateway.base",
            "ti.gateway.mcp"
        }
)
public class GatewayApplication {

    public static void main(String[] args) {
        // Add BouncyCastle security provider for JWT and TLS operations
        Security.addProvider(new BouncyCastleProvider());
        
        // Run Spring Boot application
        SpringApplication.run(GatewayApplication.class, args);
    }

}
