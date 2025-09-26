# Filters

This guide covers TiGateway's filter system, which allows you to modify requests and responses as they flow through the gateway. Filters are essential for implementing cross-cutting concerns like authentication, logging, rate limiting, and request transformation.

## Overview

TiGateway filters are based on Spring Cloud Gateway's filter system and provide:

- **Request modification**: Add, remove, or modify request headers, parameters, and body
- **Response modification**: Add, remove, or modify response headers and body
- **Cross-cutting concerns**: Authentication, logging, rate limiting, circuit breaking
- **Custom logic**: Implement custom business logic in the request flow

## Filter Types

### Gateway Filters

Gateway filters are applied to individual routes and can modify requests and responses.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service,user-service
            - AddResponseHeader=X-Response-Time,${T(java.lang.System).currentTimeMillis()}
```

### Global Filters

Global filters are applied to all routes and are useful for cross-cutting concerns.

```java
@Component
@Order(-1)
public class GlobalLoggingFilter implements GlobalFilter {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalLoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        log.info("Request: {} {}", request.getMethod(), request.getURI());
        
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    log.info("Response: {}", response.getStatusCode());
                }));
    }
}
```

## Built-in Filters

### Request Header Filters

#### AddRequestHeader

Adds a header to the request.

```yaml
filters:
  - AddRequestHeader=X-Service,user-service
  - AddRequestHeader=X-Request-ID,${T(java.util.UUID).randomUUID()}
  - AddRequestHeader=X-User-ID,${T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()}
```

#### RemoveRequestHeader

Removes a header from the request.

```yaml
filters:
  - RemoveRequestHeader=X-Forwarded-For
  - RemoveRequestHeader=X-Real-IP
```

#### SetRequestHeader

Sets a header value, replacing any existing value.

```yaml
filters:
  - SetRequestHeader=X-Service,user-service
  - SetRequestHeader=X-Version,v1
```

### Response Header Filters

#### AddResponseHeader

Adds a header to the response.

```yaml
filters:
  - AddResponseHeader=X-Response-Time,${T(java.lang.System).currentTimeMillis()}
  - AddResponseHeader=X-Service,user-service
  - AddResponseHeader=X-Request-ID,${T(java.util.UUID).randomUUID()}
```

#### RemoveResponseHeader

Removes a header from the response.

```yaml
filters:
  - RemoveResponseHeader=X-Powered-By
  - RemoveResponseHeader=Server
```

#### SetResponseHeader

Sets a response header value.

```yaml
filters:
  - SetResponseHeader=X-Service,user-service
  - SetResponseHeader=X-Version,v1
```

### Path Filters

#### StripPrefix

Removes a prefix from the request path.

```yaml
filters:
  - StripPrefix=2  # Removes /api/users from /api/users/123
```

#### PrefixPath

Adds a prefix to the request path.

```yaml
filters:
  - PrefixPath=/api/v1  # Adds /api/v1 to the path
```

#### RewritePath

Rewrites the request path using regex.

```yaml
filters:
  - RewritePath=/api/users/(?<segment>.*), /${segment}
  - RewritePath=/api/(?<service>.*)/(?<path>.*), /${service}/${path}
```

### Request Parameter Filters

#### AddRequestParameter

Adds a query parameter to the request.

```yaml
filters:
  - AddRequestParameter=version,v1
  - AddRequestParameter=debug,true
```

#### RemoveRequestParameter

Removes a query parameter from the request.

```yaml
filters:
  - RemoveRequestParameter=debug
  - RemoveRequestParameter=test
```

### Circuit Breaker Filter

Implements circuit breaker pattern for fault tolerance.

```yaml
filters:
  - name: CircuitBreaker
    args:
      name: user-service-cb
      fallbackUri: forward:/fallback
      statusCodes: BAD_GATEWAY,GATEWAY_TIMEOUT
```

#### Fallback Configuration

```java
@RestController
public class FallbackController {
    
