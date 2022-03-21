package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewayMappingSpec {
    public static final String SERIALIZED_NAME_GATEWAY_REF = "gatewayRef";
    @SerializedName("gatewayRef")
    private V1SpringCloudGatewayMappingSpecGatewayRef gatewayRef;
    public static final String SERIALIZED_NAME_ROUTE_CONFIG_REF = "routeConfigRef";
    @SerializedName("routeConfigRef")
    private V1SpringCloudGatewayMappingSpecGatewayRef routeConfigRef;

    public V1SpringCloudGatewayMappingSpec() {
    }

    public V1SpringCloudGatewayMappingSpec gatewayRef(V1SpringCloudGatewayMappingSpecGatewayRef gatewayRef) {
        this.gatewayRef = gatewayRef;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewayMappingSpecGatewayRef getGatewayRef() {
        return this.gatewayRef;
    }

    public void setGatewayRef(V1SpringCloudGatewayMappingSpecGatewayRef gatewayRef) {
        this.gatewayRef = gatewayRef;
    }

    public V1SpringCloudGatewayMappingSpec routeConfigRef(V1SpringCloudGatewayMappingSpecGatewayRef routeConfigRef) {
        this.routeConfigRef = routeConfigRef;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewayMappingSpecGatewayRef getRouteConfigRef() {
        return this.routeConfigRef;
    }

    public void setRouteConfigRef(V1SpringCloudGatewayMappingSpecGatewayRef routeConfigRef) {
        this.routeConfigRef = routeConfigRef;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewayMappingSpec v1SpringCloudGatewayMappingSpec = (V1SpringCloudGatewayMappingSpec)o;
            return Objects.equals(this.gatewayRef, v1SpringCloudGatewayMappingSpec.gatewayRef) && Objects.equals(this.routeConfigRef, v1SpringCloudGatewayMappingSpec.routeConfigRef);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.gatewayRef, this.routeConfigRef});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewayMappingSpec {\n");
        sb.append("    gatewayRef: ").append(this.toIndentedString(this.gatewayRef)).append("\n");
        sb.append("    routeConfigRef: ").append(this.toIndentedString(this.routeConfigRef)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
