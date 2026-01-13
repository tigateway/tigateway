package ti.gateway.kubernetes.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilter;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitGatewayFilterFactory
 */
class RateLimitGatewayFilterFactoryTest {

    @Mock
    private DefaultRateLimiter defaultRateLimiter;

    private RateLimitGatewayFilterFactory factory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        factory = new RateLimitGatewayFilterFactory(defaultRateLimiter);
    }

    @Test
    void testApply() {
        RateLimiterProperties config = new RateLimiterProperties();
        config.setRouteId("test-route");
        config.setLimit(100);
        config.setDuration(Duration.ofSeconds(60));
        
        @SuppressWarnings("unchecked")
        Map<String, RateLimiterProperties> configMap = mock(Map.class);
        when(defaultRateLimiter.getConfig()).thenReturn(configMap);
        
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
        verify(defaultRateLimiter, times(1)).getConfig();
    }

    @Test
    void testApplyWithClaimKeyLocation() {
        RateLimiterProperties config = new RateLimiterProperties();
        config.setRouteId("test-route");
        config.setLimit(100);
        config.setDuration(Duration.ofSeconds(60));
        config.setKeyLocation("{claim:sub}");
        
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
        assertTrue(config.hasClaim());
        assertEquals("sub", config.getClaim());
    }

    @Test
    void testApplyWithHeaderKeyLocation() {
        RateLimiterProperties config = new RateLimiterProperties();
        config.setRouteId("test-route");
        config.setLimit(100);
        config.setDuration(Duration.ofSeconds(60));
        config.setKeyLocation("{header:X-User-Id}");
        
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
        assertTrue(config.hasHeader());
        assertEquals("X-User-Id", config.getHeader());
    }

    @Test
    void testApplyWithIPsKeyLocation() {
        RateLimiterProperties config = new RateLimiterProperties();
        config.setRouteId("test-route");
        config.setLimit(100);
        config.setDuration(Duration.ofSeconds(60));
        config.setKeyLocation("{IPs:2;192.168.1.1;10.0.0.1}");
        
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
        assertTrue(config.hasIPs());
        assertEquals(2, config.getIPs().size());
        assertTrue(config.getIPs().contains("192.168.1.1"));
        assertTrue(config.getIPs().contains("10.0.0.1"));
        assertEquals(2, config.getXForwardedForMaxTrustedIndex());
    }

    @Test
    void testNewConfig() {
        RateLimiterProperties config = factory.newConfig();
        
        assertNotNull(config);
    }

    @Test
    void testGetConfigClass() {
        Class<?> configClass = factory.getConfigClass();
        
        assertEquals(RateLimiterProperties.class, configClass);
    }

    @Test
    void testShortcutFieldOrder() {
        List<String> fieldOrder = factory.shortcutFieldOrder();
        
        assertNotNull(fieldOrder);
        assertEquals(3, fieldOrder.size());
        assertTrue(fieldOrder.contains("limit"));
        assertTrue(fieldOrder.contains("duration"));
        assertTrue(fieldOrder.contains("keyLocation"));
    }
}
