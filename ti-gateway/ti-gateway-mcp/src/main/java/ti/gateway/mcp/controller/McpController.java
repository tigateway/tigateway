package ti.gateway.mcp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ti.gateway.mcp.model.McpRequest;
import ti.gateway.mcp.model.McpResponse;
import ti.gateway.mcp.server.McpServer;

import java.util.Map;

/**
 * REST controller for MCP protocol endpoints
 */
@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
public class McpController {
    
    private static final Logger logger = LoggerFactory.getLogger(McpController.class);
    
    @Autowired
    private McpServer mcpServer;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Handle MCP requests via HTTP POST
     */
    @PostMapping(value = "/request", 
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<McpResponse> handleRequest(@RequestBody Map<String, Object> requestBody) {
        logger.info("Received MCP request: {}", requestBody);
        
        return Mono.fromCallable(() -> {
            try {
                McpRequest request = objectMapper.convertValue(requestBody, McpRequest.class);
                return mcpServer.handleRequest(request);
            } catch (Exception e) {
                logger.error("Error processing MCP request: {}", e.getMessage(), e);
                return new McpResponse(
                    (String) requestBody.get("id"),
                    new ti.gateway.mcp.model.McpError(
                        ti.gateway.mcp.model.McpError.PARSE_ERROR,
                        "Failed to parse request: " + e.getMessage()
                    )
                );
            }
        });
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> health = new java.util.HashMap<>();
        health.put("status", "healthy");
        health.put("service", "TiGateway MCP Server");
        health.put("version", "1.0.0");
        health.put("timestamp", System.currentTimeMillis());
        return Mono.just(health);
    }
    
    /**
     * Get server information
     */
    @GetMapping("/info")
    public Mono<Map<String, Object>> info() {
        Map<String, Object> info = new java.util.HashMap<>();
        info.put("name", "TiGateway MCP Server");
        info.put("version", "1.0.0");
        info.put("description", "MCP server for TiGateway API Gateway management");
        info.put("protocolVersion", "2024-11-05");
        
        Map<String, Object> capabilities = new java.util.HashMap<>();
        Map<String, Object> tools = new java.util.HashMap<>();
        tools.put("listChanged", true);
        capabilities.put("tools", tools);
        
        Map<String, Object> resources = new java.util.HashMap<>();
        resources.put("subscribe", false);
        resources.put("listChanged", true);
        capabilities.put("resources", resources);
        
        info.put("capabilities", capabilities);
        return Mono.just(info);
    }
}
