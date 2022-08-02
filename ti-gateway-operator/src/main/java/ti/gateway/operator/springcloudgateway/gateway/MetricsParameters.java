package ti.gateway.operator.springcloudgateway.gateway;

import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGateway;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewaySpecObservabilityWavefront;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarSource;
import io.kubernetes.client.openapi.models.V1ObjectFieldSelector;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretKeySelector;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.compress.utils.Sets;

class MetricsParameters {
    private static final String SECRET_WAVEFRONT_API_TOKEN_KEY = "wavefront.api-token";
    private static final String SECRET_WAVEFRONT_URI_KEY = "wavefront.uri";
    private static final String WAVEFRONT_API_TOKEN_KEY = "management.metrics.export.wavefront.api-token";
    private static final String WAVEFRONT_URI_KEY = "management.metrics.export.wavefront.uri";
    private static final String WAVEFRONT_SOURCE_KEY = "management.metrics.export.wavefront.source";
    private static final String WAVEFRONT_APPLICATION_NAME = "wavefront.application.name";
    private static final String WAVEFRONT_APPLICATION_SERVICE = "wavefront.application.service";
    private final SecretLister secretLister;
    private static final Set<String> EXPECTED_SECRET_KEYS = (Set)Stream.of("wavefront.api-token", "wavefront.uri").collect(Collectors.toSet());

    public MetricsParameters(SecretLister secretLister) {
        this.secretLister = secretLister;
    }

    Set<V1EnvVar> createEnvironmentConfig(String gatewayNamespace, V1SpringCloudGateway gateway) throws ApiException {
        Set<V1EnvVar> envVars = Sets.newHashSet(new V1EnvVar[0]);
        if (this.isPrometheusEnabled(gateway)) {
            envVars.addAll(Set.of((new V1EnvVar()).name("management.endpoint.metrics.enabled").value("true"), (new V1EnvVar()).name("spring.cloud.gateway.metrics.enabled").value("true"), (new V1EnvVar()).name("spring.cloud.gateway.metrics.tags.path.enabled").value("true"), (new V1EnvVar()).name("management.metrics.export.prometheus.enabled").value("true")));
        } else {
            envVars.add((new V1EnvVar()).name("management.metrics.export.prometheus.enabled").value("false"));
        }

        if (this.isWavefrontEnabled(gateway)) {
            V1SpringCloudGatewaySpecObservabilityWavefront wavefront = gateway.getSpec().getObservability().getWavefront();
            if (wavefront == null || wavefront.getSecret() == null) {
                throw new ApiException("wavefront not set");
            }

            String metricsSecretName = wavefront.getSecret();
            V1Secret metricsSecret = this.secretLister.getSecret(gatewayNamespace, metricsSecretName);
            if (metricsSecret == null) {
                throw new ApiException(String.format("%s secret not found", metricsSecretName));
            }

            if (!this.secretIsValid(metricsSecret)) {
                throw new ApiException(String.format("Could not find all required keys %s in secret %s/%s", String.join(",", EXPECTED_SECRET_KEYS), gatewayNamespace, metricsSecretName));
            }

            envVars.addAll(this.getEnvVarsFromSecretValues(metricsSecretName));
            envVars.addAll(this.enableMetrics());
            envVars.addAll(this.createWavefrontApplicationVariables(gatewayNamespace, gateway.getMetadata().getName(), wavefront));
        } else {
            envVars.add((new V1EnvVar()).name("management.metrics.export.wavefront.enabled").value("false"));
        }

        return envVars;
    }

    Map<String, String> createMetricsAnnotations(V1SpringCloudGateway gateway, int actuatorPort) {
        return this.arePrometheusAnnotationsEnabled(gateway) ? this.createPrometheusAnnotations(actuatorPort) : Collections.emptyMap();
    }

    private boolean arePrometheusAnnotationsEnabled(V1SpringCloudGateway gateway) {
        return this.isPrometheusEnabled(gateway) && gateway.getSpec().getObservability().getMetrics().getPrometheus().getAnnotations() != null && gateway.getSpec().getObservability().getMetrics().getPrometheus().getAnnotations().getEnabled() != null && gateway.getSpec().getObservability().getMetrics().getPrometheus().getAnnotations().getEnabled();
    }

    boolean isServiceMonitorEnabled(V1SpringCloudGateway gateway) {
        return this.isPrometheusEnabled(gateway) && gateway.getSpec().getObservability().getMetrics().getPrometheus().getServiceMonitor() != null && gateway.getSpec().getObservability().getMetrics().getPrometheus().getServiceMonitor().getEnabled() != null && gateway.getSpec().getObservability().getMetrics().getPrometheus().getServiceMonitor().getEnabled();
    }

