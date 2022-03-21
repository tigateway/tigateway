package io.pivotal.spring.cloud.gateway.security;

import java.util.Objects;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class RolesSecurityGatewayFilter implements GatewayFilter {
    private final SecurityWebFilterChain webFilterChain;

    public RolesSecurityGatewayFilter(SecurityWebFilterChain securityWebFilterChain) {
        this.webFilterChain = securityWebFilterChain;
    }

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        WebFilterChainProxy webFilterChainProxy = new WebFilterChainProxy(new SecurityWebFilterChain[]{this.webFilterChain});
        Objects.requireNonNull(chain);
        return webFilterChainProxy.filter(exchange, chain::filter);
    }
}
