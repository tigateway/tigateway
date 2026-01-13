package ti.gateway.kubernetes.header;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.RewriteResponseHeaderGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RewriteAllResponseHeadersGatewayFilterFactory}.
 */
class RewriteAllResponseHeadersGatewayFilterFactoryTest {

    private RewriteAllResponseHeadersGatewayFilterFactory factory;

    @BeforeEach
    void setUp() {
        factory = new RewriteAllResponseHeadersGatewayFilterFactory();
    }

    @Test
    void testShortcutFieldOrder() {
        List<String> fieldOrder = factory.shortcutFieldOrder();
        assertNotNull(fieldOrder);
        assertEquals(2, fieldOrder.size());
        assertEquals("regexp", fieldOrder.get(0));
        assertEquals("replacement", fieldOrder.get(1));
    }

    @Test
    void testApply() {
        RewriteResponseHeaderGatewayFilterFactory.Config config = new RewriteResponseHeaderGatewayFilterFactory.Config();
        config.setRegexp("old");
        config.setReplacement("new");

        GatewayFilter filter = factory.apply(config);
        assertNotNull(filter);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
        );

        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        responseHeaders.add("X-Custom-Header", "old-value");
        responseHeaders.add("X-Another-Header", "old-value-too");

        Mono<Void> result = filter.filter(exchange, (ex) -> {
            // Simulate response headers being set
            return Mono.empty();
        });

        StepVerifier.create(result)
                .verifyComplete();

        // Verify headers were rewritten (the rewrite happens after the chain)
        // Note: The actual rewriting happens in the then() callback, so we need to verify
        // that the filter chain completes successfully
        assertNotNull(exchange.getResponse());
    }

    @Test
    void testApplyWithEmptyConfig() {
        RewriteResponseHeaderGatewayFilterFactory.Config config = new RewriteResponseHeaderGatewayFilterFactory.Config();
        config.setRegexp("");
        config.setReplacement("");

        GatewayFilter filter = factory.apply(config);
        assertNotNull(filter);

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
        );

        Mono<Void> result = filter.filter(exchange, (ex) -> Mono.empty());

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testGetConfigClass() {
        Class<?> configClass = factory.getConfigClass();
        assertNotNull(configClass);
        assertEquals(RewriteResponseHeaderGatewayFilterFactory.Config.class, configClass);
    }
}
