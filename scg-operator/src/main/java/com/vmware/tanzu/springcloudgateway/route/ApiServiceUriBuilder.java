package com.vmware.tanzu.springcloudgateway.route;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ApiServiceUriBuilder {
    private final Logger LOG = LoggerFactory.getLogger(ApiServiceUriBuilder.class);
    private final CoreV1Api coreV1Api;

    public ApiServiceUriBuilder(CoreV1Api coreV1Api) {
        this.coreV1Api = coreV1Api;
    }

    public String build(String namespace, String serviceName) throws UriBuildingException {
        try {
            V1Service service = this.coreV1Api.readNamespacedService(serviceName, namespace, (String)null, (Boolean)null, (Boolean)null);
            return this.formatUri(namespace, serviceName, this.getPortOrElse(service, 80));
        } catch (ApiException var4) {
            this.LOG.error("Failed to build service URL: " + var4.getResponseBody(), var4);
            throw new UriBuildingException("Service " + serviceName + " not found", var4);
        }
    }

    private Integer getPortOrElse(V1Service service, Integer defaultValue) {
        return service.getSpec().getPorts() == null ? defaultValue : ((V1ServicePort)service.getSpec().getPorts().get(0)).getPort();
    }

    public String build(String namespace, String serviceName, Integer port) throws UriBuildingException {
        return port == null ? this.build(namespace, serviceName) : this.formatUri(namespace, serviceName, port);
    }

    private String formatUri(String namespace, String serviceName, Integer port) {
        return "http://" + serviceName + "." + namespace + ":" + port;
    }
}

