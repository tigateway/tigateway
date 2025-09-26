# TiGateway 类型化 CRD 设计

## 概述

本文档提出了一个类型化的 CRD 设计，将谓词和过滤器从字符串规则抽象为结构化的类型系统。这种设计提供了更好的类型安全性、IDE 支持和配置验证。

## 设计目标

1. **类型安全**: 使用结构化类型而非字符串
2. **IDE 支持**: 提供自动补全和语法检查
3. **配置验证**: 在 CRD 层面进行配置验证
4. **可扩展性**: 支持自定义谓词和过滤器类型
5. **向后兼容**: 保持与现有配置的兼容性

## 核心类型系统

### 1. 谓词类型系统

#### 1.1 基础谓词接口

```yaml
# 基础谓词接口
apiVersion: tigateway.cn/v1
kind: TiGatewayPredicateType
metadata:
  name: base-predicate
spec:
  type: "Predicate"
  version: "v1"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["Path", "Method", "Header", "Cookie", "Host", "Query", "RemoteAddr", "After", "Before", "Between", "Weight", "XForwardedRemoteAddr"]
      order:
        type: integer
        minimum: 0
        maximum: 2147483647
        default: 0
      enabled:
        type: boolean
        default: true
```

#### 1.2 具体谓词类型定义

```yaml
# Path 谓词类型
apiVersion: tigateway.cn/v1
kind: TiGatewayPredicateType
metadata:
  name: path-predicate
spec:
  type: "PathPredicate"
  version: "v1"
  extends: "Predicate"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["Path"]
        default: "Path"
      patterns:
        type: array
        items:
          type: string
          pattern: "^/.*"
        minItems: 1
        description: "路径模式列表，支持 Ant 风格模式"
      matchTrailingSlash:
        type: boolean
        default: true
        description: "是否匹配尾部斜杠"
      caseSensitive:
        type: boolean
        default: true
        description: "是否区分大小写"
    required: ["patterns"]

---
# Method 谓词类型
apiVersion: tigateway.cn/v1
kind: TiGatewayPredicateType
metadata:
  name: method-predicate
spec:
  type: "MethodPredicate"
  version: "v1"
  extends: "Predicate"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["Method"]
        default: "Method"
      methods:
        type: array
        items:
          type: string
          enum: ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE"]
        minItems: 1
        description: "HTTP 方法列表"
    required: ["methods"]

---
# Header 谓词类型
apiVersion: tigateway.cn/v1
kind: TiGatewayPredicateType
metadata:
  name: header-predicate
spec:
  type: "HeaderPredicate"
  version: "v1"
  extends: "Predicate"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["Header"]
        default: "Header"
      headerName:
        type: string
        minLength: 1
        description: "请求头名称"
      regexp:
        type: string
        description: "正则表达式，可选"
      caseSensitive:
        type: boolean
        default: true
        description: "是否区分大小写"
    required: ["headerName"]

---
# Cookie 谓词类型
apiVersion: tigateway.cn/v1
kind: TiGatewayPredicateType
metadata:
  name: cookie-predicate
spec:
  type: "CookiePredicate"
  version: "v1"
  extends: "Predicate"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["Cookie"]
        default: "Cookie"
      cookieName:
        type: string
        minLength: 1
        description: "Cookie 名称"
      regexp:
        type: string
        description: "正则表达式"
    required: ["cookieName", "regexp"]

---
# Host 谓词类型
apiVersion: tigateway.cn/v1
kind: TiGatewayPredicateType
metadata:
  name: host-predicate
spec:
  type: "HostPredicate"
  version: "v1"
  extends: "Predicate"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["Host"]
        default: "Host"
      patterns:
        type: array
        items:
          type: string
        minItems: 1
        description: "主机名模式列表，支持 Ant 风格模式"
      caseSensitive:
        type: boolean
        default: true
        description: "是否区分大小写"
    required: ["patterns"]

---
# Query 谓词类型
apiVersion: tigateway.cn/v1
kind: TiGatewayPredicateType
metadata:
  name: query-predicate
spec:
  type: "QueryPredicate"
  version: "v1"
  extends: "Predicate"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["Query"]
        default: "Query"
      param:
        type: string
        minLength: 1
        description: "查询参数名称"
      regexp:
        type: string
        description: "正则表达式，可选"
      caseSensitive:
        type: boolean
        default: true
        description: "是否区分大小写"
    required: ["param"]

---
# RemoteAddr 谓词类型
apiVersion: tigateway.cn/v1
kind: TiGatewayPredicateType
metadata:
  name: remote-addr-predicate
spec:
  type: "RemoteAddrPredicate"
  version: "v1"
  extends: "Predicate"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["RemoteAddr"]
        default: "RemoteAddr"
      sources:
        type: array
        items:
          type: string
          pattern: "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?:/[0-9]{1,2})?$|^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}(?:/[0-9]{1,3})?$"
        minItems: 1
        description: "CIDR 注解的 IP 地址列表"
      resolver:
        type: object
        properties:
          type:
            type: string
            enum: ["default", "XForwardedRemoteAddressResolver"]
            default: "default"
          maxTrustedIndex:
            type: integer
            minimum: 1
            description: "最大信任索引，仅当 type 为 XForwardedRemoteAddressResolver 时使用"
    required: ["sources"]

---
# 时间谓词类型
apiVersion: tigateway.cn/v1
kind: TiGatewayPredicateType
metadata:
  name: time-predicate
spec:
  type: "TimePredicate"
  version: "v1"
  extends: "Predicate"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["After", "Before", "Between"]
      datetime:
        type: string
        format: date-time
        description: "时间点，ISO 8601 格式"
      startTime:
        type: string
        format: date-time
        description: "开始时间，仅用于 Between 谓词"
      endTime:
        type: string
        format: date-time
        description: "结束时间，仅用于 Between 谓词"
      timezone:
        type: string
        default: "UTC"
        description: "时区"
    oneOf:
      - required: ["datetime"]  # After 或 Before
      - required: ["startTime", "endTime"]  # Between

---
# Weight 谓词类型
apiVersion: tigateway.cn/v1
kind: TiGatewayPredicateType
metadata:
  name: weight-predicate
spec:
  type: "WeightPredicate"
  version: "v1"
  extends: "Predicate"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["Weight"]
        default: "Weight"
      group:
        type: string
        minLength: 1
        description: "权重组名称"
      weight:
        type: integer
        minimum: 1
        maximum: 100
        description: "权重值"
    required: ["group", "weight"]
```

### 2. 过滤器类型系统

#### 2.1 基础过滤器接口

```yaml
# 基础过滤器接口
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterType
metadata:
  name: base-filter
spec:
  type: "Filter"
  version: "v1"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["AddRequestHeader", "AddResponseHeader", "SetRequestHeader", "SetResponseHeader", "RemoveRequestHeader", "RemoveResponseHeader", "AddRequestParameter", "RemoveRequestParameter", "PrefixPath", "StripPrefix", "SetPath", "RewritePath", "SetStatus", "RedirectTo", "SecureHeaders", "RequestRateLimiter", "CircuitBreaker", "Retry", "LocalResponseCache", "SaveSession", "PreserveHostHeader", "SetRequestHostHeader", "TokenRelay", "JsonToGrpc", "FallbackHeaders", "RewriteLocationResponseHeader", "ModifyRequestBody", "ModifyResponseBody", "RemoveJsonAttributesResponseBody", "CacheRequestBody", "DedupeResponseHeader", "MapRequestHeader", "RequestHeaderSize", "RequestSize"]
      order:
        type: integer
        minimum: -2147483648
        maximum: 2147483647
        default: 0
      enabled:
        type: boolean
        default: true
```

