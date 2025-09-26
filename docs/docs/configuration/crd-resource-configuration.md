# TiGateway CRD 资源配置文档

## 概述

本文档详细说明了 TiGateway 中各种 CRD 资源的配置方法，包括路由配置、过滤器配置、谓词配置、安全配置等。基于 Spring Cloud Gateway 的核心概念，提供完整的 CRD 配置指南。

## 1. 基础概念

### 1.1 Spring Cloud Gateway 核心概念

在 TiGateway 中，我们基于 Spring Cloud Gateway 的核心概念构建 CRD 配置：

- **Route（路由）**: 网关的基本构件，由 ID、目标 URI、谓词集合和过滤器集合定义
- **Predicate（谓词）**: Java 8 Function Predicate，用于匹配 HTTP 请求
- **Filter（过滤器）**: GatewayFilter 实例，用于修改请求和响应

### 1.2 CRD 资源类型

TiGateway 提供以下 CRD 资源类型：

| CRD 类型 | 用途 | 对应 Spring Cloud Gateway 概念 |
|----------|------|------------------------------|
| `TiGatewayRouteConfig` | 路由配置 | Route Definition |
| `TiGatewayCustomFilter` | 自定义过滤器 | Gateway Filter Factory |
| `TiGatewaySecurity` | 安全配置 | Security Configuration |
| `TiGatewayMonitoring` | 监控配置 | Metrics & Monitoring |

## 2. 路由配置 (TiGatewayRouteConfig)

### 2.1 基础路由配置

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
        description: "用户服务路由"
        tags: ["user", "api"]
```

### 2.2 复杂路由配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: complex-routes
  namespace: tigateway
spec:
  routes:
    - id: api-gateway-route
      uri: lb://api-service
      predicates:
        - Path=/api/**
        - Method=GET,POST
        - Header=X-Request-Id, \d+
        - Cookie=session, .*
        - Host=api.example.com
        - Query=version, v[12]
        - RemoteAddr=192.168.1.0/24
        - After=2024-01-01T00:00:00Z
        - Before=2025-12-31T23:59:59Z
        - Between=2024-01-01T00:00:00Z,2024-12-31T23:59:59Z
        - Weight=group1, 80
      filters:
        - AddRequestHeader=X-Gateway, TiGateway
        - AddRequestHeadersIfNotPresent=X-Request-Color-1:blue,X-Request-Color-2:green
        - AddRequestParameter=source, gateway
        - AddResponseHeader=X-Response-Time, ${responseTime}
        - CircuitBreaker=myCircuitBreaker
        - CacheRequestBody=1024
        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin
        - FallbackHeaders=fallback
        - JsonToGrpc=proto://user.proto
        - LocalResponseCache=30m,500MB
        - MapRequestHeader=Blue, X-Request-Red
        - ModifyRequestBody=String, Hello, application/json, (exchange, s) -> Mono.just(new Hello(s.toUpperCase()))
        - ModifyResponseBody=String, String, (exchange, s) -> Mono.just(s.toUpperCase())
        - PrefixPath=/api/v1
        - PreserveHostHeader
        - RedirectTo=302, https://new-api.example.com
        - RemoveJsonAttributesResponseBody=id,color
        - RemoveRequestHeader=X-Request-Foo
        - RemoveRequestParameter=debug
        - RemoveResponseHeader=X-Response-Foo
        - RequestHeaderSize=1000B
        - RequestRateLimiter=#{@redisRateLimiter}
        - RewriteLocationResponseHeader=AS_IN_REQUEST, Location, ,
        - RewritePath=/api/(?<segment>.*), /$\{segment}
        - RewriteResponseHeader=X-Response-Red, , password=[^&]+, password=***
        - SaveSession
        - SecureHeaders
        - SetPath=/{segment}
        - SetRequestHeader=X-Request-Red, Blue
        - SetResponseHeader=X-Response-Red, Blue
        - SetStatus=UNAUTHORIZED
        - StripPrefix=2
        - Retry=3
        - RequestSize=5MB
        - SetRequestHostHeader=api.example.com
        - TokenRelay
      order: 0
      metadata:
        response-timeout: 5000
        connect-timeout: 2000
        cors:
          allowedOrigins: "*"
          allowedMethods: ["GET", "POST"]
          allowedHeaders: "*"
          maxAge: 30
```

