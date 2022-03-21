package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecObservability {
    public static final String SERIALIZED_NAME_METRICS = "metrics";
    @SerializedName("metrics")
    private V1SpringCloudGatewaySpecObservabilityMetrics metrics;
    public static final String SERIALIZED_NAME_TRACING = "tracing";
    @SerializedName("tracing")
    private V1SpringCloudGatewaySpecObservabilityTracing tracing;
    public static final String SERIALIZED_NAME_WAVEFRONT = "wavefront";
    @SerializedName("wavefront")
    private V1SpringCloudGatewaySpecObservabilityWavefront wavefront;

    public V1SpringCloudGatewaySpecObservability() {
    }

    public V1SpringCloudGatewaySpecObservability metrics(V1SpringCloudGatewaySpecObservabilityMetrics metrics) {
        this.metrics = metrics;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecObservabilityMetrics getMetrics() {
        return this.metrics;
    }

    public void setMetrics(V1SpringCloudGatewaySpecObservabilityMetrics metrics) {
        this.metrics = metrics;
    }

    public V1SpringCloudGatewaySpecObservability tracing(V1SpringCloudGatewaySpecObservabilityTracing tracing) {
        this.tracing = tracing;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecObservabilityTracing getTracing() {
        return this.tracing;
    }

    public void setTracing(V1SpringCloudGatewaySpecObservabilityTracing tracing) {
        this.tracing = tracing;
    }

    public V1SpringCloudGatewaySpecObservability wavefront(V1SpringCloudGatewaySpecObservabilityWavefront wavefront) {
        this.wavefront = wavefront;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecObservabilityWavefront getWavefront() {
        return this.wavefront;
    }

    public void setWavefront(V1SpringCloudGatewaySpecObservabilityWavefront wavefront) {
        this.wavefront = wavefront;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecObservability v1SpringCloudGatewaySpecObservability = (V1SpringCloudGatewaySpecObservability)o;
            return Objects.equals(this.metrics, v1SpringCloudGatewaySpecObservability.metrics) && Objects.equals(this.tracing, v1SpringCloudGatewaySpecObservability.tracing) && Objects.equals(this.wavefront, v1SpringCloudGatewaySpecObservability.wavefront);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.metrics, this.tracing, this.wavefront});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecObservability {\n");
        sb.append("    metrics: ").append(this.toIndentedString(this.metrics)).append("\n");
        sb.append("    tracing: ").append(this.toIndentedString(this.tracing)).append("\n");
        sb.append("    wavefront: ").append(this.toIndentedString(this.wavefront)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

