package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Request to read a specific resource
 */
public class ResourcesReadRequest extends McpRequest {
    
    @JsonProperty("params")
    private ResourcesReadParams params;
    
    public ResourcesReadRequest() {
        super();
    }
    
    public ResourcesReadRequest(String id) {
        super(id, "resources/read");
    }
    
    public ResourcesReadParams getResourcesReadParams() {
        return params;
    }
    
    public void setResourcesReadParams(ResourcesReadParams params) {
        this.params = params;
    }
    
    @Override
    public Map<String, Object> getParams() {
        // Convert ResourcesReadParams to Map for compatibility
        if (params == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("uri", params.getUri());
        return map;
    }
    
    public static class ResourcesReadParams {
        
        @JsonProperty("uri")
        private String uri;
        
        public ResourcesReadParams() {}
        
        public ResourcesReadParams(String uri) {
            this.uri = uri;
        }
        
        public String getUri() {
            return uri;
        }
        
        public void setUri(String uri) {
            this.uri = uri;
        }
    }
}
