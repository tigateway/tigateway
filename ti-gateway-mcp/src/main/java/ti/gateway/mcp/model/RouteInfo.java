package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Route information model
 */
public class RouteInfo {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("namespace")
    private String namespace;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("service")
    private String service;
    
    @JsonProperty("port")
    private Integer port;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("created")
    private String created;
    
    @JsonProperty("updated")
    private String updated;
    
    @JsonProperty("predicates")
    private java.util.List<String> predicates;
    
    @JsonProperty("filters")
    private java.util.List<String> filters;
    
    @JsonProperty("metadata")
    private java.util.Map<String, String> metadata;
    
    public RouteInfo() {}
    
    public RouteInfo(String name, String namespace, String path, String service, Integer port, String status) {
        this.name = name;
        this.namespace = namespace;
        this.path = path;
        this.service = service;
        this.port = port;
        this.status = status;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getService() {
        return service;
    }
    
    public void setService(String service) {
        this.service = service;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCreated() {
        return created;
    }
    
    public void setCreated(String created) {
        this.created = created;
    }
    
    public String getUpdated() {
        return updated;
    }
    
    public void setUpdated(String updated) {
        this.updated = updated;
    }
    
    public java.util.List<String> getPredicates() {
        return predicates;
    }
    
    public void setPredicates(java.util.List<String> predicates) {
        this.predicates = predicates;
    }
    
    public java.util.List<String> getFilters() {
        return filters;
    }
    
    public void setFilters(java.util.List<String> filters) {
        this.filters = filters;
    }
    
    public java.util.Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(java.util.Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
