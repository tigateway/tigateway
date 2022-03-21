package com.vmware.tanzu.springcloudgateway.apis;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1ObjectReference;

public class ObjectReferenceConverter {
    public ObjectReferenceConverter() {
    }

    public static V1ObjectReference toObjectReference(String defaultNamespace, String namespace, String name) {
        return (new V1ObjectReference()).namespace(namespace == null ? defaultNamespace : namespace).name(name);
    }

    public static V1ObjectReference toObjectReference(V1ObjectMeta metadata) {
        return (new V1ObjectReference()).namespace(metadata.getNamespace()).name(metadata.getName());
    }

    public static V1ObjectReference toObjectReference(KubernetesObject k8sObject) {
        return (new V1ObjectReference()).apiVersion(k8sObject.getApiVersion()).kind(k8sObject.getKind()).uid(k8sObject.getMetadata().getUid()).name(k8sObject.getMetadata().getName()).namespace(k8sObject.getMetadata().getNamespace());
    }
}