## 3. 谓词配置详解

### 3.1 时间相关谓词

#### After 谓词
```yaml
predicates:
  - After=2017-01-20T17:42:47.789-07:00[America/Denver]
```

#### Before 谓词
```yaml
predicates:
  - Before=2017-01-20T17:42:47.789-07:00[America/Denver]
```

#### Between 谓词
```yaml
predicates:
  - Between=2017-01-20T17:42:47.789-07:00[America/Denver], 2017-01-21T17:42:47.789-07:00[America/Denver]
```

### 3.2 请求匹配谓词

#### Path 谓词
```yaml
predicates:
  - Path=/red/{segment},/blue/{segment}
```

#### Method 谓词
```yaml
predicates:
  - Method=GET,POST
```

#### Header 谓词
```yaml
predicates:
  - Header=X-Request-Id, \d+
```

#### Cookie 谓词
```yaml
predicates:
  - Cookie=chocolate, ch.p
```

#### Host 谓词
```yaml
predicates:
  - Host=**.somehost.org,**.anotherhost.org
```

#### Query 谓词
```yaml
predicates:
  - Query=green
  - Query=red, gree.
```

#### RemoteAddr 谓词
```yaml
predicates:
  - RemoteAddr=192.168.1.1/24
```

#### XForwardedRemoteAddr 谓词
```yaml
predicates:
  - XForwardedRemoteAddr=192.168.1.1/24
```

#### Weight 谓词
```yaml
predicates:
  - Weight=group1, 8
```

### 3.3 自定义远程地址解析

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: custom-remote-addr
spec:
  remoteAddressResolver:
    type: XForwardedRemoteAddressResolver
    maxTrustedIndex: 1
  routes:
    - id: proxied-route
      uri: https://downstream2
      predicates:
        - RemoteAddr=10.10.1.1, 10.10.1.1/24
```

## 4. 过滤器配置详解

### 4.1 请求头过滤器

#### AddRequestHeader
```yaml
filters:
  - AddRequestHeader=X-Request-red, blue
  - AddRequestHeader=X-Request-Red, Blue-{segment}  # 使用路径变量
```

#### AddRequestHeadersIfNotPresent
```yaml
filters:
  - AddRequestHeadersIfNotPresent=X-Request-Color-1:blue,X-Request-Color-2:green
```

#### SetRequestHeader
```yaml
filters:
  - SetRequestHeader=X-Request-Red, Blue
```

#### RemoveRequestHeader
```yaml
filters:
  - RemoveRequestHeader=X-Request-Foo
```

#### MapRequestHeader
```yaml
filters:
  - MapRequestHeader=Blue, X-Request-Red
```

### 4.2 响应头过滤器

#### AddResponseHeader
```yaml
filters:
  - AddResponseHeader=X-Response-Time, ${responseTime}
```

#### SetResponseHeader
```yaml
filters:
  - SetResponseHeader=X-Response-Red, Blue
```

#### RemoveResponseHeader
```yaml
filters:
  - RemoveResponseHeader=X-Response-Foo
```

#### RewriteResponseHeader
```yaml
filters:
  - RewriteResponseHeader=X-Response-Red, , password=[^&]+, password=***
```

#### DedupeResponseHeader
```yaml
filters:
  - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin
```

### 4.3 请求参数过滤器

#### AddRequestParameter
```yaml
filters:
  - AddRequestParameter=red, blue
```

#### RemoveRequestParameter
```yaml
filters:
  - RemoveRequestParameter=red
```

### 4.4 路径处理过滤器

#### PrefixPath
```yaml
filters:
  - PrefixPath=/mypath
