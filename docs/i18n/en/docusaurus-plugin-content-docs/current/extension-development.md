# Extension Development

This guide covers how to develop custom extensions for TiGateway, including filters, predicates, load balancers, and service discovery providers.

## Overview

TiGateway provides a comprehensive extension framework that allows you to:

- **Custom Filters**: Create custom request/response processing logic
- **Custom Predicates**: Implement custom route matching conditions
- **Custom Load Balancers**: Develop custom load balancing strategies
- **Custom Service Discovery**: Integrate with custom service registries
- **Custom Health Checks**: Implement custom health check logic
- **Custom Metrics**: Add custom monitoring and metrics

## Development Environment Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6+ or Gradle 7.0+
- Spring Boot 3.x
- Spring Cloud Gateway 4.x

### Project Structure

```
tigateway-extension/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── tigateway/
│   │   │           └── extension/
│   │   │               ├── filter/
│   │   │               ├── predicate/
│   │   │               ├── loadbalancer/
│   │   │               └── servicediscovery/
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── spring.factories
│   │       └── application.yml
│   └── test/
│       └── java/
├── pom.xml
└── README.md
```

### Maven Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.tigateway</groupId>
    <artifactId>tigateway-extension</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <spring-boot.version>3.2.0</spring-boot.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
            <version>${spring-cloud.version}</version>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <version>${spring-boot.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
            </plugin>
        </plugins>
    </build>
</project>
```

## Custom Filters

### Basic Filter Implementation

```java
package com.tigateway.extension.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomFilter implements GatewayFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        // Add custom header to request
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Custom-Filter", "processed")
                .build();
        
        // Add custom header to response
        response.getHeaders().add("X-Response-Filter", "processed");
        
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        
        return chain.filter(modifiedExchange);
    }
    
    @Override
    public int getOrder() {
        return -1; // Higher priority (lower number = higher priority)
    }
}
```

### Advanced Filter with Configuration

```java
package com.tigateway.extension.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomGatewayFilterFactory.Config> {
    
    public CustomGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Apply custom logic based on configuration
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

### Filter with Request/Response Modification

```java
package com.tigateway.extension.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RequestResponseModificationFilter implements GatewayFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        // Modify request body
        if (request.getHeaders().getContentType() != null && 
            request.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)) {
            
            return DataBufferUtils.join(request.getBody())
                    .flatMap(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        
                        // Modify request body
                        String modifiedBody = modifyRequestBody(new String(bytes));
                        
                        ServerHttpRequest modifiedRequest = request.mutate()
                                .body(modifiedBody)
                                .build();
                        
                        ServerWebExchange modifiedExchange = exchange.mutate()
                                .request(modifiedRequest)
                                .build();
                        
                        return chain.filter(modifiedExchange);
                    });
        }
        
        return chain.filter(exchange);
    }
    
    private String modifyRequestBody(String originalBody) {
        // Custom logic to modify request body
        return originalBody.replace("old-value", "new-value");
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

## Custom Predicates

### Basic Predicate Implementation

```java
package com.tigateway.extension.predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

@Component
public class CustomPredicateFactory extends AbstractRoutePredicateFactory<CustomPredicateFactory.Config> {
    
    public CustomPredicateFactory() {
        super(Config.class);
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Custom predicate logic
            String customHeader = request.getHeaders().getFirst(config.getHeaderName());
            return customHeader != null && customHeader.equals(config.getExpectedValue());
        };
    }
    
    @Data
    public static class Config {
        private String headerName = "X-Custom-Header";
        private String expectedValue = "expected-value";
    }
}
```

### Advanced Predicate with Multiple Conditions

```java
package com.tigateway.extension.predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

@Component
public class AdvancedPredicateFactory extends AbstractRoutePredicateFactory<AdvancedPredicateFactory.Config> {
    
