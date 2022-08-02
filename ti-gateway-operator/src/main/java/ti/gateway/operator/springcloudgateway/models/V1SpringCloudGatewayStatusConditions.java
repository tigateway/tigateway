package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewayStatusConditions {
    public static final String SERIALIZED_NAME_LAST_TRANSITION_TIME = "lastTransitionTime";
    @SerializedName("lastTransitionTime")
    private String lastTransitionTime;
    public static final String SERIALIZED_NAME_REASON = "reason";
    @SerializedName("reason")
    private String reason;
    public static final String SERIALIZED_NAME_STATUS = "status";
    @SerializedName("status")
    private String status;
    public static final String SERIALIZED_NAME_TYPE = "type";
    @SerializedName("type")
    private String type;

    public V1SpringCloudGatewayStatusConditions() {
    }

    public V1SpringCloudGatewayStatusConditions lastTransitionTime(String lastTransitionTime) {
        this.lastTransitionTime = lastTransitionTime;
        return this;
    }

    @Nullable
    @ApiModelProperty("Last time the condition of a type changed from one status to another. The required format is 'yyyy-MM-ddTHH:mm:ssZ', in the UTC time zone.")
    public String getLastTransitionTime() {
        return this.lastTransitionTime;
    }

    public void setLastTransitionTime(String lastTransitionTime) {
        this.lastTransitionTime = lastTransitionTime;
    }

    public V1SpringCloudGatewayStatusConditions reason(String reason) {
        this.reason = reason;
        return this;
    }

    @Nullable
    @ApiModelProperty("The reason for the condition's last transition (a single word in CamelCase).")
    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public V1SpringCloudGatewayStatusConditions status(String status) {
        this.status = status;
        return this;
    }

    @Nullable
    @ApiModelProperty("The status of the condition, either True, False or Unknown.")
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public V1SpringCloudGatewayStatusConditions type(String type) {
        this.type = type;
        return this;
    }

    @Nullable
    @ApiModelProperty("The unique identifier of a condition, used to distinguish between other conditions in the resource.")
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewayStatusConditions v1SpringCloudGatewayStatusConditions = (V1SpringCloudGatewayStatusConditions)o;
            return Objects.equals(this.lastTransitionTime, v1SpringCloudGatewayStatusConditions.lastTransitionTime) && Objects.equals(this.reason, v1SpringCloudGatewayStatusConditions.reason) && Objects.equals(this.status, v1SpringCloudGatewayStatusConditions.status) && Objects.equals(this.type, v1SpringCloudGatewayStatusConditions.type);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.lastTransitionTime, this.reason, this.status, this.type});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewayStatusConditions {\n");
        sb.append("    lastTransitionTime: ").append(this.toIndentedString(this.lastTransitionTime)).append("\n");
        sb.append("    reason: ").append(this.toIndentedString(this.reason)).append("\n");
        sb.append("    status: ").append(this.toIndentedString(this.status)).append("\n");
        sb.append("    type: ").append(this.toIndentedString(this.type)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

