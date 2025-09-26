# TiGateway CRD API Documentation

## Overview

TiGateway provides comprehensive Kubernetes Custom Resource Definitions (CRD) API for managing gateway configurations, route rules, and service mappings. All CRD resources belong to the `tigateway.cn` API group.

## API Group Information

- **API Group**: `tigateway.cn`
- **API Version**: `v1`
- **Namespace**: Supports both cluster-level and namespace-level resources

## Core CRD Resources

### 1. TiGateway

The main gateway resource that defines the basic configuration and deployment parameters of gateway instances.

#### Resource Definition
```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: tigateways.tigateway.cn
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
              replicas:
                type: integer
                minimum: 1
                maximum: 10
              image:
                type: string
              resources:
                type: object
                properties:
                  requests:
                    type: object
                    properties:
                      memory:
                        type: string
                      cpu:
                        type: string
                  limits:
                    type: object
                    properties:
                      memory:
                        type: string
                      cpu:
                        type: string
              config:
                type: object
                properties:
                  storage:
                    type: object
                    properties:
                      configmap:
                        type: object
                        properties:
                          enabled:
                            type: boolean
                          name:
                            type: string
                          namespace:
                            type: string
  scope: Namespaced
  names:
    plural: tigateways
    singular: tigateway
    kind: TiGateway
    shortNames:
    - tg
```

#### Usage Example
```yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: tigateway-instance
  namespace: tigateway
spec:
  replicas: 3
  image: tigateway:1.0.0
  resources:
    requests:
      memory: "512Mi"
      cpu: "250m"
    limits:
      memory: "1Gi"
      cpu: "500m"
  config:
    storage:
      configmap:
        enabled: true
        name: tigateway-app-config
        namespace: tigateway
```

#### API Operations
```bash
# Create TiGateway instance
kubectl apply -f tigateway-instance.yaml

# Get TiGateway list
kubectl get tigateways -n tigateway

# Get specific TiGateway
kubectl get tigateway tigateway-instance -n tigateway -o yaml

# Update TiGateway
kubectl patch tigateway tigateway-instance -n tigateway --type='merge' -p='{"spec":{"replicas":5}}'

# Delete TiGateway
kubectl delete tigateway tigateway-instance -n tigateway
```

### 2. TiGatewayMapping

Route mapping resource that defines routing rules and mapping relationships between services.

#### Resource Definition
```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: tigatewaymappings.tigateway.cn
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
              source:
                type: object
                properties:
                  service:
                    type: string
                  port:
                    type: integer
                  namespace:
                    type: string
              target:
                type: object
                properties:
                  service:
                    type: string
                  port:
                    type: integer
                  namespace:
                    type: string
              rules:
                type: array
                items:
                  type: object
                  properties:
                    path:
                      type: string
                    methods:
                      type: array
                      items:
                        type: string
                    headers:
                      type: object
                    query:
                      type: object
                    weight:
                      type: integer
  scope: Namespaced
  names:
    plural: tigatewaymappings
    singular: tigatewaymapping
    kind: TiGatewayMapping
    shortNames:
    - tgm
```

#### Usage Example
```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayMapping
metadata:
  name: web-to-api-mapping
  namespace: tigateway
spec:
  source:
    service: web-frontend
    port: 80
    namespace: frontend
  target:
    service: api-backend
    port: 8080
    namespace: backend
  rules:
    - path: /api/v1/users/**
      methods: [GET, POST, PUT, DELETE]
      headers:
        X-API-Version: v1
      weight: 80
    - path: /api/v2/users/**
      methods: [GET, POST, PUT, DELETE]
      headers:
        X-API-Version: v2
      weight: 20
```

#### API Operations
```bash
# Create route mapping
kubectl apply -f web-to-api-mapping.yaml

# Get mapping list
kubectl get tigatewaymappings -n tigateway

# Update mapping rules
kubectl patch tigatewaymapping web-to-api-mapping -n tigateway --type='merge' -p='{"spec":{"rules":[{"path":"/api/v1/**","methods":["GET","POST"]}]}}'

# Delete mapping
kubectl delete tigatewaymapping web-to-api-mapping -n tigateway
```

### 3. TiGatewayRouteConfig

Route configuration resource that defines detailed routing policies, filters, and advanced configurations.

#### Resource Definition
```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: tigatewayrouteconfigs.tigateway.cn
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
                    uri:
                      type: string
                    predicates:
                      type: array
                      items:
                        type: object
                        properties:
                          name:
                            type: string
                          args:
                            type: object
                    filters:
                      type: array
                      items:
                        type: object
                        properties:
                          name:
                            type: string
                          args:
                            type: object
                    metadata:
                      type: object
              globalFilters:
                type: array
                items:
                  type: object
                  properties:
                    name:
                      type: string
                    args:
                      type: object
              loadBalancer:
                type: object
                properties:
                  type:
                    type: string
                    enum: [ROUND_ROBIN, LEAST_CONNECTIONS, RANDOM]
                  healthCheck:
                    type: object
                    properties:
                      enabled:
                        type: boolean
                      path:
                        type: string
                      interval:
                        type: string
                      timeout:
                        type: string
  scope: Namespaced
  names:
    plural: tigatewayrouteconfigs
    singular: tigatewayrouteconfig
    kind: TiGatewayRouteConfig
    shortNames:
    - tgrc
```

