# TiGateway CRD 高级配置示例

## 概述

本文档提供了基于 Kubernetes Custom Resource Definitions (CRD) 的 TiGateway 高级配置示例，包括复杂路由配置、自定义过滤器、性能优化、安全配置等企业级场景。所有配置都基于 CRD 配置抽象设计，提供声明式、云原生的配置管理方式。

## 1. 复杂路由配置

### 1.1 多条件路由匹配

```yaml
# 多条件路由匹配配置
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: complex-routing
  namespace: tigateway
spec:
  routeGroups:
    - name: complex-routing-group
      description: "复杂路由匹配组"
      routes:
        - id: multi-condition-route
          description: "多条件匹配路由"
          target:
            service: backend-service
            namespace: backend
            port: 8080
          match:
            path: /api/complex/**
            methods: [GET, POST, PUT]
            headers:
              - name: X-API-Version
                value: v2
              - name: X-User-Type
                value: PREMIUM
            query:
              - name: format
                values: [json, xml]
            time:
              start: "2024-01-01T00:00:00Z"
              end: "2024-12-31T23:59:59Z"
            weight: 100
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Complex-Route
                value: "true"
            - name: CircuitBreaker
              config:
                name: complex-route-cb
                fallbackUri: forward:/fallback/complex
                failureThreshold: 3
                timeout: 10s
```

### 1.2 金丝雀发布配置

```yaml
# 金丝雀发布配置
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: canary-deployment
  namespace: tigateway
spec:
  routeGroups:
    - name: canary-group
      description: "金丝雀发布路由组"
      routes:
        # 稳定版本路由 (90% 流量)
        - id: stable-route
          description: "稳定版本路由"
          target:
            service: user-service
            namespace: backend
            port: 8080
            version: v1
          match:
            path: /api/users/**
            weight: 90
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Service-Version
                value: "v1"
        
        # 金丝雀版本路由 (10% 流量)
        - id: canary-route
          description: "金丝雀版本路由"
          target:
            service: user-service
            namespace: backend
            port: 8080
            version: v2
          match:
            path: /api/users/**
            weight: 10
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Service-Version
                value: "v2"
            - name: AddRequestHeader
              config:
                name: X-Canary-Test
                value: "true"
```

### 1.3 A/B 测试配置

```yaml
# A/B 测试配置
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: ab-testing
  namespace: tigateway
spec:
  routeGroups:
    - name: ab-testing-group
      description: "A/B 测试路由组"
      routes:
        # A 版本路由 (50% 流量)
        - id: version-a-route
          description: "A 版本路由"
          target:
            service: user-service
            namespace: backend
            port: 8080
            version: v1
          match:
            path: /api/users/**
            headers:
              - name: X-User-ID
                pattern: ".*[02468]$"  # 用户ID以偶数结尾
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Test-Version
                value: "A"
        
        # B 版本路由 (50% 流量)
        - id: version-b-route
          description: "B 版本路由"
          target:
            service: user-service
            namespace: backend
            port: 8080
            version: v2
          match:
            path: /api/users/**
            headers:
              - name: X-User-ID
                pattern: ".*[13579]$"  # 用户ID以奇数结尾
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Test-Version
                value: "B"
```

## 2. 自定义过滤器配置

### 2.1 业务逻辑过滤器

```yaml
# 业务逻辑过滤器配置
apiVersion: tigateway.cn/v1
kind: TiGatewayCustomFilter
metadata:
  name: business-logic-filter
  namespace: tigateway
spec:
  filter:
    name: BusinessLogicFilter
    version: v1.0.0
    description: "业务逻辑过滤器"
    
    config:
      # 业务规则配置
      businessRules:
        - name: vip-user-check
          condition: "user.role == 'VIP'"
          action: "add-header"
          params:
            header: X-VIP-User
            value: "true"
        
        - name: rate-limit-by-tier
          condition: "user.tier == 'PREMIUM'"
          action: "rate-limit"
          params:
            requestsPerMinute: 200
            burstCapacity: 400
        
        - name: geo-restriction
          condition: "request.geo.country not in ['US', 'CA', 'GB']"
          action: "block"
          params:
            reason: "Geographic restriction"
            statusCode: 403
        
        - name: time-based-access
          condition: "request.time.hour < 9 || request.time.hour > 18"
          action: "redirect"
          params:
            url: "/maintenance"
            statusCode: 302
      
      # 数据源配置
      dataSources:
        - name: user-service
          type: http
          config:
            url: "http://user-service:8080"
            timeout: 5s
            retries: 3
            cache:
              enabled: true
              ttl: 300s
        
        - name: redis-cache
          type: redis
          config:
            host: redis-server
            port: 6379
            database: 1
            ttl: 300s
      
      # 缓存配置
      cache:
        enabled: true
        ttl: 300s
        maxSize: 1000
        keyGenerator: "user.id + request.path"
      
      # 监控配置
      monitoring:
        enabled: true
        metrics:
          - name: business_rule_executions
            type: counter
            labels: [rule_name, result]
          - name: business_rule_duration
            type: histogram
            labels: [rule_name]
  
  # 过滤器部署配置
  deployment:
    replicas: 3
    resources:
      requests:
        memory: "128Mi"
        cpu: "100m"
      limits:
        memory: "256Mi"
        cpu: "200m"
    
    # 健康检查配置
    healthCheck:
      enabled: true
      path: /health
      interval: 10s
      timeout: 5s
      failureThreshold: 3
```

