package io.pivotal.spring.cloud.gateway.security;

import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
@SsoEnabled
public class RolesOidcReactiveOAuth2UserService extends OidcReactiveOAuth2UserService {
    private final RolesExtractor rolesExtractor;

    public RolesOidcReactiveOAuth2UserService(RolesExtractor rolesExtractor) {
        this.rolesExtractor = rolesExtractor;
    }

    public Mono<OidcUser> loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return super.loadUser(userRequest).map((oidcUser) -> {
            return this.updateOidcUserAuthorities(oidcUser, userRequest);
        });
    }

    private OidcUser updateOidcUserAuthorities(OidcUser oidcUser, OidcUserRequest userRequest) {
        Set<GrantedAuthority> newAuthorities = new HashSet<>(oidcUser.getAuthorities());
        this.rolesExtractor.rolesFromClaim(oidcUser).forEach((role) -> {
            newAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        });
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        return StringUtils.hasText(userNameAttributeName) ? new DefaultOidcUser(newAuthorities, userRequest.getIdToken(), oidcUser.getUserInfo(), userNameAttributeName) : new DefaultOidcUser(newAuthorities, userRequest.getIdToken(), oidcUser.getUserInfo());
    }
}

