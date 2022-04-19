package com.vmware.tanzu.springcloudgateway.gateway;

import com.vmware.tanzu.springcloudgateway.helper.GroupIdGetter;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpec;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpecExtensions;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpecExtensionsFilters;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpecExtensionsFiltersApiKey;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpecExtensionsFiltersJwtKey;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpecExtensionsSecretsProviders;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpecExtensionsVault;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1StatefulSetSpec;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
class VaultStatefulSetContributor extends AbstractStatefulSetContributor {
    private static final Logger LOGGER = LoggerFactory.getLogger(VaultStatefulSetContributor.class);
    private static final String DEFAULT_VAULT_JWTKEY_FILENAME = "jwt_keys";
    private static final String DEFAULT_VAULT_K8S_AUTH_PATH = "auth/kubernetes";
    private static final String DEFAULT_VAULT_KEYS_FILENAME = "keys";
    private static final String DEFAULT_VAULT_KEYS_PATH = "/vault/secrets/keys";
    private static final String VAULT_JWTKEY_DEFAULT_PREFIX = "jwt-keys-for-vmware-tanzu";
    private static final String VAULT_KEYS_DEFAULT_PREFIX = "api-portal-for-vmware-tanzu";

    VaultStatefulSetContributor() {
    }

    boolean appliesTo(V1SpringCloudGateway gateway) {
        return this.isVaultEnabledAndConfigured(gateway);
    }

    void apply(V1StatefulSet statefulSet, V1SpringCloudGateway gateway) {
        Map<String, String> annotations = this.buildVaultAnnotations(gateway);
        HashSet<V1EnvVar> envVars = new HashSet<>();
        if (this.isApiKeyEnabledAndConfigured(gateway)) {
            annotations.putAll(this.buildApiKeyAnnotations(gateway));
            envVars.addAll(this.buildApiKeyEnvVars());
        }

        if (this.isJwtKeyEnabledAndConfigured(gateway)) {
            annotations.putAll(this.buildJwtKeyAnnotations(gateway));
            envVars.addAll(this.buildJwtKeyEnvVars());
        }

        annotations.putAll(this.buildCustomSecretProviderAnnotations(gateway));
        envVars.addAll(this.buildExtensionsVaultSupport(gateway));
        this.addAnnotationsToSpecTemplateMetadata(statefulSet, annotations);
        addGatewayEnvironmentVariables(statefulSet, envVars);
    }

    private void addAnnotationsToSpecTemplateMetadata(V1StatefulSet statefulSet, Map<String, String> annotations) {
        if (!annotations.isEmpty()) {
            V1StatefulSetSpec spec = statefulSet.getSpec();
            if (spec != null) {
                V1PodTemplateSpec template = spec.getTemplate();
                if (template != null) {
                    V1ObjectMeta metadata = template.getMetadata();
                    if (metadata != null) {
                        Map<String, String> metadataAnnotations = metadata.getAnnotations();
                        if (metadataAnnotations != null) {
                            metadataAnnotations.putAll(annotations);
                        } else {
                            metadata.setAnnotations(annotations);
                        }
                    }
                }
            }

        }
    }

    private boolean isApiKeyEnabledAndConfigured(V1SpringCloudGateway gateway) {
        V1SpringCloudGatewaySpecExtensionsFiltersApiKey apiKey = (V1SpringCloudGatewaySpecExtensionsFiltersApiKey)Optional.ofNullable(gateway.getSpec()).map(V1SpringCloudGatewaySpec::getExtensions).map(V1SpringCloudGatewaySpecExtensions::getFilters).map(V1SpringCloudGatewaySpecExtensionsFilters::getApiKey).orElse(null);
        if (apiKey == null) {
            LOGGER.debug("No API key configuration for {}", ((V1ObjectMeta)Objects.requireNonNull(gateway.getMetadata())).getName());
            return false;
        } else if (apiKey.getEnabled() != null && apiKey.getEnabled() && StringUtils.hasText(apiKey.getSecretsProviderName())) {
            if (!apiKey.getSecretsProviderName().equals(this.findFirstValidVaultConfig(gateway).getName())) {
                LOGGER.warn("API Key management invalid secret provider configuration: secretsProviderName does not match");
                return false;
            } else {
                return true;
            }
        } else {
            LOGGER.warn("API Key management is disabled, skipping configuration");
            return false;
        }
    }

