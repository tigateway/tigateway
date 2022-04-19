package com.vmware.tanzu.springcloudgateway.gateway;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class SecretLister {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecretLister.class);
    private final CoreV1Api coreV1Api;

    public SecretLister(CoreV1Api coreV1Api) {
        this.coreV1Api = coreV1Api;
    }

    public V1Secret getSecret(String gatewayNamespace, String secretName) {
        try {
            V1SecretList metricsSecretList = this.coreV1Api.listNamespacedSecret(gatewayNamespace, (String)null, (Boolean)null, (String)null, String.format("metadata.name=%s", secretName), (String)null, (Integer)null, (String)null, (String)null, 10, false);
            return (V1Secret)metricsSecretList.getItems().stream().findFirst().orElse(null);
        } catch (ApiException var4) {
            if (var4.getCode() == HttpStatus.NOT_FOUND.value()) {
                return null;
            } else {
                LOGGER.error("Failed to list Secret", var4);
                throw new RuntimeException("Failed to list Secret", var4);
            }
        }
    }
}

