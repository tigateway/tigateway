package ti.gateway.kubernetes.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Roles Gateway Filter Factory
 * 
 * Note: Uses deprecated Spring Security API methods (deprecated in 6.1+).
 * These methods are still functional and will be migrated to new API when stable.
 */
@Component
public class RolesGatewayFilterFactory implements GatewayFilterFactory<RolesGatewayFilterFactory.RolesProperties> {
    private final Logger logger = LoggerFactory.getLogger(RolesGatewayFilterFactory.class);

    public RolesGatewayFilterFactory() {
    }

    public GatewayFilter apply(RolesGatewayFilterFactory.RolesProperties config) {
        SecurityWebFilterChain chain = CommonSecurity.configureCommonSecurity(ServerHttpSecurity.http()).authorizeExchange().anyExchange().access(this.hasAnyRole(config.getRoles())).and().build();
        return new RolesSecurityGatewayFilter(chain);
    }

    private ReactiveAuthorizationManager<AuthorizationContext> hasAnyRole(String[] roles) {
        return roles == null ? (authentication, object) -> {
            return Mono.empty();
        } : (authentication, object) -> {
            return authentication.map(Authentication::getAuthorities).filter(Objects::nonNull).doOnNext((grantedAuthorities) -> {
                this.logger.debug("User authorities: {}, required roles: {}", Strings.join(grantedAuthorities, ','), Strings.join(Arrays.asList(roles), ','));
            }).map((grantedAuthorities) -> {
                return grantedAuthorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
            }).map((authorities) -> {
                Set<String> roleSet = Arrays.stream(roles)
                        .map((s) -> "ROLE_" + s)
                        .collect(Collectors.toSet());
                boolean hasRole = roleSet.stream().anyMatch(authorities::contains);
                return new AuthorizationDecision(hasRole);
            });
        };
    }

    public RolesGatewayFilterFactory.RolesProperties newConfig() {
        return new RolesGatewayFilterFactory.RolesProperties();
    }

    public Class<RolesGatewayFilterFactory.RolesProperties> getConfigClass() {
        return RolesGatewayFilterFactory.RolesProperties.class;
    }

    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("roles");
    }

    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    public static class RolesProperties {
        private String[] roles;

        public RolesProperties() {
        }

        public String[] getRoles() {
            return this.roles;
        }

        public void setRoles(String[] roles) {
            this.roles = roles;
        }
    }
}