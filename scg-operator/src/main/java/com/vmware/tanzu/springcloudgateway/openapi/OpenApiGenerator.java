package com.vmware.tanzu.springcloudgateway.openapi;

import com.vmware.tanzu.springcloudgateway.helper.GroupIdGetter;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewaySpecApi;
import com.vmware.tanzu.springcloudgateway.route.RouteDefinition;
import com.vmware.tanzu.springcloudgateway.route.RoutesDefinition;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
public class OpenApiGenerator {
    private static final String INFO_DESCRIPTION = "Generated OpenAPI 3 document that describes the API routes configured for '%s' Spring Cloud Gateway instance deployed under '%s' namespace.";
    private static final String UNSPECIFIED_VERSION = "unspecified";
    private final List<PathItemCustomizer> pathItemCustomizers;
    private final List<ComponentCustomizer> componentCustomizers;

    public OpenApiGenerator(List<PathItemCustomizer> pathItemCustomizers, List<ComponentCustomizer> componentCustomizers) {
        this.pathItemCustomizers = pathItemCustomizers;
        this.componentCustomizers = (List)Objects.requireNonNull(componentCustomizers);
    }

    public OpenAPI generate(V1SpringCloudGateway scg, RoutesDefinition routesDefinition) {
        Assert.notNull(scg, "'V1SpringCloudGateway' should not be null");
        Assert.notNull(routesDefinition, "'RoutesDefinition' should not be null");
        OpenAPI openAPI = this.buildOpenAPI(scg);
        Iterator var4 = routesDefinition.getRouteDefinitions().iterator();

        while(var4.hasNext()) {
            RouteDefinition routeDefinition = (RouteDefinition)var4.next();
            String[] var6 = this.getPathStrings(routeDefinition);
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                String routeDefinitionPath = var6[var8];
                PathItem pathItem = (new PathItem()).summary("Route ID: " + routeDefinition.getId());
                Iterator var11 = this.pathItemCustomizers.iterator();

                while(var11.hasNext()) {
                    PathItemCustomizer customizer = (PathItemCustomizer)var11.next();
                    if (customizer.supports(routeDefinition, routeDefinitionPath)) {
                        customizer.customize(pathItem, routeDefinition, routeDefinitionPath);
                    }
                }

                var11 = this.componentCustomizers.iterator();

                while(var11.hasNext()) {
                    ComponentCustomizer customizer = (ComponentCustomizer)var11.next();
                    if (customizer.supports(routeDefinition)) {
                        customizer.customize(scg, routeDefinition, pathItem, openAPI);
                    }
                }

                openAPI.path(routeDefinitionPath, pathItem);
            }
        }

        return openAPI;
    }

    public Collection<OpenAPI> generate(Map<V1SpringCloudGateway, RoutesDefinition> routesForGateways) {
        Assert.notNull(routesForGateways, "'routesForGateways' should not be null");
        return (Collection)routesForGateways.entrySet().stream().map((entry) -> {
            return this.generate((V1SpringCloudGateway)entry.getKey(), (RoutesDefinition)entry.getValue());
        }).collect(Collectors.toList());
    }

    private String[] getPathStrings(RouteDefinition routeDefinition) {
        String path = routeDefinition.getPath();
        return StringUtils.hasText(path) ? path.split(",") : new String[]{"/"};
    }

    private OpenAPI buildOpenAPI(V1SpringCloudGateway scg) {
        OpenAPI openAPI = new OpenAPI();
        openAPI.setInfo(this.buildDefaultInfo(scg));
        V1SpringCloudGatewaySpecApi api;
        if (scg.getSpec() != null && (api = scg.getSpec().getApi()) != null) {
            if (StringUtils.hasText(api.getDocumentation())) {
                openAPI.setExternalDocs((new ExternalDocumentation()).url(api.getDocumentation()));
            }

            if (StringUtils.hasText(api.getServerUrl())) {
                openAPI.addServersItem((new Server()).url(api.getServerUrl()));
            }
        }

        openAPI.setExtensions(Map.of("groupId", GroupIdGetter.getGroupId(scg)));
        return openAPI;
    }

    private Info buildDefaultInfo(V1SpringCloudGateway scg) {
        String title = scg.getMetadata().getName();
        String description = String.format("Generated OpenAPI 3 document that describes the API routes configured for '%s' Spring Cloud Gateway instance deployed under '%s' namespace.", scg.getMetadata().getName(), scg.getMetadata().getNamespace());
        String version = "unspecified";
        V1SpringCloudGatewaySpecApi api;
        if (scg.getSpec() != null && (api = scg.getSpec().getApi()) != null) {
            if (StringUtils.hasText(api.getTitle())) {
                title = api.getTitle();
            }

            if (StringUtils.hasText(api.getDescription())) {
                description = api.getDescription();
            }

            if (StringUtils.hasText(api.getVersion())) {
                version = api.getVersion();
            }
        }

        return (new Info()).title(title).description(description).version(version);
    }
}

