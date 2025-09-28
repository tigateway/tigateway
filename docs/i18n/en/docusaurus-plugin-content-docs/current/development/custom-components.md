# TiGateway Custom Components Development Guide

## Overview

This guide explains how to develop custom route predicates, filters, and other components to extend TiGateway's functionality. You'll learn how to create custom components that integrate seamlessly with Spring Cloud Gateway.

## Development Environment Setup

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Spring Boot 2.7+
- Spring Cloud Gateway 3.1+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Project Structure

```
tigateway-custom-components/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/tigateway/
│   │   │       ├── predicate/
│   │   │       │   ├── CustomRoutePredicateFactory.java
│   │   │       │   └── config/
│   │   │       │       └── CustomPredicateConfig.java
│   │   │       ├── filter/
│   │   │       │   ├── CustomGatewayFilterFactory.java
│   │   │       │   └── config/
│   │   │       │       └── CustomFilterConfig.java
│   │   │       └── balancer/
│   │   │           └── CustomLoadBalancer.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── META-INF/
│   │           └── spring.factories
│   └── test/
│       └── java/
│           └── com/example/tigateway/
│               ├── predicate/
│               └── filter/
└── README.md
```

### Maven Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Custom Route Predicates

### Basic Predicate Factory

```java
@Component
public class CustomRoutePredicateFactory extends AbstractRoutePredicateFactory<CustomRoutePredicateFactory.Config> {
    
    public CustomRoutePredicateFactory() {
        super(Config.class);
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            // Custom predicate logic
            String headerValue = exchange.getRequest().getHeaders().getFirst(config.getHeaderName());
            return config.getExpectedValue().equals(headerValue);
        };
    }
    
    @Data
    public static class Config {
        private String headerName = "X-Custom-Header";
        private String expectedValue = "custom-value";
    }
}
```

### Advanced Predicate Factory

```java
@Component
@Slf4j
public class TimeBasedRoutePredicateFactory extends AbstractRoutePredicateFactory<TimeBasedRoutePredicateFactory.Config> {
    
    public TimeBasedRoutePredicateFactory() {
        super(Config.class);
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            LocalTime currentTime = LocalTime.now();
            LocalTime startTime = config.getStartTime();
            LocalTime endTime = config.getEndTime();
            
            boolean isWithinTimeRange = currentTime.isAfter(startTime) && currentTime.isBefore(endTime);
            
            log.debug("Time-based predicate check: current={}, start={}, end={}, result={}", 
                currentTime, startTime, endTime, isWithinTimeRange);
            
            return isWithinTimeRange;
        };
    }
    
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("startTime", "endTime");
    }
    
    @Data
    public static class Config {
        @DateTimeFormat(pattern = "HH:mm")
        private LocalTime startTime = LocalTime.of(9, 0);
        
        @DateTimeFormat(pattern = "HH:mm")
        private LocalTime endTime = LocalTime.of(17, 0);
    }
}
```