    public AdvancedPredicateFactory() {
        super(Config.class);
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Check multiple conditions
            boolean headerMatch = checkHeader(request, config);
            boolean pathMatch = checkPath(request, config);
            boolean methodMatch = checkMethod(request, config);
            boolean timeMatch = checkTime(config);
            
            return headerMatch && pathMatch && methodMatch && timeMatch;
        };
    }
    
    private boolean checkHeader(ServerHttpRequest request, Config config) {
        String headerValue = request.getHeaders().getFirst(config.getHeaderName());
        return headerValue != null && headerValue.matches(config.getHeaderPattern());
    }
    
    private boolean checkPath(ServerHttpRequest request, Config config) {
        String path = request.getURI().getPath();
        return path.matches(config.getPathPattern());
    }
    
    private boolean checkMethod(ServerHttpRequest request, Config config) {
        return config.getMethods().contains(request.getMethod());
    }
    
    private boolean checkTime(Config config) {
        LocalTime now = LocalTime.now();
        return now.isAfter(config.getStartTime()) && now.isBefore(config.getEndTime());
    }
    
    @Data
    public static class Config {
        private String headerName = "X-Custom-Header";
        private String headerPattern = ".*";
        private String pathPattern = ".*";
        private List<HttpMethod> methods = Arrays.asList(HttpMethod.GET, HttpMethod.POST);
        private LocalTime startTime = LocalTime.of(0, 0);
        private LocalTime endTime = LocalTime.of(23, 59);
    }
}
```

## Custom Load Balancers

### Basic Load Balancer Implementation

```java
package com.tigateway.extension.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class CustomLoadBalancerConfiguration {
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> customLoadBalancer(
            ConfigurableApplicationContext context,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        
        String name = context.getEnvironment().getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new CustomLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
                name);
    }
}

class CustomLoadBalancer implements ReactorLoadBalancer<ServiceInstance> {
    
    private final AtomicInteger nextServerCyclicCounter = new AtomicInteger(0);
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
        
        return supplier.get(request).next()
                .map(serviceInstances -> processInstanceResponse(serviceInstances, request));
    }
    
    private Response<ServiceInstance> processInstanceResponse(List<ServiceInstance> serviceInstances,
                                                             Request request) {
        if (serviceInstances.isEmpty()) {
            return new EmptyResponse();
        }
        
        // Custom load balancing logic
        ServiceInstance instance = chooseInstance(serviceInstances, request);
        return new DefaultResponse(instance);
    }
    
    private ServiceInstance chooseInstance(List<ServiceInstance> instances, Request request) {
        // Custom selection logic (e.g., based on request headers, user preferences, etc.)
        int index = nextServerCyclicCounter.getAndIncrement() % instances.size();
        return instances.get(index);
    }
}
```

### Advanced Load Balancer with Health Awareness

```java
package com.tigateway.extension.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class HealthAwareLoadBalancerConfiguration {
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> healthAwareLoadBalancer(
            ConfigurableApplicationContext context,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        
        String name = context.getEnvironment().getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new HealthAwareLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
                name);
    }
}

class HealthAwareLoadBalancer implements ReactorLoadBalancer<ServiceInstance> {
    
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    private final WebClient webClient;
    private final ConcurrentHashMap<String, Boolean> healthCache = new ConcurrentHashMap<>();
    private final AtomicInteger nextServerCyclicCounter = new AtomicInteger(0);
    
