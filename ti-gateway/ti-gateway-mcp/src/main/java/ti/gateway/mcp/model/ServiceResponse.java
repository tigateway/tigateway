package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Service operation response model
 */
public class ServiceResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private List<ServiceInfo> data;
    
    @JsonProperty("count")
    private Integer count;
    
    @JsonProperty("namespace")
    private String namespace;
    
    @JsonProperty("health")
    private java.util.Map<String, Object> health;
    
    public ServiceResponse() {}
    
    public ServiceResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public static ServiceResponse listSuccess(List<ServiceInfo> services, String namespace) {
        ServiceResponse response = new ServiceResponse(true, "Services retrieved successfully");
        response.setData(services);
        response.setCount(services.size());
        response.setNamespace(namespace);
        return response;
    }
    
    public static ServiceResponse healthSuccess(java.util.Map<String, Object> health) {
        ServiceResponse response = new ServiceResponse(true, "Service health retrieved successfully");
        response.setHealth(health);
        return response;
    }
    
    public static ServiceResponse error(String message) {
        return new ServiceResponse(false, message);
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
    
    public List<ServiceInfo> getData() {
        return data;
    }
    
    public void setData(List<ServiceInfo> data) {
        this.data = data;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public java.util.Map<String, Object> getHealth() {
        return health;
    }
    
    public void setHealth(java.util.Map<String, Object> health) {
        this.health = health;
    }
}
