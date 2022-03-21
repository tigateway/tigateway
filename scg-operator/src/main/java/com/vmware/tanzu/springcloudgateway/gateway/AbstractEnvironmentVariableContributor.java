package com.vmware.tanzu.springcloudgateway.gateway;

import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import java.util.Set;

abstract class AbstractEnvironmentVariableContributor extends AbstractStatefulSetContributor {
    AbstractEnvironmentVariableContributor() {
    }

    public final void apply(V1StatefulSet statefulSet, V1SpringCloudGateway gateway) {
        AbstractStatefulSetContributor.addGatewayEnvironmentVariables(statefulSet, this.createEnvironmentConfig(gateway));
    }

    abstract Set<V1EnvVar> createEnvironmentConfig(V1SpringCloudGateway gateway);
}

