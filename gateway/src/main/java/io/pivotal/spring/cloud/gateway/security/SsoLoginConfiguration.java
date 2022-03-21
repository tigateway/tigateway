package io.pivotal.spring.cloud.gateway.security;

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
