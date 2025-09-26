# Quick Start Examples

This guide provides practical examples to help you get started with TiGateway quickly. We'll cover basic configuration, common use cases, and step-by-step tutorials.

## Prerequisites

Before starting, ensure you have:

- **Java 17+** installed
- **Docker** and **Docker Compose** (for containerized deployment)
- **kubectl** (for Kubernetes deployment)
- Basic understanding of **Spring Cloud Gateway** concepts

## Example 1: Basic Gateway Setup

### 1.1 Create a Simple Gateway

Let's start with a basic TiGateway setup that routes requests to backend services.

```yaml
# application.yml
server:
  port: 8080

spring:
  application:
    name: tigateway
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: http://localhost:8081
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service,user-service
        - id: order-service-route
          uri: http://localhost:8082
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service,order-service
      default-filters:
        - AddRequestHeader=X-Gateway,TiGateway
        - AddResponseHeader=X-Response-Time,${timestamp}
```

### 1.2 Start the Gateway

```bash
# Start TiGateway
java -jar tigateway-1.0.0.jar

# Or with Spring Boot Maven plugin
mvn spring-boot:run
```

### 1.3 Test the Gateway

```bash
# Test user service route
curl http://localhost:8080/api/users/123
# This will be routed to http://localhost:8081/users/123

# Test order service route
curl http://localhost:8080/api/orders/456
# This will be routed to http://localhost:8082/orders/456
```

## Example 2: Load Balancing

### 2.1 Configure Load Balancing

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: load-balanced-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - LoadBalancer=ROUND_ROBIN
```

### 2.2 Service Discovery Configuration

```yaml
spring:
  cloud:
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

### 2.3 Test Load Balancing

```bash
# Make multiple requests to see load balancing in action
for i in {1..10}; do
  curl http://localhost:8080/api/users/123
  echo "Request $i completed"
done
```

## Example 3: Circuit Breaker

### 3.1 Configure Circuit Breaker

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: circuit-breaker-route
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
```

### 3.2 Create Fallback Controller

```java
@RestController
public class FallbackController {
    
    @GetMapping("/fallback/user")
    public ResponseEntity<Map<String, Object>> userFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User service is temporarily unavailable");
        response.put("timestamp", Instant.now());
        response.put("fallback", true);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
```

### 3.3 Test Circuit Breaker

```bash
# Test normal operation
curl http://localhost:8080/api/users/123

# Simulate service failure and test fallback
# (Stop the user service and make requests)
curl http://localhost:8080/api/users/123
# Should return fallback response
```

## Example 4: Rate Limiting

### 4.1 Configure Rate Limiting

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rate-limited-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@userKeyResolver}"
```

### 4.2 Configure Redis

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
```

### 4.3 Create Key Resolver

```java
@Configuration
public class RateLimiterConfig {
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }
}
```

### 4.4 Test Rate Limiting

```bash
# Make requests quickly to test rate limiting
for i in {1..25}; do
  curl -w "Status: %{http_code}\n" http://localhost:8080/api/users/123
  sleep 0.1
done
```

## Example 5: Authentication and Authorization

### 5.1 Configure JWT Authentication

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
            - Header=Authorization, Bearer .+
          filters:
            - StripPrefix=2
            - name: JwtAuthenticationFilter
            - name: RoleAuthorizationFilter
              args:
                required-roles: ["USER", "ADMIN"]
```

### 5.2 Create Authentication Filter

```java
@Component
public class JwtAuthenticationFilter implements GatewayFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = extractToken(exchange.getRequest());
        
        if (token != null && validateToken(token)) {
            // Add user info to headers
            ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-User-Id", getUserIdFromToken(token))
                .header("X-User-Roles", getUserRolesFromToken(token))
                .build();
            
            return chain.filter(exchange.mutate().request(request).build());
        }
        
        return unauthorized(exchange);
    }
    
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    private boolean validateToken(String token) {
        // Implement JWT token validation
        return true; // Simplified for example
    }
    
    private String getUserIdFromToken(String token) {
        // Extract user ID from JWT token
        return "user123"; // Simplified for example
    }
    
    private String getUserRolesFromToken(String token) {
        // Extract user roles from JWT token
        return "USER,ADMIN"; // Simplified for example
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Unauthorized\",\"message\":\"Invalid or missing token\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
```

### 5.3 Test Authentication

```bash
# Test without token (should fail)
curl http://localhost:8080/api/users/123

# Test with valid token
curl -H "Authorization: Bearer your-jwt-token" http://localhost:8080/api/users/123
```

## Example 6: Request/Response Transformation

### 6.1 Configure Request Transformation

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: transform-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - name: ModifyRequestBody
              args:
                content-type: application/json
                rewrite-function: "#{@requestBodyTransformer}"
            - name: ModifyResponseBody
              args:
                content-type: application/json
                rewrite-function: "#{@responseBodyTransformer}"
```

### 6.2 Create Transformation Functions

```java
@Configuration
public class TransformationConfig {
    
