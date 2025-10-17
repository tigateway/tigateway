package ti.gateway.ainative.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * LLM请求模型
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmRequest {
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("messages")
    private List<Message> messages;
    
    @JsonProperty("temperature")
    private Double temperature;
    
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    @JsonProperty("top_p")
    private Double topP;
    
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;
    
    @JsonProperty("presence_penalty")
    private Double presencePenalty;
    
    @JsonProperty("stream")
    private Boolean stream;
    
    @JsonProperty("user")
    private String user;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("request_id")
    private String requestId;
    
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * 消息模型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        
        @JsonProperty("role")
        private String role; // system, user, assistant
        
        @JsonProperty("content")
        private String content;
        
        @JsonProperty("name")
        private String name;
        
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
}
