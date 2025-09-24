# TiGateway 高级配置示例

## 概述

本文档提供了 TiGateway 的高级配置示例，包括复杂路由配置、高级过滤器、性能优化、安全配置等企业级场景。

## 复杂路由配置

### 1. 多条件路由匹配
```yaml
spring:
  cloud:
    gateway:
      routes:
        # 基于多个条件的复杂路由
        - id: complex-route
          uri: lb://backend-service
          predicates:
            # 路径匹配
            - Path=/api/v2/**
            # 方法匹配
            - Method=GET,POST
            # 请求头匹配
            - Header=X-API-Version, v2
            # 查询参数匹配
            - Query=source, mobile
            # 时间范围匹配
            - Between=2024-01-01T00:00:00+08:00, 2024-12-31T23:59:59+08:00
            # 权重匹配
            - Weight=group1, 80
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Route-Type, complex
            - AddResponseHeader=X-Processed-By, TiGateway
```

### 2. 动态路由配置
```java
@Configuration
public class DynamicRouteConfiguration {
    
    @Bean
    public RouteLocator dynamicRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // 基于数据库配置的动态路由
            .route("dynamic-route", r -> r
                .path("/api/dynamic/**")
                .filters(f -> f
                    .filter(dynamicFilter())
                    .circuitBreaker(config -> config
                        .setName("dynamic-circuit")
                        .setFallbackUri("forward:/fallback")
                    )
                )
                .uri("lb://dynamic-service")
            )
            // 基于环境变量的路由
            .route("env-route", r -> r
                .path("/api/env/**")
                .filters(f -> f
                    .filter(environmentFilter())
                )
                .uri("${BACKEND_SERVICE_URL:lb://default-service}")
            )
            .build();
    }
    
    @Bean
    public GatewayFilter dynamicFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // 根据请求头动态选择目标服务
            String serviceType = request.getHeaders().getFirst("X-Service-Type");
            String targetUri = determineTargetUri(serviceType);
            
            // 修改请求 URI
            ServerHttpRequest modifiedRequest = request.mutate()
                .uri(URI.create(targetUri))
                .build();
                
            return chain.filter(exchange.mutate()
                .request(modifiedRequest)
                .build());
        };
    }
}
```

### 3. 路由分组和标签
```yaml
spring:
  cloud:
    gateway:
      routes:
        # 用户服务路由组
        - id: user-service-read
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
            - Method=GET
            - Header=X-Service-Group, user
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Operation, read
            - name: Retry
              args:
                retries: 3
                methods: GET
                backoff:
                  firstBackoff: 50ms
                  maxBackoff: 500ms
                  factor: 2
        
        - id: user-service-write
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
            - Method=POST,PUT,DELETE
            - Header=X-Service-Group, user
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Operation, write
            - name: CircuitBreaker
              args:
                name: user-service-cb
                fallbackUri: forward:/fallback/user
```

## 高级过滤器配置

### 1. 自定义过滤器
```java
@Component
public class CustomAuthenticationFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 跳过不需要认证的路径
        if (shouldSkipAuth(request.getPath().value())) {
            return chain.filter(exchange);
        }
        
        // 提取认证信息
        String token = extractToken(request);
        if (token == null) {
            return handleUnauthorized(exchange);
        }
        
        // 验证 Token
        return validateToken(token)
            .flatMap(user -> {
                // 设置用户上下文
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", user.getId())
                    .header("X-User-Roles", String.join(",", user.getRoles()))
                    .build();
                    
                return chain.filter(exchange.mutate()
                    .request(modifiedRequest)
                    .build());
            })
            .onErrorResume(e -> handleAuthenticationError(exchange, e));
    }
    
    @Override
    public int getOrder() {
        return -100; // 高优先级
    }
}
```

