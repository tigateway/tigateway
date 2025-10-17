package ti.gateway.ainative.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * WebFlux配置类
 * 
 * 提供WebFlux相关的配置，包括ServerCodecConfigurer
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    /**
     * 配置ServerCodecConfigurer
     * 
     * 这个bean是Spring Cloud Gateway所需的，用于处理HTTP消息的编解码
     */
    @Bean
    public ServerCodecConfigurer serverCodecConfigurer() {
        return ServerCodecConfigurer.create();
    }

    /**
     * 配置WebFlux
     */
    @Override
    public void configureHttpMessageCodecs(org.springframework.http.codec.ServerCodecConfigurer configurer) {
        // 配置消息编解码器
        configurer.defaultCodecs().maxInMemorySize(1024 * 1024); // 1MB
    }
}
