package ti.gateway.kubernetes.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimiterProperties
 */
class RateLimiterPropertiesTest {

    private RateLimiterProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RateLimiterProperties();
    }

    @Test
    void testDefaultValues() {
        assertEquals(0, properties.getLimit());
        assertEquals(Duration.ofSeconds(1L), properties.getDuration());
        assertNull(properties.getRouteId());
        assertNull(properties.getKeyLocation());
        assertNull(properties.getClaim());
        assertNull(properties.getHeader());
        assertEquals(1, properties.getXForwardedForMaxTrustedIndex());
        assertTrue(properties.getIPs().isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        properties.setRouteId("test-route");
        properties.setLimit(100);
        properties.setDuration(Duration.ofMinutes(5));
        
        assertEquals("test-route", properties.getRouteId());
        assertEquals(100, properties.getLimit());
        assertEquals(Duration.ofMinutes(5), properties.getDuration());
    }

    @Test
    void testHasClaim() {
        assertFalse(properties.hasClaim());
        
        properties.setKeyLocation("{claim:sub}");
        assertTrue(properties.hasClaim());
        assertEquals("sub", properties.getClaim());
    }

    @Test
    void testHasHeader() {
        assertFalse(properties.hasHeader());
        
        properties.setKeyLocation("{header:X-User-Id}");
        assertTrue(properties.hasHeader());
        assertEquals("X-User-Id", properties.getHeader());
    }

    @Test
    void testHasIPs() {
        assertFalse(properties.hasIPs());
        
        properties.setKeyLocation("{IPs:192.168.1.1;10.0.0.1}");
        assertTrue(properties.hasIPs());
        assertEquals(2, properties.getIPs().size());
    }

    @Test
    void testParseKeyLocationWithClaim() {
        properties.setKeyLocation("{claim:email}");
        
        assertTrue(properties.hasClaim());
        assertEquals("email", properties.getClaim());
        assertFalse(properties.hasHeader());
        assertFalse(properties.hasIPs());
    }

    @Test
    void testParseKeyLocationWithHeader() {
        properties.setKeyLocation("{header:X-Custom-Header}");
        
        assertTrue(properties.hasHeader());
        assertEquals("X-Custom-Header", properties.getHeader());
        assertFalse(properties.hasClaim());
        assertFalse(properties.hasIPs());
    }

    @Test
    void testParseKeyLocationWithIPs() {
        properties.setKeyLocation("{IPs:192.168.1.1;10.0.0.1;172.16.0.1}");
        
        assertTrue(properties.hasIPs());
        List<String> ips = properties.getIPs();
        assertEquals(3, ips.size());
        assertTrue(ips.contains("192.168.1.1"));
        assertTrue(ips.contains("10.0.0.1"));
        assertTrue(ips.contains("172.16.0.1"));
    }

    @Test
    void testParseKeyLocationWithIPsAndMaxTrustedIndex() {
        properties.setKeyLocation("{IPs:3;192.168.1.1;10.0.0.1}");
        
        assertTrue(properties.hasIPs());
        assertEquals(3, properties.getXForwardedForMaxTrustedIndex());
        assertEquals(2, properties.getIPs().size());
    }

    @Test
    void testParseKeyLocationWithInvalidFormat() {
        properties.setKeyLocation("invalid-format");
        
        assertFalse(properties.hasClaim());
        assertFalse(properties.hasHeader());
        assertFalse(properties.hasIPs());
    }

    @Test
    void testParseKeyLocationWithEmptyBraces() {
        properties.setKeyLocation("{}");
        
        assertFalse(properties.hasClaim());
        assertFalse(properties.hasHeader());
        assertFalse(properties.hasIPs());
    }

    @Test
    void testSetKeyLocationNull() {
        properties.setKeyLocation(null);
        
        assertFalse(properties.hasClaim());
        assertFalse(properties.hasHeader());
        assertFalse(properties.hasIPs());
    }

    @Test
    void testSetKeyLocationEmpty() {
        properties.setKeyLocation("");
        
        assertFalse(properties.hasClaim());
        assertFalse(properties.hasHeader());
        assertFalse(properties.hasIPs());
    }
}
