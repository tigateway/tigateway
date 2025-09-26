# TiGateway 调试指南

## 概述

本文档提供了 TiGateway 项目的调试指南，包括常见问题诊断、日志分析、性能调优、故障排除等。通过系统化的调试方法帮助开发者快速定位和解决问题。

## 1. 调试环境配置

### 1.1 开发环境调试配置

```yaml
# application-dev.yml
spring:
  profiles:
    active: dev
  
  # 日志配置
  logging:
    level:
      root: INFO
      ti.gateway: DEBUG
      org.springframework.cloud.gateway: DEBUG
      org.springframework.web.reactive: DEBUG
      reactor.netty: DEBUG
      io.kubernetes: DEBUG
    pattern:
      console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
      file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
    file:
      name: logs/tigateway-dev.log
      max-size: 100MB
      max-history: 30

# 调试配置
debug:
  enabled: true
  endpoints:
    - health
    - info
    - metrics
    - env
    - configprops
    - beans
    - mappings
    - loggers

# 管理端点配置
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
```

### 1.2 IDE 调试配置

#### 1.2.1 IntelliJ IDEA 配置

```bash
# 运行配置
VM Options: -Dspring.profiles.active=dev -Ddebug=true -Xmx2g -XX:+UseG1GC
Program Arguments: --server.port=8080 --admin.server.port=8081
Environment Variables: 
  - SPRING_PROFILES_ACTIVE=dev
  - DEBUG=true
  - LOG_LEVEL=DEBUG
```

#### 1.2.2 VS Code 配置

```json
// .vscode/launch.json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Debug TiGateway",
            "request": "launch",
            "mainClass": "ti.gateway.kubernetes.GatewayApplication",
            "projectName": "ti-gateway-kubernetes",
            "args": "--spring.profiles.active=dev",
            "vmArgs": "-Ddebug=true -Xmx2g",
            "env": {
                "SPRING_PROFILES_ACTIVE": "dev",
                "DEBUG": "true"
            }
        }
    ]
}
```

### 1.3 远程调试配置

```bash
# 启动应用时添加远程调试参数
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
     -jar tigateway-kubernetes-1.0.0.jar \
     --spring.profiles.active=dev

# 连接远程调试
# IntelliJ IDEA: Run -> Edit Configurations -> Remote JVM Debug
# Host: localhost, Port: 5005
```

## 2. 日志调试

### 2.1 日志级别配置

```java
// 动态调整日志级别
@RestController
@RequestMapping("/admin/debug")
public class DebugController {
    
    @Autowired
    private LoggingSystem loggingSystem;
    
    @PostMapping("/loggers/{loggerName}")
    public ResponseEntity<String> setLoggerLevel(
            @PathVariable String loggerName,
            @RequestParam String level) {
        
        LoggerConfiguration loggerConfig = LoggerConfiguration.builder()
                .name(loggerName)
                .configuredLevel(LogLevel.valueOf(level.toUpperCase()))
                .build();
        
        loggingSystem.setLogLevel(loggerName, LogLevel.valueOf(level.toUpperCase()));
        
        return ResponseEntity.ok("Logger level updated successfully");
    }
    
    @GetMapping("/loggers")
    public ResponseEntity<Map<String, Object>> getLoggers() {
        Map<String, Object> loggers = new HashMap<>();
        
        // 获取所有日志配置
        Collection<LoggerConfiguration> configurations = loggingSystem.getLoggerConfigurations();
        for (LoggerConfiguration config : configurations) {
            loggers.put(config.getName(), Map.of(
                "configuredLevel", config.getConfiguredLevel(),
                "effectiveLevel", config.getEffectiveLevel()
            ));
        }
        
        return ResponseEntity.ok(loggers);
    }
}
```

### 2.2 结构化日志

```java
@Slf4j
public class RouteService {
    
    public Route createRoute(RouteRequest request) {
        // 使用 MDC 添加上下文信息
        MDC.put("routeId", request.getId());
        MDC.put("operation", "createRoute");
        MDC.put("timestamp", Instant.now().toString());
        
        try {
            log.info("Starting route creation, request: {}", request);
            
            // 业务逻辑
            Route route = transformToRoute(request);
            validateRoute(route);
            Route savedRoute = routeRepository.save(route);
            
            log.info("Route created successfully, routeId: {}, uri: {}", 
                    savedRoute.getId(), savedRoute.getUri());
            
            return savedRoute;
            
        } catch (Exception e) {
            log.error("Failed to create route, routeId: {}, error: {}", 
                     request.getId(), e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
```