### 2.2 数据转换过滤器

```yaml
# 数据转换过滤器配置
apiVersion: tigateway.cn/v1
kind: TiGatewayCustomFilter
metadata:
  name: data-transform-filter
  namespace: tigateway
spec:
  filter:
    name: DataTransformFilter
    version: v1.0.0
    description: "数据转换过滤器"
    
    config:
      # 请求转换配置
      requestTransform:
        enabled: true
        type: json
        rules:
          - path: $.user.id
            transform: toUpperCase
          - path: $.user.email
            transform: toLowerCase
          - path: $.user.phone
            transform: formatPhone
            params:
              format: "+1-{area}-{exchange}-{number}"
          - path: $.user.address
            transform: normalizeAddress
            params:
              country: "US"
      
      # 响应转换配置
      responseTransform:
        enabled: true
        type: json
        rules:
          - path: $.data
            transform: addMetadata
            params:
              metadata:
                processedBy: "tigateway"
                timestamp: "${timestamp}"
                version: "v1.0.0"
          - path: $.data.user
            transform: maskSensitiveData
            params:
              fields: [ssn, creditCard]
              mask: "***"
      
      # 转换函数配置
      transformFunctions:
        - name: toUpperCase
          type: string
          function: "value.toUpperCase()"
        
        - name: toLowerCase
          type: string
          function: "value.toLowerCase()"
        
        - name: formatPhone
          type: string
          function: "formatPhoneNumber(value, params.format)"
        
        - name: normalizeAddress
          type: object
          function: "normalizeAddress(value, params.country)"
        
        - name: addMetadata
          type: object
          function: "addMetadata(value, params.metadata)"
        
        - name: maskSensitiveData
          type: object
          function: "maskSensitiveFields(value, params.fields, params.mask)"
      
      # 性能配置
      performance:
        enabled: true
        maxPayloadSize: 1048576  # 1MB
        timeout: 5000  # 5s
        cache:
          enabled: true
          ttl: 300s
          maxSize: 1000
```

### 2.3 缓存过滤器

```yaml
# 缓存过滤器配置
apiVersion: tigateway.cn/v1
kind: TiGatewayCustomFilter
metadata:
  name: cache-filter
  namespace: tigateway
spec:
  filter:
    name: CacheFilter
    version: v1.0.0
    description: "缓存过滤器"
    
    config:
      # 缓存策略配置
      strategies:
        - name: user-profile-cache
          type: redis
          config:
            host: redis-server
            port: 6379
            database: 0
            ttl: 3600s
            keyPrefix: "user:profile:"
            keyGenerator: "user.id"
            conditions:
              - path: /api/users/profile/**
              - method: GET
              - header: X-Cache-Enabled
                value: "true"
        
        - name: product-cache
          type: local
          config:
            ttl: 1800s
            maxSize: 10000
            keyPrefix: "product:"
            keyGenerator: "request.path + request.query"
            conditions:
              - path: /api/products/**
              - method: GET
        
        - name: api-response-cache
          type: redis
          config:
            host: redis-server
            port: 6379
            database: 1
            ttl: 300s
            keyPrefix: "api:response:"
            keyGenerator: "request.path + request.query + user.id"
            conditions:
              - path: /api/cached/**
              - method: GET
      
      # 缓存控制配置
      cacheControl:
        enabled: true
        defaultTTL: 300s
        maxTTL: 3600s
        minTTL: 60s
        headers:
          - name: Cache-Control
            value: "public, max-age=300"
          - name: X-Cache-Status
            value: "HIT"
      
      # 缓存失效配置
      invalidation:
        enabled: true
        strategies:
          - name: time-based
            type: ttl
            config:
              ttl: 3600s
          - name: event-based
            type: event
            config:
              events:
                - user.update
                - product.update
                - order.create
          - name: manual
            type: manual
            config:
              endpoint: /admin/cache/invalidate
              authentication: required
      
      # 监控配置
      monitoring:
        enabled: true
        metrics:
          - name: cache_hits
            type: counter
            labels: [strategy, key]
          - name: cache_misses
            type: counter
            labels: [strategy, key]
          - name: cache_operations
            type: histogram
            labels: [strategy, operation]
```

## 3. 性能优化配置

### 3.1 连接池优化

```yaml
# 连接池优化配置
apiVersion: tigateway.cn/v1
kind: TiGatewayPerformanceConfig
metadata:
  name: connection-pool-optimization
  namespace: tigateway
spec:
  # HTTP 客户端配置
  httpClient:
    connectTimeout: 1000
    responseTimeout: 5s
    pool:
      type: elastic
      maxConnections: 500
      maxIdleTime: 30s
      maxLifeTime: 60s
      pendingAcquireTimeout: 60s
      pendingAcquireMaxCount: -1
      evictInBackground: true
      evictPeriod: 30s
    
    # SSL 配置
    ssl:
      handshakeTimeout: 10000
      closeNotifyFlushTimeout: 3000
      closeNotifyReadTimeout: 0
    
    # 压缩配置
    compression:
      enabled: true
      minResponseSize: 1024
      mimeTypes:
        - text/plain
        - text/html
        - text/css
        - text/javascript
        - application/json
        - application/javascript
        - application/xml
  
  # 负载均衡配置
  loadBalancer:
    cache:
      ttl: 5s
      capacity: 256
    healthCheck:
      enabled: true
      interval: 10s
      timeout: 5s
      failureThreshold: 3
      successThreshold: 2
  
  # 缓存配置
  cache:
    enabled: true
    type: redis
    config:
      host: redis-server
      port: 6379
      database: 0
      ttl: 300s
      maxSize: 10000
      evictionPolicy: LRU
  
  # 线程池配置
  threadPool:
    coreSize: 10
    maxSize: 50
    queueCapacity: 100
    keepAliveTime: 60s
    threadNamePrefix: "tigateway-"
```

