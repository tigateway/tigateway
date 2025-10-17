package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Initialize request for MCP protocol
 */
public class InitializeRequest extends McpRequest {
    
    @JsonProperty("params")
    private InitializeParams params;
    
    public InitializeRequest() {
        super();
    }
    
    public InitializeRequest(String id) {
        super(id, "initialize");
    }
    
    public InitializeParams getInitializeParams() {
        return params;
    }
    
    public void setInitializeParams(InitializeParams params) {
        this.params = params;
    }
    
    @Override
    public Map<String, Object> getParams() {
        // Convert InitializeParams to Map for compatibility
        if (params == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("protocolVersion", params.getProtocolVersion());
        map.put("capabilities", params.getCapabilities());
        map.put("clientInfo", params.getClientInfo());
        return map;
    }
    
    public static class InitializeParams {
        
        @JsonProperty("protocolVersion")
        private String protocolVersion;
        
        @JsonProperty("capabilities")
        private Map<String, Object> capabilities;
        
        @JsonProperty("clientInfo")
        private ClientInfo clientInfo;
        
        public InitializeParams() {}
        
        public String getProtocolVersion() {
            return protocolVersion;
        }
        
        public void setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
        }
        
        public Map<String, Object> getCapabilities() {
            return capabilities;
        }
        
        public void setCapabilities(Map<String, Object> capabilities) {
            this.capabilities = capabilities;
        }
        
        public ClientInfo getClientInfo() {
            return clientInfo;
        }
        
        public void setClientInfo(ClientInfo clientInfo) {
            this.clientInfo = clientInfo;
        }
    }
    
    public static class ClientInfo {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("version")
        private String version;
        
        public ClientInfo() {}
        
        public ClientInfo(String name, String version) {
            this.name = name;
            this.version = version;
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
    }
}
