package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecObservabilityMetricsPrometheusAnnotations {
    public static final String SERIALIZED_NAME_ENABLED = "enabled";
    @SerializedName("enabled")
    private Boolean enabled;

    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheusAnnotations() {
    }

    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheusAnnotations enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Nullable
    @ApiModelProperty("If scrapping annotations should be included in the Pod ")
    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecObservabilityMetricsPrometheusAnnotations v1SpringCloudGatewaySpecObservabilityMetricsPrometheusAnnotations = (V1SpringCloudGatewaySpecObservabilityMetricsPrometheusAnnotations)o;
            return Objects.equals(this.enabled, v1SpringCloudGatewaySpecObservabilityMetricsPrometheusAnnotations.enabled);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.enabled});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecObservabilityMetricsPrometheusAnnotations {\n");
        sb.append("    enabled: ").append(this.toIndentedString(this.enabled)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
