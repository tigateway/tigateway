package ti.gateway.operator.springcloudgateway.openapi.paths;

import ti.gateway.operator.springcloudgateway.openapi.PathItemCustomizer;
import ti.gateway.operator.springcloudgateway.route.RouteDefinition;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
class HeaderParameterPathItemCustomizer implements PathItemCustomizer {
    private static final String HEADER_PREDICATE_NAME = "Header";

    HeaderParameterPathItemCustomizer() {
    }

    public void customize(PathItem pathItem, RouteDefinition routeDefinition, String path) {
        String headerValue = routeDefinition.getPredicateValueFor("Header");
        Parameter headerParameter = this.buildHeaderParameter(headerValue);
        pathItem.addParametersItem(headerParameter);
    }

    public boolean supports(RouteDefinition routeDefinition, String path) {
        String headerValue = routeDefinition.getPredicateValueFor("Header");
        return StringUtils.hasText(headerValue);
    }

    private Parameter buildHeaderParameter(String headerPredicate) {
        String[] headerPredicateTokens = headerPredicate.split(",");
        String headerName = headerPredicateTokens[0].trim();
        String headerPattern = headerPredicateTokens.length > 1 ? headerPredicateTokens[1].trim() : ".*";
        return (new Parameter()).name(headerName).in("header").required(true).description(String.format("Header Route Predicate regexp: %s", headerPattern)).schema((new Schema()).pattern(headerPattern));
    }
}
