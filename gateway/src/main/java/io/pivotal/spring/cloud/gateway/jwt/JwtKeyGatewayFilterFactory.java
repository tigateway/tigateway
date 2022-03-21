package io.pivotal.spring.cloud.gateway.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import io.pivotal.spring.cloud.gateway.security.CommonSecurity;
import io.pivotal.spring.cloud.gateway.security.RolesExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.HttpMessage;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@JwtKeyEnabled
public class JwtKeyGatewayFilterFactory implements GatewayFilterFactory<JwtKeyGatewayFilterFactory.Config> {
    private static final Logger log = LoggerFactory.getLogger(JwtKeyGatewayFilterFactory.class);
    private static final String DEFAULT_WWW_HEADER_MESSAGE = "Token could not be validated";
    private final KeyParser keyParser;
    private final TokenVerifier tokenVerifier;
    private final LocalFileJwtKeysLocator localFileJwtKeysLocator;
    private final RolesExtractor rolesExtractor;

    public JwtKeyGatewayFilterFactory(LocalFileJwtKeysLocator localFileJwtKeysLocator, KeyParser keyParser, TokenVerifier tokenVerifier, RolesExtractor rolesExtractor) {
        this.keyParser = keyParser;
        this.tokenVerifier = tokenVerifier;
        this.localFileJwtKeysLocator = localFileJwtKeysLocator;
        this.rolesExtractor = rolesExtractor;
    }

    public GatewayFilter apply(JwtKeyGatewayFilterFactory.Config config) {
        SecurityWebFilterChain customChain = CommonSecurity.configureCommonSecurity(ServerHttpSecurity.http()).oauth2ResourceServer().authenticationManagerResolver((exchange) -> {
            return this.resolveAuthentication(exchange, config);
        }).and().authorizeExchange().anyExchange().authenticated().and().build();
        return (exchange, chain) -> {
            WebFilterChainProxy var10000 = new WebFilterChainProxy(new SecurityWebFilterChain[]{customChain});
            Objects.requireNonNull(chain);
            return var10000.filter(exchange, chain::filter);
        };
    }

    public Mono<ReactiveAuthenticationManager> resolveAuthentication(ServerWebExchange exchange, JwtKeyGatewayFilterFactory.Config config) {
        return Mono.just((authentication) -> {
            String plainToken = ((BearerTokenAuthenticationToken)authentication).getToken();

            JwtKeyGatewayFilterFactory.TokenParser token;
            try {
                token = JwtKeyGatewayFilterFactory.TokenParser.parseToken(plainToken);
            } catch (ParseException var7) {
                return this.errorResponse();
            }

            if (!this.isValidToken(token, config, exchange)) {
                return this.errorResponse();
            } else {
                DefaultOAuth2AuthenticatedPrincipal principal = this.buildPrincipal(plainToken, config);
                return Mono.just(new BearerTokenAuthentication(principal, this.buildCredentials(token), principal.getAuthorities()));
            }
        });
    }

    private Mono<Authentication> errorResponse() {
        return Mono.error(new InvalidBearerTokenException("Token could not be validated"));
    }

    private DefaultOAuth2AuthenticatedPrincipal buildPrincipal(String token, JwtKeyGatewayFilterFactory.Config config) {
        Collection<GrantedAuthority> roles = (Collection) this.rolesExtractor.rolesFromClaim(token).stream().map((role) -> {
            return new SimpleGrantedAuthority("ROLE_" + role);
        }).collect(Collectors.toSet());
        return new DefaultOAuth2AuthenticatedPrincipal(Map.of("keyLocation", config.getKeyLocation()), roles);
    }

    private OAuth2AccessToken buildCredentials(JwtKeyGatewayFilterFactory.TokenParser token) {
        Instant iat = token.extractTimeClaim("iat");
        Instant exp = token.extractTimeClaim("exp");
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, token.getPlainToken(), iat, exp);
    }

    private boolean isValidToken(JwtKeyGatewayFilterFactory.TokenParser token, JwtKeyGatewayFilterFactory.Config config, ServerWebExchange exchange) {
        if (config == null) {
            return false;
        } else {
            String keyLocation = this.getKeyLocation(exchange, token, config);
            if (!StringUtils.hasText(keyLocation)) {
                return false;
            } else {
                LocalFileJwtKeysLocator.Key key = this.localFileJwtKeysLocator.getKey(keyLocation);
                if (key == null) {
                    return false;
                } else {
                    try {
                        Object parsedKey = this.keyParser.parse(key.getAlg(), key.getKey());
                        return parsedKey == null ? false : this.tokenHasValidSignature(token.getPlainToken(), parsedKey);
                    } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
                        log.info(exception.getMessage());
                        return false;
                    }
                }
            }
        }
    }

    private boolean tokenHasValidSignature(String token, Object key) {
        return this.tokenVerifier.verify(token, key);
    }

    private String getKeyLocation(ServerWebExchange exchange, JwtKeyGatewayFilterFactory.TokenParser token, JwtKeyGatewayFilterFactory.Config config) {
        if (config.getKeyLocation().contains("{")) {
            String[] parts = config.getKeyLocation().substring(1, config.getKeyLocation().indexOf("}")).split(":");
            String sourceType = parts[0];
            String source = parts[1];
            if ("header".equals(sourceType)) {
                return (String) Optional.of(exchange).map(ServerWebExchange::getRequest).map(HttpMessage::getHeaders).map((headers) -> {
                    return headers.get(source);
                }).filter((headers) -> {
                    return !headers.isEmpty();
                }).map((headers) -> {
                    return (String)headers.get(0);
                }).orElse("");
            }

            if ("claim".equals(sourceType)) {
                String claimValue = JwtHelper.getClaimAsString(token.getPlainToken(), source);
                return claimValue == null ? "" : claimValue;
            }
        }

        return "";
    }

    public JwtKeyGatewayFilterFactory.Config newConfig() {
        return new JwtKeyGatewayFilterFactory.Config();
    }

    public Class<JwtKeyGatewayFilterFactory.Config> getConfigClass() {
        return JwtKeyGatewayFilterFactory.Config.class;
    }

    public List<String> shortcutFieldOrder() {
        return List.of("keyLocation");
    }


    @Validated
    static class Config {
        @NotBlank
        private String keyLocation;

        Config() {
        }

        public void setKeyLocation(String keyLocation) {
            this.keyLocation = keyLocation;
        }

        public String getKeyLocation() {
            return this.keyLocation;
        }
    }

    private static class TokenParser {
        private final String plainToken;
        private final JWTClaimsSet claimSet;

        TokenParser(String token) throws ParseException {
            this.plainToken = token;
            this.claimSet = JWTParser.parse(token).getJWTClaimsSet();
        }

        public String getPlainToken() {
            return this.plainToken;
        }

        private static JwtKeyGatewayFilterFactory.TokenParser parseToken(String token) throws ParseException {
            return new JwtKeyGatewayFilterFactory.TokenParser(token);
        }

        private Instant extractTimeClaim(String claim) {
            try {
                Long epochSecond = this.claimSet.getLongClaim(claim);
                return epochSecond != null ? Instant.ofEpochSecond(epochSecond) : null;
            } catch (ParseException var3) {
                JwtKeyGatewayFilterFactory.log.warn("Could not parse claim {}: {}", claim, var3.getMessage());
                return null;
            }
        }
    }

}
