package ti.gateway.kubernetes.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SecurityGatewayFilter
 */
class SecurityGatewayFilterTest {

    @Mock
    private SecurityWebFilterChain securityWebFilterChain;

    @Mock
    private GatewayFilterChain gatewayFilterChain;

    @Mock
    private ServerWebExchange exchange;

    private SecurityGatewayFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new SecurityGatewayFilter(securityWebFilterChain);
    }

    @Test
    void testFilter() {
        when(gatewayFilterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
        
        Mono<Void> result = filter.filter(exchange, gatewayFilterChain);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(gatewayFilterChain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void testFilterWithError() {
        RuntimeException error = new RuntimeException("Test error");
        when(gatewayFilterChain.filter(any(ServerWebExchange.class)))
                .thenReturn(Mono.error(error));
        
        Mono<Void> result = filter.filter(exchange, gatewayFilterChain);
        
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}
