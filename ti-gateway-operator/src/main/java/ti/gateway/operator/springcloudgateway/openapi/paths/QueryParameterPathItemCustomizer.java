package ti.gateway.operator.springcloudgateway.openapi.paths;

import ti.gateway.operator.springcloudgateway.openapi.PathItemCustomizer;
import ti.gateway.operator.springcloudgateway.route.RouteDefinition;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
class QueryParameterPathItemCustomizer implements PathItemCustomizer {
    private static final String QUERY_PREDICATE_NAME = "Query";

    QueryParameterPathItemCustomizer() {
    }

    public void customize(PathItem pathItem, RouteDefinition routeDefinition, String path) {
        String queryParameter = routeDefinition.getPredicateValueFor("Query");
        String[] queryParameterTokens = queryParameter.split(",");
        String queryParameterName = queryParameterTokens[0];
        Parameter parameter = (new Parameter()).name(queryParameterName).in("query").required(true);
        if (queryParameterTokens.length > 1) {
            parameter.description(String.format("Allows value with regular expression \"%s\".", queryParameterTokens[1]));
        }

        pathItem.addParametersItem(parameter);
    }

    public boolean supports(RouteDefinition routeDefinition, String path) {
        String queryParameter = routeDefinition.getPredicateValueFor("Query");
        return StringUtils.hasText(queryParameter);
    }
}
