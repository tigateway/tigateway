package ti.gateway.kubernetes.header;

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
 * Unit tests for AllowedRequestHeadersCountGatewayFilterFactory
 */
class AllowedRequestHeadersCountGatewayFilterFactoryTest {

    private AllowedRequestHeadersCountGatewayFilterFactory factory;
    private AllowedRequestHeadersCountGatewayFilterFactory.Config config;

    @BeforeEach
    void setUp() {
        factory = new AllowedRequestHeadersCountGatewayFilterFactory();
        config = new AllowedRequestHeadersCountGatewayFilterFactory.Config();
    }

    @Test
    void testApply() {
        config.setHeaderCount(10);
        
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
    }

    @Test
    void testFilterWithAllowedHeadersCount() {
        config.setHeaderCount(5);
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("Header1", "value1")
                        .header("Header2", "value2")
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
    void testFilterWithExceededHeadersCount() {
        config.setHeaderCount(2);
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("Header1", "value1")
                        .header("Header2", "value2")
                        .header("Header3", "value3")
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain, never()).filter(any(ServerWebExchange.class));
        assertEquals(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, exchange.getResponse().getStatusCode());
        assertEquals("Request exceeded the maximum number of allowed headers",
                exchange.getResponse().getHeaders().getFirst("errorMessage"));
    }

    @Test
    void testFilterWithExactHeadersCount() {
        config.setHeaderCount(3);
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("Header1", "value1")
                        .header("Header2", "value2")
                        .header("Header3", "value3")
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
        assertEquals("headerCount", fieldOrder.get(0));
    }

    @Test
    void testConfigGettersAndSetters() {
        assertEquals(0, config.getHeaderCount());
        
        config.setHeaderCount(15);
        assertEquals(15, config.getHeaderCount());
    }
}
