package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewayRouteConfigSpecBasicAuth {
    public static final String SERIALIZED_NAME_SECRET = "secret";
    @SerializedName("secret")
    private String secret;

    public V1SpringCloudGatewayRouteConfigSpecBasicAuth() {
    }

    public V1SpringCloudGatewayRouteConfigSpecBasicAuth secret(String secret) {
        this.secret = secret;
        return this;
    }

    @Nullable
    @ApiModelProperty("The secret name containing basic auth credentials. It should contain a username and password keys.")
    public String getSecret() {
        return this.secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewayRouteConfigSpecBasicAuth v1SpringCloudGatewayRouteConfigSpecBasicAuth = (V1SpringCloudGatewayRouteConfigSpecBasicAuth)o;
            return Objects.equals(this.secret, v1SpringCloudGatewayRouteConfigSpecBasicAuth.secret);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.secret});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewayRouteConfigSpecBasicAuth {\n");
        sb.append("    secret: ").append(this.toIndentedString(this.secret)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
