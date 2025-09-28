      - status
      - duration
      - ip
      - user_agent
      - trace_id
      - span_id
    
    # Log output configuration
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
  
  # Tracing configuration
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
          value: "tenant-123"
    
    # Tracing rules configuration
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
  
  # Alerting configuration
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
            webhook: "https://hooks.slack.com/services/example"
      
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
            integrationKey: "pagerduty-integration-key-example"
```

## 6. Configuration Management

### 6.1 Configuration Version Management

```yaml
# Configuration version management
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigVersion
metadata:
  name: config-v1.1.0
  namespace: tigateway
spec:
  # Version information
  version: v1.1.0
  description: "Advanced configuration with multi-tenant support"
  createdBy: admin
  createdAt: "2024-09-23T10:00:00Z"
  
  # Configuration snapshot
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
  
  # Deployment status
  deployment:
    status: active
    replicas: 3
    lastDeployed: "2024-09-23T10:00:00Z"
    deployedBy: admin
    rollbackVersion: v1.0.0
  
  # Validation status
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
# Configuration rollback
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigRollback
metadata:
  name: rollback-to-v1.0.0
  namespace: tigateway
spec:
  # Rollback target version
  targetVersion: v1.0.0
  
  # Rollback reason
  reason: "Performance issues with v1.1.0"
  
  # Rollback strategy
  strategy: blue-green
  
  # Post-rollback validation
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

### 6.2 Configuration Template Management

```yaml
# Configuration template management
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigTemplate
metadata:
  name: enterprise-template
  namespace: tigateway
spec:
  # Template definition
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
  
  # Template parameters
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
# Using enterprise template to create configuration
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

## 7. High Availability Configuration

### 7.1 Multi-Region Deployment Configuration

```yaml
# Multi-region deployment configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: multi-region-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: multi-region-group
      description: "Multi-region route group"
      routes:
        # Primary region route
        - id: primary-region-route
          description: "Primary region route"
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
        
        # Secondary region route
        - id: secondary-region-route
          description: "Secondary region route"
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

### 7.2 Failover Configuration

```yaml
# Failover configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: failover-routes
  namespace: tigateway
spec:
  routeGroups:
    - name: failover-group
      description: "Failover route group"
      routes:
        - id: failover-route
          description: "Failover route"
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

## 8. Data Flow Control Configuration

### 8.1 Request/Response Size Limits

```yaml
# Data flow control configuration
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

### 8.2 Streaming Processing Configuration

```yaml
# Streaming processing configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayCustomFilter
metadata:
  name: streaming-filter
  namespace: tigateway
spec:
  filter:
    name: StreamingFilter
    version: v1.0.0
    description: "Streaming processing filter"
    
    config:
      # Streaming configuration
      streaming:
        enabled: true
        chunkSize: 8192  # 8KB
        bufferSize: 65536  # 64KB
        timeout: 30s
        compression:
          enabled: true
          algorithm: gzip
          level: 6
      
      # Data transformation configuration
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
      
      # Monitoring configuration
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

## 9. Advanced Security Configuration

### 9.1 Zero Trust Security Model

```yaml
# Zero trust security model configuration
apiVersion: tigateway.cn/v1
kind: TiGatewaySecurity
metadata:
  name: zero-trust-security
  namespace: tigateway
spec:
  # Zero trust principles configuration
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
  
  # Authentication configuration
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
  
  # Authorization configuration
  authorization:
    policies:
      - name: zero-trust-policy
        description: "Zero trust authorization policy"
        rules:
          - subjects: ["*"]
            resources: ["*"]
            actions: ["*"]
            conditions:
              - expression: "user.verified == true && user.riskScore < 0.3"
              - expression: "request.context.trustLevel > 0.8"
              - expression: "device.compliance == true"
  
  # Risk assessment configuration
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

### 9.2 Data Encryption Configuration

```yaml
# Data encryption configuration
apiVersion: tigateway.cn/v1
kind: TiGatewaySecurity
metadata:
  name: data-encryption
  namespace: tigateway
spec:
  # Transport encryption configuration
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
  
  # Data encryption configuration
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
        keyId: "kms-key-id-example"
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
        keyId: "sensitive-data-key-id-example"
      
      - name: general-data
        fields:
          - "*.email"
          - "*.name"
          - "*.address"
        algorithm: ChaCha20-Poly1305
        keyId: "general-data-key-id-example"
  
  # Key rotation configuration
  keyRotation:
    enabled: true
    schedule: "0 2 * * 0"  # Every Sunday at 2 AM
    strategy: gradual
    config:
      overlapPeriod: 24h
      validationPeriod: 1h
      rollbackOnFailure: true
```

## 10. Performance Tuning Configuration

### 10.1 Cache Optimization Configuration