#### 2.2 具体过滤器类型定义

```yaml
# 请求头过滤器类型
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterType
metadata:
  name: request-header-filters
spec:
  type: "RequestHeaderFilters"
  version: "v1"
  extends: "Filter"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["AddRequestHeader", "SetRequestHeader", "RemoveRequestHeader", "MapRequestHeader"]
      headerName:
        type: string
        minLength: 1
        description: "请求头名称"
      value:
        type: string
        description: "请求头值，AddRequestHeader 和 SetRequestHeader 需要"
      fromHeader:
        type: string
        description: "源请求头名称，MapRequestHeader 需要"
      toHeader:
        type: string
        description: "目标请求头名称，MapRequestHeader 需要"
      overwrite:
        type: boolean
        default: true
        description: "是否覆盖现有请求头"
    oneOf:
      - required: ["headerName", "value"]  # AddRequestHeader, SetRequestHeader
      - required: ["headerName"]  # RemoveRequestHeader
      - required: ["fromHeader", "toHeader"]  # MapRequestHeader

---
# 响应头过滤器类型
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterType
metadata:
  name: response-header-filters
spec:
  type: "ResponseHeaderFilters"
  version: "v1"
  extends: "Filter"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["AddResponseHeader", "SetResponseHeader", "RemoveResponseHeader", "RewriteResponseHeader", "DedupeResponseHeader"]
      headerName:
        type: string
        minLength: 1
        description: "响应头名称"
      value:
        type: string
        description: "响应头值"
      regexp:
        type: string
        description: "正则表达式，RewriteResponseHeader 需要"
      replacement:
        type: string
        description: "替换值，RewriteResponseHeader 需要"
      headers:
        type: array
        items:
          type: string
        description: "要去重的响应头名称列表，DedupeResponseHeader 需要"
    oneOf:
      - required: ["headerName", "value"]  # AddResponseHeader, SetResponseHeader
      - required: ["headerName"]  # RemoveResponseHeader
      - required: ["headerName", "regexp", "replacement"]  # RewriteResponseHeader
      - required: ["headers"]  # DedupeResponseHeader

---
# 路径处理过滤器类型
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterType
metadata:
  name: path-filters
spec:
  type: "PathFilters"
  version: "v1"
  extends: "Filter"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["PrefixPath", "StripPrefix", "SetPath", "RewritePath"]
      prefix:
        type: string
        pattern: "^/.*"
        description: "路径前缀，PrefixPath 需要"
      parts:
        type: integer
        minimum: 1
        description: "要移除的路径段数量，StripPrefix 需要"
      template:
        type: string
        pattern: "^/.*"
        description: "路径模板，SetPath 需要"
      regexp:
        type: string
        description: "正则表达式，RewritePath 需要"
      replacement:
        type: string
        description: "替换表达式，RewritePath 需要"
    oneOf:
      - required: ["prefix"]  # PrefixPath
      - required: ["parts"]  # StripPrefix
      - required: ["template"]  # SetPath
      - required: ["regexp", "replacement"]  # RewritePath

---
# 状态和重定向过滤器类型
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterType
metadata:
  name: status-redirect-filters
spec:
  type: "StatusRedirectFilters"
  version: "v1"
  extends: "Filter"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["SetStatus", "RedirectTo"]
      status:
        oneOf:
          - type: integer
            minimum: 100
            maximum: 599
          - type: string
            enum: ["CONTINUE", "SWITCHING_PROTOCOLS", "PROCESSING", "CHECKPOINT", "OK", "CREATED", "ACCEPTED", "NON_AUTHORITATIVE_INFORMATION", "NO_CONTENT", "RESET_CONTENT", "PARTIAL_CONTENT", "MULTI_STATUS", "ALREADY_REPORTED", "IM_USED", "MULTIPLE_CHOICES", "MOVED_PERMANENTLY", "FOUND", "SEE_OTHER", "NOT_MODIFIED", "USE_PROXY", "TEMPORARY_REDIRECT", "PERMANENT_REDIRECT", "BAD_REQUEST", "UNAUTHORIZED", "FORBIDDEN", "NOT_FOUND", "METHOD_NOT_ALLOWED", "NOT_ACCEPTABLE", "PROXY_AUTHENTICATION_REQUIRED", "REQUEST_TIMEOUT", "CONFLICT", "GONE", "LENGTH_REQUIRED", "PRECONDITION_FAILED", "PAYLOAD_TOO_LARGE", "URI_TOO_LONG", "UNSUPPORTED_MEDIA_TYPE", "REQUESTED_RANGE_NOT_SATISFIABLE", "EXPECTATION_FAILED", "I_AM_A_TEAPOT", "UNPROCESSABLE_ENTITY", "LOCKED", "FAILED_DEPENDENCY", "UPGRADE_REQUIRED", "PRECONDITION_REQUIRED", "TOO_MANY_REQUESTS", "REQUEST_HEADER_FIELDS_TOO_LARGE", "UNAVAILABLE_FOR_LEGAL_REASONS", "INTERNAL_SERVER_ERROR", "NOT_IMPLEMENTED", "BAD_GATEWAY", "SERVICE_UNAVAILABLE", "GATEWAY_TIMEOUT", "HTTP_VERSION_NOT_SUPPORTED", "VARIANT_ALSO_NEGOTIATES", "INSUFFICIENT_STORAGE", "LOOP_DETECTED", "BANDWIDTH_LIMIT_EXCEEDED", "NOT_EXTENDED", "NETWORK_AUTHENTICATION_REQUIRED"]
        description: "HTTP 状态码"
      url:
        type: string
        format: uri
        description: "重定向目标 URL，RedirectTo 需要"
    oneOf:
      - required: ["status"]  # SetStatus
      - required: ["status", "url"]  # RedirectTo

---
# 安全过滤器类型
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterType
metadata:
  name: security-filters
spec:
  type: "SecurityFilters"
  version: "v1"
  extends: "Filter"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["SecureHeaders", "RequestHeaderSize", "RequestSize"]
      maxSize:
        type: string
        pattern: "^\\d+[BKMGT]?$"
        description: "最大大小，支持 B/K/M/G/T 后缀"
      errorHeaderName:
        type: string
        default: "errorMessage"
        description: "错误信息响应头名称"
      disabledHeaders:
        type: array
        items:
          type: string
          enum: ["x-frame-options", "strict-transport-security", "x-content-type-options", "x-xss-protection", "referrer-policy", "content-security-policy", "x-download-options", "x-permitted-cross-domain-policies"]
        description: "要禁用的安全头列表"
    oneOf:
      - required: []  # SecureHeaders
      - required: ["maxSize"]  # RequestHeaderSize, RequestSize

---
# 限流和熔断过滤器类型
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterType
metadata:
  name: rate-limit-circuit-breaker-filters
spec:
  type: "RateLimitCircuitBreakerFilters"
  version: "v1"
  extends: "Filter"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["RequestRateLimiter", "CircuitBreaker"]
      rateLimiter:
        type: object
        properties:
          type:
            type: string
            enum: ["redis", "in-memory"]
            default: "redis"
          replenishRate:
            type: integer
            minimum: 1
            description: "每秒允许的请求数"
          burstCapacity:
            type: integer
            minimum: 1
            description: "突发容量"
          requestedTokens:
            type: integer
            minimum: 1
            default: 1
            description: "每个请求消耗的令牌数"
          keyResolver:
            type: string
            description: "键解析器 Bean 名称"
      circuitBreaker:
        type: object
        properties:
          name:
            type: string
            description: "熔断器名称"
          failureRateThreshold:
            type: number
            minimum: 0
            maximum: 100
            default: 50
            description: "失败率阈值"
          waitDurationInOpenState:
            type: string
            default: "30s"
            description: "开启状态等待时间"
          slidingWindowSize:
            type: integer
            minimum: 1
            default: 10
            description: "滑动窗口大小"
          minimumNumberOfCalls:
            type: integer
            minimum: 1
            default: 5
            description: "最小调用次数"
    oneOf:
      - required: ["rateLimiter"]  # RequestRateLimiter
      - required: ["circuitBreaker"]  # CircuitBreaker

---
# 缓存过滤器类型
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterType
metadata:
  name: cache-filters
spec:
  type: "CacheFilters"
  version: "v1"
  extends: "Filter"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["LocalResponseCache", "CacheRequestBody"]
      timeToLive:
        type: string
        pattern: "^\\d+[smhd]?$"
        description: "缓存生存时间，支持 s/m/h/d 后缀"
      maxSize:
        type: string
        pattern: "^\\d+[BKMGT]?$"
        description: "最大缓存大小，支持 B/K/M/G/T 后缀"
      size:
        type: integer
        minimum: 1
        description: "缓存大小（字节），CacheRequestBody 需要"
    oneOf:
      - required: ["timeToLive", "maxSize"]  # LocalResponseCache
      - required: ["size"]  # CacheRequestBody

---
# 重试过滤器类型
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterType
metadata:
  name: retry-filter
spec:
  type: "RetryFilter"
  version: "v1"
  extends: "Filter"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["Retry"]
        default: "Retry"
      retries:
        type: integer
        minimum: 1
        maximum: 10
        default: 3
        description: "重试次数"
      statuses:
        type: array
        items:
          oneOf:
            - type: integer
              minimum: 100
              maximum: 599
            - type: string
              enum: ["BAD_GATEWAY", "INTERNAL_SERVER_ERROR", "SERVICE_UNAVAILABLE", "GATEWAY_TIMEOUT"]
        description: "需要重试的状态码"
      methods:
        type: array
        items:
          type: string
          enum: ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"]
        description: "需要重试的 HTTP 方法"
      backoff:
        type: object
        properties:
          firstBackoff:
            type: string
            default: "50ms"
            description: "首次退避时间"
          maxBackoff:
            type: string
            default: "500ms"
            description: "最大退避时间"
          factor:
            type: number
            minimum: 1
            default: 2
            description: "退避因子"
          basedOnPreviousValue:
            type: boolean
            default: false
            description: "是否基于前一次值"
    required: ["retries"]
```

