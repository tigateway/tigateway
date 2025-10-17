package ti.gateway.ainative.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板模型
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("variables")
    private List<TemplateVariable> variables;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("created_at")
    private Long createdAt;
    
    @JsonProperty("updated_at")
    private Long updatedAt;

    /**
     * 模板变量
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateVariable {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("type")
        private String type; // string, number, boolean, object, array
        
        @JsonProperty("required")
        private Boolean required;
        
        @JsonProperty("default_value")
        private Object defaultValue;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("validation")
        private ValidationRule validation;
    }

    /**
     * 验证规则
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationRule {
        
        @JsonProperty("min_length")
        private Integer minLength;
        
        @JsonProperty("max_length")
        private Integer maxLength;
        
        @JsonProperty("pattern")
        private String pattern;
        
        @JsonProperty("min_value")
        private Number minValue;
        
        @JsonProperty("max_value")
        private Number maxValue;
        
        @JsonProperty("allowed_values")
        private List<Object> allowedValues;
    }
}
