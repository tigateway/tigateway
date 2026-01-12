# TiGateway CRDs 用户手册

## 目录

1. [概述](#概述)
2. [快速开始](#快速开始)
3. [CRDs 详细说明](#crds-详细说明)
4. [配置指南](#配置指南)
5. [最佳实践](#最佳实践)
6. [故障排除](#故障排除)
7. [API 参考](#api-参考)
8. [示例场景](#示例场景)

## 概述

TiGateway CRDs 是一套基于 Kubernetes 自定义资源定义（CRD）的 API 网关管理方案。它提供了声明式的方式来管理 API 网关配置，支持 Kubernetes Ingress 自动发现，并集成了完整的监控、安全和扩展功能。

### 核心特性

- **Kubernetes 原生**: 完全基于 Kubernetes CRD 规范
- **Ingress 集成**: 自动发现和管理 Kubernetes Ingress 资源
- **IngressClass 支持**: 原生支持 Kubernetes IngressClass，使用 `tigateway` 控制器
- **声明式配置**: YAML 驱动的配置管理
- **可扩展性**: 支持自定义扩展和过滤器
- **可观测性**: 内置指标、链路追踪和监控支持
- **安全性**: SSO、TLS 和认证支持
- **高可用**: 支持多副本和负载均衡

### 架构组件

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   TiGateway     │    │ TiGatewayMapping│    │TiGatewayRouteConfig│
│   (主网关实例)    │◄───┤   (映射关系)     │◄───┤   (路由配置)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Kubernetes     │    │   Ingress       │    │   Services      │
│   Ingress       │    │   Resources     │    │   & Pods        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 快速开始

### 前置要求

- Kubernetes 1.19+
- Helm 3.0+
- kubectl 配置正确

### 安装 CRDs

```bash
# 1. 添加 Helm 仓库
helm repo add tigateway https://charts.tigateway.cn
helm repo update

# 2. 安装 CRDs
helm install tigateway-crds tigateway/tigateway-crds \
  -n tigateway-system \
  --create-namespace

# 3. 验证安装
kubectl get crd | grep tigateway.cn
```

### 创建第一个网关

```yaml
# my-first-gateway.yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: my-first-gateway
  namespace: default
spec:
  count: 1
  ingress:
    enabled: true
    namespace: default
  api:
    title: "My First Gateway"
    version: "1.0.0"
```

```bash
# 应用配置
kubectl apply -f my-first-gateway.yaml

# 检查状态
kubectl get tigateway
kubectl describe tigateway my-first-gateway
```

## CRDs 详细说明

### 1. TiGateway

TiGateway 是主要的网关实例配置资源，定义了网关的运行参数、资源配置和功能特性。

#### 核心字段

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| `spec.count` | integer | 是 | 网关副本数量 |
| `spec.resources` | object | 否 | 资源限制和请求 |
| `spec.ingress` | object | 否 | Ingress 集成配置 |
| `spec.api` | object | 否 | API 文档配置 |
| `spec.sso` | object | 否 | SSO 配置 |
| `spec.observability` | object | 否 | 监控配置 |

#### 示例配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: production-gateway
  namespace: production
  labels:
    environment: production
    team: platform
spec:
  count: 3
  resources:
    limits:
      cpu: 1000m
      memory: 2Gi
    requests:
      cpu: 500m
      memory: 1Gi
  java-opts: "-Xmx1536m -Xms512m"
  env:
    - name: SPRING_PROFILES_ACTIVE
      value: "production"
    - name: LOG_LEVEL
      value: "INFO"
  ingress:
    enabled: true
    namespace: production
    refreshInterval: 15
    cacheEnabled: true
    cacheExpiration: 600
    tlsEnabled: true
    defaultServicePort: 80
    pathRewriteEnabled: true
    pathRewritePattern: "/(.*)"
    pathRewriteReplacement: "/$1"
  api:
    groupId: "com.company"
    title: "Production API Gateway"
    description: "Main API Gateway for production environment"
    version: "2.0.0"
    serverUrl: "https://api.company.com"
    cors:
      allowedOrigins:
        - "https://app.company.com"
        - "https://admin.company.com"
      allowedMethods:
        - "GET"
        - "POST"
        - "PUT"
        - "DELETE"
        - "OPTIONS"
      allowedHeaders:
        - "Content-Type"
        - "Authorization"
        - "X-Requested-With"
        - "X-API-Key"
      allowCredentials: true
      maxAge: 3600
  sso:
    secret: "sso-secret"
    roles-attribute-name: "roles"
    inactive-session-expiration-in-minutes: 60
  observability:
    metrics:
      prometheus:
        enabled: true
        serviceMonitor:
          enabled: true
          labels:
            app: tigateway
            environment: production
      wavefront:
        enabled: false
    tracing:
      wavefront:
        enabled: false
  securityContext:
    runAsUser: 1000
    runAsGroup: 1000
    fsGroup: 1000
  tls:
    - hosts:
        - "api.company.com"
        - "gateway.company.com"
      secretName: "tigateway-tls"
```

### 2. TiGatewayRouteConfig

TiGatewayRouteConfig 定义了路由规则和配置，支持手动路由配置和 Ingress 自动发现。

#### 核心字段

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| `spec.service` | object | 否 | 默认服务配置 |
| `spec.routes` | array | 否 | 路由规则列表 |
| `spec.ingress` | object | 否 | Ingress 自动发现配置 |

#### 示例配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: api-routes
  namespace: production
  labels:
    app: tigateway
    environment: production
spec:
  service:
    name: default-service
    namespace: production
    port: 8080
    predicates:
      - "Path=/api/**"
    filters:
      - "StripPrefix=1"
    ssoEnabled: true
  routes:
    - title: "User Management API"
      description: "User management and authentication endpoints"
      uri: "lb://user-service"
      predicates:
        - "Path=/api/users/**"
        - "Method=GET,POST,PUT,DELETE"
      filters:
        - "StripPrefix=2"
        - "AddRequestHeader=X-Service,user"
        - "AddRequestHeader=X-Version,v2"
      order: 1
      ssoEnabled: true
      tokenRelay: true
      tags:
        - "user"
        - "authentication"
        - "api"
      ingressSource: "manual"
      host: "api.company.com"
      pathType: "Prefix"
      tlsEnabled: true
    - title: "Product Catalog API"
      description: "Product catalog and inventory management"
      uri: "lb://product-service"
      predicates:
        - "Path=/api/products/**"
        - "Method=GET,POST,PUT,DELETE"
      filters:
        - "StripPrefix=2"
        - "AddRequestHeader=X-Service,product"
        - "AddRequestHeader=X-Version,v1"
      order: 2
      ssoEnabled: false
      tokenRelay: false
      tags:
        - "product"
        - "catalog"
        - "api"
      ingressSource: "kubernetes-ingress"
      host: "api.company.com"
      pathType: "Prefix"
      tlsEnabled: true
    - title: "Order Management API"
      description: "Order processing and management"
      uri: "lb://order-service"
      predicates:
        - "Path=/api/orders/**"
        - "Method=GET,POST,PUT,DELETE"
      filters:
        - "StripPrefix=2"
        - "AddRequestHeader=X-Service,order"
        - "AddRequestHeader=X-Version,v1"
        - "AddRequestHeader=X-Request-ID,${random.uuid}"
      order: 3
      ssoEnabled: true
      tokenRelay: true
      tags:
        - "order"
        - "ecommerce"
        - "api"
      ingressSource: "manual"
      host: "api.company.com"
      pathType: "Prefix"
      tlsEnabled: true
  basicAuth:
    secret: "basic-auth-secret"
  openapi:
    components:
      securitySchemes:
        bearerAuth:
          type: http
          scheme: bearer
          bearerFormat: JWT
        basicAuth:
          type: http
          scheme: basic
        apiKeyAuth:
          type: apiKey
          in: header
          name: X-API-Key
      schemas:
        User:
          type: object
          properties:
            id:
              type: integer
              format: int64
            username:
              type: string
            email:
              type: string
              format: email
        Product:
          type: object
          properties:
            id:
              type: integer
              format: int64
            name:
              type: string
            price:
              type: number
              format: decimal
  ingress:
    autoDiscovery: true
    namespace: production
    labelSelector: "app=tigateway,environment=production"
    annotationSelector: "tigateway.cn/auto-discover=true"
    refreshInterval: 30
    cacheEnabled: true
    cacheExpiration: 300
```

### 3. TiGatewayMapping

TiGatewayMapping 将路由配置映射到网关实例，支持优先级控制和启用/禁用状态。

#### 核心字段

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| `spec.gatewayRef` | object | 是 | 网关实例引用 |
| `spec.routeConfigRef` | object | 是 | 路由配置引用 |
| `spec.priority` | integer | 否 | 优先级（默认 0） |
| `spec.enabled` | boolean | 否 | 是否启用（默认 true） |

#### 示例配置

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayMapping
metadata:
  name: api-mapping
  namespace: production
  labels:
    app: tigateway
    environment: production
spec:
  gatewayRef:
    name: production-gateway
    namespace: production
  routeConfigRef:
    name: api-routes
    namespace: production
  priority: 100
  enabled: true
---
apiVersion: tigateway.cn/v1
kind: TiGatewayMapping
metadata:
  name: admin-mapping
  namespace: production
  labels:
    app: tigateway
    environment: production
spec:
  gatewayRef:
    name: production-gateway
    namespace: production
  routeConfigRef:
    name: admin-routes
    namespace: production
  priority: 200
  enabled: true
```

## 配置指南

### Ingress 集成配置

TiGateway 支持自动发现和管理 Kubernetes Ingress 资源，实现动态路由管理。通过 IngressClass 机制，TiGateway 可以与其他 Ingress 控制器共存。

#### 1. IngressClass 配置

TiGateway 自动创建名为 `tigateway` 的 IngressClass：

```yaml
apiVersion: networking.k8s.io/v1
kind: IngressClass
metadata:
  name: tigateway
  annotations:
    ingressclass.kubernetes.io/is-default-class: "false"
    tigateway.cn/description: "TiGateway Ingress Controller"
spec:
  controller: tigateway.cn/ingress-controller
```

#### 2. 在 TiGateway 中配置 IngressClass

```yaml
spec:
  ingress:
    enabled: true
    namespace: production
    refreshInterval: 30
    cacheEnabled: true
    cacheExpiration: 300
    tlsEnabled: true
    defaultServicePort: 80
    pathRewriteEnabled: true
    pathRewritePattern: "/(.*)"
    pathRewriteReplacement: "/$1"
    ingressClass:
      name: "tigateway"
      enabled: true
      controller: "tigateway.cn/ingress-controller"
      annotations:
        ingressclass.kubernetes.io/is-default-class: "false"
        tigateway.cn/description: "TiGateway Ingress Controller"
```

#### 3. 在 TiGatewayRouteConfig 中配置 IngressClass 过滤

```yaml
spec:
  ingress:
    autoDiscovery: true
    namespace: production
    labelSelector: "app=tigateway,environment=production"
    annotationSelector: "tigateway.cn/auto-discover=true"
    refreshInterval: 30
    cacheEnabled: true
    cacheExpiration: 300
    ingressClass: "tigateway"
    ingressClassSelector: "tigateway"
```

#### 4. 创建 Ingress 资源

```yaml
# ingress-example.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: microservices-ingress
  namespace: production
  labels:
    app: tigateway
    environment: production
  annotations:
    tigateway.cn/auto-discover: "true"
    tigateway.cn/rewrite-target: /$2
    tigateway.cn/ssl-redirect: "true"
    tigateway.cn/force-ssl-redirect: "true"
    tigateway.cn/proxy-body-size: "10m"
    tigateway.cn/proxy-connect-timeout: "600"
    tigateway.cn/proxy-send-timeout: "600"
    tigateway.cn/proxy-read-timeout: "600"
    tigateway.cn/rate-limit: "100"
    tigateway.cn/rate-limit-window: "1m"
spec:
  ingressClassName: tigateway
  tls:
    - hosts:
        - "api.company.com"
        - "admin.company.com"
      secretName: "company-tls"
  rules:
    - host: api.company.com
      http:
        paths:
          - path: /api/users(/|$)(.*)
            pathType: Prefix
            backend:
              service:
                name: user-service
                port:
                  number: 8080
          - path: /api/products(/|$)(.*)
            pathType: Prefix
            backend:
              service:
                name: product-service
                port:
                  number: 8080
          - path: /api/orders(/|$)(.*)
            pathType: Prefix
            backend:
              service:
                name: order-service
                port:
                  number: 8080
    - host: admin.company.com
      http:
        paths:
          - path: /admin(/|$)(.*)
            pathType: Prefix
            backend:
              service:
                name: admin-service
                port:
                  number: 8080
```

#### 2. 配置自动发现

```yaml
# 在 TiGateway 中配置
spec:
  ingress:
    enabled: true
    namespace: production
    refreshInterval: 30
    cacheEnabled: true
    cacheExpiration: 300
    tlsEnabled: true
    defaultServicePort: 80
    pathRewriteEnabled: true
    pathRewritePattern: "/(.*)"
    pathRewriteReplacement: "/$1"

# 在 TiGatewayRouteConfig 中配置
spec:
  ingress:
    autoDiscovery: true
    namespace: production
    labelSelector: "app=tigateway,environment=production"
    annotationSelector: "tigateway.cn/auto-discover=true"
    refreshInterval: 30
    cacheEnabled: true
    cacheExpiration: 300
```

### 安全配置

#### 1. SSO 配置

```yaml
spec:
  sso:
    secret: "sso-secret"
    roles-attribute-name: "roles"
    inactive-session-expiration-in-minutes: 60
```

#### 2. TLS 配置

```yaml
spec:
  tls:
    - hosts:
        - "api.company.com"
        - "gateway.company.com"
      secretName: "tigateway-tls"
```

#### 3. 基础认证

```yaml
spec:
  basicAuth:
    secret: "basic-auth-secret"
```

### 监控配置

#### 1. Prometheus 监控

```yaml
spec:
  observability:
    metrics:
      prometheus:
        enabled: true
        serviceMonitor:
          enabled: true
          labels:
            app: tigateway
            environment: production
```

#### 2. 链路追踪

```yaml
spec:
  observability:
    tracing:
      wavefront:
        enabled: true
    wavefront:
      secret: "wavefront-secret"
      source: "tigateway"
      application: "api-gateway"
      service: "gateway"
```

## 最佳实践

### 1. 命名规范

- **资源命名**: 使用有意义的名称，如 `production-gateway`、`user-api-routes`
- **标签规范**: 统一使用标签标识环境、团队、应用等
- **命名空间**: 按环境或团队划分命名空间

```yaml
metadata:
  name: production-gateway
  namespace: production
  labels:
    app: tigateway
    environment: production
    team: platform
    version: v2.0.0
```

### 2. 资源配置

- **资源限制**: 根据实际负载设置合理的资源限制
- **副本数量**: 生产环境建议至少 2 个副本
- **JVM 参数**: 根据内存配置调整 JVM 参数

```yaml
spec:
  count: 3
  resources:
    limits:
      cpu: 1000m
      memory: 2Gi
    requests:
      cpu: 500m
      memory: 1Gi
  java-opts: "-Xmx1536m -Xms512m"
```

### 3. 路由设计

- **路径规划**: 使用清晰的路径结构，如 `/api/v1/users`
- **过滤器顺序**: 合理设置过滤器顺序
- **标签分类**: 使用标签对路由进行分类

```yaml
routes:
  - title: "User API v2"
    uri: "lb://user-service"
    predicates:
      - "Path=/api/v2/users/**"
    filters:
      - "StripPrefix=3"
      - "AddRequestHeader=X-Version,v2"
    order: 1
    tags:
      - "user"
      - "api"
      - "v2"
```

### 4. 安全最佳实践

- **最小权限**: 使用最小权限原则配置 RBAC
- **TLS 加密**: 生产环境必须启用 TLS
- **认证授权**: 合理配置 SSO 和基础认证

### 5. 监控和日志

- **指标收集**: 启用 Prometheus 指标收集
- **日志级别**: 生产环境使用 INFO 级别
- **告警配置**: 配置关键指标的告警

## 故障排除

### 常见问题

#### 1. CRD 未找到

**问题**: `error: unable to recognize "gateway.yaml": no matches for kind "TiGateway"`

**解决方案**:
```bash
# 检查 CRD 是否安装
kubectl get crd | grep tigateway.cn

# 重新安装 CRD
helm install tigateway-crds tigateway/tigateway-crds -n tigateway-system
```

#### 2. 网关实例未启动

**问题**: TiGateway 状态为 `NotReady`

**解决方案**:
```bash
# 检查网关状态
kubectl get tigateway -o wide

# 查看详细信息
kubectl describe tigateway my-gateway

# 检查相关 Pod
kubectl get pods -l app=tigateway
```

#### 3. Ingress 路由未发现

**问题**: Ingress 资源未被自动发现

**解决方案**:
```bash
# 检查 Ingress 资源
kubectl get ingress -o wide

# 检查标签和注解
kubectl describe ingress my-ingress

# 验证自动发现配置
kubectl get tigateway my-gateway -o yaml | grep -A 10 ingress
```

#### 4. 路由配置错误

**问题**: 路由不生效或返回 404

**解决方案**:
```bash
# 检查路由配置
kubectl get tigatewayrouteconfig -o wide

# 查看路由详情
kubectl describe tigatewayrouteconfig my-routes

# 检查映射关系
kubectl get tigatewaymapping -o wide
```

### 调试命令

```bash
# 查看所有 TiGateway 资源
kubectl get tigateway,tigatewayrouteconfig,tigatewaymapping -A

# 查看网关日志
kubectl logs -l app=tigateway -f

# 检查网关配置
kubectl get tigateway my-gateway -o yaml

# 验证 YAML 语法
kubectl apply --dry-run=client -f my-gateway.yaml
```

## API 参考

### TiGateway API

#### 完整 Schema

```yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: string
  namespace: string
  labels: map[string]string
  annotations: map[string]string
spec:
  count: integer
  resources:
    limits:
      cpu: string
      memory: string
    requests:
      cpu: string
      memory: string
  java-opts: string
  env:
    - name: string
      value: string
  sso:
    secret: string
    roles-attribute-name: string
    inactive-session-expiration-in-minutes: integer
  api:
    groupId: string
    title: string
    description: string
    documentation: string
    version: string
    serverUrl: string
    cors:
      maxAge: integer
      allowCredentials: boolean
      allowedOrigins: []string
      allowedMethods: []string
      allowedHeaders: []string
      exposedHeaders: []string
      allowedOriginPatterns: []string
      perRoute: map[string]object
  extensions:
    custom: []string
    secretsProviders:
      - name: string
        vault:
          path: string
          roleName: string
          authPath: string
    filters:
      apiKey:
        enabled: boolean
        secretsProviderName: string
      jwtKey:
        enabled: boolean
        secretsProviderName: string
  observability:
    metrics:
      wavefront:
        enabled: boolean
      prometheus:
        enabled: boolean
        annotations:
          enabled: boolean
        serviceMonitor:
          enabled: boolean
          labels: map[string]string
    tracing:
      wavefront:
        enabled: boolean
    wavefront:
      secret: string
      source: string
      application: string
      service: string
  serviceAccount:
    name: string
  securityContext:
    runAsUser: integer
    runAsGroup: integer
    fsGroup: integer
  tls:
    - hosts: []string
      secretName: string
  ingress:
    enabled: boolean
    namespace: string
    refreshInterval: integer
    cacheEnabled: boolean
    cacheExpiration: integer
    tlsEnabled: boolean
    defaultServicePort: integer
    pathRewriteEnabled: boolean
    pathRewritePattern: string
    pathRewriteReplacement: string
status:
  conditions:
    - type: string
      status: string
      lastTransitionTime: string
      reason: string
  replicas: integer
  ingressRoutes: integer
```

### TiGatewayRouteConfig API

#### 完整 Schema

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: string
  namespace: string
  labels: map[string]string
  annotations: map[string]string
spec:
  service:
    namespace: string
    name: string
    port: integer
    predicates: []string
    filters: []string
    ssoEnabled: boolean
  routes:
    - title: string
      description: string
      uri: string
      predicates: []string
      filters: []string
      order: integer
      ssoEnabled: boolean
      tokenRelay: boolean
      tags: []string
      model:
        requestBody: object
        responses: object
      ingressSource: string
      host: string
      pathType: string
      tlsEnabled: boolean
  basicAuth:
    secret: string
  openapi:
    components: object
  ingress:
    autoDiscovery: boolean
    namespace: string
    labelSelector: string
    annotationSelector: string
    refreshInterval: integer
    cacheEnabled: boolean
    cacheExpiration: integer
```

### TiGatewayMapping API

#### 完整 Schema

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayMapping
metadata:
  name: string
  namespace: string
  labels: map[string]string
  annotations: map[string]string
spec:
  gatewayRef:
    name: string
    namespace: string
  routeConfigRef:
    name: string
    namespace: string
  ingressRef:
    name: string
    namespace: string
  priority: integer
  enabled: boolean
```

## 示例场景

### 场景 1: 微服务 API 网关

为微服务架构创建统一的 API 网关，支持用户服务、产品服务和订单服务。

```yaml
# 1. 创建网关实例
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: microservices-gateway
  namespace: production
spec:
  count: 2
  ingress:
    enabled: true
    namespace: production
  api:
    title: "Microservices API Gateway"
    version: "1.0.0"
    serverUrl: "https://api.company.com"
    cors:
      allowedOrigins:
        - "https://app.company.com"
      allowedMethods:
        - "GET"
        - "POST"
        - "PUT"
        - "DELETE"
---
# 2. 创建路由配置
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: microservices-routes
  namespace: production
spec:
  routes:
    - title: "User Service"
      uri: "lb://user-service"
      predicates:
        - "Path=/api/users/**"
      filters:
        - "StripPrefix=2"
      order: 1
      tags:
        - "user"
        - "microservice"
    - title: "Product Service"
      uri: "lb://product-service"
      predicates:
        - "Path=/api/products/**"
      filters:
        - "StripPrefix=2"
      order: 2
      tags:
        - "product"
        - "microservice"
    - title: "Order Service"
      uri: "lb://order-service"
      predicates:
        - "Path=/api/orders/**"
      filters:
        - "StripPrefix=2"
      order: 3
      tags:
        - "order"
        - "microservice"
---
# 3. 创建映射关系
apiVersion: tigateway.cn/v1
kind: TiGatewayMapping
metadata:
  name: microservices-mapping
  namespace: production
spec:
  gatewayRef:
    name: microservices-gateway
    namespace: production
  routeConfigRef:
    name: microservices-routes
    namespace: production
```

### 场景 2: 多环境管理

为开发、测试和生产环境创建不同的网关配置。

```yaml
# 开发环境
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: dev-gateway
  namespace: development
  labels:
    environment: development
spec:
  count: 1
  resources:
    limits:
      cpu: 500m
      memory: 1Gi
  ingress:
    enabled: true
    namespace: development
  api:
    title: "Development API Gateway"
    version: "1.0.0"
    serverUrl: "https://dev-api.company.com"
---
# 测试环境
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: test-gateway
  namespace: testing
  labels:
    environment: testing
spec:
  count: 2
  resources:
    limits:
      cpu: 1000m
      memory: 2Gi
  ingress:
    enabled: true
    namespace: testing
  api:
    title: "Testing API Gateway"
    version: "1.0.0"
    serverUrl: "https://test-api.company.com"
---
# 生产环境
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: prod-gateway
  namespace: production
  labels:
    environment: production
spec:
  count: 3
  resources:
    limits:
      cpu: 2000m
      memory: 4Gi
  ingress:
    enabled: true
    namespace: production
  api:
    title: "Production API Gateway"
    version: "2.0.0"
    serverUrl: "https://api.company.com"
  observability:
    metrics:
      prometheus:
        enabled: true
        serviceMonitor:
          enabled: true
```

### 场景 3: 蓝绿部署

使用 TiGateway 实现蓝绿部署策略。

```yaml
# 蓝环境
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: blue-gateway
  namespace: production
  labels:
    environment: production
    deployment: blue
spec:
  count: 2
  ingress:
    enabled: true
    namespace: production
---
# 绿环境
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: green-gateway
  namespace: production
  labels:
    environment: production
    deployment: green
spec:
  count: 2
  ingress:
    enabled: true
    namespace: production
---
# 蓝环境路由
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: blue-routes
  namespace: production
spec:
  routes:
    - title: "User Service Blue"
      uri: "lb://user-service-blue"
      predicates:
        - "Path=/api/users/**"
        - "Header=X-Deployment,blue"
      filters:
        - "StripPrefix=2"
      order: 1
---
# 绿环境路由
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: green-routes
  namespace: production
spec:
  routes:
    - title: "User Service Green"
      uri: "lb://user-service-green"
      predicates:
        - "Path=/api/users/**"
        - "Header=X-Deployment,green"
      filters:
        - "StripPrefix=2"
      order: 1
```

### 场景 4: 多租户支持

为多租户应用创建隔离的网关配置。

```yaml
# 租户 A 网关
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: tenant-a-gateway
  namespace: tenant-a
  labels:
    tenant: tenant-a
spec:
  count: 2
  ingress:
    enabled: true
    namespace: tenant-a
  api:
    title: "Tenant A API Gateway"
    serverUrl: "https://tenant-a.company.com"
---
# 租户 B 网关
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: tenant-b-gateway
  namespace: tenant-b
  labels:
    tenant: tenant-b
spec:
  count: 2
  ingress:
    enabled: true
    namespace: tenant-b
  api:
    title: "Tenant B API Gateway"
    serverUrl: "https://tenant-b.company.com"
---
# 租户 A 路由
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: tenant-a-routes
  namespace: tenant-a
spec:
  routes:
    - title: "Tenant A User Service"
      uri: "lb://tenant-a-user-service"
      predicates:
        - "Path=/api/users/**"
        - "Header=X-Tenant,tenant-a"
      filters:
        - "StripPrefix=2"
        - "AddRequestHeader=X-Tenant,tenant-a"
      order: 1
---
# 租户 B 路由
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: tenant-b-routes
  namespace: tenant-b
spec:
  routes:
    - title: "Tenant B User Service"
      uri: "lb://tenant-b-user-service"
      predicates:
        - "Path=/api/users/**"
        - "Header=X-Tenant,tenant-b"
      filters:
        - "StripPrefix=2"
        - "AddRequestHeader=X-Tenant,tenant-b"
      order: 1
```

## 总结

TiGateway CRDs 提供了一套完整的 Kubernetes 原生 API 网关管理方案。通过声明式配置，您可以轻松管理复杂的路由规则、安全策略和监控配置。结合 Kubernetes Ingress 自动发现功能，TiGateway 能够实现动态路由管理，提高运维效率。

本手册涵盖了 TiGateway CRDs 的所有核心功能和使用场景，帮助您快速上手并应用到生产环境中。如有任何问题，请参考故障排除部分或联系技术支持团队。
