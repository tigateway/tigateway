package com.vmware.tanzu.springcloudgateway.gateway;

import com.google.gson.Gson;
import com.vmware.tanzu.springcloudgateway.apis.EventRecorder;
import com.vmware.tanzu.springcloudgateway.apis.LabelsBuilder;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpec;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpecResources;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpecResourcesLimits;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpecSecurityContext;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpecSso;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1HTTPGetAction;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1LocalObjectReference;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodReadinessGate;
import io.kubernetes.client.openapi.models.V1PodSecurityContext;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1Probe;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1SecurityContext;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1StatefulSetSpec;
import io.kubernetes.client.openapi.models.V1StatefulSetUpdateStrategy;
import io.kubernetes.client.util.PatchUtils;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatefulSetBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatefulSetBuilder.class);
    private static final String DEFAULT_RESOURCE_LIMITS_FILE = "default-gateway-statefulset-resource-limits.json";
    private static final Set<V1EnvVar> DEFAULT_ENV_VARS = (Set<V1EnvVar>)Stream.of(
            (new V1EnvVar()).name("management.endpoint.gateway.enabled").value("true"),
            (new V1EnvVar()).name("management.endpoints.web.exposure.include").value("gateway,health,info,conditions,configprops,metrics,prometheus")
    ).collect(Collectors.toSet());
    private static final Gson GSON = new Gson();
    private static final int DEFAULT_PROBE_INITIAL_DELAY = 5;
    private static final int DEFAULT_PROBE_FAILURE_THRESHOLD = 10;
    private static final int DEFAULT_PROBE_SUCCESS_THRESHOLD = 1;
    private static final int DEFAULT_PROBE_PERIOD_SECONDS = 3;
    private static final int DEFAULT_PROBE_TIMEOUT_SECONDS = 1;
    private static final long DEFAULT_TERMINATION_GRACE_SECONDS = 10L;
    private static final int ACTUATOR_PORT = 8090;
    private static final String DEFAULT_PROBE_SCHEME = "HTTP";
    private static final String DEFAULT_IMAGE_PULL_POLICY = "IfNotPresent";
    private static final String DEFAULT_RESTART_POLICY = "Always";
    private static final String DEFAULT_UPDATE_POLICY = "RollingUpdate";
    private final String gatewayImageName;
    private final AppsV1Api appsV1Api;
    private final SsoParameters ssoParameters;
    private final MetricsParameters metricsParameters;
    private final TracingParameters tracingParameters;
    private final String imagePullSecretName;
    private final EventRecorder eventRecorder;
    private final Collection<StatefulSetContributor> statefulSetContributors;
    static final String STATEFULSET_KIND = "StatefulSet";
    static final String STATEFULSET_API_VERSION = "apps/v1";
    static final String GATEWAY_CONTAINER_NAME = "gateway";

    public StatefulSetBuilder(String gatewayImageName, AppsV1Api appsV1Api, SsoParameters ssoParameters, MetricsParameters metricsParameters, TracingParameters tracingParameters, String imagePullSecretName, EventRecorder eventRecorder, Collection<StatefulSetContributor> statefulSetContributors) {
        this.gatewayImageName = gatewayImageName;
        this.appsV1Api = appsV1Api;
        this.ssoParameters = ssoParameters;
        this.metricsParameters = metricsParameters;
        this.tracingParameters = tracingParameters;
        this.imagePullSecretName = imagePullSecretName;
        this.eventRecorder = eventRecorder;
        this.statefulSetContributors = statefulSetContributors;
    }

    V1StatefulSet createStatefulSet(String namespace, V1SpringCloudGateway gateway) throws ApiException {
        EnvVarsBucket statefulSetEnvVars = new EnvVarsBucket();
        statefulSetEnvVars.addAll(DEFAULT_ENV_VARS);
        statefulSetEnvVars.addAll(this.buildBuildInfoEnvVar());
        statefulSetEnvVars.addAll(this.buildSsoEnvVar(namespace, gateway));
        statefulSetEnvVars.addAll(this.buildMetricsEnvVar(namespace, gateway));
        statefulSetEnvVars.addAll(this.buildTracingEnvVar(namespace, gateway));
        statefulSetEnvVars.addAll(this.buildJavaEnvVars(gateway));
        statefulSetEnvVars.addAll(this.buildSpecEnvVars(gateway));
        String gatewayName = ((V1ObjectMeta)Objects.requireNonNull(gateway.getMetadata())).getName();
        LOGGER.debug("Creating stateful set with name {}", gatewayName);
        V1SpringCloudGatewaySpec spec = gateway.getSpec();
        V1ResourceRequirements resourceRequirements;
        if (spec != null && (resourceRequirements = this.toV1ResourceRequirements(spec)) != null) {
            LOGGER.debug("Gateway resource requirements are specified: {}", resourceRequirements);
        } else {
            resourceRequirements = (V1ResourceRequirements)GSON.fromJson(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("default-gateway-statefulset-resource-limits.json")), V1ResourceRequirements.class);
            LOGGER.debug("Gateway resource requirements are not specified. Use defaults: {}", resourceRequirements);
        }

        V1Container v1Container = (new V1Container()).name("gateway").image(this.gatewayImageName).imagePullPolicy("IfNotPresent").resources(resourceRequirements).livenessProbe((new V1Probe()).initialDelaySeconds(5).failureThreshold(10).periodSeconds(3).timeoutSeconds(1).successThreshold(1).httpGet((new V1HTTPGetAction()).port(new IntOrString(8090)).scheme("HTTP").path("actuator/health/liveness"))).readinessProbe((new V1Probe()).initialDelaySeconds(5).failureThreshold(10).periodSeconds(3).timeoutSeconds(1).successThreshold(1).httpGet((new V1HTTPGetAction()).port(new IntOrString(8090)).scheme("HTTP").path("actuator/health/readiness"))).startupProbe((new V1Probe()).initialDelaySeconds(10).failureThreshold(30).periodSeconds(3).timeoutSeconds(1).successThreshold(1).httpGet((new V1HTTPGetAction()).port(new IntOrString(8090)).scheme("HTTP").path("actuator/health/readiness"))).env(new ArrayList(statefulSetEnvVars.getValues())).securityContext((new V1SecurityContext()).privileged(false).allowPrivilegeEscalation(false));
        V1PodSpec podSpec = (new V1PodSpec()).imagePullSecrets(Collections.singletonList((new V1LocalObjectReference()).name(this.imagePullSecretName))).containers(Collections.singletonList(v1Container)).restartPolicy("Always").securityContext(this.gatewayPodSecurityContext(gateway)).terminationGracePeriodSeconds(10L).addReadinessGatesItem((new V1PodReadinessGate()).conditionType("RoutesUpToDate"));
        Map<String, String> labels = LabelsBuilder.build(gatewayName);
        if (gateway.getMetadata() != null && gateway.getMetadata().getLabels() != null) {
            labels.putAll(gateway.getMetadata().getLabels());
        }

        podSpec.serviceAccountName(RbacBuilder.buildServiceAccountName(gateway));
        V1StatefulSet statefulSet = (new V1StatefulSet()).apiVersion("apps/v1").kind("StatefulSet").metadata((new V1ObjectMeta()).name(gatewayName).namespace(namespace).labels(labels)).spec((new V1StatefulSetSpec()).serviceName(ServiceBuilder.headlessServiceName(gatewayName)).replicas(getReplicas(gateway)).updateStrategy((new V1StatefulSetUpdateStrategy()).type("RollingUpdate")).selector((new V1LabelSelector()).matchLabels(LabelsBuilder.buildServiceSelector(gatewayName))).template((new V1PodTemplateSpec()).metadata((new V1ObjectMeta()).labels(labels).annotations(this.createAnnotations(gateway))).spec(podSpec)));
        this.statefulSetContributors.forEach((contributor) -> {
            contributor.accept(statefulSet, gateway);
        });
        return statefulSet;
    }

    private V1PodSecurityContext gatewayPodSecurityContext(V1SpringCloudGateway gateway) {
        V1PodSecurityContext defaultSecurityContext = (new V1PodSecurityContext()).fsGroup(1000L);
        if (this.hasSecurityContext(gateway)) {
            V1SpringCloudGatewaySpecSecurityContext userSecurityContext = gateway.getSpec().getSecurityContext();
            if (userSecurityContext.getRunAsUser() != null) {
                defaultSecurityContext.runAsUser(userSecurityContext.getRunAsUser().longValue());
            }

            if (userSecurityContext.getRunAsGroup() != null) {
                defaultSecurityContext.runAsGroup(userSecurityContext.getRunAsGroup().longValue());
            }

            if (userSecurityContext.getFsGroup() != null) {
                defaultSecurityContext.fsGroup(userSecurityContext.getFsGroup().longValue());
            }
        }

        return defaultSecurityContext;
    }

    private boolean hasSecurityContext(V1SpringCloudGateway gateway) {
        return gateway.getSpec() != null && gateway.getSpec().getSecurityContext() != null;
    }

    private Map<String, String> createAnnotations(V1SpringCloudGateway gateway) {
        Map<String, String> annotations = new HashMap<>(this.metricsParameters.createMetricsAnnotations(gateway, 8090));
        return annotations.isEmpty() ? null : annotations;
    }

    private V1ResourceRequirements toV1ResourceRequirements(V1SpringCloudGatewaySpec spec) {
        V1SpringCloudGatewaySpecResources resources = spec.getResources();
        if (resources == null) {
            return null;
        } else {
            V1ResourceRequirements resourceRequirements = new V1ResourceRequirements();
            Optional<Map<String, Quantity>> stringQuantityMap = this.toMap(resources.getLimits());
            Objects.requireNonNull(resourceRequirements);
            stringQuantityMap.ifPresent(resourceRequirements::setLimits);
            stringQuantityMap = this.toMap(resources.getRequests());
            Objects.requireNonNull(resourceRequirements);
            stringQuantityMap.ifPresent(resourceRequirements::setRequests);
            return resourceRequirements;
        }
    }

    private Optional<Map<String, Quantity>> toMap(V1SpringCloudGatewaySpecResourcesLimits limits) {
        if (limits == null) {
            return Optional.empty();
        } else {
            Map<String, Quantity> limitMap = new HashMap<>();
            if (limits.getCpu() != null) {
                limitMap.put("cpu", new Quantity(limits.getCpu()));
            }

            if (limits.getMemory() != null) {
                limitMap.put("memory", new Quantity(limits.getMemory()));
            }

            return Optional.of(limitMap);
        }
    }

    private static Integer getReplicas(V1SpringCloudGateway gateway) {
        return gateway.getSpec() != null && gateway.getSpec().getCount() != null ? gateway.getSpec().getCount() : 1;
    }

    private Set<V1EnvVar> buildJavaEnvVars(V1SpringCloudGateway desiredGateway) {
        return desiredGateway.getSpec() != null && desiredGateway.getSpec().getJavaOpts() != null ? Set.of((new V1EnvVar()).name("JAVA_OPTS").value(desiredGateway.getSpec().getJavaOpts())) : Collections.emptySet();
    }

    private Set<V1EnvVar> buildSsoEnvVar(String namespace, V1SpringCloudGateway desiredGateway) throws ApiException {
        if (SsoParameters.isEnabled(desiredGateway)) {
            try {
                LOGGER.debug("Generating SSO environment for desired StatefulSet");
                V1SpringCloudGatewaySpecSso ssoSpec = desiredGateway.getSpec().getSso();
                return this.ssoParameters.createEnvironmentConfig(namespace, ssoSpec.getSecret(), ssoSpec.getRolesAttributeName(), ssoSpec.getInactiveSessionExpirationInMinutes());
            } catch (ApiException var5) {
                String errorMessage = String.format("error configuring SpringCloudGateway using SSO parameters %s", desiredGateway.getSpec().toString());
                LOGGER.error(errorMessage, var5);
                this.eventRecorder.recordGatewayEvent(namespace, desiredGateway, errorMessage);
                throw new ApiException(var5);
            }
        } else {
            return Collections.emptySet();
        }
    }

    private Set<V1EnvVar> buildMetricsEnvVar(String namespace, V1SpringCloudGateway desiredGateway) throws ApiException {
        try {
            return this.metricsParameters.createEnvironmentConfig(namespace, desiredGateway);
        } catch (ApiException var5) {
            String errorMessage = String.format("error configuring SpringCloudGateway using metrics parameters %s", desiredGateway.getSpec().toString());
            LOGGER.error(errorMessage, var5);
            this.eventRecorder.recordGatewayEvent(namespace, desiredGateway, errorMessage);
            throw new ApiException(var5);
        }
    }

    private Set<V1EnvVar> buildTracingEnvVar(String namespace, V1SpringCloudGateway desiredGateway) throws ApiException {
        if (TracingParameters.isEnabled(desiredGateway)) {
            try {
                LOGGER.debug("Generating tracing environment for desired StatefulSet");
                return this.tracingParameters.createEnvironmentConfig(namespace, desiredGateway.getMetadata().getName(), desiredGateway.getSpec().getObservability().getWavefront());
            } catch (ApiException var5) {
                String errorMessage = String.format("error configuring SpringCloudGateway using tracing parameters %s", desiredGateway.getSpec().toString());
                LOGGER.error(errorMessage, var5);
                this.eventRecorder.recordGatewayEvent(namespace, desiredGateway, errorMessage);
                throw new ApiException(var5);
            }
        } else {
            return TracingParameters.createTracingDisabledVariables();
        }
    }

    private Set<V1EnvVar> buildSpecEnvVars(V1SpringCloudGateway gateway) {
        return hasEnvironmentVariablesInSpec(gateway) ? (Set<V1EnvVar>)gateway.getSpec().getEnv().stream().map((specEnvVar) -> {
            return (new V1EnvVar()).name(specEnvVar.getName()).value(specEnvVar.getValue());
        }).collect(Collectors.toSet()) : Collections.emptySet();
    }

    private Set<V1EnvVar> buildBuildInfoEnvVar() {
        return Set.of((new V1EnvVar()).name("com.vmware.tanzu.springcloudgateway.version").value(this.gatewayImageName.substring(this.gatewayImageName.indexOf(":") + 1)));
    }

    private static boolean hasEnvironmentVariablesInSpec(V1SpringCloudGateway gateway) {
        return gateway.getSpec() != null && gateway.getSpec().getEnv() != null && !gateway.getSpec().getEnv().isEmpty();
    }

    V1StatefulSet patchStatefulSet(String desiredNamespace, String statefulSetName, String statefulSetPatch) throws ApiException {
        LOGGER.info("Patching StatefulSet {}/{} with desired state: {}", new Object[]{desiredNamespace, statefulSetName, statefulSetPatch});
        return (V1StatefulSet)PatchUtils.patch(V1StatefulSet.class, () -> {
            return this.appsV1Api.patchNamespacedStatefulSetCall(statefulSetName, desiredNamespace, new V1Patch(statefulSetPatch), (String)null, (String)null, (String)null, (Boolean)null, (ApiCallback)null);
        }, "application/merge-patch+json", this.appsV1Api.getApiClient());
    }
}

