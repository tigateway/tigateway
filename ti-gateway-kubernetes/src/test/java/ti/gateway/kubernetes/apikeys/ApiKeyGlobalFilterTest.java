package ti.gateway.kubernetes.apikeys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApiKeyGlobalFilter
 */
class ApiKeyGlobalFilterTest {

    @Mock
    private ApiKeyValidator apiKeyValidator;

    @Mock
    private GatewayFilterChain filterChain;

    private ApiKeyGlobalFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new ApiKeyGlobalFilter(apiKeyValidator);
    }

    @Test
    void testFilterWithValidApiKey() {
        String validApiKey = "valid-key-123";
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("X-Api-Key", validApiKey)
        );

        when(apiKeyValidator.keyIsValid(validApiKey)).thenReturn(Mono.just(true));
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(apiKeyValidator, times(1)).keyIsValid(validApiKey);
        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
    }

    @Test
    void testFilterWithInvalidApiKey() {
        String invalidApiKey = "invalid-key";
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("X-Api-Key", invalidApiKey)
        );

        when(apiKeyValidator.keyIsValid(invalidApiKey)).thenReturn(Mono.just(false));
        when(exchange.getResponse().setComplete()).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(apiKeyValidator, times(1)).keyIsValid(invalidApiKey);
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void testFilterWithMissingApiKey() {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
        );

        when(apiKeyValidator.keyIsValid(null)).thenReturn(Mono.just(false));
        when(exchange.getResponse().setComplete()).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(apiKeyValidator, times(1)).keyIsValid(null);
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }
}
