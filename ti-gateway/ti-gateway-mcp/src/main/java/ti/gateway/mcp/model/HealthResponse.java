package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Health check response model
 */
public class HealthResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("service")
    private String service;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    @JsonProperty("port")
    private Integer port;
    
    public HealthResponse() {}
    
    public HealthResponse(String status, String service, String version, long timestamp) {
        this.status = status;
        this.service = service;
        this.version = version;
        this.timestamp = timestamp;
    }
    
    public HealthResponse(String status, String service, String version, long timestamp, Integer port) {
        this.status = status;
        this.service = service;
        this.version = version;
        this.timestamp = timestamp;
        this.port = port;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getService() {
        return service;
    }
    
    public void setService(String service) {
        this.service = service;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
}
