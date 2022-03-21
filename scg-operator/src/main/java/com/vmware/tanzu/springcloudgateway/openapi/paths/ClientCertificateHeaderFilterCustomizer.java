package com.vmware.tanzu.springcloudgateway.openapi.paths;

import com.vmware.tanzu.springcloudgateway.openapi.PathItemCustomizer;
import com.vmware.tanzu.springcloudgateway.route.RouteDefinition;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.Iterator;
import org.springframework.stereotype.Component;

@Component
public class ClientCertificateHeaderFilterCustomizer implements PathItemCustomizer {
    private static final String FILTER_NAME = "ClientCertificateHeader";

    public ClientCertificateHeaderFilterCustomizer() {
    }

    public void customize(PathItem pathItem, RouteDefinition routeDefinition, String path) {
        this.customizeRequestHeaders(pathItem);
        Iterator var4 = pathItem.readOperations().iterator();

        while(var4.hasNext()) {
            Operation operation = (Operation)var4.next();
            operation.responses(this.customizeResponses(routeDefinition, operation.getResponses()));
        }

    }

    private void customizeRequestHeaders(PathItem pathItem) {
        Parameter parametersItem = new Parameter();
        parametersItem.setIn("header");
        parametersItem.setRequired(true);
        parametersItem.setName("X-Forwarded-Client-Cert");
        pathItem.addParametersItem(parametersItem);
    }

    private ApiResponses customizeResponses(RouteDefinition routeDefinition, ApiResponses responses) {
        if (responses == null) {
            responses = new ApiResponses();
        }

        String filter = (String)routeDefinition.getFilters().stream().filter((s) -> {
            return s.startsWith("ClientCertificateHeader=");
        }).findFirst().orElseThrow();
        responses.putIfAbsent("200", (new ApiResponse()).description("Ok"));
        responses.putIfAbsent("401", (new ApiResponse()).description("Client certificate via X-Forwarded-Client-Cert header is required. " + filter));
        return responses;
    }

    public boolean supports(RouteDefinition routeDefinition, String path) {
        return routeDefinition.getFilters().stream().anyMatch((s) -> {
            return s.startsWith("ClientCertificateHeader=");
        });
    }
}