### 2.3 日志分析工具

```bash
# 实时查看日志
tail -f logs/tigateway-dev.log | grep ERROR

# 统计错误日志
grep "ERROR" logs/tigateway-dev.log | wc -l

# 分析日志模式
grep "Route created successfully" logs/tigateway-dev.log | \
  awk '{print $1, $2}' | sort | uniq -c

# 使用 jq 分析 JSON 日志
cat logs/tigateway-dev.log | jq '.level == "ERROR"' | jq -s 'length'

# 查找特定时间段的日志
grep "2024-09-23 10:" logs/tigateway-dev.log
```

## 3. 性能调试

### 3.1 JVM 性能监控

```bash
# 启动应用时添加性能监控参数
java -XX:+UseG1GC \
     -XX:+PrintGC \
     -XX:+PrintGCDetails \
     -XX:+PrintGCTimeStamps \
     -XX:+PrintGCApplicationStoppedTime \
     -Xloggc:logs/gc.log \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=logs/ \
     -jar tigateway-kubernetes-1.0.0.jar

# 使用 jstat 监控 GC
jstat -gc <pid> 1s

# 使用 jmap 生成堆转储
jmap -dump:format=b,file=heap.hprof <pid>

# 使用 jstack 查看线程栈
jstack <pid> > thread-dump.txt
```

### 3.2 应用性能监控

```java
@Component
public class PerformanceMonitor {
    
    private final MeterRegistry meterRegistry;
    private final Timer.Sample sample;
    
    public PerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.sample = Timer.start(meterRegistry);
    }
    
    @EventListener
    public void handleRouteRequest(RouteRequestEvent event) {
        Timer.Sample requestSample = Timer.start(meterRegistry);
        
        try {
            // 处理请求
            processRequest(event);
        } finally {
            requestSample.stop(Timer.builder("route.request.duration")
                    .tag("route.id", event.getRouteId())
                    .tag("operation", event.getOperation())
                    .register(meterRegistry));
        }
    }
    
    @Scheduled(fixedRate = 60000) // 每分钟
    public void logPerformanceMetrics() {
        // 获取性能指标
        double avgResponseTime = meterRegistry.get("route.request.duration")
                .timer()
                .mean(TimeUnit.MILLISECONDS);
        
        long requestCount = meterRegistry.get("route.request.count")
                .counter()
                .count();
        
        log.info("Performance Metrics - Avg Response Time: {}ms, Request Count: {}", 
                avgResponseTime, requestCount);
    }
}
```

### 3.3 内存泄漏检测

```java
@Component
public class MemoryLeakDetector {
    
    private final MemoryMXBean memoryBean;
    private final List<MemoryUsage> memoryHistory = new ArrayList<>();
    
    public MemoryLeakDetector() {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
    }
    
    @Scheduled(fixedRate = 30000) // 每30秒
    public void checkMemoryUsage() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        memoryHistory.add(heapUsage);
        
        // 保持最近100个记录
        if (memoryHistory.size() > 100) {
            memoryHistory.remove(0);
        }
        
        // 检测内存泄漏
        if (memoryHistory.size() >= 10) {
            long currentUsed = heapUsage.getUsed();
            long averageUsed = memoryHistory.stream()
                    .mapToLong(MemoryUsage::getUsed)
                    .sum() / memoryHistory.size();
            
            // 如果当前使用量比平均值高20%，可能存在内存泄漏
            if (currentUsed > averageUsed * 1.2) {
                log.warn("Potential memory leak detected. Current: {}, Average: {}", 
                        currentUsed, averageUsed);
                
                // 生成堆转储
                generateHeapDump();
            }
        }
        
        log.debug("Memory usage - Used: {}MB, Max: {}MB, Usage: {}%", 
                heapUsage.getUsed() / 1024 / 1024,
                heapUsage.getMax() / 1024 / 1024,
                (double) heapUsage.getUsed() / heapUsage.getMax() * 100);
    }
    
    private void generateHeapDump() {
        try {
            String fileName = "heap-dump-" + System.currentTimeMillis() + ".hprof";
            String filePath = "logs/" + fileName;
            
            // 使用 HotSpotDiagnosticMXBean 生成堆转储
            HotSpotDiagnosticMXBean diagnosticBean = ManagementFactory
                    .newPlatformMXBeanProxy(
                            ManagementFactory.getPlatformMBeanServer(),
                            "com.sun.management:type=HotSpotDiagnostic",
                            HotSpotDiagnosticMXBean.class);
            
            diagnosticBean.dumpHeap(filePath, true);
            log.info("Heap dump generated: {}", filePath);
            
        } catch (Exception e) {
            log.error("Failed to generate heap dump", e);
        }
    }
}
```

