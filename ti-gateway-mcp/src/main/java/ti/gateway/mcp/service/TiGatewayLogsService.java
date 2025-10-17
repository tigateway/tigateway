package ti.gateway.mcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ti.gateway.mcp.model.LogInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for retrieving TiGateway logs
 */
@Service
public class TiGatewayLogsService {
    
    private static final Logger logger = LoggerFactory.getLogger(TiGatewayLogsService.class);
    
    /**
     * Get logs with filters
     */
    public List<LogInfo> getLogs(String level, Integer lines, String namespace, 
                                String service, String filter) {
        logger.info("Getting logs - level: {} lines: {} namespace: {} service: {} filter: {}", 
                   level, lines, namespace, service, filter);
        
        // TODO: Implement actual log retrieval from Kubernetes or log aggregation system
        // This is a mock implementation
        List<LogInfo> logs = new ArrayList<>();
        
        // Generate mock logs
        for (int i = 0; i < Math.min(lines, 10); i++) {
            LogInfo logEntry = new LogInfo(
                Instant.now().minusSeconds(i * 10).toString(),
                level,
                service != null ? service : "tigateway",
                generateMockLogMessage(level, i)
            );
            logEntry.setNamespace(namespace);
            logEntry.setThread("http-nio-8080-exec-" + (i % 10 + 1));
            logEntry.setLogger("ti.gateway.core.RouteHandler");
            
            // Set context information
            Map<String, Object> context = new HashMap<>();
            context.put("pod", "tigateway-" + (i % 3 + 1));
            context.put("service", service != null && !service.isEmpty() ? service : "tigateway");
            logEntry.setContext(context);
            
            logs.add(logEntry);
        }
        
        // Apply text filter if provided
        if (!filter.isEmpty()) {
            List<LogInfo> filteredLogs = new ArrayList<>();
            for (LogInfo log : logs) {
                if (log.getMessage().toLowerCase().contains(filter.toLowerCase())) {
                    filteredLogs.add(log);
                }
            }
            logs = filteredLogs;
        }
        
        return logs;
    }
    
    private String generateMockLogMessage(String level, int index) {
        switch (level) {
            case "DEBUG":
                return "Debug message " + index + " - Processing request";
            case "INFO":
                return "Info message " + index + " - Route processed successfully";
            case "WARN":
                return "Warning message " + index + " - High response time detected";
            case "ERROR":
                return "Error message " + index + " - Failed to process request";
            default:
                return "Log message " + index + " - General information";
        }
    }
}
