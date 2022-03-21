package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor {
    public static final String SERIALIZED_NAME_ENABLED = "enabled";
    @SerializedName("enabled")
    private Boolean enabled;
    public static final String SERIALIZED_NAME_LABELS = "labels";
    @SerializedName("labels")
    private Map<String, String> labels = null;

    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor() {
    }

    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Nullable
    @ApiModelProperty("If a Prometheus ServiceMonitor should be added ")
    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor labels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    public V1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor putLabelsItem(String key, String labelsItem) {
        if (this.labels == null) {
            this.labels = new HashMap();
        }

        this.labels.put(key, labelsItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("Labels to add to the service monitor, e.g. to be picked up by the Prometheus serviceMonitorSelector ")
    public Map<String, String> getLabels() {
        return this.labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor v1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor = (V1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor)o;
            return Objects.equals(this.enabled, v1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor.enabled) && Objects.equals(this.labels, v1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor.labels);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.enabled, this.labels});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecObservabilityMetricsPrometheusServiceMonitor {\n");
        sb.append("    enabled: ").append(this.toIndentedString(this.enabled)).append("\n");
        sb.append("    labels: ").append(this.toIndentedString(this.labels)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