```yaml
# Cache optimization configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayPerformanceConfig
metadata:
  name: cache-optimization
  namespace: tigateway
spec:
  # Multi-level cache configuration
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
  
  # Cache strategy configuration
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
  
  # Cache warming configuration
  cacheWarming:
    enabled: true
    strategies:
      - name: user-profile-warming
        schedule: "0 0 * * *"  # Daily at midnight
        config:
          target: user-profile-cache
          dataSource: user-service
          batchSize: 1000
          concurrency: 10
      
      - name: api-response-warming
        schedule: "0 */6 * * *"  # Every 6 hours
        config:
          target: api-response-cache
          dataSource: api-service
          batchSize: 500
          concurrency: 5
```

### 10.2 Connection Pool Optimization Configuration

```yaml
# Connection pool optimization configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayPerformanceConfig
metadata:
  name: connection-pool-optimization
  namespace: tigateway
spec:
  # HTTP connection pool configuration
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
  
  # Database connection pool configuration
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
  
  # Redis connection pool configuration
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

## 11. Configuration Validation and Testing

### 11.1 Configuration Validation Rules

```yaml
# Configuration validation rules
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigValidation
metadata:
  name: config-validation-rules
  namespace: tigateway
spec:
  # Route validation rules
  routeValidation:
    enabled: true
    rules:
      - name: route-id-uniqueness
        description: "Route ID must be unique"
        type: uniqueness
        field: id
        scope: global
      
      - name: target-service-exists
        description: "Target service must exist"
        type: existence
        field: target.service
        validator: service-existence
      
      - name: path-format-valid
        description: "Path format must be valid"
        type: format
        field: match.path
        pattern: "^/api/.*"
      
      - name: weight-sum-valid
        description: "Weight sum must be 100"
        type: custom
        validator: weight-sum-validation
        config:
          targetSum: 100
          tolerance: 5
  
  # Security validation rules
  securityValidation:
    enabled: true
    rules:
      - name: authentication-required
        description: "Sensitive paths must require authentication"
        type: custom
        validator: authentication-required
        config:
          sensitivePaths: ["/admin/**", "/api/secure/**"]
          requiredAuth: true
      
      - name: ssl-required
        description: "Production environment must use SSL"
        type: custom
        validator: ssl-required
        config:
          environments: [production, staging]
          requiredSSL: true
  
  # Performance validation rules
  performanceValidation:
    enabled: true
    rules:
      - name: timeout-reasonable
        description: "Timeout must be reasonable"
        type: range
        field: filters[].config.timeout
        min: 1s
        max: 300s
      
      - name: rate-limit-reasonable
        description: "Rate limit configuration must be reasonable"
        type: custom
        validator: rate-limit-validation
        config:
          minRequestsPerMinute: 1
          maxRequestsPerMinute: 10000
```

### 11.2 Configuration Testing Configuration

```yaml
# Configuration testing configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigTest
metadata:
  name: config-test-suite
  namespace: tigateway
spec:
  # Test suite configuration
  testSuites:
    - name: smoke-tests
      description: "Smoke tests"
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
      description: "Integration tests"
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
      description: "Performance tests"
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
  
  # Test execution configuration
  execution:
    enabled: true
    schedule: "0 2 * * *"  # Daily at 2 AM
    timeout: 1800s  # 30 minutes
    retries: 3
    parallel: true
    maxConcurrency: 10
  
  # Test reporting configuration
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
            Authorization: "Bearer test-results-token-example"
```

## 12. Advanced Multi-Tenant Configuration

### 12.1 Tenant Isolation Configuration

```yaml
# Advanced multi-tenant configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayMultiTenantConfig
metadata:
  name: advanced-multi-tenant
  namespace: tigateway
spec:
  # Tenant isolation strategy
  isolation:
    strategy: namespace-based
    levels:
      - name: strict
        description: "Complete isolation with dedicated resources"
        config:
          resourceQuota: true
          networkPolicy: true
          storageIsolation: true
          computeIsolation: true
      
      - name: relaxed
        description: "Shared resources with data isolation"
        config:
          resourceQuota: false
          networkPolicy: true
          storageIsolation: true
          computeIsolation: false
      
      - name: shared
        description: "Shared resources with logical isolation"
        config:
          resourceQuota: false
          networkPolicy: false
          storageIsolation: false
          computeIsolation: false
  
  # Tenant management
  tenantManagement:
    autoProvisioning: true
    defaultIsolationLevel: relaxed
    maxTenants: 1000
    resourceLimits:
      cpu: "1000m"
      memory: "2Gi"
      storage: "10Gi"
    
    # Tenant lifecycle management
    lifecycle:
      provisioning:
        enabled: true
        template: default-tenant-template
        validation: true
      
      deprovisioning:
        enabled: true
        gracePeriod: 30d
        dataRetention: 90d
        backup: true
  
  # Tenant-specific configurations
  tenantConfigs:
    - name: enterprise-tenant
      isolationLevel: strict
      resources:
        cpu: "2000m"
        memory: "4Gi"
        storage: "50Gi"
      features:
        - advanced-monitoring
        - custom-filters
        - dedicated-cache
        - priority-routing
      
    - name: standard-tenant
      isolationLevel: relaxed
      resources:
        cpu: "500m"
        memory: "1Gi"
        storage: "10Gi"
      features:
        - basic-monitoring
        - standard-filters
        - shared-cache
