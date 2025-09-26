# Health Checks

This guide covers TiGateway's health check system, including built-in health checks, custom health checks, monitoring, and best practices for ensuring service reliability.

## Overview

TiGateway provides comprehensive health checking capabilities:

- **Built-in Health Checks**: Automatic health monitoring for services
- **Custom Health Checks**: Custom health check implementations
- **Service Discovery Integration**: Health checks for discovered services
- **Load Balancer Integration**: Health-based load balancing
- **Monitoring and Alerting**: Health check monitoring and notifications
- **Circuit Breaker Integration**: Health-based circuit breaker decisions

## Built-in Health Checks

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

### Health Check Endpoints

TiGateway automatically checks the following endpoints:

- **Spring Boot Actuator**: `/actuator/health`
- **Custom Health Endpoints**: Configurable health check paths
- **Service-specific Endpoints**: Service-specific health check URLs

### Health Check Response Format

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "6.2.0"
      }
    }
  }
}
```

## Custom Health Checks

### Implementing Custom Health Checks

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    private final UserService userService;
    
    public CustomHealthIndicator(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public Health health() {
        try {
            // Perform health check
            boolean isHealthy = userService.isHealthy();
            
            if (isHealthy) {
                return Health.up()
                        .withDetail("service", "user-service")
                        .withDetail("version", "1.0.0")
                        .withDetail("timestamp", System.currentTimeMillis())
                        .build();
            } else {
                return Health.down()
                        .withDetail("service", "user-service")
                        .withDetail("error", "Service is not responding")
                        .withDetail("timestamp", System.currentTimeMillis())
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("service", "user-service")
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        }
    }
}
```

### Advanced Health Check

```java
@Component
public class AdvancedHealthIndicator implements HealthIndicator {
    
    private final DatabaseService databaseService;
    private final CacheService cacheService;
    private final ExternalServiceClient externalServiceClient;
    
    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        
        // Check database
        try {
            boolean dbHealthy = databaseService.isHealthy();
            builder.withDetail("database", dbHealthy ? "UP" : "DOWN");
        } catch (Exception e) {
            builder.withDetail("database", "DOWN")
                   .withDetail("database.error", e.getMessage());
        }
        
        // Check cache
        try {
            boolean cacheHealthy = cacheService.isHealthy();
            builder.withDetail("cache", cacheHealthy ? "UP" : "DOWN");
        } catch (Exception e) {
            builder.withDetail("cache", "DOWN")
                   .withDetail("cache.error", e.getMessage());
        }
        
        // Check external service
        try {
            boolean externalHealthy = externalServiceClient.isHealthy();
            builder.withDetail("external-service", externalHealthy ? "UP" : "DOWN");
        } catch (Exception e) {
            builder.withDetail("external-service", "DOWN")
                   .withDetail("external-service.error", e.getMessage());
        }
        
        return builder.build();
    }
}
```

### Health Check with Metrics

```java
@Component
public class MetricsHealthIndicator implements HealthIndicator {
    
    private final MeterRegistry meterRegistry;
    private final Counter healthCheckCounter;
    private final Timer healthCheckTimer;
    
    public MetricsHealthIndicator(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.healthCheckCounter = Counter.builder("healthcheck.checks.total")
                .description("Total health checks")
                .register(meterRegistry);
        
        this.healthCheckTimer = Timer.builder("healthcheck.checks.duration")
                .description("Health check duration")
                .register(meterRegistry);
    }
    
    @Override
    public Health health() {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Perform health check
            Health health = performHealthCheck();
            
            // Record metrics
            sample.stop(healthCheckTimer.tags("status", health.getStatus().getCode()));
            healthCheckCounter.increment(Tags.of("status", health.getStatus().getCode()));
            
            return health;
        } catch (Exception e) {
            sample.stop(healthCheckTimer.tags("status", "DOWN"));
            healthCheckCounter.increment(Tags.of("status", "DOWN"));
            
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
    
    private Health performHealthCheck() {
        // Implementation
        return Health.up().build();
    }
}
```

## Service Discovery Health Checks

### Eureka Health Checks

```yaml
spring:
  cloud:
    discovery:
      client:
        health-indicator:
          enabled: true
        eureka:
          instance:
            health-check-url: http://${spring.cloud.client.ip-address}:${server.port}/actuator/health
            health-check-url-path: /actuator/health
            prefer-ip-address: true
```

