package ti.gateway.kubernetes;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.config.GatewayResilience4JCircuitBreakerAutoConfiguration;
// import org.springframework.cloud.kubernetes.client.discovery.KubernetesDiscoveryClientAutoConfiguration; // Class not found in Spring Cloud 2023.0.0
import org.springframework.cloud.kubernetes.client.discovery.reactive.KubernetesInformerReactiveDiscoveryClientAutoConfiguration;
import org.springframework.cloud.kubernetes.client.KubernetesClientAutoConfiguration;
import org.springframework.cloud.kubernetes.client.KubernetesClientActuatorConfiguration;

import java.security.Security;

@SpringBootApplication(
        exclude = {
            GatewayResilience4JCircuitBreakerAutoConfiguration.class,
            // KubernetesDiscoveryClientAutoConfiguration.class, // Class not found in Spring Cloud 2023.0.0
            KubernetesInformerReactiveDiscoveryClientAutoConfiguration.class,
            KubernetesClientAutoConfiguration.class,
            KubernetesClientActuatorConfiguration.class
        },
        scanBasePackages = {"ti.gateway.kubernetes", "ti.gateway.admin", "ti.gateway.base", "ti.gateway.mcp"},
        scanBasePackageClasses = {}
)
public class GatewayApplication {

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        SpringApplication.run(GatewayApplication.class, args);
    }

}
