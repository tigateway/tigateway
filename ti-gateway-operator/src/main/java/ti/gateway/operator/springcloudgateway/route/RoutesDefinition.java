package ti.gateway.operator.springcloudgateway.route;

import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayRouteConfig;
import io.kubernetes.client.openapi.models.V1Secret;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.util.Assert;

public class RoutesDefinition {
    private List<RouteDefinition> routeDefinitions;
    private String routePrefix;

    public static RoutesDefinition from(V1SpringCloudGatewayRouteConfig scgRouteConfig, V1Secret basicAuthSecret) {
        if (scgRouteConfig != null && scgRouteConfig.getSpec() != null && scgRouteConfig.getSpec().getRoutes() != null) {
            RoutesDefinition routesDefinition = new RoutesDefinition();
            Stream<RouteDefinition> routeDefinitionStream = scgRouteConfig.getSpec().getRoutes().stream().map((crdRoute) -> {
                return RouteDefinition.from(crdRoute, basicAuthSecret);
            });
            Objects.requireNonNull(routesDefinition);
            routeDefinitionStream.forEach(routesDefinition::addRouteDefinition);
            return routesDefinition;
        } else {
            return new RoutesDefinition();
        }
    }

    public static RoutesDefinition from(String routePrefix, V1SpringCloudGatewayRouteConfig scgRouteConfig, V1Secret basicAuthSecret) {
        RoutesDefinition routesDefinition = from(scgRouteConfig, basicAuthSecret);
        routesDefinition.setRoutePrefix(routePrefix);

        for(int i = 0; i < routesDefinition.getRouteDefinitions().size(); ++i) {
            ((RouteDefinition)routesDefinition.getRouteDefinitions().get(i)).setId(routePrefix + i);
        }

        return routesDefinition;
    }

    public RoutesDefinition() {
        this(new ArrayList<>());
    }

    public RoutesDefinition(RouteDefinition... routeDefinitions) {
        this(Arrays.asList(routeDefinitions));
    }

    public RoutesDefinition(List<RouteDefinition> routeDefinitions) {
        this.setRouteDefinitions(routeDefinitions);
    }

    public void setRouteDefinitions(List<RouteDefinition> routeDefinitions) {
        Assert.notNull(routeDefinitions, "'routeDefinitions' should not be null");
        this.routeDefinitions = routeDefinitions;
    }

    public List<RouteDefinition> getRouteDefinitions() {
        return this.routeDefinitions;
    }

    public RoutesDefinition addRouteDefinition(RouteDefinition routeDefinition) {
        this.routeDefinitions.add(routeDefinition);
        return this;
    }

    public String getRoutePrefix() {
        return this.routePrefix;
    }

    public void setRoutePrefix(String routePrefix) {
        this.routePrefix = routePrefix;
    }
}
