# Advanced Configuration Examples

This guide provides advanced configuration examples for TiGateway, covering complex routing scenarios, custom filters, integration patterns, and production-ready configurations.

## Table of Contents

1. [Complex Routing Scenarios](#1-complex-routing-scenarios)
2. [Custom Filters](#2-custom-filters)
3. [Integration Patterns](#3-integration-patterns)
4. [Production Configurations](#4-production-configurations)
5. [Performance Optimization](#5-performance-optimization)
6. [Security Configurations](#6-security-configurations)
7. [Monitoring and Observability](#7-monitoring-and-observability)

## 1. Complex Routing Scenarios

### 1.1 A/B Testing with Weighted Routing

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Version A (70% traffic)
        - id: user-service-v1
          uri: lb://user-service-v1
          predicates:
            - Path=/api/users/**
            - Weight=user-service,70
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Version,v1
            - AddRequestHeader=X-AB-Test,version-a
        
        # Version B (30% traffic)
        - id: user-service-v2
          uri: lb://user-service-v2
          predicates:
            - Path=/api/users/**
            - Weight=user-service,30
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Version,v2
            - AddRequestHeader=X-AB-Test,version-b
```

### 1.2 Canary Deployment with Header-Based Routing

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Canary route for beta users
        - id: user-service-canary
          uri: lb://user-service-canary
          predicates:
            - Path=/api/users/**
            - Header=X-Beta-User,true
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Canary,true
            - AddRequestHeader=X-Service-Version,canary
        
        # Stable route for regular users
        - id: user-service-stable
          uri: lb://user-service-stable
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service-Version,stable
```

### 1.3 Multi-Tenant Routing

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Tenant A routing
        - id: tenant-a-route
          uri: lb://tenant-a-service
          predicates:
            - Path=/api/tenant-a/**
            - Header=X-Tenant-ID,tenant-a
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Tenant,tenant-a
            - AddRequestHeader=X-Data-Center,us-east-1
        
        # Tenant B routing
        - id: tenant-b-route
          uri: lb://tenant-b-service
          predicates:
            - Path=/api/tenant-b/**
            - Header=X-Tenant-ID,tenant-b
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Tenant,tenant-b
            - AddRequestHeader=X-Data-Center,eu-west-1
        
        # Default tenant routing
        - id: default-tenant-route
          uri: lb://default-tenant-service
          predicates:
            - Path=/api/**
          filters:
            - StripPrefix=1
            - AddRequestHeader=X-Tenant,default
```

### 1.4 Geographic Routing

```yaml
spring:
  cloud:
    gateway:
      routes:
        # US East routing
        - id: us-east-route
          uri: lb://us-east-service
          predicates:
            - Path=/api/**
            - RemoteAddr=192.168.1.0/24,10.0.0.0/8
          filters:
            - StripPrefix=1
            - AddRequestHeader=X-Region,us-east
            - AddRequestHeader=X-Data-Center,us-east-1
        
        # EU West routing
        - id: eu-west-route
          uri: lb://eu-west-service
          predicates:
            - Path=/api/**
            - RemoteAddr=172.16.0.0/12
          filters:
            - StripPrefix=1
            - AddRequestHeader=X-Region,eu-west
            - AddRequestHeader=X-Data-Center,eu-west-1
        
        # Default routing
        - id: default-route
          uri: lb://default-service
          predicates:
            - Path=/api/**
          filters:
            - StripPrefix=1
            - AddRequestHeader=X-Region,default
```

## 2. Custom Filters

### 2.1 Custom Authentication Filter

```java
@Component
public class CustomAuthenticationFilter implements GatewayFilter, Ordered {
    
    private final JwtService jwtService;
    private final UserService userService;
    
    public CustomAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Extract token from header or cookie
        String token = extractToken(request);
        
        if (token == null) {
            return unauthorized(exchange, "Missing authentication token");
        }
        
        try {
            // Validate token
            Claims claims = jwtService.validateToken(token);
            String userId = claims.getSubject();
            
            // Get user details
            return userService.getUserById(userId)
                    .flatMap(user -> {
                        // Add user info to request headers
                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header("X-User-Id", user.getId())
                                .header("X-User-Roles", String.join(",", user.getRoles()))
                                .header("X-User-Permissions", String.join(",", user.getPermissions()))
                                .build();
                        
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    })
                    .onErrorResume(throwable -> unauthorized(exchange, "Invalid token"));
                    
        } catch (Exception e) {
            return unauthorized(exchange, "Invalid token");
        }
    }
    
    private String extractToken(ServerHttpRequest request) {
        // Try Authorization header first
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // Try cookie
        HttpCookie cookie = request.getCookies().getFirst("auth-token");
        if (cookie != null) {
            return cookie.getValue();
        }
        
        return null;
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -100; // High priority
    }
}
```

### 2.2 Custom Rate Limiting Filter

```java
@Component
public class CustomRateLimitingFilter implements GatewayFilter, Ordered {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitConfig rateLimitConfig;
    
    public CustomRateLimitingFilter(RedisTemplate<String, String> redisTemplate, 
                                   RateLimitConfig rateLimitConfig) {
        this.redisTemplate = redisTemplate;
        this.rateLimitConfig = rateLimitConfig;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String key = generateKey(exchange);
        
        return checkRateLimit(key)
                .flatMap(allowed -> {
                    if (allowed) {
                        return chain.filter(exchange);
                    } else {
                        return rateLimitExceeded(exchange);
                    }
                });
    }
    
    private String generateKey(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Use user ID if available, otherwise use IP address
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null) {
            return "rate_limit:user:" + userId;
        }
        
        String clientIp = getClientIp(request);
        return "rate_limit:ip:" + clientIp;
    }
    
    private Mono<Boolean> checkRateLimit(String key) {
        return Mono.fromCallable(() -> {
            String script = """
                local key = KEYS[1]
                local limit = tonumber(ARGV[1])
                local window = tonumber(ARGV[2])
                local current = redis.call('GET', key)
                
                if current == false then
                    redis.call('SET', key, 1)
                    redis.call('EXPIRE', key, window)
                    return 1
                end
                
                local count = tonumber(current)
                if count < limit then
                    redis.call('INCR', key)
                    return 1
                else
                    return 0
                end
                """;
            
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);
            
            Long result = redisTemplate.execute(redisScript, 
                    Collections.singletonList(key),
                    rateLimitConfig.getLimit(),
                    rateLimitConfig.getWindow());
            
            return result != null && result == 1;
        });
    }
    
    private Mono<Void> rateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("Retry-After", "60");
        
        String body = "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
    
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    
    @Override
    public int getOrder() {
        return -50;
    }
}
```

### 2.3 Custom Logging Filter

```java
@Component
public class CustomLoggingFilter implements GatewayFilter, Ordered {
    
    private final Logger logger = LoggerFactory.getLogger(CustomLoggingFilter.class);
    private final ObjectMapper objectMapper;
    
    public CustomLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = UUID.randomUUID().toString();
        
        // Add request ID to headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .build();
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .doOnSuccess(aVoid -> logRequest(exchange, requestId, startTime, true, null))
                .doOnError(throwable -> logRequest(exchange, requestId, startTime, false, throwable));
    }
    
    private void logRequest(ServerWebExchange exchange, String requestId, long startTime, 
                           boolean success, Throwable error) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        long duration = System.currentTimeMillis() - startTime;
        
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("requestId", requestId);
            logData.put("method", request.getMethod().name());
            logData.put("path", request.getPath().value());
            logData.put("queryParams", request.getQueryParams());
            logData.put("headers", filterHeaders(request.getHeaders()));
            logData.put("status", response.getStatusCode().value());
            logData.put("duration", duration);
            logData.put("success", success);
            logData.put("timestamp", Instant.now().toString());
            
            if (error != null) {
                logData.put("error", error.getMessage());
                logData.put("errorType", error.getClass().getSimpleName());
            }
            
            String logMessage = objectMapper.writeValueAsString(logData);
            
            if (success) {
                logger.info("Request processed: {}", logMessage);
            } else {
                logger.error("Request failed: {}", logMessage);
            }
            
        } catch (Exception e) {
            logger.error("Failed to log request", e);
        }
    }
    
    private Map<String, String> filterHeaders(HttpHeaders headers) {
        Map<String, String> filteredHeaders = new HashMap<>();
        
        headers.forEach((name, values) -> {
            // Filter out sensitive headers
            if (!isSensitiveHeader(name)) {
                filteredHeaders.put(name, String.join(",", values));
            }
        });
        
        return filteredHeaders;
    }
    
    private boolean isSensitiveHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("authorization") ||
               lowerName.contains("cookie") ||
               lowerName.contains("x-api-key") ||
               lowerName.contains("password");
    }
    
    @Override
    public int getOrder() {
        return -200; // Very high priority
    }
}
```

## 3. Integration Patterns

### 3.1 Service Mesh Integration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: service-mesh-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service-Mesh,istio
            - AddRequestHeader=X-Trace-Id,${traceId}
            - AddRequestHeader=X-Span-Id,${spanId}
```

### 3.2 API Gateway Integration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: api-gateway-route
          uri: lb://api-gateway
          predicates:
            - Path=/api/**
          filters:
            - StripPrefix=1
            - AddRequestHeader=X-Gateway-Type,api-gateway
            - AddRequestHeader=X-Request-Source,tigateway
```

### 3.3 Message Queue Integration

```java
@Component
public class MessageQueueIntegrationFilter implements GatewayFilter {
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    
    public MessageQueueIntegrationFilter(RabbitTemplate rabbitTemplate, 
                                        ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> publishEvent(exchange, "SUCCESS"))
                .doOnError(throwable -> publishEvent(exchange, "ERROR", throwable));
    }
    
    private void publishEvent(ServerWebExchange exchange, String status) {
        publishEvent(exchange, status, null);
    }
    
    private void publishEvent(ServerWebExchange exchange, String status, Throwable error) {
        try {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "GATEWAY_REQUEST");
            event.put("status", status);
            event.put("method", request.getMethod().name());
            event.put("path", request.getPath().value());
            event.put("statusCode", response.getStatusCode().value());
            event.put("timestamp", Instant.now().toString());
            event.put("requestId", request.getHeaders().getFirst("X-Request-ID"));
            
            if (error != null) {
                event.put("error", error.getMessage());
            }
            
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend("gateway.events", message);
            
        } catch (Exception e) {
            // Log error but don't fail the request
            logger.error("Failed to publish event", e);
        }
    }
}
```

## 4. Production Configurations

### 4.1 High Availability Configuration

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 1000
        response-timeout: 5000
        pool:
          max-connections: 500
          max-idle-time: 30s
          max-life-time: 60s
          pending-acquire-timeout: 60s
          pending-acquire-max-count: 1000
      routes:
        - id: ha-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: user-service-cb
                fallbackUri: forward:/fallback/user
                failure-threshold: 5
                timeout: 30s
                reset-timeout: 60s
            - name: Retry
              args:
                retries: 3
                backoff:
                  first-backoff: 100ms
                  max-backoff: 1s
                  multiplier: 2
```

### 4.2 Security Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: secure-route
          uri: lb://secure-service
          predicates:
            - Path=/api/secure/**
            - Header=Authorization, Bearer .+
          filters:
            - StripPrefix=2
            - name: JwtAuthenticationFilter
            - name: RoleAuthorizationFilter
              args:
                required-roles: ["ADMIN", "USER"]
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
                key-resolver: "#{@userKeyResolver}"
            - name: AddRequestHeader
              args:
                name: X-Security-Level
                value: high
```

### 4.3 Performance Configuration

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 500
        response-timeout: 3000
        pool:
          max-connections: 1000
          max-idle-time: 60s
          max-life-time: 120s
      routes:
        - id: performance-route
          uri: lb://performance-service
          predicates:
            - Path=/api/performance/**
          filters:
            - StripPrefix=2
            - name: CacheRequest
              args:
                cache-name: request-cache
                ttl: 300s
            - name: CacheResponse
              args:
                cache-name: response-cache
                ttl: 600s
            - name: CompressResponse
              args:
                min-response-size: 1024
```

## 5. Performance Optimization

### 5.1 Connection Pooling

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 1000
          max-idle-time: 60s
          max-life-time: 120s
          pending-acquire-timeout: 60s
          pending-acquire-max-count: 1000
          evict-in-background: true
          eviction-interval: 30s
```

### 5.2 Caching Configuration

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000
      cache-null-values: false
      enable-statistics: true
  cloud:
    gateway:
      routes:
        - id: cached-route
          uri: lb://cached-service
          predicates:
            - Path=/api/cached/**
          filters:
            - StripPrefix=2
            - name: CacheRequest
              args:
                cache-name: request-cache
                ttl: 300s
            - name: CacheResponse
              args:
                cache-name: response-cache
                ttl: 600s
```

### 5.3 Compression Configuration

```yaml
server:
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
    min-response-size: 1024

spring:
  cloud:
    gateway:
      routes:
        - id: compressed-route
          uri: lb://compressed-service
          predicates:
            - Path=/api/compressed/**
          filters:
            - StripPrefix=2
            - name: CompressResponse
              args:
                min-response-size: 1024
                compression-level: 6
```

## 6. Security Configurations

### 6.1 CORS Configuration

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: 
              - "https://example.com"
              - "https://app.example.com"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowed-headers:
              - "*"
            allow-credentials: true
            max-age: 3600
```

### 6.2 CSRF Protection

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: csrf-protected-route
          uri: lb://csrf-protected-service
          predicates:
            - Path=/api/csrf/**
          filters:
            - StripPrefix=2
            - name: CsrfProtectionFilter
            - name: AddRequestHeader
              args:
                name: X-CSRF-Protected
                value: true
```

### 6.3 Input Validation

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: validated-route
          uri: lb://validated-service
          predicates:
            - Path=/api/validated/**
          filters:
            - StripPrefix=2
            - name: InputValidationFilter
              args:
                max-request-size: 10485760
                allowed-content-types: 
                  - application/json
                  - application/xml
                validation-rules:
                  - field: "email"
                    pattern: "^[A-Za-z0-9+_.-]+@(.+)$"
                  - field: "phone"
                    pattern: "^\\+?[1-9]\\d{1,14}$"
```

## 7. Monitoring and Observability

### 7.1 Metrics Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: tigateway
      environment: production
      region: us-east-1
```

### 7.2 Tracing Configuration

```yaml
spring:
  sleuth:
    zipkin:
      base-url: http://zipkin:9411
    sampler:
      probability: 1.0
  cloud:
    gateway:
      routes:
        - id: traced-route
          uri: lb://traced-service
          predicates:
            - Path=/api/traced/**
          filters:
            - StripPrefix=2
            - name: AddRequestHeader
              args:
                name: X-Trace-Id
                value: "${traceId}"
            - name: AddRequestHeader
              args:
                name: X-Span-Id
                value: "${spanId}"
```

### 7.3 Custom Metrics

```java
@Component
public class CustomMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Counter requestCounter;
    private final Timer responseTimer;
    private final Gauge activeConnections;
    
    public CustomMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.requestCounter = Counter.builder("tigateway.requests.total")
                .description("Total number of requests")
                .tag("type", "gateway")
                .register(meterRegistry);
        
        this.responseTimer = Timer.builder("tigateway.response.time")
                .description("Response time")
                .register(meterRegistry);
        
        this.activeConnections = Gauge.builder("tigateway.connections.active")
                .description("Active connections")
                .register(meterRegistry, this, CustomMetricsCollector::getActiveConnections);
    }
    
    public void incrementRequestCount(String route, String method) {
        requestCounter.increment(
                Tags.of(
                        "route", route,
                        "method", method
                )
        );
    }
    
    public void recordResponseTime(Duration duration, String route) {
        responseTimer.record(duration, Tags.of("route", route));
    }
    
    private double getActiveConnections() {
        // Implement logic to get active connections
        return 0.0;
    }
}
```

## Best Practices

### 1. Configuration Management

- Use environment-specific configuration files
- Externalize sensitive configuration
- Implement configuration validation
- Use configuration hot-reload

### 2. Error Handling

- Implement comprehensive error handling
- Use circuit breakers for fault tolerance
- Provide meaningful error messages
- Log errors for debugging

### 3. Performance

- Use connection pooling
- Implement caching where appropriate
- Monitor performance metrics
- Optimize filter chains

### 4. Security

- Implement authentication and authorization
- Use HTTPS in production
- Validate input data
- Implement rate limiting

### 5. Monitoring

- Set up comprehensive monitoring
- Use health checks
- Implement alerting
- Monitor business metrics

## Troubleshooting

### Common Issues

1. **Routes not working**
   - Check route configuration
   - Verify service availability
   - Check logs for errors

2. **Authentication failures**
   - Verify JWT token format
   - Check token expiration
   - Validate authentication configuration

3. **Performance issues**
   - Monitor response times
   - Check resource usage
   - Optimize filter chains

4. **Configuration not loading**
   - Check configuration file format
   - Verify environment variables
   - Check configuration validation

### Debug Commands

```bash
# Check gateway status
curl http://localhost:8080/actuator/health

# View route configuration
curl http://localhost:8080/actuator/gateway/routes

# Check metrics
curl http://localhost:8080/actuator/metrics

# View logs
tail -f logs/tigateway.log
```

## Next Steps

After completing these advanced configurations:

1. **[Production Deployment](../deployment/kubernetes.md)** - Deploy TiGateway in production
2. **[Monitoring Setup](../monitoring-and-metrics.md)** - Set up comprehensive monitoring
3. **[Security Best Practices](../security-best-practices.md)** - Secure your deployment
4. **[Troubleshooting Guide](../troubleshooting.md)** - Common issues and solutions

---

**Ready for production?** Check out our [Production Deployment Guide](../deployment/kubernetes.md) to deploy TiGateway in a production environment.
