package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metrics operation response model
 */
public class MetricsResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("metrics")
    private MetricsInfo metrics;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("timeRange")
    private String timeRange;
    
    @JsonProperty("namespace")
    private String namespace;
    
    public MetricsResponse() {}
    
    public MetricsResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public static MetricsResponse success(MetricsInfo metrics, String type, String timeRange, String namespace) {
        MetricsResponse response = new MetricsResponse(true, "Metrics retrieved successfully");
        response.setMetrics(metrics);
        response.setType(type);
        response.setTimeRange(timeRange);
        response.setNamespace(namespace);
        return response;
    }
    
    public static MetricsResponse error(String message) {
        return new MetricsResponse(false, message);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public MetricsInfo getMetrics() {
        return metrics;
    }
    
    public void setMetrics(MetricsInfo metrics) {
        this.metrics = metrics;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getTimeRange() {
        return timeRange;
    }
    
    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
