package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;

/**
 * Base class for MCP requests
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "method")
@JsonSubTypes({
    @JsonSubTypes.Type(value = InitializeRequest.class, name = "initialize"),
    @JsonSubTypes.Type(value = ToolsListRequest.class, name = "tools/list"),
    @JsonSubTypes.Type(value = ToolsCallRequest.class, name = "tools/call"),
    @JsonSubTypes.Type(value = ResourcesListRequest.class, name = "resources/list"),
    @JsonSubTypes.Type(value = ResourcesReadRequest.class, name = "resources/read")
})
public abstract class McpRequest {
    
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    public McpRequest() {}
    
    public McpRequest(String id, String method) {
        this.id = id;
        this.method = method;
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
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public Map<String, Object> getParams() {
        return params;
    }
    
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