### 3.2 内存优化

```yaml
# 内存优化配置
apiVersion: tigateway.cn/v1
kind: TiGatewayPerformanceConfig
metadata:
  name: memory-optimization
  namespace: tigateway
spec:
  # JVM 配置
  jvm:
    heap:
      initial: 512m
      max: 2g
    gc:
      type: G1GC
      maxGCPauseMillis: 200
      heapRegionSize: 16m
      concurrentGCThreads: 4
      parallelGCThreads: 8
  
  # 内存管理配置
  memory:
    buffer:
      maxSize: 64k
      direct: true
    cache:
      maxSize: 1000
      evictionPolicy: LRU
      ttl: 300s
  
  # 对象池配置
  objectPool:
    enabled: true
    maxSize: 1000
    minSize: 10
    evictionInterval: 30s
    evictionPolicy: LRU
```

## 4. 高级安全配置

### 4.1 多租户安全配置

```yaml
# 多租户安全配置
apiVersion: tigateway.cn/v1
kind: TiGatewaySecurity
metadata:
  name: multi-tenant-security
  namespace: tigateway
spec:
  # 租户隔离配置
  tenantIsolation:
    enabled: true
    strategy: namespace
    config:
      tenantHeader: X-Tenant-ID
      defaultTenant: default
      tenantValidation:
        enabled: true
        endpoint: http://tenant-service:8080/validate
        timeout: 5s
        cache:
          enabled: true
          ttl: 300s
  
  # 数据隔离配置
  dataIsolation:
    enabled: true
    strategies:
      - name: database
        type: database
        config:
          tenantColumn: tenant_id
          isolationLevel: row
      - name: cache
        type: cache
        config:
          keyPrefix: "tenant:{tenantId}:"
          isolationLevel: key
      - name: storage
        type: storage
        config:
          pathPrefix: "/tenants/{tenantId}/"
          isolationLevel: path
  
  # 权限控制配置
  accessControl:
    enabled: true
    policies:
      - name: tenant-resource-policy
        description: "租户资源访问策略"
        rules:
          - subjects: [USER, ADMIN]
            resources: ["*"]
            actions: [read, write, delete]
            conditions:
              - expression: "user.tenant == resource.tenant"
      
      - name: cross-tenant-policy
        description: "跨租户访问策略"
        rules:
          - subjects: [SUPER_ADMIN]
            resources: ["*"]
            actions: [read, write, delete, admin]
            conditions:
              - expression: "user.roles.contains('SUPER_ADMIN')"
  
  # 审计配置
  auditing:
    enabled: true
    events:
      - name: tenant-access
        description: "租户访问事件"
        fields:
          - tenantId
          - userId
          - resource
          - action
          - timestamp
          - ip
          - userAgent
      - name: cross-tenant-access
        description: "跨租户访问事件"
        fields:
          - sourceTenant
          - targetTenant
          - userId
          - resource
          - action
          - timestamp
          - ip
          - userAgent
    
    outputs:
      - name: audit-log
        type: file
        config:
          path: /app/logs/audit.log
          format: json
          rotation:
            maxSize: 100MB
            maxFiles: 10
            maxAge: 30d
      
      - name: audit-database
        type: database
        config:
          url: jdbc:postgresql://audit-db:5432/audit
          table: audit_logs
          batchSize: 100
          flushInterval: 10s
```

### 4.2 高级认证配置