## 3. 类型化 CRD 资源定义

### 3.1 类型化路由配置

```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: tigatewaytypedrouteconfigs.tigateway.cn
spec:
  group: tigateway.cn
  versions:
    - name: v1
      served: true
      storage: true
      schema:
        openAPIV3Schema:
          type: object
          properties:
            spec:
              type: object
              properties:
                routes:
                  type: array
                  items:
                    type: object
                    properties:
                      id:
                        type: string
                        minLength: 1
                        description: "路由唯一标识符"
                      uri:
                        type: string
                        format: uri
                        description: "目标服务 URI"
                      predicates:
                        type: array
                        items:
                          oneOf:
                            # Path 谓词
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["PathPredicate"]
                                patterns:
                                  type: array
                                  items:
                                    type: string
                                    pattern: "^/.*"
                                  minItems: 1
                                matchTrailingSlash:
                                  type: boolean
                                  default: true
                                caseSensitive:
                                  type: boolean
                                  default: true
                              required: ["type", "patterns"]
                            
                            # Method 谓词
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["MethodPredicate"]
                                methods:
                                  type: array
                                  items:
                                    type: string
                                    enum: ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE"]
                                  minItems: 1
                              required: ["type", "methods"]
                            
                            # Header 谓词
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["HeaderPredicate"]
                                headerName:
                                  type: string
                                  minLength: 1
                                regexp:
                                  type: string
                                caseSensitive:
                                  type: boolean
                                  default: true
                              required: ["type", "headerName"]
                            
                            # Cookie 谓词
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["CookiePredicate"]
                                cookieName:
                                  type: string
                                  minLength: 1
                                regexp:
                                  type: string
                              required: ["type", "cookieName", "regexp"]
                            
                            # Host 谓词
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["HostPredicate"]
                                patterns:
                                  type: array
                                  items:
                                    type: string
                                  minItems: 1
                                caseSensitive:
                                  type: boolean
                                  default: true
                              required: ["type", "patterns"]
                            
                            # Query 谓词
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["QueryPredicate"]
                                param:
                                  type: string
                                  minLength: 1
                                regexp:
                                  type: string
                                caseSensitive:
                                  type: boolean
                                  default: true
                              required: ["type", "param"]
                            
                            # RemoteAddr 谓词
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["RemoteAddrPredicate"]
                                sources:
                                  type: array
                                  items:
                                    type: string
                                    pattern: "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?:/[0-9]{1,2})?$|^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}(?:/[0-9]{1,3})?$"
                                  minItems: 1
                                resolver:
                                  type: object
                                  properties:
                                    type:
                                      type: string
                                      enum: ["default", "XForwardedRemoteAddressResolver"]
                                      default: "default"
                                    maxTrustedIndex:
                                      type: integer
                                      minimum: 1
                              required: ["type", "sources"]
                            
                            # 时间谓词
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["TimePredicate"]
                                timeType:
                                  type: string
                                  enum: ["After", "Before", "Between"]
                                datetime:
                                  type: string
                                  format: date-time
                                startTime:
                                  type: string
                                  format: date-time
                                endTime:
                                  type: string
                                  format: date-time
                                timezone:
                                  type: string
                                  default: "UTC"
                              oneOf:
                                - required: ["type", "timeType", "datetime"]
                                - required: ["type", "timeType", "startTime", "endTime"]
                            
                            # Weight 谓词
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["WeightPredicate"]
                                group:
                                  type: string
                                  minLength: 1
                                weight:
                                  type: integer
                                  minimum: 1
                                  maximum: 100
                              required: ["type", "group", "weight"]
                      
                      filters:
                        type: array
                        items:
                          oneOf:
                            # 请求头过滤器
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["RequestHeaderFilter"]
                                filterType:
                                  type: string
                                  enum: ["AddRequestHeader", "SetRequestHeader", "RemoveRequestHeader", "MapRequestHeader"]
                                headerName:
                                  type: string
                                  minLength: 1
                                value:
                                  type: string
                                fromHeader:
                                  type: string
                                toHeader:
                                  type: string
                                overwrite:
                                  type: boolean
                                  default: true
                              oneOf:
                                - required: ["type", "filterType", "headerName", "value"]
                                - required: ["type", "filterType", "headerName"]
                                - required: ["type", "filterType", "fromHeader", "toHeader"]
                            
                            # 响应头过滤器
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["ResponseHeaderFilter"]
                                filterType:
                                  type: string
                                  enum: ["AddResponseHeader", "SetResponseHeader", "RemoveResponseHeader", "RewriteResponseHeader", "DedupeResponseHeader"]
                                headerName:
                                  type: string
                                  minLength: 1
                                value:
                                  type: string
                                regexp:
                                  type: string
                                replacement:
                                  type: string
                                headers:
                                  type: array
                                  items:
                                    type: string
                              oneOf:
                                - required: ["type", "filterType", "headerName", "value"]
                                - required: ["type", "filterType", "headerName"]
                                - required: ["type", "filterType", "headerName", "regexp", "replacement"]
                                - required: ["type", "filterType", "headers"]
                            
                            # 路径处理过滤器
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["PathFilter"]
                                filterType:
                                  type: string
                                  enum: ["PrefixPath", "StripPrefix", "SetPath", "RewritePath"]
                                prefix:
                                  type: string
                                  pattern: "^/.*"
                                parts:
                                  type: integer
                                  minimum: 1
                                template:
                                  type: string
                                  pattern: "^/.*"
                                regexp:
                                  type: string
                                replacement:
                                  type: string
                              oneOf:
                                - required: ["type", "filterType", "prefix"]
                                - required: ["type", "filterType", "parts"]
                                - required: ["type", "filterType", "template"]
                                - required: ["type", "filterType", "regexp", "replacement"]
                            
                            # 状态和重定向过滤器
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["StatusRedirectFilter"]
                                filterType:
                                  type: string
                                  enum: ["SetStatus", "RedirectTo"]
                                status:
                                  oneOf:
                                    - type: integer
                                      minimum: 100
                                      maximum: 599
                                    - type: string
                                      enum: ["OK", "CREATED", "BAD_REQUEST", "UNAUTHORIZED", "FORBIDDEN", "NOT_FOUND", "INTERNAL_SERVER_ERROR", "BAD_GATEWAY", "SERVICE_UNAVAILABLE"]
                                url:
                                  type: string
                                  format: uri
                                                 url:
                                  type: string
                                  format: uri
                              oneOf:
                                - required: ["type", "filterType", "status"]
                                - required: ["type", "filterType", "status", "url"]
                            
                            # 安全过滤器
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["SecurityFilter"]
                                filterType:
                                  type: string
                                  enum: ["SecureHeaders", "RequestHeaderSize", "RequestSize"]
                                maxSize:
                                  type: string
                                  pattern: "^\\d+[BKMGT]?$"
                                errorHeaderName:
                                  type: string
                                  default: "errorMessage"
                                disabledHeaders:
                                  type: array
                                  items:
                                    type: string
                                    enum: ["x-frame-options", "strict-transport-security", "x-content-type-options", "x-xss-protection", "referrer-policy", "content-security-policy", "x-download-options", "x-permitted-cross-domain-policies"]
                              oneOf:
                                - required: ["type", "filterType"]
                                - required: ["type", "filterType", "maxSize"]
                            
                            # 限流和熔断过滤器
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["RateLimitCircuitBreakerFilter"]
                                filterType:
                                  type: string
                                  enum: ["RequestRateLimiter", "CircuitBreaker"]
                                rateLimiter:
                                  type: object
                                  properties:
                                    type:
                                      type: string
                                      enum: ["redis", "in-memory"]
                                      default: "redis"
                                    replenishRate:
                                      type: integer
                                      minimum: 1
                                    burstCapacity:
                                      type: integer
                                      minimum: 1
                                    requestedTokens:
                                      type: integer
                                      minimum: 1
                                      default: 1
                                    keyResolver:
                                      type: string
                                circuitBreaker:
                                  type: object
                                  properties:
                                    name:
                                      type: string
                                    failureRateThreshold:
                                      type: number
                                      minimum: 0
                                      maximum: 100
                                      default: 50
                                    waitDurationInOpenState:
                                      type: string
                                      default: "30s"
                                    slidingWindowSize:
                                      type: integer
                                      minimum: 1
                                      default: 10
                                    minimumNumberOfCalls:
                                      type: integer
                                      minimum: 1
                                      default: 5
                              oneOf:
                                - required: ["type", "filterType", "rateLimiter"]
                                - required: ["type", "filterType", "circuitBreaker"]
                            
                            # 缓存过滤器
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["CacheFilter"]
                                filterType:
                                  type: string
                                  enum: ["LocalResponseCache", "CacheRequestBody"]
                                timeToLive:
                                  type: string
                                  pattern: "^\\d+[smhd]?$"
                                maxSize:
                                  type: string
                                  pattern: "^\\d+[BKMGT]?$"
                                size:
                                  type: integer
                                  minimum: 1
                              oneOf:
                                - required: ["type", "filterType", "timeToLive", "maxSize"]
                                - required: ["type", "filterType", "size"]
                            
                            # 重试过滤器
                            - type: object
                              properties:
                                type:
                                  type: string
                                  enum: ["RetryFilter"]
                                filterType:
                                  type: string
                                  enum: ["Retry"]
                                retries:
                                  type: integer
                                  minimum: 1
                                  maximum: 10
                                  default: 3
                                statuses:
                                  type: array
                                  items:
                                    oneOf:
                                      - type: integer
                                        minimum: 100
                                        maximum: 599
                                      - type: string
                                        enum: ["BAD_GATEWAY", "INTERNAL_SERVER_ERROR", "SERVICE_UNAVAILABLE", "GATEWAY_TIMEOUT"]
                                methods:
                                  type: array
                                  items:
                                    type: string
                                    enum: ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"]
                                backoff:
                                  type: object
                                  properties:
                                    firstBackoff:
                                      type: string
                                      default: "50ms"
                                    maxBackoff:
                                      type: string
                                      default: "500ms"
                                    factor:
                                      type: number
                                      minimum: 1
                                      default: 2
                                    basedOnPreviousValue:
                                      type: boolean
                                      default: false
                              required: ["type", "filterType", "retries"]
                      
                      order:
                        type: integer
                        minimum: 0
                        maximum: 2147483647
                        default: 0
                        description: "路由顺序"
                      
                      metadata:
                        type: object
                        properties:
                          description:
                            type: string
                            description: "路由描述"
                          tags:
                            type: array
                            items:
                              type: string
                            description: "路由标签"
                          responseTimeout:
                            type: integer
                            minimum: 1
                            description: "响应超时时间（毫秒）"
                          connectTimeout:
                            type: integer
                            minimum: 1
                            description: "连接超时时间（毫秒）"
                          cors:
                            type: object
                            properties:
                              allowedOrigins:
                                type: array
                                items:
                                  type: string
                              allowedMethods:
                                type: array
                                items:
                                  type: string
                                  enum: ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"]
                              allowedHeaders:
                                type: array
                                items:
                                  type: string
                              maxAge:
                                type: integer
                                minimum: 0
                    required: ["id", "uri"]
                
                globalCors:
                  type: object
                  properties:
                    corsConfigurations:
                      type: object
                      additionalProperties:
                        type: object
                        properties:
                          allowedOrigins:
                            type: array
                            items:
                              type: string
                          allowedMethods:
                            type: array
                            items:
                              type: string
                              enum: ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"]
                          allowedHeaders:
                            type: array
                            items:
                              type: string
                          allowCredentials:
                            type: boolean
                            default: false
                          maxAge:
                            type: integer
                            minimum: 0
                    addToSimpleUrlHandlerMapping:
                      type: boolean
                      default: false
                
                defaultFilters:
                  type: array
                  items:
                    oneOf:
                      # 复用过滤器类型定义
                      - $ref: "#/definitions/RequestHeaderFilter"
                      - $ref: "#/definitions/ResponseHeaderFilter"
                      - $ref: "#/definitions/PathFilter"
                      - $ref: "#/definitions/StatusRedirectFilter"
                      - $ref: "#/definitions/SecurityFilter"
                      - $ref: "#/definitions/RateLimitCircuitBreakerFilter"
                      - $ref: "#/definitions/CacheFilter"
                      - $ref: "#/definitions/RetryFilter"
                
                httpClient:
                  type: object
                  properties:
                    connectTimeout:
                      type: integer
                      minimum: 1
                      description: "连接超时时间（毫秒）"
                    responseTimeout:
                      type: string
                      pattern: "^\\d+[smhd]?$"
                      description: "响应超时时间"
                
                secureHeaders:
                  type: object
                  properties:
                    xssProtectionHeader:
                      type: string
                      default: "1; mode=block"
                    strictTransportSecurity:
                      type: string
                      default: "max-age=31536000; includeSubDomains"
                    frameOptions:
                      type: string
                      enum: ["DENY", "SAMEORIGIN", "ALLOW-FROM"]
                      default: "DENY"
                    contentTypeOptions:
                      type: string
                      default: "nosniff"
                    referrerPolicy:
                      type: string
                      enum: ["no-referrer", "no-referrer-when-downgrade", "origin", "origin-when-cross-origin", "same-origin", "strict-origin", "strict-origin-when-cross-origin", "unsafe-url"]
                      default: "no-referrer"
                    contentSecurityPolicy:
                      type: string
                      default: "default-src 'self'"
                    downloadOptions:
                      type: string
                      default: "noopen"
                    permittedCrossDomainPolicies:
                      type: string
                      enum: ["none", "master-only", "by-content-type", "all"]
                      default: "none"
                    disable:
                      type: array
                      items:
                        type: string
                        enum: ["x-frame-options", "strict-transport-security", "x-content-type-options", "x-xss-protection", "referrer-policy", "content-security-policy", "x-download-options", "x-permitted-cross-domain-policies"]
              
              definitions:
                RequestHeaderFilter:
                  type: object
                  properties:
                    type:
                      type: string
                      enum: ["RequestHeaderFilter"]
                    filterType:
                      type: string
                      enum: ["AddRequestHeader", "SetRequestHeader", "RemoveRequestHeader", "MapRequestHeader"]
                    headerName:
                      type: string
                      minLength: 1
                    value:
                      type: string
                    fromHeader:
                      type: string
                    toHeader:
                      type: string
                    overwrite:
                      type: boolean
                      default: true
                  oneOf:
                    - required: ["type", "filterType", "headerName", "value"]
                    - required: ["type", "filterType", "headerName"]
                    - required: ["type", "filterType", "fromHeader", "toHeader"]
                
                ResponseHeaderFilter:
                  type: object
                  properties:
                    type:
                      type: string
                      enum: ["ResponseHeaderFilter"]
                    filterType:
                      type: string
                      enum: ["AddResponseHeader", "SetResponseHeader", "RemoveResponseHeader", "RewriteResponseHeader", "DedupeResponseHeader"]
                    headerName:
                      type: string
                      minLength: 1
                    value:
                      type: string
                    regexp:
                      type: string
                    replacement:
                      type: string
                    headers:
                      type: array
                      items:
                        type: string
                  oneOf:
                    - required: ["type", "filterType", "headerName", "value"]
                    - required: ["type", "filterType", "headerName"]
                    - required: ["type", "filterType", "headerName", "regexp", "replacement"]
                    - required: ["type", "filterType", "headers"]
                
                PathFilter:
                  type: object
                  properties:
                    type:
                      type: string
                      enum: ["PathFilter"]
                    filterType:
                      type: string
                      enum: ["PrefixPath", "StripPrefix", "SetPath", "RewritePath"]
                    prefix:
                      type: string
                      pattern: "^/.*"
                    parts:
                      type: integer
                      minimum: 1
                    template:
                      type: string
                      pattern: "^/.*"
                    regexp:
                      type: string
                    replacement:
                      type: string
                  oneOf:
                    - required: ["type", "filterType", "prefix"]
                    - required: ["type", "filterType", "parts"]
                    - required: ["type", "filterType", "template"]
                    - required: ["type", "filterType", "regexp", "replacement"]
                
                StatusRedirectFilter:
                  type: object
                  properties:
                    type:
                      type: string
                      enum: ["StatusRedirectFilter"]
                    filterType:
                      type: string
                      enum: ["SetStatus", "RedirectTo"]
                    status:
                      oneOf:
                        - type: integer
                          minimum: 100
                          maximum: 599
                        - type: string
                          enum: ["OK", "CREATED", "BAD_REQUEST", "UNAUTHORIZED", "FORBIDDEN", "NOT_FOUND", "INTERNAL_SERVER_ERROR", "BAD_GATEWAY", "SERVICE_UNAVAILABLE"]
                    url:
                      type: string
                      format: uri
                  oneOf:
                    - required: ["type", "filterType", "status"]
                    - required: ["type", "filterType", "status", "url"]
                
                SecurityFilter:
                  type: object
                  properties:
                    type:
                      type: string
                      enum: ["SecurityFilter"]
                    filterType:
                      type: string
                      enum: ["SecureHeaders", "RequestHeaderSize", "RequestSize"]
                    maxSize:
                      type: string
                      pattern: "^\\d+[BKMGT]?$"
                    errorHeaderName:
                      type: string
                      default: "errorMessage"
                    disabledHeaders:
                      type: array
                      items:
                        type: string
                        enum: ["x-frame-options", "strict-transport-security", "x-content-type-options", "x-xss-protection", "referrer-policy", "content-security-policy", "x-download-options", "x-permitted-cross-domain-policies"]
                  oneOf:
                    - required: ["type", "filterType"]
                    - required: ["type", "filterType", "maxSize"]
                
                RateLimitCircuitBreakerFilter:
                  type: object
                  properties:
                    type:
                      type: string
                      enum: ["RateLimitCircuitBreakerFilter"]
                    filterType:
                      type: string
                      enum: ["RequestRateLimiter", "CircuitBreaker"]
                    rateLimiter:
                      type: object
                      properties:
                        type:
                          type: string
                          enum: ["redis", "in-memory"]
                          default: "redis"
                        replenishRate:
                          type: integer
                          minimum: 1
                        burstCapacity:
                          type: integer
                          minimum: 1
                        requestedTokens:
                          type: integer
                          minimum: 1
                          default: 1
                        keyResolver:
                          type: string
                    circuitBreaker:
                      type: object
                      properties:
                        name:
                          type: string
                        failureRateThreshold:
                          type: number
                          minimum: 0
                          maximum: 100
                          default: 50
                        waitDurationInOpenState:
                          type: string
                          default: "30s"
                        slidingWindowSize:
                          type: integer
                          minimum: 1
                          default: 10
                        minimumNumberOfCalls:
                          type: integer
                          minimum: 1
                          default: 5
                  oneOf:
                    - required: ["type", "filterType", "rateLimiter"]
                    - required: ["type", "filterType", "circuitBreaker"]
                
                CacheFilter:
                  type: object
                  properties:
                    type:
                      type: string
                      enum: ["CacheFilter"]
                    filterType:
                      type: string
                      enum: ["LocalResponseCache", "CacheRequestBody"]
                    timeToLive:
                      type: string
                      pattern: "^\\d+[smhd]?$"
                    maxSize:
                      type: string
                      pattern: "^\\d+[BKMGT]?$"
                    size:
                      type: integer
                      minimum: 1
                  oneOf:
                    - required: ["type", "filterType", "timeToLive", "maxSize"]
                    - required: ["type", "filterType", "size"]
                
                RetryFilter:
                  type: object
                  properties:
                    type:
                      type: string
                      enum: ["RetryFilter"]
                    filterType:
                      type: string
                      enum: ["Retry"]
                    retries:
                      type: integer
                      minimum: 1
                      maximum: 10
                      default: 3
                    statuses:
                      type: array
                      items:
                        oneOf:
                          - type: integer
                            minimum: 100
                            maximum: 599
                          - type: string
                            enum: ["BAD_GATEWAY", "INTERNAL_SERVER_ERROR", "SERVICE_UNAVAILABLE", "GATEWAY_TIMEOUT"]
                    methods:
                      type: array
                      items:
                        type: string
                        enum: ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"]
                    backoff:
                      type: object
                      properties:
                        firstBackoff:
                          type: string
                          default: "50ms"
                        maxBackoff:
                          type: string
                          default: "500ms"
                        factor:
                          type: number
                          minimum: 1
                          default: 2
                        basedOnPreviousValue:
                          type: boolean
                          default: false
                  required: ["type", "filterType", "retries"]
  
  scope: Namespaced
  names:
    plural: tigatewaytypedrouteconfigs
    singular: tigatewaytypedrouteconfig
    kind: TiGatewayTypedRouteConfig
    shortNames:
      - tgtrc
      - tgtrcs
```

