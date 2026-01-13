package ti.gateway.kubernetes.jwt;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtHelper
 */
class JwtHelperTest {

    @Test
    void testCleanupHeaderValue() {
        String headerValue = "Bearer token123";
        String result = JwtHelper.cleanupHeaderValue(headerValue);
        assertEquals("token123", result);
    }

    @Test
    void testCleanupHeaderValueWithSpaces() {
        String headerValue = "Bearer   token123  ";
        String result = JwtHelper.cleanupHeaderValue(headerValue);
        assertEquals("token123", result);
    }

    @Test
    void testCleanupHeaderValueCaseInsensitive() {
        String headerValue = "bearer token123";
        String result = JwtHelper.cleanupHeaderValue(headerValue);
        assertEquals("token123", result);
    }

    @Test
    void testGetClaimAsStringWithString() {
        String result = JwtHelper.getClaimAsString("test-value");
        assertEquals("test-value", result);
    }

    @Test
    void testGetClaimAsStringWithList() {
        List<String> list = List.of("value1", "value2");
        String result = JwtHelper.getClaimAsString(list);
        assertEquals("value1", result);
    }

    @Test
    void testGetClaimAsStringWithArray() {
        String[] array = {"value1", "value2"};
        String result = JwtHelper.getClaimAsString(array);
        assertEquals("value1", result);
    }

    @Test
    void testGetClaimAsStringWithNull() {
        String result = JwtHelper.getClaimAsString(null);
        assertNull(result);
    }

    @Test
    void testGetClaimAsListWithString() {
        List<String> result = JwtHelper.getClaimAsList("value1");
        assertEquals(1, result.size());
        assertEquals("value1", result.get(0));
    }

    @Test
    void testGetClaimAsListWithCommaSeparatedString() {
        List<String> result = JwtHelper.getClaimAsList("value1,value2,value3");
        assertEquals(3, result.size());
        assertEquals("value1", result.get(0));
        assertEquals("value2", result.get(1));
        assertEquals("value3", result.get(2));
    }

    @Test
    void testGetClaimAsListWithList() {
        List<String> input = List.of("value1", "value2");
        List<String> result = JwtHelper.getClaimAsList(input);
        assertEquals(2, result.size());
        assertEquals("value1", result.get(0));
        assertEquals("value2", result.get(1));
    }

    @Test
    void testGetClaimAsListWithArray() {
        String[] array = {"value1", "value2"};
        List<String> result = JwtHelper.getClaimAsList(array);
        assertEquals(2, result.size());
        assertEquals("value1", result.get(0));
        assertEquals("value2", result.get(1));
    }

    @Test
    void testGetClaimAsListWithNumber() {
        List<String> result = JwtHelper.getClaimAsList(123L);
        assertEquals(1, result.size());
        assertEquals("123", result.get(0));
    }

    @Test
    void testGetClaimAsListWithInvalidType() {
        List<String> result = JwtHelper.getClaimAsList(new Object());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetClaimsWithInvalidToken() {
        Map<String, Object> result = JwtHelper.getClaims("invalid-token");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
