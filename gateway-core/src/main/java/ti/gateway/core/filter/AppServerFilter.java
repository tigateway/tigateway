package ti.gateway.core.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ti.gateway.core.server.AppServerCheck;
import ti.gateway.core.server.InvalidAccessServerException;

/**
 * APP 服务状态授权校验
 */
public class AppServerFilter implements GlobalFilter, Ordered {

    private static final Log log = LogFactory.getLog(AppkeySecretFilter.class);

    private AppServerCheck appServerCheck;

    public static final int APP_SERVER_FILTER_ORDER = 10200;

    public AppServerFilter(AppServerCheck appServerCheck) {
        this.appServerCheck = appServerCheck;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        try {
            appServerCheck.validAccessServer(exchange);
        } catch (InvalidAccessServerException e) {
            log.warn("invalid app server token warn", e);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return APP_SERVER_FILTER_ORDER;
    }

}
