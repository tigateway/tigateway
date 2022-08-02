package ti.gateway.operator.springcloudgateway.gateway;

import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.openapi.models.V1ConfigMapProjection;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1ProjectedVolumeSource;
import io.kubernetes.client.openapi.models.V1SecretVolumeSource;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.kubernetes.client.openapi.models.V1VolumeProjection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

abstract class AbstractStatefulSetContributor implements StatefulSetContributor {
    AbstractStatefulSetContributor() {
    }

    public final void accept(V1StatefulSet statefulSet, V1SpringCloudGateway gateway) {
        if (this.appliesTo(gateway)) {
            this.apply(statefulSet, gateway);
        }

    }

    abstract void apply(V1StatefulSet statefulSet, V1SpringCloudGateway gateway);

    boolean appliesTo(V1SpringCloudGateway gateway) {
        return true;
    }

    static void addGatewayEnvironmentVariables(V1StatefulSet statefulSet, V1EnvVar... envVars) {
        addGatewayEnvironmentVariables(statefulSet, Set.of(envVars));
    }

    static void addGatewayEnvironmentVariables(V1StatefulSet statefulSet, Set<V1EnvVar> envVars) {
        V1Container gatewayContainer = gatewayContainer(statefulSet);
        Set<V1EnvVar> gatewayEnvironment = new TreeSet(Comparator.comparing(V1EnvVar::getName));
        if (gatewayContainer.getEnv() != null) {
            gatewayEnvironment.addAll(gatewayContainer.getEnv());
        }

        gatewayEnvironment.addAll(envVars);
        gatewayContainer.setEnv(new ArrayList<>(gatewayEnvironment));
    }

    static void addGatewayVolumeMount(V1StatefulSet statefulSet, String volumeName, String mountPath, boolean readonly) {
        addGatewayVolumeMount(statefulSet, volumeName, mountPath, (String)null, readonly);
    }

    static void addGatewayVolumeMount(V1StatefulSet statefulSet, String volumeName, String mountPath, String subPath, boolean readonly) {
        gatewayContainer(statefulSet).addVolumeMountsItem((new V1VolumeMount()).name(volumeName).mountPath(mountPath).subPath(subPath).readOnly(readonly));
    }

    static void addGatewayVolumeReadOnly(V1StatefulSet statefulSet, String pvcName) {
        statefulSet.getSpec().getTemplate().getSpec().addVolumesItem((new V1Volume()).name(pvcName).persistentVolumeClaim((new V1PersistentVolumeClaimVolumeSource()).claimName(pvcName).readOnly(true)));
    }

    static void addProjectedConfigMapVolumes(V1StatefulSet statefulSet, List<String> configMapNames, String volumeName) {
        List<V1Volume> volumes = new ArrayList<>();
        volumes.add((new V1Volume()).name(volumeName).projected((new V1ProjectedVolumeSource()).sources((List)configMapNames.stream().map((configMapName) -> {
            return (new V1VolumeProjection()).configMap((new V1ConfigMapProjection()).name(configMapName));
        }).collect(Collectors.toList()))));
        V1PodSpec podSpec = statefulSet.getSpec().getTemplate().getSpec();
        podSpec.volumes(volumes);
    }

    static void addVolumeFromSecret(V1StatefulSet statefulSet, String volumeName, String secretName) {
        V1PodSpec podSpec = statefulSet.getSpec().getTemplate().getSpec();
        if (podSpec.getVolumes() == null) {
            podSpec.volumes(new ArrayList());
        }

        podSpec.getVolumes().add((new V1Volume()).name(volumeName).secret((new V1SecretVolumeSource()).secretName(secretName)));
    }

    static V1Container gatewayContainer(V1StatefulSet statefulSet) {
        return (V1Container)statefulSet.getSpec().getTemplate().getSpec().getContainers().stream().filter((container) -> {
            return "gateway".equals(container.getName());
        }).findFirst().orElseThrow(() -> {
            return new IllegalStateException("No gateway container found in stateful set");
        });
    }
}