```yaml
# 高级认证配置
apiVersion: tigateway.cn/v1
kind: TiGatewayAuthentication
metadata:
  name: advanced-authentication
  namespace: tigateway
spec:
  # 多认证提供者配置
  providers:
    - name: jwt-provider
      type: jwt
      config:
        secret: ${JWT_SECRET}
        algorithm: HS256
        issuer: tigateway
        audience: tigateway-clients
        expiration: 3600s
        refreshToken:
          enabled: true
          expiration: 7200s
        claims:
          - name: sub
            required: true
            type: string
          - name: roles
            required: true
            type: array
          - name: tenant
            required: true
            type: string
          - name: permissions
            required: false
            type: array
    
    - name: oauth2-provider
      type: oauth2
      config:
        issuer: ${OAUTH2_ISSUER}
        clientId: ${OAUTH2_CLIENT_ID}
        clientSecret: ${OAUTH2_CLIENT_SECRET}
        scopes: [openid, profile, email, offline_access]
        redirectUri: "{baseUrl}/oauth2/callback"
        tokenEndpoint: ${OAUTH2_TOKEN_ENDPOINT}
        userInfoEndpoint: ${OAUTH2_USERINFO_ENDPOINT}
        jwkSetUri: ${OAUTH2_JWK_SET_URI}
    
    - name: saml-provider
      type: saml
      config:
        entityId: ${SAML_ENTITY_ID}
        ssoUrl: ${SAML_SSO_URL}
        certificate: ${SAML_CERTIFICATE}
        privateKey: ${SAML_PRIVATE_KEY}
        nameIdFormat: urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress
        attributeMapping:
          email: http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress
          firstName: http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname
          lastName: http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname
          roles: http://schemas.microsoft.com/ws/2008/06/identity/claims/role
  
  # 认证策略配置
  strategies:
    - name: jwt-strategy
      provider: jwt-provider
      order: 1
      conditions:
        - header: Authorization
          pattern: "Bearer .+"
    
    - name: oauth2-strategy
      provider: oauth2-provider
      order: 2
      conditions:
        - path: /oauth2/**
    
    - name: saml-strategy
      provider: saml-provider
      order: 3
      conditions:
        - path: /saml/**
    
    - name: api-key-strategy
      provider: api-key-provider
      order: 4
      conditions:
        - header: X-API-Key
          required: true
  
  # 认证规则配置
  rules:
    - name: public-access
      paths:
        - /health
        - /actuator/**
        - /public/**
        - /oauth2/**
        - /saml/**
      authentication:
        required: false
    
    - name: api-access
      paths:
        - /api/**
      authentication:
        required: true
        strategies: [jwt-strategy, api-key-strategy]
    
    - name: admin-access
      paths:
        - /admin/**
      authentication:
        required: true
        strategies: [jwt-strategy]
        roles: [ADMIN, SUPER_ADMIN]
    
    - name: sso-access
      paths:
        - /sso/**
      authentication:
        required: true
        strategies: [saml-strategy]
  
  # 会话管理配置
  session:
    enabled: true
    type: redis
    config:
      host: redis-server
      port: 6379
      database: 2
      ttl: 3600s
      maxSessions: 10000
      cleanupInterval: 300s
  
  # 单点登录配置
  sso:
    enabled: true
    providers:
      - name: oauth2-sso
        type: oauth2
        config:
          clientId: ${SSO_CLIENT_ID}
          clientSecret: ${SSO_CLIENT_SECRET}
          authorizationUri: ${SSO_AUTHORIZATION_URI}
          tokenUri: ${SSO_TOKEN_URI}
          userInfoUri: ${SSO_USERINFO_URI}
          redirectUri: "{baseUrl}/login/oauth2/code/sso"
          scope: openid,profile,email
      
      - name: saml-sso
        type: saml
        config:
          entityId: ${SSO_SAML_ENTITY_ID}
          ssoUrl: ${SSO_SAML_SSO_URL}
          certificate: ${SSO_SAML_CERTIFICATE}
          privateKey: ${SSO_SAML_PRIVATE_KEY}
```

## 5. 监控和告警配置

### 5.1 高级监控配置

```yaml
# 高级监控配置
apiVersion: tigateway.cn/v1
kind: TiGatewayMonitoring
metadata:
  name: advanced-monitoring
  namespace: tigateway
spec:
  # 指标配置
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
            - tenant
      
      - name: influxdb
        type: influxdb
        config:
          url: http://influxdb:8086
          database: tigateway
          username: ${INFLUXDB_USERNAME}
          password: ${INFLUXDB_PASSWORD}
          interval: 60s
          batchSize: 1000
    
    # 自定义指标配置
    customMetrics:
      - name: request_duration_seconds
        type: histogram
        description: "Request duration in seconds"
        labels:
          - method
          - path
          - status
          - service
          - tenant
          - user_type
        buckets: [0.1, 0.5, 1.0, 2.5, 5.0, 10.0]
      
      - name: active_connections
        type: gauge
        description: "Number of active connections"
        labels:
          - service
          - instance
          - tenant
      
      - name: error_rate
        type: counter
        description: "Error rate by service"
        labels:
          - service
          - error_type
          - status_code
          - tenant
      
      - name: business_metrics
        type: counter
        description: "Business metrics"
        labels:
          - metric_type
          - tenant
          - user_type
          - service
  
  # 日志配置
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
      - trace_id
      - span_id
    
    # 日志输出配置
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
      
      - name: elasticsearch
        type: elasticsearch
        config:
          enabled: true
          url: http://elasticsearch:9200
          index: tigateway-logs
          level: INFO
          bulkSize: 1000
          flushInterval: 10s
      
      - name: kafka
        type: kafka
        config:
          enabled: true
          brokers: kafka:9092
          topic: tigateway-logs
          level: INFO
          batchSize: 1000
          lingerMs: 100
  
  # 追踪配置
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
        - key: tenant
          value: ${TENANT_ID}
    
    # 追踪规则配置
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
      
      - name: admin-tracing
        paths:
          - /admin/**
        sampleRate: 1.0
        includeHeaders:
          - X-Request-ID
          - X-User-ID
          - X-Admin-Action
      
      - name: error-tracing
        paths:
          - /api/**
        sampleRate: 1.0
        conditions:
          - status: [4xx, 5xx]
  
  # 告警配置
  alerts:
    enabled: true
    rules:
      - name: high-error-rate
        condition: "error_rate > 0.05"
        duration: "5m"
        severity: warning
        labels:
          service: tigateway
          alert_type: error_rate
        annotations:
          summary: "High error rate detected"
          description: "Error rate is above 5% for 5 minutes"
        actions:
          - type: webhook
            url: http://alertmanager:9093/api/v1/alerts
          - type: email
            recipients: [admin@example.com]
      
      - name: high-latency
        condition: "request_duration_seconds_p95 > 2.0"
        duration: "3m"
        severity: critical
        labels:
          service: tigateway
          alert_type: latency
        annotations:
          summary: "High latency detected"
          description: "95th percentile latency is above 2 seconds"
        actions:
          - type: webhook
            url: http://alertmanager:9093/api/v1/alerts
          - type: slack
            webhook: ${SLACK_WEBHOOK_URL}
      
      - name: service-down
        condition: "up == 0"
        duration: "1m"
        severity: critical
        labels:
          service: tigateway
          alert_type: availability
        annotations:
          summary: "Service is down"
          description: "Service has been down for 1 minute"
        actions:
          - type: webhook
            url: http://alertmanager:9093/api/v1/alerts
          - type: pagerduty
            integrationKey: ${PAGERDUTY_INTEGRATION_KEY}
```

