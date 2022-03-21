package io.pivotal.spring.cloud.gateway.cors;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CorsGatewayFilterFactory implements GatewayFilterFactory<CorsGatewayFilterConfig> {
    public CorsGatewayFilterFactory() {
    }

    @Override
    public GatewayFilter apply(CorsGatewayFilterConfig config) {
        return (exchange, chain) -> {
            return chain.filter(exchange);
        };
    }

    @Override
    public Class<CorsGatewayFilterConfig> getConfigClass() {
        return CorsGatewayFilterConfig.class;
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("cors");
    }
}
