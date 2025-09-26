# Performance Tuning Guide

This comprehensive performance tuning guide helps you optimize TiGateway for maximum performance and efficiency. It covers JVM tuning, application optimization, network optimization, and monitoring strategies.

## Table of Contents

1. [Performance Overview](#1-performance-overview)
2. [JVM Tuning](#2-jvm-tuning)
3. [Application Optimization](#3-application-optimization)
4. [Network Optimization](#4-network-optimization)
5. [Database Optimization](#5-database-optimization)
6. [Caching Strategies](#6-caching-strategies)
7. [Load Balancing](#7-load-balancing)
8. [Monitoring and Profiling](#8-monitoring-and-profiling)
9. [Benchmarking](#9-benchmarking)
10. [Best Practices](#10-best-practices)

## 1. Performance Overview

### 1.1 Performance Metrics

Key performance metrics to monitor:

- **Throughput**: Requests per second (RPS)
- **Latency**: Response time (P50, P95, P99)
- **Resource Usage**: CPU, memory, disk, network
- **Error Rate**: Percentage of failed requests
- **Concurrency**: Number of concurrent connections

### 1.2 Performance Bottlenecks

Common performance bottlenecks:

- **JVM Garbage Collection**: GC pauses affecting response times
- **Network I/O**: Slow network connections to backend services
- **Database Connections**: Connection pool exhaustion
- **Memory Usage**: High memory consumption leading to GC pressure
- **CPU Usage**: High CPU utilization limiting throughput

### 1.3 Performance Targets

Typical performance targets:

- **Response Time**: P95 < 100ms, P99 < 500ms
- **Throughput**: > 10,000 RPS
- **Availability**: 99.9% uptime
- **Error Rate**: < 0.1%
- **Resource Usage**: < 80% CPU, < 80% memory

## 2. JVM Tuning

### 2.1 Heap Size Configuration

#### Initial Configuration
```bash
# Basic heap configuration
java -Xms1g -Xmx4g -jar tigateway.jar

# For production environments
java -Xms2g -Xmx8g -jar tigateway.jar

# For high-throughput scenarios
java -Xms4g -Xmx16g -jar tigateway.jar
```

#### Heap Size Guidelines
- **Development**: 1-2GB heap
- **Staging**: 2-4GB heap
- **Production**: 4-16GB heap (depending on load)
- **High-load**: 16-32GB heap

### 2.2 Garbage Collection Tuning

#### G1GC Configuration (Recommended)
```bash
# Basic G1GC configuration
java -XX:+UseG1GC -jar tigateway.jar

# Optimized G1GC configuration
java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:G1HeapRegionSize=16m \
     -XX:G1NewSizePercent=30 \
     -XX:G1MaxNewSizePercent=40 \
     -XX:G1MixedGCCountTarget=8 \
     -XX:G1MixedGCLiveThresholdPercent=85 \
     -jar tigateway.jar
```

#### Parallel GC Configuration
```bash
# Parallel GC for high-throughput scenarios
java -XX:+UseParallelGC \
     -XX:ParallelGCThreads=8 \
     -XX:MaxGCPauseMillis=100 \
     -jar tigateway.jar
```

#### ZGC Configuration (Java 11+)
```bash
# ZGC for low-latency scenarios
java -XX:+UnlockExperimentalVMOptions \
     -XX:+UseZGC \
     -XX:+UnlockDiagnosticVMOptions \
     -XX:+LogVMOutput \
     -jar tigateway.jar
```

### 2.3 JVM Monitoring

#### GC Logging
```bash
# Enable GC logging
java -XX:+PrintGC \
     -XX:+PrintGCDetails \
     -XX:+PrintGCTimeStamps \
     -XX:+PrintGCApplicationStoppedTime \
     -Xloggc:gc.log \
     -jar tigateway.jar
```

#### JVM Metrics
```bash
# Check JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/jvm.gc.pause
curl http://localhost:8080/actuator/metrics/jvm.threads.live
```

## 3. Application Optimization

### 3.1 HTTP Client Configuration

#### Connection Pool Tuning
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

#### HTTP/2 Configuration
```yaml
server:
  http2:
    enabled: true
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
    min-response-size: 1024
```

### 3.2 Thread Pool Configuration

#### Tomcat Thread Pool
```yaml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
    connection-timeout: 20000
    max-connections: 8192
    accept-count: 100
```

#### Netty Configuration
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 1000
          max-idle-time: 60s
          max-life-time: 120s
```

### 3.3 Filter Chain Optimization

#### Filter Order Optimization
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: optimized-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            # Order filters by performance impact
            - StripPrefix=2  # Fast operation
            - AddRequestHeader=X-Service,user-service  # Fast operation
            - CircuitBreaker=user-service-cb,forward:/fallback  # Slower operation
            - RequestRateLimiter=user-service-rl  # Slower operation
```

#### Custom Filter Optimization
```java
@Component
public class OptimizedFilter implements GatewayFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Fast path for common cases
        if (isCommonCase(exchange)) {
            return chain.filter(exchange);
        }
        
        // Slower path for complex cases
        return processComplexCase(exchange)
                .then(chain.filter(exchange));
    }
    
    @Override
    public int getOrder() {
        return -100; // High priority for fast filters
    }
}
```

## 4. Network Optimization

### 4.1 Connection Pooling

#### HTTP Connection Pool
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
```

#### Database Connection Pool
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

### 4.2 Network Configuration

#### TCP Tuning
```bash
# TCP buffer sizes
echo 'net.core.rmem_max = 16777216' >> /etc/sysctl.conf
echo 'net.core.wmem_max = 16777216' >> /etc/sysctl.conf
echo 'net.ipv4.tcp_rmem = 4096 87380 16777216' >> /etc/sysctl.conf
echo 'net.ipv4.tcp_wmem = 4096 65536 16777216' >> /etc/sysctl.conf

# Apply changes
sysctl -p
```

#### Keep-Alive Configuration
```yaml
server:
  tomcat:
    connection-timeout: 20000
    keep-alive-timeout: 60000
    max-keep-alive-requests: 100
```

### 4.3 Load Balancing Optimization

#### Load Balancing Strategy
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: optimized-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - LoadBalancer=LEAST_CONNECTIONS  # Use least connections for better distribution
```

#### Health Check Configuration
```yaml
spring:
  cloud:
    loadbalancer:
      health-check:
        enabled: true
        path: /actuator/health
        interval: 10s
        timeout: 5s
```

## 5. Database Optimization

### 5.1 Connection Pool Optimization

#### HikariCP Configuration
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
      validation-timeout: 5000
      connection-test-query: SELECT 1
```

#### Connection Pool Monitoring
```java
@Component
public class ConnectionPoolMonitor {
    
    private final HikariDataSource dataSource;
    
    public ConnectionPoolMonitor(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
        
        // Monitor connection pool metrics
        Timer.scheduleAtFixedRate(() -> {
            log.info("Active connections: {}", poolBean.getActiveConnections());
            log.info("Idle connections: {}", poolBean.getIdleConnections());
            log.info("Total connections: {}", poolBean.getTotalConnections());
            log.info("Threads awaiting connection: {}", poolBean.getThreadsAwaitingConnection());
        }, 0, 30, TimeUnit.SECONDS);
    }
}
```

### 5.2 Query Optimization

#### Database Indexing
```sql
-- Create indexes for frequently queried columns
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_created_at ON users(created_at);
CREATE INDEX idx_route_path ON routes(path);

-- Composite indexes for complex queries
CREATE INDEX idx_user_status_created ON users(status, created_at);
```

#### Query Monitoring
```yaml
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true
        session:
          events:
            log:
              LOG_QUERIES_SLOWER_THAN_MS: 100
```

## 6. Caching Strategies

### 6.1 Application-Level Caching

#### Redis Configuration
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD}
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 2000ms
```

#### Cache Configuration
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

#### Cache Implementation
```java
@Service
public class RouteCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RouteCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Cacheable(value = "routes", key = "#routeId")
    public Route getRoute(String routeId) {
        // Expensive operation to get route
        return routeRepository.findById(routeId);
    }
    
    @CacheEvict(value = "routes", key = "#routeId")
    public void evictRoute(String routeId) {
        // Route updated, evict cache
    }
    
    @CacheEvict(value = "routes", allEntries = true)
    public void evictAllRoutes() {
        // All routes updated, evict all cache
    }
}
```

### 6.2 HTTP Caching

#### Response Caching
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: cached-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - name: CacheResponse
              args:
                cache-name: user-cache
                ttl: 300s
```

#### Cache Headers
```java
@Component
public class CacheHeaderFilter implements GatewayFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    
                    // Set cache headers
                    response.getHeaders().add("Cache-Control", "public, max-age=300");
                    response.getHeaders().add("ETag", generateETag(exchange));
                }));
    }
}
```

## 7. Load Balancing

### 7.1 Load Balancing Strategies

#### Round Robin (Default)
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
            - LoadBalancer=ROUND_ROBIN
```

#### Least Connections
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
            - LoadBalancer=LEAST_CONNECTIONS
```

#### Weighted Round Robin
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

### 7.2 Health Checks

#### Health Check Configuration
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

#### Custom Health Check
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

## 8. Monitoring and Profiling

### 8.1 Performance Metrics

#### Application Metrics
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
        tigateway.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
        tigateway.requests: 0.5, 0.95, 0.99
```

#### Custom Metrics
```java
@Component
public class PerformanceMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Timer requestTimer;
    private final Counter requestCounter;
    private final Gauge activeConnections;
    
    public PerformanceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.requestTimer = Timer.builder("tigateway.request.duration")
                .description("Request duration")
                .register(meterRegistry);
        
        this.requestCounter = Counter.builder("tigateway.requests.total")
                .description("Total requests")
                .register(meterRegistry);
        
        this.activeConnections = Gauge.builder("tigateway.connections.active")
                .description("Active connections")
                .register(meterRegistry, this, PerformanceMetrics::getActiveConnections);
    }
    
    public void recordRequest(Duration duration, String route) {
        requestTimer.record(duration, Tags.of("route", route));
        requestCounter.increment(Tags.of("route", route));
    }
    
    private double getActiveConnections() {
        // Implement logic to get active connections
        return 0.0;
    }
}
```

### 8.2 Profiling

#### JVM Profiling
```bash
# Enable JVM profiling
java -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=profile.jfr \
     -jar tigateway.jar

# Analyze profile
jfr print profile.jfr
```

#### Application Profiling
```java
@Component
public class PerformanceProfiler {
    
    private final MeterRegistry meterRegistry;
    
    public PerformanceProfiler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public <T> Mono<T> profile(String operation, Mono<T> operationMono) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        return operationMono
                .doOnSuccess(result -> sample.stop(Timer.builder("operation.duration")
                        .tag("operation", operation)
                        .tag("status", "success")
                        .register(meterRegistry)))
                .doOnError(error -> sample.stop(Timer.builder("operation.duration")
                        .tag("operation", operation)
                        .tag("status", "error")
                        .register(meterRegistry)));
    }
}
```

## 9. Benchmarking

### 9.1 Load Testing

#### Apache Bench
```bash
# Basic load test
ab -n 10000 -c 100 http://localhost:8080/api/users/123

# Load test with keep-alive
ab -n 10000 -c 100 -k http://localhost:8080/api/users/123

# Load test with specific headers
ab -n 10000 -c 100 -H "Authorization: Bearer token" http://localhost:8080/api/users/123
```

#### JMeter
```xml
<!-- JMeter test plan -->
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan testname="TiGateway Load Test">
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup testname="Thread Group">
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <stringProp name="ThreadGroup.duration">300</stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
      </ThreadGroup>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

#### Gatling
```scala
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class TiGatewayLoadTest extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .userAgentHeader("Gatling Load Test")

  val scn = scenario("TiGateway Load Test")
    .exec(http("Get Users")
      .get("/api/users/123")
      .check(status.is(200)))

  setUp(
    scn.inject(
      rampUsers(100) during (10 seconds),
      constantUsers(100) during (300 seconds)
    )
  ).protocols(httpProtocol)
}
```

### 9.2 Performance Testing

#### Response Time Testing
```bash
# Test response times
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/api/users/123

# curl-format.txt content:
#      time_namelookup:  %{time_namelookup}\n
#         time_connect:  %{time_connect}\n
#      time_appconnect:  %{time_appconnect}\n
#     time_pretransfer:  %{time_pretransfer}\n
#        time_redirect:  %{time_redirect}\n
#   time_starttransfer:  %{time_starttransfer}\n
#                      ----------\n
#           time_total:  %{time_total}\n
```

#### Throughput Testing
```bash
# Test throughput
for i in {1..1000}; do
  curl -s http://localhost:8080/api/users/123 > /dev/null &
done
wait
```

## 10. Best Practices

### 10.1 Performance Optimization

#### General Principles
- **Measure first**: Always measure before optimizing
- **Profile regularly**: Use profiling tools to identify bottlenecks
- **Optimize incrementally**: Make small, measurable improvements
- **Test thoroughly**: Test performance changes thoroughly
- **Monitor continuously**: Monitor performance continuously

#### JVM Optimization
- **Use appropriate GC**: Choose GC based on requirements
- **Tune heap size**: Set appropriate heap size
- **Monitor GC**: Monitor GC performance
- **Use profiling**: Use profiling tools for optimization
- **Test changes**: Test JVM changes thoroughly

#### Application Optimization
- **Optimize filters**: Order filters by performance impact
- **Use caching**: Implement appropriate caching strategies
- **Optimize I/O**: Use async I/O where possible
- **Minimize allocations**: Reduce object allocations
- **Use connection pooling**: Implement connection pooling

### 10.2 Monitoring

#### Key Metrics
- **Response time**: Monitor P50, P95, P99 response times
- **Throughput**: Monitor requests per second
- **Error rate**: Monitor error rates
- **Resource usage**: Monitor CPU, memory, disk usage
- **GC performance**: Monitor GC pause times

#### Alerting
- **Set thresholds**: Set appropriate performance thresholds
- **Monitor trends**: Monitor performance trends
- **Alert on anomalies**: Alert on performance anomalies
- **Escalate issues**: Escalate performance issues
- **Review regularly**: Review performance regularly

### 10.3 Capacity Planning

#### Load Testing
- **Test regularly**: Conduct regular load tests
- **Test scenarios**: Test various load scenarios
- **Test limits**: Test system limits
- **Document results**: Document test results
- **Plan capacity**: Plan capacity based on results

#### Scaling
- **Horizontal scaling**: Scale horizontally when possible
- **Vertical scaling**: Scale vertically when needed
- **Auto-scaling**: Implement auto-scaling
- **Load balancing**: Use load balancing
- **Monitor scaling**: Monitor scaling effectiveness

## Next Steps

After optimizing performance:

1. **[Monitoring Setup](../monitoring-and-metrics.md)** - Set up performance monitoring
2. **[Security Best Practices](../security-best-practices.md)** - Implement security measures
3. **[Troubleshooting Guide](../troubleshooting.md)** - Common performance issues
4. **[Production Deployment](../deployment/kubernetes.md)** - Deploy optimized version

---

**Ready to monitor performance?** Check out our [Monitoring Setup](../monitoring-and-metrics.md) guide for comprehensive performance monitoring.
