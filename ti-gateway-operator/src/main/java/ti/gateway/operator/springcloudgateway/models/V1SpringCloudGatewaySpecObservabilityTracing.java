package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecObservabilityTracing {
    public static final String SERIALIZED_NAME_WAVEFRONT = "wavefront";
    @SerializedName("wavefront")
    private V1SpringCloudGatewaySpecObservabilityTracingWavefront wavefront;

    public V1SpringCloudGatewaySpecObservabilityTracing() {
    }

    public V1SpringCloudGatewaySpecObservabilityTracing wavefront(V1SpringCloudGatewaySpecObservabilityTracingWavefront wavefront) {
        this.wavefront = wavefront;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecObservabilityTracingWavefront getWavefront() {
        return this.wavefront;
    }

    public void setWavefront(V1SpringCloudGatewaySpecObservabilityTracingWavefront wavefront) {
        this.wavefront = wavefront;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecObservabilityTracing v1SpringCloudGatewaySpecObservabilityTracing = (V1SpringCloudGatewaySpecObservabilityTracing)o;
            return Objects.equals(this.wavefront, v1SpringCloudGatewaySpecObservabilityTracing.wavefront);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.wavefront});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecObservabilityTracing {\n");
        sb.append("    wavefront: ").append(this.toIndentedString(this.wavefront)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

