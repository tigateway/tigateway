package ti.gateway.kubernetes.header;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
public class StoreHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<StoreHeaderGatewayFilterFactory.TracingHeadersConfig> {

    public StoreHeaderGatewayFilterFactory() {
        super(StoreHeaderGatewayFilterFactory.TracingHeadersConfig.class);
    }

    @Override
    public GatewayFilter apply(TracingHeadersConfig config) {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            Iterator<String> iterator = this.getTracingHeaders(config).iterator();

            while (iterator.hasNext()) {
                String headerName = iterator.next();
                if (headers.containsKey(headerName)) {
                    exchange.getAttributes().put(this.getTracingParamName(config), headers.get(headerName));
                    break;
                }
            }

            return chain.filter(exchange);
        };
    }

    private String getTracingParamName(TracingHeadersConfig config) {
        return config.getTracingHeaders().get(config.getTracingHeaders().size() - 1);
    }

    private List<String> getTracingHeaders(TracingHeadersConfig config) {
        return config.getTracingHeaders().size() == 1 ? config.getTracingHeaders() : config.getTracingHeaders().subList(0, config.getTracingHeaders().size() - 1);
    }

    @Override
    public Class<TracingHeadersConfig> getConfigClass() {
        return TracingHeadersConfig.class;
    }

    @Override
    public TracingHeadersConfig newConfig() {
        return new TracingHeadersConfig();
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("tracingHeaders");
    }

    public static class TracingHeadersConfig {
        List<String> tracingHeaders;

        public TracingHeadersConfig() {
        }

        public List<String> getTracingHeaders() {
            return tracingHeaders;
        }

        public void setTracingHeaders(List<String> tracingHeaders) {
            this.tracingHeaders = tracingHeaders;
        }
    }

}
