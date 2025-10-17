package ti.gateway.mcp.model;

/**
 * Request to list available resources
 */
public class ResourcesListRequest extends McpRequest {
    
    public ResourcesListRequest() {
        super();
    }
    
    public ResourcesListRequest(String id) {
        super(id, "resources/list");
    }
}
