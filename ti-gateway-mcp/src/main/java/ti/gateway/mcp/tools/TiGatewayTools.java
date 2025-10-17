package ti.gateway.mcp.tools;

import org.springframework.stereotype.Component;
import ti.gateway.mcp.model.McpTool;

import java.util.List;
import java.util.Map;

/**
 * TiGateway MCP tools definition
 */
@Component
public class TiGatewayTools {
    
    /**
     * Get all available TiGateway tools
     */
    public List<McpTool> getAllTools() {
        return List.of(
            createRouteListTool(),
            createRouteCreateTool(),
            createRouteUpdateTool(),
            createRouteDeleteTool(),
            createRouteTestTool(),
            createServiceListTool(),
            createServiceHealthTool(),
            createMetricsTool(),
            createConfigTool(),
            createLogsTool()
        );
    }
    
    /**
     * List all routes tool
     */
    private McpTool createRouteListTool() {
        return new McpTool(
            "tigateway_list_routes",
            "List all routes in TiGateway",
            McpTool.createJsonSchema("object", Map.of(
                "namespace", McpTool.createProperty("string", "Kubernetes namespace (optional)"),
                "filter", McpTool.createProperty("string", "Filter routes by name or pattern (optional)")
            ), List.of())
        );
    }
    
    /**
     * Create route tool
     */
    private McpTool createRouteCreateTool() {
        return new McpTool(
            "tigateway_create_route",
            "Create a new route in TiGateway",
            McpTool.createJsonSchema("object", Map.of(
                "name", McpTool.createProperty("string", "Route name"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace"),
                "path", McpTool.createProperty("string", "Route path pattern"),
                "service", McpTool.createProperty("string", "Target service name"),
                "port", McpTool.createProperty("integer", "Target service port"),
                "filters", McpTool.createProperty("array", "List of filters to apply (optional)"),
                "predicates", McpTool.createProperty("array", "List of predicates (optional)")
            ), List.of("name", "namespace", "path", "service", "port"))
        );
    }
    
    /**
     * Update route tool
     */
    private McpTool createRouteUpdateTool() {
        return new McpTool(
            "tigateway_update_route",
            "Update an existing route in TiGateway",
            McpTool.createJsonSchema("object", Map.of(
                "name", McpTool.createProperty("string", "Route name"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace"),
                "path", McpTool.createProperty("string", "Route path pattern (optional)"),
                "service", McpTool.createProperty("string", "Target service name (optional)"),
                "port", McpTool.createProperty("integer", "Target service port (optional)"),
                "filters", McpTool.createProperty("array", "List of filters to apply (optional)"),
                "predicates", McpTool.createProperty("array", "List of predicates (optional)")
            ), List.of("name", "namespace"))
        );
    }
    
    /**
     * Delete route tool
     */
    private McpTool createRouteDeleteTool() {
        return new McpTool(
            "tigateway_delete_route",
            "Delete a route from TiGateway",
            McpTool.createJsonSchema("object", Map.of(
                "name", McpTool.createProperty("string", "Route name"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace")
            ), List.of("name", "namespace"))
        );
    }
    
    /**
     * Test route tool
     */
    private McpTool createRouteTestTool() {
        return new McpTool(
            "tigateway_test_route",
            "Test a route by sending a request",
            McpTool.createJsonSchema("object", Map.of(
                "name", McpTool.createProperty("string", "Route name"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace"),
                "path", McpTool.createProperty("string", "Test path"),
                "method", McpTool.createPropertyWithEnum("string", "HTTP method", List.of("GET", "POST", "PUT", "DELETE", "PATCH")),
                "headers", McpTool.createProperty("object", "Request headers (optional)"),
                "body", McpTool.createProperty("string", "Request body (optional)")
            ), List.of("name", "namespace", "path", "method"))
        );
    }
    
    /**
     * List services tool
     */
    private McpTool createServiceListTool() {
        return new McpTool(
            "tigateway_list_services",
            "List all services in TiGateway",
            McpTool.createJsonSchema("object", Map.of(
                "namespace", McpTool.createProperty("string", "Kubernetes namespace (optional)"),
                "filter", McpTool.createProperty("string", "Filter services by name (optional)")
            ), List.of())
        );
    }
    
    /**
     * Check service health tool
     */
    private McpTool createServiceHealthTool() {
        return new McpTool(
            "tigateway_service_health",
            "Check the health status of a service",
            McpTool.createJsonSchema("object", Map.of(
                "service", McpTool.createProperty("string", "Service name"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace")
            ), List.of("service", "namespace"))
        );
    }
    
    /**
     * Get metrics tool
     */
    private McpTool createMetricsTool() {
        return new McpTool(
            "tigateway_get_metrics",
            "Get TiGateway metrics and statistics",
            McpTool.createJsonSchema("object", Map.of(
                "type", McpTool.createPropertyWithEnum("string", "Metrics type", 
                    List.of("routes", "services", "requests", "errors", "performance")),
                "timeRange", McpTool.createProperty("string", "Time range (e.g., '1h', '24h', '7d') (optional)"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace (optional)")
            ), List.of("type"))
        );
    }
    
    /**
     * Get configuration tool
     */
    private McpTool createConfigTool() {
        return new McpTool(
            "tigateway_get_config",
            "Get TiGateway configuration",
            McpTool.createJsonSchema("object", Map.of(
                "type", McpTool.createPropertyWithEnum("string", "Configuration type", 
                    List.of("routes", "filters", "global", "security", "monitoring")),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace (optional)"),
                "format", McpTool.createPropertyWithEnum("string", "Output format", List.of("json", "yaml"))
            ), List.of("type"))
        );
    }
    
    /**
     * Get logs tool
     */
    private McpTool createLogsTool() {
        return new McpTool(
            "tigateway_get_logs",
            "Get TiGateway logs",
            McpTool.createJsonSchema("object", Map.of(
                "level", McpTool.createPropertyWithEnum("string", "Log level", 
                    List.of("DEBUG", "INFO", "WARN", "ERROR")),
                "lines", McpTool.createProperty("integer", "Number of log lines to retrieve (default: 100)"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace (optional)"),
                "service", McpTool.createProperty("string", "Service name (optional)"),
                "filter", McpTool.createProperty("string", "Filter logs by text (optional)")
            ), List.of())
        );
    }
    
}
