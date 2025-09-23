package ti.gateway.kubernetes;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.config.GatewayResilience4JCircuitBreakerAutoConfiguration;
import org.springframework.cloud.kubernetes.client.discovery.KubernetesDiscoveryClientAutoConfiguration;
import org.springframework.cloud.kubernetes.client.discovery.reactive.KubernetesInformerReactiveDiscoveryClientAutoConfiguration;

import java.security.Security;

@SpringBootApplication(
        exclude = {
            GatewayResilience4JCircuitBreakerAutoConfiguration.class,
            KubernetesDiscoveryClientAutoConfiguration.class,
            KubernetesInformerReactiveDiscoveryClientAutoConfiguration.class
        },
        scanBasePackages = {"com.vmware.scg.extensions", "io.pivotal.spring.cloud.gateway", "ti.gateway.kubernetes", "ti.gateway.admin"}
)
public class GatewayApplication {

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        SpringApplication.run(GatewayApplication.class, args);
    }

}
