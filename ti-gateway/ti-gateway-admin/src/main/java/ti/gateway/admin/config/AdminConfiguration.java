package ti.gateway.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Admin配置类
 * 配置Admin相关的路由和属性
 */
@Configuration
public class AdminConfiguration {

    @Bean
    @ConfigurationProperties("admin")
    public AdminProperties adminProperties() {
        return new AdminProperties();
    }
}