### Consul Health Checks

```yaml
spring:
  cloud:
    consul:
      discovery:
        health-check-path: /actuator/health
        health-check-interval: 10s
        health-check-timeout: 5s
        health-check-critical-timeout: 30s
```

### Kubernetes Health Checks

```yaml
spring:
  cloud:
    kubernetes:
      discovery:
        health-check-path: /actuator/health
        health-check-interval: 10s
        health-check-timeout: 5s
```

## Load Balancer Health Checks

### Health Check Configuration

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
        initial-delay: 30s
```

### Custom Health Check Strategy

```java
@Component
public class CustomHealthCheckStrategy implements HealthCheckStrategy {
    
    @Override
    public boolean isHealthy(ServiceInstance instance) {
        try {
            String healthUrl = "http://" + instance.getHost() + ":" + 
                              instance.getPort() + "/actuator/health";
            
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
            
            ResponseEntity<Map> response = restTemplate.getForEntity(healthUrl, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                return "UP".equals(body.get("status"));
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### Health Check with Circuit Breaker

```java
@Component
public class CircuitBreakerHealthCheck implements HealthCheckStrategy {
    
    private final CircuitBreaker circuitBreaker;
    
    public CircuitBreakerHealthCheck() {
        this.circuitBreaker = CircuitBreaker.ofDefaults("health-check");
    }
    
    @Override
    public boolean isHealthy(ServiceInstance instance) {
        try {
            return circuitBreaker.executeSupplier(() -> {
                String healthUrl = "http://" + instance.getHost() + ":" + 
                                  instance.getPort() + "/actuator/health";
                
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Map> response = restTemplate.getForEntity(healthUrl, Map.class);
                
                return response.getStatusCode().is2xxSuccessful() &&
                       "UP".equals(response.getBody().get("status"));
            });
        } catch (Exception e) {
            return false;
        }
    }
}
```

## Health Check Monitoring

### Health Check Metrics

```java
@Component
public class HealthCheckMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter healthCheckCounter;
    private final Timer healthCheckTimer;
    private final Gauge healthyInstances;
    
    public HealthCheckMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.healthCheckCounter = Counter.builder("healthcheck.checks.total")
                .description("Total health checks")
                .register(meterRegistry);
        
        this.healthCheckTimer = Timer.builder("healthcheck.checks.duration")
                .description("Health check duration")
                .register(meterRegistry);
        
        this.healthyInstances = Gauge.builder("healthcheck.instances.healthy")
                .description("Number of healthy instances")
                .register(meterRegistry, this, HealthCheckMetrics::getHealthyInstances);
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
    
    private double getHealthyInstances() {
        // Implement logic to get healthy instances count
        return 0.0;
    }
}
```

### Health Check Alerting

```java
@Component
public class HealthCheckAlerting {
    
    private final MeterRegistry meterRegistry;
    private final AlertManager alertManager;
    
    public HealthCheckAlerting(MeterRegistry meterRegistry, AlertManager alertManager) {
        this.meterRegistry = meterRegistry;
        this.alertManager = alertManager;
    }
    
    @EventListener
    public void handleHealthCheckEvent(HealthCheckEvent event) {
        if (!event.isHealthy()) {
            // Send alert
            Alert alert = Alert.builder()
                    .title("Service Health Check Failed")
                    .description("Service " + event.getServiceName() + " is unhealthy")
                    .severity(AlertSeverity.HIGH)
                    .timestamp(Instant.now())
                    .build();
            
            alertManager.sendAlert(alert);
        }
    }
}
```

### Health Check Dashboard

```java
@RestController
@RequestMapping("/admin/health")
public class HealthCheckController {
    
    private final HealthCheckService healthCheckService;
    
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getHealthOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // Get overall health status
        overview.put("overall", healthCheckService.getOverallHealth());
        
        // Get service health status
        overview.put("services", healthCheckService.getServiceHealth());
        
        // Get health check metrics
        overview.put("metrics", healthCheckService.getHealthMetrics());
        
        return ResponseEntity.ok(overview);
    }
    
    @GetMapping("/services/{serviceName}")
    public ResponseEntity<Map<String, Object>> getServiceHealth(@PathVariable String serviceName) {
        Map<String, Object> serviceHealth = healthCheckService.getServiceHealth(serviceName);
        return ResponseEntity.ok(serviceHealth);
    }
    
    @PostMapping("/services/{serviceName}/check")
    public ResponseEntity<Map<String, Object>> performHealthCheck(@PathVariable String serviceName) {
        Map<String, Object> result = healthCheckService.performHealthCheck(serviceName);
        return ResponseEntity.ok(result);
    }
}
```

## Health Check Configuration

### Global Health Check Configuration

```yaml
health:
  checks:
    enabled: true
    default:
      interval: 10s
      timeout: 5s
      retries: 3
      initial-delay: 30s
    services:
      user-service:
        interval: 5s
        timeout: 3s
        retries: 2
        path: /actuator/health
      order-service:
        interval: 15s
        timeout: 10s
        retries: 5
        path: /health
```

### Service-specific Configuration

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
            - name: HealthCheck
              args:
                enabled: true
                interval: 5s
                timeout: 3s
                retries: 2
```

### Health Check Filter

```java
@Component
public class HealthCheckFilter implements GlobalFilter, Ordered {
    
    private final HealthCheckService healthCheckService;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String serviceName = extractServiceName(request);
        
        if (serviceName != null) {
            boolean isHealthy = healthCheckService.isServiceHealthy(serviceName);
            
            if (!isHealthy) {
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

## Best Practices

### Health Check Design

1. **Lightweight Checks**: Keep health checks lightweight and fast
2. **Meaningful Checks**: Check actual service functionality, not just availability
3. **Timeout Configuration**: Set appropriate timeouts to avoid blocking
4. **Retry Logic**: Implement retry logic for transient failures
5. **Graceful Degradation**: Handle health check failures gracefully

### Performance Considerations

1. **Check Frequency**: Balance check frequency with performance impact
2. **Parallel Checks**: Perform health checks in parallel when possible
3. **Caching**: Cache health check results when appropriate
4. **Resource Usage**: Monitor health check resource usage
5. **Scaling**: Scale health checks with service instances

### Monitoring and Alerting

1. **Health Metrics**: Collect comprehensive health check metrics
2. **Alerting**: Set up appropriate alerts for health check failures
3. **Dashboard**: Create health check dashboards for monitoring
4. **Logging**: Log health check events for debugging
5. **Trends**: Monitor health check trends over time

## Troubleshooting

### Common Issues

#### Health Checks Failing

```bash
# Check health check configuration
curl http://localhost:8080/actuator/configprops | grep -i health

# Test health check endpoint
curl http://localhost:8080/actuator/health

# Check service health
curl http://user-service:8080/actuator/health

# Check health check logs
tail -f logs/tigateway.log | grep -i health
```

#### Health Check Timeouts

```bash
# Check health check timeout configuration
curl http://localhost:8080/actuator/configprops | grep -i timeout

# Test health check response time
time curl http://user-service:8080/actuator/health

# Check network connectivity
ping user-service
telnet user-service 8080
```

#### Service Discovery Issues

```bash
# Check service discovery
curl http://localhost:8080/actuator/health

# Check service registry
curl http://localhost:8761/eureka/apps

# Check Consul services
curl http://localhost:8500/v1/catalog/services
```

### Debug Commands

```bash
# Check health check status
curl http://localhost:8080/actuator/health

# Check service health
curl http://localhost:8080/admin/health/overview

# Check health check metrics
curl http://localhost:8080/actuator/metrics/healthcheck.checks.total

# Perform manual health check
curl -X POST http://localhost:8080/admin/health/services/user-service/check
```

## Next Steps

After configuring health checks:

1. **[Monitoring Setup](../monitoring-and-metrics.md)** - Set up comprehensive monitoring
2. **[Load Balancing](./load-balancing.md)** - Configure health-based load balancing
3. **[Troubleshooting Guide](../troubleshooting.md)** - Common health check issues
4. **[Performance Tuning](../performance-tuning.md)** - Optimize health check performance

---

**Ready to set up monitoring?** Check out our [Monitoring Setup](../monitoring-and-metrics.md) guide for comprehensive monitoring solutions.
