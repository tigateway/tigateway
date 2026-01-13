package ti.gateway.kubernetes.parameter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AllowedRequestQueryParamsCountGatewayFilterFactory
 */
class AllowedRequestQueryParamsCountGatewayFilterFactoryTest {

    private AllowedRequestQueryParamsCountGatewayFilterFactory factory;
    private AllowedRequestQueryParamsCountGatewayFilterFactory.Config config;

    @BeforeEach
    void setUp() {
        factory = new AllowedRequestQueryParamsCountGatewayFilterFactory();
        config = new AllowedRequestQueryParamsCountGatewayFilterFactory.Config();
    }

    @Test
    void testApply() {
        config.setParamsCount(10);
        
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
    }

    @Test
    void testFilterWithAllowedParamsCount() {
        config.setParamsCount(5);
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .queryParam("param1", "value1")
                        .queryParam("param2", "value2")
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
    }

    @Test
    void testFilterWithExceededParamsCount() {
        config.setParamsCount(2);
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .queryParam("param1", "value1")
                        .queryParam("param2", "value2")
                        .queryParam("param3", "value3")
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain, never()).filter(any(ServerWebExchange.class));
        assertEquals(HttpStatus.URI_TOO_LONG, exchange.getResponse().getStatusCode());
        assertEquals("Request exceeded the maximum number of allowed query parameters",
                exchange.getResponse().getHeaders().getFirst("errorMessage"));
    }

    @Test
    void testFilterWithExactParamsCount() {
        config.setParamsCount(3);
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .queryParam("param1", "value1")
                        .queryParam("param2", "value2")
                        .queryParam("param3", "value3")
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testShortcutFieldOrder() {
        List<String> fieldOrder = factory.shortcutFieldOrder();
        
        assertNotNull(fieldOrder);
        assertEquals(1, fieldOrder.size());
        assertEquals("paramsCount", fieldOrder.get(0));
    }

    @Test
    void testConfigGettersAndSetters() {
        assertEquals(0, config.getParamsCount());
        
        config.setParamsCount(20);
        assertEquals(20, config.getParamsCount());
    }
}