## 4. 网络调试

### 4.1 HTTP 请求调试

```java
@Component
public class HttpRequestDebugger {
    
    private final WebClient webClient;
    
    public HttpRequestDebugger(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }
    
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> 
                    log.debug("Request Header: {} = {}", name, values));
            return Mono.just(clientRequest);
        });
    }
    
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("Response: {}", clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) -> 
                    log.debug("Response Header: {} = {}", name, values));
            return Mono.just(clientResponse);
        });
    }
    
    public Mono<String> makeRequest(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Request successful, response length: {}", 
                        response.length()))
                .doOnError(error -> log.error("Request failed", error));
    }
}
```

### 4.2 网络连接调试

```java
@Component
public class NetworkConnectionDebugger {
    
    @EventListener
    public void handleConnectionEvent(ConnectionEvent event) {
        log.debug("Connection event: {} - {}", event.getType(), event.getConnectionId());
        
        switch (event.getType()) {
            case CONNECTED:
                log.info("New connection established: {}", event.getConnectionId());
                break;
            case DISCONNECTED:
                log.info("Connection closed: {}", event.getConnectionId());
                break;
            case ERROR:
                log.error("Connection error: {} - {}", event.getConnectionId(), event.getError());
                break;
        }
    }
    
    @Scheduled(fixedRate = 60000) // 每分钟
    public void logConnectionStats() {
        // 获取连接统计信息
        ConnectionPoolStats stats = getConnectionPoolStats();
        
        log.info("Connection Pool Stats - Active: {}, Idle: {}, Total: {}", 
                stats.getActiveConnections(),
                stats.getIdleConnections(),
                stats.getTotalConnections());
    }
    
    private ConnectionPoolStats getConnectionPoolStats() {
        // 实现获取连接池统计信息的逻辑
        return new ConnectionPoolStats();
    }
}
```

## 5. 数据库调试

### 5.1 SQL 查询调试

```yaml
# application-dev.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        type: trace
        use_sql_comments: true
        jdbc:
          batch_size: 20
          batch_versioned_data: true
        order_inserts: true
        order_updates: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.hibernate.orm.jdbc.bind: TRACE
```

### 5.2 数据库连接池监控

```java
@Component
public class DatabaseConnectionPoolMonitor {
    
    @Autowired
    private DataSource dataSource;
    
    @Scheduled(fixedRate = 30000) // 每30秒
    public void monitorConnectionPool() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
            
            log.info("Database Connection Pool Stats:");
            log.info("  Active Connections: {}", poolBean.getActiveConnections());
            log.info("  Idle Connections: {}", poolBean.getIdleConnections());
            log.info("  Total Connections: {}", poolBean.getTotalConnections());
            log.info("  Threads Awaiting Connection: {}", poolBean.getThreadsAwaitingConnection());
            log.info("  Connection Timeout: {}", poolBean.getConnectionTimeout());
        }
    }
    
    @EventListener
    public void handleSlowQuery(SlowQueryEvent event) {
        log.warn("Slow query detected: {}ms - {}", 
                event.getExecutionTime(), event.getSql());
    }
}
```

## 6. 缓存调试

### 6.1 Redis 缓存调试