## 4. 类型化配置示例

### 4.1 基础路由配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: basic-typed-routes
  namespace: tigateway
spec:
  routes:
    - id: user-service-route
      uri: http://user-service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/api/users/**"
          matchTrailingSlash: true
          caseSensitive: true
        - type: MethodPredicate
          methods:
            - GET
            - POST
            - PUT
            - DELETE
        - type: HeaderPredicate
          headerName: "X-API-Version"
          regexp: "v[12]"
          caseSensitive: true
      filters:
        - type: PathFilter
          filterType: StripPrefix
          parts: 2
        - type: RequestHeaderFilter
          filterType: AddRequestHeader
          headerName: "X-Service"
          value: "user-service"
        - type: ResponseHeaderFilter
          filterType: AddResponseHeader
          headerName: "X-Response-Time"
          value: "${responseTime}"
        - type: SecurityFilter
          filterType: SecureHeaders
      order: 1
      metadata:
        description: "用户服务路由"
        tags: ["user", "api", "v1"]
        responseTimeout: 5000
        connectTimeout: 2000
```

### 4.2 复杂路由配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: complex-typed-routes
  namespace: tigateway
spec:
  routes:
    - id: api-gateway-route
      uri: lb://api-service
      predicates:
        - type: PathPredicate
          patterns:
            - "/api/**"
          matchTrailingSlash: true
        - type: MethodPredicate
          methods:
            - GET
            - POST
        - type: HeaderPredicate
          headerName: "X-Request-Id"
          regexp: "\\d+"
        - type: CookiePredicate
          cookieName: "session"
          regexp: ".*"
        - type: HostPredicate
          patterns:
            - "api.example.com"
            - "*.api.example.com"
        - type: QueryPredicate
          param: "version"
          regexp: "v[12]"
        - type: RemoteAddrPredicate
          sources:
            - "192.168.1.0/24"
            - "10.0.0.0/8"
          resolver:
            type: XForwardedRemoteAddressResolver
            maxTrustedIndex: 1
        - type: TimePredicate
          timeType: After
          datetime: "2024-01-01T00:00:00Z"
          timezone: "UTC"
        - type: WeightPredicate
          group: "production"
          weight: 80
      filters:
        - type: RequestHeaderFilter
          filterType: AddRequestHeader
          headerName: "X-Gateway"
          value: "TiGateway"
        - type: RequestHeaderFilter
          filterType: AddRequestHeadersIfNotPresent
          headerName: "X-Request-Color-1"
          value: "blue"
        - type: ResponseHeaderFilter
          filterType: AddResponseHeader
          headerName: "X-Response-Time"
          value: "${responseTime}"
        - type: RateLimitCircuitBreakerFilter
          filterType: RequestRateLimiter
          rateLimiter:
            type: redis
            replenishRate: 10
            burstCapacity: 20
            requestedTokens: 1
            keyResolver: "userKeyResolver"
        - type: RateLimitCircuitBreakerFilter
          filterType: CircuitBreaker
          circuitBreaker:
            name: "apiServiceBreaker"
            failureRateThreshold: 50
            waitDurationInOpenState: "30s"
            slidingWindowSize: 10
            minimumNumberOfCalls: 5
        - type: CacheFilter
          filterType: LocalResponseCache
          timeToLive: "30m"
          maxSize: "500MB"
        - type: RetryFilter
          filterType: Retry
          retries: 3
          statuses:
            - BAD_GATEWAY
            - INTERNAL_SERVER_ERROR
          methods:
            - GET
            - POST
          backoff:
            firstBackoff: "100ms"
            maxBackoff: "1s"
            factor: 2
            basedOnPreviousValue: false
      order: 0
      metadata:
        description: "API网关路由"
        tags: ["api", "gateway"]
        cors:
          allowedOrigins: ["*"]
          allowedMethods: ["GET", "POST"]
          allowedHeaders: ["*"]
          maxAge: 30
```

