package com.vmware.tanzu.springcloudgateway.openapi.extensions;

import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import com.vmware.tanzu.springcloudgateway.openapi.ComponentCustomizer;
import com.vmware.tanzu.springcloudgateway.route.RouteDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AbstractSecuritySchemeComponentCustomizer implements ComponentCustomizer {
    private final String securityName;
    private final String securityScheme;
    private final Type securityType;

    public AbstractSecuritySchemeComponentCustomizer(String securityName, String securityScheme, Type securityType) {
        this.securityName = securityName;
        this.securityScheme = securityScheme;
        this.securityType = securityType;
    }

    public void customize(V1SpringCloudGateway gateway, RouteDefinition routeDefinition, PathItem pathItem, OpenAPI apiToCustomize) {
        this.defineSecuritySchemeComponent(gateway, apiToCustomize);
        this.applySecurityTo(pathItem);
    }

    public boolean supports(RouteDefinition routeDefinition) {
        return routeDefinition.hasSsoLoginFilter();
    }

    protected void applySecurityTo(PathItem pathItem) {
        SecurityRequirement securityRequirement = (new SecurityRequirement()).addList(this.securityName);
        pathItem.readOperations().forEach((operation) -> {
            operation.addSecurityItem(securityRequirement);
        });
    }

    protected SecurityScheme defaultSecurityRequirement(V1SpringCloudGateway gateway) {
        return (new SecurityScheme()).type(this.securityType).scheme(this.securityScheme);
    }

    private void defineSecuritySchemeComponent(V1SpringCloudGateway gateway, OpenAPI apiToCustomize) {
        Map<String, SecurityScheme> schemes = this.getOrCreateSecuritySchemes(apiToCustomize);
        if (!schemes.containsKey(this.securityName)) {
            apiToCustomize.getComponents().getSecuritySchemes().put(this.securityName, this.defaultSecurityRequirement(gateway));
        }

    }

    private Map<String, SecurityScheme> getOrCreateSecuritySchemes(OpenAPI api) {
        Components components = api.getComponents();
        if (Objects.isNull(components)) {
            components = new Components();
            api.setComponents(components);
        }

        Map<String, SecurityScheme> schemes = components.getSecuritySchemes();
        if (Objects.isNull(schemes)) {
            schemes = new HashMap<>();
            components.setSecuritySchemes((Map)schemes);
        }

        return (Map)schemes;
    }
}
