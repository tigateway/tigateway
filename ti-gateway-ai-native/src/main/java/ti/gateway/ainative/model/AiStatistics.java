package ti.gateway.ainative.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI统计模型
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiStatistics {
    
    @JsonProperty("request_id")
    private String requestId;
    
    @JsonProperty("start_time")
    private Long startTime;
    
    @JsonProperty("end_time")
    private Long endTime;
    
    @JsonProperty("duration")
    private Long duration; // milliseconds
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("status_code")
    private Integer statusCode;
    
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("error_message")
    private String errorMessage;
    
    @JsonProperty("user_agent")
    private String userAgent;
    
    @JsonProperty("client_ip")
    private String clientIp;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("provider")
    private String provider;
    
    @JsonProperty("token_usage")
    private TokenUsage tokenUsage;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("timestamp")
    private Long timestamp;
}