```java
@Component
public class RedisCacheDebugger {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Scheduled(fixedRate = 60000) // 每分钟
    public void monitorRedisCache() {
        try {
            // 获取 Redis 信息
            Properties info = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .info();
            
            log.info("Redis Cache Stats:");
            log.info("  Used Memory: {}", info.getProperty("used_memory_human"));
            log.info("  Connected Clients: {}", info.getProperty("connected_clients"));
            log.info("  Total Commands Processed: {}", info.getProperty("total_commands_processed"));
            log.info("  Keyspace Hits: {}", info.getProperty("keyspace_hits"));
            log.info("  Keyspace Misses: {}", info.getProperty("keyspace_misses"));
            
            // 计算命中率
            long hits = Long.parseLong(info.getProperty("keyspace_hits", "0"));
            long misses = Long.parseLong(info.getProperty("keyspace_misses", "0"));
            double hitRate = hits + misses > 0 ? (double) hits / (hits + misses) * 100 : 0;
            
            log.info("  Cache Hit Rate: {:.2f}%", hitRate);
            
        } catch (Exception e) {
            log.error("Failed to monitor Redis cache", e);
        }
    }
    
    @EventListener
    public void handleCacheEvent(CacheEvent event) {
        log.debug("Cache event: {} - Key: {}, Value: {}", 
                event.getType(), event.getKey(), event.getValue());
    }
}
```

### 6.2 本地缓存调试

```java
@Component
public class LocalCacheDebugger {
    
    @Autowired
    private CacheManager cacheManager;
    
    @Scheduled(fixedRate = 60000) // 每分钟
    public void monitorLocalCache() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache) {
                CaffeineCache caffeineCache = (CaffeineCache) cache;
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = 
                        caffeineCache.getNativeCache();
                
                com.github.benmanes.caffeine.cache.stats.CacheStats stats = 
                        nativeCache.stats();
                
                log.info("Local Cache '{}' Stats:", cacheName);
                log.info("  Size: {}", nativeCache.estimatedSize());
                log.info("  Hit Rate: {:.2f}%", stats.hitRate() * 100);
                log.info("  Miss Rate: {:.2f}%", stats.missRate() * 100);
                log.info("  Eviction Count: {}", stats.evictionCount());
            }
        });
    }
}
```

## 7. 故障排除

### 7.1 常见问题诊断

```java
@Component
public class HealthDiagnostic {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private RouteService routeService;
    
    @GetMapping("/admin/diagnostic")
    public ResponseEntity<Map<String, Object>> runDiagnostic() {
        Map<String, Object> diagnostic = new HashMap<>();
        
        // 检查应用健康状态
        diagnostic.put("applicationHealth", checkApplicationHealth());
        
        // 检查数据库连接
        diagnostic.put("databaseHealth", checkDatabaseHealth());
        
        // 检查 Redis 连接
        diagnostic.put("redisHealth", checkRedisHealth());
        
        // 检查路由状态
        diagnostic.put("routeHealth", checkRouteHealth());
        
        // 检查内存使用
        diagnostic.put("memoryHealth", checkMemoryHealth());
        
        // 检查线程状态
        diagnostic.put("threadHealth", checkThreadHealth());
        
        return ResponseEntity.ok(diagnostic);
    }
    
    private Map<String, Object> checkApplicationHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 检查 Spring 上下文
            health.put("springContext", "OK");
            
            // 检查关键 Bean
            String[] beanNames = applicationContext.getBeanDefinitionNames();
            health.put("beanCount", beanNames.length);
            
            // 检查配置属性
            Environment env = applicationContext.getEnvironment();
            health.put("activeProfiles", env.getActiveProfiles());
            
        } catch (Exception e) {
            health.put("springContext", "ERROR: " + e.getMessage());
        }
        
        return health;
    }
    
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 检查数据库连接
            DataSource dataSource = applicationContext.getBean(DataSource.class);
            try (Connection connection = dataSource.getConnection()) {
                health.put("connection", "OK");
                health.put("url", connection.getMetaData().getURL());
            }
            
        } catch (Exception e) {
            health.put("connection", "ERROR: " + e.getMessage());
        }
        
        return health;
    }
    
    private Map<String, Object> checkMemoryHealth() {
        Map<String, Object> health = new HashMap<>();
        
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        health.put("usedMemory", heapUsage.getUsed() / 1024 / 1024 + "MB");
        health.put("maxMemory", heapUsage.getMax() / 1024 / 1024 + "MB");
        health.put("usagePercentage", 
                (double) heapUsage.getUsed() / heapUsage.getMax() * 100);
        
        // 检查内存使用率
        double usagePercentage = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
        if (usagePercentage > 90) {
            health.put("status", "WARNING: High memory usage");
        } else if (usagePercentage > 80) {
            health.put("status", "CAUTION: Memory usage is high");
        } else {
            health.put("status", "OK");
        }
        
        return health;
    }
}
```

