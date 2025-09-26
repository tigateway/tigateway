# TiGateway CRD API 文档

## 概述

TiGateway 提供了完整的 Kubernetes Custom Resource Definitions (CRD) API，用于管理网关配置、路由规则和服务映射。所有 CRD 资源都属于 `tigateway.cn` API 组。

## API 组信息

- **API 组**: `tigateway.cn`
- **API 版本**: `v1`
- **命名空间**: 支持集群级别和命名空间级别资源

## 核心 CRD 资源

### 1. TiGateway

主网关资源，定义网关实例的基本配置和部署参数。

#### 资源定义
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

#### 使用示例
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

#### API 操作
```bash
# 创建 TiGateway 实例
kubectl apply -f tigateway-instance.yaml

# 获取 TiGateway 列表
kubectl get tigateways -n tigateway

# 获取特定 TiGateway
kubectl get tigateway tigateway-instance -n tigateway -o yaml

# 更新 TiGateway
kubectl patch tigateway tigateway-instance -n tigateway --type='merge' -p='{"spec":{"replicas":5}}'

# 删除 TiGateway
kubectl delete tigateway tigateway-instance -n tigateway
```

### 2. TiGatewayMapping

路由映射资源，定义服务间的路由规则和映射关系。

#### 资源定义
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

#### 使用示例
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

#### API 操作
```bash
# 创建路由映射
kubectl apply -f web-to-api-mapping.yaml

# 获取映射列表
kubectl get tigatewaymappings -n tigateway

# 更新映射规则
kubectl patch tigatewaymapping web-to-api-mapping -n tigateway --type='merge' -p='{"spec":{"rules":[{"path":"/api/v1/**","methods":["GET","POST"]}]}}'

# 删除映射
kubectl delete tigatewaymapping web-to-api-mapping -n tigateway
```

### 3. TiGatewayRouteConfig

路由配置资源，定义详细的路由策略、过滤器和高级配置。

#### 资源定义
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

#### 使用示例
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

#### API 操作
```bash
# 创建路由配置
kubectl apply -f api-route-config.yaml

# 获取路由配置列表
kubectl get tigatewayrouteconfigs -n tigateway

# 查看路由配置详情
kubectl describe tigatewayrouteconfig api-route-config -n tigateway

# 更新路由配置
kubectl patch tigatewayrouteconfig api-route-config -n tigateway --type='merge' -p='{"spec":{"routes":[{"id":"new-route","uri":"lb://new-service","predicates":[{"name":"Path","args":{"pattern":"/api/new/**"}}]}]}}'

# 删除路由配置
kubectl delete tigatewayrouteconfig api-route-config -n tigateway
```

## IngressClass 集成

### IngressClass 定义
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

### IngressClass 参数
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

## 高级配置示例

### 1. 多环境路由配置
```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: multi-env-routes
  namespace: tigateway
spec:
  routes:
    # 开发环境路由
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
    
    # 测试环境路由
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
    
    # 生产环境路由
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

### 2. 金丝雀发布配置
```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: canary-deployment
  namespace: tigateway
spec:
  routes:
    # 稳定版本路由 (90% 流量)
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
    
    # 金丝雀版本路由 (10% 流量)
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

### 3. 安全路由配置
```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: secure-routes
  namespace: tigateway
spec:
  routes:
    # 公开 API 路由
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
    
    # 认证 API 路由
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
    
    # 管理 API 路由
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

## 状态和事件

### 资源状态
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

### 事件监控
```bash
# 查看 TiGateway 事件
kubectl get events --field-selector involvedObject.kind=TiGateway -n tigateway

# 查看路由配置事件
kubectl get events --field-selector involvedObject.kind=TiGatewayRouteConfig -n tigateway

# 实时监控事件
kubectl get events -n tigateway --watch
```

## 最佳实践

### 1. 资源命名规范
```yaml
# 推荐的命名规范
metadata:
  name: tigateway-{environment}-{purpose}
  namespace: tigateway-{environment}
  labels:
    app: tigateway
    environment: {environment}
    purpose: {purpose}
    version: {version}
```

### 2. 配置管理
```yaml
# 使用 ConfigMap 管理配置
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
            # 路由配置
  routes.json: |
    [
      {
        "id": "route-1",
        "uri": "lb://service-1"
      }
    ]
```

### 3. 监控和告警
```yaml
# Prometheus 监控配置
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

**相关文档**:
- [REST API](./rest-api.md)
- [Kubernetes 部署](../deployment/kubernetes.md)
- [高级配置示例](../examples/advanced-config.md)
