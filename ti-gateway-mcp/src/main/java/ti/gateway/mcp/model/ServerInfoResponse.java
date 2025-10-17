package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Server information response model
 */
public class ServerInfoResponse {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("protocolVersion")
    private String protocolVersion;
    
    @JsonProperty("port")
    private Integer port;
    
    @JsonProperty("independent")
    private Boolean independent;
    
    @JsonProperty("capabilities")
    private Capabilities capabilities;
    
    public ServerInfoResponse() {}
    
    public ServerInfoResponse(String name, String version, String description, String protocolVersion) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.protocolVersion = protocolVersion;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getProtocolVersion() {
        return protocolVersion;
    }
    
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public Boolean getIndependent() {
        return independent;
    }
    
    public void setIndependent(Boolean independent) {
        this.independent = independent;
    }
    
    public Capabilities getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
    }
    
    /**
     * Server capabilities model
     */
    public static class Capabilities {
        
        @JsonProperty("tools")
        private ToolsCapability tools;
        
        @JsonProperty("resources")
        private ResourcesCapability resources;
        
        public Capabilities() {}
        
        public Capabilities(ToolsCapability tools, ResourcesCapability resources) {
            this.tools = tools;
            this.resources = resources;
        }
        
        public ToolsCapability getTools() {
            return tools;
        }
        
        public void setTools(ToolsCapability tools) {
            this.tools = tools;
        }
        
        public ResourcesCapability getResources() {
            return resources;
        }
        
        public void setResources(ResourcesCapability resources) {
            this.resources = resources;
        }
    }
    
    /**
     * Tools capability model
     */
    public static class ToolsCapability {
        
        @JsonProperty("listChanged")
        private boolean listChanged;
        
        public ToolsCapability() {}
        
        public ToolsCapability(boolean listChanged) {
            this.listChanged = listChanged;
        }
        
        public boolean isListChanged() {
            return listChanged;
        }
        
        public void setListChanged(boolean listChanged) {
            this.listChanged = listChanged;
        }
    }
    
    /**
     * Resources capability model
     */
    public static class ResourcesCapability {
        
        @JsonProperty("subscribe")
        private boolean subscribe;
        
        @JsonProperty("listChanged")
        private boolean listChanged;
        
        public ResourcesCapability() {}
        
        public ResourcesCapability(boolean subscribe, boolean listChanged) {
            this.subscribe = subscribe;
            this.listChanged = listChanged;
        }
        
        public boolean isSubscribe() {
            return subscribe;
        }
        
        public void setSubscribe(boolean subscribe) {
            this.subscribe = subscribe;
        }
        
        public boolean isListChanged() {
            return listChanged;
        }
        
        public void setListChanged(boolean listChanged) {
            this.listChanged = listChanged;
        }
    }
}
