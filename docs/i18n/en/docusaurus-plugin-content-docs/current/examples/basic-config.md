# TiGateway Basic Configuration Examples

## Overview

This document provides basic configuration examples for TiGateway, including routing configuration, service discovery, security settings, and other common scenarios.

## Application Configuration

### Basic application.yml
```yaml
server:
  port: 8080

spring:
  application:
    name: tigateway
  
  cloud:
    gateway:
      # Basic route configuration
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Gateway, TiGateway
      
      # Global filters
      default-filters:
        - AddResponseHeader=X-Response-Time, ${timestamp}
      
      # Service discovery configuration
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      
      # Load balancer configuration
      loadbalancer:
        cache:
          ttl: 5s
          capacity: 256

# Admin configuration
admin:
  server:
    enabled: true
    port: 8081
    context-path: /admin
    name: tigateway-admin

# Management configuration
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

# Logging configuration
logging:
  level:
    ti.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## Route Configuration Examples

### 1. Basic Routes
```yaml
spring:
  cloud:
    gateway:
      routes:
        # User service route
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service, user-service
        
        # Order service route
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service, order-service
        
        # Product service route
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service, product-service
```

### 2. Advanced Route Configuration
```yaml
spring:
  cloud:
    gateway:
      routes:
        # Weighted route
        - id: weighted-route
          uri: lb://backend-service
          predicates:
            - Path=/api/weighted/**
            - Weight=group1, 80
          filters:
            - StripPrefix=2
        
        # Time-based route
        - id: time-based-route
          uri: lb://maintenance-service
          predicates:
            - Path=/api/maintenance/**
            - Between=2024-01-01T00:00:00+08:00, 2024-12-31T23:59:59+08:00
          filters:
            - StripPrefix=2
        
        # Header-based route
        - id: header-based-route
          uri: lb://vip-service
          predicates:
            - Path=/api/vip/**
            - Header=X-User-Type, VIP
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-VIP-User, true
```

### 3. Path Rewrite Routes
```yaml
spring:
  cloud:
    gateway:
      routes:
        # Path rewrite
        - id: rewrite-path-route
          uri: lb://backend-service
          predicates:
            - Path=/api/rewrite/**
          filters:
            - RewritePath=/api/rewrite/(?<segment>.*), /$\{segment}
        
        # Regex rewrite
        - id: regex-rewrite-route
          uri: lb://backend-service
          predicates:
            - Path=/api/regex/**
          filters:
            - RewritePath=/api/regex/(?<path>.*), /api/v2/$\{path}
```

## Filter Configuration

### 1. Request Header Filters
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
            # Add request headers
            - AddRequestHeader=X-Request-ID, ${random.uuid}
            - AddRequestHeader=X-Forwarded-For, ${remote-addr}
            - AddRequestHeader=X-Gateway-Version, 1.0.0
            
            # Add response headers
            - AddResponseHeader=X-Response-Time, ${timestamp}
            - AddResponseHeader=X-Gateway-Processed, true
            
            # Remove request headers
            - RemoveRequestHeader=X-Secret-Header
            
            # Set request headers
            - SetRequestHeader=X-User-ID, ${user.id}
```

### 2. Rate Limiting Filters
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
            # Token bucket rate limiting
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@userKeyResolver}"
```

### 3. Circuit Breaker Filters
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

## Service Discovery Configuration

### 1. Kubernetes Service Discovery
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

### 2. Consul Service Discovery
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

### 3. Eureka Service Discovery
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

## Security Configuration

### 1. OAuth2 Configuration
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

### 2. JWT Configuration
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

### 3. API Key Configuration
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

## Monitoring Configuration

### 1. Prometheus Monitoring
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

### 2. Custom Metrics
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

## Logging Configuration

### 1. Structured Logging
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

### 2. Access Logging
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

## Cache Configuration

### 1. Redis Cache
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

### 2. Local Cache
```yaml
spring:
  cache:
    type: simple
    cache-names:
      - routes
      - services
      - configs
```

## Performance Optimization Configuration

### 1. Connection Pool Configuration
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

### 2. Thread Pool Configuration
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

## Environment-Specific Configuration

### 1. Development Environment
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

### 2. Test Environment
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

### 3. Production Environment
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

## Configuration Validation

### 1. Configuration Check Endpoints
```bash
# Check configuration properties
curl http://localhost:8090/actuator/configprops

# Check environment variables
curl http://localhost:8090/actuator/env

# Check health status
curl http://localhost:8090/actuator/health
```

### 2. Route Validation
```bash
# Get all routes
curl http://localhost:8080/actuator/gateway/routes

# Test specific route
curl http://localhost:8080/api/users/123
```

---

**Related Documentation**:
- [Advanced Configuration Examples](./crd-advanced-config.md)
- [Quick Start Guide](../getting-started/quick-start.md)
- [Troubleshooting Guide](./troubleshooting.md)
