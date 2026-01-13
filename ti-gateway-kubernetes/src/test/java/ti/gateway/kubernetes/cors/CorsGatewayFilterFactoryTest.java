package ti.gateway.kubernetes.cors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CorsGatewayFilterFactory
 */
class CorsGatewayFilterFactoryTest {

    private CorsGatewayFilterFactory factory;
    private CorsGatewayFilterConfig config;

    @BeforeEach
    void setUp() {
        factory = new CorsGatewayFilterFactory();
        config = new CorsGatewayFilterConfig();
    }

    @Test
    void testApply() {
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
    }

    @Test
    void testFilterExecution() {
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
    void testGetConfigClass() {
        Class<?> configClass = factory.getConfigClass();
        
        assertEquals(CorsGatewayFilterConfig.class, configClass);
    }

    @Test
    void testShortcutType() {
        var shortcutType = factory.shortcutType();
        
        assertNotNull(shortcutType);
        assertEquals(org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory.ShortcutType.GATHER_LIST, shortcutType);
    }

    @Test
    void testShortcutFieldOrder() {
        var fieldOrder = factory.shortcutFieldOrder();
        
        assertNotNull(fieldOrder);
        assertEquals(1, fieldOrder.size());
        assertEquals("cors", fieldOrder.get(0));
    }
}
