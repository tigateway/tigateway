package io.pivotal.spring.cloud.gateway.apikeys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@ApiKeyRequired
public class ApiKeyGlobalFilter implements GlobalFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyGlobalFilter.class);
    private final ApiKeyValidator apiKeyValidator;

    public ApiKeyGlobalFilter(ApiKeyValidator apiKeyValidator) {
        this.apiKeyValidator = apiKeyValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String apiKey = exchange.getRequest().getHeaders().getFirst("X-Api-Key");
        return this.apiKeyValidator.keyIsValid(apiKey).flatMap((keyIsValid) -> {
            if (! keyIsValid) {
                LOGGER.warn("The provided API key is not valid!");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } else {
                return chain.filter(exchange);
            }
        });
    }
}
