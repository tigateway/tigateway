# TiGateway Typed CRD Design

## Overview

This document proposes a typed CRD design that abstracts predicates and filters from string rules to a structured type system. This design provides better type safety, IDE support, and configuration validation.

## Design Goals

1. **Type Safety**: Use structured types instead of strings
2. **IDE Support**: Provide auto-completion and syntax checking
3. **Configuration Validation**: Perform configuration validation at the CRD level
4. **Extensibility**: Support custom predicate and filter types
5. **Backward Compatibility**: Maintain compatibility with existing configurations

## Core Type System

### 1. Predicate Type System

#### 1.1 Base Predicate Interface

```yaml
# Base predicate interface
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

#### 1.2 Specific Predicate Type Definitions

```yaml
# Path predicate type
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
        minItems: 1
        description: "Path patterns to match"
      caseSensitive:
        type: boolean
        default: true
        description: "Whether path matching is case sensitive"
    required: ["patterns"]
    examples:
      - name: "Simple path match"
        patterns: ["/api/users/**"]
        caseSensitive: true
      - name: "Multiple path match"
        patterns: ["/api/users/**", "/api/orders/**"]
        caseSensitive: true
```

```yaml
# Method predicate type
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
        description: "HTTP methods to match"
    required: ["methods"]
    examples:
      - name: "Single method"
        methods: ["GET"]
      - name: "Multiple methods"
        methods: ["GET", "POST"]
```

```yaml
# Header predicate type
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
      headers:
        type: array
        items:
          type: object
          properties:
            name:
              type: string
              description: "Header name"
            value:
              type: string
              description: "Header value or regex pattern"
            regexp:
              type: boolean
              default: false
              description: "Whether value is a regex pattern"
          required: ["name", "value"]
        minItems: 1
        description: "Headers to match"
    required: ["headers"]
    examples:
      - name: "Exact header match"
        headers:
          - name: "X-API-Version"
            value: "v1"
            regexp: false
      - name: "Regex header match"
        headers:
          - name: "X-Request-Id"
            value: "\\d+"
            regexp: true
```

### 2. Filter Type System

#### 2.1 Base Filter Interface

```yaml
# Base filter interface
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
        enum: ["AddRequestHeader", "AddResponseHeader", "AddRequestParameter", "DedupeResponseHeader", "Hystrix", "CircuitBreaker", "FallbackHeaders", "MapRequestHeader", "PrefixPath", "PreserveHostHeader", "RequestRateLimiter", "RedirectTo", "RemoveRequestHeader", "RemoveResponseHeader", "RemoveRequestParameter", "RewritePath", "RewriteLocationResponseHeader", "RewriteResponseHeader", "SaveSession", "SecureHeaders", "SetPath", "SetRequestHeader", "SetResponseHeader", "SetStatus", "StripPrefix", "Retry", "RequestSize", "ModifyRequestBody", "ModifyResponseBody", "TokenRelay", "CacheRequestBody", "LocalResponseCache", "RequestHeaderToRequestUri", "RequestHeaderSize", "ResponseHeaderSize", "AddRequestHeadersIfNotPresent", "AddResponseHeadersIfNotPresent", "SetRequestHostHeader", "SetRequestHostHeaderIfAbsent"]
      order:
        type: integer
        minimum: -2147483648
        maximum: 2147483647
        default: 0
      enabled:
        type: boolean
        default: true
```

#### 2.2 Specific Filter Type Definitions

```yaml
# AddRequestHeader filter type
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterType
metadata:
  name: add-request-header-filter
spec:
  type: "AddRequestHeaderFilter"
  version: "v1"
  extends: "Filter"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["AddRequestHeader"]
        default: "AddRequestHeader"
      headers:
        type: array
        items:
          type: object
          properties:
            name:
              type: string
              description: "Header name"
            value:
              type: string
              description: "Header value"
          required: ["name", "value"]
        minItems: 1
        description: "Headers to add to the request"
    required: ["headers"]
    examples:
      - name: "Single header"
        headers:
          - name: "X-Request-ID"
            value: "{{random.uuid}}"
      - name: "Multiple headers"
        headers:
          - name: "X-Request-ID"
            value: "{{random.uuid}}"
          - name: "X-Service-Name"
            value: "user-service"
```

```yaml
# CircuitBreaker filter type
apiVersion: tigateway.cn/v1
kind: TiGatewayFilterType
metadata:
  name: circuit-breaker-filter
