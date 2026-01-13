package ti.gateway.kubernetes.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RolesSecurityGatewayFilter
 */
class RolesSecurityGatewayFilterTest {

    @Mock
    private SecurityWebFilterChain securityWebFilterChain;

    @Mock
    private GatewayFilterChain gatewayFilterChain;

    private RolesSecurityGatewayFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new RolesSecurityGatewayFilter(securityWebFilterChain);
    }

    @Test
    void testFilter() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
        );
        
        when(gatewayFilterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        Mono<Void> result = filter.filter(exchange, gatewayFilterChain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(gatewayFilterChain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testFilterWithError() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
        );
        
        RuntimeException error = new RuntimeException("Test error");
        when(gatewayFilterChain.filter(any(ServerWebExchange.class)))
                .thenReturn(Mono.error(error));
        
        Mono<Void> result = filter.filter(exchange, gatewayFilterChain);
        
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testFilterWithNullChain() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
        );
        
        // Should throw NullPointerException when chain is null
        assertThrows(NullPointerException.class, () -> {
            filter.filter(exchange, null);
        });
    }
}
