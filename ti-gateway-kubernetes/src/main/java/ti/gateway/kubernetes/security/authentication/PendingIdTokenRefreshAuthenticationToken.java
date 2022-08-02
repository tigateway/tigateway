package ti.gateway.kubernetes.security.authentication;

import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public class PendingIdTokenRefreshAuthenticationToken extends AbstractAuthenticationToken {
    private final OAuth2AuthorizedClient client;
    private final OAuth2AuthenticationToken originalAuthenticationToken;

    public PendingIdTokenRefreshAuthenticationToken(OAuth2AuthorizedClient client, OAuth2AuthenticationToken originalAuthenticationToken) {
        super(Collections.emptySet());
        this.client = client;
        this.originalAuthenticationToken = originalAuthenticationToken;
    }

    public Object getCredentials() {
        return "";
    }

    public Object getPrincipal() {
        return "";
    }

    public OAuth2AuthorizedClient getClient() {
        return this.client;
    }

    public OAuth2AuthenticationToken getOriginalAuthenticationToken() {
        return this.originalAuthenticationToken;
    }
}
