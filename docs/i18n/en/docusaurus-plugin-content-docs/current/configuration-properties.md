# Configuration Properties

This guide covers all TiGateway configuration properties, including gateway settings, security configuration, monitoring options, and advanced features.

## Overview

TiGateway configuration is organized into several categories:

- **Gateway Configuration**: Core gateway settings
- **Security Configuration**: Authentication and authorization
- **Monitoring Configuration**: Metrics and logging
- **Service Discovery**: Service discovery settings
- **Load Balancing**: Load balancing configuration
- **Custom Properties**: User-defined properties

## Gateway Configuration

### Basic Gateway Settings

```yaml
spring:
  cloud:
    gateway:
      # Gateway server settings
      server:
        port: 8080
        context-path: /
      
      # Default filters
      default-filters:
        - AddRequestHeader=X-Gateway, TiGateway
        - AddResponseHeader=X-Response-Time, ${T(java.lang.System).currentTimeMillis()}
      
      # Global CORS configuration
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
            allowed-headers: "*"
            allow-credentials: true
            max-age: 3600
```

### HTTP Client Configuration

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        # Connection settings
        connect-timeout: 1000
        response-timeout: 5000
        
        # Connection pool settings
        pool:
          max-connections: 500
          max-idle-time: 30s
          max-life-time: 60s
          pending-acquire-timeout: 60s
          pending-acquire-max-count: 1000
          evict-in-background: true
          eviction-interval: 30s
        
        # HTTP/2 settings
        http2:
          enabled: true
        
        # Compression settings
        compression:
          enabled: true
          mime-types: "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json"
          min-response-size: 1024
```

### Route Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
            - Method=GET,POST
            - Header=X-API-Version,v1
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service,user-service
            - CircuitBreaker=user-service-cb,forward:/fallback
          order: 0
          metadata:
            response-timeout: 5000
            connect-timeout: 1000
```

## Security Configuration

