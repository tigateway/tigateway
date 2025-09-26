# TiGateway CRD 基础配置示例

## 概述

本文档提供了基于 Kubernetes Custom Resource Definitions (CRD) 的 TiGateway 基础配置示例，展示如何使用声明式配置管理 TiGateway 网关。

## 1. 基础路由配置

### 1.1 简单路由配置

```yaml
# 使用 TiGatewayRouteConfig CRD
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: user-service-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: user-service-group
      description: "用户服务路由组"
      routes:
        - id: user-service
          description: "用户服务路由"
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

### 1.2 负载均衡路由配置

```yaml
# 使用 TiGatewayRouteConfig CRD 配置负载均衡
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: user-service-lb-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: user-service-lb-group
      description: "用户服务负载均衡路由组"
      routes:
        - id: user-service-lb
          description: "用户服务负载均衡路由"
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

### 1.3 多服务路由配置

```yaml
# 多服务路由配置
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: microservices-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: microservices-group
      description: "微服务路由组"
      routes:
        # 用户服务路由
        - id: user-service
          description: "用户服务路由"
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
        
        # 订单服务路由
        - id: order-service
          description: "订单服务路由"
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
        
        # 商品服务路由
        - id: product-service
          description: "商品服务路由"
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

## 2. 高级路由配置

### 2.1 带权重的路由配置

```yaml
# 带权重的路由配置
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: weighted-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: weighted-group
      description: "权重路由组"
      routes:
        - id: weighted-route-80
          description: "80% 流量路由"
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
          description: "20% 流量路由"
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

### 2.2 带时间限制的路由配置

```yaml
# 带时间限制的路由配置
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: time-based-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: time-based-group
      description: "时间限制路由组"
      routes:
        - id: maintenance-route
          description: "维护时间路由"
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

### 2.3 带请求头限制的路由配置

```yaml
# 带请求头限制的路由配置
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: header-based-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: header-based-group
      description: "请求头限制路由组"
      routes:
        - id: vip-route
          description: "VIP 用户路由"
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

## 3. 过滤器配置

### 3.1 请求头过滤器配置

```yaml
# 请求头过滤器配置
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

### 3.2 限流过滤器配置

```yaml
# 限流过滤器配置
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

### 3.3 熔断器过滤器配置

```yaml
# 熔断器过滤器配置
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

## 4. 安全配置

### 4.1 认证配置

```yaml
# 认证配置
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

### 4.2 授权配置

```yaml
# 授权配置
apiVersion: tigateway.cn/v1
kind: TiGatewayAuthorization
metadata:
  name: rbac-authorization
  namespace: tigateway
spec:
  roles:
    - name: GUEST
      description: "访客角色"
      permissions:
        - resource: public
          actions: [read]
    
    - name: USER
      description: "普通用户角色"
      permissions:
        - resource: user
          actions: [read, write]
        - resource: profile
          actions: [read, write]
    
    - name: ADMIN
      description: "管理员角色"
      permissions:
        - resource: "*"
          actions: [read, write, delete]
  
  policies:
    - name: user-resource-policy
      description: "用户资源访问策略"
      rules:
        - subjects: [USER, ADMIN]
          resources: [user, profile]
          actions: [read, write]
          conditions:
            - expression: "user.id == resource.owner_id || user.roles.contains('ADMIN')"
    
    - name: admin-policy
      description: "管理员访问策略"
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

## 5. 监控配置

### 5.1 指标配置

```yaml
# 监控配置
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

## 6. 服务发现配置

### 6.1 Kubernetes 服务发现配置

```yaml
# 服务发现配置
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

## 7. 配置模板

### 7.1 微服务路由模板

```yaml
# 配置模板
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
# 使用模板创建配置
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

## 8. 环境特定配置

### 8.1 开发环境配置

```yaml
# 开发环境配置
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

### 8.2 生产环境配置

```yaml
# 生产环境配置
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

## 9. 配置验证

### 9.1 配置检查

```bash
# 检查 CRD 配置
kubectl get tigatewayrouteconfig -n tigateway

# 检查配置详情
kubectl describe tigatewayrouteconfig user-service-routes -n tigateway

# 检查配置状态
kubectl get tigatewayrouteconfig user-service-routes -n tigateway -o yaml
```

### 9.2 配置测试

```bash
# 测试路由
curl http://localhost:8080/api/users/123

# 检查健康状态
curl http://localhost:8090/actuator/health

# 检查指标
curl http://localhost:9090/metrics
```

---

**相关文档**:
- [CRD 配置抽象设计](../configuration/crd-configuration-design.md)
- [高级配置示例](./advanced-config.md)
- [快速开始](./quick-start.md)
- [故障排除](./troubleshooting.md)
