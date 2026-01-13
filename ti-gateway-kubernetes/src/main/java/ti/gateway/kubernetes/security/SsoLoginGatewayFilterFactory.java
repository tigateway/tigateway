package ti.gateway.kubernetes.security;

import ti.gateway.kubernetes.security.authentication.RefreshIdTokenAuthenticationConverter;
import ti.gateway.kubernetes.security.authentication.RefreshIdTokenAuthenticationManager;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * SSO Login Gateway Filter Factory
 * 
 * Configures SSO login for TiGateway routes using Spring Security 6.1+ API.
 */
@Component
@SsoEnabled
@SuppressWarnings("unused")
public class SsoLoginGatewayFilterFactory implements GatewayFilterFactory<SsoLoginConfiguration> {
    @SuppressWarnings("unused")
    private static final String DEFAULT_SSO_CLIENT_REGISTRATION_ID = "sso";
    private final ReactiveClientRegistrationRepository clientRegistrationRepository;
    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;
    private final ReactiveJwtDecoder reactiveJwtDecoder;
    private final RefreshIdTokenAuthenticationManager refreshIdTokenAuthenticationManager;
    private final RolesJwtAuthenticationConverter rolesJwtAuthenticationConverter;

    SsoLoginGatewayFilterFactory(ReactiveClientRegistrationRepository clientRegistrationRepository, ServerOAuth2AuthorizedClientRepository authorizedClientRepository, ReactiveJwtDecoder gatewayReactiveJwtDecoder, RefreshIdTokenAuthenticationManager refreshIdTokenAuthenticationManager, RolesJwtAuthenticationConverter rolesJwtAuthenticationConverter) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizedClientRepository = authorizedClientRepository;
        this.reactiveJwtDecoder = gatewayReactiveJwtDecoder;
        this.refreshIdTokenAuthenticationManager = refreshIdTokenAuthenticationManager;
        this.rolesJwtAuthenticationConverter = rolesJwtAuthenticationConverter;
    }

    public GatewayFilter apply(SsoLoginConfiguration config) {
        String registrationId = config.getClientRegistrationId() == null ? "sso" : config.getClientRegistrationId();
        AuthenticationWebFilter refreshIdTokenAuthenticationWebFilter = new AuthenticationWebFilter(this.refreshIdTokenAuthenticationManager);
        refreshIdTokenAuthenticationWebFilter.setSecurityContextRepository(new WebSessionServerSecurityContextRepository());
        refreshIdTokenAuthenticationWebFilter.setServerAuthenticationConverter(new RefreshIdTokenAuthenticationConverter(this.authorizedClientRepository));
        SecurityWebFilterChain chain = CommonSecurity.configureCommonSecurity(ServerHttpSecurity.http())
                .oauth2Login(oauth2 -> oauth2
                        .clientRegistrationRepository(this.clientRegistrationRepository)
                        .authorizedClientRepository(this.authorizedClientRepository))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(this.reactiveJwtDecoder)
                                .jwtAuthenticationConverter(this.rolesJwtAuthenticationConverter)))
                .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/" + registrationId)))
                .addFilterAfter(refreshIdTokenAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
        return new SecurityGatewayFilter(chain);
    }

    public SsoLoginConfiguration newConfig() {
        return new SsoLoginConfiguration();
    }

    public Class<SsoLoginConfiguration> getConfigClass() {
        return SsoLoginConfiguration.class;
    }
}
