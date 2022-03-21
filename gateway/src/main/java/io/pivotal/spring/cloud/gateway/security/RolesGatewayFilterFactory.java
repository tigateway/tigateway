package io.pivotal.spring.cloud.gateway.security;

import java.util.Arrays;
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
import org.springframework.cloud.gateway.support.ShortcutConfigurable.ShortcutType;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
                return (Set)grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
            }).map((authorities) -> {
                Stream var10000 = Arrays.stream(roles).map((s) -> {
                    return "ROLE_" + s;
                });
                Objects.requireNonNull(authorities);
                boolean hasRole = var10000.anyMatch(authorities::contains);
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