package com.vmware.tanzu.springcloudgateway.gateway;

import com.vmware.tanzu.springcloudgateway.apis.TanzuVmwareComV1Api;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.PatchUtils;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayStatusEditor {
    private final TanzuVmwareComV1Api springCloudGatewayApi;
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayStatusEditor.class);

    public GatewayStatusEditor(TanzuVmwareComV1Api springCloudGatewayApi) {
        this.springCloudGatewayApi = springCloudGatewayApi;
    }

    public void setGatewayStatus(V1SpringCloudGateway desiredGateway, String type, String status, String reason) {
        String patch = String.format("{\"status\": { \"conditions\": [{ \"type\": \"%s\", \"status\": \"%s\", \"lastTransitionTime\": \"%s\", \"reason\": \"%s\"}]}}", type, status, ZonedDateTime.now(ZoneOffset.UTC).toString(), reason);

        try {
            PatchUtils.patch(V1SpringCloudGateway.class, () -> {
                return this.springCloudGatewayApi.patchNamespacedSpringCloudGatewayStatusCall(desiredGateway.getMetadata().getName(), desiredGateway.getMetadata().getNamespace(), new V1Patch(patch), (String)null, (String)null, (String)null, (ApiCallback)null);
            }, "application/merge-patch+json", this.springCloudGatewayApi.getApiClient());
        } catch (ApiException var7) {
            LOGGER.error("Status API call failed: {}: {}, {}, with patch {}", new Object[]{var7.getCode(), var7.getMessage(), var7.getResponseBody(), patch});
        }

    }
}