## 6. 配置管理

### 6.1 配置版本管理

```yaml
# 配置版本管理
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigVersion
metadata:
  name: config-v1.1.0
  namespace: tigateway
spec:
  # 版本信息
  version: v1.1.0
  description: "Advanced configuration with multi-tenant support"
  createdBy: admin
  createdAt: "2024-09-23T10:00:00Z"
  
  # 配置快照
  config:
    routes:
      - id: user-service-v2
        target:
          service: user-service
          version: v2
        match:
          path: /api/users/**
        filters:
          - name: StripPrefix
            config:
              parts: 2
          - name: MultiTenantFilter
            config:
              tenantHeader: X-Tenant-ID
              isolationLevel: strict
  
  # 部署状态
  deployment:
    status: active
    replicas: 3
    lastDeployed: "2024-09-23T10:00:00Z"
    deployedBy: admin
    rollbackVersion: v1.0.0
  
  # 验证状态
  validation:
    status: passed
    validatedAt: "2024-09-23T09:55:00Z"
    validatedBy: system
    tests:
      - name: route-validation
        status: passed
      - name: security-validation
        status: passed
      - name: performance-validation
        status: passed

---
# 配置回滚
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigRollback
metadata:
  name: rollback-to-v1.0.0
  namespace: tigateway
spec:
  # 回滚目标版本
  targetVersion: v1.0.0
  
  # 回滚原因
  reason: "Performance issues with v1.1.0"
  
  # 回滚策略
  strategy: blue-green
  
  # 回滚后验证
  validation:
    enabled: true
    timeout: 300s
    healthCheck:
      enabled: true
      path: /health
      interval: 10s
    tests:
      - name: smoke-test
        enabled: true
        timeout: 60s
      - name: performance-test
        enabled: true
        timeout: 120s
```

### 6.2 配置模板管理

```yaml
# 配置模板管理
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigTemplate
metadata:
  name: enterprise-template
  namespace: tigateway
spec:
  # 模板定义
  template:
    routes:
      - id: "{{.serviceName}}-route"
        target:
          service: "{{.serviceName}}"
          namespace: "{{.namespace}}"
          version: "{{.version}}"
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
          - name: MultiTenantFilter
            config:
              tenantHeader: X-Tenant-ID
              isolationLevel: "{{.isolationLevel}}"
          - name: CircuitBreaker
            config:
              name: "{{.serviceName}}-cb"
              failureThreshold: "{{.failureThreshold}}"
              timeout: "{{.timeout}}"
    
    security:
      authentication:
        required: true
        type: jwt
        header: Authorization
      authorization:
        required: true
        roles: "{{.requiredRoles}}"
      cors:
        enabled: true
        allowedOrigins: "{{.allowedOrigins}}"
    
    monitoring:
      metrics:
        enabled: true
        labels:
          - service
          - version
          - tenant
      tracing:
        enabled: true
        sampleRate: "{{.sampleRate}}"
      logging:
        enabled: true
        level: "{{.logLevel}}"
  
  # 模板参数
  parameters:
    - name: serviceName
      required: true
      type: string
      description: "Service name"
      validation:
        pattern: "^[a-z0-9-]+$"
        minLength: 3
        maxLength: 50
    
    - name: namespace
      required: true
      type: string
      description: "Namespace"
      validation:
        pattern: "^[a-z0-9-]+$"
        minLength: 3
        maxLength: 50
    
    - name: version
      required: false
      type: string
      default: "v1"
      description: "Service version"
      validation:
        pattern: "^v[0-9]+$"
    
    - name: isolationLevel
      required: false
      type: string
      default: "strict"
      description: "Tenant isolation level"
      validation:
        enum: [strict, relaxed, none]
    
    - name: failureThreshold
      required: false
      type: integer
      default: 5
      description: "Circuit breaker failure threshold"
      validation:
        min: 1
        max: 100
    
    - name: timeout
      required: false
      type: string
      default: "30s"
      description: "Circuit breaker timeout"
      validation:
        pattern: "^[0-9]+[smh]$"
    
    - name: requiredRoles
      required: false
      type: array
      default: [USER]
      description: "Required roles for access"
    
    - name: allowedOrigins
      required: false
      type: array
      default: ["*"]
      description: "Allowed CORS origins"
    
    - name: sampleRate
      required: false
      type: number
      default: 0.1
      description: "Tracing sample rate"
      validation:
        min: 0.0
        max: 1.0
    
    - name: logLevel
      required: false
      type: string
      default: "INFO"
      description: "Log level"
      validation:
        enum: [DEBUG, INFO, WARN, ERROR]

---
# 使用企业模板创建配置
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: payment-service-routes
  namespace: tigateway
spec:
  template: enterprise-template
  parameters:
    serviceName: payment-service
    namespace: backend
    version: v2
    isolationLevel: strict
    failureThreshold: 3
    timeout: 15s
    requiredRoles: [USER, PREMIUM_USER]
    allowedOrigins: ["https://app.example.com", "https://admin.example.com"]
    sampleRate: 0.2
    logLevel: DEBUG
```

