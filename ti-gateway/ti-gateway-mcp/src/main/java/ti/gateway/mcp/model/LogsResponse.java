package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Logs operation response model
 */
public class LogsResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("logs")
    private List<LogInfo> logs;
    
    @JsonProperty("count")
    private Integer count;
    
    @JsonProperty("level")
    private String level;
    
    @JsonProperty("namespace")
    private String namespace;
    
    @JsonProperty("service")
    private String service;
    
    @JsonProperty("filter")
    private String filter;
    
    public LogsResponse() {}
    
    public LogsResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public static LogsResponse success(List<LogInfo> logs, String level, String namespace, String service, String filter) {
        LogsResponse response = new LogsResponse(true, "Logs retrieved successfully");
        response.setLogs(logs);
        response.setCount(logs.size());
        response.setLevel(level);
        response.setNamespace(namespace);
        response.setService(service);
        response.setFilter(filter);
        return response;
    }
    
    public static LogsResponse error(String message) {
        return new LogsResponse(false, message);
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
    
    public List<LogInfo> getLogs() {
        return logs;
    }
    
    public void setLogs(List<LogInfo> logs) {
        this.logs = logs;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getService() {
        return service;
    }
    
    public void setService(String service) {
        this.service = service;
    }
    
    public String getFilter() {
        return filter;
    }
    
    public void setFilter(String filter) {
        this.filter = filter;
    }
}
