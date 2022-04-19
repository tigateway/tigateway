package com.vmware.tanzu.springcloudgateway.gateway;

import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarSource;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretKeySelector;
import io.kubernetes.client.openapi.models.V1SecretList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.util.StringUtils;

public class SsoParameters {
    static final String SPRING_PROFILE_INCLUDE = "spring.profiles.include";
    static final String CLIENT_ID_ENV_KEY = "sso.client-id";
    static final String CLIENT_SECRET_ENV_KEY = "sso.client-secret";
    static final String SCOPE_ENV_KEY = "sso.scope";
    static final String ISSUER_URI_ENV_KEY = "sso.issuer-uri";
    static final String CLIENT_ID_SECRET_KEY = "client-id";
    static final String CLIENT_SECRET_SECRET_KEY = "client-secret";
    static final String SCOPE_SECRET_KEY = "scope";
    static final String ISSUER_URI_SECRET_KEY = "issuer-uri";
    static final String AUTH_ROLES_ATTRIBUTE_NAME = "sso.roles-attribute-name";
    static final String SPRING_SESSION_TIMEOUT = "spring.session.timeout";
    private static final Logger LOGGER = LoggerFactory.getLogger(SsoParameters.class);
    private final CoreV1Api coreV1Api;

    public SsoParameters(CoreV1Api coreV1Api) {
        this.coreV1Api = coreV1Api;
    }

    Set<V1EnvVar> createEnvironmentConfig(String gatewayNamespace, String ssoCredentialsSecret, String rolesAttributeName, Integer inactiveSessionExpirationInMinutes) throws ApiException {
        LOGGER.debug("finding secret {} in namespace {}", ssoCredentialsSecret, gatewayNamespace);
        if (!this.secretExists(gatewayNamespace, ssoCredentialsSecret)) {
            throw new ApiException(String.format("%s secret not found", ssoCredentialsSecret));
        } else {
            this.validateIssuerUri(this.readIssuerId(gatewayNamespace, ssoCredentialsSecret));
            Set<V1EnvVar> envVars = this.getEnvVarsFromSecret(ssoCredentialsSecret);
            if (StringUtils.hasText(rolesAttributeName)) {
                envVars.add((new V1EnvVar()).name("sso.roles-attribute-name").value(rolesAttributeName));
            }

            if (inactiveSessionExpirationInMinutes != null) {
                envVars.add((new V1EnvVar()).name("spring.session.timeout").value(inactiveSessionExpirationInMinutes + "m"));
            }

            return envVars;
        }
    }

    private void validateIssuerUri(String issuerUri) throws ApiException {
        try {
            ReactiveJwtDecoders.fromOidcIssuerLocation(issuerUri);
        } catch (Exception var3) {
            throw new ApiException(String.format("Failed to validate SSO issuer %s", issuerUri), var3, 422, Map.of());
        }
    }

    private Set<V1EnvVar> getEnvVarsFromSecret(String ssoCredentialsSecret) {
        return (Set<V1EnvVar>)Stream.of(
                (new V1EnvVar()).name("spring.profiles.include").value("sso"),
                (new V1EnvVar()).name("sso.client-id").valueFrom(this.envVarSourceFromSecret("client-id", ssoCredentialsSecret)),
                (new V1EnvVar()).name("sso.client-secret").valueFrom(this.envVarSourceFromSecret("client-secret", ssoCredentialsSecret)),
                (new V1EnvVar()).name("sso.scope").valueFrom(this.envVarSourceFromSecret("scope", ssoCredentialsSecret)),
                (new V1EnvVar()).name("sso.issuer-uri").valueFrom(this.envVarSourceFromSecret("issuer-uri", ssoCredentialsSecret))).collect(Collectors.toCollection(HashSet::new)
        );
    }

    boolean secretExists(String gatewayNamespace, String ssoCredentialsSecret) throws ApiException {
        return this.getSecret(gatewayNamespace, ssoCredentialsSecret) != null;
    }

    public String readIssuerId(String gatewayNamespace, String ssoCredentialsSecret) throws ApiException {
        V1Secret secret = this.getSecret(gatewayNamespace, ssoCredentialsSecret);
        return secret != null && secret.getData() != null ? new String((byte[])secret.getData().get("issuer-uri")) : "";
    }

    private V1Secret getSecret(String gatewayNamespace, String secretName) throws ApiException {
        V1SecretList ssoCredentialsSecret = this.coreV1Api.listNamespacedSecret(gatewayNamespace, (String)null, (Boolean)null, (String)null, String.format("metadata.name=%s", secretName), (String)null, (Integer)null, (String)null, (String)null, 10, false);
        V1Secret ssoSecret = (V1Secret)ssoCredentialsSecret.getItems().stream().findFirst().orElse(null);
        if (ssoSecret == null) {
            return null;
        } else {
            Set<String> expectedSecretKeys = (Set<String>)Stream.of("client-id", "client-secret").collect(Collectors.toSet());
            if (ssoSecret.getData() != null && ssoSecret.getData().keySet().containsAll(expectedSecretKeys)) {
                return ssoSecret;
            } else {
                throw new ApiException(String.format("Could not find all required keys %s in secret %s/%s", String.join(",", expectedSecretKeys), gatewayNamespace, secretName));
            }
        }
    }

    private V1EnvVarSource envVarSourceFromSecret(String secretKey, String ssoCredentialsSecret) {
        return (new V1EnvVarSource()).secretKeyRef((new V1SecretKeySelector()).name(ssoCredentialsSecret).key(secretKey));
    }

    static boolean isEnabled(V1SpringCloudGateway gateway) {
        return gateway.getSpec() != null && gateway.getSpec().getSso() != null && gateway.getSpec().getSso().getSecret() != null;
    }
}
