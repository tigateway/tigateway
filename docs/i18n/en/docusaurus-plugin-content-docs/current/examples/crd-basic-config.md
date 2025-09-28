# TiGateway CRD Basic Configuration Examples

## Overview

This document provides basic configuration examples for TiGateway based on Kubernetes Custom Resource Definitions (CRD), demonstrating how to use declarative configuration to manage the TiGateway gateway.

## 1. Basic Route Configuration

### 1.1 Simple Route Configuration

```yaml
# Using TiGatewayRouteConfig CRD
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: user-service-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: user-service-group
      description: "User service route group"
      routes:
        - id: user-service
          description: "User service route"
          target:
            service: user-service
            namespace: backend
            port: 8080
          match:
            path: /api/users/**
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Service
                value: user-service
```

### 1.2 Load Balancing Route Configuration

```yaml
# Using TiGatewayRouteConfig CRD for load balancing configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: user-service-lb-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: user-service-lb-group
      description: "User service load balancing route group"
      routes:
        - id: user-service-lb
          description: "User service load balancing route"
          target:
            service: user-service
            namespace: backend
            loadBalancer:
              type: round_robin
          match:
            path: /api/users/**
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Service
                value: user-service
```

### 1.3 Multi-Service Route Configuration

```yaml
# Multi-service route configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: microservices-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: microservices-group
      description: "Microservices route group"
      routes:
        # User service route
        - id: user-service
          description: "User service route"
          target:
            service: user-service
            namespace: backend
            port: 8080
          match:
            path: /api/users/**
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Service
                value: user-service
        
        # Order service route
        - id: order-service
          description: "Order service route"
          target:
            service: order-service
            namespace: backend
            port: 8080
          match:
            path: /api/orders/**
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Service
                value: order-service
        
        # Product service route
        - id: product-service
          description: "Product service route"
          target:
            service: product-service
            namespace: backend
            port: 8080
          match:
            path: /api/products/**
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Service
                value: product-service
```

## 2. Advanced Route Configuration

### 2.1 Weighted Route Configuration

```yaml
# Weighted route configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: weighted-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: weighted-group
      description: "Weighted route group"
      routes:
        - id: weighted-route-80
          description: "80% traffic route"
          target:
            service: backend-service
            namespace: backend
            port: 8080
          match:
            path: /api/weighted/**
            weight: 80
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Traffic-Weight
                value: "80"
        
        - id: weighted-route-20
          description: "20% traffic route"
          target:
            service: backend-service-v2
            namespace: backend
            port: 8080
          match:
            path: /api/weighted/**
            weight: 20
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Traffic-Weight
                value: "20"
```

### 2.2 Time-Based Route Configuration

```yaml
# Time-based route configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: time-based-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: time-based-group
      description: "Time-based route group"
      routes:
        - id: maintenance-route
          description: "Maintenance time route"
          target:
            service: maintenance-service
            namespace: backend
            port: 8080
          match:
            path: /api/maintenance/**
            time:
              start: "2024-01-01T00:00:00Z"
              end: "2024-12-31T23:59:59Z"
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Maintenance-Mode
                value: "true"
```

### 2.3 Header-Based Route Configuration

```yaml
# Header-based route configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: header-based-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: header-based-group
      description: "Header-based route group"
      routes:
        - id: vip-route
          description: "VIP user route"
          target:
            service: vip-service
            namespace: backend
            port: 8080
          match:
            path: /api/vip/**
            headers:
              - name: X-User-Type
                value: VIP
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-VIP-User
                value: "true"
```

## 3. Filter Configuration

### 3.1 Request Header Filter Configuration

```yaml
# Request header filter configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterChain
metadata:
  name: header-filters
  namespace: tigateway
spec:
  routeFilters:
    - routeSelector:
        labels:
          service: user
      filters:
        - name: AddRequestHeader
          order: -100
          config:
            name: X-Request-ID
            value: "${random.uuid}"
        
        - name: AddRequestHeader
          order: -99
          config:
            name: X-Forwarded-For
            value: "${remote-addr}"
        
        - name: AddRequestHeader
          order: -98
          config:
            name: X-Gateway-Version
            value: "1.0.0"
        
        - name: AddResponseHeader
          order: 100
          config:
            name: X-Response-Time
            value: "${timestamp}"
        
        - name: AddResponseHeader
          order: 101
          config:
            name: X-Gateway-Processed
            value: "true"
```

### 3.2 Rate Limiting Filter Configuration

