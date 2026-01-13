package ti.gateway.kubernetes.circuitbreaker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ScgFallbackHeadersGatewayFilterFactory}.
 */
class ScgFallbackHeadersGatewayFilterFactoryTest {

    private ScgFallbackHeadersGatewayFilterFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ScgFallbackHeadersGatewayFilterFactory();
    }

    @Test
    void testShortcutFieldOrder() {
        List<String> fieldOrder = factory.shortcutFieldOrder();
        assertNotNull(fieldOrder);
        assertEquals(4, fieldOrder.size());
        assertEquals("executionExceptionTypeHeaderName", fieldOrder.get(0));
        assertEquals("executionExceptionMessageHeaderName", fieldOrder.get(1));
        assertEquals("rootCauseExceptionTypeHeaderName", fieldOrder.get(2));
        assertEquals("rootCauseExceptionMessageHeaderName", fieldOrder.get(3));
    }

    @Test
    void testName() {
        String name = factory.name();
        assertNotNull(name);
        assertEquals("FallbackHeaders", name);
    }

    @Test
    void testGetConfigClass() {
        Class<?> configClass = factory.getConfigClass();
        assertNotNull(configClass);
        // FallbackHeadersGatewayFilterFactory.Config is the parent class config
        assertTrue(configClass.getName().contains("Config"));
    }
}