    @Bean
    public RewriteFunction<String, String> requestBodyTransformer() {
        return (exchange, body) -> {
            // Transform request body
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(body);
                
                // Add gateway metadata
                ((ObjectNode) jsonNode).put("gateway", "TiGateway");
                ((ObjectNode) jsonNode).put("timestamp", Instant.now().toString());
                
                return Mono.just(mapper.writeValueAsString(jsonNode));
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
    }
    
    @Bean
    public RewriteFunction<String, String> responseBodyTransformer() {
        return (exchange, body) -> {
            // Transform response body
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(body);
                
                // Add response metadata
                ((ObjectNode) jsonNode).put("processedBy", "TiGateway");
                ((ObjectNode) jsonNode).put("responseTime", Instant.now().toString());
                
                return Mono.just(mapper.writeValueAsString(jsonNode));
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
    }
}
```

## Example 7: Monitoring and Metrics

### 7.1 Configure Metrics

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
      environment: development
```

### 7.2 Custom Metrics

```java
@Component
public class CustomMetrics {
    
    private final Counter requestCounter;
    private final Timer responseTimer;
    private final Gauge activeConnections;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.requestCounter = Counter.builder("tigateway.requests.total")
                .description("Total number of requests")
                .tag("type", "gateway")
                .register(meterRegistry);
        
        this.responseTimer = Timer.builder("tigateway.response.time")
                .description("Response time")
                .register(meterRegistry);
        
        this.activeConnections = Gauge.builder("tigateway.connections.active")
                .description("Active connections")
                .register(meterRegistry, this, CustomMetrics::getActiveConnections);
    }
    
    public void incrementRequestCount() {
        requestCounter.increment();
    }
    
    public void recordResponseTime(Duration duration) {
        responseTimer.record(duration);
    }
    
    private double getActiveConnections() {
        // Implement logic to get active connections
        return 0.0;
    }
}
```

### 7.3 Test Metrics

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Check metrics
curl http://localhost:8080/actuator/metrics

# Check Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

## Example 8: Docker Deployment

### 8.1 Create Dockerfile

```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app

COPY target/tigateway-1.0.0.jar app.jar

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 8.2 Create Docker Compose

```yaml
version: '3.8'

services:
  tigateway:
    build: .
    ports:
      - "8080:8080"
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - REDIS_HOST=redis
    depends_on:
      - redis
      - user-service
      - order-service

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  user-service:
    image: user-service:1.0.0
    ports:
      - "8081:8080"
    environment:
      - SERVER_PORT=8080

  order-service:
    image: order-service:1.0.0
    ports:
      - "8082:8080"
    environment:
      - SERVER_PORT=8080
```

### 8.3 Deploy with Docker Compose

```bash
# Build and start services
docker-compose up --build

# Check service status
docker-compose ps

# View logs
docker-compose logs tigateway

# Stop services
docker-compose down
```

## Example 9: Kubernetes Deployment

### 9.1 Create Kubernetes Manifests

```yaml
# tigateway-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tigateway
  namespace: tigateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: tigateway
  template:
    metadata:
      labels:
        app: tigateway
    spec:
      containers:
      - name: tigateway
        image: tigateway:1.0.0
        ports:
        - containerPort: 8080
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        resources:
          requests:
            memory: "256Mi"
            cpu: "100m"
          limits:
            memory: "512Mi"
            cpu: "200m"
---
apiVersion: v1
kind: Service
metadata:
  name: tigateway
  namespace: tigateway
spec:
  selector:
    app: tigateway
  ports:
  - port: 8080
    targetPort: 8080
  - port: 8081
    targetPort: 8081
  type: LoadBalancer
```

### 9.2 Deploy to Kubernetes

```bash
# Create namespace
kubectl create namespace tigateway

# Deploy TiGateway
kubectl apply -f tigateway-deployment.yaml

# Check deployment status
kubectl get pods -n tigateway

# Check service
kubectl get svc -n tigateway

# Port forward for testing
kubectl port-forward svc/tigateway 8080:8080 -n tigateway
```

## Example 10: Advanced Configuration

### 10.1 Multi-Environment Configuration

```yaml
# application.yml
spring:
  profiles:
    active: ${ENVIRONMENT:dev}
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: ${USER_SERVICE_URL:http://localhost:8081}
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Environment,${ENVIRONMENT:dev}
---
# application-dev.yml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: http://user-service-dev:8080
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Environment,dev
            - AddRequestHeader=X-Debug,true
---
# application-prod.yml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: lb://user-service-prod
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Environment,prod
            - CircuitBreaker=user-service-cb,forward:/fallback
            - RequestRateLimiter=user-service-rl
```

### 10.2 Dynamic Configuration

```java
@Configuration
@EnableConfigurationProperties
public class DynamicConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("dynamic-route", r -> r
                        .path("/api/dynamic/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Dynamic", "true")
                                .circuitBreaker(c -> c
                                        .setName("dynamic-cb")
                                        .setFallbackUri("forward:/fallback/dynamic")))
                        .uri("lb://dynamic-service"))
                .build();
    }
}
```

## Best Practices

### 1. Configuration Management

- Use environment-specific configuration files
- Externalize sensitive configuration (secrets, passwords)
- Use configuration validation
- Implement configuration hot-reload

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

After completing these examples:

1. **[Configuration Guide](../configuration.md)** - Learn about advanced configuration options
2. **[Deployment Guide](../deployment/kubernetes.md)** - Deploy TiGateway in production
3. **[Monitoring Setup](../monitoring-and-metrics.md)** - Set up comprehensive monitoring
4. **[Security Best Practices](../security-best-practices.md)** - Secure your deployment

---

**Ready to explore more?** Check out our [Configuration Guide](../configuration.md) for advanced configuration options.
