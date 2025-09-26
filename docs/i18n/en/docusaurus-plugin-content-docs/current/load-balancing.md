# Load Balancing

This guide covers TiGateway's load balancing capabilities, including different load balancing strategies, health checks, service discovery integration, and performance optimization.

## Overview

TiGateway provides comprehensive load balancing features:

- **Multiple Load Balancing Strategies**: Round Robin, Least Connections, Weighted Round Robin
- **Health Checks**: Automatic health monitoring and service instance management
- **Service Discovery Integration**: Works with Eureka, Consul, Kubernetes, and custom discovery
- **Circuit Breaker Integration**: Fault tolerance and failure handling
- **Performance Optimization**: Connection pooling and caching
- **Dynamic Configuration**: Runtime load balancer configuration updates

## Load Balancing Strategies

### Round Robin (Default)

Distributes requests evenly across all available service instances.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: round-robin-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
```

**Configuration:**
```yaml
spring:
  cloud:
    loadbalancer:
      configurations:
        round-robin:
          enable: true
```

### Least Connections

Routes requests to the service instance with the fewest active connections.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: least-connections-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - name: LoadBalancer
              args:
                strategy: LEAST_CONNECTIONS
```

**Configuration:**
```yaml
spring:
  cloud:
    loadbalancer:
      configurations:
        least-connections:
          enable: true
          strategy: LEAST_CONNECTIONS
```

### Weighted Round Robin

