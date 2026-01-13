package ti.gateway.kubernetes.header;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StoreHeaderGatewayFilterFactory
 */
class StoreHeaderGatewayFilterFactoryTest {

    private StoreHeaderGatewayFilterFactory factory;
    private StoreHeaderGatewayFilterFactory.TracingHeadersConfig config;

    @BeforeEach
    void setUp() {
        factory = new StoreHeaderGatewayFilterFactory();
        config = new StoreHeaderGatewayFilterFactory.TracingHeadersConfig();
    }

    @Test
    void testApply() {
        config.setTracingHeaders(Arrays.asList("X-Request-Id", "trace-id"));
        
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
    }

    @Test
    void testFilterExecutionWithMatchingHeader() {
        config.setTracingHeaders(Arrays.asList("X-Request-Id", "trace-id"));
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("X-Request-Id", "12345")
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        
        // Verify header was stored in attributes
        Object storedValue = exchange.getAttributes().get("trace-id");
        assertNotNull(storedValue);
    }

    @Test
    void testFilterExecutionWithNoMatchingHeader() {
        config.setTracingHeaders(Arrays.asList("X-Request-Id", "trace-id"));
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testFilterExecutionWithMultipleHeaders() {
        config.setTracingHeaders(Arrays.asList("X-Request-Id", "X-Correlation-Id", "trace-id"));
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("X-Correlation-Id", "corr-123")
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        
        // Verify header was stored
        Object storedValue = exchange.getAttributes().get("trace-id");
        assertNotNull(storedValue);
    }

    @Test
    void testGetConfigClass() {
        Class<?> configClass = factory.getConfigClass();
        
        assertEquals(StoreHeaderGatewayFilterFactory.TracingHeadersConfig.class, configClass);
    }

    @Test
    void testNewConfig() {
        StoreHeaderGatewayFilterFactory.TracingHeadersConfig newConfig = factory.newConfig();
        
        assertNotNull(newConfig);
    }

    @Test
    void testShortcutType() {
        var shortcutType = factory.shortcutType();
        
        assertEquals(org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory.ShortcutType.GATHER_LIST, shortcutType);
    }

    @Test
    void testShortcutFieldOrder() {
        List<String> fieldOrder = factory.shortcutFieldOrder();
        
        assertNotNull(fieldOrder);
        assertEquals(1, fieldOrder.size());
        assertEquals("tracingHeaders", fieldOrder.get(0));
    }

    @Test
    void testTracingHeadersConfig() {
        List<String> headers = Arrays.asList("X-Request-Id", "trace-id");
        config.setTracingHeaders(headers);
        
        assertEquals(headers, config.getTracingHeaders());
    }
}