## 7. 高可用性配置

### 7.1 多区域部署配置

```yaml
# 多区域部署配置
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: multi-region-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: multi-region-group
      description: "多区域路由组"
      routes:
        # 主区域路由
        - id: primary-region-route
          description: "主区域路由"
          target:
            service: user-service
            namespace: backend
            region: us-east-1
            port: 8080
          match:
            path: /api/users/**
            weight: 80
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Region
                value: "us-east-1"
            - name: CircuitBreaker
              config:
                name: primary-region-cb
                failureThreshold: 5
                timeout: 30s
                fallbackUri: forward:/fallback/region
        
        # 备用区域路由
        - id: secondary-region-route
          description: "备用区域路由"
          target:
            service: user-service
            namespace: backend
            region: us-west-2
            port: 8080
          match:
            path: /api/users/**
            weight: 20
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: AddRequestHeader
              config:
                name: X-Region
                value: "us-west-2"
            - name: CircuitBreaker
              config:
                name: secondary-region-cb
                failureThreshold: 3
                timeout: 20s
                fallbackUri: forward:/fallback/region
```

### 7.2 故障转移配置

```yaml
# 故障转移配置
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: failover-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: failover-group
      description: "故障转移路由组"
      routes:
        - id: failover-route
          description: "故障转移路由"
          target:
            service: user-service
            namespace: backend
            port: 8080
            failover:
              enabled: true
              strategy: circuit-breaker
              fallbackServices:
                - service: user-service-backup
                  namespace: backup
                  port: 8080
                - service: user-service-cache
                  namespace: cache
                  port: 8080
          match:
            path: /api/users/**
          filters:
            - name: StripPrefix
              config:
                parts: 2
            - name: CircuitBreaker
              config:
                name: user-service-failover
                failureThreshold: 3
                timeout: 10s
                fallbackUri: forward:/fallback/user
            - name: Retry
              config:
                retries: 3
                backoff:
                  firstBackoff: 100ms
                  maxBackoff: 1s
                  multiplier: 2
```

## 8. 数据流控制配置

### 8.1 请求/响应大小限制

```yaml
# 数据流控制配置
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterChain
metadata:
  name: data-flow-control
  namespace: tigateway
spec:
  globalFilters:
    - name: RequestSizeFilter
      order: -200
      config:
        maxSize: 10485760  # 10MB
        errorResponse:
          statusCode: 413
          message: "Request entity too large"
    
    - name: ResponseSizeFilter
      order: 200
      config:
        maxSize: 52428800  # 50MB
        errorResponse:
          statusCode: 413
          message: "Response entity too large"
    
    - name: RateLimitFilter
      order: -150
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
```

### 8.2 流式处理配置

```yaml
# 流式处理配置
apiVersion: tigateway.cn/v1
kind: TiGatewayCustomFilter
metadata:
  name: streaming-filter
  namespace: tigateway
spec:
  filter:
    name: StreamingFilter
    version: v1.0.0
    description: "流式处理过滤器"
    
    config:
      # 流式处理配置
      streaming:
        enabled: true
        chunkSize: 8192  # 8KB
        bufferSize: 65536  # 64KB
        timeout: 30s
        compression:
          enabled: true
          algorithm: gzip
          level: 6
      
      # 数据转换配置
      transform:
        enabled: true
        rules:
          - name: json-streaming
            condition: "content-type == 'application/json'"
            transform: "stream-json"
            config:
              batchSize: 100
              flushInterval: 1s
          
          - name: csv-streaming
            condition: "content-type == 'text/csv'"
            transform: "stream-csv"
            config:
              batchSize: 1000
              flushInterval: 2s
      
      # 监控配置
      monitoring:
        enabled: true
        metrics:
          - name: streaming_bytes_processed
            type: counter
            labels: [content_type, transform_type]
          - name: streaming_duration
            type: histogram
            labels: [content_type, transform_type]
          - name: streaming_errors
            type: counter
            labels: [error_type, content_type]
```

## 9. 高级安全配置

### 9.1 零信任安全模型

```yaml
# 零信任安全模型配置
apiVersion: tigateway.cn/v1
kind: TiGatewaySecurity
metadata:
  name: zero-trust-security
  namespace: tigateway
spec:
  # 零信任原则配置
  zeroTrust:
    enabled: true
    principles:
      - name: never-trust-always-verify
        enabled: true
        config:
          verificationLevel: strict
          trustBoundary: none
      
      - name: least-privilege-access
        enabled: true
        config:
          defaultDeny: true
          explicitAllow: true
          privilegeEscalation: false
      
      - name: continuous-monitoring
        enabled: true
        config:
          realTimeMonitoring: true
          anomalyDetection: true
          threatIntelligence: true
  
  # 身份验证配置
  authentication:
    providers:
      - name: mfa-provider
        type: mfa
        config:
          primaryAuth: jwt
          secondaryAuth: totp
          backupAuth: sms
          requiredFactors: 2
      
      - name: biometric-provider
        type: biometric
        config:
          supportedTypes: [fingerprint, face, voice]
          fallbackAuth: mfa
          confidenceThreshold: 0.95
  
  # 授权配置
  authorization:
    policies:
      - name: zero-trust-policy
        description: "零信任授权策略"
        rules:
          - subjects: ["*"]
            resources: ["*"]
            actions: ["*"]
            conditions:
              - expression: "user.verified == true && user.riskScore < 0.3"
              - expression: "request.context.trustLevel > 0.8"
              - expression: "device.compliance == true"
  
  # 风险评估配置
  riskAssessment:
    enabled: true
    factors:
      - name: user-behavior
        weight: 0.3
        config:
          baselinePeriod: 30d
          anomalyThreshold: 0.8
      
      - name: device-compliance
        weight: 0.2
        config:
          requiredPolicies: [encryption, antivirus, firewall]
          complianceCheck: continuous
      
      - name: network-context
        weight: 0.2
        config:
          trustedNetworks: [corporate, vpn]
          geoRestrictions: enabled
          timeRestrictions: enabled
      
      - name: request-pattern
        weight: 0.3
        config:
          frequencyAnalysis: true
          volumeAnalysis: true
          patternAnalysis: true
    
    riskThresholds:
      low: 0.3
      medium: 0.6
      high: 0.8
      critical: 0.9
```

