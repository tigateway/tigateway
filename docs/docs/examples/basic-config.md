# TiGateway 基础配置示例

## 概述

本文档提供了 TiGateway 的基础配置示例，包括路由配置、服务发现、安全设置等常用场景。

## 应用配置

### 基础 application.yml
```yaml
server:
  port: 8080

spring:
  application:
    name: tigateway
  
  cloud:
    gateway:
      # 基础路由配置
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Gateway, TiGateway
      
      # 全局过滤器
      default-filters:
        - AddResponseHeader=X-Response-Time, ${timestamp}
      
      # 服务发现配置
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      
      # 负载均衡配置
      loadbalancer:
        cache:
          ttl: 5s
          capacity: 256

# Admin 配置
admin:
  server:
    enabled: true
    port: 8081
    context-path: /admin
    name: tigateway-admin

# Management 配置
management:
  server:
    port: 8090
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true

# 日志配置
logging:
  level:
    ti.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 路由配置示例

### 1. 基础路由
```yaml
spring:
  cloud:
    gateway:
      routes:
        # 用户服务路由
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service, user-service
        
        # 订单服务路由
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service, order-service
        
        # 商品服务路由
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service, product-service
```

### 2. 高级路由配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        # 带权重的路由
        - id: weighted-route
          uri: lb://backend-service
          predicates:
            - Path=/api/weighted/**
            - Weight=group1, 80
          filters:
            - StripPrefix=2
        
        # 带时间限制的路由
        - id: time-based-route
          uri: lb://maintenance-service
          predicates:
            - Path=/api/maintenance/**
            - Between=2024-01-01T00:00:00+08:00, 2024-12-31T23:59:59+08:00
          filters:
            - StripPrefix=2
        
        # 带请求头限制的路由
        - id: header-based-route
          uri: lb://vip-service
          predicates:
            - Path=/api/vip/**
            - Header=X-User-Type, VIP
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-VIP-User, true
```

### 3. 重写路径路由
```yaml
spring:
  cloud:
    gateway:
      routes:
        # 路径重写
        - id: rewrite-path-route
          uri: lb://backend-service
          predicates:
            - Path=/api/rewrite/**
          filters:
            - RewritePath=/api/rewrite/(?<segment>.*), /$\{segment}
        
        # 正则表达式重写
        - id: regex-rewrite-route
          uri: lb://backend-service
          predicates:
            - Path=/api/regex/**
          filters:
            - RewritePath=/api/regex/(?<path>.*), /api/v2/$\{path}
```

## 过滤器配置

