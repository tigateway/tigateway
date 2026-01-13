package ti.gateway.kubernetes.ratelimit;

import ti.gateway.kubernetes.jwt.JwtHelper;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory.Config;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.support.ipresolver.RemoteAddressResolver;
import org.springframework.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
class RateLimitGatewayFilterFactory implements GatewayFilterFactory<RateLimiterProperties> {
    private static final Logger LOG = LoggerFactory.getLogger(RateLimitGatewayFilterFactory.class);
    static final String DEFAULT_RATE_LIMIT_MAP = "GLOBAL_RATE_LIMIT";
    private final DefaultRateLimiter defaultRateLimiter;
    private final RequestRateLimiterGatewayFilterFactory requestRateLimiterGatewayFilterFactory;

    RateLimitGatewayFilterFactory(DefaultRateLimiter defaultRateLimiter) {
        this.defaultRateLimiter = defaultRateLimiter;
        this.requestRateLimiterGatewayFilterFactory = new RequestRateLimiterGatewayFilterFactory(defaultRateLimiter, (exchange) -> {
            return Mono.just("GLOBAL_RATE_LIMIT");
        });
    }

    public GatewayFilter apply(RateLimiterProperties config) {
        this.defaultRateLimiter.getConfig().put(config.getRouteId(), config);
        Config requestRateLimiterConfig = new Config();
        requestRateLimiterConfig.setRouteId(config.getRouteId());
        if (StringUtils.hasText(config.getKeyLocation())) {
            KeyResolver keyResolver;
            if (config.hasClaim()) {
                keyResolver = (exchange) -> {
                    return this.getKeyFromClaim(exchange, config.getClaim());
                };
            } else if (config.hasHeader()) {
                keyResolver = (exchange) -> {
                    return this.getKeyFromHeader(exchange, config.getHeader());
                };
            } else {
                keyResolver = (exchange) -> {
                    return this.getKeyFromIpAddress(exchange, config.getIPs(), config.getXForwardedForMaxTrustedIndex());
                };
            }

            requestRateLimiterConfig.setKeyResolver(keyResolver);
        }

        return this.requestRateLimiterGatewayFilterFactory.apply(requestRateLimiterConfig);
    }

    private Mono<String> getKeyFromClaim(ServerWebExchange exchange, String claimName) {
        Mono<String> key = ReactiveSecurityContextHolder.getContext().map((context) -> {
            Authentication authentication = context.getAuthentication();
            Object claimValue = null;
            if (authentication instanceof JwtAuthenticationToken) {
                claimValue = ((JwtAuthenticationToken)authentication).getTokenAttributes().get(claimName);
            }

            if (authentication instanceof OAuth2AuthenticationToken) {
                claimValue = ((OAuth2AuthenticationToken)authentication).getPrincipal().getAttributes().get(claimName);
            }

            String returnValue = JwtHelper.getClaimAsString(claimValue);
            return returnValue == null ? "MISSING_RATE_LIMIT_KEY" : returnValue;
        }).switchIfEmpty(Mono.fromSupplier(() -> {
            return this.getClaimFromAuthorizationHeaderToken(exchange, claimName);
        }));
        return key;
    }

    private Mono<String> getKeyFromHeader(ServerWebExchange exchange, String headerName) {
        List<String> headerValues = exchange.getRequest().getHeaders().get(headerName);
        if (headerValues != null && !headerValues.isEmpty()) {
            if (headerValues.size() > 0) {
                LOG.warn("Multiple values found for {}, using first one", headerName);
            }

            return Mono.just((String)headerValues.get(0));
        } else {
            return Mono.just("MISSING_RATE_LIMIT_KEY");
        }
    }

    private Mono<String> getKeyFromIpAddress(ServerWebExchange exchange, List<String> allowedIPs, int maxTrustedIndex) {
        RemoteAddressResolver remoteAddressResolver = XForwardedRemoteAddressResolver.maxTrustedIndex(maxTrustedIndex);
        InetSocketAddress remoteAddress = remoteAddressResolver.resolve(exchange);
        if (remoteAddress != null) {
            String ip = remoteAddress.getAddress().getHostAddress();
            if (allowedIPs.contains(ip)) {
                return Mono.just(ip);
            }
        }

        return Mono.empty();
    }

    private String getClaimFromAuthorizationHeaderToken(ServerWebExchange exchange, String claimName) {
        List<String> authHeaders = exchange.getRequest().getHeaders().getOrDefault("Authorization", Collections.emptyList());
        Optional<String> authorizationHeader = authHeaders.stream().findFirst();
        if (authorizationHeader.isEmpty()) {
            return "MISSING_RATE_LIMIT_KEY";
        } else {
            String sanitizedHeader = JwtHelper.cleanupHeaderValue(authorizationHeader.get());
            return JwtHelper.getClaimAsString(sanitizedHeader, claimName);
        }
    }

    public RateLimiterProperties newConfig() {
        return new RateLimiterProperties();
    }

    public Class<RateLimiterProperties> getConfigClass() {
        return RateLimiterProperties.class;
    }

    public List<String> shortcutFieldOrder() {
        return Arrays.asList("limit", "duration", "keyLocation");
    }
}