```

#### StripPrefix
```yaml
filters:
  - StripPrefix=2
```

#### SetPath
```yaml
filters:
  - SetPath=/{segment}
```

#### RewritePath
```yaml
filters:
  - RewritePath=/red/?(?<segment>.*), /$\{segment}
```

### 4.5 请求体处理过滤器

#### CacheRequestBody
```yaml
filters:
  - CacheRequestBody=1024
```

#### ModifyRequestBody
```yaml
filters:
  - ModifyRequestBody=String, Hello, application/json, (exchange, s) -> Mono.just(new Hello(s.toUpperCase()))
```

#### ModifyResponseBody
```yaml
filters:
  - ModifyResponseBody=String, String, (exchange, s) -> Mono.just(s.toUpperCase())
```

#### RemoveJsonAttributesResponseBody
```yaml
filters:
  - RemoveJsonAttributesResponseBody=id,color
  - RemoveJsonAttributesResponseBody=id,color,true  # 递归删除
```

### 4.6 状态和重定向过滤器

#### SetStatus
```yaml
filters:
  - SetStatus=UNAUTHORIZED
  - SetStatus=401
```

#### RedirectTo
```yaml
filters:
  - RedirectTo=302, https://acme.org
```

### 4.7 安全过滤器

#### SecureHeaders
```yaml
filters:
  - SecureHeaders
```

#### RequestHeaderSize
```yaml
filters:
  - RequestHeaderSize=1000B
```

#### RequestSize
```yaml
filters:
  - RequestSize=5MB
```

### 4.8 限流和熔断过滤器

#### RequestRateLimiter
```yaml
filters:
  - RequestRateLimiter=#{@redisRateLimiter}
```

#### CircuitBreaker
```yaml
filters:
  - CircuitBreaker=myCircuitBreaker
```

### 4.9 缓存过滤器

#### LocalResponseCache
```yaml
filters:
  - LocalResponseCache=30m,500MB
```

### 4.10 会话过滤器

#### SaveSession
```yaml
filters:
  - SaveSession
```

### 4.11 重试过滤器

#### Retry
```yaml
filters:
  - Retry=3
```

### 4.12 主机头过滤器

#### PreserveHostHeader
```yaml
filters:
  - PreserveHostHeader
```

#### SetRequestHostHeader
```yaml
filters:
  - SetRequestHostHeader=api.example.com
```

### 4.13 令牌中继过滤器

#### TokenRelay
```yaml
filters:
  - TokenRelay
```

### 4.14 协议转换过滤器

#### JsonToGrpc
```yaml
filters:
  - JsonToGrpc=proto://user.proto
```

### 4.15 降级过滤器

#### FallbackHeaders
```yaml
filters:
  - FallbackHeaders=fallback
```

### 4.16 位置重写过滤器

#### RewriteLocationResponseHeader
```yaml
filters:
  - RewriteLocationResponseHeader=AS_IN_REQUEST, Location, ,
```

## 5. 全局过滤器配置

### 5.1 全局过滤器定义

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayGlobalFilter
metadata:
  name: global-filters
  namespace: tigateway
spec:
  filters:
    - name: CustomGlobalFilter
      order: -1
      config:
        headerName: X-Global-Filter
        headerValue: "Global Value"
    - name: MetricsFilter
      order: 0
      config:
        enabled: true
    - name: LocalResponseCacheFilter
      order: 1
      config:
        enabled: true
        maxSize: 100MB
```

### 5.2 默认过滤器配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: default-filters
spec:
  defaultFilters:
    - AddRequestHeader=X-Gateway, TiGateway
    - AddResponseHeader=X-Response-Time, ${responseTime}
    - SecureHeaders
```

## 6. 服务发现配置

### 6.1 DiscoveryClient 路由配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: discovery-routes
spec:
  discovery:
    enabled: true
    locator:
      enabled: true
      lowerCaseServiceId: true
      predicates:
        - name: Path
          args:
            pattern: "'/'+serviceId+'/**'"
        - name: Host
          args:
            pattern: "'**.foo.com'"
      filters:
        - name: CircuitBreaker
          args:
            name: serviceId
        - name: RewritePath
          args:
            regexp: "'/' + serviceId + '/?(?<remaining>.*)'"
            replacement: "'/${remaining}'"
```

