package io.pivotal.spring.cloud.gateway.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class JWTClaimRoutePredicateFactory extends AbstractRoutePredicateFactory<JWTClaimRoutePredicateFactory.Config> {
    private static final Logger LOG = LoggerFactory.getLogger(JWTClaimRoutePredicateFactory.class);
    public static final String HEADER_KEY = "header";
    public static final String CLAIM_KEY = "claim";
    public static final String REGEXP_KEY = "regexp";
    private static final int MAX_HEADER_VALUES_TO_CHECK = 3;

    public JWTClaimRoutePredicateFactory() {
        super(JWTClaimRoutePredicateFactory.Config.class);
    }

    public List<String> shortcutFieldOrder() {
        return Arrays.asList("header", "claim", "regexp");
    }

    public Predicate<ServerWebExchange> apply(JWTClaimRoutePredicateFactory.Config config) {
        return new GatewayPredicate() {
            private final String configStr;

            {
                this.configStr = String.format("Header = %s, claim = %s, regexp = %s", config.header, config.claim, config.regexp);
            }

            public boolean test(ServerWebExchange exchange) {
                List<String> values = (List)((List)exchange.getRequest().getHeaders().getOrDefault(config.header, Collections.emptyList())).stream().limit(3L).collect(Collectors.toList());
                if (values.isEmpty()) {
                    JWTClaimRoutePredicateFactory.LOG.debug("Header: {} is empty", config.header);
                    return false;
                } else {
                    Iterator<String> iterator = values.iterator();

                    Object claimValue;
                    do {
                        if (!iterator.hasNext()) {
                            return false;
                        }

                        String headerValue = (String) iterator.next();
                        headerValue = JwtHelper.cleanupHeaderValue(headerValue);
                        claimValue = JwtHelper.getClaimValue(headerValue, config.claim);
                    } while(claimValue == null);

                    boolean match = claimValue.toString().matches(config.regexp);
                    JWTClaimRoutePredicateFactory.LOG.debug("JWT Token from request {} matched: {} on criteria: {}", new Object[]{exchange.getRequest().getPath(), match, this.configStr});
                    return match;
                }
            }

            public String toString() {
                return this.configStr;
            }
        };
    }

    @Validated
    public static class Config {
        @NotEmpty
        private String header;
        @NotEmpty
        private String claim;
        @NotEmpty
        private String regexp;

        public Config() {
        }

        public String getHeader() {
            return this.header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getClaim() {
            return this.claim;
        }

        public JWTClaimRoutePredicateFactory.Config setClaim(String claim) {
            this.claim = claim;
            return this;
        }

        public String getRegexp() {
            return this.regexp;
        }

        public JWTClaimRoutePredicateFactory.Config setRegexp(String regexp) {
            this.regexp = regexp;
            return this;
        }
    }

}
