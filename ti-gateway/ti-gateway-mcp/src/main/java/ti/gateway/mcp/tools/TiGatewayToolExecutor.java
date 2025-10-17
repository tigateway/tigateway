package ti.gateway.mcp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ti.gateway.mcp.model.McpError;
import ti.gateway.mcp.model.McpResponse;
import ti.gateway.mcp.service.TiGatewayKubernetesService;
import ti.gateway.mcp.service.TiGatewayMetricsService;
import ti.gateway.mcp.service.TiGatewayConfigService;
import ti.gateway.mcp.service.TiGatewayLogsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executor for TiGateway MCP tools
 */
@Component
public class TiGatewayToolExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(TiGatewayToolExecutor.class);
    
    @Autowired
    private TiGatewayKubernetesService kubernetesService;
    
    @Autowired
    private TiGatewayMetricsService metricsService;
    
    @Autowired
    private TiGatewayConfigService configService;
    
    @Autowired
    private TiGatewayLogsService logsService;
    
    /**
     * Execute a tool call
     */
    public McpResponse executeTool(String toolName, Map<String, Object> arguments, String requestId) {
        try {
            logger.info("Executing tool: {} with arguments: {}", toolName, arguments);
            
            Map<String, Object> result;
            switch (toolName) {
                case "tigateway_list_routes":
                    result = executeListRoutes(arguments);
                    break;
                case "tigateway_create_route":
                    result = executeCreateRoute(arguments);
                    break;
                case "tigateway_update_route":
                    result = executeUpdateRoute(arguments);
                    break;
                case "tigateway_delete_route":
                    result = executeDeleteRoute(arguments);
                    break;
                case "tigateway_test_route":
                    result = executeTestRoute(arguments);
                    break;
                case "tigateway_list_services":
                    result = executeListServices(arguments);
                    break;
                case "tigateway_service_health":
                    result = executeServiceHealth(arguments);
                    break;
                case "tigateway_get_metrics":
                    result = executeGetMetrics(arguments);
                    break;
                case "tigateway_get_config":
                    result = executeGetConfig(arguments);
                    break;
                case "tigateway_get_logs":
                    result = executeGetLogs(arguments);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown tool: " + toolName);
            }
            
            return new McpResponse(requestId, result);
            
        } catch (Exception e) {
            logger.error("Error executing tool {}: {}", toolName, e.getMessage(), e);
            return new McpResponse(requestId, new McpError(
                McpError.INTERNAL_ERROR, 
                "Tool execution failed: " + e.getMessage()
            ));
        }
    }
    
    private Map<String, Object> executeListRoutes(Map<String, Object> arguments) {
        String namespace = (String) arguments.getOrDefault("namespace", "default");
        String filter = (String) arguments.getOrDefault("filter", "");
        
        List<Map<String, Object>> routes = kubernetesService.listRoutes(namespace, filter);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", routes);
        result.put("count", routes.size());
        result.put("namespace", namespace);
        return result;
    }
    
    private Map<String, Object> executeCreateRoute(Map<String, Object> arguments) {
        String name = (String) arguments.get("name");
        String namespace = (String) arguments.get("namespace");
        String path = (String) arguments.get("path");
        String service = (String) arguments.get("service");
        Integer port = (Integer) arguments.get("port");
        @SuppressWarnings("unchecked")
        List<String> filters = (List<String>) arguments.getOrDefault("filters", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<String> predicates = (List<String>) arguments.getOrDefault("predicates", new ArrayList<>());
        
        Map<String, Object> routeConfig = new HashMap<>();
        routeConfig.put("name", name);
        routeConfig.put("path", path);
        routeConfig.put("service", service);
        routeConfig.put("port", port);
        routeConfig.put("filters", filters);
        routeConfig.put("predicates", predicates);
        
        boolean success = kubernetesService.createRoute(namespace, routeConfig);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Route created successfully" : "Failed to create route");
        result.put("route", routeConfig);
        return result;
    }
    
    private Map<String, Object> executeUpdateRoute(Map<String, Object> arguments) {
        String name = (String) arguments.get("name");
        String namespace = (String) arguments.get("namespace");
        
        Map<String, Object> updates = new HashMap<>(arguments);
        updates.remove("name");
        updates.remove("namespace");
        
        boolean success = kubernetesService.updateRoute(namespace, name, updates);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Route updated successfully" : "Failed to update route");
        result.put("routeName", name);
        result.put("updates", updates);
        return result;
    }
    
    private Map<String, Object> executeDeleteRoute(Map<String, Object> arguments) {
        String name = (String) arguments.get("name");
        String namespace = (String) arguments.get("namespace");
        
        boolean success = kubernetesService.deleteRoute(namespace, name);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Route deleted successfully" : "Failed to delete route");
        result.put("routeName", name);
        return result;
    }
    
    private Map<String, Object> executeTestRoute(Map<String, Object> arguments) {
        String name = (String) arguments.get("name");
        String namespace = (String) arguments.get("namespace");
        String path = (String) arguments.get("path");
        String method = (String) arguments.get("method");
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) arguments.getOrDefault("headers", new HashMap<>());
        String body = (String) arguments.getOrDefault("body", "");
        
        Map<String, Object> testResult = kubernetesService.testRoute(namespace, name, path, method, headers, body);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("testResult", testResult);
        return result;
    }
    
    private Map<String, Object> executeListServices(Map<String, Object> arguments) {
        String namespace = (String) arguments.getOrDefault("namespace", "default");
        String filter = (String) arguments.getOrDefault("filter", "");
        
        List<Map<String, Object>> services = kubernetesService.listServices(namespace, filter);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", services);
        result.put("count", services.size());
        result.put("namespace", namespace);
        return result;
    }
    
    private Map<String, Object> executeServiceHealth(Map<String, Object> arguments) {
        String service = (String) arguments.get("service");
        String namespace = (String) arguments.get("namespace");
        
        Map<String, Object> health = kubernetesService.getServiceHealth(namespace, service);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("health", health);
        return result;
    }
    
    private Map<String, Object> executeGetMetrics(Map<String, Object> arguments) {
        String type = (String) arguments.get("type");
        String timeRange = (String) arguments.getOrDefault("timeRange", "1h");
        String namespace = (String) arguments.getOrDefault("namespace", "default");
        
        Map<String, Object> metrics = metricsService.getMetrics(type, timeRange, namespace);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("metrics", metrics);
        result.put("type", type);
        result.put("timeRange", timeRange);
        return result;
    }
    
    private Map<String, Object> executeGetConfig(Map<String, Object> arguments) {
        String type = (String) arguments.get("type");
        String namespace = (String) arguments.getOrDefault("namespace", "default");
        String format = (String) arguments.getOrDefault("format", "json");
        
        Map<String, Object> config = configService.getConfig(type, namespace, format);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("config", config);
        result.put("type", type);
        result.put("format", format);
        return result;
    }
    
    private Map<String, Object> executeGetLogs(Map<String, Object> arguments) {
        String level = (String) arguments.getOrDefault("level", "INFO");
        Integer lines = (Integer) arguments.getOrDefault("lines", 100);
        String namespace = (String) arguments.getOrDefault("namespace", "default");
        String service = (String) arguments.getOrDefault("service", "");
        String filter = (String) arguments.getOrDefault("filter", "");
        
        List<Map<String, Object>> logs = logsService.getLogs(level, lines, namespace, service, filter);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("logs", logs);
        result.put("count", logs.size());
        result.put("level", level);
        return result;
    }
}