### 4.3 全局配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: global-typed-config
  namespace: tigateway
spec:
  globalCors:
    corsConfigurations:
      "[/**]":
        allowedOrigins: ["https://docs.spring.io"]
        allowedMethods: ["GET", "POST"]
        allowedHeaders: ["*"]
        allowCredentials: true
        maxAge: 3600
    addToSimpleUrlHandlerMapping: true
  
  defaultFilters:
    - type: RequestHeaderFilter
      filterType: AddRequestHeader
      headerName: "X-Gateway"
      value: "TiGateway"
    - type: ResponseHeaderFilter
      filterType: AddResponseHeader
      headerName: "X-Response-Time"
      value: "${responseTime}"
    - type: SecurityFilter
      filterType: SecureHeaders
  
  httpClient:
    connectTimeout: 1000
    responseTimeout: "5s"
  
  secureHeaders:
    xssProtectionHeader: "1; mode=block"
    strictTransportSecurity: "max-age=31536000; includeSubDomains"
    frameOptions: "SAMEORIGIN"
    contentTypeOptions: "nosniff"
    referrerPolicy: "strict-origin-when-cross-origin"
    contentSecurityPolicy: "default-src 'self'"
    disable: ["x-frame-options"]
```

## 5. 类型系统优势

### 5.1 类型安全

```yaml
# ❌ 字符串配置 - 容易出错
predicates:
  - Path=/api/users/**  # 拼写错误：应该是 Path
  - Method=GET,POST     # 格式错误：应该是数组

# ✅ 类型化配置 - 类型安全
predicates:
  - type: PathPredicate
    patterns:
      - "/api/users/**"
  - type: MethodPredicate
    methods:
      - GET
      - POST
```

### 5.2 IDE 支持

```yaml
# IDE 自动补全和验证
predicates:
  - type: PathPredicate  # IDE 会提示可用的谓词类型
    patterns:            # IDE 会提示必需的字段
      - "/api/users/**"
    matchTrailingSlash:  # IDE 会提示可选字段
      true
```

### 5.3 配置验证

```yaml
# CRD 层面的验证
filters:
  - type: RequestHeaderFilter
    filterType: AddRequestHeader
    headerName: "X-Service"  # 必需字段
    value: "user-service"    # 必需字段
    # 缺少必需字段会导致 CRD 验证失败
```

### 5.4 文档生成

```yaml
# 自动生成 API 文档
metadata:
  description: "用户服务路由"  # 用于生成文档
  tags: ["user", "api", "v1"]  # 用于分类和搜索
```

## 6. 迁移策略

### 6.1 向后兼容

```yaml
# 支持两种配置方式
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: hybrid-config
spec:
  routes:
    # 传统字符串配置
    - id: legacy-route
      uri: http://legacy-service:8080
      predicates:
        - Path=/legacy/**
      filters:
        - StripPrefix=1
    
    # 新的类型化配置
    - id: typed-route
      uri: http://typed-service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/typed/**"
      filters:
        - type: PathFilter
          filterType: StripPrefix
          parts: 1
```

### 6.2 渐进式迁移

```yaml
# 步骤1：启用类型化配置
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: migration-step1
  annotations:
    tigateway.cn/migration-mode: "hybrid"
spec:
  # 现有配置保持不变
  routes:
    - id: existing-route
      uri: http://service:8080
      predicates:
        - Path=/api/**
      filters:
        - StripPrefix=1

---
# 步骤2：逐步迁移到类型化配置
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: migration-step2
spec:
  routes:
    - id: migrated-route
      uri: http://service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/api/**"
      filters:
        - type: PathFilter
          filterType: StripPrefix
          parts: 1
```

## 7. 自定义类型扩展

### 7.1 自定义谓词类型

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayPredicateType
metadata:
  name: custom-business-predicate
spec:
  type: "BusinessPredicate"
  version: "v1"
  extends: "Predicate"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["Business"]
        default: "Business"
      businessType:
        type: string
        enum: ["VIP", "PREMIUM", "STANDARD"]
        description: "业务类型"
      region:
        type: string
        enum: ["US", "EU", "ASIA"]
        description: "地区"
      timeWindow:
        type: object
        properties:
          start:
            type: string
            format: time
          end:
            type: string
            format: time
        description: "时间窗口"
    required: ["businessType", "region"]
```

### 7.2 自定义过滤器类型

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterType
metadata:
  name: custom-business-filter
spec:
  type: "BusinessFilter"
  version: "v1"
  extends: "Filter"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["BusinessAuth", "BusinessRateLimit", "BusinessTransform"]
        default: "BusinessAuth"
      businessRules:
        type: array
        items:
          type: object
          properties:
            ruleId:
              type: string
            condition:
              type: string
            action:
              type: string
              enum: ["ALLOW", "DENY", "TRANSFORM", "RATE_LIMIT"]
            parameters:
              type: object
        description: "业务规则列表"
      fallbackAction:
        type: string
        enum: ["ALLOW", "DENY"]
        default: "DENY"
        description: "回退动作"
    required: ["businessRules"]
```

### 7.3 使用自定义类型

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: custom-business-routes
spec:
  routes:
    - id: business-route
      uri: http://business-service:8080
      predicates:
        - type: BusinessPredicate
          businessType: "VIP"
          region: "US"
          timeWindow:
            start: "09:00"
            end: "18:00"
      filters:
        - type: BusinessFilter
          name: "BusinessAuth"
          businessRules:
            - ruleId: "vip-access"
              condition: "user.level == 'VIP'"
              action: "ALLOW"
            - ruleId: "region-check"
              condition: "user.region == 'US'"
              action: "ALLOW"
          fallbackAction: "DENY"
```

## 8. 工具支持

### 8.1 配置验证工具

```bash
# 验证类型化配置
kubectl apply --dry-run=server -f typed-route-config.yaml

# 使用自定义验证工具
tigateway validate --config typed-route-config.yaml --schema v1
```

### 8.2 配置转换工具

```bash
# 从字符串配置转换为类型化配置
tigateway convert --from legacy-config.yaml --to typed-config.yaml

# 验证转换结果
tigateway validate --config typed-config.yaml
```

### 8.3 IDE 插件

```yaml
# VS Code 插件配置
{
  "yaml.schemas": {
    "https://schemas.tigateway.cn/v1/typed-route-config.json": "**/typed-route-config.yaml"
  },
  "yaml.customTags": [
    "!Ref",
    "!GetAtt",
    "!Sub"
  ]
}
```

## 9. 性能考虑

### 9.1 配置解析性能

```yaml
# 优化配置解析
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: performance-optimized
  annotations:
    tigateway.cn/cache-enabled: "true"
    tigateway.cn/cache-ttl: "300"
