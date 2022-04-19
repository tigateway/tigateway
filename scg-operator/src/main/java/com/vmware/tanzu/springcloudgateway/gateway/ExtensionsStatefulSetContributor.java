package com.vmware.tanzu.springcloudgateway.gateway;

import com.vmware.tanzu.springcloudgateway.apis.EventRecorder;
import com.vmware.tanzu.springcloudgateway.apis.ObjectReferenceConverter;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.extended.event.EventType;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ObjectReference;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
class ExtensionsStatefulSetContributor extends AbstractStatefulSetContributor {
    private final EventRecorder eventRecorder;
    private final Lister<V1ConfigMap> configMapLister;
    private final Lister<V1PersistentVolumeClaim> persistentVolumeClaimLister;

    ExtensionsStatefulSetContributor(EventRecorder eventRecorder, Lister<V1ConfigMap> configMapLister, SharedIndexInformer<V1PersistentVolumeClaim> persistentVolumeClaimIndexInformer) {
        this.eventRecorder = eventRecorder;
        this.configMapLister = configMapLister;
        this.persistentVolumeClaimLister = new Lister<>(persistentVolumeClaimIndexInformer.getIndexer());
    }

    void apply(V1StatefulSet statefulSet, V1SpringCloudGateway gateway) {
        List<String> validConfigMapExtensions = new ArrayList<>();
        List<String> validPersistentVolumeClaimExtensions = new ArrayList<>();
        List<String> invalidExtensions = new ArrayList<>();
        gateway.getSpec().getExtensions().getCustom().forEach((extensionName) -> {
            if (this.isConfigMapValid(extensionName, gateway.getMetadata().getNamespace())) {
                validConfigMapExtensions.add(extensionName);
            } else if (this.isPersistentVolumeClaimValid(extensionName, gateway.getMetadata().getNamespace())) {
                validPersistentVolumeClaimExtensions.add(extensionName);
            } else {
                invalidExtensions.add(extensionName);
            }

        });
        invalidExtensions.forEach((extensionName) -> {
            this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(gateway), (V1ObjectReference)null, "InvalidExtensionException", "Could not find ConfigMap or PersistentVolumeClaim with name '" + extensionName + "'. Skipping configuration.", EventType.Warning);
        });
        if (!validConfigMapExtensions.isEmpty()) {
            String volumeName = "extensions";
            addGatewayVolumeMount(statefulSet, volumeName, "/workspace/extensions/", true);
            addProjectedConfigMapVolumes(statefulSet, validConfigMapExtensions, volumeName);
        }

        if (!validPersistentVolumeClaimExtensions.isEmpty()) {
            validPersistentVolumeClaimExtensions.forEach((pvc) -> {
                addGatewayVolumeMount(statefulSet, pvc, "/workspace/BOOT-INF/lib/" + pvc, "extensions", true);
                addGatewayVolumeReadOnly(statefulSet, pvc);
            });
        }

    }

    boolean appliesTo(V1SpringCloudGateway gateway) {
        return gateway.getSpec() != null && gateway.getSpec().getExtensions() != null && !CollectionUtils.isEmpty(gateway.getSpec().getExtensions().getCustom());
    }

    private boolean isConfigMapValid(String name, String gatewayNamespace) {
        V1ConfigMap configMap = (V1ConfigMap)this.configMapLister.namespace(gatewayNamespace).get(name);
        return configMap != null;
    }

    private boolean isPersistentVolumeClaimValid(String name, String gatewayNamespace) {
        V1PersistentVolumeClaim persistentVolumeClaim = (V1PersistentVolumeClaim)this.persistentVolumeClaimLister.namespace(gatewayNamespace).get(name);
        return persistentVolumeClaim != null;
    }
}

