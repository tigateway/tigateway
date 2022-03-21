package com.vmware.tanzu.springcloudgateway.gateway;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.PatchUtils;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PodStatusEditor {
    private final CoreV1Api coreV1Api;
    private static final Logger LOG = LoggerFactory.getLogger(PodStatusEditor.class);
    public static final String POD_CUSTOM_READINESS_GATE = "RoutesUpToDate";

    public PodStatusEditor(CoreV1Api coreV1Api) {
        this.coreV1Api = coreV1Api;
    }

    public void setRoutesUpToDateCondition(V1Pod pod, boolean status) {
        char var10000 = Character.toUpperCase(String.valueOf(status).charAt(0));
        String statusString = var10000 + String.valueOf(status).substring(1);
        LOG.info("Setting Pod {} status to Ready={}", pod.getMetadata().getName(), statusString);
        V1Patch statusPatch = new V1Patch("{\"status\": { \"conditions\": [{ \"type\": \"RoutesUpToDate\", \"status\": \"" + statusString + "\", \"lastTransitionTime\": \"" + OffsetDateTime.now().toString() + "\"}]}}");

        try {
            PatchUtils.patch(V1Pod.class, () -> {
                return this.coreV1Api.patchNamespacedPodStatusCall(pod.getMetadata().getName(), pod.getMetadata().getNamespace(), statusPatch, (String)null, (String)null, (String)null, (Boolean)null, (ApiCallback)null);
            }, "application/merge-patch+json", this.coreV1Api.getApiClient());
        } catch (ApiException var6) {
            LOG.error("Failed to update pod {} RoutesUpToDate status", pod.getMetadata().getName());
        }

    }
}
