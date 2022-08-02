package ti.gateway.operator.springcloudgateway.openapi;

import ti.gateway.operator.springcloudgateway.route.RouteDefinition;
import io.swagger.v3.oas.models.PathItem;

public interface PathItemCustomizer {
    void customize(PathItem pathItem, RouteDefinition routeDefinition, String path);

    boolean supports(RouteDefinition routeDefinition, String path);
}
