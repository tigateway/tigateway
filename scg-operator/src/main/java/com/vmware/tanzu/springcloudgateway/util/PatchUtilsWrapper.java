package com.vmware.tanzu.springcloudgateway.util;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.PatchUtils;
import io.kubernetes.client.util.PatchUtils.PatchCallFunc;
import org.springframework.stereotype.Component;

@Component
public class PatchUtilsWrapper {
    public PatchUtilsWrapper() {
    }

    public <ApiType> ApiType patch(Class<ApiType> apiTypeClass, PatchCallFunc callFunc, String patchFormat) throws ApiException {
        return PatchUtils.patch(apiTypeClass, callFunc, patchFormat);
    }

    public <ApiType> ApiType patch(Class<ApiType> apiTypeClass, PatchCallFunc callFunc, String patchFormat, ApiClient apiClient) throws ApiException {
        return PatchUtils.patch(apiTypeClass, callFunc, patchFormat, apiClient);
    }
}

