package ti.gateway.kubernetes.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@ConditionalOnProperty(
        value = {"spring.security.oauth2.client.provider.sso.issuer-uri"},
        matchIfMissing = true,
        havingValue = "not-a-real-url-to-match-missing-property-only"
)
/**
 * SSO Disabled Configuration
 * 
 * Configures default security filters when SSO is disabled using Spring Security 6.1+ API.
 */
public class SsoDisabledConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SsoDisabledConfiguration.class);

    public SsoDisabledConfiguration() {
    }


    @Bean
    public SecurityWebFilterChain defaultWebFilterChain(ServerHttpSecurity httpSecurity) {
        log.info("SSO is disabled, setting up default security filters");
        return CommonSecurity.configureCommonSecurity(httpSecurity)
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }
}