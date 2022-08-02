package ti.gateway.kubernetes.parameter;

import java.util.Arrays;
import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AllowedRequestQueryParamsCountGatewayFilterFactory extends AbstractGatewayFilterFactory<AllowedRequestQueryParamsCountGatewayFilterFactory.Config> {
    public AllowedRequestQueryParamsCountGatewayFilterFactory() {
        super(AllowedRequestQueryParamsCountGatewayFilterFactory.Config.class);
    }

    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paramsCount");
    }

    public GatewayFilter apply(AllowedRequestQueryParamsCountGatewayFilterFactory.Config config) {
        return (exchange, chain) -> {
            if (exchange.getRequest().getQueryParams().size() > config.getParamsCount()) {
                exchange.getResponse().setStatusCode(HttpStatus.URI_TOO_LONG);
                exchange.getResponse().getHeaders().set("errorMessage", "Request exceeded the maximum number of allowed query parameters");
                return exchange.getResponse().setComplete();
            } else {
                return chain.filter(exchange);
            }
        };
    }

    static class Config {
        private int paramsCount;

        Config() {
        }

        public int getParamsCount() {
            return this.paramsCount;
        }

        public void setParamsCount(int paramsCount) {
            this.paramsCount = paramsCount;
        }
    }
}
