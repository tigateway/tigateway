package io.pivotal.spring.cloud.gateway.security.authentication;

import java.time.Clock;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class RefreshIdTokenAuthenticationConverter implements ServerAuthenticationConverter {
    private static final Logger LOG = LoggerFactory.getLogger(RefreshIdTokenAuthenticationConverter.class);
    private final Duration clockSkew = Duration.ofSeconds(60L);
    private final Clock clock = Clock.systemUTC();
    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;

    public RefreshIdTokenAuthenticationConverter(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.authorizedClientRepository = authorizedClientRepository;
    }

    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext().flatMap((securityContext) -> {
            Authentication authentication = securityContext.getAuthentication();
            if (!(authentication instanceof OAuth2AuthenticationToken)) {
                LOG.trace("Current authentication is not an OAuth2AuthenticationToken, no need to refresh");
                return Mono.empty();
            } else {
                OAuth2AuthenticationToken oauth2AuthenticationToken = (OAuth2AuthenticationToken)authentication;
                OAuth2User principal = oauth2AuthenticationToken.getPrincipal();
                if (!(principal instanceof OidcUser)) {
                    LOG.trace("Current principal is not an OidcUser, no need to refresh");
                    return Mono.empty();
                } else {
                    OidcUser oidcUser = (OidcUser)principal;
                    if (!this.hasTokenExpired(oidcUser.getIdToken())) {
                        LOG.debug("Current ID token is not expired, no need to refresh");
                        return Mono.empty();
                    } else {
                        return this.authorizedClientRepository.loadAuthorizedClient(oauth2AuthenticationToken.getAuthorizedClientRegistrationId(), oauth2AuthenticationToken, exchange).map((client) -> {
                            return new PendingIdTokenRefreshAuthenticationToken(client, oauth2AuthenticationToken);
                        });
                    }
                }
            }
        });
    }

    private boolean hasTokenExpired(AbstractOAuth2Token token) {
        return this.clock.instant().isAfter(token.getExpiresAt().minus(this.clockSkew));
    }
}

