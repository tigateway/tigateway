package ti.gateway.mcp.tools;

import org.springframework.stereotype.Component;
import ti.gateway.mcp.model.McpTool;

import java.util.Arrays;
import java.util.HashMap;
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
        return Arrays.asList(
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
            McpTool.createJsonSchema("object", createMap(
                "namespace", McpTool.createProperty("string", "Kubernetes namespace (optional)"),
                "filter", McpTool.createProperty("string", "Filter routes by name or pattern (optional)")
            ), Arrays.asList())
        );
    }
    
    /**
     * Create route tool
     */
    private McpTool createRouteCreateTool() {
        return new McpTool(
            "tigateway_create_route",
            "Create a new route in TiGateway",
            McpTool.createJsonSchema("object", createMap(
                "name", McpTool.createProperty("string", "Route name"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace"),
                "path", McpTool.createProperty("string", "Route path pattern"),
                "service", McpTool.createProperty("string", "Target service name"),
                "port", McpTool.createProperty("integer", "Target service port"),
                "filters", McpTool.createProperty("array", "List of filters to apply (optional)"),
                "predicates", McpTool.createProperty("array", "List of predicates (optional)")
            ), Arrays.asList("name", "namespace", "path", "service", "port"))
        );
    }
    
    /**
     * Update route tool
     */
    private McpTool createRouteUpdateTool() {
        return new McpTool(
            "tigateway_update_route",
            "Update an existing route in TiGateway",
            McpTool.createJsonSchema("object", createMap(
                "name", McpTool.createProperty("string", "Route name"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace"),
                "path", McpTool.createProperty("string", "Route path pattern (optional)"),
                "service", McpTool.createProperty("string", "Target service name (optional)"),
                "port", McpTool.createProperty("integer", "Target service port (optional)"),
                "filters", McpTool.createProperty("array", "List of filters to apply (optional)"),
                "predicates", McpTool.createProperty("array", "List of predicates (optional)")
            ), Arrays.asList("name", "namespace"))
        );
    }
    
    /**
     * Delete route tool
     */
    private McpTool createRouteDeleteTool() {
        return new McpTool(
            "tigateway_delete_route",
            "Delete a route from TiGateway",
            McpTool.createJsonSchema("object", createMap(
                "name", McpTool.createProperty("string", "Route name"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace")
            ), Arrays.asList("name", "namespace"))
        );
    }
    
    /**
     * Test route tool
     */
    private McpTool createRouteTestTool() {
        return new McpTool(
            "tigateway_test_route",
            "Test a route by sending a request",
            McpTool.createJsonSchema("object", createMap(
                "name", McpTool.createProperty("string", "Route name"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace"),
                "path", McpTool.createProperty("string", "Test path"),
                "method", McpTool.createPropertyWithEnum("string", "HTTP method", Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH")),
                "headers", McpTool.createProperty("object", "Request headers (optional)"),
                "body", McpTool.createProperty("string", "Request body (optional)")
            ), Arrays.asList("name", "namespace", "path", "method"))
        );
    }
    
    /**
     * List services tool
     */
    private McpTool createServiceListTool() {
        return new McpTool(
            "tigateway_list_services",
            "List all services in TiGateway",
            McpTool.createJsonSchema("object", createMap(
                "namespace", McpTool.createProperty("string", "Kubernetes namespace (optional)"),
                "filter", McpTool.createProperty("string", "Filter services by name (optional)")
            ), Arrays.asList())
        );
    }
    
    /**
     * Check service health tool
     */
    private McpTool createServiceHealthTool() {
        return new McpTool(
            "tigateway_service_health",
            "Check the health status of a service",
            McpTool.createJsonSchema("object", createMap(
                "service", McpTool.createProperty("string", "Service name"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace")
            ), Arrays.asList("service", "namespace"))
        );
    }
    
    /**
     * Get metrics tool
     */
    private McpTool createMetricsTool() {
        return new McpTool(
            "tigateway_get_metrics",
            "Get TiGateway metrics and statistics",
            McpTool.createJsonSchema("object", createMap(
                "type", McpTool.createPropertyWithEnum("string", "Metrics type", 
                    Arrays.asList("routes", "services", "requests", "errors", "performance")),
                "timeRange", McpTool.createProperty("string", "Time range (e.g., '1h', '24h', '7d') (optional)"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace (optional)")
            ), Arrays.asList("type"))
        );
    }
    
    /**
     * Get configuration tool
     */
    private McpTool createConfigTool() {
        return new McpTool(
            "tigateway_get_config",
            "Get TiGateway configuration",
            McpTool.createJsonSchema("object", createMap(
                "type", McpTool.createPropertyWithEnum("string", "Configuration type", 
                    Arrays.asList("routes", "filters", "global", "security", "monitoring")),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace (optional)"),
                "format", McpTool.createPropertyWithEnum("string", "Output format", Arrays.asList("json", "yaml"))
            ), Arrays.asList("type"))
        );
    }
    
    /**
     * Get logs tool
     */
    private McpTool createLogsTool() {
        return new McpTool(
            "tigateway_get_logs",
            "Get TiGateway logs",
            McpTool.createJsonSchema("object", createMap(
                "level", McpTool.createPropertyWithEnum("string", "Log level", 
                    Arrays.asList("DEBUG", "INFO", "WARN", "ERROR")),
                "lines", McpTool.createProperty("integer", "Number of log lines to retrieve (default: 100)"),
                "namespace", McpTool.createProperty("string", "Kubernetes namespace (optional)"),
                "service", McpTool.createProperty("string", "Service name (optional)"),
                "filter", McpTool.createProperty("string", "Filter logs by text (optional)")
            ), Arrays.asList())
        );
    }
    
    /**
     * Helper method to create Map for Java 8 compatibility
     */
    private static <K, V> Map<K, V> createMap(K key1, V value1, K key2, V value2) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }
    
    private static <K, V> Map<K, V> createMap(K key1, V value1, K key2, V value2, K key3, V value3) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }
    
    private static <K, V> Map<K, V> createMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        return map;
    }
    
    private static <K, V> Map<K, V> createMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        return map;
    }
    
    private static <K, V> Map<K, V> createMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5, K key6, V value6) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        return map;
    }
    
    private static <K, V> Map<K, V> createMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5, K key6, V value6, K key7, V value7) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        map.put(key7, value7);
        return map;
    }
    
    private static <K, V> Map<K, V> createMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5, K key6, V value6, K key7, V value7, K key8, V value8) {
        Map<K, V> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        map.put(key4, value4);
        map.put(key5, value5);
        map.put(key6, value6);
        map.put(key7, value7);
        map.put(key8, value8);
        return map;
    }
}