    private boolean isJwtKeyEnabledAndConfigured(V1SpringCloudGateway gateway) {
        V1SpringCloudGatewaySpecExtensionsFiltersJwtKey jwtKey = (V1SpringCloudGatewaySpecExtensionsFiltersJwtKey)Optional.ofNullable(gateway.getSpec()).map(V1SpringCloudGatewaySpec::getExtensions).map(V1SpringCloudGatewaySpecExtensions::getFilters).map(V1SpringCloudGatewaySpecExtensionsFilters::getJwtKey).orElse(null);
        if (jwtKey == null) {
            LOGGER.debug("No JWT key configuration for {}", ((V1ObjectMeta)Objects.requireNonNull(gateway.getMetadata())).getName());
            return false;
        } else if (!Boolean.FALSE.equals(jwtKey.getEnabled()) && StringUtils.hasText(jwtKey.getSecretsProviderName())) {
            if (!jwtKey.getSecretsProviderName().equals(this.findFirstValidVaultConfig(gateway).getName())) {
                LOGGER.warn("JWT Key feature invalid secret provider configuration: secretsProviderName does not match");
                return false;
            } else {
                return true;
            }
        } else {
            LOGGER.warn("JWT Key feature is disabled, skipping configuration");
            return false;
        }
    }

    private boolean isVaultEnabledAndConfigured(V1SpringCloudGateway gateway) {
        if (gateway.getSpec() != null && gateway.getSpec().getExtensions() != null && gateway.getSpec().getExtensions().getSecretsProviders() != null && !gateway.getSpec().getExtensions().getSecretsProviders().isEmpty()) {
            if (gateway.getSpec().getExtensions().getSecretsProviders().stream().filter(this::validVaultElementWithRole).map((vault) -> {
                return ((V1SpringCloudGatewaySpecExtensionsVault)Objects.requireNonNull(vault.getVault())).getRoleName();
            }).distinct().count() > 1L) {
                LOGGER.warn("Vault integration incorrectly configured, all providers require the same role name");
                return false;
            } else if (gateway.getSpec().getExtensions().getSecretsProviders().stream().filter(this::validVaultElementWithAuthPath).map((vault) -> {
                return ((V1SpringCloudGatewaySpecExtensionsVault)Objects.requireNonNull(vault.getVault())).getAuthPath();
            }).distinct().count() > 1L) {
                LOGGER.warn("Vault integration incorrectly configured, all providers require the same auth path");
                return false;
            } else if (gateway.getSpec().getExtensions().getSecretsProviders().stream().noneMatch(this::validVaultElementWithRole)) {
                LOGGER.warn("Vault integration not configured, skipping configuration");
                return false;
            } else {
                return true;
            }
        } else {
            LOGGER.debug("No Vault configuration for {}", ((V1ObjectMeta)Objects.requireNonNull(gateway.getMetadata())).getName());
            return false;
        }
    }

    private Set<V1EnvVar> buildApiKeyEnvVars() {
        return Set.of((new V1EnvVar()).name("apiKey.enabled").value("true"), (new V1EnvVar()).name("apiKey.keysFilePath").value("/vault/secrets/keys"));
    }

    private Set<V1EnvVar> buildJwtKeyEnvVars() {
        return Set.of((new V1EnvVar()).name("jwtKey.enabled").value("true"), (new V1EnvVar()).name("jwtKey.keysFilePath").value("/vault/secrets/jwt_keys"));
    }

    private Set<V1EnvVar> buildExtensionsVaultSupport(V1SpringCloudGateway gateway) {
        String paths = (String)this.customSecretProviders(gateway).stream().map((sp) -> {
            return "/vault/secrets/" + sp.getName();
        }).collect(Collectors.joining(","));
        return StringUtils.hasText(paths) ? Set.of((new V1EnvVar()).name("extensions.vault.enabled").value("true"), (new V1EnvVar()).name("extensions.vault.paths").value(paths)) : Collections.emptySet();
    }

