package ti.gateway.mcp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ti.gateway.mcp.model.*;
import ti.gateway.mcp.server.McpServer;

import java.util.Map;

/**
 * REST controller for MCP protocol endpoints
 */
@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
@ConditionalOnProperty(prefix = "tigateway.mcp", name = "enabled", havingValue = "true", matchIfMissing = true)
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
    public Mono<HealthResponse> health() {
        HealthResponse health = new HealthResponse(
            "healthy",
            "TiGateway MCP Server",
            "1.0.0",
            System.currentTimeMillis()
        );
        return Mono.just(health);
    }
    
    /**
     * Get server information
     */
    @GetMapping("/info")
    public Mono<ServerInfoResponse> info() {
        ServerInfoResponse info = new ServerInfoResponse(
            "TiGateway MCP Server",
            "1.0.0",
            "MCP server for TiGateway API Gateway management",
            "2024-11-05"
        );
        
        // Set capabilities
        ServerInfoResponse.ToolsCapability tools = new ServerInfoResponse.ToolsCapability(true);
        ServerInfoResponse.ResourcesCapability resources = new ServerInfoResponse.ResourcesCapability(false, true);
        ServerInfoResponse.Capabilities capabilities = new ServerInfoResponse.Capabilities(tools, resources);
        info.setCapabilities(capabilities);
        
        return Mono.just(info);
    }
}
