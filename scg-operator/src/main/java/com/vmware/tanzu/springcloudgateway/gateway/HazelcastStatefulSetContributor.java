package com.vmware.tanzu.springcloudgateway.gateway;

import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class HazelcastStatefulSetContributor extends AbstractStatefulSetContributor {
    static final int HAZELCAST_SERVICE_PORT = 5701;

    public HazelcastStatefulSetContributor() {
    }

    void apply(V1StatefulSet statefulSet, V1SpringCloudGateway gateway) {
        addGatewayEnvironmentVariables(statefulSet, this.buildHazelcastEnvVars(gateway));
    }

    private Set<V1EnvVar> buildHazelcastEnvVars(V1SpringCloudGateway gateway) {
        String namespace = gateway.getMetadata().getNamespace();
        String name = gateway.getMetadata().getName();
        return Set.of((new V1EnvVar()).name("hazelcast.network.join.kubernetes.service-name").value(ServiceBuilder.headlessServiceName(name)), (new V1EnvVar()).name("hazelcast.network.join.kubernetes.service-port").value(Integer.toString(5701)), (new V1EnvVar()).name("hazelcast.network.join.kubernetes.namespace").value(namespace), (new V1EnvVar()).name("JDK_JAVA_OPTIONS").value("--add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.management/sun.management=ALL-UNNAMED --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"));
    }
}

