package ti.gateway.kubernetes.metrics;

import io.micrometer.core.instrument.Tags;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.gateway.support.tagsprovider.GatewayTagsProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class GatewayFilterTagsProvider implements GatewayTagsProvider {
    public GatewayFilterTagsProvider() {
    }

    public Tags apply(ServerWebExchange exchange) {
        Route route = (Route)exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        return Tags.of("hasAuthenticationFilter", String.valueOf(containsAuthenticationFilter(route)));
    }

    private static boolean containsAuthenticationFilter(Route route) {
        return route != null && route.getFilters() != null && route.toString().contains("SecurityGatewayFilter");
    }
}