### 1. 请求头过滤器
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: header-filters
          uri: lb://backend-service
          predicates:
            - Path=/api/headers/**
          filters:
            # 添加请求头
            - AddRequestHeader=X-Request-ID, ${random.uuid}
            - AddRequestHeader=X-Forwarded-For, ${remote-addr}
            - AddRequestHeader=X-Gateway-Version, 1.0.0
            
            # 添加响应头
            - AddResponseHeader=X-Response-Time, ${timestamp}
            - AddResponseHeader=X-Gateway-Processed, true
            
            # 移除请求头
            - RemoveRequestHeader=X-Secret-Header
            
            # 设置请求头
            - SetRequestHeader=X-User-ID, ${user.id}
```

### 2. 限流过滤器
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rate-limit-route
          uri: lb://backend-service
          predicates:
            - Path=/api/limited/**
          filters:
            # 基于令牌桶的限流
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@userKeyResolver}"
```

### 3. 熔断器过滤器
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: circuit-breaker-route
          uri: lb://backend-service
          predicates:
            - Path=/api/circuit/**
          filters:
            - name: CircuitBreaker
              args:
                name: backend-service
                fallbackUri: forward:/fallback
                statusCodes: BAD_GATEWAY,INTERNAL_SERVER_ERROR
```

## 服务发现配置

### 1. Kubernetes 服务发现
```yaml
spring:
  cloud:
    kubernetes:
      discovery:
        enabled: true
        namespace: default
        all-namespaces: false
        include-not-ready-addresses: false
      
      config:
        enabled: true
        name: tigateway-config
        namespace: default
        sources:
          - name: tigateway-config
            namespace: default
```

### 2. Consul 服务发现
```yaml
spring:
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
        service-name: tigateway
        instance-id: ${spring.application.name}:${server.port}
        health-check-interval: 10s
        health-check-timeout: 5s
        health-check-critical-timeout: 30s
```

### 3. Eureka 服务发现
```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
          predicates:
            - name: Path
              args:
                pattern: "'/api/'+serviceId+'/**'"
          filters:
            - name: RewritePath
              args:
                regexp: "'/api/' + serviceId + '/(?<remaining>.*)'"
                replacement: "'/${remaining}'"

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## 安全配置

### 1. OAuth2 配置
```yaml
spring:
  security:
    oauth2:
      client:
        provider:
          sso:
            issuer-uri: ${SSO_ISSUER_URI}
        registration:
          sso:
            client-id: ${SSO_CLIENT_ID}
            client-secret: ${SSO_CLIENT_SECRET}
            scope: openid,profile,email
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"

sso:
  roles-attribute-name: roles
```

### 2. JWT 配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: jwt-route
          uri: lb://backend-service
          predicates:
            - Path=/api/secure/**
          filters:
            - name: JwtAuthenticationFilter
              args:
                jwt-secret: ${JWT_SECRET}
                jwt-expiration: 3600
```

### 3. API Key 配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: api-key-route
          uri: lb://backend-service
          predicates:
            - Path=/api/key/**
            - Header=X-API-Key, ${API_KEY}
          filters:
            - AddRequestHeader=X-Authenticated, true
```

## 监控配置

### 1. Prometheus 监控
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
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
```

### 2. 自定义指标
```yaml
management:
  metrics:
    custom:
      tigateway:
        routes:
          enabled: true
        requests:
          enabled: true
        errors:
          enabled: true
```

## 日志配置

### 1. 结构化日志
```yaml
logging:
  level:
    root: INFO
    ti.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
    org.springframework.web.reactive: DEBUG
  
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
  
  file:
    name: /app/logs/tigateway.log
    max-size: 100MB
    max-history: 30
```

### 2. 访问日志
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        wiretap: true
      httpserver:
        wiretap: true

logging:
  level:
    reactor.netty.http.client: DEBUG
    reactor.netty.http.server: DEBUG
```

## 缓存配置

### 1. Redis 缓存
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
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms

  cache:
    type: redis
    redis:
      time-to-live: 600000
      cache-null-values: false
```

### 2. 本地缓存
```yaml
spring:
  cache:
    type: simple
    cache-names:
      - routes
      - services
      - configs
```

## 性能优化配置

### 1. 连接池配置
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 1000
        response-timeout: 5s
        pool:
          max-connections: 500
          max-idle-time: 30s
          max-life-time: 60s
          pending-acquire-timeout: 60s
          pending-acquire-max-count: -1
```

### 2. 线程池配置
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          type: elastic
          max-connections: 500
          max-idle-time: 30s
          max-life-time: 60s
```

## 环境特定配置

### 1. 开发环境
```yaml
# application-dev.yml
spring:
  cloud:
    gateway:
      routes:
        - id: dev-route
          uri: http://localhost:8081
          predicates:
            - Path=/dev/**
          filters:
            - StripPrefix=1

logging:
  level:
    ti.gateway: DEBUG
```

### 2. 测试环境
```yaml
# application-test.yml
spring:
  cloud:
    gateway:
      routes:
        - id: test-route
          uri: http://test-backend:8080
          predicates:
            - Path=/test/**
          filters:
            - StripPrefix=1

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 3. 生产环境
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
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

## 配置验证

### 1. 配置检查端点
```bash
# 检查配置属性
curl http://localhost:8090/actuator/configprops

# 检查环境变量
curl http://localhost:8090/actuator/env

# 检查健康状态
curl http://localhost:8090/actuator/health
```

### 2. 路由验证
```bash
# 获取所有路由
curl http://localhost:8080/actuator/gateway/routes

# 测试特定路由
curl http://localhost:8080/api/users/123
```

---

**相关文档**:
- [高级配置示例](./advanced-config.md)
- [快速开始](./quick-start.md)
- [故障排除](./troubleshooting.md)
