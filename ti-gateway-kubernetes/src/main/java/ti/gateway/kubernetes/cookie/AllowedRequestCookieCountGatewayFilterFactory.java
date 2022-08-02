package ti.gateway.kubernetes.cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;

@Component
public class AllowedRequestCookieCountGatewayFilterFactory extends AbstractGatewayFilterFactory<AllowedRequestCookieCountGatewayFilterFactory.Config> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AllowedRequestCookieCountGatewayFilterFactory.class);

    public AllowedRequestCookieCountGatewayFilterFactory() {
        super(AllowedRequestCookieCountGatewayFilterFactory.Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("cookieCount");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
            if (cookies.size() > config.getCookieCount()) {
                LOGGER.info("Request exceeded the maximum number of cookies: ", cookies);
                exchange.getResponse().setStatusCode(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
                exchange.getResponse().getHeaders().set("errorMessage", "Request exceeded the maximum of cookies");
                return exchange.getResponse().setComplete();
            } else {
                return chain.filter(exchange);
            }
        };
    }


    static class Config {
        private int cookieCount;

        public Config() {
        }

        public int getCookieCount() {
            return cookieCount;
        }

        public void setCookieCount(int cookieCount) {
            this.cookieCount = cookieCount;
        }
    }
}