    @GetMapping("/fallback")
    public ResponseEntity<Map<String, Object>> fallback(ServerHttpRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service temporarily unavailable");
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", request.getURI().getPath());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
```

### Rate Limiting Filter

Implements rate limiting using Redis.

```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
      redis-rate-limiter.requestedTokens: 1
      key-resolver: "#{@userKeyResolver}"
```

#### Key Resolver Configuration

```java
@Configuration
public class RateLimiterConfig {
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getHeaders().getFirst("X-User-ID")
        );
    }
    
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }
    
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getURI().getPath()
        );
    }
}
```

### Retry Filter

Implements retry logic for failed requests.

```yaml
filters:
  - name: Retry
    args:
      retries: 3
      statuses: BAD_GATEWAY,GATEWAY_TIMEOUT
      methods: GET,POST
      backoff:
        firstBackoff: 50ms
        maxBackoff: 500ms
        factor: 2
        basedOnPreviousValue: false
```

### Request Size Filter

Limits the size of request bodies.

```yaml
filters:
  - name: RequestSize
    args:
      maxSize: 5MB
```

### Redirect Filter

Redirects requests to a different URL.

```yaml
filters:
  - name: RedirectTo
    args:
      status: 302
      url: https://new-api.example.com
```

## Custom Filters

### Creating Custom Gateway Filters

```java
@Component
public class CustomGatewayFilter implements GatewayFilter, Ordered {
    
    private static final Logger log = LoggerFactory.getLogger(CustomGatewayFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Pre-processing
        log.info("Processing request: {}", request.getURI());
        
        // Add custom header
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Custom-Filter", "processed")
                .build();
        
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        
        return chain.filter(modifiedExchange)
                .then(Mono.fromRunnable(() -> {
                    // Post-processing
                    ServerHttpResponse response = exchange.getResponse();
                    log.info("Response status: {}", response.getStatusCode());
                }));
    }
    
    @Override
    public int getOrder() {
        return -1; // Higher priority (lower number = higher priority)
    }
}
```

### Using Custom Filters

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: custom-filter-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: CustomGatewayFilter
            - StripPrefix=2
```

### Filter Factory

For more complex filters with configuration:

```java
@Component
public class CustomFilterFactory extends AbstractGatewayFilterFactory<CustomFilterFactory.Config> {
    
    public CustomFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Use configuration
            if (config.isEnabled()) {
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header(config.getHeaderName(), config.getHeaderValue())
                        .build();
                
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();
                
                return chain.filter(modifiedExchange);
            }
            
            return chain.filter(exchange);
        };
    }
    
    @Data
    public static class Config {
        private boolean enabled = true;
        private String headerName = "X-Custom";
        private String headerValue = "processed";
    }
}
```

### Using Filter Factory

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: custom-factory-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: CustomFilterFactory
              args:
                enabled: true
                headerName: X-Custom-Header
                headerValue: custom-value
```

## Authentication and Authorization Filters

### JWT Authentication Filter

```java
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String token = getTokenFromRequest(request);
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User", username)
                    .build();
            
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();
            
            return chain.filter(modifiedExchange);
        }
        
        return unauthorized(exchange);
    }
    
    private String getTokenFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Unauthorized\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -100; // High priority
    }
}
```

### Role-Based Authorization Filter

```java
@Component
public class RoleAuthorizationFilter implements GlobalFilter, Ordered {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public RoleAuthorizationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Check if path requires admin role
        if (path.startsWith("/api/admin/")) {
            String token = getTokenFromRequest(request);
            
            if (token != null && jwtTokenProvider.validateToken(token)) {
                List<String> roles = jwtTokenProvider.getRolesFromToken(token);
                
                if (roles.contains("ADMIN")) {
                    return chain.filter(exchange);
                }
            }
            
            return forbidden(exchange);
        }
        
        return chain.filter(exchange);
    }
    
    private String getTokenFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private Mono<Void> forbidden(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Forbidden\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -99; // After authentication
    }
}
```

## Logging and Monitoring Filters

### Request Logging Filter

```java
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        // Add request ID to headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .build();
        
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        
        log.info("Request started: {} {} - ID: {}", 
                request.getMethod(), request.getURI(), requestId);
        
        return chain.filter(modifiedExchange)
                .then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    ServerHttpResponse response = exchange.getResponse();
                    
                    log.info("Request completed: {} {} - ID: {} - Status: {} - Duration: {}ms",
                            request.getMethod(), request.getURI(), requestId, 
                            response.getStatusCode(), duration);
                }));
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