spec:
  type: "CircuitBreakerFilter"
  version: "v1"
  extends: "Filter"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["CircuitBreaker"]
        default: "CircuitBreaker"
      circuitBreakerName:
        type: string
        description: "Circuit breaker name"
      fallbackUri:
        type: string
        description: "Fallback URI when circuit is open"
      statusCodes:
        type: array
        items:
          type: string
        description: "HTTP status codes that should be considered failures"
      failureThreshold:
        type: integer
        minimum: 1
        default: 5
        description: "Number of failures before opening the circuit"
      waitDurationInOpenState:
        type: string
        pattern: "^\\d+[smh]$"
        default: "60s"
        description: "Duration to wait in open state before attempting to close"
      successThreshold:
        type: integer
        minimum: 1
        default: 3
        description: "Number of successful calls needed to close the circuit"
    required: ["circuitBreakerName"]
    examples:
      - name: "Basic circuit breaker"
        circuitBreakerName: "user-service-cb"
        fallbackUri: "forward:/fallback/user-service"
        failureThreshold: 5
        waitDurationInOpenState: "60s"
        successThreshold: 3
```

### 3. Route Type System

#### 3.1 Route Definition Type

```yaml
# Route definition type
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteType
metadata:
  name: route-definition
spec:
  type: "RouteDefinition"
  version: "v1"
  schema:
    type: object
    properties:
      id:
        type: string
        pattern: "^[a-zA-Z0-9-_]+$"
        description: "Unique route identifier"
      uri:
        type: string
        format: uri
        description: "Target URI for the route"
      predicates:
        type: array
        items:
          $ref: "#/definitions/Predicate"
        minItems: 1
        description: "Predicates to match the route"
      filters:
        type: array
        items:
          $ref: "#/definitions/Filter"
        description: "Filters to apply to the route"
      order:
        type: integer
        default: 0
        description: "Route order (lower values have higher priority)"
      metadata:
        type: object
        description: "Route metadata"
    required: ["id", "uri", "predicates"]
    examples:
      - name: "Basic route"
        id: "user-service-route"
        uri: "lb://user-service"
        predicates:
          - type: "PathPredicate"
            patterns: ["/api/users/**"]
        filters:
          - type: "StripPrefixFilter"
            parts: 2
        order: 0
```

### 4. Configuration Type System

#### 4.1 Route Configuration Type

```yaml
# Route configuration type
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfigType
metadata:
  name: route-config
spec:
  type: "RouteConfig"
  version: "v1"
  schema:
    type: object
    properties:
      routes:
        type: array
        items:
          $ref: "#/definitions/RouteDefinition"
        description: "Route definitions"
      globalFilters:
        type: array
        items:
          $ref: "#/definitions/Filter"
        description: "Global filters applied to all routes"
      defaultFilters:
        type: array
        items:
          $ref: "#/definitions/Filter"
        description: "Default filters applied to routes without specific filters"
    examples:
      - name: "Basic route configuration"
        routes:
          - id: "user-service-route"
            uri: "lb://user-service"
            predicates:
              - type: "PathPredicate"
                patterns: ["/api/users/**"]
            filters:
              - type: "StripPrefixFilter"
                parts: 2
        globalFilters:
          - type: "AddRequestHeaderFilter"
            headers:
              - name: "X-Global-Request-ID"
                value: "{{random.uuid}}"
```

## Type Validation

### 1. Schema Validation

```yaml
# Schema validation configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayValidationConfig
metadata:
  name: validation-config
spec:
  validation:
    enabled: true
    strict: true
    rules:
      - name: "predicate-type-validation"
        description: "Validate predicate types"
        condition: "predicate.type in ['PathPredicate', 'MethodPredicate', 'HeaderPredicate', 'CookiePredicate', 'HostPredicate', 'QueryPredicate', 'RemoteAddrPredicate', 'AfterPredicate', 'BeforePredicate', 'BetweenPredicate', 'WeightPredicate', 'XForwardedRemoteAddrPredicate']"
        severity: "error"
      
      - name: "filter-type-validation"
        description: "Validate filter types"
        condition: "filter.type in ['AddRequestHeaderFilter', 'AddResponseHeaderFilter', 'CircuitBreakerFilter', 'StripPrefixFilter', 'PrefixPathFilter', 'RewritePathFilter', 'RetryFilter', 'RequestRateLimiterFilter']"
        severity: "error"
      
      - name: "route-id-uniqueness"
        description: "Ensure route IDs are unique"
        condition: "route.id is unique within routes"
        severity: "error"
      
      - name: "uri-format-validation"
        description: "Validate URI format"
        condition: "route.uri matches URI pattern"
        severity: "error"