#### Usage Example
```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: api-route-config
  namespace: tigateway
spec:
  routes:
    - id: user-service-route
      uri: lb://user-service
      predicates:
        - name: Path
          args:
            pattern: "/api/users/**"
        - name: Method
          args:
            methods: [GET, POST, PUT, DELETE]
        - name: Header
          args:
            name: X-API-Version
            value: v1
      filters:
        - name: StripPrefix
          args:
            parts: 2
        - name: AddRequestHeader
          args:
            name: X-Gateway
            value: TiGateway
        - name: CircuitBreaker
          args:
            name: user-service-cb
            fallbackUri: forward:/fallback/user
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 100
            redis-rate-limiter.burstCapacity: 200
            key-resolver: "#{@userKeyResolver}"
      metadata:
        description: "User service route configuration"
        tags: [user, api, v1]
    
    - id: order-service-route
      uri: lb://order-service
      predicates:
        - name: Path
          args:
            pattern: "/api/orders/**"
        - name: Weight
          args:
            group: order-group
            weight: 80
      filters:
        - name: StripPrefix
          args:
            parts: 2
        - name: AddResponseHeader
          args:
            name: X-Processed-By
            value: TiGateway
      metadata:
        description: "Order service route configuration"
        tags: [order, api, v1]
  
  globalFilters:
    - name: AddRequestHeader
      args:
        name: X-Request-ID
        value: "${random.uuid}"
    - name: AddResponseHeader
      args:
        name: X-Response-Time
        value: "${timestamp}"
  
  loadBalancer:
    type: ROUND_ROBIN
    healthCheck:
      enabled: true
      path: /health
      interval: 10s
      timeout: 5s
```

#### API Operations
```bash
# Create route configuration
kubectl apply -f api-route-config.yaml

# Get route configuration list
kubectl get tigatewayrouteconfigs -n tigateway

# View route configuration details
kubectl describe tigatewayrouteconfig api-route-config -n tigateway

# Update route configuration
kubectl patch tigatewayrouteconfig api-route-config -n tigateway --type='merge' -p='{"spec":{"routes":[{"id":"new-route","uri":"lb://new-service","predicates":[{"name":"Path","args":{"pattern":"/api/new/**"}}]}]}}'

# Delete route configuration
kubectl delete tigatewayrouteconfig api-route-config -n tigateway
```

## IngressClass Integration

### IngressClass Definition
```yaml
apiVersion: networking.k8s.io/v1
kind: IngressClass
metadata:
  name: tigateway
  annotations:
    ingressclass.kubernetes.io/is-default-class: "true"
spec:
  controller: tigateway.cn/ingress-controller
  parameters:
    apiGroup: tigateway.cn
    kind: TiGatewayIngressClassParams
    name: tigateway-params
```

### IngressClass Parameters
```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayIngressClassParams
metadata:
  name: tigateway-params
spec:
  defaultLoadBalancerType: ROUND_ROBIN
  defaultHealthCheck:
    enabled: true
    path: /health
    interval: 10s
    timeout: 5s
  defaultRateLimit:
    enabled: true
    requestsPerMinute: 1000
  defaultCircuitBreaker:
    enabled: true
    failureThreshold: 5
    timeout: 30s
```

## Advanced Configuration Examples

### 1. Multi-Environment Route Configuration
```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: multi-env-routes
  namespace: tigateway
spec:
  routes:
    # Development environment route
    - id: dev-user-service
      uri: lb://user-service-dev
      predicates:
        - name: Path
          args:
            pattern: "/api/dev/users/**"
        - name: Header
          args:
            name: X-Environment
            value: dev
      filters:
        - name: StripPrefix
          args:
            parts: 3
        - name: AddRequestHeader
          args:
            name: X-Environment
            value: dev
    
    # Test environment route
    - id: test-user-service
      uri: lb://user-service-test
      predicates:
        - name: Path
          args:
            pattern: "/api/test/users/**"
        - name: Header
          args:
            name: X-Environment
            value: test
      filters:
        - name: StripPrefix
          args:
            parts: 3
        - name: AddRequestHeader
          args:
            name: X-Environment
            value: test
    
    # Production environment route
    - id: prod-user-service
      uri: lb://user-service-prod
      predicates:
        - name: Path
          args:
            pattern: "/api/users/**"
        - name: Header
          args:
            name: X-Environment
            value: prod
      filters:
        - name: StripPrefix
          args:
            parts: 2
        - name: AddRequestHeader
          args:
            name: X-Environment
            value: prod
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 1000
            redis-rate-limiter.burstCapacity: 2000
```

