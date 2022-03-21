package io.pivotal.spring.cloud.gateway.header;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AllowedRequestHeadersCountGatewayFilterFactory extends AbstractGatewayFilterFactory<AllowedRequestHeadersCountGatewayFilterFactory.Config> {
    public AllowedRequestHeadersCountGatewayFilterFactory() {
        super(AllowedRequestHeadersCountGatewayFilterFactory.Config.class);
    }

    public GatewayFilter apply(AllowedRequestHeadersCountGatewayFilterFactory.Config config) {
        return (exchange, chain) -> {
            if (exchange.getRequest().getHeaders().size() > config.getHeaderCount()) {
                exchange.getResponse().setStatusCode(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
                exchange.getResponse().getHeaders().set("errorMessage", "Request exceeded the maximum number of allowed headers");
                return exchange.getResponse().setComplete();
            } else {
                return chain.filter(exchange);
            }
        };
    }

    public List<String> shortcutFieldOrder() {
        return List.of("headerCount");
    }


    static class Config {
        private int headerCount;

        Config() {
        }

        public int getHeaderCount() {
            return this.headerCount;
        }

        public void setHeaderCount(int headerCount) {
            this.headerCount = headerCount;
        }
    }

}
