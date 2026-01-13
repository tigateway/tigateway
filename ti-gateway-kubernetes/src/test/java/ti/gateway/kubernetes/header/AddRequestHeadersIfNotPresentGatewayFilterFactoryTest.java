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
import ti.gateway.kubernetes.core.KeyValue;
import ti.gateway.kubernetes.core.KeyValueConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AddRequestHeadersIfNotPresentGatewayFilterFactory
 */
class AddRequestHeadersIfNotPresentGatewayFilterFactoryTest {

    private AddRequestHeadersIfNotPresentGatewayFilterFactory factory;
    private KeyValueConfig config;

    @BeforeEach
    void setUp() {
        factory = new AddRequestHeadersIfNotPresentGatewayFilterFactory();
        config = new KeyValueConfig();
    }

    @Test
    void testApply() {
        KeyValue[] keyValues = new KeyValue[]{
                new KeyValue("X-Custom-Header", "value1")
        };
        config.setKeyValues(keyValues);
        
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
    }

    @Test
    void testFilterExecutionWithMissingHeader() {
        KeyValue[] keyValues = new KeyValue[]{
                new KeyValue("X-Custom-Header", "custom-value")
        };
        config.setKeyValues(keyValues);
        
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
        
        // Verify header was added
        String headerValue = exchange.getRequest().getHeaders().getFirst("X-Custom-Header");
        assertEquals("custom-value", headerValue);
    }

    @Test
    void testFilterExecutionWithExistingHeader() {
        KeyValue[] keyValues = new KeyValue[]{
                new KeyValue("X-Custom-Header", "new-value")
        };
        config.setKeyValues(keyValues);
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("X-Custom-Header", "existing-value")
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        
        // Verify existing header was not replaced
        String headerValue = exchange.getRequest().getHeaders().getFirst("X-Custom-Header");
        assertEquals("existing-value", headerValue);
    }

    @Test
    void testFilterExecutionWithMultipleHeaders() {
        KeyValue[] keyValues = new KeyValue[]{
                new KeyValue("X-Header-1", "value1"),
                new KeyValue("X-Header-2", "value2")
        };
        config.setKeyValues(keyValues);
        
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
        
        // Verify both headers were added
        assertEquals("value1", exchange.getRequest().getHeaders().getFirst("X-Header-1"));
        assertEquals("value2", exchange.getRequest().getHeaders().getFirst("X-Header-2"));
    }

    @Test
    void testFilterExecutionWithEmptyKeyValues() {
        KeyValue[] keyValues = new KeyValue[0];
        config.setKeyValues(keyValues);
        
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
}
