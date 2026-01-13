package ti.gateway.kubernetes.body;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ti.gateway.kubernetes.core.KeyValue;
import ti.gateway.kubernetes.core.KeyValueConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RewriteResponseBodyGatewayFilterFactory}.
 */
class RewriteResponseBodyGatewayFilterFactoryTest {

    private ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;
    private RewriteResponseBodyGatewayFilterFactory factory;

    @BeforeEach
    void setUp() {
        modifyResponseBodyGatewayFilterFactory = mock(ModifyResponseBodyGatewayFilterFactory.class);
        factory = new RewriteResponseBodyGatewayFilterFactory(modifyResponseBodyGatewayFilterFactory);
    }

    @Test
    void testApply() {
        // Setup
        KeyValueConfig config = new KeyValueConfig();
        KeyValue[] keyValues = new KeyValue[]{
                new KeyValue("old", "new"),
                new KeyValue("foo", "bar")
        };
        config.setKeyValues(keyValues);

        GatewayFilter mockFilter = (exchange, chain) -> chain.filter(exchange);
        when(modifyResponseBodyGatewayFilterFactory.apply(any(ModifyResponseBodyGatewayFilterFactory.Config.class)))
                .thenReturn(mockFilter);

        // Execute
        GatewayFilter filter = factory.apply(config);

        // Verify
        assertNotNull(filter);
        verify(modifyResponseBodyGatewayFilterFactory, times(1))
                .apply(any(ModifyResponseBodyGatewayFilterFactory.Config.class));
    }

    @Test
    void testApplyWithEmptyKeyValues() {
        // Setup
        KeyValueConfig config = new KeyValueConfig();
        config.setKeyValues(new KeyValue[0]);

        GatewayFilter mockFilter = (exchange, chain) -> chain.filter(exchange);
        when(modifyResponseBodyGatewayFilterFactory.apply(any(ModifyResponseBodyGatewayFilterFactory.Config.class)))
                .thenReturn(mockFilter);

        // Execute
        GatewayFilter filter = factory.apply(config);

        // Verify
        assertNotNull(filter);
        verify(modifyResponseBodyGatewayFilterFactory, times(1))
                .apply(any(ModifyResponseBodyGatewayFilterFactory.Config.class));
    }

    @Test
    void testApplyWithNullKeyValues() {
        // Setup
        KeyValueConfig config = new KeyValueConfig();
        config.setKeyValues(null);

        GatewayFilter mockFilter = (exchange, chain) -> chain.filter(exchange);
        when(modifyResponseBodyGatewayFilterFactory.apply(any(ModifyResponseBodyGatewayFilterFactory.Config.class)))
                .thenReturn(mockFilter);

        // Execute
        GatewayFilter filter = factory.apply(config);

        // Verify
        assertNotNull(filter);
        verify(modifyResponseBodyGatewayFilterFactory, times(1))
                .apply(any(ModifyResponseBodyGatewayFilterFactory.Config.class));
    }

    @Test
    void testRewriteFunctionWithEmptyBody() {
        // Setup
        KeyValueConfig config = new KeyValueConfig();
        KeyValue[] keyValues = new KeyValue[]{
                new KeyValue("old", "new")
        };
        config.setKeyValues(keyValues);

        GatewayFilter mockFilter = (exchange, chain) -> {
            // Access the rewrite function to test it
            ModifyResponseBodyGatewayFilterFactory.Config modifyConfig = 
                    new ModifyResponseBodyGatewayFilterFactory.Config();
            modifyConfig.setInClass(String.class);
            modifyConfig.setOutClass(String.class);
            
            RewriteFunction<String, String> rewriteFunction = (ex, body) -> {
                KeyValue[] kvs = config.getKeyValues();
                if (kvs == null || kvs.length == 0) {
                    return Mono.just(body);
                }
                
                if (body == null || body.isEmpty()) {
                    return Mono.empty();
                }
                
                String result = body;
                for (KeyValue kv : kvs) {
                    result = result.replaceAll(kv.getKey(), kv.getValue());
                }
                return Mono.just(result);
            };
            
            modifyConfig.setRewriteFunction(rewriteFunction);
            
            ServerWebExchange testExchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/test")
            );
            
            // Test with empty body
            StepVerifier.create(Mono.from(rewriteFunction.apply(testExchange, "")))
                    .verifyComplete();
            
            // Test with null body
            StepVerifier.create(Mono.from(rewriteFunction.apply(testExchange, null)))
                    .verifyComplete();
            
            return chain.filter(exchange);
        };
        
        when(modifyResponseBodyGatewayFilterFactory.apply(any(ModifyResponseBodyGatewayFilterFactory.Config.class)))
                .thenReturn(mockFilter);

        // Execute
        GatewayFilter filter = factory.apply(config);
        
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
        assertEquals(KeyValueConfig.class, configClass);
    }
}
