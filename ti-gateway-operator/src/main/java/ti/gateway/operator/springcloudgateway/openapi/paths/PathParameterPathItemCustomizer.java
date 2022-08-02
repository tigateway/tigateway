package ti.gateway.operator.springcloudgateway.openapi.paths;

import ti.gateway.operator.springcloudgateway.openapi.PathItemCustomizer;
import ti.gateway.operator.springcloudgateway.route.RouteDefinition;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
class PathParameterPathItemCustomizer implements PathItemCustomizer {
    PathParameterPathItemCustomizer() {
    }

    public void customize(PathItem pathItem, RouteDefinition routeDefinition, String path) {
        String[] pathParameterNames = StringUtils.substringsBetween(path, "{", "}");
        String[] var5 = pathParameterNames;
        int var6 = pathParameterNames.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            String pathParameterName = var5[var7];
            Parameter pathParameter = (new Parameter()).name(pathParameterName).in("path").required(true);
            pathItem.addParametersItem(pathParameter);
        }

    }

    public boolean supports(RouteDefinition routeDefinition, String path) {
        String[] pathParameterNames = StringUtils.substringsBetween(path, "{", "}");
        return !ArrayUtils.isEmpty(pathParameterNames);
    }
}

