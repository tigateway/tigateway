package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Log information model
 */
public class LogInfo {
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("level")
    private String level;
    
    @JsonProperty("service")
    private String service;
    
    @JsonProperty("namespace")
    private String namespace;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("logger")
    private String logger;
    
    @JsonProperty("thread")
    private String thread;
    
    @JsonProperty("exception")
    private String exception;
    
    @JsonProperty("context")
    private java.util.Map<String, Object> context;
    
    public LogInfo() {}
    
    public LogInfo(String timestamp, String level, String service, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.service = service;
        this.message = message;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
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
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getLogger() {
        return logger;
    }
    
    public void setLogger(String logger) {
        this.logger = logger;
    }
    
    public String getThread() {
        return thread;
    }
    
    public void setThread(String thread) {
        this.thread = thread;
    }
    
    public String getException() {
        return exception;
    }
    
    public void setException(String exception) {
        this.exception = exception;
    }
    
    public java.util.Map<String, Object> getContext() {
        return context;
    }
    
    public void setContext(java.util.Map<String, Object> context) {
        this.context = context;
    }
}
