# Custom Components

This guide covers how to create custom components for TiGateway, including custom filters, predicates, load balancers, and service discovery implementations.

## Overview

TiGateway provides extensive customization capabilities:

- **Custom Filters**: Create custom request/response filters
- **Custom Predicates**: Implement custom route matching logic
- **Custom Load Balancers**: Create custom load balancing strategies
- **Custom Service Discovery**: Implement custom service discovery mechanisms
- **Custom Health Checks**: Create custom health check implementations
- **Custom Metrics**: Implement custom metrics collection

## Custom Filters

### Basic Custom Filter

```java
@Component
public class CustomFilter implements GatewayFilter, Ordered {
    
    private static final Logger log = LoggerFactory.getLogger(CustomFilter.class);
    
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

### Custom Filter Factory

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

### Using Custom Filter Factory

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
            - name: CustomFilterFactory
              args:
                enabled: true
                headerName: X-Custom-Header
                headerValue: custom-value
```

### Advanced Custom Filter

```java
@Component
public class AdvancedCustomFilter implements GatewayFilter, Ordered {
    
    private final MeterRegistry meterRegistry;
    private final Timer requestTimer;
    private final Counter requestCounter;
    
    public AdvancedCustomFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.requestTimer = Timer.builder("custom.filter.duration")
                .description("Custom filter processing duration")
                .register(meterRegistry);
        
        this.requestCounter = Counter.builder("custom.filter.requests")
                .description("Custom filter request count")
                .register(meterRegistry);
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        return chain.filter(exchange)
                .doOnSuccess(result -> {
                    sample.stop(requestTimer.tags("path", path, "status", "success"));
                    requestCounter.increment(Tags.of("path", path, "status", "success"));
                })
                .doOnError(error -> {
                    sample.stop(requestTimer.tags("path", path, "status", "error"));
                    requestCounter.increment(Tags.of("path", path, "status", "error"));
                });
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

## Custom Predicates

### Basic Custom Predicate

```java
@Component
public class CustomPredicateFactory extends AbstractRoutePredicateFactory<CustomPredicateFactory.Config> {
    
    public CustomPredicateFactory() {
        super(Config.class);
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Custom logic here
            String customHeader = request.getHeaders().getFirst("X-Custom-Header");
            return config.getValue().equals(customHeader);
        };
    }
    
    @Data
    public static class Config {
        private String value;
    }
}
```

### Using Custom Predicate

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: custom-predicate-route
          uri: lb://user-service
          predicates:
            - name: Custom
              args:
                value: "custom-value"
```

### Advanced Custom Predicate

```java
@Component
public class AdvancedCustomPredicateFactory extends AbstractRoutePredicateFactory<AdvancedCustomPredicateFactory.Config> {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public AdvancedCustomPredicateFactory(RedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            String clientIp = getClientIp(request);
            
            // Check rate limit
            String key = "rate_limit:" + clientIp;
            String count = redisTemplate.opsForValue().get(key);
            
            if (count == null) {
                redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
                return true;
            }
            
            int attempts = Integer.parseInt(count);
            if (attempts >= config.getMaxRequests()) {
                return false;
            }
            
            redisTemplate.opsForValue().increment(key);
            return true;
        };
    }
    
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddress().getAddress().getHostAddress();
    }
    
    @Data
    public static class Config {
        private int maxRequests = 100;
    }
}
```

## Custom Load Balancers

### Custom Load Balancer Strategy

```java
@Component
public class CustomLoadBalancerStrategy implements LoadBalancerStrategy {
    
    @Override
    public ServiceInstance choose(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            return null;
        }
        
        // Custom load balancing logic
        // For example, choose instance with least CPU usage
        return instances.stream()
                .min(Comparator.comparing(this::getCpuUsage))
                .orElse(instances.get(0));
    }
    
    private double getCpuUsage(ServiceInstance instance) {
        // Implement logic to get CPU usage for instance
        // This could involve calling a metrics endpoint
        return 0.0;
    }
}
```

### Custom Load Balancer Configuration

