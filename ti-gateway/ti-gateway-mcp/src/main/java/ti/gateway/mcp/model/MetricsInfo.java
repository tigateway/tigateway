package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metrics information model
 */
public class MetricsInfo {
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    @JsonProperty("service")
    private String service;
    
    @JsonProperty("namespace")
    private String namespace;
    
    @JsonProperty("metrics")
    private java.util.Map<String, Object> metrics;
    
    @JsonProperty("summary")
    private MetricsSummary summary;
    
    public MetricsInfo() {}
    
    public MetricsInfo(long timestamp, String service, String namespace) {
        this.timestamp = timestamp;
        this.service = service;
        this.namespace = namespace;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getService() {
        return service;
    }
    
    public void setService(String service) {
        this.service = service;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public java.util.Map<String, Object> getMetrics() {
        return metrics;
    }
    
    public void setMetrics(java.util.Map<String, Object> metrics) {
        this.metrics = metrics;
    }
    
    public MetricsSummary getSummary() {
        return summary;
    }
    
    public void setSummary(MetricsSummary summary) {
        this.summary = summary;
    }
    
    /**
     * Metrics summary model
     */
    public static class MetricsSummary {
        
        @JsonProperty("totalRequests")
        private long totalRequests;
        
        @JsonProperty("successfulRequests")
        private long successfulRequests;
        
        @JsonProperty("failedRequests")
        private long failedRequests;
        
        @JsonProperty("averageResponseTime")
        private double averageResponseTime;
        
        @JsonProperty("errorRate")
        private double errorRate;
        
        @JsonProperty("throughput")
        private double throughput;
        
        public MetricsSummary() {}
        
        public MetricsSummary(long totalRequests, long successfulRequests, long failedRequests, 
                            double averageResponseTime, double errorRate, double throughput) {
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.averageResponseTime = averageResponseTime;
            this.errorRate = errorRate;
            this.throughput = throughput;
        }
        
        public long getTotalRequests() {
            return totalRequests;
        }
        
        public void setTotalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
        }
        
        public long getSuccessfulRequests() {
            return successfulRequests;
        }
        
        public void setSuccessfulRequests(long successfulRequests) {
            this.successfulRequests = successfulRequests;
        }
        
        public long getFailedRequests() {
            return failedRequests;
        }
        
        public void setFailedRequests(long failedRequests) {
            this.failedRequests = failedRequests;
        }
        
        public double getAverageResponseTime() {
            return averageResponseTime;
        }
        
        public void setAverageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
        }
        
        public double getErrorRate() {
            return errorRate;
        }
        
        public void setErrorRate(double errorRate) {
            this.errorRate = errorRate;
        }
        
        public double getThroughput() {
            return throughput;
        }
        
        public void setThroughput(double throughput) {
            this.throughput = throughput;
        }
    }
}