## 7. HTTP 超时配置

### 7.1 全局超时配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: timeout-config
spec:
  httpClient:
    connectTimeout: 1000  # 毫秒
    responseTimeout: 5s   # Duration 格式
```

### 7.2 路由级超时配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: per-route-timeout
spec:
  routes:
    - id: timeout-route
      uri: https://example.org
      predicates:
        - Path=/delay/{timeout}
      metadata:
        response-timeout: 200  # 毫秒
        connect-timeout: 200   # 毫秒
```

## 8. CORS 配置

### 8.1 全局 CORS 配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: global-cors
spec:
  globalCors:
    corsConfigurations:
      "[/**]":
        allowedOrigins: "https://docs.spring.io"
        allowedMethods:
          - GET
        allowedHeaders:
          - "*"
        allowCredentials: true
        maxAge: 3600
    addToSimpleUrlHandlerMapping: true
```

### 8.2 路由级 CORS 配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: route-cors
spec:
  routes:
    - id: cors_route
      uri: https://example.org
      predicates:
        - Path=/service/**
      metadata:
        cors:
          allowedOrigins: "*"
          allowedMethods:
            - GET
            - POST
          allowedHeaders: "*"
          maxAge: 30
```

## 9. 路由元数据配置

### 9.1 基础元数据

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: metadata-routes
spec:
  routes:
    - id: route_with_metadata
      uri: https://example.org
      metadata:
        optionName: "OptionValue"
        compositeObject:
          name: "value"
        iAmNumber: 1
        description: "路由描述"
        tags: ["api", "v1"]
        version: "1.0.0"
```

### 9.2 超时元数据

```yaml
metadata:
  response-timeout: 5000
  connect-timeout: 2000
```

### 9.3 CORS 元数据

```yaml
metadata:
  cors:
    allowedOrigins: "*"
    allowedMethods: ["GET", "POST"]
    allowedHeaders: "*"
    maxAge: 30
```

## 10. 指标配置

### 10.1 路由指标

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayMonitoring
metadata:
  name: route-metrics
spec:
  metrics:
    enabled: true
    routeMetrics:
      enabled: true
      includeTags:
        - route_id
        - method
        - status
    globalMetrics:
      enabled: true
      includeTags:
        - instance
        - version
```

### 10.2 自定义指标

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayMonitoring
metadata:
  name: custom-metrics
spec:
  customMetrics:
    - name: custom_request_count
      type: counter
      description: "自定义请求计数"
      tags:
        - service
        - endpoint
    - name: custom_response_time
      type: histogram
      description: "自定义响应时间"
      buckets: [0.1, 0.5, 1.0, 2.0, 5.0]
```

## 11. 安全配置

### 11.1 基础安全配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewaySecurity
metadata:
  name: basic-security
spec:
  authentication:
    enabled: true
    type: JWT
    jwt:
      secret: "your-secret-key"
      issuer: "tigateway"
      audience: "api-users"
  authorization:
    enabled: true
    rules:
      - path: "/api/admin/**"
        roles: ["ADMIN"]
      - path: "/api/user/**"
        roles: ["USER", "ADMIN"]
  rateLimiting:
    enabled: true
    defaultRate: 100
    burstCapacity: 200
```

### 11.2 OAuth2 配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewaySecurity
metadata:
  name: oauth2-security
spec:
  oauth2:
    enabled: true
    clientId: "tigateway-client"
    clientSecret: "client-secret"
    authorizationUri: "https://auth.example.com/oauth/authorize"
    tokenUri: "https://auth.example.com/oauth/token"
    userInfoUri: "https://auth.example.com/oauth/userinfo"
    scopes:
      - "read"
      - "write"
