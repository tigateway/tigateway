package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Service health status model
 */
public class ServiceHealth {
    
    @JsonProperty("service")
    private String service;
    
    @JsonProperty("namespace")
    private String namespace;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("uptime")
    private String uptime;
    
    @JsonProperty("lastCheck")
    private String lastCheck;
    
    @JsonProperty("endpoints")
    private List<ServiceEndpoint> endpoints;
    
    public ServiceHealth() {}
    
    public ServiceHealth(String service, String namespace, String status) {
        this.service = service;
        this.namespace = namespace;
        this.status = status;
    }
    
    public String getService() {
        return service;
    }
    
    public void setService(String service) {
        this.service = service;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getUptime() {
        return uptime;
    }
    
    public void setUptime(String uptime) {
        this.uptime = uptime;
    }
    
    public String getLastCheck() {
        return lastCheck;
    }
    
    public void setLastCheck(String lastCheck) {
        this.lastCheck = lastCheck;
    }
    
    public List<ServiceEndpoint> getEndpoints() {
        return endpoints;
    }
    
    public void setEndpoints(List<ServiceEndpoint> endpoints) {
        this.endpoints = endpoints;
    }
    
    /**
     * Service endpoint model
     */
    public static class ServiceEndpoint {
        
        @JsonProperty("address")
        private String address;
        
        @JsonProperty("status")
        private String status;
        
        public ServiceEndpoint() {}
        
        public ServiceEndpoint(String address, String status) {
            this.address = address;
            this.status = status;
        }
        
        public String getAddress() {
            return address;
        }
        
        public void setAddress(String address) {
            this.address = address;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
}
