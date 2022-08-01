package ti.gateway.core.filter;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.web.server.ServerWebExchange;
import ti.gateway.util.GatewayServerWebExchangeUtils;

/**
 * 负载均衡
 */
public class AppLoadBalancerClientFilter extends LoadBalancerClientFilter {

    public AppLoadBalancerClientFilter(LoadBalancerClient loadBalancer, LoadBalancerProperties properties) {
        super(loadBalancer, properties);
    }

    @Override
    protected ServiceInstance choose(ServerWebExchange exchange) {
        ServiceInstance choose = super.choose(exchange);
        if (choose != null) {
            exchange.getAttributes().put(GatewayServerWebExchangeUtils.GATEWAY_REQUEST_SERVICE_INSTANCE, choose);
        }
        return choose;
    }

}
