package ti.gateway.kubernetes.ingress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IngressProperties}.
 */
class IngressPropertiesTest {

    private IngressProperties properties;

    @BeforeEach
    void setUp() {
        properties = new IngressProperties();
    }

    @Test
    void testDefaultValues() {
        assertFalse(properties.isEnabled());
        assertEquals("default", properties.getNamespace());
        assertEquals(30L, properties.getRefreshInterval());
        assertTrue(properties.isCacheEnabled());
        assertEquals(300L, properties.getCacheExpiration());
        assertTrue(properties.isTlsEnabled());
        assertEquals(80, properties.getDefaultServicePort());
        assertTrue(properties.isPathRewriteEnabled());
        assertEquals("/(.*)", properties.getPathRewritePattern());
        assertEquals("/$1", properties.getPathRewriteReplacement());
    }

    @Test
    void testEnabled() {
        properties.setEnabled(true);
        assertTrue(properties.isEnabled());
        
        properties.setEnabled(false);
        assertFalse(properties.isEnabled());
    }

    @Test
    void testNamespace() {
        properties.setNamespace("test-namespace");
        assertEquals("test-namespace", properties.getNamespace());
        
        properties.setNamespace("production");
        assertEquals("production", properties.getNamespace());
    }

    @Test
    void testRefreshInterval() {
        properties.setRefreshInterval(60L);
        assertEquals(60L, properties.getRefreshInterval());
        
        properties.setRefreshInterval(0L);
        assertEquals(0L, properties.getRefreshInterval());
    }

    @Test
    void testCacheEnabled() {
        properties.setCacheEnabled(false);
        assertFalse(properties.isCacheEnabled());
        
        properties.setCacheEnabled(true);
        assertTrue(properties.isCacheEnabled());
    }

    @Test
    void testCacheExpiration() {
        properties.setCacheExpiration(600L);
        assertEquals(600L, properties.getCacheExpiration());
        
        properties.setCacheExpiration(0L);
        assertEquals(0L, properties.getCacheExpiration());
    }

    @Test
    void testTlsEnabled() {
        properties.setTlsEnabled(false);
        assertFalse(properties.isTlsEnabled());
        
        properties.setTlsEnabled(true);
        assertTrue(properties.isTlsEnabled());
    }

    @Test
    void testDefaultServicePort() {
        properties.setDefaultServicePort(8080);
        assertEquals(8080, properties.getDefaultServicePort());
        
        properties.setDefaultServicePort(443);
        assertEquals(443, properties.getDefaultServicePort());
    }

    @Test
    void testPathRewriteEnabled() {
        properties.setPathRewriteEnabled(false);
        assertFalse(properties.isPathRewriteEnabled());
        
        properties.setPathRewriteEnabled(true);
        assertTrue(properties.isPathRewriteEnabled());
    }

    @Test
    void testPathRewritePattern() {
        properties.setPathRewritePattern("/api/(.*)");
        assertEquals("/api/(.*)", properties.getPathRewritePattern());
        
        properties.setPathRewritePattern("/(v\\d+)/(.*)");
        assertEquals("/(v\\d+)/(.*)", properties.getPathRewritePattern());
    }

    @Test
    void testPathRewriteReplacement() {
        properties.setPathRewriteReplacement("/$1");
        assertEquals("/$1", properties.getPathRewriteReplacement());
        
        properties.setPathRewriteReplacement("/api/v1/$1");
        assertEquals("/api/v1/$1", properties.getPathRewriteReplacement());
    }

    @Test
    void testAllProperties() {
        properties.setEnabled(true);
        properties.setNamespace("custom-namespace");
        properties.setRefreshInterval(120L);
        properties.setCacheEnabled(false);
        properties.setCacheExpiration(180L);
        properties.setTlsEnabled(false);
        properties.setDefaultServicePort(9090);
        properties.setPathRewriteEnabled(false);
        properties.setPathRewritePattern("/custom/(.*)");
        properties.setPathRewriteReplacement("/replaced/$1");

        assertTrue(properties.isEnabled());
        assertEquals("custom-namespace", properties.getNamespace());
        assertEquals(120L, properties.getRefreshInterval());
        assertFalse(properties.isCacheEnabled());
        assertEquals(180L, properties.getCacheExpiration());
        assertFalse(properties.isTlsEnabled());
        assertEquals(9090, properties.getDefaultServicePort());
        assertFalse(properties.isPathRewriteEnabled());
        assertEquals("/custom/(.*)", properties.getPathRewritePattern());
        assertEquals("/replaced/$1", properties.getPathRewriteReplacement());
    }
}
