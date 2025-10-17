package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration operation response model
 */
public class ConfigResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("config")
    private ConfigInfo config;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("format")
    private String format;
    
    @JsonProperty("namespace")
    private String namespace;
    
    public ConfigResponse() {}
    
    public ConfigResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public static ConfigResponse success(ConfigInfo config, String type, String format, String namespace) {
        ConfigResponse response = new ConfigResponse(true, "Configuration retrieved successfully");
        response.setConfig(config);
        response.setType(type);
        response.setFormat(format);
        response.setNamespace(namespace);
        return response;
    }
    
    public static ConfigResponse error(String message) {
        return new ConfigResponse(false, message);
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
    
    public ConfigInfo getConfig() {
        return config;
    }
    
    public void setConfig(ConfigInfo config) {
        this.config = config;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
