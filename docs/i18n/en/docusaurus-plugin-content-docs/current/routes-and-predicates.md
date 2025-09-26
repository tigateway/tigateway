# Routes and Predicates

This guide covers TiGateway's routing capabilities and predicate system, which are fundamental to how requests are matched and routed to backend services.

## Overview

TiGateway uses a powerful routing system based on Spring Cloud Gateway that allows you to:

- **Route requests** to different backend services based on various criteria
- **Apply filters** to modify requests and responses
- **Load balance** across multiple service instances
- **Handle failures** with circuit breakers and fallbacks

## Route Configuration

### Basic Route Structure

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: route-id
          uri: http://backend-service:8080
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
```

### Route Components

Each route consists of:

- **ID**: Unique identifier for the route
- **URI**: Target service endpoint
- **Predicates**: Conditions that must be met for the route to match
- **Filters**: Operations to perform on the request/response
- **Order**: Priority when multiple routes could match

## Predicates

Predicates are conditions that determine whether a route should handle a request. TiGateway supports many built-in predicates and allows custom predicates.

### Path Predicate

Matches requests based on URL path patterns.

```yaml
predicates:
  - Path=/api/users/**
  - Path=/api/orders/{segment}
  - Path=/api/**,/admin/**
```

**Examples:**
```yaml
# Match all requests to /api/users/*
- Path=/api/users/**

# Match requests with path variables
- Path=/api/users/{id}

# Match multiple paths
- Path=/api/**,/admin/**
```

### Method Predicate

Matches requests based on HTTP method.

```yaml
predicates:
  - Method=GET,POST
  - Method=PUT,DELETE
```

**Examples:**
```yaml
# Match GET and POST requests
- Method=GET,POST

# Match only PUT requests
- Method=PUT

# Match all methods except DELETE
- Method=GET,POST,PUT
```

### Header Predicate

Matches requests based on HTTP headers.

```yaml
predicates:
  - Header=X-API-Version,v1
  - Header=Authorization,Bearer .+
  - Header=X-Request-ID
```

**Examples:**
```yaml
# Match requests with specific header value
- Header=X-API-Version,v1

# Match requests with header matching regex
- Header=Authorization,Bearer .+

# Match requests that have the header (any value)
- Header=X-Request-ID

# Match requests with multiple headers
- Header=X-API-Version,v1
- Header=X-Client-Type,mobile
```

### Query Predicate

Matches requests based on query parameters.

```yaml
predicates:
  - Query=version,v1
  - Query=debug,true
  - Query=page
```

**Examples:**
```yaml
# Match requests with specific query parameter value
- Query=version,v1

# Match requests with query parameter (any value)
- Query=debug

# Match requests with query parameter matching regex
- Query=page,\d+
```

### Host Predicate

Matches requests based on the Host header.

```yaml
predicates:
  - Host=api.example.com
  - Host=*.example.com
  - Host=api.example.com,admin.example.com
```

**Examples:**
```yaml
# Match requests to specific host
- Host=api.example.com

# Match requests to subdomains
- Host=*.example.com

# Match requests to multiple hosts
- Host=api.example.com,admin.example.com
```

### RemoteAddr Predicate

Matches requests based on client IP address.

```yaml
predicates:
  - RemoteAddr=192.168.1.1/24
  - RemoteAddr=10.0.0.0/8,172.16.0.0/12
```

**Examples:**
```yaml
# Match requests from specific IP range
- RemoteAddr=192.168.1.1/24

# Match requests from multiple IP ranges
- RemoteAddr=10.0.0.0/8,172.16.0.0/12

# Match requests from specific IP
- RemoteAddr=192.168.1.100
```

### Cookie Predicate

Matches requests based on cookies.

```yaml
predicates:
  - Cookie=sessionId,abc123
  - Cookie=theme,dark
```

**Examples:**
```yaml
# Match requests with specific cookie value
- Cookie=sessionId,abc123

# Match requests with cookie (any value)
- Cookie=theme

# Match requests with cookie matching regex
- Cookie=sessionId,\w+
```

### Weight Predicate

Used for A/B testing and canary deployments.

```yaml
predicates:
  - Weight=group1,80
  - Weight=group1,20
```

**Examples:**
```yaml
# Route 80% of traffic to version A
- Weight=user-service,80

# Route 20% of traffic to version B
- Weight=user-service,20
```

### After/Before/Between Predicate

Matches requests based on time.

```yaml
predicates:
  - After=2024-01-01T00:00:00+00:00
  - Before=2024-12-31T23:59:59+00:00
  - Between=2024-01-01T00:00:00+00:00,2024-12-31T23:59:59+00:00
```

**Examples:**
```yaml
# Match requests after specific time
- After=2024-01-01T00:00:00+00:00

# Match requests before specific time
- Before=2024-12-31T23:59:59+00:00

# Match requests between specific times
- Between=2024-01-01T00:00:00+00:00,2024-12-31T23:59:59+00:00
```

## Advanced Routing Scenarios

### A/B Testing

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Version A (80% traffic)
        - id: user-service-v1
          uri: lb://user-service-v1
          predicates:
            - Path=/api/users/**
            - Weight=user-service,80
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Version,v1
        
        # Version B (20% traffic)
        - id: user-service-v2
          uri: lb://user-service-v2
          predicates:
            - Path=/api/users/**
            - Weight=user-service,20
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Version,v2
```

### Canary Deployment

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
        
        # Stable route for regular users
        - id: user-service-stable
          uri: lb://user-service-stable
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
```

### Multi-Tenant Routing

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
        
        # Tenant B routing
        - id: tenant-b-route
          uri: lb://tenant-b-service
          predicates:
            - Path=/api/tenant-b/**
            - Header=X-Tenant-ID,tenant-b
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Tenant,tenant-b
        
        # Default tenant routing
        - id: default-tenant-route
          uri: lb://default-tenant-service
          predicates:
            - Path=/api/**
          filters:
            - StripPrefix=1
```

### Geographic Routing

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
        
        # EU West routing
        - id: eu-west-route
          uri: lb://eu-west-service
          predicates:
            - Path=/api/**
            - RemoteAddr=172.16.0.0/12
          filters:
            - StripPrefix=1
            - AddRequestHeader=X-Region,eu-west
        
        # Default routing
        - id: default-route
          uri: lb://default-service
          predicates:
            - Path=/api/**
          filters:
            - StripPrefix=1
```

## Custom Predicates

### Creating Custom Predicates

```java
@Component
public class CustomPredicateFactory extends AbstractRoutePredicateFactory<CustomPredicateFactory.Config> {
    
    public CustomPredicateFactory() {
        super(Config.class);
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            // Custom logic here
            String customHeader = exchange.getRequest().getHeaders().getFirst("X-Custom-Header");
            return config.getValue().equals(customHeader);
        };
    }
    
    @Data
    public static class Config {
        private String value;
    }
}
```

### Using Custom Predicates

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: custom-predicate-route
          uri: lb://custom-service
          predicates:
            - name: Custom
              args:
                value: "custom-value"
          filters:
            - StripPrefix=2
```

## Route Discovery

### Service Discovery Integration

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
          predicates:
            - name: Path
              args:
                pattern: "'/'+serviceId+'/**'"
          filters:
            - name: RewritePath
              args:
                regexp: "'/'+serviceId+'/(?<remaining>.*)'"
                replacement: "'/${remaining}'"
```

### Dynamic Route Configuration

```java
@Configuration
public class DynamicRouteConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("dynamic-route", r -> r
                        .path("/api/dynamic/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Dynamic", "true"))
                        .uri("lb://dynamic-service"))
                .build();
    }
}
```

## Route Ordering

### Default Ordering

Routes are ordered by their configuration order. The first matching route is used.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: first-route
          uri: lb://service1
          predicates:
            - Path=/api/**
          order: 1
        
        - id: second-route
          uri: lb://service2
          predicates:
            - Path=/api/users/**
          order: 2
```

### Explicit Ordering

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: high-priority-route
          uri: lb://high-priority-service
          predicates:
            - Path=/api/admin/**
          order: -100
        
        - id: low-priority-route
          uri: lb://low-priority-service
          predicates:
            - Path=/api/**
          order: 100
```

## Route Testing

### Testing Routes

```bash
# Test route matching
curl -v http://localhost:8080/api/users/123

# Test with headers
curl -H "X-API-Version: v1" http://localhost:8080/api/users/123

# Test with query parameters
curl "http://localhost:8080/api/users/123?version=v1"

# Test with cookies
curl -H "Cookie: sessionId=abc123" http://localhost:8080/api/users/123
```

### Route Debugging

```bash
# Check route configuration
curl http://localhost:8080/actuator/gateway/routes

# Check specific route
curl http://localhost:8080/actuator/gateway/routes/{route-id}

# Refresh routes
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

## Best Practices

### Route Design

1. **Use specific predicates first**: More specific routes should come before general ones
2. **Minimize predicate complexity**: Keep predicates simple and efficient
3. **Use meaningful route IDs**: Choose descriptive IDs for easier debugging
4. **Group related routes**: Organize routes logically
5. **Test thoroughly**: Test all route combinations

### Performance Optimization

1. **Order routes efficiently**: Place frequently used routes first
2. **Use efficient predicates**: Avoid expensive operations in predicates
3. **Cache route decisions**: Use caching where appropriate
4. **Monitor route performance**: Track route matching performance
5. **Optimize predicate order**: Order predicates by selectivity

### Security Considerations

1. **Validate input**: Ensure predicates validate input properly
2. **Use secure predicates**: Avoid predicates that could be exploited
3. **Implement rate limiting**: Use rate limiting predicates
4. **Monitor route access**: Log and monitor route access
5. **Regular security reviews**: Review route configurations regularly

## Troubleshooting

### Common Issues

#### Routes Not Matching

```bash
# Check route configuration
curl http://localhost:8080/actuator/gateway/routes

# Test predicate conditions
curl -v http://localhost:8080/api/users/123

# Check route order
curl http://localhost:8080/actuator/gateway/routes | jq '.[] | {id, order}'
```

#### Route Conflicts

```bash
# Check for overlapping routes
curl http://localhost:8080/actuator/gateway/routes | jq '.[] | {id, predicates}'

# Test route precedence
curl -v http://localhost:8080/api/users/123
```

#### Performance Issues

```bash
# Check route matching performance
curl http://localhost:8080/actuator/metrics/gateway.requests

# Monitor route response times
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### Debug Commands

```bash
# List all routes
curl http://localhost:8080/actuator/gateway/routes

# Get route details
curl http://localhost:8080/actuator/gateway/routes/{route-id}

# Refresh routes
curl -X POST http://localhost:8080/actuator/gateway/refresh

# Check route filters
curl http://localhost:8080/actuator/gateway/routefilters
```

## Next Steps

After configuring routes and predicates:

1. **[Filters Guide](./filters.md)** - Learn about request/response filters
2. **[Service Discovery](./service-discovery.md)** - Configure service discovery
3. **[Load Balancing](./load-balancing.md)** - Set up load balancing
4. **[Monitoring Setup](../monitoring-and-metrics.md)** - Monitor route performance

---

**Ready to add filters?** Check out our [Filters Guide](./filters.md) to learn about request and response processing.
