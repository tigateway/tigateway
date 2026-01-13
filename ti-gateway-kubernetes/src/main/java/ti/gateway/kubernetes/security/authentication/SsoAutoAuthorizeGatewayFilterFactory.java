package ti.gateway.kubernetes.security.authentication;

import ti.gateway.kubernetes.security.CommonSecurity;
import ti.gateway.kubernetes.security.DevMode;
import ti.gateway.kubernetes.security.RolesSecurityGatewayFilter;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@DevMode
@SsoAutoAuthorize
public class SsoAutoAuthorizeGatewayFilterFactory extends AbstractGatewayFilterFactory<SsoAutoAuthorizeGatewayFilterFactory.AuthoritiesProperties> {
    public SsoAutoAuthorizeGatewayFilterFactory() {
    }

    public SsoAutoAuthorizeGatewayFilterFactory.AuthoritiesProperties newConfig() {
        return new SsoAutoAuthorizeGatewayFilterFactory.AuthoritiesProperties();
    }

    public Class<SsoAutoAuthorizeGatewayFilterFactory.AuthoritiesProperties> getConfigClass() {
        return SsoAutoAuthorizeGatewayFilterFactory.AuthoritiesProperties.class;
    }

    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("authorities");
    }

    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    public GatewayFilter apply(SsoAutoAuthorizeGatewayFilterFactory.AuthoritiesProperties config) {
        SecurityWebFilterChain chain = CommonSecurity.configureCommonSecurity(ServerHttpSecurity.http()).addFilterAt(new SsoAutoAuthorizeGatewayFilterFactory.AuthWebFilter(this.parseGrantedAuthorities(config)), SecurityWebFiltersOrder.ANONYMOUS_AUTHENTICATION).authorizeExchange().anyExchange().permitAll().and().build();
        return new RolesSecurityGatewayFilter(chain);
    }

    private List<GrantedAuthority> parseGrantedAuthorities(SsoAutoAuthorizeGatewayFilterFactory.AuthoritiesProperties config) {
        return config.getAuthorities().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public class AuthWebFilter implements WebFilter {
        private List<GrantedAuthority> authorities;

        public AuthWebFilter(List<GrantedAuthority> authorities) {
            this.authorities = authorities;
        }

        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            return ReactiveSecurityContextHolder.getContext().switchIfEmpty(Mono.defer(() -> {
                OidcIdToken oidcIdToken = new OidcIdToken(UUID.randomUUID().toString(), Instant.now(), Instant.now().plusSeconds(3600L), Map.of("sub", UUID.randomUUID().toString(), "iss", UUID.randomUUID().toString()));
                DefaultOidcUser oidcUser = new DefaultOidcUser(this.authorities, oidcIdToken);
                Authentication authentication = new OAuth2AuthenticationToken(oidcUser, this.authorities, UUID.randomUUID().toString());
                return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(authentication)))).then(Mono.empty());
            })).flatMap((securityContext) -> {
                return chain.filter(exchange);
            });
        }
    }

    static class AuthoritiesProperties {
        private List<String> authorities;

        AuthoritiesProperties() {
        }

        public List<String> getAuthorities() {
            return this.authorities;
        }

        public void setAuthorities(List<String> authorities) {
            this.authorities = authorities;
        }
    }
}
