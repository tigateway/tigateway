package ti.gateway.mcp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * MCP Server Configuration for independent port
 */
@Configuration
@EnableConfigurationProperties(McpProperties.class)
@ConditionalOnProperty(prefix = "tigateway.mcp", name = "enabled", havingValue = "true", matchIfMissing = true)
public class McpServerConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(McpServerConfiguration.class);
    
    /**
     * Configure MCP server routes for independent port
     */
    @Bean
    @ConditionalOnProperty(prefix = "tigateway.mcp", name = "independent-port", havingValue = "true", matchIfMissing = false)
    public RouterFunction<ServerResponse> mcpRouterFunction() {
        logger.info("Configuring MCP server for independent port");
        
        return RouterFunctions.route()
                .GET("/mcp/health", request -> {
                    Map<String, Object> health = Map.of(
                        "status", "healthy",
                        "service", "TiGateway MCP Server",
                        "version", "1.0.0",
                        "timestamp", System.currentTimeMillis()
                    );
                    return ServerResponse.ok().bodyValue(health);
                })
                .GET("/mcp/info", request -> {
                    Map<String, Object> info = Map.of(
                        "name", "TiGateway MCP Server",
                        "version", "1.0.0",
                        "description", "MCP server for TiGateway API Gateway management",
                        "protocolVersion", "2024-11-05"
                    );
                    return ServerResponse.ok().bodyValue(info);
                })
                .build();
    }
}
