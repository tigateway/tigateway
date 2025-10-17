package ti.gateway.mcp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ti.gateway.mcp.model.*;
import ti.gateway.mcp.tools.TiGatewayToolExecutor;
import ti.gateway.mcp.tools.TiGatewayTools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Server implementation for TiGateway
 */
@Component
public class McpServer {
    
    private static final Logger logger = LoggerFactory.getLogger(McpServer.class);
    
    @Autowired
    private TiGatewayTools tools;
    
    @Autowired
    private TiGatewayToolExecutor toolExecutor;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Handle MCP request
     */
    public McpResponse handleRequest(McpRequest request) {
        logger.info("Handling MCP request: {}", request.getMethod());
        
        try {
            switch (request.getMethod()) {
                case "initialize":
                    return handleInitialize((InitializeRequest) request);
                case "tools/list":
                    return handleToolsList((ToolsListRequest) request);
                case "tools/call":
                    return handleToolsCall((ToolsCallRequest) request);
                case "resources/list":
                    return handleResourcesList((ResourcesListRequest) request);
                case "resources/read":
                    return handleResourcesRead((ResourcesReadRequest) request);
                default:
                    return createErrorResponse(request.getId(), McpError.METHOD_NOT_FOUND, 
                        "Unknown method: " + request.getMethod());
            }
        } catch (Exception e) {
            logger.error("Error handling MCP request: {}", e.getMessage(), e);
            return createErrorResponse(request.getId(), McpError.INTERNAL_ERROR, 
                "Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Handle initialize request
     */
    private McpResponse handleInitialize(InitializeRequest request) {
        logger.info("Handling initialize request");
        
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        Map<String, Object> capabilities = new HashMap<>();
        Map<String, Object> toolsCap = new HashMap<>();
        toolsCap.put("listChanged", true);
        capabilities.put("tools", toolsCap);
        
        Map<String, Object> resourcesCap = new HashMap<>();
        resourcesCap.put("subscribe", false);
        resourcesCap.put("listChanged", true);
        capabilities.put("resources", resourcesCap);
        result.put("capabilities", capabilities);
        
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("name", "TiGateway MCP Server");
        serverInfo.put("version", "1.0.0");
        serverInfo.put("description", "MCP server for TiGateway API Gateway management");
        result.put("serverInfo", serverInfo);
        
        return new McpResponse(request.getId(), result);
    }
    
    /**
     * Handle tools/list request
     */
    private McpResponse handleToolsList(ToolsListRequest request) {
        logger.info("Handling tools/list request");
        
        List<McpTool> toolList = tools.getAllTools();
        
        Map<String, Object> result = new HashMap<>();
        result.put("tools", toolList);
        
        return new McpResponse(request.getId(), result);
    }
    
    /**
     * Handle tools/call request
     */
    private McpResponse handleToolsCall(ToolsCallRequest request) {
        Map<String, Object> params = request.getParams();
        String toolName = (String) params.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
        
        logger.info("Handling tools/call request for tool: {}", toolName);
        
        return toolExecutor.executeTool(toolName, arguments, request.getId());
    }
    
    /**
     * Handle resources/list request
     */
    private McpResponse handleResourcesList(ResourcesListRequest request) {
        logger.info("Handling resources/list request");
        
        List<McpResource> resources = Arrays.asList(
            new McpResource(
                "tigateway://routes",
                "TiGateway Routes",
                "All routes configured in TiGateway",
                "application/json"
            ),
            new McpResource(
                "tigateway://services",
                "TiGateway Services",
                "All services managed by TiGateway",
                "application/json"
            ),
            new McpResource(
                "tigateway://config",
                "TiGateway Configuration",
                "Current TiGateway configuration",
                "application/yaml"
            ),
            new McpResource(
                "tigateway://metrics",
                "TiGateway Metrics",
                "Current TiGateway metrics and statistics",
                "application/json"
            )
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("resources", resources);
        
        return new McpResponse(request.getId(), result);
    }
    
    /**
     * Handle resources/read request
     */
    private McpResponse handleResourcesRead(ResourcesReadRequest request) {
        Map<String, Object> params = request.getParams();
        String uri = (String) params.get("uri");
        
        logger.info("Handling resources/read request for URI: {}", uri);
        
        Map<String, Object> result = new HashMap<>();
        
        switch (uri) {
            case "tigateway://routes": {
                Map<String, Object> content = new HashMap<>();
                content.put("uri", uri);
                content.put("mimeType", "application/json");
                content.put("text", "{\"routes\": [{\"name\": \"user-service-route\", \"path\": \"/api/users/**\"}]}");
                result.put("contents", Arrays.asList(content));
                break;
            }
            case "tigateway://services": {
                Map<String, Object> content = new HashMap<>();
                content.put("uri", uri);
                content.put("mimeType", "application/json");
                content.put("text", "{\"services\": [{\"name\": \"user-service\", \"status\": \"running\"}]}");
                result.put("contents", Arrays.asList(content));
                break;
            }
            case "tigateway://config": {
                Map<String, Object> content = new HashMap<>();
                content.put("uri", uri);
                content.put("mimeType", "application/yaml");
                content.put("text", "server:\n  port: 8080\nspring:\n  application:\n    name: tigateway");
                result.put("contents", Arrays.asList(content));
                break;
            }
            case "tigateway://metrics": {
                Map<String, Object> content = new HashMap<>();
                content.put("uri", uri);
                content.put("mimeType", "application/json");
                content.put("text", "{\"metrics\": {\"requests\": 125000, \"errors\": 5000}}");
                result.put("contents", Arrays.asList(content));
                break;
            }
            default:
                return createErrorResponse(request.getId(), McpError.INVALID_PARAMS, 
                    "Unknown resource URI: " + uri);
        }
        
        return new McpResponse(request.getId(), result);
    }
    
    /**
     * Create error response
     */
    private McpResponse createErrorResponse(String id, int code, String message) {
        return new McpResponse(id, new McpError(code, message));
    }
}
