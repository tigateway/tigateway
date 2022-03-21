package com.vmware.tanzu.springcloudgateway.openapi.paths;

import com.vmware.tanzu.springcloudgateway.openapi.PathItemCustomizer;
import com.vmware.tanzu.springcloudgateway.route.RouteDefinition;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.Iterator;
import org.springframework.stereotype.Component;

@Component
public class AllowedRequestCookieCountFilterCustomizer implements PathItemCustomizer {
    private static final String ALLOWED_REQUEST_COOKIE_COUNT_FILTER = "AllowedRequestCookieCount";

    public AllowedRequestCookieCountFilterCustomizer() {
    }

    public void customize(PathItem pathItem, RouteDefinition routeDefinition, String path) {
        Iterator var4 = pathItem.readOperations().iterator();

        while(var4.hasNext()) {
            Operation operation = (Operation)var4.next();
            operation.responses(this.customizeResponses(routeDefinition, operation.getResponses()));
        }

    }

    private ApiResponses customizeResponses(RouteDefinition routeDefinition, ApiResponses responses) {
        if (responses == null) {
            responses = new ApiResponses();
        }

        responses.computeIfAbsent("200", (k) -> {
            return (new ApiResponse()).description("Ok");
        });
        String maxNumberOfCookies = ((String)routeDefinition.getFilters().stream().filter((f) -> {
            return f.startsWith("AllowedRequestCookieCount");
        }).findFirst().get()).split("=")[1];
        ApiResponse tooManyReqResponse = (ApiResponse)responses.computeIfAbsent("431", (k) -> {
            return (new ApiResponse()).description("Request exceeded the maximum number of cookies: " + maxNumberOfCookies);
        });
        tooManyReqResponse.addHeaderObject("errorMessage", (new Header()).schema(new StringSchema()));
        return responses;
    }

    public boolean supports(RouteDefinition routeDefinition, String path) {
        return routeDefinition.getFilters().stream().anyMatch((s) -> {
            return s.startsWith("AllowedRequestCookieCount=");
        });
    }
}

