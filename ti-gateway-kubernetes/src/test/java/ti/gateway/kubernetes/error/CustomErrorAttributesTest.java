package ti.gateway.kubernetes.error;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.net.ConnectException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CustomErrorAttributes
 */
class CustomErrorAttributesTest {

    private CustomErrorAttributes errorAttributes;

    @BeforeEach
    void setUp() {
        errorAttributes = new CustomErrorAttributes();
    }

    @Test
    void testGetErrorAttributesWithResponseStatusException() {
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Resource not found"
        );
        
        ServerRequest request = ServerRequest.create(
                MockServerWebExchange.from(MockServerHttpRequest.get("/test")),
                java.util.Collections.emptyList()
        );

        // Store the exception in the exchange attributes
        request.exchange().getAttributes().put("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", exception);

        Map<String, Object> attributes = errorAttributes.getErrorAttributes(
                request, ErrorAttributeOptions.defaults()
        );

        assertNotNull(attributes);
        assertEquals(404, attributes.get("status"));
        assertEquals("Not Found", attributes.get("error"));
        assertEquals("/test", attributes.get("path"));
        assertNotNull(attributes.get("timestamp"));
        assertNotNull(attributes.get("requestId"));
    }

    @Test
    void testGetErrorAttributesWithConnectException() {
        ConnectException connectException = new ConnectException("Connection refused");
        RuntimeException exception = new RuntimeException(connectException);
        
        ServerRequest request = ServerRequest.create(
                MockServerWebExchange.from(MockServerHttpRequest.get("/test")),
                java.util.Collections.emptyList()
        );

        request.exchange().getAttributes().put("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", exception);

        Map<String, Object> attributes = errorAttributes.getErrorAttributes(
                request, ErrorAttributeOptions.defaults()
        );

        assertNotNull(attributes);
        assertEquals(503, attributes.get("status")); // SERVICE_UNAVAILABLE
        assertEquals("Service Unavailable", attributes.get("error"));
    }

    @Test
    void testGetErrorAttributesWithStackTrace() {
        RuntimeException exception = new RuntimeException("Test error");
        
        ServerRequest request = ServerRequest.create(
                MockServerWebExchange.from(MockServerHttpRequest.get("/test")),
                java.util.Collections.emptyList()
        );

        request.exchange().getAttributes().put("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", exception);

        ErrorAttributeOptions options = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE);
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(request, options);

        assertNotNull(attributes);
        assertTrue(attributes.containsKey("trace"));
        assertNotNull(attributes.get("trace"));
    }

    @Test
    void testGetErrorAttributesWithoutStackTrace() {
        RuntimeException exception = new RuntimeException("Test error");
        
        ServerRequest request = ServerRequest.create(
                MockServerWebExchange.from(MockServerHttpRequest.get("/test")),
                java.util.Collections.emptyList()
        );

        request.exchange().getAttributes().put("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", exception);

        Map<String, Object> attributes = errorAttributes.getErrorAttributes(
                request, ErrorAttributeOptions.defaults()
        );

        assertNotNull(attributes);
        assertFalse(attributes.containsKey("trace"));
    }
}