### 7.2 自动故障恢复

```java
@Component
public class AutoRecoveryService {
    
    @Autowired
    private RouteService routeService;
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    @Scheduled(fixedRate = 300000) // 每5分钟
    public void checkAndRecover() {
        log.info("Starting auto-recovery check");
        
        // 检查断路器状态
        checkCircuitBreakers();
        
        // 检查路由状态
        checkRouteHealth();
        
        // 检查连接池状态
        checkConnectionPools();
        
        log.info("Auto-recovery check completed");
    }
    
    private void checkCircuitBreakers() {
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            CircuitBreaker.State state = circuitBreaker.getState();
            if (state == CircuitBreaker.State.OPEN) {
                log.warn("Circuit breaker '{}' is OPEN, attempting recovery", 
                        circuitBreaker.getName());
                
                // 尝试重置断路器
                circuitBreaker.transitionToHalfOpenState();
            }
        });
    }
    
    private void checkRouteHealth() {
        try {
            List<Route> routes = routeService.getAllRoutes();
            long unhealthyRoutes = routes.stream()
                    .filter(route -> route.getStatus() == RouteStatus.ERROR)
                    .count();
            
            if (unhealthyRoutes > 0) {
                log.warn("Found {} unhealthy routes, attempting recovery", unhealthyRoutes);
                
                // 尝试恢复不健康的路由
                routes.stream()
                        .filter(route -> route.getStatus() == RouteStatus.ERROR)
                        .forEach(this::attemptRouteRecovery);
            }
            
        } catch (Exception e) {
            log.error("Failed to check route health", e);
        }
    }
    
    private void attemptRouteRecovery(Route route) {
        try {
            log.info("Attempting to recover route: {}", route.getId());
            
            // 重新验证路由
            routeService.validateRoute(route);
            
            // 更新路由状态
            route.setStatus(RouteStatus.ACTIVE);
            routeService.updateRoute(route);
            
            log.info("Successfully recovered route: {}", route.getId());
            
        } catch (Exception e) {
            log.error("Failed to recover route: {}", route.getId(), e);
        }
    }
}
```

## 8. 调试工具和技巧

### 8.1 调试端点

```java
@RestController
@RequestMapping("/admin/debug")
public class DebugEndpoint {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @GetMapping("/beans")
    public ResponseEntity<Map<String, Object>> getBeans() {
        Map<String, Object> beans = new HashMap<>();
        
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);
                beans.put(beanName, bean.getClass().getName());
            } catch (Exception e) {
                beans.put(beanName, "ERROR: " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(beans);
    }
    
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        
        Environment env = applicationContext.getEnvironment();
        MutablePropertySources propertySources = env.getPropertySources();
        
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> enumerablePropertySource = 
                        (EnumerablePropertySource<?>) propertySource;
                
                Map<String, Object> properties = new HashMap<>();
                for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                    properties.put(propertyName, enumerablePropertySource.getProperty(propertyName));
                }
                
                config.put(propertySource.getName(), properties);
            }
        }
        
        return ResponseEntity.ok(config);
    }
    
    @PostMapping("/gc")
    public ResponseEntity<String> triggerGC() {
        System.gc();
        return ResponseEntity.ok("GC triggered");
    }
    
    @GetMapping("/threads")
    public ResponseEntity<List<Map<String, Object>>> getThreads() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadBean.getAllThreadIds());
        
        List<Map<String, Object>> threads = Arrays.stream(threadInfos)
                .map(threadInfo -> Map.of(
                        "id", threadInfo.getThreadId(),
                        "name", threadInfo.getThreadName(),
                        "state", threadInfo.getThreadState().toString(),
                        "cpuTime", threadBean.getThreadCpuTime(threadInfo.getThreadId())
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(threads);
    }
}
```

### 8.2 调试脚本

