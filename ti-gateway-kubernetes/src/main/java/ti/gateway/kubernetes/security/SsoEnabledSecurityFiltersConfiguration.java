package ti.gateway.kubernetes.security;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Configuration
@SsoEnabled
public class SsoEnabledSecurityFiltersConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(SsoEnabledSecurityFiltersConfiguration.class);
    private static final String LOGOUT_PATH = "/scg-logout";

    public SsoEnabledSecurityFiltersConfiguration() {
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        LOGGER.info("SSO is enabled, setting up OAuth2 login");
        return CommonSecurity.configureCommonSecurity(httpSecurity).oauth2Login().and().build();
    }

    @Bean
    ReactiveJwtDecoder gatewayReactiveJwtDecoder(@Value("${spring.security.oauth2.client.provider.sso.issuer-uri}") String ssoIssuerUri) {
        return ReactiveJwtDecoders.fromOidcIssuerLocation(ssoIssuerUri);
    }

    @Bean
    RolesJwtAuthenticationConverter rolesJwtAuthenticationConverter(RolesExtractor rolesExtractor) {
        ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverterAdapter(new JwtAuthenticationConverter());
        return new RolesJwtAuthenticationConverter(rolesExtractor, jwtAuthenticationConverter);
    }

    @Bean
    @Order(-2147483648)
    SecurityWebFilterChain logoutWebFilterChain(ServerHttpSecurity httpSecurity) {
        return CommonSecurity.configureCommonSecurity(httpSecurity).securityMatcher(new PathPatternParserServerWebExchangeMatcher("/scg-logout")).logout().requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, new String[]{"/scg-logout"})).logoutSuccessHandler(new SsoEnabledSecurityFiltersConfiguration.RedirectAwareLogoutSuccessHandler()).and().build();
    }

    private static class RedirectAwareLogoutSuccessHandler implements ServerLogoutSuccessHandler {
        private final RedirectServerLogoutSuccessHandler redirect = new RedirectServerLogoutSuccessHandler();

        private RedirectAwareLogoutSuccessHandler() {
        }

        public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
            String redirectUri = (String)exchange.getExchange().getRequest().getQueryParams().getFirst("redirect");
            if (redirectUri != null) {
                this.redirect.setLogoutSuccessUrl(URI.create(redirectUri));
                return this.redirect.onLogoutSuccess(exchange, authentication);
            } else {
                return Mono.empty();
            }
        }
    }
}

