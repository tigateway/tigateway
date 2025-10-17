package ti.gateway.mcp.model;

/**
 * Request to list available tools
 */
public class ToolsListRequest extends McpRequest {
    
    public ToolsListRequest() {
        super();
    }
    
    public ToolsListRequest(String id) {
        super(id, "tools/list");
    }
}
