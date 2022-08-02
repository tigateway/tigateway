package ti.gateway.operator.springcloudgateway.openapi.paths;

import ti.gateway.operator.springcloudgateway.openapi.PathItemCustomizer;
import ti.gateway.operator.springcloudgateway.route.RouteDefinition;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.Iterator;
import org.springframework.stereotype.Component;

@Component
public class RateLimitFilterCustomizer implements PathItemCustomizer {
    private static final String RATE_LIMIT_FILTER_NAME = "RateLimit";
    private static final Schema<?> INT_SCHEMA = (new Schema()).type("integer");

    public RateLimitFilterCustomizer() {
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

        String filter = (String)routeDefinition.getFilters().stream().filter((s) -> {
            return s.startsWith("RateLimit=");
        }).findFirst().orElse("");
        ApiResponse okResponse = (ApiResponse)responses.computeIfAbsent("200", (k) -> {
            return (new ApiResponse()).description("Ok");
        });
        okResponse.addHeaderObject("X-Remaining", (new Header()).schema(INT_SCHEMA).description("RateLimit: number of requests remaining"));
        ApiResponse tooManyReqResponse = (ApiResponse)responses.computeIfAbsent("429", (k) -> {
            return (new ApiResponse()).description("Too Many Requests. " + filter);
        });
        tooManyReqResponse.addHeaderObject("X-Retry-In", (new Header()).schema(INT_SCHEMA).description("RateLimit: time in milliseconds until retry"));
        return responses;
    }

    public boolean supports(RouteDefinition routeDefinition, String path) {
        return routeDefinition.getFilters().stream().anyMatch((s) -> {
            return s.startsWith("RateLimit=");
        });
    }
}
