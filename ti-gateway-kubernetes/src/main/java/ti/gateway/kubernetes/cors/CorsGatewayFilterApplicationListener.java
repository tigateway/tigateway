package ti.gateway.kubernetes.cors;

import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;

import java.util.HashMap;
import java.util.Optional;

@Component
@SuppressWarnings("unused")
public class CorsGatewayFilterApplicationListener implements ApplicationListener<RefreshRoutesEvent> {
    private final GlobalCorsProperties globalCorsProperties;
    private final RoutePredicateHandlerMapping routePredicateHandlerMapping;
    private final RouteDefinitionLocator routeDefinitionLocator;
    @SuppressWarnings("unused")
    private static final String PATH_PREDICATE_NAME = "Path";
    @SuppressWarnings("unused")
    private static final String CORS_FILTER_NAME = "Cors";
    @SuppressWarnings("unused")
    private static final String ALL_PATHS = "/**";

    public CorsGatewayFilterApplicationListener(GlobalCorsProperties globalCorsProperties, RoutePredicateHandlerMapping routePredicateHandlerMapping, RouteDefinitionLocator routeDefinitionLocator) {
        this.globalCorsProperties = globalCorsProperties;
        this.routePredicateHandlerMapping = routePredicateHandlerMapping;
        this.routeDefinitionLocator = routeDefinitionLocator;
    }

    @Override
    public void onApplicationEvent(RefreshRoutesEvent event) {
        this.routeDefinitionLocator.getRouteDefinitions().collectList().subscribe((routeDefinitions) -> {
            HashMap<String, CorsConfiguration> corsConfigurations = new HashMap<>(this.globalCorsProperties.getCorsConfigurations());
            routeDefinitions.forEach(routeDefinition -> {
                String pathPredicate = this.getPathPredicate(routeDefinition);
                Optional<CorsConfiguration> corsConfiguration = this.getCorsConfiguration(routeDefinition);
                corsConfiguration.ifPresent(configuration -> {
                    corsConfigurations.put(pathPredicate, configuration);
                });
            });
            this.routePredicateHandlerMapping.setCorsConfigurations(corsConfigurations);
        });
    }

    private String getPathPredicate(RouteDefinition routeDefinition) {
        return routeDefinition.getPredicates().stream()
                .filter(predicate -> {
                    return "Path".equals(predicate.getName());
                })
                .findFirst().flatMap(predicate -> {
                    return predicate.getArgs().values().stream().findFirst();
                })
                .orElse("/**");
    }

    private Optional<CorsConfiguration> getCorsConfiguration(RouteDefinition routeDefinition) {
        return  routeDefinition.getFilters().stream()
                .filter(filter -> {
                    return "Cors".equals(filter.getName());
                })
                .findFirst()
                .map(filter -> {
                    return String.join(",", filter.getArgs().values());
                })
                .map(filterArgs -> {
                    CorsGatewayFilterConfig filterConfig = new CorsGatewayFilterConfig();
                    filterConfig.setCors(filterArgs);
                    return filterConfig.getCorsConfiguration();
                })
                .or(Optional::empty);
    }

}
