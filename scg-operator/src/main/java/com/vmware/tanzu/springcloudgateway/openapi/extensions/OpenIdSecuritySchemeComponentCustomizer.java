package com.vmware.tanzu.springcloudgateway.openapi.extensions;

import com.vmware.tanzu.springcloudgateway.gateway.SsoParameters;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.openapi.ApiException;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import java.net.URI;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OpenIdSecuritySchemeComponentCustomizer extends AbstractSecuritySchemeComponentCustomizer {
    private final SsoParameters ssoParameters;
    private static final Logger LOG = LoggerFactory.getLogger(OpenIdSecuritySchemeComponentCustomizer.class);
    private static final String OIDC_METADATA_PATH = "/.well-known/openid-configuration";
    private static final String SECURITY_NAME = "OpenId";
    private static final String SECURITY_SCHEME = "openId";
    private static final Type SECURITY_TYPE;

    public OpenIdSecuritySchemeComponentCustomizer(SsoParameters ssoParameters) {
        super("OpenId", "openId", SECURITY_TYPE);
        this.ssoParameters = ssoParameters;
    }

    protected SecurityScheme defaultSecurityRequirement(V1SpringCloudGateway gateway) {
        String openIdConnectUrl = this.extractOpenIdConnectUrl(gateway);
        return super.defaultSecurityRequirement(gateway).openIdConnectUrl(openIdConnectUrl);
    }

    private String extractOpenIdConnectUrl(V1SpringCloudGateway gateway) {
        if (gateway != null && gateway.getSpec() != null && gateway.getSpec().getSso() != null && gateway.getMetadata() != null) {
            String secret = gateway.getSpec().getSso().getSecret();
            String namespace = gateway.getMetadata().getNamespace();

            try {
                String issuerUriValue = this.ssoParameters.readIssuerId(namespace, secret);
                URI issuerUri = URI.create(issuerUriValue);
                return UriComponentsBuilder.fromUri(issuerUri).replacePath(issuerUri.getPath() + "/.well-known/openid-configuration").build(Collections.emptyMap()).toString();
            } catch (ApiException var6) {
                LOG.error("Error reading Issuer ID from namespace " + namespace + "gateway: " + gateway.getMetadata().getName(), var6);
            }
        } else {
            LOG.error("OpenIdConnectorUrl is not defined in the gateway {}", gateway);
        }

        return "";
    }

    static {
        SECURITY_TYPE = Type.OPENIDCONNECT;
    }
}

