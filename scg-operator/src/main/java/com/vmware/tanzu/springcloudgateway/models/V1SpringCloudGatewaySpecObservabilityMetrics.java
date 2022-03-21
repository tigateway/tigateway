package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecObservabilityMetrics {
    public static final String SERIALIZED_NAME_PROMETHEUS = "prometheus";
    @SerializedName("prometheus")
    private V1SpringCloudGatewaySpecObservabilityMetricsPrometheus prometheus;
    public static final String SERIALIZED_NAME_WAVEFRONT = "wavefront";
    @SerializedName("wavefront")
    private V1SpringCloudGatewaySpecObservabilityMetricsWavefront wavefront;

    public V1SpringCloudGatewaySpecObservabilityMetrics() {
    }

    public V1SpringCloudGatewaySpecObservabilityMetrics prometheus(V1SpringCloudGatewaySpecObservabilityMetricsPrometheus prometheus) {
        this.prometheus = prometheus;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheus getPrometheus() {
        return this.prometheus;
    }

    public void setPrometheus(V1SpringCloudGatewaySpecObservabilityMetricsPrometheus prometheus) {
        this.prometheus = prometheus;
    }

    public V1SpringCloudGatewaySpecObservabilityMetrics wavefront(V1SpringCloudGatewaySpecObservabilityMetricsWavefront wavefront) {
        this.wavefront = wavefront;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecObservabilityMetricsWavefront getWavefront() {
        return this.wavefront;
    }

    public void setWavefront(V1SpringCloudGatewaySpecObservabilityMetricsWavefront wavefront) {
        this.wavefront = wavefront;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecObservabilityMetrics v1SpringCloudGatewaySpecObservabilityMetrics = (V1SpringCloudGatewaySpecObservabilityMetrics)o;
            return Objects.equals(this.prometheus, v1SpringCloudGatewaySpecObservabilityMetrics.prometheus) && Objects.equals(this.wavefront, v1SpringCloudGatewaySpecObservabilityMetrics.wavefront);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.prometheus, this.wavefront});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecObservabilityMetrics {\n");
        sb.append("    prometheus: ").append(this.toIndentedString(this.prometheus)).append("\n");
        sb.append("    wavefront: ").append(this.toIndentedString(this.wavefront)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