spec:
  routes:
    # 使用预编译的正则表达式
    - id: optimized-route
      uri: http://service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/api/v1/users/**"  # 精确匹配，避免复杂正则
          matchTrailingSlash: false  # 减少匹配复杂度
```

### 9.2 内存使用优化

```yaml
# 减少内存使用
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: memory-optimized
spec:
  routes:
    # 使用轻量级配置
    - id: lightweight-route
      uri: http://service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/api/**"
      filters:
        - type: RequestHeaderFilter
          filterType: AddRequestHeader
          headerName: "X-Service"
          value: "lightweight"
      # 最小化元数据
      metadata:
        description: "轻量级路由"
```

### 9.3 缓存策略

```yaml
# 配置缓存策略
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: cache-strategy
  annotations:
    tigateway.cn/cache-strategy: "aggressive"
    tigateway.cn/cache-ttl: "600"
spec:
  routes:
    - id: cached-route
      uri: http://service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/api/static/**"
      filters:
        - type: CacheFilter
          filterType: LocalResponseCache
          timeToLive: "1h"
          maxSize: "1GB"
      metadata:
        cache:
          enabled: true
          ttl: "1h"
          maxSize: "1GB"
```

## 10. 最佳实践

### 10.1 配置组织

```yaml
# 按环境组织配置
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: production-routes
  namespace: tigateway-prod
  labels:
    environment: production
    version: v1.0.0
