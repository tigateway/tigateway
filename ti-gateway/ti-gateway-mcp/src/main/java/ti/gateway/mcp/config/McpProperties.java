package ti.gateway.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for MCP server
 */
@ConfigurationProperties(prefix = "tigateway.mcp")
public class McpProperties {
    
    /**
     * Enable MCP server
     */
    private boolean enabled = true;
    
    /**
     * MCP server port
     */
    private int port = 8082;
    
    /**
     * MCP server context path
     */
    private String contextPath = "/mcp";
    
    /**
     * Allowed origins for CORS
     */
    private List<String> allowedOrigins = List.of("*");
    
    /**
     * Enable authentication
     */
    private boolean authenticationEnabled = false;
    
    /**
     * Authentication token
     */
    private String authToken;
    
    /**
     * Kubernetes configuration
     */
    private Kubernetes kubernetes = new Kubernetes();
    
    /**
     * Metrics configuration
     */
    private Metrics metrics = new Metrics();
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getContextPath() {
        return contextPath;
    }
    
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
    
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
    
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
    
    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }
    
    public void setAuthenticationEnabled(boolean authenticationEnabled) {
        this.authenticationEnabled = authenticationEnabled;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public Kubernetes getKubernetes() {
        return kubernetes;
    }
    
    public void setKubernetes(Kubernetes kubernetes) {
        this.kubernetes = kubernetes;
    }
    
    public Metrics getMetrics() {
        return metrics;
    }
    
    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }
    
    public static class Kubernetes {
        
        /**
         * Kubernetes namespace
         */
        private String namespace = "default";
        
        /**
         * Kubernetes API server URL
         */
        private String apiServerUrl;
        
        /**
         * Service account token path
         */
        private String serviceAccountTokenPath = "/var/run/secrets/kubernetes.io/serviceaccount/token";
        
        /**
         * CA certificate path
         */
        private String caCertPath = "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt";
        
        public String getNamespace() {
            return namespace;
        }
        
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
        
        public String getApiServerUrl() {
            return apiServerUrl;
        }
        
        public void setApiServerUrl(String apiServerUrl) {
            this.apiServerUrl = apiServerUrl;
        }
        
        public String getServiceAccountTokenPath() {
            return serviceAccountTokenPath;
        }
        
        public void setServiceAccountTokenPath(String serviceAccountTokenPath) {
            this.serviceAccountTokenPath = serviceAccountTokenPath;
        }
        
        public String getCaCertPath() {
            return caCertPath;
        }
        
        public void setCaCertPath(String caCertPath) {
            this.caCertPath = caCertPath;
        }
    }
    
    public static class Metrics {
        
        /**
         * Enable metrics collection
         */
        private boolean enabled = true;
        
        /**
         * Metrics collection interval in seconds
         */
        private int collectionInterval = 30;
        
        /**
         * Metrics retention period in hours
         */
        private int retentionPeriod = 24;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getCollectionInterval() {
            return collectionInterval;
        }
        
        public void setCollectionInterval(int collectionInterval) {
            this.collectionInterval = collectionInterval;
        }
        
        public int getRetentionPeriod() {
            return retentionPeriod;
        }
        
        public void setRetentionPeriod(int retentionPeriod) {
            this.retentionPeriod = retentionPeriod;
        }
    }
}
