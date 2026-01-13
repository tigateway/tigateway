package ti.gateway.kubernetes.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KeyValue
 */
class KeyValueTest {

    @Test
    void testConstructorAndGetters() {
        String key = "test-key";
        String value = "test-value";
        KeyValue keyValue = new KeyValue(key, value);

        assertNotNull(keyValue);
        assertEquals(key, keyValue.getKey());
        assertEquals(value, keyValue.getValue());
    }

    @Test
    void testWithNullKey() {
        KeyValue keyValue = new KeyValue(null, "value");
        assertNull(keyValue.getKey());
        assertEquals("value", keyValue.getValue());
    }

    @Test
    void testWithNullValue() {
        KeyValue keyValue = new KeyValue("key", null);
        assertEquals("key", keyValue.getKey());
        assertNull(keyValue.getValue());
    }

    @Test
    void testWithEmptyStrings() {
        KeyValue keyValue = new KeyValue("", "");
        assertEquals("", keyValue.getKey());
        assertEquals("", keyValue.getValue());
    }
}