```java
@Configuration
public class CustomLoadBalancerConfig {
    
    @Bean
    public LoadBalancerClient customLoadBalancerClient() {
        return new CustomLoadBalancerClient();
    }
    
    @Bean
    public LoadBalancerStrategy customStrategy() {
        return new CustomLoadBalancerStrategy();
    }
}
```

### Using Custom Load Balancer

```yaml
spring:
  cloud:
    loadbalancer:
      configurations:
        custom:
          enable: true
          strategy: CUSTOM
```

## Custom Service Discovery

### Custom Service Discovery Implementation

```java
@Component
public class CustomServiceDiscovery implements ServiceDiscovery {
    
    private final Map<String, List<ServiceInstance>> services = new ConcurrentHashMap<>();
    
    @Override
    public List<ServiceInstance> getInstances(String serviceName) {
        return services.getOrDefault(serviceName, Collections.emptyList());
    }
    
    @Override
    public void registerService(ServiceInstance instance) {
        services.computeIfAbsent(instance.getServiceId(), k -> new ArrayList<>())
                .add(instance);
    }
    
    @Override
    public void deregisterService(ServiceInstance instance) {
        List<ServiceInstance> instances = services.get(instance.getServiceId());
        if (instances != null) {
            instances.removeIf(i -> i.getId().equals(instance.getId()));
        }
    }
    
    @Override
    public List<String> getServiceNames() {
        return new ArrayList<>(services.keySet());
    }
}
```

### Custom Service Discovery Configuration

```java
@Configuration
public class CustomServiceDiscoveryConfig {
    
    @Bean
    public ServiceDiscovery customServiceDiscovery() {
        return new CustomServiceDiscovery();
    }
    
    @Bean
    public ServiceDiscoveryClient customServiceDiscoveryClient() {
        return new CustomServiceDiscoveryClient();
    }
}
```

## Custom Health Checks

### Custom Health Check Implementation

```java
@Component
public class CustomHealthChecker implements HealthChecker {
    
    private final RestTemplate restTemplate;
    
    public CustomHealthChecker() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }
    
    @Override
    public boolean isHealthy(ServiceInstance instance) {
        try {
            String healthUrl = "http://" + instance.getHost() + ":" + 
                              instance.getPort() + "/actuator/health";
            
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

### Custom Health Check with Circuit Breaker

```java
@Component
public class CircuitBreakerHealthChecker implements HealthChecker {
    
    private final CircuitBreaker circuitBreaker;
    private final RestTemplate restTemplate;
    
