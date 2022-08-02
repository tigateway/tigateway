package ti.gateway.operator.springcloudgateway.openapi.paths;

import ti.gateway.operator.springcloudgateway.openapi.PathItemCustomizer;
import ti.gateway.operator.springcloudgateway.route.RouteDefinition;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
@Order(-2147483648)
class HttpMethodPathItemCustomizer implements PathItemCustomizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMethodPathItemCustomizer.class);
    private static final List<String> DEFAULT_TAGS = Collections.singletonList("default");

    HttpMethodPathItemCustomizer() {
    }

    public void customize(PathItem pathItem, RouteDefinition routeDefinition, String path) {
        String method = routeDefinition.getMethod();
        if (!StringUtils.hasLength(method)) {
            pathItem.setGet(this.buildOperation(routeDefinition));
            pathItem.setPut(this.buildOperation(routeDefinition));
            pathItem.setPost(this.buildOperation(routeDefinition));
            pathItem.setDelete(this.buildOperation(routeDefinition));
        } else {
            String[] var5 = method.toUpperCase().split(",");
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String m = var5[var7];

                try {
                    HttpMethod httpMethod = HttpMethod.valueOf(m);
                    pathItem.operation(httpMethod, this.buildOperation(routeDefinition));
                } catch (IllegalArgumentException var10) {
                    LOGGER.error(String.format("Unable to build path item for openapi: Unknown http method (%s) in route definition (%s)", m, path));
                }
            }
        }

    }

    public boolean supports(RouteDefinition routeDefinition, String path) {
        return true;
    }

    private Operation buildOperation(RouteDefinition routeDefinition) {
        Operation operation = new Operation();
        operation.responses(this.buildDefaultResponses());
        String title = routeDefinition.getTitle();
        if (StringUtils.hasText(title)) {
            operation.summary(title);
        }

        String description = routeDefinition.getDescription();
        if (StringUtils.hasText(description)) {
            operation.description(description);
        }

        String documentation = routeDefinition.getDocumentation();
        if (StringUtils.hasText(documentation)) {
            operation.setExternalDocs((new ExternalDocumentation()).url(documentation));
        }

        if (!CollectionUtils.isEmpty(routeDefinition.getTags())) {
            operation.setTags(List.copyOf(routeDefinition.getTags()));
        } else {
            operation.setTags(DEFAULT_TAGS);
        }

        return operation;
    }

    private ApiResponses buildDefaultResponses() {
        return (new ApiResponses()).addApiResponse("200", (new ApiResponse()).description("Ok"));
    }
}

