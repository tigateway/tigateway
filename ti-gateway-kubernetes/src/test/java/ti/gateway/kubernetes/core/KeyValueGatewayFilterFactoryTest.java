package ti.gateway.kubernetes.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link KeyValueGatewayFilterFactory}.
 */
class KeyValueGatewayFilterFactoryTest {

    private TestKeyValueGatewayFilterFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TestKeyValueGatewayFilterFactory();
    }

    @Test
    void testShortcutType() {
        assertEquals(AbstractGatewayFilterFactory.ShortcutType.GATHER_LIST, factory.shortcutType());
    }

    @Test
    void testShortcutFieldOrder() {
        List<String> fieldOrder = factory.shortcutFieldOrder();
        assertNotNull(fieldOrder);
        assertEquals(1, fieldOrder.size());
        assertEquals("keyValues", fieldOrder.get(0));
    }

    @Test
    void testNewConfig() {
        KeyValueConfig config = factory.newConfig();
        assertNotNull(config);
        assertTrue(config instanceof KeyValueConfig);
    }

    @Test
    void testGetConfigClass() {
        Class<KeyValueConfig> configClass = factory.getConfigClass();
        assertNotNull(configClass);
        assertEquals(KeyValueConfig.class, configClass);
    }

    /**
     * Test implementation of KeyValueGatewayFilterFactory for testing.
     */
    private static class TestKeyValueGatewayFilterFactory extends KeyValueGatewayFilterFactory {
        @Override
        public org.springframework.cloud.gateway.filter.GatewayFilter apply(KeyValueConfig config) {
            return (exchange, chain) -> chain.filter(exchange);
        }
    }
}
