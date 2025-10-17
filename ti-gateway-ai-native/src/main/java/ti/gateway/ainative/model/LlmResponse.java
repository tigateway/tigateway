package ti.gateway.ainative.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * LLM响应模型
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("object")
    private String object;
    
    @JsonProperty("created")
    private Long created;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("choices")
    private List<Choice> choices;
    
    @JsonProperty("usage")
    private Usage usage;
    
    @JsonProperty("system_fingerprint")
    private String systemFingerprint;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("cache_key")
    private String cacheKey;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("response_time")
    private Long responseTime;
    
    @JsonProperty("provider")
    private String provider;

    /**
     * 选择项模型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        
        @JsonProperty("index")
        private Integer index;
        
        @JsonProperty("message")
        private Message message;
        
        @JsonProperty("logprobs")
        private Object logprobs;
        
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    /**
     * 消息模型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        
        @JsonProperty("role")
        private String role;
        
        @JsonProperty("content")
        private String content;
        
        @JsonProperty("function_call")
        private FunctionCall functionCall;
        
        @JsonProperty("tool_calls")
        private List<ToolCall> toolCalls;
    }

    /**
     * 函数调用模型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionCall {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("arguments")
        private String arguments;
    }

    /**
     * 工具调用模型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCall {
        
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("function")
        private FunctionCall function;
    }

    /**
     * 使用情况模型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
