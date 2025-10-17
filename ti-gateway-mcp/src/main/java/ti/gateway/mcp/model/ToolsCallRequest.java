package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Request to call a specific tool
 */
public class ToolsCallRequest extends McpRequest {
    
    @JsonProperty("params")
    private ToolsCallParams params;
    
    public ToolsCallRequest() {
        super();
    }
    
    public ToolsCallRequest(String id) {
        super(id, "tools/call");
    }
    
    public ToolsCallParams getToolsCallParams() {
        return params;
    }
    
    public void setToolsCallParams(ToolsCallParams params) {
        this.params = params;
    }
    
    @Override
    public Map<String, Object> getParams() {
        // Convert ToolsCallParams to Map for compatibility
        if (params == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("name", params.getName());
        map.put("arguments", params.getArguments());
        return map;
    }
    
    public static class ToolsCallParams {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("arguments")
        private Map<String, Object> arguments;
        
        public ToolsCallParams() {}
        
        public ToolsCallParams(String name, Map<String, Object> arguments) {
            this.name = name;
            this.arguments = arguments;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Map<String, Object> getArguments() {
            return arguments;
        }
        
        public void setArguments(Map<String, Object> arguments) {
            this.arguments = arguments;
        }
    }
}
