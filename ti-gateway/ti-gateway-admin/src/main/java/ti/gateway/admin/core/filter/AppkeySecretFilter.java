package ti.gateway.admin.core.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ti.gateway.admin.core.sign.AccessAppSignCheck;
import ti.gateway.admin.core.sign.InvalidAccessTokenException;

/**
 * App key Secret Filter
 */
public class AppkeySecretFilter implements GlobalFilter, Ordered {

    private static final Log log = LogFactory.getLog(AppkeySecretFilter.class);

    private AccessAppSignCheck accessAppSignCheck;

    public static final int APP_KEY_SECRET_FILTER_ORDER = 0;


    public AppkeySecretFilter(AccessAppSignCheck accessAppSignCheck) {
        this.accessAppSignCheck = accessAppSignCheck;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        try {
            accessAppSignCheck.validAccessAppSign(request);
        } catch (InvalidAccessTokenException e) {
            log.warn("invalid access token warn", e);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return APP_KEY_SECRET_FILTER_ORDER;
    }

}
