package io.pivotal.spring.cloud.gateway.header;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.RewriteResponseHeaderGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

public class RewriteAllResponseHeadersGatewayFilterFactory extends RewriteResponseHeaderGatewayFilterFactory {

    public RewriteAllResponseHeadersGatewayFilterFactory() {
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("regexp", "replacement");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                this.rewriteAllHeaders(exchange, config);
            }));
        };
    }

    private void rewriteAllHeaders(ServerWebExchange exchange, Config config) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.keySet().forEach(headerName -> {
            headers.compute(headerName, (k, v) -> {
                return super.rewriteHeaders(config, v);
            });
        });
    }
}
