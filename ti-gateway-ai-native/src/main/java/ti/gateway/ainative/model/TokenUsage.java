package ti.gateway.ainative.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Token使用量模型
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsage {
    
    @JsonProperty("estimated_tokens")
    private Integer estimatedTokens;
    
    @JsonProperty("actual_tokens")
    private Integer actualTokens;
    
    @JsonProperty("prompt_tokens")
    private Integer promptTokens;
    
    @JsonProperty("completion_tokens")
    private Integer completionTokens;
    
    @JsonProperty("total_tokens")
    private Integer totalTokens;
    
    @JsonProperty("request_type")
    private String requestType; // completion, chat, embedding, etc.
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("cost")
    private Double cost;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    /**
     * 创建Token使用量记录
     */
    public static TokenUsage create(Integer promptTokens, Integer completionTokens, String model, String userId) {
        TokenUsage usage = new TokenUsage();
        usage.setPromptTokens(promptTokens);
        usage.setCompletionTokens(completionTokens);
        usage.setTotalTokens(promptTokens + completionTokens);
        usage.setModel(model);
        usage.setUserId(userId);
        usage.setTimestamp(System.currentTimeMillis());
        return usage;
    }

    /**
     * 创建估算的Token使用量
     */
    public static TokenUsage estimate(Integer estimatedTokens, String requestType, String model, String userId) {
        TokenUsage usage = new TokenUsage();
        usage.setEstimatedTokens(estimatedTokens);
        usage.setRequestType(requestType);
        usage.setModel(model);
        usage.setUserId(userId);
        usage.setTimestamp(System.currentTimeMillis());
        return usage;
    }
}
