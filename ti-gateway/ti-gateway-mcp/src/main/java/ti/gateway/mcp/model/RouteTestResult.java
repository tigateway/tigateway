package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Route test result model
 */
public class RouteTestResult {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("statusCode")
    private Integer statusCode;
    
    @JsonProperty("responseTime")
    private String responseTime;
    
    @JsonProperty("headers")
    private Map<String, String> headers;
    
    @JsonProperty("body")
    private String body;
    
    @JsonProperty("route")
    private String route;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("method")
    private String method;
    
    public RouteTestResult() {}
    
    public RouteTestResult(String status, Integer statusCode, String responseTime) {
        this.status = status;
        this.statusCode = statusCode;
        this.responseTime = responseTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public String getRoute() {
        return route;
    }
    
    public void setRoute(String route) {
        this.route = route;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
}
