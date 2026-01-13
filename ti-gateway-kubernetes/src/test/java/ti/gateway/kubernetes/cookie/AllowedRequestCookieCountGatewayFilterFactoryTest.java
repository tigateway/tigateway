package ti.gateway.kubernetes.cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AllowedRequestCookieCountGatewayFilterFactory
 */
class AllowedRequestCookieCountGatewayFilterFactoryTest {

    private AllowedRequestCookieCountGatewayFilterFactory factory;
    private AllowedRequestCookieCountGatewayFilterFactory.Config config;

    @BeforeEach
    void setUp() {
        factory = new AllowedRequestCookieCountGatewayFilterFactory();
        config = new AllowedRequestCookieCountGatewayFilterFactory.Config();
    }

    @Test
    void testApply() {
        config.setCookieCount(10);
        
        GatewayFilter filter = factory.apply(config);
        
        assertNotNull(filter);
    }

    @Test
    void testFilterWithAllowedCookieCount() {
        config.setCookieCount(5);
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .cookie(new HttpCookie("cookie1", "value1"))
                        .cookie(new HttpCookie("cookie2", "value2"))
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        // Status code is not set when request is allowed, so it should be null
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void testFilterWithExceededCookieCount() {
        config.setCookieCount(2);
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .cookie(new HttpCookie("cookie1", "value1"))
                        .cookie(new HttpCookie("cookie2", "value2"))
                        .cookie(new HttpCookie("cookie3", "value3"))
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain, never()).filter(any(ServerWebExchange.class));
        assertEquals(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE, exchange.getResponse().getStatusCode());
        assertEquals("Request exceeded the maximum of cookies",
                exchange.getResponse().getHeaders().getFirst("errorMessage"));
    }

    @Test
    void testFilterWithExactCookieCount() {
        config.setCookieCount(3);
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .cookie(new HttpCookie("cookie1", "value1"))
                        .cookie(new HttpCookie("cookie2", "value2"))
                        .cookie(new HttpCookie("cookie3", "value3"))
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testFilterWithNoCookies() {
        config.setCookieCount(5);
        
        GatewayFilter filter = factory.apply(config);
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
        );
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        Mono<Void> result = filter.filter(exchange, chain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testShortcutFieldOrder() {
        List<String> fieldOrder = factory.shortcutFieldOrder();
        
        assertNotNull(fieldOrder);
        assertEquals(1, fieldOrder.size());
        assertEquals("cookieCount", fieldOrder.get(0));
    }

    @Test
    void testConfigGettersAndSetters() {
        assertEquals(0, config.getCookieCount());
        
        config.setCookieCount(20);
        assertEquals(20, config.getCookieCount());
    }
}
