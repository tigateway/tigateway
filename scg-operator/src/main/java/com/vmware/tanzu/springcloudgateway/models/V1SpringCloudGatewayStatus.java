package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewayStatus {
    public static final String SERIALIZED_NAME_CONDITIONS = "conditions";
    @SerializedName("conditions")
    private List<V1SpringCloudGatewayStatusConditions> conditions = null;

    public V1SpringCloudGatewayStatus() {
    }

    public V1SpringCloudGatewayStatus conditions(List<V1SpringCloudGatewayStatusConditions> conditions) {
        this.conditions = conditions;
        return this;
    }

    public V1SpringCloudGatewayStatus addConditionsItem(V1SpringCloudGatewayStatusConditions conditionsItem) {
        if (this.conditions == null) {
            this.conditions = new ArrayList();
        }

        this.conditions.add(conditionsItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("List of status conditions.")
    public List<V1SpringCloudGatewayStatusConditions> getConditions() {
        return this.conditions;
    }

    public void setConditions(List<V1SpringCloudGatewayStatusConditions> conditions) {
        this.conditions = conditions;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewayStatus v1SpringCloudGatewayStatus = (V1SpringCloudGatewayStatus)o;
            return Objects.equals(this.conditions, v1SpringCloudGatewayStatus.conditions);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.conditions});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewayStatus {\n");
        sb.append("    conditions: ").append(this.toIndentedString(this.conditions)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

