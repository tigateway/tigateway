package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Service information model
 */
public class ServiceInfo {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("namespace")
    private String namespace;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("clusterIP")
    private String clusterIP;
    
    @JsonProperty("externalIPs")
    private java.util.List<String> externalIPs;
    
    @JsonProperty("ports")
    private java.util.List<ServicePort> ports;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("created")
    private String created;
    
    @JsonProperty("labels")
    private java.util.Map<String, String> labels;
    
    @JsonProperty("annotations")
    private java.util.Map<String, String> annotations;
    
    public ServiceInfo() {}
    
    public ServiceInfo(String name, String namespace, String type, String status) {
        this.name = name;
        this.namespace = namespace;
        this.type = type;
        this.status = status;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getClusterIP() {
        return clusterIP;
    }
    
    public void setClusterIP(String clusterIP) {
        this.clusterIP = clusterIP;
    }
    
    public java.util.List<String> getExternalIPs() {
        return externalIPs;
    }
    
    public void setExternalIPs(java.util.List<String> externalIPs) {
        this.externalIPs = externalIPs;
    }
    
    public java.util.List<ServicePort> getPorts() {
        return ports;
    }
    
    public void setPorts(java.util.List<ServicePort> ports) {
        this.ports = ports;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCreated() {
        return created;
    }
    
    public void setCreated(String created) {
        this.created = created;
    }
    
    public java.util.Map<String, String> getLabels() {
        return labels;
    }
    
    public void setLabels(java.util.Map<String, String> labels) {
        this.labels = labels;
    }
    
    public java.util.Map<String, String> getAnnotations() {
        return annotations;
    }
    
    public void setAnnotations(java.util.Map<String, String> annotations) {
        this.annotations = annotations;
    }
    
    /**
     * Service port model
     */
    public static class ServicePort {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("port")
        private Integer port;
        
        @JsonProperty("targetPort")
        private String targetPort;
        
        @JsonProperty("protocol")
        private String protocol;
        
        public ServicePort() {}
        
        public ServicePort(String name, Integer port, String targetPort, String protocol) {
            this.name = name;
            this.port = port;
            this.targetPort = targetPort;
            this.protocol = protocol;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Integer getPort() {
            return port;
        }
        
        public void setPort(Integer port) {
            this.port = port;
        }
        
        public String getTargetPort() {
            return targetPort;
        }
        
        public void setTargetPort(String targetPort) {
            this.targetPort = targetPort;
        }
        
        public String getProtocol() {
            return protocol;
        }
        
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }
    }
}
