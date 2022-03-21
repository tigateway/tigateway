package io.pivotal.spring.cloud.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class IdTokenRelayGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    private final Logger log = LoggerFactory.getLogger(IdTokenRelayGatewayFilterFactory.class);

    public IdTokenRelayGatewayFilterFactory() {
    }

    public String name() {
        return "TokenRelay";
    }

    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            Mono<ServerWebExchange> serverWebExchangeMono = ReactiveSecurityContextHolder.getContext().map((context) -> {
                Authentication authentication = context.getAuthentication();
                if (authentication instanceof OAuth2AuthenticationToken) {
                    this.log.debug("Found OAuth2AuthenticationToken authentication object");
                    OAuth2User principal = ((OAuth2AuthenticationToken) authentication).getPrincipal();
                    if (principal instanceof OidcUser) {
                        this.log.debug("Principal is OpenID Connect user, including ID token into request");
                        OidcUser oidcUser = (OidcUser) principal;
                        return exchange.mutate().request((r) -> {
                            r.headers((h) -> {
                                h.setBearerAuth(oidcUser.getIdToken().getTokenValue());
                            });
                        }).build();
                    }
                }

                this.log.debug("No OpenID principal found");
                return exchange;
            }).defaultIfEmpty(exchange);
            Objects.requireNonNull(chain);
            return serverWebExchangeMono.flatMap(chain::filter);
        };
    }
}
