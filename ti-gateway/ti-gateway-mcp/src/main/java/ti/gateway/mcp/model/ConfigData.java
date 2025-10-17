package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Configuration data model
 */
public class ConfigData {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("namespace")
    private String namespace;
    
    @JsonProperty("format")
    private String format;
    
    @JsonProperty("routes")
    private List<ConfigItem> routes;
    
    @JsonProperty("filters")
    private List<ConfigItem> filters;
    
    @JsonProperty("settings")
    private Map<String, Object> settings;
    
    @JsonProperty("count")
    private Integer count;
    
    public ConfigData() {}
    
    public ConfigData(String type, String namespace, String format) {
        this.type = type;
        this.namespace = namespace;
        this.format = format;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public List<ConfigItem> getRoutes() {
        return routes;
    }
    
    public void setRoutes(List<ConfigItem> routes) {
        this.routes = routes;
    }
    
    public List<ConfigItem> getFilters() {
        return filters;
    }
    
    public void setFilters(List<ConfigItem> filters) {
        this.filters = filters;
    }
    
    public Map<String, Object> getSettings() {
        return settings;
    }
    
    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
}
