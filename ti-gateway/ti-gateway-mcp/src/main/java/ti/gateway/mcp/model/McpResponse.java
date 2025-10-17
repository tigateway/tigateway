package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Base class for MCP responses
 */
public class McpResponse {
    
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("result")
    private Map<String, Object> result;
    
    @JsonProperty("error")
    private McpError error;
    
    public McpResponse() {}
    
    public McpResponse(String id) {
        this.id = id;
    }
    
    public McpResponse(String id, Map<String, Object> result) {
        this.id = id;
        this.result = result;
    }
    
    public McpResponse(String id, McpError error) {
        this.id = id;
        this.error = error;
    }
    
    public String getJsonrpc() {
        return jsonrpc;
    }
    
    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Map<String, Object> getResult() {
        return result;
    }
    
    public void setResult(Map<String, Object> result) {
        this.result = result;
    }
    
    public McpError getError() {
        return error;
    }
    
    public void setError(McpError error) {
        this.error = error;
    }
    
    public boolean isError() {
        return error != null;
    }
}