```

### 2. Custom Validation Rules

```yaml
# Custom validation rules
apiVersion: tigateway.cn/v1
kind: TiGatewayValidationRule
metadata:
  name: custom-validation-rules
spec:
  rules:
    - name: "business-rule-validation"
      description: "Validate business rules"
      type: "custom"
      implementation:
        className: "com.tigateway.validation.BusinessRuleValidator"
        method: "validate"
      config:
        rules:
          - name: "premium-user-access"
            condition: "user.type == 'PREMIUM'"
            action: "ALLOW"
          - name: "basic-user-restriction"
            condition: "user.type == 'BASIC'"
            action: "DENY"
            message: "Premium access required"
```

## Type Extensions

### 1. Custom Predicate Types

```yaml
# Custom predicate type
apiVersion: tigateway.cn/v1
kind: TiGatewayCustomPredicateType
metadata:
  name: custom-business-predicate
spec:
  type: "CustomBusinessPredicate"
  version: "v1"
  extends: "Predicate"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["CustomBusinessPredicate"]
        default: "CustomBusinessPredicate"
      businessRule:
        type: string
        description: "Business rule expression"
      threshold:
        type: number
        minimum: 0
        description: "Business rule threshold"
    required: ["businessRule"]
  implementation:
    className: "com.tigateway.predicate.CustomBusinessPredicate"
    dependencies:
      - groupId: "com.tigateway"
        artifactId: "tigateway-core"
        version: "1.0.0"
```

### 2. Custom Filter Types

```yaml
# Custom filter type
apiVersion: tigateway.cn/v1
kind: TiGatewayCustomFilterType
metadata:
  name: custom-business-filter
spec:
  type: "CustomBusinessFilter"
  version: "v1"
  extends: "Filter"
  schema:
    type: object
    properties:
      name:
        type: string
        enum: ["CustomBusinessFilter"]
        default: "CustomBusinessFilter"
      businessLogic:
        type: string
        description: "Business logic configuration"
      enableLogging:
        type: boolean
        default: true
        description: "Enable business logic logging"
    required: ["businessLogic"]
  implementation:
    className: "com.tigateway.filter.CustomBusinessFilter"
    dependencies:
      - groupId: "com.tigateway"
        artifactId: "tigateway-core"
        version: "1.0.0"
```

## Type Registry

### 1. Type Registration

```yaml
# Type registry configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayTypeRegistry
metadata:
  name: type-registry
spec:
  types:
    predicates:
      - name: "PathPredicate"
        version: "v1"
        schema: "path-predicate-schema"
        implementation: "com.tigateway.predicate.PathPredicate"
      
      - name: "MethodPredicate"
        version: "v1"
        schema: "method-predicate-schema"
        implementation: "com.tigateway.predicate.MethodPredicate"
      
      - name: "HeaderPredicate"
        version: "v1"
        schema: "header-predicate-schema"
        implementation: "com.tigateway.predicate.HeaderPredicate"
    
    filters:
      - name: "AddRequestHeaderFilter"
        version: "v1"
        schema: "add-request-header-filter-schema"
        implementation: "com.tigateway.filter.AddRequestHeaderFilter"
      
      - name: "CircuitBreakerFilter"
        version: "v1"
        schema: "circuit-breaker-filter-schema"
        implementation: "com.tigateway.filter.CircuitBreakerFilter"
      
      - name: "StripPrefixFilter"
        version: "v1"
        schema: "strip-prefix-filter-schema"
        implementation: "com.tigateway.filter.StripPrefixFilter"
```

### 2. Type Discovery

```yaml
# Type discovery configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayTypeDiscovery
metadata:
  name: type-discovery
spec:
  discovery:
    enabled: true
    sources:
      - type: "configmap"
        name: "tigateway-type-registry"
        namespace: "tigateway"
      
      - type: "crd"
        group: "tigateway.cn"
        version: "v1"
        kind: "TiGatewayPredicateType"
      
      - type: "crd"
        group: "tigateway.cn"
        version: "v1"
        kind: "TiGatewayFilterType"
    
    autoRegistration: true
    validation: true
