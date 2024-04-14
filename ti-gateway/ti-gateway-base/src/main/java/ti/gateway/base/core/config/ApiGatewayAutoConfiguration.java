package ti.gateway.base.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.DispatcherHandler;
import ti.gateway.base.core.cache.AppServerCache;
import ti.gateway.base.core.cache.AppServerStorage;
import ti.gateway.base.core.cache.DefaultAppServerCache;
import ti.gateway.base.core.filter.AppServerFilter;
import ti.gateway.base.core.filter.AppkeySecretFilter;
import ti.gateway.base.core.cache.DefaultAppServerStorage;
import ti.gateway.base.core.server.AppServerCheck;
import ti.gateway.base.core.server.DefaultAppServerCheck;
import ti.gateway.base.core.sign.AccessAppSignCheck;
import ti.gateway.base.core.sign.DefaultAccessAppSignCheck;

/**
 * api gateway auto configuration
 */
@ConditionalOnProperty(name = "spring.cloud.gateway.enabled", matchIfMissing = true)
@Configuration
@ConditionalOnClass(DispatcherHandler.class)
@EnableConfigurationProperties({ApiGatewayCacheProperties.class})
public class ApiGatewayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AppServerCache appServerCache(ApiGatewayCacheProperties apiGatewayCacheProperties, AppServerStorage appServerStorage) {
        return new DefaultAppServerCache(apiGatewayCacheProperties, appServerStorage);
    }

    @Bean
    @ConditionalOnMissingBean
    public AppServerStorage appServerStorage() {
        return new DefaultAppServerStorage();
    }

    @ConditionalOnProperty(name = "spring.cloud.gateway.app.skip", havingValue = "false", matchIfMissing = true)
    @Configuration
    @EnableConfigurationProperties({ApiGatewayAppProperties.class})
    public static class ApiGatewayAppkeySecretAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public AppkeySecretFilter appkeySecretFilter(AccessAppSignCheck accessAppSignCheck) {
            return new AppkeySecretFilter(accessAppSignCheck);
        }

        @Bean
        @ConditionalOnMissingBean
        public AccessAppSignCheck accessAppSignCheck(ApiGatewayAppProperties apiGatewayAppProperties, AppServerCache appServerCache) {
            return new DefaultAccessAppSignCheck(apiGatewayAppProperties, appServerCache);
        }
    }

    @ConditionalOnProperty(name = "spring.cloud.gateway.server.skip", havingValue = "false", matchIfMissing = true)
    @Configuration
    @EnableConfigurationProperties({ApiGatewayServerProperties.class})
    public static class ApiGatewayServerAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public AppServerFilter appServerFilter(AppServerCheck appServerCheck) {
            return new AppServerFilter(appServerCheck);
        }

        @Bean
        @ConditionalOnMissingBean
        public AppServerCheck appServerCheck(ApiGatewayAppProperties apiGatewayAppProperties
                , AppServerCache appServerCache) {
            return new DefaultAppServerCheck(apiGatewayAppProperties, appServerCache);
        }
    }


}
