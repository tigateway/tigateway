package ti.gateway.kubernetes.basicauth;

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
 * Unit tests for BasicAuthGatewayFilterFactory
 */
class BasicAuthGatewayFilterFactoryTest {

    private BasicAuthGatewayFilterFactory factory;
    private BasicAuthenticationProperties config;

    @BeforeEach
    void setUp() {
        factory = new BasicAuthGatewayFilterFactory();
        config = new BasicAuthenticationProperties();
    }

    @Test
    void testGetConfigClass() {
        Class<?> configClass = factory.getConfigClass();
        
        assertEquals(BasicAuthenticationProperties.class, configClass);
    }

    @Test
    void testNewConfig() {
        BasicAuthenticationProperties newConfig = factory.newConfig();
        
        assertNotNull(newConfig);
    }

    @Test
    void testApply() {
        String encodedCredentials = "dXNlcm5hbWU6cGFzc3dvcmQ="; // username:password
        config.setEncodedCredentials(encodedCredentials);
        
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
    }

    @Test
    void testFilterExecution() {
        String encodedCredentials = "dXNlcm5hbWU6cGFzc3dvcmQ=";
        config.setEncodedCredentials(encodedCredentials);
        
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
        
        // Verify Authorization header is added
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith("Basic "));
        assertEquals("Basic " + encodedCredentials, authHeader);
    }

    @Test
    void testShortcutFieldOrder() {
        var fieldOrder = factory.shortcutFieldOrder();
        
        assertNotNull(fieldOrder);
        assertEquals(1, fieldOrder.size());
        assertEquals("encodedCredentials", fieldOrder.get(0));
    }

    @Test
    void testApplyWithEmptyCredentials() {
        config.setEncodedCredentials("");
        
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
    }
}