Distributes requests based on predefined weights for each service instance.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: weighted-route-v1
          uri: lb://user-service-v1
          predicates:
            - Path=/api/users/**
            - Weight=user-service,80
          filters:
            - StripPrefix=2
        - id: weighted-route-v2
          uri: lb://user-service-v2
          predicates:
            - Path=/api/users/**
            - Weight=user-service,20
          filters:
            - StripPrefix=2
```

**Configuration:**
```yaml
spring:
  cloud:
    loadbalancer:
      configurations:
        weighted-round-robin:
          enable: true
          strategy: WEIGHTED_ROUND_ROBIN
          weights:
            user-service-v1: 80
            user-service-v2: 20
```

### Random

Randomly selects a service instance for each request.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: random-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - name: LoadBalancer
              args:
                strategy: RANDOM
```

**Configuration:**
```yaml
spring:
  cloud:
    loadbalancer:
      configurations:
        random:
          enable: true
          strategy: RANDOM
```

## Health Checks

### Basic Health Check Configuration

```yaml
spring:
  cloud:
    loadbalancer:
      health-check:
        enabled: true
        path: /actuator/health
        interval: 10s
        timeout: 5s
        retries: 3
```

### Custom Health Check

```java
@Component
public class CustomHealthChecker implements HealthChecker {
    
    @Override
    public boolean isHealthy(ServiceInstance instance) {
        try {
            String healthUrl = "http://" + instance.getHost() + ":" + 
                              instance.getPort() + "/actuator/health";
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
```

### Health Check Filter

```java
@Component
public class HealthCheckFilter implements GlobalFilter, Ordered {
    
    private final LoadBalancerClient loadBalancerClient;
    private final HealthChecker healthChecker;
    
    public HealthCheckFilter(LoadBalancerClient loadBalancerClient, HealthChecker healthChecker) {
        this.loadBalancerClient = loadBalancerClient;
        this.healthChecker = healthChecker;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String serviceName = extractServiceName(request);
        
        if (serviceName != null) {
            ServiceInstance instance = loadBalancerClient.choose(serviceName);
            
            if (instance != null && !healthChecker.isHealthy(instance)) {
                return serviceUnavailable(exchange);
            }
        }
        
        return chain.filter(exchange);
    }
    
    private String extractServiceName(ServerHttpRequest request) {
        // Extract service name from request
        String path = request.getURI().getPath();
        if (path.startsWith("/api/")) {
            String[] parts = path.split("/");
            if (parts.length > 2) {
                return parts[2] + "-service";
            }
        }
        return null;
    }
    
    private Mono<Void> serviceUnavailable(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Service Unavailable\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -100;
    }
}
```

## Service Discovery Integration

### Eureka Integration

```yaml
spring:
  cloud:
    discovery:
      client:
        enabled: true
        service-id: tigateway
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

### Consul Integration

```yaml
spring:
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
        service-name: tigateway
        health-check-path: /actuator/health
        health-check-interval: 10s
```

### Kubernetes Integration

```yaml
spring:
  cloud:
    kubernetes:
      discovery:
        enabled: true
        service-name: tigateway
        namespace: default
        health-check-path: /actuator/health
```

## Circuit Breaker Integration

### Circuit Breaker Configuration

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
                fallbackUri: forward:/fallback
                statusCodes: BAD_GATEWAY,GATEWAY_TIMEOUT
```

### Fallback Configuration

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

### Custom Circuit Breaker

```java
@Component
public class CustomCircuitBreaker implements GlobalFilter, Ordered {
    
    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String serviceName = extractServiceName(request);
        
        if (serviceName != null) {
            CircuitBreaker circuitBreaker = getCircuitBreaker(serviceName);
            
            return circuitBreaker.executeSupplier(() -> chain.filter(exchange))
                    .onErrorResume(CircuitBreakerOpenException.class, e -> 
                            fallback(exchange, "Circuit breaker is open"));
        }
        
        return chain.filter(exchange);
    }
    
    private CircuitBreaker getCircuitBreaker(String serviceName) {
        return circuitBreakers.computeIfAbsent(serviceName, name -> 
                CircuitBreaker.ofDefaults(name));
    }
    
    private Mono<Void> fallback(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().add("Content-Type", "application/json");
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Service Unavailable");
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        
        String body = new ObjectMapper().writeValueAsString(error);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -99;
    }
}
```

## Performance Optimization

### Connection Pool Configuration

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
          evict-in-background: true
          eviction-interval: 30s
```

### Caching Configuration

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000
      cache-null-values: false
      enable-statistics: true
      key-prefix: "tigateway:"
      use-key-prefix: true
```

### Load Balancer Caching

```java
@Component
public class LoadBalancerCache {
    
    private final Cache<String, List<ServiceInstance>> instanceCache;
    
    public LoadBalancerCache() {
        this.instanceCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
    }
    
    public List<ServiceInstance> getInstances(String serviceName) {
        return instanceCache.get(serviceName, this::loadInstances);
    }
    
    private List<ServiceInstance> loadInstances(String serviceName) {
        // Load instances from service discovery
        return discoveryClient.getInstances(serviceName);
    }
    
    public void evictInstances(String serviceName) {
        instanceCache.invalidate(serviceName);
    }
}
```

## Dynamic Configuration

### Runtime Configuration Updates

```java
@RestController
@RequestMapping("/admin/loadbalancer")
public class LoadBalancerAdminController {
    
    private final LoadBalancerClient loadBalancerClient;
    
    @PostMapping("/{serviceName}/strategy")
    public ResponseEntity<String> updateStrategy(
            @PathVariable String serviceName,
            @RequestParam String strategy) {
        
        // Update load balancing strategy
        loadBalancerClient.updateStrategy(serviceName, strategy);
        
        return ResponseEntity.ok("Strategy updated successfully");
    }
    
    @PostMapping("/{serviceName}/weights")
    public ResponseEntity<String> updateWeights(
            @PathVariable String serviceName,
            @RequestBody Map<String, Integer> weights) {
        
        // Update service instance weights
        loadBalancerClient.updateWeights(serviceName, weights);
        
        return ResponseEntity.ok("Weights updated successfully");
    }
    
    @PostMapping("/{serviceName}/health-check")
    public ResponseEntity<String> updateHealthCheck(
            @PathVariable String serviceName,
            @RequestBody HealthCheckConfig config) {
        
        // Update health check configuration
        loadBalancerClient.updateHealthCheck(serviceName, config);
        
        return ResponseEntity.ok("Health check updated successfully");
    }
}
```

### Configuration Management

```java
@Component
public class LoadBalancerConfigManager {
    
    private final Map<String, LoadBalancerConfig> configs = new ConcurrentHashMap<>();
    
    public void updateConfig(String serviceName, LoadBalancerConfig config) {
        configs.put(serviceName, config);
        
        // Notify load balancer of configuration change
        applicationEventPublisher.publishEvent(
                new LoadBalancerConfigChangedEvent(serviceName, config));
    }
    
    public LoadBalancerConfig getConfig(String serviceName) {
        return configs.getOrDefault(serviceName, LoadBalancerConfig.defaultConfig());
    }
    
    @EventListener
    public void handleConfigChange(LoadBalancerConfigChangedEvent event) {
        // Handle configuration change
        String serviceName = event.getServiceName();
        LoadBalancerConfig config = event.getConfig();
        
        // Update load balancer instance
        updateLoadBalancer(serviceName, config);
    }
}
```

## Monitoring and Metrics

### Load Balancer Metrics

```java
@Component
public class LoadBalancerMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter requestCounter;
    private final Timer requestTimer;
    private final Gauge activeConnections;
    
    public LoadBalancerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.requestCounter = Counter.builder("loadbalancer.requests.total")
                .description("Total load balancer requests")
                .register(meterRegistry);
        
        this.requestTimer = Timer.builder("loadbalancer.requests.duration")
                .description("Load balancer request duration")
                .register(meterRegistry);
        
        this.activeConnections = Gauge.builder("loadbalancer.connections.active")
                .description("Active load balancer connections")
                .register(meterRegistry, this, LoadBalancerMetrics::getActiveConnections);
    }
    
    public void recordRequest(String serviceName, String instance, Duration duration) {
        requestCounter.increment(Tags.of(
                "service", serviceName,
                "instance", instance
        ));
        
        requestTimer.record(duration, Tags.of(
                "service", serviceName,
                "instance", instance
        ));
    }
    
    private double getActiveConnections() {
        // Implement logic to get active connections
        return 0.0;
    }
}
```

### Health Check Metrics

```java
@Component
public class HealthCheckMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter healthCheckCounter;
    private final Timer healthCheckTimer;
    
    public HealthCheckMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.healthCheckCounter = Counter.builder("healthcheck.checks.total")
                .description("Total health checks")
                .register(meterRegistry);
        
        this.healthCheckTimer = Timer.builder("healthcheck.checks.duration")
                .description("Health check duration")
                .register(meterRegistry);
    }
    
    public void recordHealthCheck(String serviceName, String instance, boolean healthy, Duration duration) {
        healthCheckCounter.increment(Tags.of(
                "service", serviceName,
                "instance", instance,
                "healthy", String.valueOf(healthy)
        ));
        
        healthCheckTimer.record(duration, Tags.of(
                "service", serviceName,
                "instance", instance,
                "healthy", String.valueOf(healthy)
        ));
    }
}
```

## Best Practices

### Load Balancing Strategy Selection

1. **Round Robin**: Use for stateless services with similar performance
2. **Least Connections**: Use for services with varying response times
3. **Weighted Round Robin**: Use for services with different capacities
4. **Random**: Use for simple load distribution

### Health Check Configuration

1. **Appropriate intervals**: Set health check intervals based on service characteristics
2. **Timeout configuration**: Configure timeouts to avoid blocking
3. **Retry logic**: Implement retry logic for failed health checks
4. **Graceful degradation**: Handle health check failures gracefully

### Performance Optimization

1. **Connection pooling**: Use connection pooling for better performance
2. **Caching**: Cache service instances and health status
3. **Monitoring**: Monitor load balancer performance
4. **Scaling**: Scale load balancers based on demand

### Security Considerations

1. **Service authentication**: Authenticate service instances
2. **Network security**: Secure communication between services
3. **Access control**: Control access to load balancer configuration
4. **Audit logging**: Log load balancer operations

## Troubleshooting

### Common Issues

#### Service Instances Not Found

```bash
# Check service discovery
curl http://localhost:8080/actuator/health

# Check service registry
curl http://localhost:8761/eureka/apps

# Check Consul services
curl http://localhost:8500/v1/catalog/services
```

#### Load Balancing Not Working

```bash
# Check load balancer configuration
curl http://localhost:8080/actuator/configprops | grep -i loadbalancer

# Test load balancing
for i in {1..10}; do curl http://localhost:8080/api/users/123; done

# Check service instances
curl http://localhost:8080/actuator/gateway/routes
```

#### Health Check Failures

```bash
# Check health check configuration
curl http://localhost:8080/actuator/configprops | grep -i health

# Test health checks
curl http://localhost:8080/actuator/health

# Check service health
curl http://user-service:8080/actuator/health
```

### Debug Commands

```bash
# List all services
curl http://localhost:8080/actuator/gateway/routes

# Check load balancer status
curl http://localhost:8080/actuator/loadbalancer

# Check health check status
curl http://localhost:8080/actuator/health

# Refresh service discovery
curl -X POST http://localhost:8080/actuator/refresh
```

## Next Steps

After configuring load balancing:

1. **[Service Discovery](./service-discovery.md)** - Configure service discovery
2. **[Monitoring Setup](../monitoring-and-metrics.md)** - Monitor load balancer performance
3. **[Troubleshooting Guide](../troubleshooting.md)** - Common load balancing issues
4. **[Performance Tuning](../performance-tuning.md)** - Optimize load balancer performance

---

**Ready to configure service discovery?** Check out our [Service Discovery](./service-discovery.md) guide for comprehensive service discovery setup.
