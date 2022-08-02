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
class SsoEnabledPathItemCustomizer implements PathItemCustomizer {
    private static final String SSO_LOGIN_FILTER = "SsoLogin";
    private static final String SCOPES_FILTER_KEY = "Scopes";
    private static final String ROLES_FILTER_KEY = "Roles";

    SsoEnabledPathItemCustomizer() {
    }

    public boolean supports(RouteDefinition routeDefinition, String path) {
        return routeDefinition.getFilters().contains("SsoLogin");
    }

    public void customize(PathItem pathItem, RouteDefinition routeDefinition, String path) {
        Iterator var4 = pathItem.readOperations().iterator();

        while(var4.hasNext()) {
            Operation operation = (Operation)var4.next();
            operation.responses(this.customizeResponses(operation.getResponses()));
        }

    }

    private ApiResponses customizeResponses(ApiResponses responses) {
        if (responses == null) {
            responses = new ApiResponses();
        }

        responses.putIfAbsent("302", (new ApiResponse()).addHeaderObject("Location", (new Header()).schema(new StringSchema())).description("Redirect client to SSO login page to authenticate for authorizing access."));
        return responses;
    }
}
