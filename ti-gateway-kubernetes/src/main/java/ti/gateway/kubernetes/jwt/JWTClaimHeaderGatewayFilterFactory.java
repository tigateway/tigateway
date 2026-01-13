package ti.gateway.kubernetes.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class JWTClaimHeaderGatewayFilterFactory implements GatewayFilterFactory<JWTClaimHeaderGatewayFilterFactory.Config> {
    private final Logger log = LoggerFactory.getLogger(JWTClaimHeaderGatewayFilterFactory.class);

    public JWTClaimHeaderGatewayFilterFactory() {
    }

    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Mono<ServerWebExchange> serverWebExchangeMono = ReactiveSecurityContextHolder.getContext().map((context) -> {
                ServerWebExchange mutatedExchange = this.addClaimFromSessionToken(config, exchange, context.getAuthentication());
                return mutatedExchange != null ? mutatedExchange : exchange;
            }).switchIfEmpty(Mono.fromSupplier(() -> {
                return this.addClaimFromAuthorizationHeaderToken(exchange, config);
            }));
            Objects.requireNonNull(chain);
            return serverWebExchangeMono.flatMap(chain::filter);
        };
    }

    private ServerWebExchange addClaimFromSessionToken(JWTClaimHeaderGatewayFilterFactory.Config config, ServerWebExchange exchange, Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken) {
            this.log.debug("Found JwtAuthenticationToken authentication object");
            return this.copyClaimToHeader(exchange, config, () -> {
                return ((JwtAuthenticationToken)authentication).getTokenAttributes().get(config.claim);
            });
        } else if (authentication instanceof OAuth2AuthenticationToken) {
            this.log.debug("Found OAuth2AuthenticationToken authentication object");
            return this.copyClaimToHeader(exchange, config, () -> {
                return ((OAuth2AuthenticationToken)authentication).getPrincipal().getAttributes().get(config.claim);
            });
        } else {
            return null;
        }
    }

    private ServerWebExchange copyClaimToHeader(ServerWebExchange exchange, JWTClaimHeaderGatewayFilterFactory.Config config, Supplier<Object> valueSupplier) {
        return exchange.mutate().request((request) -> {
            request.headers((headers) -> {
                Object value = valueSupplier.get();
                if (value != null) {
                    List<String> previousValues = headers.get(config.headerName);
                    this.log.debug("Found value for claim '{}', including into request headers", config.claim);
                    headers.put(config.headerName, this.buildHeaderValue(this.addValueToList(previousValues, value)));
                }

            });
        }).build();
    }

    private ServerWebExchange addClaimFromAuthorizationHeaderToken(ServerWebExchange exchange, JWTClaimHeaderGatewayFilterFactory.Config config) {
        List<String> authHeaders = exchange.getRequest().getHeaders().getOrDefault("Authorization", Collections.emptyList());
        Optional<String> authorizationHeader = authHeaders.stream().findFirst();
        return authorizationHeader.isPresent() ? exchange.mutate().request((request) -> {
            request.headers((headers) -> {
                String cleanupHeaderValue = JwtHelper.cleanupHeaderValue(authorizationHeader.get());
                Object value = JwtHelper.getClaimValue(cleanupHeaderValue, config.claim);
                if (value != null) {
                    List<String> previousValues = headers.get(config.headerName);
                    this.log.debug("Found value for claim '{}', including into request headers", config.claim);
                    headers.put(config.headerName, this.buildHeaderValue(this.addValueToList(previousValues, value)));
                }

            });
        }).build() : exchange;
    }

    private Collection<Object> addValueToList(List<String> previousValues, Object value) {
        if (previousValues == null) {
            if (value instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Object> valueCollection = (Collection<Object>) value;
                return new ArrayList<>(valueCollection);
            } else {
                return List.of(value);
            }
        } else {
            ArrayList<Object> mergedValues = new ArrayList<>();
            if (value instanceof Collection) {
                mergedValues.addAll(previousValues);
                @SuppressWarnings("unchecked")
                Collection<Object> valueCollection = (Collection<Object>) value;
                mergedValues.addAll(valueCollection);
            } else {
                mergedValues.add(value);
            }

            return mergedValues;
        }
    }

    private List<String> buildHeaderValue(Object value) {
        if (value instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> valueCollection = (Collection<Object>) value;
            return valueCollection.stream()
                    .map(this::mapSimpleValue)
                    .collect(Collectors.toList());
        } else {
            return List.of(this.mapSimpleValue(value));
        }
    }

    private String mapSimpleValue(Object value) {
        if (value instanceof String) {
            return (String)value;
        } else {
            return value instanceof Instant ? String.valueOf(((Instant)value).getEpochSecond()) : value.toString();
        }
    }

    public String name() {
        return "ClaimHeader";
    }

    public JWTClaimHeaderGatewayFilterFactory.Config newConfig() {
        return new JWTClaimHeaderGatewayFilterFactory.Config();
    }

    public Class<JWTClaimHeaderGatewayFilterFactory.Config> getConfigClass() {
        return JWTClaimHeaderGatewayFilterFactory.Config.class;
    }

    public List<String> shortcutFieldOrder() {
        return List.of("claim", "headerName");
    }


    @Validated
    static class Config {
        @NotBlank
        private String claim;
        @NotBlank
        private String headerName;

        Config() {
        }

        public String getClaim() {
            return this.claim;
        }

        public void setClaim(String claim) {
            this.claim = claim;
        }

        public String getHeaderName() {
            return this.headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

}
