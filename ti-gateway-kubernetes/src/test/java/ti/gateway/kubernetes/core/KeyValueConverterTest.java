package ti.gateway.kubernetes.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KeyValueConverter
 */
class KeyValueConverterTest {

    private KeyValueConverter converter;

    @BeforeEach
    void setUp() {
        converter = new KeyValueConverter();
    }

    @Test
    void testConvertValidKeyValue() {
        String source = "key:value";
        KeyValue result = converter.convert(source);

        assertNotNull(result);
        assertEquals("key", result.getKey());
        assertEquals("value", result.getValue());
    }

    @Test
    void testConvertWithEmptyValue() {
        String source = "key:";
        KeyValue result = converter.convert(source);

        assertNotNull(result);
        assertEquals("key", result.getKey());
        assertEquals("", result.getValue());
    }

    @Test
    void testConvertWithMultipleColons() {
        String source = "key:value:with:colons";
        KeyValue result = converter.convert(source);

        assertNotNull(result);
        assertEquals("key", result.getKey());
        assertEquals("value:with:colons", result.getValue());
    }

    @Test
    void testConvertThrowsExceptionWhenNoColon() {
        String source = "keyvalue";
        
        assertThrows(IllegalArgumentException.class, () -> {
            converter.convert(source);
        });
    }

    @Test
    void testConvertThrowsExceptionWhenEmptyKey() {
        String source = ":value";
        
        assertThrows(IllegalArgumentException.class, () -> {
            converter.convert(source);
        });
    }

    @Test
    void testConvertThrowsExceptionWhenNull() {
        assertThrows(NullPointerException.class, () -> {
            converter.convert(null);
        });
    }
}
