package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewayRouteConfig implements KubernetesObject {
    public static final String SERIALIZED_NAME_API_VERSION = "apiVersion";
    @SerializedName("apiVersion")
    private String apiVersion;
    public static final String SERIALIZED_NAME_KIND = "kind";
    @SerializedName("kind")
    private String kind;
    public static final String SERIALIZED_NAME_METADATA = "metadata";
    @SerializedName("metadata")
    private V1ObjectMeta metadata = null;
    public static final String SERIALIZED_NAME_SPEC = "spec";
    @SerializedName("spec")
    private V1SpringCloudGatewayRouteConfigSpec spec;

    public V1SpringCloudGatewayRouteConfig() {
    }

    public V1SpringCloudGatewayRouteConfig apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Nullable
    @ApiModelProperty("APIVersion defines the versioned schema of this representation of an object. Servers should convert recognized schemas to the latest internal value, and may reject unrecognized values. More info: https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#resources")
    public String getApiVersion() {
        return this.apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public V1SpringCloudGatewayRouteConfig kind(String kind) {
        this.kind = kind;
        return this;
    }

    @Nullable
    @ApiModelProperty("Kind is a string value representing the REST resource this object represents. Servers may infer this from the endpoint the client submits requests to. Cannot be updated. In CamelCase. More info: https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#types-kinds")
    public String getKind() {
        return this.kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public V1SpringCloudGatewayRouteConfig metadata(V1ObjectMeta metadata) {
        this.metadata = metadata;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1ObjectMeta getMetadata() {
        return this.metadata;
    }

    public void setMetadata(V1ObjectMeta metadata) {
        this.metadata = metadata;
    }

    public V1SpringCloudGatewayRouteConfig spec(V1SpringCloudGatewayRouteConfigSpec spec) {
        this.spec = spec;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewayRouteConfigSpec getSpec() {
        return this.spec;
    }

    public void setSpec(V1SpringCloudGatewayRouteConfigSpec spec) {
        this.spec = spec;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewayRouteConfig v1SpringCloudGatewayRouteConfig = (V1SpringCloudGatewayRouteConfig)o;
            return Objects.equals(this.apiVersion, v1SpringCloudGatewayRouteConfig.apiVersion) && Objects.equals(this.kind, v1SpringCloudGatewayRouteConfig.kind) && Objects.equals(this.metadata, v1SpringCloudGatewayRouteConfig.metadata) && Objects.equals(this.spec, v1SpringCloudGatewayRouteConfig.spec);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.apiVersion, this.kind, this.metadata, this.spec});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewayRouteConfig {\n");
        sb.append("    apiVersion: ").append(this.toIndentedString(this.apiVersion)).append("\n");
        sb.append("    kind: ").append(this.toIndentedString(this.kind)).append("\n");
        sb.append("    metadata: ").append(this.toIndentedString(this.metadata)).append("\n");
        sb.append("    spec: ").append(this.toIndentedString(this.spec)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
