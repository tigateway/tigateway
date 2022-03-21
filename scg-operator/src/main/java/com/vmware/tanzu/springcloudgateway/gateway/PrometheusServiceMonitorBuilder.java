package com.vmware.tanzu.springcloudgateway.gateway;

import com.coreos.monitoring.models.V1ServiceMonitor;
import com.coreos.monitoring.models.V1ServiceMonitorSpec;
import com.coreos.monitoring.models.V1ServiceMonitorSpecEndpoints;
import com.coreos.monitoring.models.V1ServiceMonitorSpecSelector;
import com.vmware.tanzu.springcloudgateway.apis.LabelsBuilder;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusServiceMonitorBuilder {
    public static final String RESOURCE_GROUP = "monitoring.coreos.com";
    public static final String RESOURCE_VERSION = "v1";
    public static final String RESOURCE_KIND = "ServiceMonitor";
    public static final String RESOURCE_PLURAL = "servicemonitors";
    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusServiceMonitorBuilder.class);
    private static final String API_VERSION = "monitoring.coreos.com/v1";
    private static final String APP_LABEL = "app";
    private static final String TCP_GATEWAY_ACTUATOR_PORT_NAME = "tcp-gateway-actuator";
    private static final String PROMETHEUS_ACTUATOR_PATH = "/actuator/prometheus";
    private static final int TARGET_PORT = 8090;
    private final MetricsParameters metricsParameters;

    public PrometheusServiceMonitorBuilder(MetricsParameters metricsParameters) {
        this.metricsParameters = metricsParameters;
    }

    boolean isServiceMonitorEnabled(V1SpringCloudGateway gateway) {
        return this.metricsParameters.isServiceMonitorEnabled(gateway);
    }

    V1ServiceMonitor createServiceMonitor(String namespace, V1SpringCloudGateway gateway, V1OwnerReference ownerReference) {
        if (this.isServiceMonitorEnabled(gateway)) {
            String gatewayName = gateway.getMetadata().getName();
            return (new V1ServiceMonitor()).apiVersion("monitoring.coreos.com/v1").kind("ServiceMonitor").metadata((new V1ObjectMeta()).name(this.getServiceMonitorName(gateway)).namespace(namespace).labels(this.buildLabels(gateway, gatewayName)).addOwnerReferencesItem(ownerReference)).spec((new V1ServiceMonitorSpec()).selector((new V1ServiceMonitorSpecSelector()).matchLabels(Map.of("app", gatewayName))).endpoints(List.of((new V1ServiceMonitorSpecEndpoints()).port("tcp-gateway-actuator").targetPort(8090).path("/actuator/prometheus"))));
        } else {
            LOGGER.debug("ServiceMonitor flag is disabled. ServiceMonitor will not be created.");
            return null;
        }
    }

    private HashMap<String, String> buildLabels(V1SpringCloudGateway gateway, String gatewayName) {
        HashMap<String, String> labels = new HashMap(this.metricsParameters.getServiceMonitorLabels(gateway));
        labels.putAll(LabelsBuilder.build(gatewayName));
        return labels;
    }

    String getServiceMonitorName(V1SpringCloudGateway gateway) {
        return gateway != null && gateway.getMetadata() != null && gateway.getMetadata().getName() != null ? String.format("%s-service-monitor", gateway.getMetadata().getName()) : null;
    }
}