```bash
#!/bin/bash
# debug.sh - TiGateway 调试脚本

echo "TiGateway Debug Script"
echo "======================"

# 检查应用状态
echo "1. Checking application status..."
curl -s http://localhost:8080/actuator/health | jq '.'

# 检查路由状态
echo -e "\n2. Checking route status..."
curl -s http://localhost:8080/actuator/gateway/routes | jq '.'

# 检查内存使用
echo -e "\n3. Checking memory usage..."
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq '.'

# 检查 GC 状态
echo -e "\n4. Checking GC status..."
curl -s http://localhost:8080/actuator/metrics/jvm.gc.pause | jq '.'

# 检查线程状态
echo -e "\n5. Checking thread status..."
curl -s http://localhost:8080/actuator/metrics/jvm.threads.live | jq '.'

# 检查日志级别
echo -e "\n6. Checking log levels..."
curl -s http://localhost:8080/actuator/loggers | jq '.'

echo -e "\nDebug information collected successfully!"
```

### 8.3 性能分析工具

```java
@Component
public class PerformanceProfiler {
    
    private final Map<String, Long> methodExecutionTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> methodCallCounts = new ConcurrentHashMap<>();
    
    @EventListener
    public void handleMethodExecution(MethodExecutionEvent event) {
        String methodName = event.getMethodName();
        long executionTime = event.getExecutionTime();
        
        // 记录执行时间
        methodExecutionTimes.merge(methodName, executionTime, Long::max);
        
        // 记录调用次数
        methodCallCounts.computeIfAbsent(methodName, k -> new AtomicInteger(0))
                .incrementAndGet();
    }
    
    @Scheduled(fixedRate = 60000) // 每分钟
    public void logPerformanceProfile() {
        log.info("Performance Profile:");
        
        methodExecutionTimes.forEach((method, maxTime) -> {
            int callCount = methodCallCounts.getOrDefault(method, new AtomicInteger(0)).get();
            double avgTime = (double) maxTime / callCount;
            
            log.info("  {} - Max: {}ms, Avg: {:.2f}ms, Calls: {}", 
                    method, maxTime, avgTime, callCount);
        });
    }
    
    @GetMapping("/admin/debug/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceProfile() {
        Map<String, Object> profile = new HashMap<>();
        
        methodExecutionTimes.forEach((method, maxTime) -> {
            int callCount = methodCallCounts.getOrDefault(method, new AtomicInteger(0)).get();
            double avgTime = (double) maxTime / callCount;
            
            profile.put(method, Map.of(
                    "maxTime", maxTime,
                    "avgTime", avgTime,
                    "callCount", callCount
            ));
        });
        
        return ResponseEntity.ok(profile);
    }
}
```

## 9. 生产环境调试

### 9.1 生产环境监控

```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
    metrics:
      enabled: true
    prometheus:
      enabled: true

logging:
  level:
    root: WARN
    ti.gateway: INFO
  file:
    name: /app/logs/tigateway.log
    max-size: 100MB
    max-history: 30
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
```

### 9.2 生产环境调试策略

```java
@Component
@Profile("prod")
public class ProductionDebugger {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 300000) // 每5分钟
    public void monitorProductionHealth() {
        // 检查关键指标
        checkCriticalMetrics();
        
        // 检查错误率
        checkErrorRate();
        
        // 检查响应时间
        checkResponseTime();
        
        // 检查资源使用
        checkResourceUsage();
    }
    
    private void checkCriticalMetrics() {
        // 检查请求成功率
        double successRate = getSuccessRate();
        if (successRate < 0.95) {
            log.error("Low success rate detected: {:.2f}%", successRate * 100);
            // 发送告警
            sendAlert("Low success rate", String.format("Success rate: %.2f%%", successRate * 100));
        }
        
        // 检查错误率
        double errorRate = getErrorRate();
        if (errorRate > 0.05) {
            log.error("High error rate detected: {:.2f}%", errorRate * 100);
            // 发送告警
            sendAlert("High error rate", String.format("Error rate: %.2f%%", errorRate * 100));
        }
    }
    
    private void sendAlert(String title, String message) {
        // 实现告警发送逻辑
        log.warn("ALERT: {} - {}", title, message);
    }
}
```

---

**相关文档**:
- [编码规范](./coding-standards.md)
- [测试指南](./testing.md)
- [开发环境搭建](./setup.md)
- [自定义组件开发](./custom-components.md)
