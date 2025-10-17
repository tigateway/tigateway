package ti.gateway.kubernetes.basicauth;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

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
