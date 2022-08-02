package ti.gateway.kubernetes.ip;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ipresolver.RemoteAddressResolver;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

@Component
public class StoreIpAddressGatewayFilterFactory extends AbstractGatewayFilterFactory<StoreIpAddressGatewayFilterFactory.Config> {
    public StoreIpAddressGatewayFilterFactory() {
        super(StoreIpAddressGatewayFilterFactory.Config.class);
    }

    public List<String> shortcutFieldOrder() {
        return Arrays.asList("parameterName");
    }

    public GatewayFilter apply(Config config) {
        RemoteAddressResolver remoteAddressResolver = new RemoteAddressResolver() {
        };
        return (exchange, chain) -> {
            InetSocketAddress remoteAddress = remoteAddressResolver.resolve(exchange);
            if (remoteAddress != null) {
                exchange.getAttributes().put(config.getParameterName(), remoteAddress.getAddress().getHostAddress());
            }

            return chain.filter(exchange);
        };
    }


    static class Config {
        private String parameterName;

        Config() {
        }

        public String getParameterName() {
            return this.parameterName;
        }

        public void setParameterName(String parameterName) {
            this.parameterName = parameterName;
        }
    }

}
