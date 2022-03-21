package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecObservabilityMetricsPrometheus {
    public static final String SERIALIZED_NAME_ANNOTATIONS = "annotations";
    @SerializedName("annotations")
    private V1SpringCloudGatewaySpecObservabilityMetricsPrometheusAnnotations annotations;
    public static final String SERIALIZED_NAME_ENABLED = "enabled";
    @SerializedName("enabled")
    private Boolean enabled;
    public static final String SERIALIZED_NAME_SERVICE_MONITOR = "serviceMonitor";
    @SerializedName("serviceMonitor")
    private V1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor serviceMonitor;

    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheus() {
    }

    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheus annotations(V1SpringCloudGatewaySpecObservabilityMetricsPrometheusAnnotations annotations) {
        this.annotations = annotations;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheusAnnotations getAnnotations() {
        return this.annotations;
    }

    public void setAnnotations(V1SpringCloudGatewaySpecObservabilityMetricsPrometheusAnnotations annotations) {
        this.annotations = annotations;
    }

    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheus enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Nullable
    @ApiModelProperty("If a prometheus endpoint should be exposed ")
    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheus serviceMonitor(V1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor serviceMonitor) {
        this.serviceMonitor = serviceMonitor;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor getServiceMonitor() {
        return this.serviceMonitor;
    }

    public void setServiceMonitor(V1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor serviceMonitor) {
        this.serviceMonitor = serviceMonitor;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecObservabilityMetricsPrometheus v1SpringCloudGatewaySpecObservabilityMetricsPrometheus = (V1SpringCloudGatewaySpecObservabilityMetricsPrometheus)o;
            return Objects.equals(this.annotations, v1SpringCloudGatewaySpecObservabilityMetricsPrometheus.annotations) && Objects.equals(this.enabled, v1SpringCloudGatewaySpecObservabilityMetricsPrometheus.enabled) && Objects.equals(this.serviceMonitor, v1SpringCloudGatewaySpecObservabilityMetricsPrometheus.serviceMonitor);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.annotations, this.enabled, this.serviceMonitor});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecObservabilityMetricsPrometheus {\n");
        sb.append("    annotations: ").append(this.toIndentedString(this.annotations)).append("\n");
        sb.append("    enabled: ").append(this.toIndentedString(this.enabled)).append("\n");
        sb.append("    serviceMonitor: ").append(this.toIndentedString(this.serviceMonitor)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