```

## 12. 配置验证

### 12.1 配置验证规则

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: validated-routes
spec:
  validation:
    enabled: true
    rules:
      - field: "spec.routes[].uri"
        required: true
        pattern: "^(http|https|lb)://.*"
      - field: "spec.routes[].predicates"
        required: true
        minItems: 1
      - field: "spec.routes[].order"
        type: "integer"
        minimum: 0
        maximum: 2147483647
```

### 12.2 配置测试

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigTest
metadata:
  name: route-test
spec:
  testCases:
    - name: "用户服务路由测试"
      request:
        method: GET
        path: "/api/users/123"
        headers:
          X-Request-Id: "12345"
      expected:
        status: 200
        headers:
          X-Gateway: "TiGateway"
        responseTime: "< 1000ms"
```

## 13. 配置模板

### 13.1 路由模板

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigTemplate
metadata:
  name: api-route-template
spec:
  template:
    routes:
      - id: "{{.serviceName}}-route"
        uri: "{{.serviceUri}}"
        predicates:
          - Path=/api/{{.serviceName}}/**
        filters:
          - StripPrefix=2
          - AddRequestHeader=X-Service,{{.serviceName}}
        order: {{.order}}
        metadata:
          description: "{{.serviceName}} 服务路由"
          tags: ["{{.serviceName}}", "api"]
  parameters:
    - name: serviceName
      type: string
      required: true
      description: "服务名称"
    - name: serviceUri
      type: string
      required: true
      description: "服务 URI"
    - name: order
      type: integer
      default: 0
      description: "路由顺序"
```

### 13.2 使用模板

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: user-service-from-template
spec:
  template: api-route-template
  parameters:
    serviceName: "user"
    serviceUri: "http://user-service:8080"
    order: 1
```

## 14. 配置版本管理

### 14.1 配置版本

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigVersion
metadata:
  name: route-config-v1
spec:
  version: "1.0.0"
  description: "初始路由配置版本"
  config:
    routes:
      - id: user-service-route
        uri: http://user-service:8080
        predicates:
          - Path=/api/users/**
        filters:
          - StripPrefix=2
  rollbackStrategy:
    enabled: true
    maxVersions: 10
    autoRollback: false
```

### 14.2 配置回滚

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayConfigRollback
metadata:
  name: rollback-to-v1
spec:
  targetVersion: "1.0.0"
  reason: "新版本存在问题，回滚到稳定版本"
  rollbackStrategy:
    immediate: true
    preserveMetadata: true
```

## 15. 最佳实践

### 15.1 路由设计原则

1. **单一职责**: 每个路由应该只负责一个服务的代理
2. **明确命名**: 使用有意义的 ID 和描述
3. **合理排序**: 使用 order 字段控制路由匹配顺序
4. **适当过滤**: 只添加必要的过滤器

### 15.2 性能优化

1. **缓存配置**: 合理使用 LocalResponseCache
2. **连接池**: 配置适当的连接超时
3. **限流保护**: 使用 RequestRateLimiter 防止过载
4. **熔断机制**: 使用 CircuitBreaker 提高可用性

### 15.3 安全考虑

1. **输入验证**: 使用谓词验证请求
2. **敏感信息**: 使用 RewriteResponseHeader 隐藏敏感数据
3. **安全头**: 使用 SecureHeaders 添加安全头
4. **访问控制**: 使用 RemoteAddr 限制访问来源

### 15.4 监控和调试

1. **指标收集**: 启用路由指标收集
2. **日志记录**: 配置适当的日志级别
3. **链路追踪**: 添加请求 ID 用于追踪
4. **健康检查**: 配置健康检查端点

---

**相关文档**:
- [CRD 配置抽象设计](./crd-configuration-design.md)
- [CRD 基础配置示例](../examples/crd-basic-config.md)
- [CRD 高级配置示例](../examples/crd-advanced-config.md)
- [Spring Cloud Gateway 集成指南](../development/spring-cloud-gateway-integration.md)