    private Map<String, String> buildVaultAnnotations(V1SpringCloudGateway gateway) {
        Map<String, String> annotations = new HashMap();
        V1SpringCloudGatewaySpecExtensionsVault vault = this.findFirstValidVaultConfig(gateway).getVault();
        annotations.put("vault.hashicorp.com/agent-init-first", "true");
        annotations.put("vault.hashicorp.com/agent-inject", "true");
        annotations.put("vault.hashicorp.com/role", ((V1SpringCloudGatewaySpecExtensionsVault)Objects.requireNonNull(vault)).getRoleName());
        String authPath = (String)this.findFirstVaultAuthPathConfig(gateway).map((sp) -> {
            return ((V1SpringCloudGatewaySpecExtensionsVault)Objects.requireNonNull(sp.getVault())).getAuthPath();
        }).orElse("auth/kubernetes");
        annotations.put("vault.hashicorp.com/auth-path", authPath);
        return annotations;
    }

    private Map<String, String> buildApiKeyAnnotations(V1SpringCloudGateway gateway) {
        Map<String, String> annotations = new HashMap();
        String groupId = GroupIdGetter.getGroupId(gateway);
        String secretsPath = "api-portal-for-vmware-tanzu/" + groupId;
        annotations.put("vault.hashicorp.com/agent-inject-secret-keys", secretsPath);
        annotations.put("vault.hashicorp.com/agent-inject-template-keys", "{{ range secrets \"api-portal-for-vmware-tanzu/metadata/" + groupId + "\" }}\n{{.}},{{ with secret (printf \"" + secretsPath + "/%s\" .) }}{{ .Data.data.key }}\n{{ end }}{{ end }}");
        return annotations;
    }

    private Map<String, String> buildJwtKeyAnnotations(V1SpringCloudGateway gateway) {
        Map<String, String> annotations = new HashMap();
        Optional var10000 = this.findJwtKeySecretProviderWithPath(gateway).map(V1SpringCloudGatewaySpecExtensionsVault::getPath);
        String var10001 = this.getGatewayId(gateway);
        String jwtKeySecretsPath = (String)var10000.orElse("jwt-keys-for-vmware-tanzu/" + var10001);
        annotations.put("vault.hashicorp.com/agent-inject-secret-jwt_keys", jwtKeySecretsPath);
        annotations.put("vault.hashicorp.com/agent-inject-template-jwt_keys", "{{ range secrets \"" + jwtKeySecretsPath + "\" }}{{ with secret (printf \"" + jwtKeySecretsPath + "/%s\" .) }}{{ .Data.data.kid }},{{ .Data.data.alg }},{{ .Data.data.key }}\n{{ end }}{{ end }}");
        return annotations;
    }

    private Map<String, String> buildCustomSecretProviderAnnotations(V1SpringCloudGateway gateway) {
        Map<String, String> annotations = new HashMap();
        this.customSecretProviders(gateway).forEach((sp) -> {
            String secretPath = ((V1SpringCloudGatewaySpecExtensionsVault)Objects.requireNonNull(sp.getVault())).getPath();
            annotations.put("vault.hashicorp.com/agent-inject-secret-" + sp.getName(), secretPath);
            annotations.put("vault.hashicorp.com/agent-inject-template-" + sp.getName(), "{{ range secrets \"" + secretPath + "\" }}{{ . }}{{ with secret (printf \"" + secretPath + "/%s\" .) }}{{ .Data.data | toJSON }}\n{{ end }}{{ end }}");
        });
        return annotations;
    }

    private String getGatewayId(V1SpringCloudGateway gateway) {
        V1ObjectMeta metadata = (V1ObjectMeta)Objects.requireNonNull(gateway.getMetadata());
        String var10000 = metadata.getNamespace();
        return var10000 + "-" + metadata.getName();
    }

    private List<V1SpringCloudGatewaySpecExtensionsSecretsProviders> customSecretProviders(V1SpringCloudGateway gateway) {
        return (List<V1SpringCloudGatewaySpecExtensionsSecretsProviders>)Optional.ofNullable(gateway.getSpec()).map(V1SpringCloudGatewaySpec::getExtensions).map(V1SpringCloudGatewaySpecExtensions::getSecretsProviders).stream().flatMap(Collection::stream).filter((sp) -> {
            V1SpringCloudGatewaySpec spec = gateway.getSpec();
            V1SpringCloudGatewaySpecExtensions ext = (V1SpringCloudGatewaySpecExtensions)Objects.requireNonNull(spec.getExtensions());
            return !this.usedByGatewayFilters(sp, ext.getFilters());
        }).filter(this::hasVaultPath).collect(Collectors.toList());
    }