spec:
  routes:
    - id: prod-api-route
      uri: http://prod-api-service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/api/**"
        - type: RemoteAddrPredicate
          sources:
            - "10.0.0.0/8"
            - "172.16.0.0/12"
      filters:
        - type: SecurityFilter
          filterType: SecureHeaders
        - type: RateLimitCircuitBreakerFilter
          filterType: RequestRateLimiter
          rateLimiter:
            type: redis
            replenishRate: 100
            burstCapacity: 200
      metadata:
        environment: production
        criticality: high
        monitoring: enabled
```

### 10.2 配置版本管理

```yaml
# 配置版本管理
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: versioned-routes
  annotations:
    tigateway.cn/config-version: "v1.2.0"
    tigateway.cn/config-hash: "abc123def456"
    tigateway.cn/rollback-enabled: "true"
spec:
  routes:
    - id: versioned-route
      uri: http://service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/api/v1/**"
      filters:
        - type: RequestHeaderFilter
          filterType: AddRequestHeader
          headerName: "X-API-Version"
          value: "v1.2.0"
      metadata:
        version: "v1.2.0"
        compatibility:
          minVersion: "v1.0.0"
          maxVersion: "v2.0.0"
```

### 10.3 配置测试

```yaml
# 配置测试
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: test-routes
  namespace: tigateway-test
  annotations:
    tigateway.cn/test-mode: "true"
    tigateway.cn/test-timeout: "30s"
