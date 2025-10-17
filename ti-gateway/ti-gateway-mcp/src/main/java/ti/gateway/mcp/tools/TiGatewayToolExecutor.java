package ti.gateway.mcp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ti.gateway.mcp.model.*;
import ti.gateway.mcp.service.TiGatewayKubernetesService;
import ti.gateway.mcp.service.TiGatewayMetricsService;
import ti.gateway.mcp.service.TiGatewayConfigService;
import ti.gateway.mcp.service.TiGatewayLogsService;

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
            
            Object result;
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
    
    private RouteResponse executeListRoutes(Map<String, Object> arguments) {
        String namespace = (String) arguments.getOrDefault("namespace", "default");
        String filter = (String) arguments.getOrDefault("filter", "");
        
        List<RouteInfo> routes = kubernetesService.listRoutes(namespace, filter);
        return RouteResponse.listSuccess(routes, namespace);
    }
    
    private RouteResponse executeCreateRoute(Map<String, Object> arguments) {
        String name = (String) arguments.get("name");
        String namespace = (String) arguments.get("namespace");
        String path = (String) arguments.get("path");
        String service = (String) arguments.get("service");
        Integer port = (Integer) arguments.get("port");
        @SuppressWarnings("unchecked")
        List<String> filters = (List<String>) arguments.getOrDefault("filters", List.of());
        @SuppressWarnings("unchecked")
        List<String> predicates = (List<String>) arguments.getOrDefault("predicates", List.of());
        
        Map<String, Object> routeConfig = new java.util.HashMap<>();
        routeConfig.put("name", name);
        routeConfig.put("path", path);
        routeConfig.put("service", service);
        routeConfig.put("port", port);
        routeConfig.put("filters", filters);
        routeConfig.put("predicates", predicates);
        
        boolean success = kubernetesService.createRoute(namespace, routeConfig);
        
        if (success) {
            RouteInfo route = new RouteInfo(name, namespace, path, service, port, "active");
            route.setFilters(filters);
            route.setPredicates(predicates);
            return RouteResponse.createSuccess(route);
        } else {
            return RouteResponse.createError("Failed to create route");
        }
    }
    
    private RouteResponse executeUpdateRoute(Map<String, Object> arguments) {
        String name = (String) arguments.get("name");
        String namespace = (String) arguments.get("namespace");
        
        Map<String, Object> updates = new java.util.HashMap<>(arguments);
        updates.remove("name");
        updates.remove("namespace");
        
        boolean success = kubernetesService.updateRoute(namespace, name, updates);
        
        if (success) {
            return RouteResponse.updateSuccess(name, updates);
        } else {
            return RouteResponse.updateError("Failed to update route");
        }
    }
    
    private RouteResponse executeDeleteRoute(Map<String, Object> arguments) {
        String name = (String) arguments.get("name");
        String namespace = (String) arguments.get("namespace");
        
        boolean success = kubernetesService.deleteRoute(namespace, name);
        
        if (success) {
            return RouteResponse.deleteSuccess(name);
        } else {
            return RouteResponse.deleteError("Failed to delete route");
        }
    }
    
    private RouteResponse executeTestRoute(Map<String, Object> arguments) {
        String name = (String) arguments.get("name");
        String namespace = (String) arguments.get("namespace");
        String path = (String) arguments.get("path");
        String method = (String) arguments.get("method");
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) arguments.getOrDefault("headers", Map.of());
        String body = (String) arguments.getOrDefault("body", "");
        
        Map<String, Object> testResult = kubernetesService.testRoute(namespace, name, path, method, headers, body);
        
        return RouteResponse.testSuccess(testResult);
    }
    
    private ServiceResponse executeListServices(Map<String, Object> arguments) {
        String namespace = (String) arguments.getOrDefault("namespace", "default");
        String filter = (String) arguments.getOrDefault("filter", "");
        
        List<ServiceInfo> services = kubernetesService.listServices(namespace, filter);
        return ServiceResponse.listSuccess(services, namespace);
    }
    
    private ServiceResponse executeServiceHealth(Map<String, Object> arguments) {
        String service = (String) arguments.get("service");
        String namespace = (String) arguments.get("namespace");
        
        Map<String, Object> health = kubernetesService.getServiceHealth(namespace, service);
        return ServiceResponse.healthSuccess(health);
    }
    
    private MetricsResponse executeGetMetrics(Map<String, Object> arguments) {
        String type = (String) arguments.get("type");
        String timeRange = (String) arguments.getOrDefault("timeRange", "1h");
        String namespace = (String) arguments.getOrDefault("namespace", "default");
        
        MetricsInfo metrics = metricsService.getMetrics(type, timeRange, namespace);
        return MetricsResponse.success(metrics, type, timeRange, namespace);
    }
    
    private ConfigResponse executeGetConfig(Map<String, Object> arguments) {
        String type = (String) arguments.get("type");
        String namespace = (String) arguments.getOrDefault("namespace", "default");
        String format = (String) arguments.getOrDefault("format", "json");
        
        ConfigInfo config = configService.getConfig(type, namespace, format);
        return ConfigResponse.success(config, type, format, namespace);
    }
    
    private LogsResponse executeGetLogs(Map<String, Object> arguments) {
        String level = (String) arguments.getOrDefault("level", "INFO");
        Integer lines = (Integer) arguments.getOrDefault("lines", 100);
        String namespace = (String) arguments.getOrDefault("namespace", "default");
        String service = (String) arguments.getOrDefault("service", "");
        String filter = (String) arguments.getOrDefault("filter", "");
        
        List<LogInfo> logs = logsService.getLogs(level, lines, namespace, service, filter);
        return LogsResponse.success(logs, level, namespace, service, filter);
    }
}
