package ti.gateway.operator.springcloudgateway.route;

public class RouteDefinitionException extends RuntimeException {
    public RouteDefinitionException() {
    }

    public RouteDefinitionException(String message) {
        super(message);
    }

    public RouteDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RouteDefinitionException(Throwable cause) {
        super(cause);
    }
}