### Usage in Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: time-based-route
          uri: lb://business-hours-service
          predicates:
            - TimeBased=09:00,17:00
            - Path=/api/business/**
```

## Custom Gateway Filters

### Basic Filter Factory

```java
@Component
@Slf4j
public class CustomGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomGatewayFilterFactory.Config> {
    
    public CustomGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Pre-processing
            log.info("Processing request: {} {}", request.getMethod(), request.getURI());
            
            // Add custom header
            ServerHttpRequest modifiedRequest = request.mutate()
                .header(config.getHeaderName(), config.getHeaderValue())
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
        };
    }
    
    @Data
    public static class Config {
        private String headerName = "X-Custom-Filter";
        private String headerValue = "processed";
    }
}
```

### Request Processing Filter

```java
@Component
@Slf4j
public class RequestTransformationFilterFactory extends AbstractGatewayFilterFactory<RequestTransformationFilterFactory.Config> {
    
    public RequestTransformationFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (config.isEnabled()) {
                return transformRequest(exchange, chain, config);
            }
            return chain.filter(exchange);
        };
    }
    
    private Mono<Void> transformRequest(ServerWebExchange exchange, GatewayFilterChain chain, Config config) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Transform request body
        if (request.getHeaders().getContentType() != null && 
            request.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)) {
            
            return DataBufferUtils.join(request.getBody())
                .defaultIfEmpty(DataBufferFactory.DEFAULT_ALLOCATOR.allocateBuffer(0))
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    
                    try {
                        String jsonString = new String(bytes, StandardCharsets.UTF_8);
                        String transformedJson = transformJson(jsonString, config);
                        
                        byte[] transformedBytes = transformedJson.getBytes(StandardCharsets.UTF_8);
                        DataBuffer transformedDataBuffer = exchange.getResponse().bufferFactory().wrap(transformedBytes);
                        
                        ServerHttpRequest transformedRequest = request.mutate()
                            .body(Flux.just(transformedDataBuffer))
                            .build();
                        
                        return chain.filter(exchange.mutate().request(transformedRequest).build());
                        
                    } catch (Exception e) {
                        log.error("Error transforming request body", e);
                        return chain.filter(exchange);
                    }
                });
        }
        
        return chain.filter(exchange);
    }
    
    private String transformJson(String jsonString, Config config) {
        // Custom JSON transformation logic
        if (config.isAddTimestamp()) {
            // Add timestamp to JSON
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode jsonNode = mapper.readTree(jsonString);
                ((ObjectNode) jsonNode).put("timestamp", Instant.now().toString());
                return mapper.writeValueAsString(jsonNode);
            } catch (Exception e) {
                log.error("Error adding timestamp to JSON", e);
                return jsonString;
            }
        }
        return jsonString;
    }
    
    @Data
    public static class Config {
        private boolean enabled = true;
        private boolean addTimestamp = false;
        private String transformationType = "default";
    }
}
```

### Response Processing Filter

```java
@Component
@Slf4j
public class ResponseEnhancementFilterFactory extends AbstractGatewayFilterFactory<ResponseEnhancementFilterFactory.Config> {
    
    public ResponseEnhancementFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    
                    // Add custom response headers
                    if (config.getResponseHeaders() != null) {
                        config.getResponseHeaders().forEach(response.getHeaders()::add);
                    }
                    
                    // Add processing time header
                    if (config.isAddProcessingTime()) {
                        Long startTime = exchange.getAttribute("startTime");
                        if (startTime != null) {
                            long processingTime = System.currentTimeMillis() - startTime;
                            response.getHeaders().add("X-Processing-Time", String.valueOf(processingTime));
                        }
                    }
                    
                    log.debug("Response enhanced with headers: {}", response.getHeaders());
                }));
        };
    }
    
    @Data
    public static class Config {
        private Map<String, String> responseHeaders = new HashMap<>();
        private boolean addProcessingTime = true;
    }
}
```

## Global Filters

### Custom Global Filter

```java
@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    
    private final MeterRegistry meterRegistry;
    private final Counter requestCounter;
    private final Timer requestTimer;
    
    public CustomGlobalFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestCounter = Counter.builder("tigateway.requests.total")
            .description("Total number of requests")
            .register(meterRegistry);
        this.requestTimer = Timer.builder("tigateway.request.duration")
            .description("Request processing duration")
            .register(meterRegistry);
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put("startTime", startTime);
        
        // Increment request counter
        requestCounter.increment(
            Tags.of(
                "method", exchange.getRequest().getMethod().name(),
                "path", exchange.getRequest().getPath().value()
            )
        );
        
        return chain.filter(exchange)
            .doOnSuccess(result -> {
                long duration = System.currentTimeMillis() - startTime;
                requestTimer.record(duration, TimeUnit.MILLISECONDS);
                
                log.debug("Request processed in {}ms: {} {}", 
                    duration, 
                    exchange.getRequest().getMethod(), 
                    exchange.getRequest().getURI());
            })
            .doOnError(error -> {
                long duration = System.currentTimeMillis() - startTime;
                requestTimer.record(duration, TimeUnit.MILLISECONDS);
                
                log.error("Request failed after {}ms: {} {}", 
                    duration, 
                    exchange.getRequest().getMethod(), 
                    exchange.getRequest().getURI(), 
                    error);
            });
    }
    
    @Override
    public int getOrder() {
        return -1000; // High priority
    }
}
```

## Custom Load Balancers

### Custom Load Balancer Implementation

```java
@Component
public class CustomLoadBalancer implements ReactorLoadBalancer<ServiceInstance> {
    
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    
    public CustomLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
                            String serviceId) {
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
    }
    
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
            .getIfAvailable(NoopServiceInstanceListSupplier::new);
        
        return supplier.get(request)
            .next()
            .map(serviceInstances -> processInstanceResponse(serviceInstances, request));
    }
    
    private Response<ServiceInstance> processInstanceResponse(List<ServiceInstance> serviceInstances, Request request) {
        if (serviceInstances.isEmpty()) {
            return new EmptyResponse();
        }
        
        // Custom load balancing logic
        ServiceInstance selectedInstance = selectInstance(serviceInstances, request);
        
        return new DefaultResponse(selectedInstance);
    }
    
    private ServiceInstance selectInstance(List<ServiceInstance> instances, Request request) {
        // Custom selection logic - e.g., weighted round-robin, least connections, etc.
        return instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
    }
}
```

### Load Balancer Configuration

```java
@Configuration
public class CustomLoadBalancerConfiguration {
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> customLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new CustomLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
            name);
    }
}
```

## Custom Configuration Properties

### Configuration Properties Class

```java
@ConfigurationProperties(prefix = "tigateway.custom")
@Data
@Validated
public class CustomComponentProperties {
    
    private Predicate predicate = new Predicate();
    private Filter filter = new Filter();
    private LoadBalancer loadBalancer = new LoadBalancer();
    
    @Data
    public static class Predicate {
        private boolean enabled = true;
        private String defaultHeaderName = "X-Custom-Header";
        private String defaultExpectedValue = "custom-value";
    }
    
    @Data
    public static class Filter {
        private boolean enabled = true;
        private boolean addTimestamp = false;
        private Map<String, String> defaultHeaders = new HashMap<>();
    }
    
    @Data
    public static class LoadBalancer {
        private boolean enabled = true;
        private String algorithm = "round-robin";
        private int maxRetries = 3;
    }
}
```

### Auto-Configuration

```java
@Configuration
@EnableConfigurationProperties(CustomComponentProperties.class)
@ConditionalOnProperty(name = "tigateway.custom.enabled", havingValue = "true", matchIfMissing = true)
public class CustomComponentAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public CustomRoutePredicateFactory customRoutePredicateFactory(CustomComponentProperties properties) {
        return new CustomRoutePredicateFactory();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public CustomGatewayFilterFactory customGatewayFilterFactory(CustomComponentProperties properties) {
        return new CustomGatewayFilterFactory();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public CustomGlobalFilter customGlobalFilter(MeterRegistry meterRegistry) {
        return new CustomGlobalFilter(meterRegistry);
    }
}
```

## Testing Custom Components

### Unit Tests for Predicates

```java
@ExtendWith(MockitoExtension.class)
class CustomRoutePredicateFactoryTest {
    
    private CustomRoutePredicateFactory predicateFactory;
    
    @BeforeEach
    void setUp() {
        predicateFactory = new CustomRoutePredicateFactory();
    }
    
    @Test
    @DisplayName("Should match when header value equals expected value")
    void shouldMatchWhenHeaderValueEqualsExpectedValue() {
        // Given
        CustomRoutePredicateFactory.Config config = new CustomRoutePredicateFactory.Config();
        config.setHeaderName("X-Custom-Header");
        config.setExpectedValue("test-value");
        
        ServerHttpRequest request = MockServerHttpRequest.get("/test")
            .header("X-Custom-Header", "test-value")
            .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Predicate<ServerWebExchange> predicate = predicateFactory.apply(config);
        boolean result = predicate.test(exchange);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("Should not match when header value differs from expected value")
    void shouldNotMatchWhenHeaderValueDiffersFromExpectedValue() {
        // Given
        CustomRoutePredicateFactory.Config config = new CustomRoutePredicateFactory.Config();
        config.setHeaderName("X-Custom-Header");
        config.setExpectedValue("test-value");
        
        ServerHttpRequest request = MockServerHttpRequest.get("/test")
            .header("X-Custom-Header", "different-value")
            .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Predicate<ServerWebExchange> predicate = predicateFactory.apply(config);
        boolean result = predicate.test(exchange);
        
        // Then
        assertThat(result).isFalse();
    }
}
```

### Integration Tests for Filters

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomGatewayFilterFactoryIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    @DisplayName("Should apply custom filter to requests")
    void shouldApplyCustomFilterToRequests() {
        // Given
        CustomGatewayFilterFactory.Config config = new CustomGatewayFilterFactory.Config();
        config.setHeaderName("X-Custom-Filter");
        config.setHeaderValue("processed");
        
        // When & Then
        webTestClient.get()
            .uri("/test")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists("X-Custom-Filter");
    }
}
```

## Packaging and Distribution

### Maven Configuration

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>11</source>
                <target>11</target>
            </configuration>
        </plugin>
        
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <skip>true</skip>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Spring Factories Configuration

```properties
# META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.tigateway.config.CustomComponentAutoConfiguration
```

## Best Practices

### 1. Component Design

- Keep components focused and single-purpose
- Use dependency injection for external dependencies
- Implement proper error handling and logging
- Follow Spring Cloud Gateway conventions

### 2. Configuration

- Use `@ConfigurationProperties` for configuration
- Provide sensible defaults
- Validate configuration values
- Support both programmatic and declarative configuration

### 3. Testing

- Write comprehensive unit tests
- Include integration tests
- Test error scenarios
- Use proper mocking and test utilities

### 4. Performance

- Minimize object creation in hot paths
- Use reactive programming patterns
- Implement proper caching where appropriate
- Monitor performance impact

### 5. Documentation

- Document all configuration options
- Provide usage examples
- Include troubleshooting information
- Maintain API documentation

---

**Related Documentation**:
- [Predicate Factories Development](./predicate-factories.md)
- [Filter Factories Development](./filter-factories.md)
- [Spring Cloud Gateway Integration](./spring-cloud-gateway-integration.md)
- [Testing Guide](./testing.md)