### 2. Canary Deployment Configuration
```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: canary-deployment
  namespace: tigateway
spec:
  routes:
    # Stable version route (90% traffic)
    - id: stable-service
      uri: lb://user-service-v1
      predicates:
        - name: Path
          args:
            pattern: "/api/users/**"
        - name: Weight
          args:
            group: user-service
            weight: 90
      filters:
        - name: StripPrefix
          args:
            parts: 2
        - name: AddRequestHeader
          args:
            name: X-Version
            value: v1
    
    # Canary version route (10% traffic)
    - id: canary-service
      uri: lb://user-service-v2
      predicates:
        - name: Path
          args:
            pattern: "/api/users/**"
        - name: Weight
          args:
            group: user-service
            weight: 10
      filters:
        - name: StripPrefix
          args:
            parts: 2
        - name: AddRequestHeader
          args:
            name: X-Version
            value: v2
        - name: AddRequestHeader
          args:
            name: X-Canary
            value: "true"
```

### 3. Secure Route Configuration
```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: secure-routes
  namespace: tigateway
spec:
  routes:
    # Public API route
    - id: public-api
      uri: lb://public-service
      predicates:
        - name: Path
          args:
            pattern: "/api/public/**"
      filters:
        - name: StripPrefix
          args:
            parts: 2
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 100
            redis-rate-limiter.burstCapacity: 200
    
    # Authenticated API route
    - id: auth-api
      uri: lb://auth-service
      predicates:
        - name: Path
          args:
            pattern: "/api/auth/**"
        - name: Header
          args:
            name: Authorization
            regexp: "Bearer .+"
      filters:
        - name: StripPrefix
          args:
            parts: 2
        - name: JwtAuthenticationFilter
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 50
            redis-rate-limiter.burstCapacity: 100
    
    # Admin API route
    - id: admin-api
      uri: lb://admin-service
      predicates:
        - name: Path
          args:
            pattern: "/api/admin/**"
        - name: Header
          args:
            name: Authorization
            regexp: "Bearer .+"
        - name: Header
          args:
            name: X-User-Roles
            regexp: ".*ADMIN.*"
      filters:
        - name: StripPrefix
          args:
            parts: 2
        - name: JwtAuthenticationFilter
        - name: RoleAuthorizationFilter
          args:
            required-roles: ["ADMIN", "SUPER_ADMIN"]
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 20
            redis-rate-limiter.burstCapacity: 50
```

## Status and Events

### Resource Status
```yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: tigateway-instance
  namespace: tigateway
status:
  phase: Running
  replicas: 3
  readyReplicas: 3
  conditions:
    - type: Ready
      status: "True"
      lastTransitionTime: "2024-09-23T10:00:00Z"
      reason: AllReplicasReady
      message: All replicas are ready
    - type: ConfigLoaded
      status: "True"
      lastTransitionTime: "2024-09-23T10:00:00Z"
      reason: ConfigLoaded
      message: Configuration loaded successfully
  endpoints:
    - address: "10.0.0.1:8080"
      ready: true
    - address: "10.0.0.2:8080"
      ready: true
    - address: "10.0.0.3:8080"
      ready: true
```

### Event Monitoring
```bash
# View TiGateway events
kubectl get events --field-selector involvedObject.kind=TiGateway -n tigateway

# View route configuration events
kubectl get events --field-selector involvedObject.kind=TiGatewayRouteConfig -n tigateway

# Real-time event monitoring
kubectl get events -n tigateway --watch
```

## Best Practices

### 1. Resource Naming Conventions
```yaml
# Recommended naming conventions
metadata:
  name: tigateway-{environment}-{purpose}
  namespace: tigateway-{environment}
  labels:
    app: tigateway
    environment: {environment}
    purpose: {purpose}
    version: {version}
```

### 2. Configuration Management
```yaml
# Using ConfigMap for configuration management
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-config
  namespace: tigateway
data:
  application.yml: |
    spring:
      cloud:
        gateway:
          routes:
            # Route configuration
  routes.json: |
    [
      {
        "id": "route-1",
        "uri": "lb://service-1"
      }
    ]
```

### 3. Monitoring and Alerting
```yaml
# Prometheus monitoring configuration
apiVersion: v1
kind: ServiceMonitor
metadata:
  name: tigateway-monitor
  namespace: tigateway
spec:
  selector:
    matchLabels:
      app: tigateway
  endpoints:
    - port: management
      path: /actuator/prometheus
      interval: 30s
```

---

**Related Documentation**:
- [REST API](./rest-api.md)
- [Kubernetes Deployment](../deployment/kubernetes.md)
- [Advanced Configuration Examples](../examples/advanced-config.md)