### 2. 请求/响应转换过滤器
```java
@Component
public class RequestResponseTransformFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 请求转换
        if (needsRequestTransform(request)) {
            return transformRequest(exchange)
                .flatMap(transformedExchange -> 
                    chain.filter(transformedExchange)
                        .then(transformResponse(transformedExchange))
                );
        }
        
        return chain.filter(exchange)
            .then(transformResponse(exchange));
    }
    
    private Mono<ServerWebExchange> transformRequest(ServerWebExchange exchange) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
            .flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                
                // 执行请求转换逻辑
                String transformedBody = transformRequestBody(new String(bytes));
                
                // 创建新的请求
                ServerHttpRequest newRequest = exchange.getRequest().mutate()
                    .body(transformedBody)
                    .build();
                    
                return Mono.just(exchange.mutate().request(newRequest).build());
            });
    }
    
    private Mono<Void> transformResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        
        return DataBufferUtils.join(response.bufferFactory().allocateBuffer())
            .flatMap(dataBuffer -> {
                // 执行响应转换逻辑
                String transformedResponse = transformResponseBody(dataBuffer);
                
                // 写入转换后的响应
                response.getHeaders().setContentLength(transformedResponse.length());
                return response.writeWith(Mono.just(
                    response.bufferFactory().wrap(transformedResponse.getBytes())
                ));
            });
    }
}
```

### 3. 缓存过滤器
```java
@Component
public class CacheFilter implements GlobalFilter, Ordered {
    
    @Autowired
    private CacheManager cacheManager;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 检查是否应该缓存
        if (!shouldCache(request)) {
            return chain.filter(exchange);
        }
        
        String cacheKey = generateCacheKey(request);
        Cache cache = cacheManager.getCache("response-cache");
        
        // 尝试从缓存获取
        Cache.ValueWrapper cached = cache.get(cacheKey);
        if (cached != null) {
            return writeCachedResponse(exchange, (CachedResponse) cached.get());
        }
        
        // 缓存未命中，继续处理并缓存响应
        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                // 缓存响应逻辑
                cacheResponse(cacheKey, response);
            }));
    }
    
    private boolean shouldCache(ServerHttpRequest request) {
        // 只缓存 GET 请求
        return HttpMethod.GET.equals(request.getMethod()) &&
               // 检查缓存控制头
               !request.getHeaders().containsKey("Cache-Control");
    }
}
```

## 性能优化配置

### 1. 连接池优化
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        # 连接池配置
        pool:
          type: elastic
          max-connections: 1000
          max-idle-time: 30s
          max-life-time: 60s
          pending-acquire-timeout: 60s
          pending-acquire-max-count: -1
        # 连接超时配置
        connect-timeout: 5s
        response-timeout: 30s
        # 压缩配置
        compression: true
        # 重试配置
        retry:
          max-retries: 3
          backoff:
            first-backoff: 50ms
            max-backoff: 500ms
            factor: 2
```

### 2. 负载均衡优化
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: optimized-route
          uri: lb://backend-service
          predicates:
            - Path=/api/optimized/**
          filters:
            - StripPrefix=2
            # 负载均衡配置
            - name: LoadBalancer
              args:
                loadBalancerType: ROUND_ROBIN
                healthCheck:
                  enabled: true
                  path: /health
                  interval: 10s
                  timeout: 5s
            # 熔断器配置
            - name: CircuitBreaker
              args:
                name: optimized-circuit
                fallbackUri: forward:/fallback
                statusCodes: BAD_GATEWAY,INTERNAL_SERVER_ERROR,SERVICE_UNAVAILABLE
                waitDurationInOpenState: 30s
                slidingWindowSize: 10
                minimumNumberOfCalls: 5
                permittedNumberOfCallsInHalfOpenState: 3
```

### 3. 缓存策略优化
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10分钟
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "tigateway:"
    cache-names:
      - routes
      - services
      - configs
      - responses

# 自定义缓存配置
tigateway:
  cache:
    routes:
      ttl: 300000  # 5分钟
      max-size: 1000
    services:
      ttl: 600000  # 10分钟
      max-size: 500
    responses:
      ttl: 30000   # 30秒
      max-size: 10000
