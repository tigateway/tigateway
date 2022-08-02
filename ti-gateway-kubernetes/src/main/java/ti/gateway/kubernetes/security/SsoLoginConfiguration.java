package ti.gateway.kubernetes.security;

public class SsoLoginConfiguration {
    private String clientRegistrationId;

    public SsoLoginConfiguration() {
    }

    public String getClientRegistrationId() {
        return this.clientRegistrationId;
    }

    public void setClientRegistrationId(String clientRegistrationId) {
        this.clientRegistrationId = clientRegistrationId;
    }
}