    private boolean usedByGatewayFilters(V1SpringCloudGatewaySpecExtensionsSecretsProviders secretProvider, V1SpringCloudGatewaySpecExtensionsFilters filters) {
        String secretProviderName = secretProvider.getName();
        return this.usedByApiKeyFilter(filters, secretProviderName) || this.usedByJwtKeyFilter(secretProviderName, filters);
    }

    private boolean usedByApiKeyFilter(V1SpringCloudGatewaySpecExtensionsFilters filters, String secretProviderName) {
        return filters != null && filters.getApiKey() != null && secretProviderName.equals(filters.getApiKey().getSecretsProviderName());
    }

    private boolean usedByJwtKeyFilter(String secretProviderName, V1SpringCloudGatewaySpecExtensionsFilters filters) {
        return filters != null && filters.getJwtKey() != null && secretProviderName.equals(filters.getJwtKey().getSecretsProviderName());
    }

    private Optional<V1SpringCloudGatewaySpecExtensionsSecretsProviders> findFirstVaultAuthPathConfig(V1SpringCloudGateway gateway) {
        V1SpringCloudGatewaySpecExtensions extensions = ((V1SpringCloudGatewaySpec)Objects.requireNonNull(gateway.getSpec())).getExtensions();
        List<V1SpringCloudGatewaySpecExtensionsSecretsProviders> secretsProviders = ((V1SpringCloudGatewaySpecExtensions)Objects.requireNonNull(extensions)).getSecretsProviders();
        return ((List<V1SpringCloudGatewaySpecExtensionsSecretsProviders>)Objects.requireNonNull(secretsProviders)).stream().filter(this::validVaultElementWithAuthPath).findFirst();
    }

    private Optional<V1SpringCloudGatewaySpecExtensionsVault> findJwtKeySecretProviderWithPath(V1SpringCloudGateway gateway) {
        return Optional.ofNullable(gateway.getSpec()).map(V1SpringCloudGatewaySpec::getExtensions).map(V1SpringCloudGatewaySpecExtensions::getSecretsProviders).stream().flatMap(Collection::stream).filter((sp) -> {
            V1SpringCloudGatewaySpecExtensions extensions = gateway.getSpec().getExtensions();
            return this.usedByJwtKeyFilter(sp.getName(), ((V1SpringCloudGatewaySpecExtensions)Objects.requireNonNull(extensions)).getFilters());
        }).filter(this::hasVaultPath).map(V1SpringCloudGatewaySpecExtensionsSecretsProviders::getVault).findFirst();
    }

    private V1SpringCloudGatewaySpecExtensionsSecretsProviders findFirstValidVaultConfig(V1SpringCloudGateway gateway) {
        V1SpringCloudGatewaySpec spec = (V1SpringCloudGatewaySpec)Objects.requireNonNull(gateway.getSpec());
        V1SpringCloudGatewaySpecExtensions extensions = (V1SpringCloudGatewaySpecExtensions)Objects.requireNonNull(spec.getExtensions());
        return (V1SpringCloudGatewaySpecExtensionsSecretsProviders)((List<V1SpringCloudGatewaySpecExtensionsSecretsProviders>)Objects.requireNonNull(extensions.getSecretsProviders())).stream().filter(this::validVaultElementWithRole).findFirst().orElseThrow();
    }

    private boolean hasVaultPath(V1SpringCloudGatewaySpecExtensionsSecretsProviders secretsProvider) {
        return secretsProvider != null && secretsProvider.getVault() != null && StringUtils.hasText(secretsProvider.getVault().getPath());
    }

    private boolean validVaultElementWithRole(V1SpringCloudGatewaySpecExtensionsSecretsProviders secretsProvider) {
        return StringUtils.hasText(secretsProvider.getName()) && secretsProvider.getVault() != null && StringUtils.hasText(secretsProvider.getVault().getRoleName());
    }

    private boolean validVaultElementWithAuthPath(V1SpringCloudGatewaySpecExtensionsSecretsProviders secretsProvider) {
        return StringUtils.hasText(secretsProvider.getName()) && secretsProvider.getVault() != null && StringUtils.hasText(secretsProvider.getVault().getAuthPath());
    }
}

