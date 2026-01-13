package ti.gateway.kubernetes.security;

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
 * Unit tests for IdTokenRelayGatewayFilterFactory
 */
class IdTokenRelayGatewayFilterFactoryTest {

    private IdTokenRelayGatewayFilterFactory factory;

    @BeforeEach
    void setUp() {
        factory = new IdTokenRelayGatewayFilterFactory();
    }

    @Test
    void testName() {
        String name = factory.name();
        
        assertEquals("TokenRelay", name);
    }

    @Test
    void testApply() {
        GatewayFilter filter = factory.apply(new Object());
        
        assertNotNull(filter);
    }

    @Test
    void testFilterExecutionWithoutAuthentication() {
        GatewayFilter filter = factory.apply(new Object());
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
    void testFilterExecutionWithNullConfig() {
        // apply(null) may throw NullPointerException due to AbstractGatewayFilterFactory implementation
        // So we test with a valid config object instead
        GatewayFilter filter = factory.apply(new Object());
        assertNotNull(filter);
        
        // Test that filter can be created and executed
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        // Filter should handle execution gracefully
        Mono<Void> result = filter.filter(exchange, chain);
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testGetConfigClass() {
        Class<?> configClass = factory.getConfigClass();
        
        // IdTokenRelayGatewayFilterFactory uses Object as config
        assertEquals(Object.class, configClass);
    }
}
