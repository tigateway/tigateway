package ti.gateway.kubernetes.jwt;

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
 * Unit tests for JWTClaimHeaderGatewayFilterFactory
 */
class JWTClaimHeaderGatewayFilterFactoryTest {

    private JWTClaimHeaderGatewayFilterFactory factory;
    private JWTClaimHeaderGatewayFilterFactory.Config config;

    @BeforeEach
    void setUp() {
        factory = new JWTClaimHeaderGatewayFilterFactory();
        config = new JWTClaimHeaderGatewayFilterFactory.Config();
        config.setClaim("sub");
        config.setHeaderName("X-User-Id");
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
    void testFilterWithNoAuthentication() {
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testGetConfigClass() {
        Class<?> configClass = factory.getConfigClass();
        
        assertEquals(JWTClaimHeaderGatewayFilterFactory.Config.class, configClass);
    }

    @Test
    void testConfigValidation() {
        JWTClaimHeaderGatewayFilterFactory.Config testConfig = new JWTClaimHeaderGatewayFilterFactory.Config();
        
        // Test default values
        assertNull(testConfig.getClaim());
        assertNull(testConfig.getHeaderName());
        
        // Test setting values
        testConfig.setClaim("email");
        testConfig.setHeaderName("X-User-Email");
        
        assertEquals("email", testConfig.getClaim());
        assertEquals("X-User-Email", testConfig.getHeaderName());
    }
}
