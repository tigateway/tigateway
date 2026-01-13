package ti.gateway.kubernetes.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

/**
 * Scopes Gateway Filter Factory
 * 
 * Configures scope-based authorization for TiGateway routes using Spring Security 6.1+ API.
 */
@Component
@SsoEnabled
public class ScopesGatewayFilterFactory implements GatewayFilterFactory<ScopesProperties> {
    private final Logger logger = LoggerFactory.getLogger(ScopesGatewayFilterFactory.class);
    private final ReactiveJwtDecoder reactiveJwtDecoder;

    ScopesGatewayFilterFactory(ReactiveJwtDecoder gatewayReactiveJwtDecoder) {
        this.reactiveJwtDecoder = gatewayReactiveJwtDecoder;
    }

    public GatewayFilter apply(ScopesProperties config) {
        SecurityWebFilterChain chain = CommonSecurity.configureCommonSecurity(ServerHttpSecurity.http())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(this.reactiveJwtDecoder)))
                .authorizeExchange(exchanges -> exchanges.anyExchange().access(this.hasAnyScopes(config.getScopes())))
                .build();
        return new SecurityGatewayFilter(chain);
    }

    private ReactiveAuthorizationManager<AuthorizationContext> hasAnyScopes(String[] scopes) {
        return (authentication, context) -> {
            return authentication.flatMap((auth) -> {
                return scopes != null && scopes.length != 0 && !CollectionUtils.isEmpty(auth.getAuthorities()) ? Mono.just(this.validateJwtToken(scopes, auth)) : Mono.empty();
            });
        };
    }

    private AuthorizationDecision validateJwtToken(String[] scopes, Authentication authentication) {
        Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
        this.logger.debug("Token scopes {}, required scopes {}", Strings.join(grantedAuthorities, ','), Strings.join(Arrays.asList(scopes), ','));
        Set<String> authorityNames = (Set<String>) grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        Stream<String> stringStream = Arrays.stream(scopes).map((s) -> {
            return "SCOPE_" + s;
        });
        Objects.requireNonNull(authorityNames);
        boolean hasAnyScope = stringStream.anyMatch(authorityNames::contains);
        return new AuthorizationDecision(hasAnyScope);
    }

    public ScopesProperties newConfig() {
        return new ScopesProperties();
    }

    public Class<ScopesProperties> getConfigClass() {
        return ScopesProperties.class;
    }

    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("scopes");
    }

    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }
}

