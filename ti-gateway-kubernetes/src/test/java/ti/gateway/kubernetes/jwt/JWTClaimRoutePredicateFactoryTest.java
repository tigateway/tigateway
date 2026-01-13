package ti.gateway.kubernetes.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JWTClaimRoutePredicateFactory
 */
class JWTClaimRoutePredicateFactoryTest {

    private JWTClaimRoutePredicateFactory factory;

    @BeforeEach
    void setUp() {
        factory = new JWTClaimRoutePredicateFactory();
    }

    @Test
    void testShortcutFieldOrder() {
        var fieldOrder = factory.shortcutFieldOrder();
        
        assertNotNull(fieldOrder);
        assertEquals(3, fieldOrder.size());
        assertEquals("header", fieldOrder.get(0));
        assertEquals("claim", fieldOrder.get(1));
        assertEquals("regexp", fieldOrder.get(2));
    }

    @Test
    void testApply() {
        JWTClaimRoutePredicateFactory.Config config = new JWTClaimRoutePredicateFactory.Config();
        config.setHeader("Authorization");
        config.setClaim("sub");
        config.setRegexp(".*");
        
        Predicate<ServerWebExchange> predicate = factory.apply(config);
        
        assertNotNull(predicate);
        assertTrue(predicate instanceof GatewayPredicate);
    }

    @Test
    void testConfigGettersAndSetters() {
        JWTClaimRoutePredicateFactory.Config config = new JWTClaimRoutePredicateFactory.Config();
        
        // Test default values
        assertNull(config.getHeader());
        assertNull(config.getClaim());
        assertNull(config.getRegexp());
        
        // Test setting values
        config.setHeader("Authorization");
        config.setClaim("sub");
        config.setRegexp("user.*");
        
        assertEquals("Authorization", config.getHeader());
        assertEquals("sub", config.getClaim());
        assertEquals("user.*", config.getRegexp());
    }

    @Test
    void testConfigFluentSetters() {
        JWTClaimRoutePredicateFactory.Config config = new JWTClaimRoutePredicateFactory.Config();
        
        // Test fluent setter for claim
        JWTClaimRoutePredicateFactory.Config result = config.setClaim("email");
        assertSame(config, result);
        assertEquals("email", config.getClaim());
        
        // Test fluent setter for regexp
        result = config.setRegexp(".*@example.com");
        assertSame(config, result);
        assertEquals(".*@example.com", config.getRegexp());
    }

    @Test
    void testPredicateWithEmptyHeader() {
        JWTClaimRoutePredicateFactory.Config config = new JWTClaimRoutePredicateFactory.Config();
        config.setHeader("Authorization");
        config.setClaim("sub");
        config.setRegexp(".*");
        
        Predicate<ServerWebExchange> predicate = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
        );
        
        // Empty header should return false
        boolean result = predicate.test(exchange);
        assertFalse(result);
    }

    @Test
    void testGetConfigClass() {
        Class<?> configClass = factory.getConfigClass();
        
        assertEquals(JWTClaimRoutePredicateFactory.Config.class, configClass);
    }
}
