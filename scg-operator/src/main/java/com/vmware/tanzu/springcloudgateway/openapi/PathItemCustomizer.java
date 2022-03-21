package com.vmware.tanzu.springcloudgateway.openapi;

import com.vmware.tanzu.springcloudgateway.route.RouteDefinition;
import io.swagger.v3.oas.models.PathItem;

public interface PathItemCustomizer {
    void customize(PathItem pathItem, RouteDefinition routeDefinition, String path);

    boolean supports(RouteDefinition routeDefinition, String path);
}
