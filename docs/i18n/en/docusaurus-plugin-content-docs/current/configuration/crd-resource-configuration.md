# TiGateway CRD Resource Configuration Documentation

## Overview

This document details the configuration methods for various CRD resources in TiGateway, including route configuration, filter configuration, predicate configuration, security configuration, etc. Based on Spring Cloud Gateway's core concepts, it provides a complete CRD configuration guide.

## 1. Basic Concepts

### 1.1 Spring Cloud Gateway Core Concepts

In TiGateway, we build CRD configurations based on Spring Cloud Gateway's core concepts:

- **Route**: The basic building block of the gateway, defined by ID, target URI, predicate collection, and filter collection
- **Predicate**: Java 8 Function Predicate, used to match HTTP requests
- **Filter**: GatewayFilter instance, used to modify requests and responses

### 1.2 CRD Resource Types

TiGateway provides the following CRD resource types:

| CRD Type | Purpose | Corresponding Spring Cloud Gateway Concept |
|----------|---------|-------------------------------------------|
| `TiGatewayRouteConfig` | Route Configuration | Route Definition |
| `TiGatewayCustomFilter` | Custom Filter | Gateway Filter Factory |
| `TiGatewaySecurity` | Security Configuration | Security Configuration |
| `TiGatewayMonitoring` | Monitoring Configuration | Metrics & Monitoring |

## 2. Route Configuration (TiGatewayRouteConfig)

### 2.1 Basic Route Configuration

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: basic-routes
  namespace: tigateway
