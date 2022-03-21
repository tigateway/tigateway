package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewayRouteConfigSpecModel {
    public static final String SERIALIZED_NAME_REQUEST_BODY = "requestBody";
    @SerializedName("requestBody")
    private Object requestBody;
    public static final String SERIALIZED_NAME_RESPONSES = "responses";
    @SerializedName("responses")
    private Object responses;

    public V1SpringCloudGatewayRouteConfigSpecModel() {
    }

    public V1SpringCloudGatewayRouteConfigSpecModel requestBody(Object requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    @Nullable
    @ApiModelProperty("The request body as specified by https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#requestBodyObject")
    public Object getRequestBody() {
        return this.requestBody;
    }

    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    public V1SpringCloudGatewayRouteConfigSpecModel responses(Object responses) {
        this.responses = responses;
        return this;
    }

    @Nullable
    @ApiModelProperty("The responses of an operation as specified by https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#responsesObject")
    public Object getResponses() {
        return this.responses;
    }

    public void setResponses(Object responses) {
        this.responses = responses;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewayRouteConfigSpecModel v1SpringCloudGatewayRouteConfigSpecModel = (V1SpringCloudGatewayRouteConfigSpecModel)o;
            return Objects.equals(this.requestBody, v1SpringCloudGatewayRouteConfigSpecModel.requestBody) && Objects.equals(this.responses, v1SpringCloudGatewayRouteConfigSpecModel.responses);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.requestBody, this.responses});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewayRouteConfigSpecModel {\n");
        sb.append("    requestBody: ").append(this.toIndentedString(this.requestBody)).append("\n");
        sb.append("    responses: ").append(this.toIndentedString(this.responses)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

