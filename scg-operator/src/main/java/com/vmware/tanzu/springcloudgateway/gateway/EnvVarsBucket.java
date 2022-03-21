package com.vmware.tanzu.springcloudgateway.gateway;

import io.kubernetes.client.openapi.models.V1EnvVar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class EnvVarsBucket {
    private final Map<String, V1EnvVar> bucketValues = new HashMap();

    EnvVarsBucket() {
    }

    public void addAll(Collection<V1EnvVar> values) {
        values.forEach((envVar) -> {
            this.bucketValues.put(envVar.getName(), envVar);
        });
    }

    public Collection<V1EnvVar> getValues() {
        return this.bucketValues.values();
    }
}
