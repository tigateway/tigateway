package ti.gateway.ainative;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayResilience4JCircuitBreakerAutoConfiguration;
import ti.gateway.ainative.config.AiNativeProperties;

/**
 * TiGateway AI Native Application
 * 
 * AI原生API网关主应用类，提供AI应用开发、安全防护、多模型适配等功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@SpringBootApplication(
    exclude = {
        GatewayResilience4JCircuitBreakerAutoConfiguration.class
    },
    scanBasePackages = {
        "ti.gateway.ainative",
        "ti.gateway.admin", 
        "ti.gateway.base",
        "ti.gateway.mcp"
    }
)
@EnableConfigurationProperties(AiNativeProperties.class)
public class TiGatewayAiNativeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiGatewayAiNativeApplication.class, args);
    }
}
