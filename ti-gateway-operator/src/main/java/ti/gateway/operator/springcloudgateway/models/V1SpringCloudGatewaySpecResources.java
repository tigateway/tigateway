package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecResources {
    public static final String SERIALIZED_NAME_LIMITS = "limits";
    @SerializedName("limits")
    private V1SpringCloudGatewaySpecResourcesLimits limits;
    public static final String SERIALIZED_NAME_REQUESTS = "requests";
    @SerializedName("requests")
    private V1SpringCloudGatewaySpecResourcesLimits requests;

    public V1SpringCloudGatewaySpecResources() {
    }

    public V1SpringCloudGatewaySpecResources limits(V1SpringCloudGatewaySpecResourcesLimits limits) {
        this.limits = limits;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecResourcesLimits getLimits() {
        return this.limits;
    }

    public void setLimits(V1SpringCloudGatewaySpecResourcesLimits limits) {
        this.limits = limits;
    }

    public V1SpringCloudGatewaySpecResources requests(V1SpringCloudGatewaySpecResourcesLimits requests) {
        this.requests = requests;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecResourcesLimits getRequests() {
        return this.requests;
    }

    public void setRequests(V1SpringCloudGatewaySpecResourcesLimits requests) {
        this.requests = requests;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecResources v1SpringCloudGatewaySpecResources = (V1SpringCloudGatewaySpecResources)o;
            return Objects.equals(this.limits, v1SpringCloudGatewaySpecResources.limits) && Objects.equals(this.requests, v1SpringCloudGatewaySpecResources.requests);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.limits, this.requests});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecResources {\n");
        sb.append("    limits: ").append(this.toIndentedString(this.limits)).append("\n");
        sb.append("    requests: ").append(this.toIndentedString(this.requests)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