### Metrics Filter

```java
@Component
public class MetricsFilter implements GlobalFilter, Ordered {
    
    private final MeterRegistry meterRegistry;
    private final Timer requestTimer;
    private final Counter requestCounter;
    
    public MetricsFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.requestTimer = Timer.builder("gateway.requests.duration")
                .description("Request duration")
                .register(meterRegistry);
        
        this.requestCounter = Counter.builder("gateway.requests.total")
                .description("Total requests")
                .register(meterRegistry);
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        return chain.filter(exchange)
                .doOnSuccess(result -> {
                    sample.stop(requestTimer.tags("path", path, "method", method, "status", "success"));
                    requestCounter.increment(Tags.of("path", path, "method", method, "status", "success"));
                })
                .doOnError(error -> {
                    sample.stop(requestTimer.tags("path", path, "method", method, "status", "error"));
                    requestCounter.increment(Tags.of("path", path, "method", method, "status", "error"));
                });
    }
    
    @Override
    public int getOrder() {
        return -2;
    }
}
```

## Filter Ordering

### Order Priority

Filters are executed in order of their priority:

1. **Global filters** (with @Order annotation)
2. **Route-specific filters** (in configuration order)
3. **Built-in filters** (in configuration order)

### Order Configuration

```java
@Component
@Order(-100) // High priority
public class HighPriorityFilter implements GlobalFilter {
    // Implementation
}

@Component
@Order(0) // Medium priority
public class MediumPriorityFilter implements GlobalFilter {
    // Implementation
}

@Component
@Order(100) // Low priority
public class LowPriorityFilter implements GlobalFilter {
    // Implementation
}
```

### Route Filter Order

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: ordered-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2  # Executed first
            - AddRequestHeader=X-Service,user-service  # Executed second
            - CircuitBreaker=user-service-cb  # Executed third
```

## Best Practices

### Filter Design

1. **Keep filters focused**: Each filter should have a single responsibility
2. **Use appropriate order**: Order filters by their dependencies
3. **Handle errors gracefully**: Implement proper error handling
4. **Use async operations**: Leverage reactive programming
5. **Test thoroughly**: Test filters in isolation and integration

### Performance Considerations

1. **Minimize processing**: Keep filter logic lightweight
2. **Use caching**: Cache expensive operations
3. **Avoid blocking operations**: Use non-blocking I/O
4. **Monitor performance**: Track filter execution time
5. **Optimize order**: Place frequently used filters first

### Security Best Practices

1. **Validate input**: Always validate filter inputs
2. **Sanitize data**: Sanitize user-provided data
3. **Use secure defaults**: Implement secure default configurations
4. **Log security events**: Log authentication and authorization events
5. **Regular audits**: Regularly audit filter configurations

## Troubleshooting

### Common Issues

#### Filter Not Executing

```bash
# Check filter configuration
curl http://localhost:8080/actuator/gateway/routes

# Check filter order
curl http://localhost:8080/actuator/gateway/routefilters

# Check application logs
tail -f logs/tigateway.log | grep -i filter
```

#### Filter Performance Issues

```bash
# Check filter execution time
curl http://localhost:8080/actuator/metrics/gateway.requests.duration

# Monitor filter errors
curl http://localhost:8080/actuator/metrics/gateway.requests.total
```

#### Filter Configuration Errors

```bash
# Validate configuration
java -jar tigateway.jar --spring.config.location=application.yml --debug

# Check configuration properties
curl http://localhost:8080/actuator/configprops
```

### Debug Commands

```bash
# List all filters
curl http://localhost:8080/actuator/gateway/routefilters

# Check route filters
curl http://localhost:8080/actuator/gateway/routes/{route-id}

# Refresh routes
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

## Next Steps

After configuring filters:

1. **[Routes and Predicates](./routes-and-predicates.md)** - Learn about route configuration
2. **[Authentication and Authorization](./authentication-and-authorization.md)** - Implement security
3. **[Monitoring Setup](../monitoring-and-metrics.md)** - Monitor filter performance
4. **[Troubleshooting Guide](../troubleshooting.md)** - Common filter issues

---

**Ready to implement security?** Check out our [Authentication and Authorization](./authentication-and-authorization.md) guide for comprehensive security implementation.
