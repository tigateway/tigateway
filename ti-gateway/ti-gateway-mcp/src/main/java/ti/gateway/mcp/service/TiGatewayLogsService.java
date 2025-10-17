package ti.gateway.mcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public List<Map<String, Object>> getLogs(String level, Integer lines, String namespace, 
                                           String service, String filter) {
        logger.info("Getting logs - level: {} lines: {} namespace: {} service: {} filter: {}", 
                   level, lines, namespace, service, filter);
        
        // TODO: Implement actual log retrieval from Kubernetes or log aggregation system
        // This is a mock implementation
        List<Map<String, Object>> logs = new ArrayList<>();
        
        // Generate mock logs
        for (int i = 0; i < Math.min(lines, 10); i++) {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("timestamp", Instant.now().minusSeconds(i * 10).toString());
            logEntry.put("level", level);
            logEntry.put("namespace", namespace);
            logEntry.put("service", service.isEmpty() ? "tigateway" : service);
            logEntry.put("pod", "tigateway-" + (i % 3 + 1));
            logEntry.put("message", generateMockLogMessage(level, i));
            logEntry.put("thread", "http-nio-8080-exec-" + (i % 10 + 1));
            logEntry.put("logger", "ti.gateway.core.RouteHandler");
            
            logs.add(logEntry);
        }
        
        // Apply text filter if provided
        if (!filter.isEmpty()) {
            List<Map<String, Object>> filteredLogs = new ArrayList<>();
            for (Map<String, Object> log : logs) {
                if (log.get("message").toString().toLowerCase().contains(filter.toLowerCase())) {
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