```

### 12.2 Cross-Tenant Communication Configuration

```yaml
# Cross-tenant communication configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayCrossTenantConfig
metadata:
  name: cross-tenant-communication
  namespace: tigateway
spec:
  # Cross-tenant policies
  policies:
    - name: allow-shared-services
      description: "Allow access to shared services"
      rules:
        - sourceTenants: ["*"]
          targetServices: ["shared-auth-service", "shared-notification-service"]
          actions: [read, write]
          conditions:
            - expression: "user.roles.contains('SHARED_SERVICE_ACCESS')"
    
    - name: allow-tenant-collaboration
      description: "Allow collaboration between specific tenants"
      rules:
        - sourceTenants: ["tenant-a", "tenant-b"]
          targetServices: ["collaboration-service"]
          actions: [read, write]
          conditions:
            - expression: "user.tenant in ['tenant-a', 'tenant-b']"
    
    - name: deny-cross-tenant-data-access
      description: "Deny cross-tenant data access"
      rules:
        - sourceTenants: ["*"]
          targetServices: ["*"]
          actions: [read, write]
          conditions:
            - expression: "request.targetTenant != user.tenant"
          effect: deny
  
  # Service mesh integration
  serviceMesh:
    enabled: true
    provider: istio
    config:
      namespace: istio-system
      mtls:
        enabled: true
        mode: STRICT
      trafficPolicy:
        loadBalancer:
          simple: ROUND_ROBIN
        connectionPool:
          tcp:
            maxConnections: 100
          http:
            http1MaxPendingRequests: 10
            maxRequestsPerConnection: 2
            maxRetries: 3
            consecutiveGatewayErrors: 5
            interval: 30s
            baseEjectionTime: 30s
            maxEjectionPercent: 50
```

## 13. Disaster Recovery Configuration

### 13.1 Backup and Recovery Configuration

```yaml
# Disaster recovery configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayDisasterRecoveryConfig
metadata:
  name: disaster-recovery
  namespace: tigateway
spec:
  # Backup configuration
  backup:
    enabled: true
    schedule: "0 2 * * *"  # Daily at 2 AM
    retention: 30d
    storage:
      type: s3
      config:
        bucket: tigateway-backups
        region: us-east-1
        prefix: "backups/"
        encryption: true
        versioning: true
    
    # Backup scope
    scope:
      configurations: true
      routes: true
      filters: true
      security: true
      monitoring: true
      customResources: true
    
    # Backup validation
    validation:
      enabled: true
      tests:
        - name: configuration-integrity
          type: schema-validation
        - name: route-connectivity
          type: connectivity-test
        - name: security-policy-validation
          type: security-validation
  
  # Recovery configuration
  recovery:
    enabled: true
    strategies:
      - name: full-recovery
        description: "Complete system recovery"
        rto: 4h  # Recovery Time Objective
        rpo: 1h  # Recovery Point Objective
        steps:
          - restore-configurations
          - restore-routes
          - restore-security
          - validate-system
          - switch-traffic
      
      - name: partial-recovery
        description: "Partial system recovery"
        rto: 2h
        rpo: 30m
        steps:
          - restore-critical-routes
          - restore-security
          - validate-critical-functions
          - gradual-traffic-restoration
  
  # Failover configuration
  failover:
    enabled: true
    triggers:
      - name: service-unavailable
        condition: "service.availability < 0.95"
        duration: "5m"
        action: automatic-failover
      
      - name: high-error-rate
        condition: "error.rate > 0.1"
        duration: "3m"
        action: automatic-failover
      
      - name: manual-failover
        condition: "manual.trigger == true"
        duration: "0s"
        action: manual-failover
    
    # Failover targets
    targets:
      - name: dr-site-1
        region: us-west-2
        priority: 1
        capacity: 100%
        latency: 50ms
      
      - name: dr-site-2
        region: eu-west-1
        priority: 2
        capacity: 80%
        latency: 150ms
```

---

**Related Documentation**:
- [CRD Configuration Design](../configuration/crd-configuration-design.md)
- [CRD Basic Configuration Examples](./crd-basic-config.md)
- [Quick Start Guide](../getting-started/quick-start.md)
- [Troubleshooting Guide](./troubleshooting.md)