### 9.2 数据加密配置

```yaml
# 数据加密配置
apiVersion: tigateway.cn/v1
kind: TiGatewaySecurity
metadata:
  name: data-encryption
  namespace: tigateway
spec:
  # 传输加密配置
  transportEncryption:
    enabled: true
    protocols:
      - name: TLS
        version: "1.3"
        ciphers:
          - TLS_AES_256_GCM_SHA384
          - TLS_CHACHA20_POLY1305_SHA256
          - TLS_AES_128_GCM_SHA256
        keyExchange: ECDHE
        certificateValidation: strict
        hsts:
          enabled: true
          maxAge: 31536000
          includeSubDomains: true
          preload: true
  
  # 数据加密配置
  dataEncryption:
    enabled: true
    algorithms:
      - name: AES-256-GCM
        keySize: 256
        mode: GCM
        padding: none
      - name: ChaCha20-Poly1305
        keySize: 256
        mode: Poly1305
        padding: none
    
    keyManagement:
      type: kms
      config:
        provider: aws-kms
        keyId: ${KMS_KEY_ID}
        region: us-east-1
        rotation:
          enabled: true
          interval: 90d
          automatic: true
    
    encryptionScope:
      - name: sensitive-data
        fields:
          - "*.password"
          - "*.ssn"
          - "*.creditCard"
          - "*.personalInfo"
        algorithm: AES-256-GCM
        keyId: ${SENSITIVE_DATA_KEY_ID}
      
      - name: general-data
        fields:
          - "*.email"
          - "*.name"
          - "*.address"
        algorithm: ChaCha20-Poly1305
        keyId: ${GENERAL_DATA_KEY_ID}
  
  # 密钥轮换配置
  keyRotation:
    enabled: true
    schedule: "0 2 * * 0"  # 每周日凌晨2点
    strategy: gradual
    config:
      overlapPeriod: 24h
      validationPeriod: 1h
      rollbackOnFailure: true
```

## 10. 性能调优配置

### 10.1 缓存优化配置

```yaml
# 缓存优化配置
apiVersion: tigateway.cn/v1
kind: TiGatewayPerformanceConfig
metadata:
  name: cache-optimization
  namespace: tigateway
spec:
  # 多级缓存配置
  multiLevelCache:
    enabled: true
    levels:
      - name: L1-cache
        type: local
        config:
          maxSize: 1000
          ttl: 60s
          evictionPolicy: LRU
          concurrency: 16
      
      - name: L2-cache
        type: redis
        config:
          host: redis-cluster
          port: 6379
          database: 0
          ttl: 300s
          maxConnections: 100
          connectionTimeout: 5s
          readTimeout: 3s
          writeTimeout: 3s
      
      - name: L3-cache
        type: distributed
        config:
          nodes: [cache-node-1, cache-node-2, cache-node-3]
          replication: 2
          ttl: 1800s
          consistency: eventual
  
  # 缓存策略配置
  cacheStrategies:
    - name: user-profile-cache
      type: write-through
      config:
        ttl: 3600s
        maxSize: 10000
        keyGenerator: "user.id"
        invalidation:
          events: [user.update, user.delete]
          ttl: 300s
    
    - name: api-response-cache
      type: write-behind
      config:
        ttl: 300s
        maxSize: 50000
        keyGenerator: "request.path + request.query + user.id"
        batchSize: 100
        flushInterval: 10s
    
    - name: static-content-cache
      type: cache-aside
      config:
        ttl: 86400s
        maxSize: 100000
        keyGenerator: "request.path"
        compression: true
        etag: true
  
  # 缓存预热配置
  cacheWarming:
    enabled: true
    strategies:
      - name: user-profile-warming
        schedule: "0 0 * * *"  # 每天午夜
        config:
          target: user-profile-cache
          dataSource: user-service
          batchSize: 1000
          concurrency: 10
      
      - name: api-response-warming
        schedule: "0 */6 * * *"  # 每6小时
        config:
          target: api-response-cache
          dataSource: api-service
          batchSize: 500
          concurrency: 5
```

### 10.2 连接池优化配置