### Authentication Settings

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # JWT configuration
          jwk-set-uri: ${JWT_JWK_SET_URI:http://auth-service:8080/.well-known/jwks.json}
          issuer-uri: ${JWT_ISSUER_URI:http://auth-service:8080}
          audience: ${JWT_AUDIENCE:api-gateway}
          
          # JWT validation
          clock-skew: 60s
          cache-ttl: 600s
          
          # Custom claims
          custom-claims:
            roles: "roles"
            tenant: "tenant"
            permissions: "permissions"
```

### Authorization Settings

```yaml
security:
  rbac:
    enabled: true
    default-role: USER
    
    # Role definitions
    roles:
      ADMIN:
        permissions:
          - "user:read"
          - "user:write"
          - "user:delete"
          - "admin:read"
          - "admin:write"
          - "admin:delete"
      USER:
        permissions:
          - "user:read"
          - "user:write"
      GUEST:
        permissions:
          - "user:read"
    
    # Permission mapping
    permission-mapping:
      "user:read": ["GET", "/api/users/**"]
      "user:write": ["POST", "PUT", "/api/users/**"]
      "user:delete": ["DELETE", "/api/users/**"]
      "admin:read": ["GET", "/api/admin/**"]
      "admin:write": ["POST", "PUT", "/api/admin/**"]
      "admin:delete": ["DELETE", "/api/admin/**"]
```

### API Key Configuration

```yaml
security:
  api-key:
    enabled: true
    header-name: X-API-Key
    
    # API key definitions
    keys:
      - key: "admin-key-123"
        roles: ["ADMIN"]
        permissions: ["*"]
        expires: "2024-12-31T23:59:59Z"
      - key: "user-key-456"
        roles: ["USER"]
        permissions: ["user:read", "user:write"]
        expires: "2024-12-31T23:59:59Z"
      - key: "guest-key-789"
        roles: ["GUEST"]
        permissions: ["user:read"]
        expires: "2024-12-31T23:59:59Z"
```

## Monitoring Configuration

### Metrics Settings

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
      base-path: /actuator
  
  metrics:
    export:
      prometheus:
        enabled: true
        step: 10s
      influx:
        enabled: false
        uri: http://influxdb:8086
        db: tigateway
        step: 10s
      wavefront:
        enabled: false
        uri: http://wavefront:8080
        step: 10s
    
    # Metric distribution
    distribution:
      percentiles-histogram:
        http.server.requests: true
        tigateway.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
        tigateway.requests: 0.5, 0.95, 0.99
```

### Logging Configuration

```yaml
logging:
  level:
    ti.gateway: INFO
    org.springframework.cloud.gateway: INFO
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
  
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId},%X{spanId}] %-5level %logger{36} - %msg%n"
  
  file:
    name: logs/tigateway.log
    max-size: 100MB
    max-history: 30
    total-size-cap: 1GB
```

### Tracing Configuration

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
    zipkin:
      tracing:
        endpoint: http://zipkin:9411/api/v2/spans
    wavefront:
      tracing:
        endpoint: http://wavefront:8080/api/v2/spans
```

## Service Discovery Configuration

### Eureka Configuration

```yaml
spring:
  cloud:
    discovery:
      client:
        enabled: true
        service-id: tigateway
        health-indicator:
          enabled: true
        eureka:
          instance:
            health-check-url: http://${spring.cloud.client.ip-address}:${server.port}/actuator/health
            health-check-url-path: /actuator/health
            prefer-ip-address: true
            lease-renewal-interval-in-seconds: 30
            lease-expiration-duration-in-seconds: 90
```

### Consul Configuration

```yaml
spring:
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
        service-name: tigateway
        health-check-path: /actuator/health
        health-check-interval: 10s
        health-check-timeout: 5s
        health-check-critical-timeout: 30s
        instance-id: ${spring.application.name}:${spring.cloud.client.ip-address}:${server.port}
        prefer-ip-address: true
```

### Kubernetes Configuration

```yaml
spring:
  cloud:
    kubernetes:
      discovery:
        enabled: true
        service-name: tigateway
        namespace: default
        health-check-path: /actuator/health
        health-check-interval: 10s
        health-check-timeout: 5s
        include-not-ready-addresses: false
        all-namespaces: false
```

## Load Balancing Configuration

### Load Balancer Settings

```yaml
spring:
  cloud:
    loadbalancer:
      # Default configuration
      configurations:
        default:
          enable: true
          strategy: ROUND_ROBIN
      
      # Health check configuration
      health-check:
        enabled: true
        path: /actuator/health
        interval: 10s
        timeout: 5s
        retries: 3
        initial-delay: 30s
      
      # Cache configuration
      cache:
        enabled: true
        ttl: 35s
        capacity: 256
```

### Circuit Breaker Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      user-service-cb:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
        slow-call-rate-threshold: 50
        slow-call-duration-threshold: 2s
        max-wait-duration-in-half-open-state: 0s
        sliding-window-type: COUNT_BASED
```

### Rate Limiting Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rate-limited-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                redis-rate-limiter.requestedTokens: 1
                key-resolver: "#{@userKeyResolver}"
```

## Custom Configuration

### Custom Properties

```yaml
custom:
  # Custom gateway settings
  gateway:
    enabled: true
    default-timeout: 5000
    max-retries: 3
    retry-delay: 1000
  
  # Custom filter settings
  filters:
    custom-filter:
      enabled: true
      header-name: X-Custom
      header-value: processed
  
  # Custom predicate settings
  predicates:
    custom-predicate:
      enabled: true
      max-requests: 100
      time-window: 60s
  
  # Custom load balancer settings
  loadbalancer:
    custom-strategy:
      enabled: true
      weight-factor: 0.8
      health-check-weight: 0.2
```

### Environment-specific Configuration

```yaml
# application-dev.yml
spring:
  profiles:
    active: dev
  
custom:
  gateway:
    default-timeout: 10000
    max-retries: 5
  
  filters:
    custom-filter:
      enabled: false

---
# application-prod.yml
spring:
  profiles:
    active: prod
  
custom:
  gateway:
    default-timeout: 5000
    max-retries: 3
  
  filters:
    custom-filter:
      enabled: true
      header-value: production
```

## Configuration Validation

### Validation Rules

```yaml
validation:
  # Gateway validation
  gateway:
    port:
      min: 1024
      max: 65535
    timeout:
      min: 1000
      max: 30000
  
  # Security validation
  security:
    jwt:
      clock-skew:
        min: 0
        max: 300
      cache-ttl:
        min: 60
        max: 3600
  
  # Monitoring validation
  monitoring:
    metrics:
      step:
        min: 1
        max: 60
      percentiles:
        min: 0.0
        max: 1.0
```

### Configuration Schema

```yaml
# Configuration schema definition
schema:
  version: "1.0"
  properties:
    spring:
      type: object
      properties:
        cloud:
          type: object
          properties:
            gateway:
              type: object
              properties:
                routes:
                  type: array
                  items:
                    type: object
                    required: ["id", "uri", "predicates"]
                    properties:
                      id:
                        type: string
                        minLength: 1
                      uri:
                        type: string
                        pattern: "^https?://.*"
                      predicates:
                        type: array
                        minItems: 1
                      filters:
                        type: array
                      order:
                        type: integer
                        minimum: 0
```

## Configuration Management

### Dynamic Configuration

```yaml
management:
  endpoint:
    configprops:
      enabled: true
    env:
      enabled: true
    refresh:
      enabled: true
  
  # Configuration refresh
  refresh:
    enabled: true
    delay: 5s
    timeout: 30s
```

### Configuration Sources

```yaml
spring:
  config:
    import:
      - configserver:http://config-server:8888
      - vault://vault-server:8200
      - consul://consul:8500
    
    # Configuration server
    cloud:
      config:
        server:
          git:
            uri: https://github.com/tigateway/config-repo
            search-paths: config
            default-label: main
```

## Best Practices

### Configuration Organization

1. **Environment Separation**: Use separate configs for different environments
2. **Sensitive Data**: Use external configuration for sensitive data
3. **Validation**: Validate configuration on startup
4. **Documentation**: Document all configuration properties
5. **Version Control**: Version control configuration files

### Security

1. **Encryption**: Encrypt sensitive configuration
2. **Access Control**: Control access to configuration
3. **Audit**: Audit configuration changes
4. **Backup**: Backup configuration regularly
5. **Recovery**: Have configuration recovery procedures

### Performance

1. **Caching**: Cache configuration when appropriate
2. **Lazy Loading**: Load configuration lazily
3. **Validation**: Validate configuration efficiently
4. **Monitoring**: Monitor configuration performance
5. **Optimization**: Optimize configuration loading

## Troubleshooting

### Common Issues

#### Configuration Not Loading

```bash
# Check configuration files
ls -la application*.yml

# Check configuration properties
curl http://localhost:8080/actuator/configprops

# Check environment variables
env | grep -i tigateway

# Check configuration validation
java -jar tigateway.jar --spring.config.location=application.yml --debug
```

#### Configuration Validation Errors

```bash
# Check validation errors
tail -f logs/tigateway.log | grep -i "validation"

# Check configuration schema
curl http://localhost:8080/actuator/configprops | jq '.'

# Validate configuration
java -jar tigateway.jar --spring.config.location=application.yml --validate
```

#### Environment Issues

```bash
# Check active profiles
curl http://localhost:8080/actuator/env | jq '.activeProfiles'

# Check configuration sources
curl http://localhost:8080/actuator/configprops | jq '.contexts'

# Check environment variables
curl http://localhost:8080/actuator/env | jq '.propertySources'
```

### Debug Commands

```bash
# Check all configuration properties
curl http://localhost:8080/actuator/configprops

# Check specific configuration
curl http://localhost:8080/actuator/configprops | jq '.contexts.application.beans.spring.cloud.gateway'

# Check environment
curl http://localhost:8080/actuator/env

# Refresh configuration
curl -X POST http://localhost:8080/actuator/refresh
```

## Next Steps

After configuring TiGateway:

1. **[Quick Start](./quick-start.md)** - Get started with TiGateway
2. **[Configuration Guide](./configuration.md)** - Advanced configuration
3. **[Troubleshooting Guide](./troubleshooting.md)** - Common configuration issues
4. **[Security Best Practices](./security-best-practices.md)** - Secure configuration

---

**Ready to get started?** Check out our [Quick Start](./quick-start.md) guide to begin using TiGateway.
