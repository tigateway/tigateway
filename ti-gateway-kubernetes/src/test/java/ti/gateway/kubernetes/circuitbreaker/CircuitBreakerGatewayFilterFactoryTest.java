package ti.gateway.kubernetes.circuitbreaker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.web.reactive.DispatcherHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CircuitBreakerGatewayFilterFactory
 */
class CircuitBreakerGatewayFilterFactoryTest {

    @Mock
    @SuppressWarnings("rawtypes")
    private ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory;

    @Mock
    private ObjectProvider<DispatcherHandler> dispatcherHandlerProvider;

    private CircuitBreakerGatewayFilterFactory factory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        factory = new CircuitBreakerGatewayFilterFactory(
                reactiveCircuitBreakerFactory,
                dispatcherHandlerProvider
        );
    }

    @Test
    void testShortcutFileOrder() {
        List<String> fieldOrder = factory.shortcutFileOrder();
        
        assertNotNull(fieldOrder);
        assertEquals(5, fieldOrder.size());
        assertTrue(fieldOrder.contains("name"));
        assertTrue(fieldOrder.contains("fallbackUri"));
        assertTrue(fieldOrder.contains("statusCodes"));
        assertTrue(fieldOrder.contains("failureRateThreshold"));
        assertTrue(fieldOrder.contains("waitIntervalInOpenState"));
    }

    @Test
    void testCustomizer() {
        CircuitBreakerGatewayFilterFactory.Resilience4JExtendedConfig config = 
                new CircuitBreakerGatewayFilterFactory.Resilience4JExtendedConfig();
        config.setName("test-circuit-breaker");
        config.setFailureRateThreshold(50.0f);
        config.setWaitIntervalInOpenState(java.time.Duration.ofSeconds(60));
        
        var customizer = factory.customizer(config);
        
        assertNotNull(customizer);
    }

    @Test
    void testCustomizerWithNullValues() {
        CircuitBreakerGatewayFilterFactory.Resilience4JExtendedConfig config = 
                new CircuitBreakerGatewayFilterFactory.Resilience4JExtendedConfig();
        config.setName("test-circuit-breaker");
        // Leave failureRateThreshold and waitIntervalInOpenState as null
        
        var customizer = factory.customizer(config);
        
        assertNotNull(customizer);
    }

    @Test
    void testGetConfigClass() {
        Class<?> configClass = factory.getConfigClass();
        
        assertEquals(CircuitBreakerGatewayFilterFactory.Resilience4JExtendedConfig.class, configClass);
    }

    @Test
    void testResilience4JExtendedConfig() {
        CircuitBreakerGatewayFilterFactory.Resilience4JExtendedConfig config = 
                new CircuitBreakerGatewayFilterFactory.Resilience4JExtendedConfig();
        
        // Test default values
        assertNull(config.getName());
        assertNull(config.getFallbackUri());
        // StatusCodes might be initialized as empty Set, not null
        assertTrue(config.getStatusCodes() == null || config.getStatusCodes().isEmpty());
        assertNull(config.getFailureRateThreshold());
        assertNull(config.getWaitIntervalInOpenState());
        
        // Test setting values
        config.setName("test-id");
        java.net.URI fallbackUri = java.net.URI.create("/fallback");
        config.setFallbackUri(fallbackUri);
        config.setStatusCodes(java.util.Set.of("500", "502", "503"));
        config.setFailureRateThreshold(50.0f);
        config.setWaitIntervalInOpenState(java.time.Duration.ofSeconds(30));
        
        assertEquals("test-id", config.getName());
        assertEquals(fallbackUri, config.getFallbackUri());
        assertEquals(3, config.getStatusCodes().size());
        assertEquals(50.0f, config.getFailureRateThreshold());
        assertEquals(java.time.Duration.ofSeconds(30), config.getWaitIntervalInOpenState());
    }
}
