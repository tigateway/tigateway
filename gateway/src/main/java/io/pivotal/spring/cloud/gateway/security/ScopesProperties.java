package io.pivotal.spring.cloud.gateway.security;

public class ScopesProperties {
    private String[] scopes;

    public ScopesProperties() {
    }

    public String[] getScopes() {
        return this.scopes;
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }
}
