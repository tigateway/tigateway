# TiGateway Debugging Guide

## Overview

This document provides a comprehensive debugging guide for TiGateway, covering environment configuration, log debugging, performance debugging, network debugging, and troubleshooting common issues.

## Environment Configuration

### Development Environment Setup

#### YAML Configuration

```yaml
# application-dev.yml
spring:
  profiles:
    active: dev
  logging:
    level:
      ti.gateway: DEBUG
      org.springframework.cloud.gateway: DEBUG
      org.springframework.web.reactive: DEBUG
      reactor.netty: DEBUG
      org.springframework.cloud.kubernetes: DEBUG
    pattern:
      console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
    loggers:
      enabled: true
    metrics:
      enabled: true

tigateway:
  debug:
    enabled: true
    request-logging: true
    response-logging: true
    route-matching: true
```

#### IDE Configuration

**IntelliJ IDEA Configuration:**

```xml
<!-- .idea/runConfigurations/TiGateway_Debug.xml -->
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="TiGateway Debug" type="SpringBootApplicationConfigurationType">
    <module name="ti-gateway-kubernetes" />
    <option name="SPRING_BOOT_MAIN_CLASS" value="cn.tigateway.TiGatewayApplication" />
    <option name="ACTIVE_PROFILES" value="dev" />
    <option name="VM_PARAMETERS" value="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" />
    <option name="PROGRAM_PARAMETERS" value="--spring.profiles.active=dev --debug" />
    <option name="WORKING_DIRECTORY" value="$PROJECT_DIR$" />
  </configuration>
</component>
```

**VS Code Configuration:**

```json
// .vscode/launch.json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug TiGateway",
      "request": "launch",
      "mainClass": "cn.tigateway.TiGatewayApplication",
      "projectName": "ti-gateway-kubernetes",
      "args": "--spring.profiles.active=dev",
      "vmArgs": "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005",
      "env": {
        "SPRING_PROFILES_ACTIVE": "dev"
      }
    }
  ]
}
```

#### Remote Debugging

```bash
# Start application with remote debugging enabled
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
     -jar tigateway-kubernetes-1.0.0.jar \
     --spring.profiles.active=dev

# Connect from IDE using:
# Host: localhost
# Port: 5005
```

## Log Debugging

### Log Level Configuration

#### Dynamic Log Level Adjustment

```bash
# Adjust log level via actuator endpoint
curl -X POST http://localhost:8090/actuator/loggers/ti.gateway \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# Check current log level
curl http://localhost:8090/actuator/loggers/ti.gateway

# Reset to default level
curl -X POST http://localhost:8090/actuator/loggers/ti.gateway \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": null}'
```

#### Structured Logging Configuration

```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="dev">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <stackTrace/>
                    <pattern>
                        <pattern>
                            {
                                "traceId": "%X{traceId:-}",
                                "spanId": "%X{spanId:-}",
                                "requestId": "%X{requestId:-}",
                                "userId": "%X{userId:-}",
                                "tenantId": "%X{tenantId:-}"
                            }
                        </pattern>
                    </pattern>
                </providers>
            </encoder>
        </appender>
        
        <logger name="ti.gateway" level="DEBUG" additivity="false">
            <appender-ref ref="CONSOLE"/>
        </logger>
        
        <logger name="org.springframework.cloud.gateway" level="DEBUG" additivity="false">
            <appender-ref ref="CONSOLE"/>
        </logger>
        
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
</configuration>
```

### Log Analysis Tools

#### Log Analysis Scripts

```bash
#!/bin/bash
# analyze-logs.sh

LOG_FILE="/app/logs/tigateway.log"

echo "=== Error Analysis ==="
grep -i "error\|exception\|failed" "$LOG_FILE" | tail -20

echo "=== Performance Analysis ==="
grep -i "slow\|timeout\|latency" "$LOG_FILE" | tail -20

echo "=== Route Matching Analysis ==="
grep -i "route.*match" "$LOG_FILE" | tail -20

echo "=== Authentication Analysis ==="
grep -i "authentication\|authorization" "$LOG_FILE" | tail -20

echo "=== Request Flow Analysis ==="
grep -E "REQUEST|RESPONSE" "$LOG_FILE" | tail -20
```

#### Log Monitoring with jq