spec:
  routes:
    - id: user-service-route
      uri: http://user-service:8080
      predicates:
        - Path=/api/users/**
      filters:
        - StripPrefix=2
      order: 0
      metadata:
        description: "User service route"
        tags: ["user", "api"]
```

### 2.2 Advanced Route Configuration

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: advanced-routes
  namespace: tigateway
spec:
  # 路由组配置
  routeGroups:
    - name: microservices-group
      description: "微服务路由组"
      labels:
        environment: production
        tier: backend
      
      # 服务发现配置
      serviceDiscovery:
        type: kubernetes
        namespace: microservices
        labelSelector:
          app.kubernetes.io/part-of: microservices
      
      # 负载均衡配置
      loadBalancing:
        strategy: round-robin
        healthCheck:
          enabled: true
          path: /health
          interval: 30s
          timeout: 5s
          retries: 3
      
      # 路由定义
      routes:
        - id: user-service-route
          description: "用户服务路由"
          # 目标服务配置
          target:
            service: user-service
            namespace: microservices
            port: 8080
            version: v1
          
          # 匹配条件
          match:
            path: "/api/users/**"
            methods: [GET, POST, PUT, DELETE]
            headers:
              - name: X-API-Version
                value: v1
            query:
              - name: format
                values: [json, xml]
            time:
              start: "2024-01-01T00:00:00Z"
              end: "2024-12-31T23:59:59Z"
          
          # 流量控制
          traffic:
            weight: 80
            priority: 100
            canary:
              enabled: false
              percentage: 0
          
          # 过滤器链
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Service-Name
                value: user-service
            - name: CircuitBreaker
              config:
                name: user-service-cb
                fallbackUri: forward:/fallback/user-service
                failureThreshold: 5
                waitDurationInOpenState: 60s
                successThreshold: 3
          
          # 路由元数据
          metadata:
            description: "用户服务路由配置"
            tags: ["user", "api", "microservice"]
            version: v1.0.0
            owner: platform-team
            created: "2024-01-01T00:00:00Z"
            updated: "2024-01-01T00:00:00Z"
```

### 2.3 路由模板配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: route-templates
  namespace: tigateway
spec:
  # 路由模板定义
  routeTemplates:
    - name: standard-api-template
      description: "标准API路由模板"
      match:
        path: "/api/{service}/**"
        methods: [GET, POST, PUT, DELETE]
      filters:
        - name: StripPrefix
          config:
            parts: 2
        - name: AddRequestHeader
          config:
            name: X-Service-Name
            value: "{service}"
        - name: CircuitBreaker
          config:
            name: "{service}-cb"
            fallbackUri: forward:/fallback/{service}
            failureThreshold: 5
            waitDurationInOpenState: 60s
      metadata:
        tags: ["template", "api"]
  
  # 应用模板的路由
  routes:
    - template: standard-api-template
      target:
        service: "{service}-service"
      match:
        service: user
      metadata:
        description: "用户服务路由（基于模板）"
    
    - template: standard-api-template
      target:
        service: "{service}-service"
      match:
        service: order
      metadata:
        description: "订单服务路由（基于模板）"
```

## 3. 自定义过滤器配置 (TiGatewayCustomFilter)

### 3.1 过滤器定义

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayCustomFilter
metadata:
  name: business-logic-filter
  namespace: tigateway
spec:
  # 过滤器基本信息
  filter:
    name: BusinessLogicFilter
    description: "业务逻辑过滤器"
    version: v1.0.0
    author: platform-team
    category: business
    
    # 配置模式定义
    configSchema:
      type: object
      properties:
        businessRules:
          type: array
          items:
            type: object
            properties:
              condition:
                type: string
                description: "业务规则条件表达式"
              action:
                type: string
                enum: [ALLOW, DENY, TRANSFORM]
                description: "规则动作"
              transform:
                type: object
                description: "转换配置"
        enableLogging:
          type: boolean
          default: true
          description: "是否启用日志"
        logLevel:
          type: string
          enum: [DEBUG, INFO, WARN, ERROR]
          default: INFO
          description: "日志级别"
      required: [businessRules]
  
  # 过滤器实现
  implementation:
    type: java
    className: com.tigateway.filter.BusinessLogicFilter
    dependencies:
      - groupId: com.tigateway
        artifactId: tigateway-core
        version: 1.0.0
      - groupId: org.springframework
        artifactId: spring-webflux
        version: 5.3.21
    resources:
      - name: business-rules.yaml
        path: /config/business-rules.yaml
        type: configmap
  
  # 使用示例
  usage:
    routes:
      - name: business-api-route
        filters:
          - name: BusinessLogicFilter
            config:
              businessRules:
                - condition: "request.header['X-User-Type'] == 'PREMIUM'"
                  action: ALLOW
                - condition: "request.header['X-User-Type'] == 'BASIC'"
                  action: TRANSFORM
                  transform:
                    addHeader:
                      X-Upgrade-Prompt: "true"
              enableLogging: true
              logLevel: INFO
```

### 3.2 过滤器工厂配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayCustomFilter
metadata:
  name: custom-filter-factory
  namespace: tigateway
spec:
  filter:
    name: CustomFilterFactory
    description: "自定义过滤器工厂"
    version: v1.0.0
    
    # 工厂配置模式
    factoryConfigSchema:
      type: object
      properties:
        defaultConfig:
          type: object
          properties:
            timeout:
              type: integer
              default: 5000
              description: "默认超时时间（毫秒）"
            retries:
              type: integer
              default: 3
              description: "默认重试次数"
        globalSettings:
          type: object
          properties:
            enableMetrics:
              type: boolean
              default: true
            enableTracing:
              type: boolean
              default: true
  
  implementation:
    type: java
    className: com.tigateway.filter.CustomFilterFactory
    factoryMethod: createFilter
    dependencies:
      - groupId: com.tigateway
        artifactId: tigateway-core
        version: 1.0.0
```

## 4. 安全配置 (TiGatewaySecurity)

### 4.1 认证配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewaySecurity
metadata:
  name: authentication-config
  namespace: tigateway
spec:
  # 认证提供者配置
  authentication:
    providers:
      - name: jwt
        type: JWT
        enabled: true
        config:
          secretKey: ${JWT_SECRET}
          issuer: tigateway
          audience: api-users
          expiration: 3600s
          algorithm: HS256
          headerName: Authorization
          tokenPrefix: Bearer
      
      - name: oauth2
        type: OAuth2
        enabled: true
        config:
          issuerUri: ${OAUTH2_ISSUER_URI}
          clientId: ${OAUTH2_CLIENT_ID}
          clientSecret: ${OAUTH2_CLIENT_SECRET}
          scope: openid,profile,email
          authorizationGrantType: authorization_code
          redirectUri: "{baseUrl}/login/oauth2/code/{registrationId}"
      
      - name: api-key
        type: APIKey
        enabled: true
        config:
          headerName: X-API-Key
          queryParamName: api_key
          secretKey: ${API_KEY_SECRET}
    
    # 认证策略
    policies:
      - name: public-access
        description: "公开访问策略"
        rules:
          - path: "/api/public/**"
            methods: [GET, POST]
            authentication: none
      
      - name: authenticated-access
        description: "认证访问策略"
        rules:
          - path: "/api/secure/**"
            methods: [GET, POST, PUT, DELETE]
            authentication: required
            providers: [jwt, oauth2]
      
      - name: api-key-access
        description: "API Key访问策略"
        rules:
          - path: "/api/external/**"
            methods: [GET, POST]
            authentication: required
            providers: [api-key]
```

### 4.2 授权配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewaySecurity
metadata:
  name: authorization-config
  namespace: tigateway
spec:
  # 授权配置
  authorization:
    # 角色定义
    roles:
      - name: ADMIN
        description: "系统管理员"
        permissions:
          - resource: "*"
            actions: ["*"]
      
      - name: USER
        description: "普通用户"
        permissions:
          - resource: "user"
            actions: [read, update]
            conditions:
              - "subject.id == resource.owner"
      
      - name: API_USER
        description: "API用户"
        permissions:
          - resource: "api"
            actions: [read]
            conditions:
              - "request.header['X-API-Version'] == 'v1'"
    
    # 权限策略
    policies:
      - name: user-data-access
        description: "用户数据访问策略"
        rules:
          - path: "/api/users/{userId}/**"
            methods: [GET, PUT, DELETE]
            authorization:
              requiredRoles: [USER, ADMIN]
              conditions:
                - "subject.roles.contains('ADMIN') || subject.id == path.userId"
      
      - name: admin-only-access
        description: "管理员专用访问策略"
        rules:
          - path: "/api/admin/**"
            methods: [GET, POST, PUT, DELETE]
            authorization:
              requiredRoles: [ADMIN]
```

### 4.3 安全过滤器配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewaySecurity
metadata:
  name: security-filters
  namespace: tigateway
spec:
  # 安全过滤器配置
  securityFilters:
    - name: SecurityHeadersFilter
      order: -1000
      config:
        headers:
          X-Content-Type-Options: nosniff
          X-Frame-Options: DENY
          X-XSS-Protection: "1; mode=block"
          Strict-Transport-Security: "max-age=31536000; includeSubDomains"
          Content-Security-Policy: "default-src 'self'"
    
    - name: CorsFilter
      order: -900
      config:
        allowedOrigins: ["https://example.com", "https://app.example.com"]
        allowedMethods: [GET, POST, PUT, DELETE, OPTIONS]
        allowedHeaders: ["*"]
        allowCredentials: true
        maxAge: 3600
    
    - name: RateLimitingFilter
      order: -800
      config:
        keyResolver: ip
        replenishRate: 100
        burstCapacity: 200
        requestedTokens: 1
        redis:
          host: redis-service
          port: 6379
          database: 0
```

## 5. 监控配置 (TiGatewayMonitoring)

### 5.1 指标配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayMonitoring
metadata:
  name: metrics-config
  namespace: tigateway
spec:
  # 指标配置
  metrics:
    # 系统指标
    system:
      enabled: true
      collection:
        jvm: true
        system: true
        custom: true
      interval: 30s
    
    # 业务指标
    business:
      enabled: true
      customMetrics:
        - name: api_request_count
          type: counter
          description: "API请求计数"
          labels: [service, method, status]
        
        - name: api_response_time
          type: histogram
          description: "API响应时间"
          buckets: [0.1, 0.5, 1.0, 2.0, 5.0]
          labels: [service, method]
        
        - name: active_connections
          type: gauge
          description: "活跃连接数"
          labels: [service]
    
    # 指标导出
    export:
      prometheus:
        enabled: true
        path: /actuator/prometheus
        port: 8090
        format: prometheus
      
      custom:
        enabled: true
        endpoints:
          - name: metrics-endpoint
            path: /metrics
            format: json
            port: 8090
```

### 5.2 日志配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayMonitoring
metadata:
  name: logging-config
  namespace: tigateway
spec:
  # 日志配置
  logging:
    # 日志级别
    level:
      root: INFO
      com.tigateway: DEBUG
      org.springframework.cloud.gateway: INFO
      org.springframework.web.reactive: WARN
    
    # 日志格式
    format:
      pattern: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
      includeTraceId: true
      includeSpanId: true
      includeRequestId: true
    
    # 日志输出
    appenders:
      - name: console
        type: console
        enabled: true
        level: INFO
      
      - name: file
        type: file
        enabled: true
        fileName: /var/log/tigateway/application.log
        maxFileSize: 100MB
        maxHistory: 30
        level: INFO
      
      - name: access
        type: file
        enabled: true
        fileName: /var/log/tigateway/access.log
        maxFileSize: 200MB
        maxHistory: 7
        level: INFO
    
    # 结构化日志
    structured:
      enabled: true
      format: json
      fields:
        - name: timestamp
          value: "${timestamp}"
        - name: level
          value: "${level}"
        - name: message
          value: "${message}"
        - name: service
          value: "tigateway"
        - name: version
          value: "${tigateway.version}"
        - name: traceId
          value: "${traceId}"
        - name: spanId
          value: "${spanId}"
```

### 5.3 链路追踪配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayMonitoring
metadata:
  name: tracing-config
  namespace: tigateway
spec:
  # 链路追踪配置
  tracing:
    enabled: true
    provider: zipkin
    config:
      endpoint: http://zipkin-service:9411/api/v2/spans
      serviceName: tigateway
      samplingRate: 0.1
      includeHeaders: true
      includeBody: false
      maxBodySize: 1024
    
    # 自定义追踪
    customTracing:
      enabled: true
      spans:
        - name: route-processing
          description: "路由处理时间"
          tags:
            - name: route.id
              value: "${route.id}"
            - name: route.uri
              value: "${route.uri}"
        
        - name: filter-processing
          description: "过滤器处理时间"
          tags:
            - name: filter.name
              value: "${filter.name}"
            - name: filter.order
              value: "${filter.order}"
```

## 6. 配置验证

### 6.1 配置验证规则

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: validated-routes
  namespace: tigateway
spec:
  # 配置验证
  validation:
    enabled: true
    rules:
      - name: route-id-required
        description: "路由ID必须存在"
        condition: "route.id != null && route.id != ''"
        severity: error
      
      - name: uri-required
        description: "目标URI必须存在"
        condition: "route.uri != null && route.uri != ''"
        severity: error
      
      - name: predicates-required
        description: "至少需要一个谓词"
        condition: "route.predicates != null && route.predicates.size() > 0"
        severity: error
      
      - name: filter-config-valid
        description: "过滤器配置必须有效"
        condition: "filter.config != null && filter.config.isValid()"
        severity: warning
  
  routes:
    - id: validated-route
      uri: https://example.org
      predicates:
        - Path=/api/validated/**
      filters:
        - StripPrefix=2
```

## 7. 配置管理最佳实践

### 7.1 环境隔离

```yaml
# 生产环境配置
apiVersion: v1
kind: Namespace
metadata:
  name: tigateway-prod
  labels:
    environment: production
    tier: gateway

---
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: production-routes
  namespace: tigateway-prod
spec:
  routes:
    - id: prod-route
      uri: https://prod.example.org
      predicates:
        - Path=/api/prod/**
      filters:
        - StripPrefix=2
```

### 7.2 配置版本管理

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: versioned-routes
  labels:
    version: v1.2.0
    app.kubernetes.io/version: v1.2.0
    app.kubernetes.io/managed-by: tigateway-operator
spec:
  routes:
    - id: versioned-route
      uri: https://example.org
      predicates:
        - Path=/api/v1/**
      filters:
        - StripPrefix=2
```

### 7.3 配置安全

```yaml
# 敏感配置使用Secret
apiVersion: v1
kind: Secret
metadata:
  name: tigateway-secrets
  namespace: tigateway
type: Opaque
data:
  jwt-secret: <base64-encoded-secret>
  oauth2-client-secret: <base64-encoded-secret>

---
# 引用Secret的配置
apiVersion: tigateway.cn/v1
kind: TiGatewaySecurity
metadata:
  name: security-config
  namespace: tigateway
spec:
  authentication:
    providers:
      - name: jwt
        type: JWT
        config:
          secretKey: ${JWT_SECRET}  # 从Secret中引用
```

---

**Related Documentation**:
- [CRD Configuration Design](./crd-configuration-design.md)
- [CRD Filter Configuration](./crd-filter-configuration.md)
- [CRD Predicate Configuration](./crd-predicate-configuration.md)
- [CRD Typed Design](./crd-typed-design.md)
