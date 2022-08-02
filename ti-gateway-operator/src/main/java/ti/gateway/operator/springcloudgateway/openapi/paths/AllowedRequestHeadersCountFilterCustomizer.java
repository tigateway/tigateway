package ti.gateway.operator.springcloudgateway.openapi.paths;

import ti.gateway.operator.springcloudgateway.openapi.PathItemCustomizer;
import ti.gateway.operator.springcloudgateway.route.RouteDefinition;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.Iterator;
import org.springframework.stereotype.Component;

@Component
public class AllowedRequestHeadersCountFilterCustomizer implements PathItemCustomizer {
    private static final String ALLOWED_REQUEST_HEADERS_COUNT_FILTER = "AllowedRequestHeadersCount";

    public AllowedRequestHeadersCountFilterCustomizer() {
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
        String maxNumberOfHeaders = ((String)routeDefinition.getFilters().stream().filter((f) -> {
            return f.startsWith("AllowedRequestHeadersCount");
        }).findFirst().get()).split("=")[1];
        ApiResponse tooManyReqResponse = (ApiResponse)responses.computeIfAbsent("431", (k) -> {
            return (new ApiResponse()).description("Request exceeded the maximum number of allowed headers: " + maxNumberOfHeaders);
        });
        tooManyReqResponse.addHeaderObject("errorMessage", (new Header()).schema(new StringSchema()));
        return responses;
    }

    public boolean supports(RouteDefinition routeDefinition, String path) {
        return routeDefinition.getFilters().stream().anyMatch((s) -> {
            return s.startsWith("AllowedRequestHeadersCount=");
        });
    }
}

