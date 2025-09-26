# Configuration

TiGateway provides flexible configuration options to meet various deployment and operational requirements. This guide covers all configuration aspects from basic setup to advanced customization.

## Configuration Overview

TiGateway supports multiple configuration sources and formats:

- **YAML Configuration**: Primary configuration format
- **Environment Variables**: Runtime configuration overrides
- **ConfigMap**: Kubernetes-native configuration storage
- **Dynamic Configuration**: Runtime configuration updates

## Basic Configuration

### Application Configuration

The main configuration file is `application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: tigateway
  profiles:
    active: kubernetes
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: default-route
          uri: lb://test-service
          predicates:
            - Path=/test/**
          filters:
            - StripPrefix=1

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always

logging:
  level:
    ti.gateway: INFO
    org.springframework.cloud.gateway: INFO
```

### Environment-Specific Configuration

#### Development Configuration

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
            - AddRequestHeader=X-Environment,dev

logging:
  level:
    ti.gateway: DEBUG
```

#### Production Configuration

```yaml
# application-prod.yml
spring:
  cloud:
    gateway:
      routes:
        - id: prod-route
          uri: lb://production-service
          predicates:
            - Path=/api/**
          filters:
            - AddRequestHeader=X-Environment,prod
            - RequestRateLimiter=10,1,redis-rate-limiter

logging:
  level:
    ti.gateway: WARN
    org.springframework.cloud.gateway: WARN
```

## Kubernetes Configuration

### ConfigMap Configuration

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-config
  namespace: tigateway
data:
  application.yml: |
    server:
      port: 8080
    spring:
      cloud:
        gateway:
          routes:
            - id: k8s-route
              uri: lb://kubernetes-service
              predicates:
                - Path=/k8s/**
              filters:
                - StripPrefix=1
  routes.yml: |
    routes:
      - id: configmap-route
        uri: lb://configmap-service
        predicates:
          - Path=/configmap/**
        filters:
          - AddRequestHeader=X-Source,configmap
```

### Environment Variables

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tigateway
spec:
  template:
    spec:
      containers:
      - name: tigateway
        image: tigateway/tigateway:1.0.0
        env:
        - name: SERVER_PORT
          value: "8080"
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: CONFIG_STORAGE_TYPE
          value: "configmap"
        - name: LOG_LEVEL
          value: "INFO"
```

## Route Configuration

### Basic Route Definition

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
            - Method=GET,POST,PUT,DELETE
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service,user-service
```

### Advanced Route Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: advanced-route
          uri: lb://backend-service
          predicates:
            - Path=/api/v1/**
            - Header=X-API-Version,v1
            - Query=debug,true
            - Cookie=sessionId,.*
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Gateway,TiGateway
            - AddResponseHeader=X-Processed-By,TiGateway
            - CircuitBreaker=backend-cb,forward:/fallback
            - RequestRateLimiter=100,1,redis-rate-limiter
          metadata:
            description: "Advanced route with multiple predicates and filters"
            tags: ["api", "v1", "advanced"]
```

## Filter Configuration

### Global Filters

```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - AddRequestHeader=X-Request-ID,${random.uuid}
        - AddResponseHeader=X-Response-Time,${timestamp}
        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin
```

### Route-Specific Filters

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: filtered-route
          uri: lb://service
          predicates:
            - Path=/filtered/**
          filters:
            - StripPrefix=1
            - AddRequestHeader=X-Custom-Header,custom-value
            - RewritePath=/filtered/(?<segment>.*),/api/${segment}
            - CircuitBreaker=service-cb,forward:/fallback
```

## Load Balancing Configuration

### Load Balancer Types

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: lb-route
          uri: lb://service-cluster
          predicates:
            - Path=/lb/**
          filters:
            - name: LoadBalancer
              args:
                type: ROUND_ROBIN
                healthCheck:
                  enabled: true
                  path: /health
                  interval: 10s
                  timeout: 5s
```

### Service Discovery Integration

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
                pattern: "'/'+serviceId+'/**'"
          filters:
            - name: RewritePath
              args:
                regexp: "'/'+serviceId+'/(?<remaining>.*)'"
                replacement: "'/${remaining}'"
```

## Security Configuration

### Authentication Configuration

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          gateway:
            client-id: ${OAUTH2_CLIENT_ID}
            client-secret: ${OAUTH2_CLIENT_SECRET}
            scope: read,write
        provider:
          gateway:
            authorization-uri: ${OAUTH2_AUTHORIZATION_URI}
            token-uri: ${OAUTH2_TOKEN_URI}
            user-info-uri: ${OAUTH2_USER_INFO_URI}
```

### Rate Limiting Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rate-limited-route
          uri: lb://service
          predicates:
            - Path=/api/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
                key-resolver: "#{@userKeyResolver}"
```

## Monitoring Configuration

### Metrics Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: tigateway
      environment: ${SPRING_PROFILES_ACTIVE:default}
```

### Logging Configuration

```yaml
logging:
  level:
    ti.gateway: INFO
    org.springframework.cloud.gateway: INFO
    org.springframework.web.reactive: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/tigateway.log
    max-size: 100MB
    max-history: 30
```

## Dynamic Configuration

### Runtime Configuration Updates

TiGateway supports dynamic configuration updates without restarts:

```yaml
# Configuration update via REST API
curl -X POST http://localhost:8080/actuator/gateway/routes/new-route \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "lb://new-service",
    "predicates": [{"name": "Path", "args": {"pattern": "/new/**"}}],
    "filters": [{"name": "StripPrefix", "args": {"parts": 1}}]
  }'
```

### Configuration Validation

```yaml
# Configuration validation
spring:
  cloud:
    gateway:
      validation:
        enabled: true
        strict-mode: true
```

## Configuration Best Practices

### 1. Environment Separation

```yaml
# Use profiles for environment-specific configuration
spring:
  profiles:
    active: ${ENVIRONMENT:dev}
```

### 2. External Configuration

```yaml
# Use external configuration sources
spring:
  config:
    import:
      - configmap:tigateway-config
      - optional:file:./config/override.yml
```

### 3. Configuration Encryption

```yaml
# Encrypt sensitive configuration
spring:
  cloud:
    config:
      server:
        encrypt:
          enabled: true
```

### 4. Configuration Validation

```yaml
# Validate configuration on startup
spring:
  cloud:
    gateway:
      validation:
        enabled: true
        fail-fast: true
```

## Troubleshooting Configuration

### Common Configuration Issues

#### Route Not Working

```bash
# Check route configuration
curl http://localhost:8080/actuator/gateway/routes

# Check route filters
curl http://localhost:8080/actuator/gateway/routefilters
```

#### Configuration Not Loading

```bash
# Check configuration properties
curl http://localhost:8080/actuator/configprops

# Check environment variables
curl http://localhost:8080/actuator/env
```

#### Performance Issues

```bash
# Check metrics
curl http://localhost:8080/actuator/metrics

# Check health status
curl http://localhost:8080/actuator/health
```

### Configuration Debugging

```yaml
# Enable debug logging
logging:
  level:
    ti.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
    org.springframework.web.reactive: DEBUG
```

## Configuration Examples

### Complete Production Configuration

```yaml
# application-prod.yml
server:
  port: 8080
  compression:
    enabled: true

spring:
  application:
    name: tigateway
  profiles:
    active: prod
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: api-route
          uri: lb://api-service
          predicates:
            - Path=/api/**
          filters:
            - StripPrefix=1
            - AddRequestHeader=X-Gateway,TiGateway
            - CircuitBreaker=api-cb,forward:/fallback
            - RequestRateLimiter=1000,1,redis-rate-limiter
      default-filters:
        - AddRequestHeader=X-Request-ID,${random.uuid}
        - AddResponseHeader=X-Response-Time,${timestamp}

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    ti.gateway: INFO
    org.springframework.cloud.gateway: INFO
  file:
    name: logs/tigateway.log
    max-size: 100MB
    max-history: 30
```

## Next Steps

After configuring TiGateway:

1. **[Route Management](./routes-and-predicates.md)** - Learn about advanced route configuration
2. **[Filter Configuration](./filters.md)** - Explore available filters
3. **[Security Setup](./security-best-practices.md)** - Secure your gateway
4. **[Monitoring Setup](./monitoring-and-metrics.md)** - Set up monitoring and alerting

---

**Configuration complete?** Check out our [Route Management Guide](./routes-and-predicates.md) to learn about advanced routing features.
