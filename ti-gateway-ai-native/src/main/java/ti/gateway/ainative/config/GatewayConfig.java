package ti.gateway.ainative.config;

import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Spring Cloud Gateway配置类
 * 
 * 提供Gateway相关的配置和bean
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Configuration
public class GatewayConfig {

    /**
     * 配置WebClient
     * 
     * 用于Gateway的HTTP客户端
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * 配置WebClient
     */
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }

    /**
     * 配置ServerCodecConfigurer
     * 
     * 这个bean是Spring Cloud Gateway所需的
     */
    @Bean
    @Primary
    public ServerCodecConfigurer gatewayServerCodecConfigurer() {
        return ServerCodecConfigurer.create();
    }

    /**
     * 配置Gateway属性
     */
    @Bean
    @Primary
    public GatewayProperties gatewayProperties() {
        return new GatewayProperties();
    }
}
