package ti.gateway.operator.springcloudgateway.mapping;

import ti.gateway.operator.springcloudgateway.apis.TanzuVmwareComV1Api;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayMapping;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.PatchUtils;

public class MappingFinalizerEditor {
    private final TanzuVmwareComV1Api mappingV1Api;
    static final String MAPPING_FINALIZER_STRING = "finalizer.springcloudgatewaymappings.tanzu.vmware.com";

    public MappingFinalizerEditor(TanzuVmwareComV1Api mappingV1Api) {
        this.mappingV1Api = mappingV1Api;
    }

    public V1SpringCloudGatewayMapping add(V1SpringCloudGatewayMapping mappingToPatch) throws ApiException {
        return (V1SpringCloudGatewayMapping)PatchUtils.patch(V1SpringCloudGatewayMapping.class, () -> {
            return this.mappingV1Api.patchNamespacedSpringCloudGatewayMappingCall(mappingToPatch.getMetadata().getName(), mappingToPatch.getMetadata().getNamespace(), new V1Patch("{\"metadata\":{\"finalizers\":[\"finalizer.springcloudgatewaymappings.tanzu.vmware.com\"]}}"), (String)null, (String)null, (String)null, (ApiCallback)null);
        }, "application/merge-patch+json", this.mappingV1Api.getApiClient());
    }

    public V1SpringCloudGatewayMapping remove(V1SpringCloudGatewayMapping mappingToPatch) throws ApiException {
        return (V1SpringCloudGatewayMapping)PatchUtils.patch(V1SpringCloudGatewayMapping.class, () -> {
            return this.mappingV1Api.patchNamespacedSpringCloudGatewayMappingCall(mappingToPatch.getMetadata().getName(), mappingToPatch.getMetadata().getNamespace(), new V1Patch("[{\"op\": \"remove\", \"path\": \"/metadata/finalizers\"}]"), (String)null, (String)null, (String)null, (ApiCallback)null);
        }, "application/json-patch+json", this.mappingV1Api.getApiClient());
    }
}