```yaml
# Rate limiting filter configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterChain
metadata:
  name: rate-limit-filters
  namespace: tigateway
spec:
  globalFilters:
    - name: RateLimitFilter
      order: -700
      config:
        type: redis
        redis:
          host: redis-server
          port: 6379
          database: 0
        limits:
          - key: ip
            requestsPerMinute: 1000
            burstCapacity: 2000
          - key: user
            requestsPerMinute: 100
            burstCapacity: 200
          - key: global
            requestsPerMinute: 10000
            burstCapacity: 20000
  
  routeFilters:
    - routeSelector:
        labels:
          service: limited
      filters:
        - name: RequestRateLimiter
          order: -50
          config:
            redis-rate-limiter:
              replenishRate: 10
              burstCapacity: 20
            keyResolver: user
```

### 3.3 Circuit Breaker Filter Configuration

```yaml
# Circuit breaker filter configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterChain
metadata:
  name: circuit-breaker-filters
  namespace: tigateway
spec:
  routeFilters:
    - routeSelector:
        labels:
          service: circuit
      filters:
        - name: CircuitBreaker
          order: -60
          config:
            name: backend-service
            fallbackUri: forward:/fallback
            statusCodes: [BAD_GATEWAY, INTERNAL_SERVER_ERROR]
            failureThreshold: 5
            timeout: 30s
            halfOpenMaxCalls: 3
```

## 4. Security Configuration

### 4.1 Authentication Configuration

```yaml
# Authentication configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayAuthentication
metadata:
  name: jwt-authentication
  namespace: tigateway
spec:
  providers:
    - name: jwt-provider
      type: jwt
      config:
        secret: ${JWT_SECRET}
        algorithm: HS256
        issuer: tigateway
        audience: tigateway-clients
        expiration: 3600s
        claims:
          - name: sub
            required: true
            type: string
          - name: roles
            required: true
            type: array
          - name: tenant
            required: false
            type: string
  
  strategies:
    - name: jwt-strategy
      provider: jwt-provider
      order: 1
      conditions:
        - header: Authorization
          pattern: "Bearer .+"
  
  rules:
    - name: public-access
      paths:
        - /health
        - /actuator/**
        - /public/**
      authentication:
        required: false
    
    - name: api-access
      paths:
        - /api/**
      authentication:
        required: true
        strategies: [jwt-strategy]
```

### 4.2 Authorization Configuration

```yaml
# Authorization configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayAuthorization
metadata:
  name: rbac-authorization
  namespace: tigateway
spec:
  roles:
    - name: GUEST
      description: "Guest role"
      permissions:
        - resource: public
          actions: [read]
    
    - name: USER
      description: "Regular user role"
      permissions:
        - resource: user
          actions: [read, write]
        - resource: profile
          actions: [read, write]
    
    - name: ADMIN
      description: "Administrator role"
      permissions:
        - resource: "*"
          actions: [read, write, delete]
  
  policies:
    - name: user-resource-policy
      description: "User resource access policy"
      rules:
        - subjects: [USER, ADMIN]
          resources: [user, profile]
          actions: [read, write]
          conditions:
            - expression: "user.id == resource.owner_id || user.roles.contains('ADMIN')"
    
    - name: admin-policy
      description: "Administrator access policy"
      rules:
        - subjects: [ADMIN]
          resources: ["*"]
          actions: [read, write, delete, admin]
  
  rules:
    - name: api-authorization
      paths:
        - /api/**
      authorization:
        required: true
        policy: user-resource-policy
    
    - name: admin-authorization
      paths:
        - /admin/**
      authorization:
        required: true
        policy: admin-policy
        roles: [ADMIN]
```

## 5. Monitoring Configuration

### 5.1 Metrics Configuration

```yaml
# Monitoring configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayMonitoring
metadata:
  name: gateway-monitoring
  namespace: tigateway
spec:
  metrics:
    enabled: true
    exporters:
      - name: prometheus
        type: prometheus
        config:
          port: 9090
          path: /metrics
          interval: 30s
          labels:
            - service
            - version
            - environment
            - instance
    
    customMetrics:
      - name: request_duration_seconds
        type: histogram
        description: "Request duration in seconds"
        labels:
          - method
          - path
          - status
          - service
        buckets: [0.1, 0.5, 1.0, 2.5, 5.0, 10.0]
      
      - name: active_connections
        type: gauge
        description: "Number of active connections"
        labels:
          - service
          - instance
      
      - name: error_rate
        type: counter
        description: "Error rate by service"
        labels:
          - service
          - error_type
          - status_code
  
  logging:
    enabled: true
    level: INFO
    format: json
    fields:
      - timestamp
      - level
      - message
      - service
      - request_id
      - user_id
      - tenant_id
      - method
      - path
      - status
      - duration
      - ip
      - user_agent
    
    outputs:
      - name: console
        type: console
        config:
          enabled: true
          level: INFO
      
      - name: file
        type: file
        config:
          enabled: true
          path: /app/logs/tigateway.log
          level: DEBUG
          maxSize: 100MB
          maxFiles: 10
          maxAge: 30d
  
  tracing:
    enabled: true
    provider: jaeger
    config:
      endpoint: http://jaeger:14268/api/traces
      sampleRate: 0.1
      serviceName: tigateway
      tags:
        - key: environment
          value: production
        - key: version
          value: v1.0.0
    
    rules:
      - name: api-tracing
        paths:
          - /api/**
        sampleRate: 0.1
        includeHeaders:
          - X-Request-ID
          - X-User-ID
          - X-Tenant-ID
        excludeHeaders:
          - Authorization
          - Cookie
```