    public HealthAwareLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
                                  String serviceId) {
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
        this.webClient = WebClient.builder().build();
    }
    
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        
        return supplier.get(request).next()
                .flatMap(serviceInstances -> filterHealthyInstances(serviceInstances))
                .map(healthyInstances -> processInstanceResponse(healthyInstances, request));
    }
    
    private Mono<List<ServiceInstance>> filterHealthyInstances(List<ServiceInstance> instances) {
        return Flux.fromIterable(instances)
                .flatMap(this::checkHealth)
                .filter(HealthCheckResult::isHealthy)
                .map(HealthCheckResult::getInstance)
                .collectList();
    }
    
    private Mono<HealthCheckResult> checkHealth(ServiceInstance instance) {
        String instanceId = instance.getInstanceId();
        
        // Check cache first
        Boolean cachedHealth = healthCache.get(instanceId);
        if (cachedHealth != null) {
            return Mono.just(new HealthCheckResult(instance, cachedHealth));
        }
        
        // Perform health check
        String healthUrl = String.format("http://%s:%d/actuator/health", 
                instance.getHost(), instance.getPort());
        
        return webClient.get()
                .uri(healthUrl)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    boolean isHealthy = response.contains("\"status\":\"UP\"");
                    healthCache.put(instanceId, isHealthy);
                    return new HealthCheckResult(instance, isHealthy);
                })
                .onErrorReturn(new HealthCheckResult(instance, false));
    }
    
    private Response<ServiceInstance> processInstanceResponse(List<ServiceInstance> serviceInstances,
                                                             Request request) {
        if (serviceInstances.isEmpty()) {
            return new EmptyResponse();
        }
        
        ServiceInstance instance = chooseInstance(serviceInstances, request);
        return new DefaultResponse(instance);
    }
    
    private ServiceInstance chooseInstance(List<ServiceInstance> instances, Request request) {
        int index = nextServerCyclicCounter.getAndIncrement() % instances.size();
        return instances.get(index);
    }
    
    private static class HealthCheckResult {
        private final ServiceInstance instance;
        private final boolean healthy;
        
        public HealthCheckResult(ServiceInstance instance, boolean healthy) {
            this.instance = instance;
            this.healthy = healthy;
        }
        
        public ServiceInstance getInstance() {
            return instance;
        }
        
        public boolean isHealthy() {
            return healthy;
        }
    }
}
```

## Custom Service Discovery

### Basic Service Discovery Implementation

```java
package com.tigateway.extension.servicediscovery;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomServiceDiscovery implements DiscoveryClient {
    
    private final Map<String, List<ServiceInstance>> serviceInstances = new ConcurrentHashMap<>();
    
    @Override
    public String description() {
        return "Custom Service Discovery";
    }
    
    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        return serviceInstances.getOrDefault(serviceId, List.of());
    }
    
    @Override
    public List<String> getServices() {
        return List.copyOf(serviceInstances.keySet());
    }
    
    public void registerService(String serviceId, ServiceInstance instance) {
        serviceInstances.computeIfAbsent(serviceId, k -> new CopyOnWriteArrayList<>())
                .add(instance);
    }
    
    public void deregisterService(String serviceId, String instanceId) {
        List<ServiceInstance> instances = serviceInstances.get(serviceId);
        if (instances != null) {
            instances.removeIf(instance -> instance.getInstanceId().equals(instanceId));
        }
    }
}
```

## Custom Health Checks

### Basic Health Check Implementation

```java
package com.tigateway.extension.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // Perform custom health check
            boolean isHealthy = performHealthCheck();
            
            if (isHealthy) {
                return Health.up()
                        .withDetail("custom-check", "OK")
                        .withDetail("timestamp", System.currentTimeMillis())
                        .build();
            } else {
                return Health.down()
                        .withDetail("custom-check", "FAILED")
                        .withDetail("error", "Custom health check failed")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("custom-check", "ERROR")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
    
    private boolean performHealthCheck() {
        // Custom health check logic
        return true;
    }
}
```

## Custom Metrics

### Basic Metrics Implementation

```java
package com.tigateway.extension.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class CustomMetrics {
    
    private final Counter requestCounter;
    private final Timer requestTimer;
    private final AtomicLong activeConnections;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.requestCounter = Counter.builder("tigateway.custom.requests")
                .description("Custom request counter")
                .register(meterRegistry);
        
        this.requestTimer = Timer.builder("tigateway.custom.request.duration")
                .description("Custom request duration")
                .register(meterRegistry);
        
        this.activeConnections = meterRegistry.gauge("tigateway.custom.active.connections",
                new AtomicLong(0));
    }
    
    public void incrementRequestCount() {
        requestCounter.increment();
    }
    
    public Timer.Sample startTimer() {
        return Timer.start();
    }
    
    public void recordTimer(Timer.Sample sample) {
        sample.stop(requestTimer);
    }
    
    public void setActiveConnections(long count) {
        activeConnections.set(count);
    }
}
```

## Testing Extensions

### Unit Testing

```java
package com.tigateway.extension.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server