    Map<String, String> getServiceMonitorLabels(V1SpringCloudGateway gateway) {
        Map<String, String> labels = new HashMap();
        if (this.isServiceMonitorEnabled(gateway) && gateway.getSpec().getObservability().getMetrics().getPrometheus().getServiceMonitor().getLabels() != null) {
            labels.putAll(gateway.getSpec().getObservability().getMetrics().getPrometheus().getServiceMonitor().getLabels());
        }

        return labels;
    }

    private Set<V1EnvVar> enableMetrics() {
        return Sets.newHashSet(new V1EnvVar[]{(new V1EnvVar()).name("management.endpoint.metrics.enabled").value("true"), (new V1EnvVar()).name("spring.cloud.gateway.metrics.enabled").value("true"), (new V1EnvVar()).name("spring.cloud.gateway.metrics.tags.path.enabled").value("true"), (new V1EnvVar()).name("management.metrics.export.wavefront.enabled").value("true")});
    }

    private Set<V1EnvVar> createWavefrontApplicationVariables(String gatewayNamespace, String gatewayName, V1SpringCloudGatewaySpecObservabilityWavefront wavefront) {
        Optional<String> sourceOverride = Optional.ofNullable(wavefront.getSource());
        Optional<String> applicationNameOverride = Optional.ofNullable(wavefront.getApplication());
        Optional<String> serviceNameOverride = Optional.ofNullable(wavefront.getService());
        String wavefrontApplicationName = (String)applicationNameOverride.orElse(gatewayNamespace);
        String wavefrontService = (String)serviceNameOverride.orElse(gatewayName);
        V1EnvVar sourceEnVar = (new V1EnvVar()).name("management.metrics.export.wavefront.source");
        Objects.requireNonNull(sourceEnVar);
        sourceOverride.ifPresentOrElse(sourceEnVar::value, () -> {
            sourceEnVar.valueFrom((new V1EnvVarSource()).fieldRef((new V1ObjectFieldSelector()).apiVersion("v1").fieldPath("metadata.name")));
        });
        return Sets.newHashSet(new V1EnvVar[]{sourceEnVar, (new V1EnvVar()).name("wavefront.application.name").value(wavefrontApplicationName), (new V1EnvVar()).name("wavefront.application.service").value(wavefrontService)});
    }

    private Set<V1EnvVar> getEnvVarsFromSecretValues(String metricsSecretName) {
        return (Set)Stream.of((new V1EnvVar()).name("management.metrics.export.wavefront.api-token").valueFrom(this.envVarSourceFromSecret("wavefront.api-token", metricsSecretName)), (new V1EnvVar()).name("management.metrics.export.wavefront.uri").valueFrom(this.envVarSourceFromSecret("wavefront.uri", metricsSecretName))).collect(Collectors.toCollection(HashSet::new));
    }

    private boolean secretIsValid(V1Secret metricsSecret) {
        return metricsSecret.getData() != null && metricsSecret.getData().keySet().containsAll(EXPECTED_SECRET_KEYS);
    }

    private V1EnvVarSource envVarSourceFromSecret(String secretKey, String metricsSecret) {
        return (new V1EnvVarSource()).secretKeyRef((new V1SecretKeySelector()).name(metricsSecret).key(secretKey));
    }

    private boolean isPrometheusEnabled(V1SpringCloudGateway gateway) {
        return gateway.getSpec() != null && gateway.getSpec().getObservability() != null && gateway.getSpec().getObservability().getMetrics() != null && gateway.getSpec().getObservability().getMetrics().getPrometheus() != null && gateway.getSpec().getObservability().getMetrics().getPrometheus().getEnabled() != null && gateway.getSpec().getObservability().getMetrics().getPrometheus().getEnabled();
    }

    private boolean isWavefrontEnabled(V1SpringCloudGateway gateway) {
        return gateway.getSpec() != null && gateway.getSpec().getObservability() != null && gateway.getSpec().getObservability().getMetrics() != null && gateway.getSpec().getObservability().getMetrics().getWavefront() != null && gateway.getSpec().getObservability().getMetrics().getWavefront().getEnabled() != null && gateway.getSpec().getObservability().getMetrics().getWavefront().getEnabled();
    }

    private Map<String, String> createPrometheusAnnotations(int actuatorPort) {
        return Map.of("prometheus.io/scrape", "true", "prometheus.io/path", "/actuator/prometheus", "prometheus.io/port", String.valueOf(actuatorPort));
    }
}

