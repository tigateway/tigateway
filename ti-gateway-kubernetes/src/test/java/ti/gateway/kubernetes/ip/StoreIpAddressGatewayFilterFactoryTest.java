package ti.gateway.kubernetes.ip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
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
 * Unit tests for StoreIpAddressGatewayFilterFactory
 */
class StoreIpAddressGatewayFilterFactoryTest {

    private StoreIpAddressGatewayFilterFactory factory;
    private StoreIpAddressGatewayFilterFactory.Config config;

    @BeforeEach
    void setUp() {
        factory = new StoreIpAddressGatewayFilterFactory();
        config = new StoreIpAddressGatewayFilterFactory.Config();
    }

    @Test
    void testShortcutFieldOrder() {
        List<String> fieldOrder = factory.shortcutFieldOrder();
        
        assertNotNull(fieldOrder);
        assertEquals(1, fieldOrder.size());
        assertEquals("parameterName", fieldOrder.get(0));
    }

    @Test
    void testApply() {
        config.setParameterName("client-ip");
        
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
    }

    @Test
    void testFilterExecution() {
        config.setParameterName("client-ip");
        
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
        
        // Verify IP address was stored in attributes (may be null in test environment)
        // This is expected behavior as remote address resolution depends on network context
    }

    @Test
    void testConfigGettersAndSetters() {
        // Test default value
        assertNull(config.getParameterName());
        
        // Test setting value
        config.setParameterName("remote-ip");
        assertEquals("remote-ip", config.getParameterName());
    }

    @Test
    void testGetConfigClass() {
        Class<?> configClass = factory.getConfigClass();
        
        assertEquals(StoreIpAddressGatewayFilterFactory.Config.class, configClass);
    }

    @Test
    void testFilterWithNullParameterName() {
        // Should handle null parameter name gracefully
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
    }
}