spec:
  routes:
    - id: test-route
      uri: http://test-service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/test/**"
        - type: HeaderPredicate
          headerName: "X-Test-Mode"
          regexp: "true"
      filters:
        - type: RequestHeaderFilter
          filterType: AddRequestHeader
          headerName: "X-Test-Environment"
          value: "test"
      metadata:
        test:
          enabled: true
          timeout: "30s"
          assertions:
            - type: "response_time"
              max: "1000ms"
            - type: "status_code"
              expected: 200
```

## 11. 监控和可观测性

### 11.1 配置监控

```yaml
# 配置监控
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: monitored-routes
  annotations:
    tigateway.cn/monitoring-enabled: "true"
    tigateway.cn/metrics-endpoint: "/actuator/prometheus"
spec:
  routes:
    - id: monitored-route
      uri: http://service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/api/**"
      filters:
        - type: RequestHeaderFilter
          filterType: AddRequestHeader
          headerName: "X-Monitoring"
          value: "enabled"
      metadata:
        monitoring:
          enabled: true
          metrics:
            - request_count
            - response_time
            - error_rate
            - throughput
          alerts:
            - type: "error_rate"
              threshold: 0.05
              duration: "5m"
            - type: "response_time"
              threshold: "2s"
              duration: "1m"
```

### 11.2 配置审计

```yaml
# 配置审计
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: audited-routes
  annotations:
    tigateway.cn/audit-enabled: "true"
    tigateway.cn/audit-level: "detailed"
spec:
  routes:
    - id: audited-route
      uri: http://service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/api/**"
      filters:
        - type: RequestHeaderFilter
          filterType: AddRequestHeader
          headerName: "X-Audit-ID"
          value: "${requestId}"
      metadata:
        audit:
          enabled: true
          level: "detailed"
          fields:
            - request_id
            - user_id
            - timestamp
            - action
            - resource
          retention: "90d"
```

## 12. 安全考虑

### 12.1 配置安全

```yaml
# 配置安全
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: secure-routes
  annotations:
    tigateway.cn/security-level: "high"
    tigateway.cn/encryption-enabled: "true"
spec:
  routes:
    - id: secure-route
      uri: https://secure-service:8443
      predicates:
        - type: PathPredicate
          patterns:
            - "/secure/**"
        - type: HeaderPredicate
          headerName: "Authorization"
          regexp: "Bearer .*"
      filters:
        - type: SecurityFilter
          filterType: SecureHeaders
        - type: RequestHeaderFilter
          filterType: AddRequestHeader
          headerName: "X-Security-Context"
          value: "${securityContext}"
      metadata:
        security:
          level: "high"
          encryption: "required"
          authentication: "required"
          authorization: "required"
```

### 12.2 配置访问控制

```yaml
# 配置访问控制
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: access-controlled-routes
  namespace: tigateway
spec:
  routes:
    - id: admin-route
      uri: http://admin-service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/admin/**"
        - type: RemoteAddrPredicate
          sources:
            - "192.168.1.0/24"
        - type: HeaderPredicate
          headerName: "X-Admin-Token"
          regexp: "admin-.*"
      filters:
        - type: SecurityFilter
          filterType: SecureHeaders
        - type: RequestHeaderFilter
          filterType: AddRequestHeader
          headerName: "X-Admin-Access"
          value: "granted"
      metadata:
        access:
          level: "admin"
          ipWhitelist:
            - "192.168.1.0/24"
          tokenRequired: true
          auditRequired: true
```

## 13. 故障排除

### 13.1 配置验证

```yaml
# 配置验证
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: validated-routes
  annotations:
    tigateway.cn/validation-enabled: "true"
    tigateway.cn/validation-strict: "true"
spec:
  routes:
    - id: validated-route
      uri: http://service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/api/**"
          matchTrailingSlash: true
          caseSensitive: true
      filters:
        - type: RequestHeaderFilter
          filterType: AddRequestHeader
          headerName: "X-Validated"
          value: "true"
      metadata:
        validation:
          enabled: true
          strict: true
          rules:
            - type: "uri_format"
              required: true
            - type: "predicate_syntax"
              required: true
            - type: "filter_syntax"
              required: true
```

### 13.2 配置调试

```yaml
# 配置调试
apiVersion: tigateway.cn/v1
kind: TiGatewayTypedRouteConfig
metadata:
  name: debug-routes
  annotations:
    tigateway.cn/debug-enabled: "true"
    tigateway.cn/debug-level: "verbose"
spec:
  routes:
    - id: debug-route
      uri: http://service:8080
      predicates:
        - type: PathPredicate
          patterns:
            - "/debug/**"
        - type: HeaderPredicate
          headerName: "X-Debug-Mode"
          regexp: "true"
      filters:
        - type: RequestHeaderFilter
          filterType: AddRequestHeader
          headerName: "X-Debug-Info"
          value: "${debugInfo}"
      metadata:
        debug:
          enabled: true
          level: "verbose"
          trace:
            - request_flow
            - predicate_evaluation
            - filter_execution
            - response_flow
```

## 14. 总结

### 14.1 类型化CRD的优势

1. **类型安全**: 提供编译时类型检查，减少配置错误
2. **IDE支持**: 自动补全、语法高亮、错误提示
3. **配置验证**: CRD层面的配置验证，确保配置正确性
4. **文档生成**: 自动生成API文档和配置说明
5. **可扩展性**: 支持自定义类型和扩展
6. **向后兼容**: 支持渐进式迁移

### 14.2 实施建议

1. **渐进式迁移**: 从简单路由开始，逐步迁移复杂配置
2. **工具支持**: 使用配置转换和验证工具
3. **团队培训**: 提供类型化配置的培训和文档
4. **监控告警**: 建立配置变更的监控和告警机制
5. **版本管理**: 实施配置版本管理和回滚策略

### 14.3 未来发展方向

1. **智能配置**: 基于AI的配置优化建议
2. **可视化配置**: 图形化配置界面
3. **配置模板**: 预定义的配置模板和最佳实践
4. **配置分析**: 配置性能分析和优化建议
5. **多环境管理**: 跨环境的配置管理和同步

---

**相关文档**:
- [CRD 配置抽象设计](./crd-configuration-design.md)
- [CRD 资源配置文档](./crd-resource-configuration.md)
- [CRD 谓词配置文档](./crd-predicate-configuration.md)
- [CRD 过滤器配置文档](./crd-filter-configuration.md)
- [Spring Cloud Gateway 集成指南](../development/spring-cloud-gateway-integration.md)
- [自定义组件开发指南](../development/custom-components.md)