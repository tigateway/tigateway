package ti.gateway.operator.springcloudgateway.gateway;

import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGateway;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewaySpecObservabilityWavefront;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarSource;
import io.kubernetes.client.openapi.models.V1ObjectFieldSelector;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretKeySelector;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.compress.utils.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TracingParameters {
    private static final String SECRET_WAVEFRONT_API_TOKEN_KEY = "wavefront.api-token";
    private static final String SECRET_WAVEFRONT_URI_KEY = "wavefront.uri";
    private static final String WAVEFRONT_API_TOKEN_KEY = "management.metrics.export.wavefront.api-token";
    private static final String WAVEFRONT_URI_KEY = "management.metrics.export.wavefront.uri";
    private static final String WAVEFRONT_SOURCE_KEY = "management.metrics.export.wavefront.source";
    private static final String WAVEFRONT_APPLICATION_NAME = "wavefront.application.name";
    private static final String WAVEFRONT_APPLICATION_SERVICE = "wavefront.application.service";
    private static final Logger LOGGER = LoggerFactory.getLogger(TracingParameters.class);
    private final SecretLister secretLister;
    private static final Set<String> EXPECTED_SECRET_KEYS = (Set)Stream.of("wavefront.api-token", "wavefront.uri").collect(Collectors.toSet());

    public TracingParameters(SecretLister secretLister) {
        this.secretLister = secretLister;
    }

    Set<V1EnvVar> createEnvironmentConfig(String gatewayNamespace, String gatewayName, V1SpringCloudGatewaySpecObservabilityWavefront wavefront) throws ApiException {
        String secretName = wavefront.getSecret();
        LOGGER.debug("finding tracing secret {} in namespace {}", secretName, gatewayNamespace);
        V1Secret tracingSecret = this.secretLister.getSecret(gatewayNamespace, secretName);
        if (tracingSecret == null) {
            throw new ApiException(String.format("%s secret not found", secretName));
        } else if (!this.secretIsValid(tracingSecret)) {
            throw new ApiException(String.format("Could not find all required keys %s in secret %s/%s", String.join(",", EXPECTED_SECRET_KEYS), gatewayNamespace, secretName));
        } else {
            Set<V1EnvVar> envVars = this.getEnvVarsFromSecretValues(secretName);
            envVars.addAll(this.enableTracing());
            envVars.addAll(this.createWavefrontApplicationVariables(gatewayNamespace, gatewayName, wavefront));
            return envVars;
        }
    }

    private Set<V1EnvVar> enableTracing() {
        return Sets.newHashSet(new V1EnvVar[]{(new V1EnvVar()).name("spring.sleuth.enabled").value("true"), (new V1EnvVar()).name("management.metrics.export.wavefront.enabled").value("true")});
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
            sourceEnVar.valueFrom((new V1EnvVarSource()).fieldRef((new V1ObjectFieldSelector()).fieldPath("metadata.name")));
        });
        return Sets.newHashSet(new V1EnvVar[]{sourceEnVar, (new V1EnvVar()).name("wavefront.application.name").value(wavefrontApplicationName), (new V1EnvVar()).name("wavefront.application.service").value(wavefrontService), (new V1EnvVar()).name("spring.application.name").value(wavefrontApplicationName)});
    }

    public static Set<V1EnvVar> createTracingDisabledVariables() {
        return Set.of((new V1EnvVar()).name("spring.sleuth.enabled").value("false"));
    }

    private Set<V1EnvVar> getEnvVarsFromSecretValues(String tracingSecretName) {
        return (Set)Stream.of((new V1EnvVar()).name("management.metrics.export.wavefront.api-token").valueFrom(this.envVarSourceFromSecret("wavefront.api-token", tracingSecretName)), (new V1EnvVar()).name("management.metrics.export.wavefront.uri").valueFrom(this.envVarSourceFromSecret("wavefront.uri", tracingSecretName))).collect(Collectors.toCollection(HashSet::new));
    }

    private boolean secretIsValid(V1Secret tracingSecret) {
        return tracingSecret.getData() != null && tracingSecret.getData().keySet().containsAll(EXPECTED_SECRET_KEYS);
    }

    private V1EnvVarSource envVarSourceFromSecret(String secretKey, String tracingSecret) {
        return (new V1EnvVarSource()).secretKeyRef((new V1SecretKeySelector()).name(tracingSecret).key(secretKey));
    }

    static boolean isEnabled(V1SpringCloudGateway gateway) {
        return gateway.getSpec() != null && gateway.getSpec().getObservability() != null && gateway.getSpec().getObservability().getTracing() != null && gateway.getSpec().getObservability().getTracing().getWavefront() != null && gateway.getSpec().getObservability().getTracing().getWavefront().getEnabled() != null && gateway.getSpec().getObservability().getTracing().getWavefront().getEnabled();
    }
}
