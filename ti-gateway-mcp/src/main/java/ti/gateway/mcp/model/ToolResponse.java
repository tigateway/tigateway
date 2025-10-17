package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic tool response model
 */
public class ToolResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private Object data;
    
    @JsonProperty("error")
    private String error;
    
    public ToolResponse() {}
    
    public ToolResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public ToolResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static ToolResponse success(String message) {
        return new ToolResponse(true, message);
    }
    
    public static ToolResponse success(String message, Object data) {
        return new ToolResponse(true, message, data);
    }
    
    public static ToolResponse error(String message) {
        return new ToolResponse(false, message);
    }
    
    public static ToolResponse error(String message, String error) {
        ToolResponse response = new ToolResponse(false, message);
        response.setError(error);
        return response;
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
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
}
