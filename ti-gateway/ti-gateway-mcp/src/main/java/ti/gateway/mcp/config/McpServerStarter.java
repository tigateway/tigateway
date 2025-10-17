package ti.gateway.mcp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.Map;

/**
 * MCP Server Starter for independent port
 */
@Configuration
@ConditionalOnProperty(prefix = "tigateway.mcp", name = "independent-port", havingValue = "true", matchIfMissing = false)
public class McpServerStarter {
    
    private static final Logger logger = LoggerFactory.getLogger(McpServerStarter.class);
    
    /**
     * Start MCP server on independent port
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> mcpServerStarter(McpProperties mcpProperties) {
        return event -> {
            if (mcpProperties.isIndependentPort()) {
                startMcpServer(mcpProperties);
            }
        };
    }
    
    private void startMcpServer(McpProperties mcpProperties) {
        logger.info("Starting MCP server on independent port: {}", mcpProperties.getPort());
        
        DisposableServer server = HttpServer.create()
                .port(mcpProperties.getPort())
                .route(routes -> {
                    routes.get("/mcp/health", (request, response) -> {
                        Map<String, Object> health = Map.of(
                            "status", "healthy",
                            "service", "TiGateway MCP Server",
                            "version", "1.0.0",
                            "timestamp", System.currentTimeMillis(),
                            "port", mcpProperties.getPort()
                        );
                        return response.sendString(reactor.core.publisher.Mono.just(health.toString()));
                    });
                    
                    routes.get("/mcp/info", (request, response) -> {
                        Map<String, Object> info = Map.of(
                            "name", "TiGateway MCP Server",
                            "version", "1.0.0",
                            "description", "MCP server for TiGateway API Gateway management",
                            "protocolVersion", "2024-11-05",
                            "port", mcpProperties.getPort(),
                            "independent", true
                        );
                        return response.sendString(reactor.core.publisher.Mono.just(info.toString()));
                    });
                })
                .bindNow();
        
        logger.info("MCP server started successfully on port: {}", mcpProperties.getPort());
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down MCP server...");
            server.disposeNow();
        }));
    }
}
