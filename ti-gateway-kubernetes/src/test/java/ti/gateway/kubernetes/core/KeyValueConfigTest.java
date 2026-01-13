package ti.gateway.kubernetes.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KeyValueConfig
 */
class KeyValueConfigTest {

    private KeyValueConfig config;

    @BeforeEach
    void setUp() {
        config = new KeyValueConfig();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(config);
        assertNull(config.getKeyValues());
    }

    @Test
    void testSetAndGetKeyValues() {
        KeyValue[] keyValues = new KeyValue[]{
                new KeyValue("key1", "value1"),
                new KeyValue("key2", "value2")
        };
        
        config.setKeyValues(keyValues);
        
        assertNotNull(config.getKeyValues());
        assertEquals(2, config.getKeyValues().length);
        assertEquals("key1", config.getKeyValues()[0].getKey());
        assertEquals("value1", config.getKeyValues()[0].getValue());
        assertEquals("key2", config.getKeyValues()[1].getKey());
        assertEquals("value2", config.getKeyValues()[1].getValue());
    }

    @Test
    void testSetEmptyArray() {
        KeyValue[] emptyArray = new KeyValue[0];
        
        config.setKeyValues(emptyArray);
        
        assertNotNull(config.getKeyValues());
        assertEquals(0, config.getKeyValues().length);
    }

    @Test
    void testSetNullKeyValues() {
        config.setKeyValues(null);
        
        assertNull(config.getKeyValues());
    }

    @Test
    void testSetSingleKeyValue() {
        KeyValue[] keyValues = new KeyValue[]{
                new KeyValue("single", "value")
        };
        
        config.setKeyValues(keyValues);
        
        assertEquals(1, config.getKeyValues().length);
        assertEquals("single", config.getKeyValues()[0].getKey());
        assertEquals("value", config.getKeyValues()[0].getValue());
    }
}
