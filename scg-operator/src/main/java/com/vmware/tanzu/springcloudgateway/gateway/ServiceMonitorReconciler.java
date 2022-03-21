package com.vmware.tanzu.springcloudgateway.gateway;

import com.coreos.monitoring.models.V1ServiceMonitor;
import com.vmware.tanzu.springcloudgateway.apis.EventRecorder;
import com.vmware.tanzu.springcloudgateway.apis.ObjectReferenceConverter;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.extended.event.EventType;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceMonitorReconciler {
    private final CustomObjectsApi customObjectsApi;
    private final PrometheusServiceMonitorBuilder serviceMonitorBuilder;
    private final Lister<V1ServiceMonitor> serviceMonitorLister;
    private final EventRecorder eventRecorder;
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMonitorReconciler.class);

    public ServiceMonitorReconciler(CustomObjectsApi customObjectsApi, PrometheusServiceMonitorBuilder serviceMonitorBuilder, SharedIndexInformer<V1ServiceMonitor> serviceMonitorSharedIndexInformer, EventRecorder eventRecorder) {
        this.customObjectsApi = customObjectsApi;
        this.serviceMonitorBuilder = serviceMonitorBuilder;
        this.serviceMonitorLister = new Lister(serviceMonitorSharedIndexInformer.getIndexer());
        this.eventRecorder = eventRecorder;
    }

    void reconcile(V1SpringCloudGateway desiredGateway, String gatewayNamespace, V1OwnerReference ownerReference) throws ApiException {
        if (this.serviceMonitorBuilder.isServiceMonitorEnabled(desiredGateway)) {
            V1ServiceMonitor existingServiceMonitor = null;

            try {
                existingServiceMonitor = (V1ServiceMonitor)this.serviceMonitorLister.namespace(gatewayNamespace).get(this.serviceMonitorBuilder.getServiceMonitorName(desiredGateway));
            } catch (Exception var9) {
                String message = String.format("Failed to retrieve the existing ServiceMonitor due to an Exception: %s", var9.getMessage());
                LOGGER.warn(message);
                this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(desiredGateway), ObjectReferenceConverter.toObjectReference(existingServiceMonitor), "Exception", message, EventType.Warning);
            }

            if (existingServiceMonitor == null) {
                V1ServiceMonitor desiredServiceMonitor = this.serviceMonitorBuilder.createServiceMonitor(gatewayNamespace, desiredGateway, ownerReference);

                try {
                    this.createNewServiceMonitor(gatewayNamespace, desiredServiceMonitor);
                } catch (ApiException var8) {
                    String message = String.format("Failed to create a ServiceMonitor due to an ApiException: %s %s", var8.getCode(), var8.getMessage());
                    LOGGER.warn(message);
                    this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(desiredGateway), ObjectReferenceConverter.toObjectReference(desiredServiceMonitor), "ApiException", message, EventType.Warning);
                }
            }

        }
    }

    private void createNewServiceMonitor(String gatewayNamespace, V1ServiceMonitor serviceMonitor) throws ApiException {
        this.customObjectsApi.createNamespacedCustomObject("monitoring.coreos.com", "v1", gatewayNamespace, "servicemonitors", serviceMonitor, (String)null, (String)null, (String)null);
    }
}
