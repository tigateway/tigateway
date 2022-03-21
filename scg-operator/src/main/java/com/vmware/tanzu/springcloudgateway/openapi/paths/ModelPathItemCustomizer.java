package com.vmware.tanzu.springcloudgateway.openapi.paths;

import com.vmware.tanzu.springcloudgateway.openapi.PathItemCustomizer;
import com.vmware.tanzu.springcloudgateway.route.RouteDefinition;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
class ModelPathItemCustomizer implements PathItemCustomizer {
    ModelPathItemCustomizer() {
    }

    public void customize(PathItem pathItem, RouteDefinition routeDefinition, String path) {
        RequestBody requestBody = routeDefinition.getRequestBody();
        ApiResponses responses = routeDefinition.getResponses();
        if (requestBody != null) {
            Operation postOp = pathItem.getPost();
            if (postOp != null) {
                postOp.setRequestBody(requestBody);
            }

            Operation putOp = pathItem.getPut();
            if (putOp != null) {
                putOp.setRequestBody(requestBody);
            }

            Operation patchOp = pathItem.getPatch();
            if (patchOp != null) {
                patchOp.setRequestBody(requestBody);
            }
        }

        if (responses != null) {
            pathItem.readOperations().forEach((operation) -> {
                operation.setResponses(responses);
            });
        }

    }

    public boolean supports(RouteDefinition routeDefinition, String path) {
        return routeDefinition.getRequestBody() != null || routeDefinition.getResponses() != null;
    }
}
