package ti.gateway.kubernetes.cors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CorsGatewayFilterConfig
 */
class CorsGatewayFilterConfigTest {

    private CorsGatewayFilterConfig config;

    @BeforeEach
    void setUp() {
        config = new CorsGatewayFilterConfig();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(config);
        assertNotNull(config.getCorsConfiguration());
    }

    @Test
    void testSetAllowCredentials() {
        String cors = "[allowCredentials:true]";
        config.setCors(cors);
        
        assertTrue(config.getCorsConfiguration().getAllowCredentials());
    }

    @Test
    void testSetAllowedHeaders() {
        String cors = "[allowedHeaders:Content-Type;Authorization]";
        config.setCors(cors);
        
        List<String> allowedHeaders = config.getCorsConfiguration().getAllowedHeaders();
        assertNotNull(allowedHeaders);
        assertTrue(allowedHeaders.contains("Content-Type"));
        assertTrue(allowedHeaders.contains("Authorization"));
    }

    @Test
    void testSetAllowedMethods() {
        String cors = "[allowedMethods:GET;POST;PUT]";
        config.setCors(cors);
        
        List<String> allowedMethods = config.getCorsConfiguration().getAllowedMethods();
        assertNotNull(allowedMethods);
        assertTrue(allowedMethods.contains("GET"));
        assertTrue(allowedMethods.contains("POST"));
        assertTrue(allowedMethods.contains("PUT"));
    }

    @Test
    void testSetAllowedOrigins() {
        String cors = "[allowedOrigins:http://localhost:3000;https://example.com]";
        config.setCors(cors);
        
        List<String> allowedOrigins = config.getCorsConfiguration().getAllowedOrigins();
        assertNotNull(allowedOrigins);
        assertTrue(allowedOrigins.contains("http://localhost:3000"));
        assertTrue(allowedOrigins.contains("https://example.com"));
    }

    @Test
    void testSetAllowedOriginPatterns() {
        String cors = "[allowedOriginPatterns:https://*.example.com;http://localhost:*]";
        config.setCors(cors);
        
        List<String> patterns = config.getCorsConfiguration().getAllowedOriginPatterns();
        assertNotNull(patterns);
        assertTrue(patterns.contains("https://*.example.com"));
        assertTrue(patterns.contains("http://localhost:*"));
    }

    @Test
    void testSetExposedHeaders() {
        String cors = "[exposedHeaders:X-Custom-Header;X-Another-Header]";
        config.setCors(cors);
        
        List<String> exposedHeaders = config.getCorsConfiguration().getExposedHeaders();
        assertNotNull(exposedHeaders);
        assertTrue(exposedHeaders.contains("X-Custom-Header"));
        assertTrue(exposedHeaders.contains("X-Another-Header"));
    }

    @Test
    void testSetMaxAge() {
        String cors = "[maxAge:3600]";
        config.setCors(cors);
        
        Long maxAge = config.getCorsConfiguration().getMaxAge();
        assertNotNull(maxAge);
        assertEquals(3600L, maxAge);
    }

    @Test
    void testSetMultipleCorsSettings() {
        String cors = "[allowCredentials:true,allowedHeaders:Content-Type,allowedMethods:GET;POST,maxAge:1800]";
        config.setCors(cors);
        
        assertTrue(config.getCorsConfiguration().getAllowCredentials());
        assertNotNull(config.getCorsConfiguration().getAllowedHeaders());
        assertNotNull(config.getCorsConfiguration().getAllowedMethods());
        assertEquals(1800L, config.getCorsConfiguration().getMaxAge());
    }

    @Test
    void testSetCorsWithEmptyString() {
        config.setCors("");
        
        // Should not throw exception
        assertNotNull(config.getCorsConfiguration());
    }

    @Test
    void testSetCorsWithInvalidFormat() {
        // Should handle gracefully
        config.setCors("invalid-format");
        
        assertNotNull(config.getCorsConfiguration());
    }

    @Test
    void testFindValue() {
        // Test private method behavior through public method
        String cors = "[allowedHeaders:Content-Type]";
        config.setCors(cors);
        
        List<String> headers = config.getCorsConfiguration().getAllowedHeaders();
        assertNotNull(headers);
        assertTrue(headers.contains("Content-Type"));
    }

    @Test
    void testParseList() {
        // Test private method behavior through public method
        String cors = "[allowedMethods:GET;POST;PUT;DELETE]";
        config.setCors(cors);
        
        List<String> methods = config.getCorsConfiguration().getAllowedMethods();
        assertEquals(4, methods.size());
        assertTrue(methods.contains("GET"));
        assertTrue(methods.contains("POST"));
        assertTrue(methods.contains("PUT"));
        assertTrue(methods.contains("DELETE"));
    }
}
