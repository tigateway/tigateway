package ti.gateway.operator.springcloudgateway.gateway;

import ti.gateway.operator.springcloudgateway.apis.EventRecorder;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGateway;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewaySpecApiCors;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class CorsEnvVarsStatefulSetContributor implements StatefulSetContributor {
    private static final String ALLOWED_ORIGINS = "allowed-origins";
    private static final String ALLOWED_METHODS = "allowed-methods";
    private static final String ALLOWED_HEADERS = "allowed-headers";
    private static final String ALLOWED_ORIGIN_PATTERNS = "allowed-origin-patterns";
    private static final String ALLOW_CREDENTIALS = "allow-credentials";
    private static final String EXPOSED_HEADERS = "exposed-headers";
    private static final String MAX_AGE = "max-age";
    private static final Logger LOGGER = LoggerFactory.getLogger(CorsEnvVarsStatefulSetContributor.class);
    static final String CORS_CONFIG_PREFIX = "spring.cloud.gateway.k8s.globalcors.";
    static final String PER_ROUTE_CORS_CONFIG_PREFIX = "spring.cloud.gateway.k8s.cors.";
    static final String GLOBAL_ALLOWED_ORIGINS = "spring.cloud.gateway.k8s.globalcors.allowed-origins";
    static final String GLOBAL_ALLOWED_METHODS = "spring.cloud.gateway.k8s.globalcors.allowed-methods";
    static final String GLOBAL_ALLOWED_HEADERS = "spring.cloud.gateway.k8s.globalcors.allowed-headers";
    static final String GLOBAL_ALLOWED_ORIGIN_PATTERNS = "spring.cloud.gateway.k8s.globalcors.allowed-origin-patterns";
    static final String GLOBAL_ALLOW_CREDENTIALS = "spring.cloud.gateway.k8s.globalcors.allow-credentials";
    static final String GLOBAL_EXPOSED_HEADERS = "spring.cloud.gateway.k8s.globalcors.exposed-headers";
    static final String GLOBAL_MAX_AGE = "spring.cloud.gateway.k8s.globalcors.max-age";
    private final EventRecorder eventRecorder;

    public CorsEnvVarsStatefulSetContributor(EventRecorder eventRecorder) {
        this.eventRecorder = eventRecorder;
    }

    public void accept(V1StatefulSet statefulSet, V1SpringCloudGateway gateway) {
        String namespace = statefulSet.getMetadata().getNamespace();
        V1Container container = (V1Container)statefulSet.getSpec().getTemplate().getSpec().getContainers().get(0);
        ArrayList envVars = new ArrayList<>(container.getEnv());
        envVars.addAll(this.buildCorsEnvVars(namespace, gateway));
        container.setEnv(envVars);
    }

    private Set<V1EnvVar> buildCorsEnvVars(String namespace, V1SpringCloudGateway desiredGateway) {
        if (desiredGateway.getSpec() != null && desiredGateway.getSpec().getApi() != null && desiredGateway.getSpec().getApi().getCors() != null) {
            V1SpringCloudGatewaySpecApiCors corsDefinition = desiredGateway.getSpec().getApi().getCors();
            Set<V1EnvVar> globalCorsConfig = this.createEnvironmentConfig(corsDefinition);
            if (globalCorsConfig.isEmpty()) {
                return this.createCorsPerRoute(corsDefinition);
            } else {
                if (corsDefinition.getPerRoute() != null) {
                    String message = "Per route CORS configuration cannot be applied when global CORS configuration is set.";
                    LOGGER.error(message);
                    this.eventRecorder.recordGatewayEvent(namespace, desiredGateway, message);
                }

                return globalCorsConfig;
            }
        } else {
            return Collections.emptySet();
        }
    }

    private Set<V1EnvVar> createEnvironmentConfig(V1SpringCloudGatewaySpecApiCors specApiCors) {
        if (specApiCors == null) {
            return Collections.emptySet();
        } else {
            HashSet<V1EnvVar> envVars = new HashSet<>();
            if (specApiCors.getMaxAge() != null) {
                envVars.add((new V1EnvVar()).name("spring.cloud.gateway.k8s.globalcors.max-age").value(specApiCors.getMaxAge().toString()));
            }

            if (specApiCors.getAllowCredentials() != null) {
                envVars.add((new V1EnvVar()).name("spring.cloud.gateway.k8s.globalcors.allow-credentials").value(specApiCors.getAllowCredentials().toString()));
            }

            if (!CollectionUtils.isEmpty(specApiCors.getAllowedOrigins())) {
                envVars.add((new V1EnvVar()).name("spring.cloud.gateway.k8s.globalcors.allowed-origins").value(this.concatWithComa(specApiCors.getAllowedOrigins())));
            }

            if (!CollectionUtils.isEmpty(specApiCors.getAllowedMethods())) {
                envVars.add((new V1EnvVar()).name("spring.cloud.gateway.k8s.globalcors.allowed-methods").value(this.concatWithComa(specApiCors.getAllowedMethods())));
            }

            if (!CollectionUtils.isEmpty(specApiCors.getAllowedHeaders())) {
                envVars.add((new V1EnvVar()).name("spring.cloud.gateway.k8s.globalcors.allowed-headers").value(this.concatWithComa(specApiCors.getAllowedHeaders())));
            }

            if (!CollectionUtils.isEmpty(specApiCors.getExposedHeaders())) {
                envVars.add((new V1EnvVar()).name("spring.cloud.gateway.k8s.globalcors.exposed-headers").value(this.concatWithComa(specApiCors.getExposedHeaders())));
            }

            if (!CollectionUtils.isEmpty(specApiCors.getAllowedOriginPatterns())) {
                envVars.add((new V1EnvVar()).name("spring.cloud.gateway.k8s.globalcors.allowed-origin-patterns").value(this.concatWithComa(specApiCors.getAllowedOriginPatterns())));
            }

            return envVars;
        }
    }

    private Set<V1EnvVar> createCorsPerRoute(V1SpringCloudGatewaySpecApiCors specCors) {
        if (specCors != null && specCors.getPerRoute() != null) {
            HashSet<V1EnvVar> envVars = new HashSet<>();
            specCors.getPerRoute().forEach((route, corsConfig) -> {
                if (corsConfig.getAllowCredentials() != null) {
                    envVars.add((new V1EnvVar()).name(this.perRouteCorsEnvVarName(route, "allow-credentials")).value(corsConfig.getAllowCredentials().toString()));
                }

                if (!CollectionUtils.isEmpty(corsConfig.getAllowedHeaders())) {
                    envVars.add((new V1EnvVar()).name(this.perRouteCorsEnvVarName(route, "allowed-headers")).value(this.concatWithComa(corsConfig.getAllowedHeaders())));
                }

                if (!CollectionUtils.isEmpty(corsConfig.getAllowedMethods())) {
                    envVars.add((new V1EnvVar()).name(this.perRouteCorsEnvVarName(route, "allowed-methods")).value(this.concatWithComa(corsConfig.getAllowedMethods())));
                }

                if (!CollectionUtils.isEmpty(corsConfig.getAllowedOrigins())) {
                    envVars.add((new V1EnvVar()).name(this.perRouteCorsEnvVarName(route, "allowed-origins")).value(this.concatWithComa(corsConfig.getAllowedOrigins())));
                }

                if (!CollectionUtils.isEmpty(corsConfig.getExposedHeaders())) {
                    envVars.add((new V1EnvVar()).name(this.perRouteCorsEnvVarName(route, "exposed-headers")).value(this.concatWithComa(corsConfig.getExposedHeaders())));
                }

                if (corsConfig.getMaxAge() != null) {
                    envVars.add((new V1EnvVar()).name(this.perRouteCorsEnvVarName(route, "max-age")).value(corsConfig.getMaxAge().toString()));
                }

                if (corsConfig.getAllowedOriginPatterns() != null) {
                    envVars.add((new V1EnvVar()).name(this.perRouteCorsEnvVarName(route, "allowed-origin-patterns")).value(this.concatWithComa(corsConfig.getAllowedOriginPatterns())));
                }

            });
            return envVars;
        } else {
            return Collections.emptySet();
        }
    }

    private String perRouteCorsEnvVarName(String route, String field) {
        String encodedRoute = new String(Hex.encode(route.getBytes()));
        return String.format("%s%s.%s", "spring.cloud.gateway.k8s.cors.", encodedRoute, field);
    }

    private String concatWithComa(List<String> list) {
        return String.join(",", list);
    }
}
