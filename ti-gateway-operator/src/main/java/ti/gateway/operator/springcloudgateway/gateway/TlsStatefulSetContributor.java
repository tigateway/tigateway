package ti.gateway.operator.springcloudgateway.gateway;

import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGateway;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewaySpecTls;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import java.util.ListIterator;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
class TlsStatefulSetContributor extends AbstractStatefulSetContributor {
    private static final String TLS_ENV_VAR_PREFIX = "SPRING_CLOUD_GATEWAY_K8S_TLS";

    TlsStatefulSetContributor() {
    }

    void apply(V1StatefulSet statefulSet, V1SpringCloudGateway gateway) {
        ListIterator tlsIterator = gateway.getSpec().getTls().listIterator();

        while(tlsIterator.hasNext()) {
            int tlsIndex = tlsIterator.nextIndex();
            V1SpringCloudGatewaySpecTls tls = (V1SpringCloudGatewaySpecTls)tlsIterator.next();
            ListIterator hostIterator = tls.getHosts().listIterator();

            String volumeName;
            while(hostIterator.hasNext()) {
                int hostIndex = hostIterator.nextIndex();
                volumeName = (String)hostIterator.next();
                addGatewayEnvironmentVariables(statefulSet, new V1EnvVar[]{hostEnvVar(tlsIndex, hostIndex, volumeName)});
            }

            String secretName = tls.getSecretName();
            volumeName = "tls-" + secretName;
            String mountPath = "/workspace/tls/" + secretName;
            addGatewayVolumeMount(statefulSet, volumeName, mountPath, false);
            addVolumeFromSecret(statefulSet, volumeName, secretName);
            addGatewayEnvironmentVariables(statefulSet, new V1EnvVar[]{secretEnvVar(tlsIndex, mountPath)});
        }

    }

    boolean appliesTo(V1SpringCloudGateway gateway) {
        return gateway.getSpec() != null && !CollectionUtils.isEmpty(gateway.getSpec().getTls());
    }

    private static V1EnvVar hostEnvVar(int tlsIndex, int hostIndex, String hostName) {
        return (new V1EnvVar()).name(String.format("%s_SERVERS_%d_HOSTS_%d", "SPRING_CLOUD_GATEWAY_K8S_TLS", tlsIndex, hostIndex)).value(hostName);
    }

    private static V1EnvVar secretEnvVar(int tlsIndex, String mountPath) {
        return (new V1EnvVar()).name(String.format("%s_SERVERS_%d_SECRET", "SPRING_CLOUD_GATEWAY_K8S_TLS", tlsIndex)).value(mountPath);
    }
}