```

## Migration Strategy

### 1. Backward Compatibility

```yaml
# Backward compatibility configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayMigrationConfig
metadata:
  name: migration-config
spec:
  migration:
    enabled: true
    mode: "hybrid"  # hybrid, typed, legacy
    
    # Legacy support
    legacy:
      enabled: true
      stringPredicates: true
      stringFilters: true
      autoConversion: true
    
    # Type conversion
    conversion:
      enabled: true
      rules:
        - name: "path-predicate-conversion"
          from: "Path=/api/users/**"
          to:
            type: "PathPredicate"
            patterns: ["/api/users/**"]
        
        - name: "method-predicate-conversion"
          from: "Method=GET,POST"
          to:
            type: "MethodPredicate"
            methods: ["GET", "POST"]
        
        - name: "add-request-header-conversion"
          from: "AddRequestHeader=X-Request-ID,{{random.uuid}}"
          to:
            type: "AddRequestHeaderFilter"
            headers:
              - name: "X-Request-ID"
                value: "{{random.uuid}}"
```

### 2. Gradual Migration

```yaml
# Gradual migration configuration
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: migrated-routes
spec:
  migration:
    strategy: "gradual"
    phases:
      - name: "phase1"
        description: "Migrate basic predicates"
        predicates:
          - type: "PathPredicate"
            patterns: ["/api/users/**"]
          - type: "MethodPredicate"
            methods: ["GET", "POST"]
        filters:
          - type: "StripPrefixFilter"
            parts: 2
      
      - name: "phase2"
        description: "Migrate advanced predicates and filters"
        predicates:
          - type: "HeaderPredicate"
            headers:
              - name: "X-API-Version"
                value: "v1"
        filters:
          - type: "CircuitBreakerFilter"
            circuitBreakerName: "user-service-cb"
            fallbackUri: "forward:/fallback/user-service"
```

## Best Practices

### 1. Type Usage Guidelines

```yaml
# Type usage guidelines
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: best-practices-routes
spec:
  routes:
    - id: "best-practice-route"
      uri: "lb://user-service"
      
      # Use typed predicates
      predicates:
        - type: "PathPredicate"
          patterns: ["/api/users/**"]
          caseSensitive: true
        
        - type: "MethodPredicate"
          methods: ["GET", "POST"]
        
        - type: "HeaderPredicate"
          headers:
            - name: "X-API-Version"
              value: "v1"
              regexp: false
      
      # Use typed filters
      filters:
        - type: "StripPrefixFilter"
          parts: 2
        
        - type: "AddRequestHeaderFilter"
          headers:
            - name: "X-Service-Name"
              value: "user-service"
        
        - type: "CircuitBreakerFilter"
          circuitBreakerName: "user-service-cb"
          fallbackUri: "forward:/fallback/user-service"
          failureThreshold: 5
          waitDurationInOpenState: "60s"
          successThreshold: 3
      
      # Use proper ordering
      order: 0
      
      # Include metadata
      metadata:
        description: "User service route with best practices"
        tags: ["user", "api", "best-practice"]
        version: "v1.0.0"
```

### 2. Type Validation Best Practices

```yaml
# Type validation best practices
apiVersion: tigateway.cn/v1
kind: TiGatewayValidationConfig
metadata:
  name: best-practices-validation
spec:
  validation:
    enabled: true
    strict: true
    
    # Comprehensive validation rules
    rules:
      - name: "type-consistency"
        description: "Ensure type consistency across configuration"
        condition: "all predicates have valid type and all filters have valid type"
        severity: "error"
      
      - name: "required-fields"
        description: "Ensure all required fields are present"
        condition: "all required fields are present for each type"
        severity: "error"
      
      - name: "value-constraints"
        description: "Ensure values meet constraints"
        condition: "all values meet their type constraints"
        severity: "error"
      
      - name: "business-rules"
        description: "Validate business rules"
        condition: "business rules are satisfied"
        severity: "warning"
```

---

**Related Documentation**:
- [CRD Configuration Design](./crd-configuration-design.md)
- [CRD Resource Configuration](./crd-resource-configuration.md)
- [CRD Filter Configuration](./crd-filter-configuration.md)
- [CRD Predicate Configuration](./crd-predicate-configuration.md)
