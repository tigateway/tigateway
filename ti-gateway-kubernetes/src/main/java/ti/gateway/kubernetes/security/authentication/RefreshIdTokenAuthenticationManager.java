package ti.gateway.kubernetes.security.authentication;

import ti.gateway.kubernetes.security.SsoEnabled;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.oidc.authentication.ReactiveOidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoderFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@SsoEnabled
public class RefreshIdTokenAuthenticationManager implements ReactiveAuthenticationManager {
    private static final Logger LOG = LoggerFactory.getLogger(RefreshIdTokenAuthenticationManager.class);
    private final ReactiveOAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> accessTokenResponseClient = new WebClientReactiveRefreshTokenTokenResponseClient();
    private final ReactiveJwtDecoderFactory<ClientRegistration> jwtDecoderFactory = new ReactiveOidcIdTokenDecoderFactory();
    private final ReactiveOAuth2UserService<OidcUserRequest, OidcUser> userService;

    public RefreshIdTokenAuthenticationManager(ReactiveOAuth2UserService<OidcUserRequest, OidcUser> userService) {
        this.userService = userService;
    }

    public Mono<Authentication> authenticate(Authentication authentication) {
        PendingIdTokenRefreshAuthenticationToken pendingIdTokenRefreshAuthenticationToken = (PendingIdTokenRefreshAuthenticationToken)authentication;
        return this.refreshIdTokenAuthentication(pendingIdTokenRefreshAuthenticationToken);
    }

    private Mono<Authentication> refreshIdTokenAuthentication(PendingIdTokenRefreshAuthenticationToken authenticationToken) {
        OAuth2AuthorizedClient authorizedClient = authenticationToken.getClient();
        if (authorizedClient.getRefreshToken() == null) {
            LOG.error("IdToken has expired, but no refresh token is provided by authorization server, can't refresh IdToken");
            return Mono.just(authenticationToken.getOriginalAuthenticationToken());
        } else {
            ClientRegistration clientRegistration = authorizedClient.getClientRegistration();
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest = new OAuth2RefreshTokenGrantRequest(clientRegistration, accessToken, authorizedClient.getRefreshToken());
            return this.accessTokenResponseClient.getTokenResponse(refreshTokenGrantRequest).flatMap((accessTokenResponse) -> {
                Mono<OidcUserRequest> oidcUserRequestMono = this.createOidcToken(clientRegistration, accessTokenResponse).map((idToken) -> {
                    return new OidcUserRequest(clientRegistration, accessToken, idToken);
                });
                ReactiveOAuth2UserService<OidcUserRequest, OidcUser> reactiveOAuth2UserService = this.userService;
                Objects.requireNonNull(reactiveOAuth2UserService);
                return oidcUserRequestMono
                        .flatMap(reactiveOAuth2UserService::loadUser)
                        .map((oauth2User) -> {
                            return new OAuth2AuthenticationToken(oauth2User, oauth2User.getAuthorities(), clientRegistration.getRegistrationId());
                        });
            });
        }
    }

    private Mono<OidcIdToken> createOidcToken(ClientRegistration clientRegistration, OAuth2AccessTokenResponse accessTokenResponse) {
        ReactiveJwtDecoder jwtDecoder = this.jwtDecoderFactory.createDecoder(clientRegistration);
        String rawIdToken = (String)accessTokenResponse.getAdditionalParameters().get("id_token");
        return jwtDecoder.decode(rawIdToken).map((jwt) -> {
            return new OidcIdToken(jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getClaims());
        });
    }
}