    public CircuitBreakerHealthChecker() {
        this.circuitBreaker = CircuitBreaker.ofDefaults("health-check");
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public boolean isHealthy(ServiceInstance instance) {
        try {
            return circuitBreaker.executeSupplier(() -> {
                String healthUrl = "http://" + instance.getHost() + ":" + 
                                  instance.getPort() + "/actuator/health";
                
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

## Custom Metrics

### Custom Metrics Implementation

```java
@Component
public class CustomMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter customCounter;
    private final Timer customTimer;
    private final Gauge customGauge;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.customCounter = Counter.builder("custom.requests.total")
                .description("Total custom requests")
                .register(meterRegistry);
        
        this.customTimer = Timer.builder("custom.requests.duration")
                .description("Custom request duration")
                .register(meterRegistry);
        
        this.customGauge = Gauge.builder("custom.instances.active")
                .description("Active custom instances")
                .register(meterRegistry, this, CustomMetrics::getActiveInstances);
    }
    
    public void recordRequest(String service, Duration duration) {
        customCounter.increment(Tags.of("service", service));
        customTimer.record(duration, Tags.of("service", service));
    }
    
    private double getActiveInstances() {
        // Implement logic to get active instances count
        return 0.0;
    }
}
```

### Custom Metrics Filter

```java
@Component
public class CustomMetricsFilter implements GlobalFilter, Ordered {
    
    private final CustomMetrics customMetrics;
    
    public CustomMetricsFilter(CustomMetrics customMetrics) {
        this.customMetrics = customMetrics;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String service = extractServiceName(request);
        
        Timer.Sample sample = Timer.start(customMetrics.getMeterRegistry());
        
        return chain.filter(exchange)
                .doOnSuccess(result -> {
                    Duration duration = Duration.ofNanos(sample.stop(customMetrics.getCustomTimer()));
                    customMetrics.recordRequest(service, duration);
                })
                .doOnError(error -> {
                    sample.stop(customMetrics.getCustomTimer());
                    customMetrics.recordRequest(service, Duration.ZERO);
                });
    }
    
    private String extractServiceName(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        if (path.startsWith("/api/")) {
            String[] parts = path.split("/");
            if (parts.length > 2) {
                return parts[2];
            }
        }
        return "unknown";
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

## Custom Configuration

### Custom Configuration Properties

```java
@ConfigurationProperties(prefix = "custom")
@Data
public class CustomConfig {
    
    private boolean enabled = true;
    private String defaultValue = "default";
    private Map<String, String> properties = new HashMap<>();
    private List<String> services = new ArrayList<>();
    
    @Data
    public static class ServiceConfig {
        private String name;
        private String url;
        private int timeout = 5000;
        private boolean enabled = true;
    }
}
```

### Custom Configuration Validation

```java
@Component
@Validated
public class CustomConfigValidator {
    
    @Autowired
    private CustomConfig customConfig;
    
    @PostConstruct
    public void validateConfig() {
        if (customConfig.isEnabled()) {
            // Validate configuration
            if (customConfig.getServices().isEmpty()) {
                throw new IllegalStateException("No services configured");
            }
            
            // Validate service configurations
            for (String service : customConfig.getServices()) {
                if (service == null || service.trim().isEmpty()) {
                    throw new IllegalStateException("Invalid service name: " + service);
                }
            }
        }
    }
}
```

## Best Practices

### Component Design

1. **Single Responsibility**: Each component should have a single responsibility
2. **Configuration**: Make components configurable through properties
3. **Error Handling**: Implement proper error handling
4. **Logging**: Add appropriate logging for debugging
5. **Testing**: Write comprehensive tests for custom components

### Performance Considerations

1. **Efficiency**: Keep custom components efficient
2. **Caching**: Use caching where appropriate
3. **Async Operations**: Use async operations when possible
4. **Resource Management**: Properly manage resources
5. **Monitoring**: Monitor custom component performance

### Security

1. **Input Validation**: Validate all inputs
2. **Access Control**: Implement proper access control
3. **Audit Logging**: Log security-relevant events
4. **Error Handling**: Don't expose sensitive information in errors
5. **Regular Updates**: Keep custom components updated

## Troubleshooting

### Common Issues

#### Custom Component Not Working

```bash
# Check component configuration
curl http://localhost:8080/actuator/configprops | grep -i custom

# Check component logs
tail -f logs/tigateway.log | grep -i custom

# Test component functionality
curl -H "X-Custom-Header: test" http://localhost:8080/api/users/123
```

#### Configuration Issues

```bash
# Check configuration properties
curl http://localhost:8080/actuator/configprops

# Validate configuration
java -jar tigateway.jar --spring.config.location=application.yml --debug

# Check configuration errors
tail -f logs/tigateway.log | grep -i "configuration"
```

#### Performance Issues

```bash
# Check component metrics
curl http://localhost:8080/actuator/metrics/custom.requests.duration

# Monitor component performance
curl http://localhost:8080/actuator/metrics/custom.requests.total

# Check component logs
tail -f logs/tigateway.log | grep -i "custom"
```

### Debug Commands

```bash
# Check component status
curl http://localhost:8080/actuator/health

# Check component configuration
curl http://localhost:8080/actuator/configprops | grep -i custom

# Test component functionality
curl -H "X-Custom-Header: test" http://localhost:8080/api/users/123

# Check component metrics
curl http://localhost:8080/actuator/metrics/custom.requests.total
```

## Next Steps

After creating custom components:

1. **[Monitoring Setup](../monitoring-and-metrics.md)** - Monitor custom component performance
2. **[Troubleshooting Guide](../troubleshooting.md)** - Common custom component issues
3. **[Performance Tuning](../performance-tuning.md)** - Optimize custom component performance
4. **[Security Best Practices](../security-best-practices.md)** - Secure custom components

---

**Ready to monitor your custom components?** Check out our [Monitoring Setup](../monitoring-and-metrics.md) guide for comprehensive monitoring solutions.