```bash
# Parse JSON logs and extract specific information
tail -f /app/logs/tigateway.log | jq -r 'select(.level == "ERROR") | .message'

# Extract request timing information
tail -f /app/logs/tigateway.log | jq -r 'select(.message | contains("Request processed")) | {timestamp, duration: .duration, method: .method, path: .path}'

# Monitor specific user requests
tail -f /app/logs/tigateway.log | jq -r 'select(.userId == "user123") | {timestamp, message}'
```

## Performance Debugging

### JVM Performance Monitoring

#### JVM Parameters for Debugging

```bash
# JVM debugging parameters
JAVA_OPTS="-Xms512m -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+PrintGC \
  -XX:+PrintGCDetails \
  -XX:+PrintGCTimeStamps \
  -XX:+PrintGCApplicationStoppedTime \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/dumps/ \
  -XX:+FlightRecorder \
  -XX:StartFlightRecording=duration=60s,filename=/app/flight-recorder.jfr"
```

#### Memory Analysis

```bash
# Generate heap dump
jcmd <pid> GC.run_finalization
jcmd <pid> VM.gc
jmap -dump:format=b,file=heap.hprof <pid>

# Analyze heap dump with jhat
jhat -J-Xmx2g heap.hprof

# Memory usage analysis
jstat -gc <pid> 5s
jstat -gccapacity <pid>
jstat -gcutil <pid> 5s
```

#### Thread Analysis

```bash
# Generate thread dump
jstack <pid> > thread-dump.txt

# Analyze thread dump
grep -A 5 -B 5 "BLOCKED\|WAITING" thread-dump.txt

# Monitor thread count
jcmd <pid> Thread.print
```

### Application Performance Monitoring

#### Custom Performance Metrics

```java
@Component
@Slf4j
public class PerformanceDebugFilter implements GlobalFilter, Ordered {
    
    private final MeterRegistry meterRegistry;
    private final Timer requestTimer;
    private final Counter errorCounter;
    
    public PerformanceDebugFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestTimer = Timer.builder("tigateway.debug.request.duration")
            .description("Request processing duration for debugging")
            .register(meterRegistry);
        this.errorCounter = Counter.builder("tigateway.debug.errors")
            .description("Error count for debugging")
            .register(meterRegistry);
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        
        // Add request ID to MDC for tracing
        MDC.put("requestId", requestId);
        MDC.put("startTime", String.valueOf(startTime));
        
        return chain.filter(exchange)
            .doOnSuccess(result -> {
                long duration = System.currentTimeMillis() - startTime;
                requestTimer.record(duration, TimeUnit.MILLISECONDS);
                
                log.debug("Request {} completed in {}ms: {} {}", 
                    requestId, duration, 
                    exchange.getRequest().getMethod(), 
                    exchange.getRequest().getURI());
                
                // Log slow requests
                if (duration > 1000) {
                    log.warn("Slow request detected: {} took {}ms", requestId, duration);
                }
            })
            .doOnError(error -> {
                long duration = System.currentTimeMillis() - startTime;
                errorCounter.increment();
                
                log.error("Request {} failed after {}ms: {} {}", 
                    requestId, duration,
                    exchange.getRequest().getMethod(), 
                    exchange.getRequest().getURI(), 
                    error);
            })
            .doFinally(signalType -> {
                MDC.clear();
            });
    }
    
    @Override
    public int getOrder() {
        return -1000;
    }
}
```

#### Performance Monitoring Endpoints

```java
@RestController
@RequestMapping("/debug/performance")
@Slf4j
public class PerformanceDebugController {
    
    private final MeterRegistry meterRegistry;
    
    public PerformanceDebugController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // JVM metrics
        metrics.put("jvm.memory.used", getJvmMemoryUsed());
        metrics.put("jvm.memory.max", getJvmMemoryMax());
        metrics.put("jvm.gc.pause", getGcPauseTime());
        
        // Application metrics
        metrics.put("request.rate", getRequestRate());
        metrics.put("response.time.p95", getResponseTimeP95());
        metrics.put("error.rate", getErrorRate());
        
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/threads")
    public ResponseEntity<Map<String, Object>> getThreadInfo() {
        Map<String, Object> threadInfo = new HashMap<>();
        
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        threadInfo.put("threadCount", threadBean.getThreadCount());
        threadInfo.put("peakThreadCount", threadBean.getPeakThreadCount());
        threadInfo.put("daemonThreadCount", threadBean.getDaemonThreadCount());
        
        // Thread states
        Map<Thread.State, Integer> threadStates = new HashMap<>();
        for (Thread.State state : Thread.State.values()) {
            threadStates.put(state, 0);
        }
        
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            Thread.State state = thread.getState();
            threadStates.put(state, threadStates.get(state) + 1);
        }
        
        threadInfo.put("threadStates", threadStates);
        
        return ResponseEntity.ok(threadInfo);
    }
}
```

