package ti.gateway.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ti.gateway.mcp.config.McpProperties;

/**
 * TiGateway MCP Server Application
 */
@SpringBootApplication
@EnableConfigurationProperties(McpProperties.class)
public class TiGatewayMcpApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(TiGatewayMcpApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting TiGateway MCP Server...");
        
        SpringApplication app = new SpringApplication(TiGatewayMcpApplication.class);
        
        // Set default properties
        java.util.Map<String, Object> defaultProperties = new java.util.HashMap<>();
        defaultProperties.put("server.port", "8082");
        defaultProperties.put("spring.application.name", "tigateway-mcp");
        defaultProperties.put("logging.level.ti.gateway.mcp", "INFO");
        app.setDefaultProperties(defaultProperties);
        
        app.run(args);
        
        logger.info("TiGateway MCP Server started successfully!");
    }
}
