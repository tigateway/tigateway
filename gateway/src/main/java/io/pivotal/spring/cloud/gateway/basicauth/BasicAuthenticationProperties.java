package io.pivotal.spring.cloud.gateway.basicauth;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
public class BasicAuthenticationProperties {

    @NotBlank
    private String encodedCredentials;

    public BasicAuthenticationProperties() {
    }

    public String getEncodedCredentials() {
        return encodedCredentials;
    }

    public void setEncodedCredentials(String encodedCredentials) {
        this.encodedCredentials = encodedCredentials;
    }
}