```yaml
# 连接池优化配置
apiVersion: tigateway.cn/v1
kind: TiGatewayPerformanceConfig
metadata:
  name: connection-pool-optimization
  namespace: tigateway
spec:
  # HTTP 连接池配置
  httpConnectionPool:
    enabled: true
    config:
      maxConnections: 1000
      maxConnectionsPerHost: 100
      connectionTimeout: 5s
      socketTimeout: 30s
      keepAliveTimeout: 60s
      maxIdleTime: 30s
      maxLifeTime: 300s
      evictionInterval: 30s
      evictionPolicy: LRU
  
  # 数据库连接池配置
  databaseConnectionPool:
    enabled: true
    config:
      maxActive: 50
      maxIdle: 10
      minIdle: 5
      maxWait: 10s
      validationQuery: "SELECT 1"
      validationInterval: 30s
      testOnBorrow: true
      testOnReturn: false
      testWhileIdle: true
      timeBetweenEvictionRuns: 60s
      minEvictableIdleTime: 300s
  
  # Redis 连接池配置
  redisConnectionPool:
    enabled: true
    config:
      maxTotal: 100
      maxIdle: 20
      minIdle: 5
      maxWaitMillis: 5000
      testOnBorrow: true
      testOnReturn: false
      testWhileIdle: true
      timeBetweenEvictionRunsMillis: 60000
      minEvictableIdleTimeMillis: 300000
      numTestsPerEvictionRun: 3
      blockWhenExhausted: true
      jmxEnabled: true
```

## 11. 配置验证和测试

### 11.1 配置验证规则

```yaml
# 配置验证规则
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigValidation
metadata:
  name: config-validation-rules
  namespace: tigateway
spec:
  # 路由验证规则
  routeValidation:
    enabled: true
    rules:
      - name: route-id-uniqueness
        description: "路由ID必须唯一"
        type: uniqueness
        field: id
        scope: global
      
      - name: target-service-exists
        description: "目标服务必须存在"
        type: existence
        field: target.service
        validator: service-existence
      
      - name: path-format-valid
        description: "路径格式必须有效"
        type: format
        field: match.path
        pattern: "^/api/.*"
      
      - name: weight-sum-valid
        description: "权重总和必须为100"
        type: custom
        validator: weight-sum-validation
        config:
          targetSum: 100
          tolerance: 5
  
  # 安全验证规则
  securityValidation:
    enabled: true
    rules:
      - name: authentication-required
        description: "敏感路径必须要求认证"
        type: custom
        validator: authentication-required
        config:
          sensitivePaths: ["/admin/**", "/api/secure/**"]
          requiredAuth: true
      
      - name: ssl-required
        description: "生产环境必须使用SSL"
        type: custom
        validator: ssl-required
        config:
          environments: [production, staging]
          requiredSSL: true
  
  # 性能验证规则
  performanceValidation:
    enabled: true
    rules:
      - name: timeout-reasonable
        description: "超时时间必须合理"
        type: range
        field: filters[].config.timeout
        min: 1s
        max: 300s
      
      - name: rate-limit-reasonable
        description: "限流配置必须合理"
        type: custom
        validator: rate-limit-validation
        config:
          minRequestsPerMinute: 1
          maxRequestsPerMinute: 10000
```

### 11.2 配置测试配置

```yaml
# 配置测试配置
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigTest
metadata:
  name: config-test-suite
  namespace: tigateway
spec:
  # 测试套件配置
  testSuites:
    - name: smoke-tests
      description: "冒烟测试"
      tests:
        - name: health-check
          type: http
          config:
            url: "/health"
            method: GET
            expectedStatus: 200
            timeout: 5s
        
        - name: routes-accessible
          type: http
          config:
            url: "/api/users/123"
            method: GET
            expectedStatus: [200, 404]
            timeout: 10s
            headers:
              Authorization: "Bearer test-token"
    
    - name: integration-tests
      description: "集成测试"
      tests:
        - name: user-service-integration
          type: http
          config:
            url: "/api/users"
            method: POST
            body: |
              {
                "name": "Test User",
                "email": "test@example.com"
              }
            expectedStatus: 201
            timeout: 30s
            headers:
              Content-Type: "application/json"
              Authorization: "Bearer test-token"
        
        - name: order-service-integration
          type: http
          config:
            url: "/api/orders"
            method: GET
            expectedStatus: 200
            timeout: 30s
            headers:
              Authorization: "Bearer test-token"
    
    - name: performance-tests
      description: "性能测试"
      tests:
        - name: load-test
          type: load
          config:
            url: "/api/users"
            method: GET
            concurrency: 100
            duration: 60s
            expectedRPS: 1000
            maxLatency: 100ms
            errorRate: 0.01
        
        - name: stress-test
          type: stress
          config:
            url: "/api/users"
            method: GET
            concurrency: 500
            duration: 300s
            rampUp: 60s
            expectedRPS: 2000
            maxLatency: 500ms
            errorRate: 0.05
  
  # 测试执行配置
  execution:
    enabled: true
    schedule: "0 2 * * *"  # 每天凌晨2点
    timeout: 1800s  # 30分钟
    retries: 3
    parallel: true
    maxConcurrency: 10
  
  # 测试报告配置
  reporting:
    enabled: true
    outputs:
      - name: console
        type: console
        config:
          level: INFO
      
      - name: file
        type: file
        config:
          path: /app/reports/test-results.json
          format: json
      
      - name: webhook
        type: webhook
        config:
          url: http://test-results-service:8080/results
          method: POST
          headers:
            Content-Type: "application/json"
            Authorization: "Bearer ${TEST_RESULTS_TOKEN}"
```

---

**相关文档**:
- [CRD 配置抽象设计](../configuration/crd-configuration-design.md)
- [CRD 基础配置示例](./crd-basic-config.md)
- [快速开始](./quick-start.md)
- [故障排除](./troubleshooting.md)