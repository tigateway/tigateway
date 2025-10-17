package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration information model
 */
public class ConfigInfo {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("namespace")
    private String namespace;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("lastModified")
    private String lastModified;
    
    @JsonProperty("properties")
    private java.util.Map<String, Object> properties;
    
    @JsonProperty("metadata")
    private java.util.Map<String, String> metadata;
    
    @JsonProperty("status")
    private String status;
    
    public ConfigInfo() {}
    
    public ConfigInfo(String name, String namespace, String type, String source) {
        this.name = name;
        this.namespace = namespace;
        this.type = type;
        this.source = source;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
    
    public java.util.Map<String, Object> getProperties() {
        return properties;
    }
    
    public void setProperties(java.util.Map<String, Object> properties) {
        this.properties = properties;
    }
    
    public java.util.Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(java.util.Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