```

## 安全高级配置

### 1. 多租户安全配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: multi-tenant-route
          uri: lb://backend-service
          predicates:
            - Path=/api/tenant/**
          filters:
            - StripPrefix=2
            # 租户隔离过滤器
            - name: TenantIsolationFilter
              args:
                tenant-header: X-Tenant-ID
                tenant-validation: true
            # 租户权限验证
            - name: TenantAuthorizationFilter
              args:
                required-permissions: ["tenant:read", "tenant:write"]
```

### 2. API 限流高级配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: advanced-rate-limit
          uri: lb://backend-service
          predicates:
            - Path=/api/limited/**
          filters:
            - StripPrefix=2
            # 基于用户的限流
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
                key-resolver: "#{@userKeyResolver}"
                deny-empty-key: true
                empty-key-status: 403
            # 基于 IP 的限流
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 50
                redis-rate-limiter.burstCapacity: 100
                key-resolver: "#{@ipKeyResolver}"
```

### 3. 安全头配置
```yaml
spring:
  cloud:
    gateway:
      default-filters:
        # 安全头过滤器
        - name: SecureHeadersFilter
          args:
            headers:
              X-Content-Type-Options: nosniff
              X-Frame-Options: DENY
              X-XSS-Protection: "1; mode=block"
              Strict-Transport-Security: "max-age=31536000; includeSubDomains"
              Content-Security-Policy: "default-src 'self'"
              Referrer-Policy: strict-origin-when-cross-origin
```

## 监控和可观测性

### 1. 自定义指标配置
```java
@Component
public class CustomMetricsConfiguration {
    
    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    @Bean
    public Counter customRequestCounter(MeterRegistry registry) {
        return Counter.builder("tigateway.requests.custom")
            .description("Custom request counter")
            .tag("type", "custom")
            .register(registry);
    }
    
    @Bean
    public Timer customRequestTimer(MeterRegistry registry) {
        return Timer.builder("tigateway.requests.duration")
            .description("Request processing time")
            .register(registry);
    }
}
```

### 2. 分布式追踪配置
```yaml
spring:
  sleuth:
    zipkin:
      base-url: http://zipkin-server:9411
    sampler:
      probability: 1.0
    web:
      client:
        enabled: true
    gateway:
      enabled: true
    kafka:
      enabled: true

# 自定义追踪配置
tigateway:
  tracing:
    enabled: true
    sample-rate: 0.1
    include-headers:
      - X-Request-ID
      - X-User-ID
      - X-Tenant-ID
    exclude-paths:
      - /actuator/**
      - /health
```

### 3. 日志聚合配置
```yaml
logging:
  level:
    root: INFO
    ti.gateway: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
  file:
    name: /app/logs/tigateway.log
    max-size: 100MB
    max-history: 30

# 结构化日志配置
tigateway:
  logging:
    structured: true
    include-mdc: true
    fields:
      - traceId
      - spanId
      - userId
      - tenantId
      - requestId
```

## 高可用配置

### 1. 集群配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: cluster-route
          uri: lb://backend-cluster
          predicates:
            - Path=/api/cluster/**
          filters:
            - StripPrefix=2
            # 健康检查
            - name: HealthCheckFilter
              args:
                health-check-path: /health
                health-check-interval: 10s
                unhealthy-threshold: 3
            # 故障转移
            - name: FailoverFilter
              args:
                failover-services:
                  - lb://backend-cluster-1
                  - lb://backend-cluster-2
                  - lb://backend-cluster-3
                failover-strategy: ROUND_ROBIN
```

### 2. 数据同步配置
```yaml
tigateway:
  cluster:
    enabled: true
    sync:
      # 配置同步
      config:
        enabled: true
        interval: 30s
        strategy: PUSH
      # 路由同步
      routes:
        enabled: true
        interval: 10s
        strategy: PULL
      # 状态同步
      status:
        enabled: true
        interval: 5s
        strategy: PUSH
```

## 环境特定配置

### 1. 生产环境配置
```yaml
# application-prod.yml
spring:
  cloud:
    gateway:
      routes:
        - id: prod-route
          uri: lb://prod-backend
          predicates:
            - Path=/api/**
          filters:
            - StripPrefix=1
            # 生产环境限流
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 1000
                redis-rate-limiter.burstCapacity: 2000
                key-resolver: "#{@ipKeyResolver}"
            # 生产环境熔断器
            - name: CircuitBreaker
              args:
                name: prod-circuit
                fallbackUri: forward:/fallback
                waitDurationInOpenState: 60s
                slidingWindowSize: 20
                minimumNumberOfCalls: 10

# 生产环境监控
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99

# 生产环境日志
logging:
  level:
    root: WARN
    ti.gateway: INFO
  file:
    name: /app/logs/tigateway-prod.log
    max-size: 500MB
    max-history: 90
```

### 2. 测试环境配置
```yaml
# application-test.yml
spring:
  cloud:
    gateway:
      routes:
        - id: test-route
          uri: lb://test-backend
          predicates:
            - Path=/api/test/**
          filters:
            - StripPrefix=2
            # 测试环境模拟延迟
            - name: DelayFilter
              args:
                delay: 100ms
            # 测试环境错误注入
            - name: ErrorInjectionFilter
              args:
                error-rate: 0.01
                error-types: [TIMEOUT, EXCEPTION]

# 测试环境监控
management:
  endpoints:
    web:
      exposure:
        include: "*"
  metrics:
    export:
      prometheus:
        enabled: true

# 测试环境日志
logging:
  level:
    root: INFO
    ti.gateway: DEBUG
```

## 配置验证和测试

### 1. 配置验证
```java
@Component
public class ConfigurationValidator {
    
    @EventListener
    public void validateConfiguration(ConfigurationChangeEvent event) {
        try {
            // 验证路由配置
            validateRouteConfiguration(event.getRoutes());
            
            // 验证过滤器配置
            validateFilterConfiguration(event.getFilters());
            
            // 验证安全配置
            validateSecurityConfiguration(event.getSecurity());
            
            log.info("Configuration validation passed");
        } catch (ValidationException e) {
            log.error("Configuration validation failed: {}", e.getMessage());
            throw new ConfigurationException("Invalid configuration", e);
        }
    }
    
    private void validateRouteConfiguration(List<RouteDefinition> routes) {
        for (RouteDefinition route : routes) {
            // 验证 URI 格式
            if (!isValidUri(route.getUri())) {
                throw new ValidationException("Invalid URI: " + route.getUri());
            }
            
            // 验证谓词配置
            validatePredicates(route.getPredicates());
            
            // 验证过滤器配置
            validateFilters(route.getFilters());
        }
    }
}
```

### 2. 配置测试
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.gateway.routes[0].id=test-route",
    "spring.cloud.gateway.routes[0].uri=lb://test-service",
    "spring.cloud.gateway.routes[0].predicates[0].name=Path",
    "spring.cloud.gateway.routes[0].predicates[0].args.pattern=/api/test/**"
})
class AdvancedConfigurationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    void shouldHandleComplexRouting() {
        webTestClient.get()
            .uri("/api/test/complex")
            .header("X-API-Version", "v2")
            .header("X-Service-Type", "premium")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists("X-Route-Type");
    }
    
    @Test
    void shouldApplyCustomFilters() {
        webTestClient.get()
            .uri("/api/test/filtered")
            .header("Authorization", "Bearer valid-token")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("X-User-ID", "user123");
    }
    
    @Test
    void shouldEnforceRateLimit() {
        // 发送大量请求测试限流
        for (int i = 0; i < 150; i++) {
            webTestClient.get()
                .uri("/api/test/limited")
                .exchange()
                .expectStatus().isOk();
        }
        
        // 超过限制应该被拒绝
        webTestClient.get()
            .uri("/api/test/limited")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
```

---

**相关文档**:
- [基础配置示例](./basic-config.md)
- [快速开始](./quick-start.md)
- [故障排除](./troubleshooting.md)
