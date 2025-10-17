package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool definition
 */
public class McpTool {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("inputSchema")
    private Map<String, Object> inputSchema;
    
    public McpTool() {}
    
    public McpTool(String name, String description, Map<String, Object> inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }
    
    public void setInputSchema(Map<String, Object> inputSchema) {
        this.inputSchema = inputSchema;
    }
    
    /**
     * Create a JSON schema for tool input
     */
    public static Map<String, Object> createJsonSchema(String type, Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", type);
        schema.put("properties", properties != null ? properties : new HashMap<>());
        schema.put("required", required != null ? required : new ArrayList<>());
        return schema;
    }
    
    /**
     * Create a property definition for JSON schema
     */
    public static Map<String, Object> createProperty(String type, String description) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", type);
        property.put("description", description);
        return property;
    }
    
    /**
     * Create a property definition with enum values
     */
    public static Map<String, Object> createPropertyWithEnum(String type, String description, List<String> enumValues) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", type);
        property.put("description", description);
        property.put("enum", enumValues);
        return property;
    }
}
