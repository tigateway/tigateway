package com.vmware.tanzu.springcloudgateway.gateway;

import com.vmware.tanzu.springcloudgateway.apis.EventRecorder;
import com.vmware.tanzu.springcloudgateway.apis.ObjectReferenceConverter;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import com.vmware.tanzu.springcloudgateway.util.PatchUtilsWrapper;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.extended.event.EventType;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigMapReconciler implements Reconciler {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigMapReconciler.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final String JSON_PATCH_FORMAT = "{\"spec\":{\"template\":{\"metadata\":{\"annotations\":{\"kubectl.kubernetes.io/restartedAt\":\"%s\"}}}}}";
    private final SharedIndexInformer<V1ConfigMap> configMapIndexInformer;
    private final Lister<V1SpringCloudGateway> gatewayLister;
    private final Lister<V1ConfigMap> configMapLister;
    private final AppsV1Api appsV1Api;
    private final EventRecorder eventRecorder;
    private final PatchUtilsWrapper patchUtilsWrapper;

    public ConfigMapReconciler(SharedIndexInformer<V1ConfigMap> configMapIndexInformer, SharedIndexInformer<V1SpringCloudGateway> gatewaySharedIndexInformer, AppsV1Api appsV1Api, EventRecorder eventRecorder, PatchUtilsWrapper patchUtilsWrapper) {
        this.configMapIndexInformer = configMapIndexInformer;
        this.configMapLister = new Lister(configMapIndexInformer.getIndexer());
        this.gatewayLister = new Lister(gatewaySharedIndexInformer.getIndexer());
        this.appsV1Api = appsV1Api;
        this.eventRecorder = eventRecorder;
        this.patchUtilsWrapper = patchUtilsWrapper;
    }

    public boolean hasSynced() {
        return this.configMapIndexInformer.hasSynced();
    }

    public boolean onAddFilter(V1ConfigMap configMap) {
        return false;
    }

    public boolean onUpdateFilter(V1ConfigMap oldConfigMap, V1ConfigMap newConfigMap) {
        return oldConfigMap.getBinaryData() != null && newConfigMap.getBinaryData() != null && !oldConfigMap.getBinaryData().equals(newConfigMap.getBinaryData());
    }

    public boolean onDeleteFilter(V1ConfigMap configMap, boolean deletedFinalStateUnknown) {
        return false;
    }

    public Result reconcile(Request request) {
        String configMapName = request.getName();
        String namespace = request.getNamespace();
        V1ConfigMap configMap = (V1ConfigMap)this.configMapLister.namespace(namespace).get(configMapName);
        return configMap == null ? new Result(false) : this.reconcileCustomExtensions(configMap, namespace);
    }

    private Result reconcileCustomExtensions(V1ConfigMap configMap, String namespace) {
        List<V1SpringCloudGateway> gatewaysWithExtensions = (List)this.gatewayLister.namespace(namespace).list().stream().filter(this.hasExtension(configMap.getMetadata().getName())).collect(Collectors.toList());
        Iterator var4 = gatewaysWithExtensions.iterator();

        while(var4.hasNext()) {
            V1SpringCloudGateway gateway = (V1SpringCloudGateway)var4.next();

            try {
                this.restartGatewayStatefulSet(namespace, gateway);
            } catch (ApiException var7) {
                this.logFailureEvent(gateway, configMap, var7);
                return new Result(true);
            }
        }

        return new Result(false);
    }

    private void restartGatewayStatefulSet(String namespace, V1SpringCloudGateway gateway) throws ApiException {
        String timestamp = ZonedDateTime.now().format(DATE_TIME_FORMATTER);
        String jsonPatch = String.format("{\"spec\":{\"template\":{\"metadata\":{\"annotations\":{\"kubectl.kubernetes.io/restartedAt\":\"%s\"}}}}}", timestamp);
        V1Patch patch = new V1Patch(jsonPatch);
        this.patchUtilsWrapper.patch(V1StatefulSet.class, () -> {
            return this.appsV1Api.patchNamespacedStatefulSetCall(gateway.getMetadata().getName(), namespace, patch, (String)null, (String)null, (String)null, (Boolean)null, (ApiCallback)null);
        }, "application/strategic-merge-patch+json");
    }

    private void logFailureEvent(V1SpringCloudGateway gateway, V1ConfigMap configMap, Exception e) {
        String message = String.format("Failed to patch gateway %s due to a change in configMap %s", gateway.getMetadata().getName(), configMap.getMetadata().getName());
        LOG.error(message, e);
        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(gateway), ObjectReferenceConverter.toObjectReference(configMap), "GatewayRestartException", message + ": " + e.getMessage(), EventType.Warning);
    }

    private Predicate<V1SpringCloudGateway> hasExtension(String extensionName) {
        return (scg) -> {
            return scg.getSpec() != null && scg.getSpec().getExtensions() != null && scg.getSpec().getExtensions().getCustom() != null && scg.getSpec().getExtensions().getCustom().contains(extensionName);
        };
    }
}