## 6. Service Discovery Configuration

### 6.1 Kubernetes Service Discovery Configuration

```yaml
# Service discovery configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayServiceDiscovery
metadata:
  name: kubernetes-discovery
  namespace: tigateway
spec:
  registries:
    - name: kubernetes
      type: kubernetes
      config:
        namespace: backend
        labelSelector: "app=backend"
        healthCheck:
          enabled: true
          path: /health
          interval: 10s
          timeout: 5s
  
  serviceMappings:
    - name: user-service
      registries: [kubernetes]
      loadBalancer:
        type: round_robin
        healthCheck:
          enabled: true
          path: /health
          interval: 10s
          timeout: 5s
          unhealthyThreshold: 3
      circuitBreaker:
        enabled: true
        failureThreshold: 5
        timeout: 30s
        halfOpenMaxCalls: 3
```

## 7. Configuration Templates

### 7.1 Microservice Route Template

```yaml
# Configuration template
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigTemplate
metadata:
  name: microservice-template
  namespace: tigateway
spec:
  template:
    routes:
      - id: "{{.serviceName}}-route"
        target:
          service: "{{.serviceName}}"
          namespace: "{{.namespace}}"
        match:
          path: "/api/{{.serviceName}}/**"
        filters:
          - name: StripPrefix
            config:
              parts: 2
          - name: AddRequestHeader
            config:
              name: X-Service
              value: "{{.serviceName}}"
  
  parameters:
    - name: serviceName
      required: true
      type: string
      description: "Service name"
    - name: namespace
      required: true
      type: string
      description: "Namespace"
    - name: version
      required: false
      type: string
      default: "v1"
      description: "Service version"

---
# Using template to create configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: user-service-routes
  namespace: tigateway
spec:
  template: microservice-template
  parameters:
    serviceName: user-service
    namespace: backend
    version: v1
```

## 8. Environment-Specific Configuration

### 8.1 Development Environment Configuration

```yaml
# Development environment configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayEnvironmentConfig
metadata:
  name: dev-config
  namespace: tigateway
spec:
  environment: development
  overrides:
    logging:
      level: DEBUG
    metrics:
      sampleRate: 1.0
    security:
      strictMode: false
    routes:
      - id: dev-route
        target:
          service: dev-backend
          namespace: dev
        match:
          path: /dev/**
        filters:
          - name: StripPrefix
            config:
              parts: 1
```

### 8.2 Production Environment Configuration

```yaml
# Production environment configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayEnvironmentConfig
metadata:
  name: prod-config
  namespace: tigateway
spec:
  environment: production
  overrides:
    logging:
      level: WARN
    metrics:
      sampleRate: 0.1
    security:
      strictMode: true
    routes:
      - id: prod-route
        target:
          service: prod-backend
          namespace: prod
        match:
          path: /api/**
        filters:
          - name: StripPrefix
            config:
              parts: 1
          - name: RequestRateLimiter
            config:
              redis-rate-limiter:
                replenishRate: 100
                burstCapacity: 200
```

## 9. Configuration Validation

### 9.1 Configuration Check

```bash
# Check CRD configuration
kubectl get tigatewayrouteconfig -n tigateway

# Check configuration details
kubectl describe tigatewayrouteconfig user-service-routes -n tigateway

# Check configuration status
kubectl get tigatewayrouteconfig user-service-routes -n tigateway -o yaml
```

### 9.2 Configuration Testing

```bash
# Test routes
curl http://localhost:8080/api/users/123

# Check health status
curl http://localhost:8090/actuator/health

# Check metrics
curl http://localhost:9090/metrics
```

---

**Related Documentation**:
- [CRD Configuration Design](../configuration/crd-configuration-design.md)
- [Advanced Configuration Examples](./crd-advanced-config.md)
- [Quick Start Guide](../getting-started/quick-start.md)
- [Troubleshooting Guide](./troubleshooting.md)
