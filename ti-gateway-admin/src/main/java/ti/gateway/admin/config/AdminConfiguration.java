package ti.gateway.admin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Admin配置类
 * 配置Admin相关的路由和属性
 */
@Configuration
public class AdminConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AdminConfiguration.class);

    public AdminConfiguration() {
        logger.info("AdminConfiguration initialized");
    }

    @Bean
//    @ConfigurationProperties("admin")
    public AdminProperties adminProperties() {
        logger.info("Creating AdminProperties bean");
        return new AdminProperties();
    }
}
