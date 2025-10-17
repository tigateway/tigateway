package ti.gateway.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Route operation response model
 */
public class RouteResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private List<RouteInfo> data;
    
    @JsonProperty("count")
    private Integer count;
    
    @JsonProperty("namespace")
    private String namespace;
    
    @JsonProperty("route")
    private RouteInfo route;
    
    @JsonProperty("routeName")
    private String routeName;
    
    @JsonProperty("updates")
    private java.util.Map<String, Object> updates;
    
    @JsonProperty("testResult")
    private RouteTestResult testResult;
    
    public RouteResponse() {}
    
    public RouteResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public static RouteResponse listSuccess(List<RouteInfo> routes, String namespace) {
        RouteResponse response = new RouteResponse(true, "Routes retrieved successfully");
        response.setData(routes);
        response.setCount(routes.size());
        response.setNamespace(namespace);
        return response;
    }
    
    public static RouteResponse createSuccess(RouteInfo route) {
        RouteResponse response = new RouteResponse(true, "Route created successfully");
        response.setRoute(route);
        return response;
    }
    
    public static RouteResponse createError(String message) {
        return new RouteResponse(false, message);
    }
    
    public static RouteResponse updateSuccess(String routeName, java.util.Map<String, Object> updates) {
        RouteResponse response = new RouteResponse(true, "Route updated successfully");
        response.setRouteName(routeName);
        response.setUpdates(updates);
        return response;
    }
    
    public static RouteResponse updateError(String message) {
        return new RouteResponse(false, message);
    }
    
    public static RouteResponse deleteSuccess(String routeName) {
        RouteResponse response = new RouteResponse(true, "Route deleted successfully");
        response.setRouteName(routeName);
        return response;
    }
    
    public static RouteResponse deleteError(String message) {
        return new RouteResponse(false, message);
    }
    
    public static RouteResponse testSuccess(RouteTestResult testResult) {
        RouteResponse response = new RouteResponse(true, "Route test completed");
        response.setTestResult(testResult);
        return response;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<RouteInfo> getData() {
        return data;
    }
    
    public void setData(List<RouteInfo> data) {
        this.data = data;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public RouteInfo getRoute() {
        return route;
    }
    
    public void setRoute(RouteInfo route) {
        this.route = route;
    }
    
    public String getRouteName() {
        return routeName;
    }
    
    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }
    
    public java.util.Map<String, Object> getUpdates() {
        return updates;
    }
    
    public void setUpdates(java.util.Map<String, Object> updates) {
        this.updates = updates;
    }
    
    public RouteTestResult getTestResult() {
        return testResult;
    }
    
    public void setTestResult(RouteTestResult testResult) {
        this.testResult = testResult;
    }
}
