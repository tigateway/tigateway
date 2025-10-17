package ti.gateway.kubernetes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;

/**
 * WebFlux 配置类
 * 
 * 提供 Spring Cloud Gateway 所需的 ServerCodecConfigurer bean
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Configuration
public class WebFluxConfiguration {

    /**
     * 配置 ServerCodecConfigurer
     * 
     * 这个 bean 是 Spring Cloud Gateway 的 modifyResponseBodyGatewayFilterFactory 所需要的
     */
    @Bean
    public ServerCodecConfigurer serverCodecConfigurer() {
        return ServerCodecConfigurer.create();
    }
}
