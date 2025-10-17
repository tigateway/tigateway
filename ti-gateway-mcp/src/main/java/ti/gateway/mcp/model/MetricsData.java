package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Metrics data model
 */
public class MetricsData {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("timeRange")
    private String timeRange;
    
    @JsonProperty("namespace")
    private String namespace;
    
    // Route metrics
    @JsonProperty("totalRoutes")
    private Integer totalRoutes;
    
    @JsonProperty("activeRoutes")
    private Integer activeRoutes;
    
    @JsonProperty("inactiveRoutes")
    private Integer inactiveRoutes;
    
    @JsonProperty("routesByService")
    private Map<String, Integer> routesByService;
    
    // Service metrics
    @JsonProperty("totalServices")
    private Integer totalServices;
    
    @JsonProperty("healthyServices")
    private Integer healthyServices;
    
    @JsonProperty("unhealthyServices")
    private Integer unhealthyServices;
    
    @JsonProperty("serviceStatus")
    private Map<String, String> serviceStatus;
    
    // Request metrics
    @JsonProperty("totalRequests")
    private Long totalRequests;
    
    @JsonProperty("successfulRequests")
    private Long successfulRequests;
    
    @JsonProperty("failedRequests")
    private Long failedRequests;
    
    @JsonProperty("requestsPerSecond")
    private Double requestsPerSecond;
    
    @JsonProperty("averageResponseTime")
    private String averageResponseTime;
    
    @JsonProperty("p95ResponseTime")
    private String p95ResponseTime;
    
    @JsonProperty("p99ResponseTime")
    private String p99ResponseTime;
    
    // Error metrics
    @JsonProperty("totalErrors")
    private Long totalErrors;
    
    @JsonProperty("errorRate")
    private String errorRate;
    
    @JsonProperty("errorsByType")
    private Map<String, Integer> errorsByType;
    
    @JsonProperty("errorsByService")
    private Map<String, Integer> errorsByService;
    
    // Performance metrics
    @JsonProperty("cpuUsage")
    private String cpuUsage;
    
    @JsonProperty("memoryUsage")
    private String memoryUsage;
    
    @JsonProperty("diskUsage")
    private String diskUsage;
    
    @JsonProperty("networkThroughput")
    private String networkThroughput;
    
    @JsonProperty("activeConnections")
    private Integer activeConnections;
    
    @JsonProperty("connectionPoolUtilization")
    private String connectionPoolUtilization;
    
    public MetricsData() {}
    
    public MetricsData(String type, String timeRange, String namespace) {
        this.type = type;
        this.timeRange = timeRange;
        this.namespace = namespace;
    }
    
    // Getters and setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getTimeRange() {
        return timeRange;
    }
    
    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public Integer getTotalRoutes() {
        return totalRoutes;
    }
    
    public void setTotalRoutes(Integer totalRoutes) {
        this.totalRoutes = totalRoutes;
    }
    
    public Integer getActiveRoutes() {
        return activeRoutes;
    }
    
    public void setActiveRoutes(Integer activeRoutes) {
        this.activeRoutes = activeRoutes;
    }
    
    public Integer getInactiveRoutes() {
        return inactiveRoutes;
    }
    
    public void setInactiveRoutes(Integer inactiveRoutes) {
        this.inactiveRoutes = inactiveRoutes;
    }
    
    public Map<String, Integer> getRoutesByService() {
        return routesByService;
    }
    
    public void setRoutesByService(Map<String, Integer> routesByService) {
        this.routesByService = routesByService;
    }
    
    public Integer getTotalServices() {
        return totalServices;
    }
    
    public void setTotalServices(Integer totalServices) {
        this.totalServices = totalServices;
    }
    
    public Integer getHealthyServices() {
        return healthyServices;
    }
    
    public void setHealthyServices(Integer healthyServices) {
        this.healthyServices = healthyServices;
    }
    
    public Integer getUnhealthyServices() {
        return unhealthyServices;
    }
    
    public void setUnhealthyServices(Integer unhealthyServices) {
        this.unhealthyServices = unhealthyServices;
    }
    
    public Map<String, String> getServiceStatus() {
        return serviceStatus;
    }
    
    public void setServiceStatus(Map<String, String> serviceStatus) {
        this.serviceStatus = serviceStatus;
    }
    
    public Long getTotalRequests() {
        return totalRequests;
    }
    
    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }
    
    public Long getSuccessfulRequests() {
        return successfulRequests;
    }
    
    public void setSuccessfulRequests(Long successfulRequests) {
        this.successfulRequests = successfulRequests;
    }
    
    public Long getFailedRequests() {
        return failedRequests;
    }
    
    public void setFailedRequests(Long failedRequests) {
        this.failedRequests = failedRequests;
    }
    
    public Double getRequestsPerSecond() {
        return requestsPerSecond;
    }
    
    public void setRequestsPerSecond(Double requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }
    
    public String getAverageResponseTime() {
        return averageResponseTime;
    }
    
    public void setAverageResponseTime(String averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }
    
    public String getP95ResponseTime() {
        return p95ResponseTime;
    }
    
    public void setP95ResponseTime(String p95ResponseTime) {
        this.p95ResponseTime = p95ResponseTime;
    }
    
    public String getP99ResponseTime() {
        return p99ResponseTime;
    }
    
    public void setP99ResponseTime(String p99ResponseTime) {
        this.p99ResponseTime = p99ResponseTime;
    }
    
    public Long getTotalErrors() {
        return totalErrors;
    }
    
    public void setTotalErrors(Long totalErrors) {
        this.totalErrors = totalErrors;
    }
    
    public String getErrorRate() {
        return errorRate;
    }
    
    public void setErrorRate(String errorRate) {
        this.errorRate = errorRate;
    }
    
    public Map<String, Integer> getErrorsByType() {
        return errorsByType;
    }
    
    public void setErrorsByType(Map<String, Integer> errorsByType) {
        this.errorsByType = errorsByType;
    }
    
    public Map<String, Integer> getErrorsByService() {
        return errorsByService;
    }
    
    public void setErrorsByService(Map<String, Integer> errorsByService) {
        this.errorsByService = errorsByService;
    }
    
    public String getCpuUsage() {
        return cpuUsage;
    }
    
    public void setCpuUsage(String cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
    
    public String getMemoryUsage() {
        return memoryUsage;
    }
    
    public void setMemoryUsage(String memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
    
    public String getDiskUsage() {
        return diskUsage;
    }
    
    public void setDiskUsage(String diskUsage) {
        this.diskUsage = diskUsage;
    }
    
    public String getNetworkThroughput() {
        return networkThroughput;
    }
    
    public void setNetworkThroughput(String networkThroughput) {
        this.networkThroughput = networkThroughput;
    }
    
    public Integer getActiveConnections() {
        return activeConnections;
    }
    
    public void setActiveConnections(Integer activeConnections) {
        this.activeConnections = activeConnections;
    }
    
    public String getConnectionPoolUtilization() {
        return connectionPoolUtilization;
    }
    
    public void setConnectionPoolUtilization(String connectionPoolUtilization) {
        this.connectionPoolUtilization = connectionPoolUtilization;
    }
}