## Network Debugging

### Network Connection Analysis

#### Connection Pool Monitoring

```java
@Component
@Slf4j
public class ConnectionPoolDebugFilter implements GlobalFilter, Ordered {
    
    private final HttpClient httpClient;
    
    public ConnectionPoolDebugFilter(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
            .doOnSuccess(result -> {
                // Log connection pool statistics
                logConnectionPoolStats();
            });
    }
    
    private void logConnectionPoolStats() {
        // This would need to be implemented based on the specific HTTP client
        // For Reactor Netty, you can access connection pool metrics
        log.debug("Connection pool statistics: {}", getConnectionPoolMetrics());
    }
    
    private Map<String, Object> getConnectionPoolMetrics() {
        // Implementation to get connection pool metrics
        return new HashMap<>();
    }
    
    @Override
    public int getOrder() {
        return -500;
    }
}
```

#### Network Debugging Scripts

```bash
#!/bin/bash
# network-debug.sh

echo "=== Network Connections ==="
netstat -tulpn | grep :8080
netstat -tulpn | grep :8081

echo "=== Active Connections ==="
ss -tuln | grep :8080
ss -tuln | grep :8081

echo "=== Connection Statistics ==="
ss -s

echo "=== Network Interface Statistics ==="
cat /proc/net/dev

echo "=== TCP Connection States ==="
ss -tuln | awk '{print $1}' | sort | uniq -c
```

### DNS and Service Discovery Debugging

```java
@Component
@Slf4j
public class ServiceDiscoveryDebugFilter implements GlobalFilter, Ordered {
    
    private final DiscoveryClient discoveryClient;
    
    public ServiceDiscoveryDebugFilter(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String serviceName = extractServiceName(exchange);
        
        if (serviceName != null) {
            logServiceDiscoveryInfo(serviceName);
        }
        
        return chain.filter(exchange);
    }
    
    private String extractServiceName(ServerWebExchange exchange) {
        // Extract service name from route or request
        return exchange.getAttribute("serviceName");
    }
    
    private void logServiceDiscoveryInfo(String serviceName) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            log.debug("Service {} has {} instances: {}", 
                serviceName, instances.size(), 
                instances.stream()
                    .map(instance -> instance.getHost() + ":" + instance.getPort())
                    .collect(Collectors.joining(", ")));
        } catch (Exception e) {
            log.warn("Failed to get service instances for {}: {}", serviceName, e.getMessage());
        }
    }
    
    @Override
    public int getOrder() {
        return -300;
    }
}
```

## Database Debugging

### Database Connection Monitoring

```java
@Component
@Slf4j
public class DatabaseDebugFilter implements GlobalFilter, Ordered {
    
    private final DataSource dataSource;
    
    public DatabaseDebugFilter(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
            .doOnSuccess(result -> {
                logDatabaseConnectionInfo();
            });
    }
    
    private void logDatabaseConnectionInfo() {
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
                
                log.debug("Database connection pool - Active: {}, Idle: {}, Total: {}, Waiting: {}", 
                    poolBean.getActiveConnections(),
                    poolBean.getIdleConnections(),
                    poolBean.getTotalConnections(),
                    poolBean.getThreadsAwaitingConnection());
            }
        } catch (Exception e) {
            log.warn("Failed to get database connection info: {}", e.getMessage());
        }
    }
    
    @Override
    public int getOrder() {
        return -400;
    }
}
```

### SQL Query Debugging

```yaml
# application-dev.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        generate_statistics: true
  datasource:
    hikari:
      leak-detection-threshold: 60000
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

## Cache Debugging

### Cache Performance Monitoring

```java
@Component
@Slf4j
public class CacheDebugFilter implements GlobalFilter, Ordered {
    
    private final CacheManager cacheManager;
    
