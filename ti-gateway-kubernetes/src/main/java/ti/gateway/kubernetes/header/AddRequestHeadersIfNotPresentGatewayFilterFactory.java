package ti.gateway.kubernetes.header;

import ti.gateway.kubernetes.core.KeyValue;
import ti.gateway.kubernetes.core.KeyValueConfig;
import ti.gateway.kubernetes.core.KeyValueGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

public class AddRequestHeadersIfNotPresentGatewayFilterFactory extends KeyValueGatewayFilterFactory {
    public AddRequestHeadersIfNotPresentGatewayFilterFactory() {
    }

    @Override
    public GatewayFilter apply(KeyValueConfig config) {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            ServerHttpRequest.Builder requestBuilder = null;
            KeyValue[] keyValues = config.getKeyValues();
            int length = keyValues.length;

            for (int i = 0; i < length; ++i) {
                KeyValue kv = keyValues[i];
                String headerName = kv.getKey();
                boolean headerIsMissingOrBlank = headers.getOrEmpty(headerName).stream().allMatch(h -> {
                    return h == null || "".equals(h.trim());
                });

                if (headerIsMissingOrBlank) {
                    if (requestBuilder == null) {
                        requestBuilder = exchange.getRequest().mutate();
                    }

                    requestBuilder.header(headerName, new String[] {kv.getValue()});
                }
            }

            if (requestBuilder != null) {
                exchange = exchange.mutate().request(requestBuilder.build()).build();
            }

            return chain.filter(exchange);
        };
    }
}