    public CacheDebugFilter(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
            .doOnSuccess(result -> {
                logCacheStatistics();
            });
    }
    
    private void logCacheStatistics() {
        try {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            for (String cacheName : cacheNames) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    log.debug("Cache {} statistics: {}", cacheName, getCacheStatistics(cache));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get cache statistics: {}", e.getMessage());
        }
    }
    
    private Map<String, Object> getCacheStatistics(Cache cache) {
        Map<String, Object> stats = new HashMap<>();
        
        if (cache instanceof RedisCache) {
            // Redis cache statistics
            stats.put("type", "redis");
        } else if (cache instanceof CaffeineCache) {
            // Caffeine cache statistics
            stats.put("type", "caffeine");
        }
        
        return stats;
    }
    
    @Override
    public int getOrder() {
        return -600;
    }
}
```

## Troubleshooting Common Issues

### Issue 1: Route Not Matching

#### Symptoms
- Requests return 404 Not Found
- Routes are configured but not working

#### Debugging Steps

```bash
# Check route configuration
curl http://localhost:8080/actuator/gateway/routes

# Check route refresh
curl -X POST http://localhost:8080/actuator/gateway/refresh

# Enable route matching debug logs
curl -X POST http://localhost:8090/actuator/loggers/org.springframework.cloud.gateway \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# Test route matching
curl -v http://localhost:8080/api/test
```

#### Common Causes and Solutions

```yaml
# Incorrect predicate configuration
spring:
  cloud:
    gateway:
      routes:
        - id: test-route
          uri: lb://test-service
          predicates:
            - Path=/api/test/**  # Correct
            # - Path=/api/test  # Incorrect - missing wildcard
```

### Issue 2: Service Discovery Problems

#### Symptoms
- Services not found
- Load balancing not working

#### Debugging Steps

```bash
# Check service discovery
curl http://localhost:8080/actuator/gateway/globalfilters

# Check discovered services
curl http://localhost:8080/actuator/health

# Check Kubernetes services
kubectl get svc
kubectl get endpoints
```

### Issue 3: Performance Issues

#### Symptoms
- Slow response times
- High memory usage
- High CPU usage

#### Debugging Steps

```bash
# Check JVM metrics
curl http://localhost:8090/actuator/metrics/jvm.memory.used
curl http://localhost:8090/actuator/metrics/jvm.gc.pause

# Check application metrics
curl http://localhost:8090/actuator/metrics/http.server.requests

# Generate heap dump
jcmd <pid> GC.run_finalization
jmap -dump:format=b,file=heap.hprof <pid>
```

## Auto-Recovery

### Health Check and Auto-Recovery

```java
@Component
@Slf4j
public class AutoRecoveryService {
    
    private final RouteLocator routeLocator;
    private final RouteDefinitionLocator routeDefinitionLocator;
    
    public AutoRecoveryService(RouteLocator routeLocator, 
                             RouteDefinitionLocator routeDefinitionLocator) {
        this.routeLocator = routeLocator;
        this.routeDefinitionLocator = routeDefinitionLocator;
    }
    
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void performHealthCheck() {
        try {
            // Check route health
            routeLocator.getRoutes()
                .collectList()
                .subscribe(routes -> {
                    log.debug("Health check: {} routes active", routes.size());
                    
                    // Perform additional health checks
                    checkServiceHealth();
                    checkConfigurationHealth();
                });
                
        } catch (Exception e) {
            log.error("Health check failed", e);
            attemptRecovery();
        }
    }
    
    private void checkServiceHealth() {
        // Implementation for service health checks
    }
    
    private void checkConfigurationHealth() {
        // Implementation for configuration health checks
    }
    
    private void attemptRecovery() {
        log.info("Attempting auto-recovery...");
        
        try {
            // Refresh routes
            refreshRoutes();
            
            // Clear caches
            clearCaches();
            
            log.info("Auto-recovery completed successfully");
            
        } catch (Exception e) {
            log.error("Auto-recovery failed", e);
        }
    }
    
    private void refreshRoutes() {
        // Implementation for route refresh
    }
    
    private void clearCaches() {
        // Implementation for cache clearing
    }
}
```

---

**Related Documentation**:
- [Development Setup](./setup.md)
- [Testing Guide](./testing.md)
- [Performance Tuning](../performance-tuning.md)
- [Troubleshooting Guide](../troubleshooting.md)
