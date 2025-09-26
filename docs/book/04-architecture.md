# TiGateway 架构概述

## 整体架构

TiGateway 采用云原生架构设计，基于 Spring Cloud Gateway 构建，专门为 Kubernetes 环境优化。整个系统由多个模块组成，每个模块都有明确的职责和边界。

## 架构图

```mermaid
graph TB
    subgraph "Kubernetes Cluster"
        subgraph "TiGateway Pods"
            GW[Gateway Core<br/>:8080]
            ADMIN[Admin Server<br/>:8081]
            MGMT[Management<br/>:8090]
        end
        
        subgraph "Storage Layer"
            CM[ConfigMap<br/>tigateway-config]
            CRD[Custom Resources<br/>tigateway.cn]
        end
        
        subgraph "Service Mesh"
            SVC1[Service A]
            SVC2[Service B]
            SVC3[Service C]
        end
        
        subgraph "Ingress Layer"
            ING[Kubernetes Ingress]
            IC[IngressClass<br/>tigateway]
        end
    end
    
    subgraph "External"
        CLIENT[Client Applications]
        K8S_API[Kubernetes API Server]
    end
    
    CLIENT --> ING
    ING --> IC
    IC --> GW
    GW --> SVC1
    GW --> SVC2
    GW --> SVC3
    
    GW --> CM
    GW --> CRD
    ADMIN --> CM
    MGMT --> K8S_API
    
    K8S_API --> CRD
    K8S_API --> CM
```

## 核心组件

### 1. Gateway Core (端口 8080)

**功能**: 主要的网关服务，处理所有入站请求

**技术栈**: 
- Spring Cloud Gateway 3.1.x
- Spring WebFlux
- Reactor Netty

**主要特性**:
- 动态路由配置
- 负载均衡
- 熔断器
- 限流
- 认证授权
- 请求/响应转换

**核心流程**:
```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant RouteLocator
    participant FilterChain
    participant Backend
    
    Client->>Gateway: HTTP Request
    Gateway->>RouteLocator: 查找匹配路由
    RouteLocator-->>Gateway: 返回路由信息
    Gateway->>FilterChain: 执行过滤器链
    FilterChain->>Backend: 转发请求
    Backend-->>FilterChain: 返回响应
    FilterChain-->>Gateway: 处理响应
    Gateway-->>Client: 返回最终响应
```

### 2. Admin Server (端口 8081)

**功能**: 管理界面和 API，提供 Web UI 和 REST API

**技术栈**:
- Spring Boot
- Thymeleaf
- Spring WebFlux
- Bootstrap

**主要特性**:
- 路由管理界面
- 配置管理
- 监控面板
- 用户管理
- 实时配置更新

**界面功能**:
- **仪表板**: 系统概览和关键指标
- **路由管理**: 创建、编辑、删除路由
- **服务发现**: 查看 Kubernetes 服务
- **配置管理**: 编辑 ConfigMap 配置
- **监控面板**: 查看系统指标和日志

### 3. Management Endpoints (端口 8090)

**功能**: 监控、健康检查和运维端点

**技术栈**:
- Spring Boot Actuator
- Micrometer
- Prometheus

**主要端点**:
- `/actuator/health` - 健康检查
- `/actuator/metrics` - 指标收集
- `/actuator/gateway/routes` - 路由信息
- `/actuator/configprops` - 配置属性
- `/actuator/env` - 环境信息

## 模块架构

### 模块依赖关系

```mermaid
graph TD
    PARENT[ti-gateway-parent<br/>父项目]
    
    subgraph "核心模块"
        BASE[ti-gateway-base<br/>基础模块]
        ADMIN[ti-gateway-admin<br/>管理模块]
        K8S[ti-gateway-kubernetes<br/>K8s集成模块]
    end
    
    subgraph "扩展模块"
        EXT[ti-gateway-kubernetes-extensions<br/>扩展模块]
        OP[ti-gateway-operator<br/>Operator模块]
    end
    
    subgraph "限流模块"
        LIMIT[ti-gateway-limit<br/>限流父模块]
        HYSTRIX[ti-gateway-limit-hystrix<br/>Hystrix限流]
        SENTINEL[ti-gateway-limit-sentinel<br/>Sentinel限流]
    end
    
    PARENT --> BASE
    PARENT --> ADMIN
    PARENT --> K8S
    PARENT --> EXT
    PARENT --> OP
    PARENT --> LIMIT
    
    ADMIN --> BASE
    K8S --> BASE
    K8S --> ADMIN
    K8S --> EXT
    
    LIMIT --> HYSTRIX
    LIMIT --> SENTINEL
```

### 模块详细说明

#### ti-gateway-base
**职责**: 基础功能模块，提供核心数据模型和存储实现

**主要包结构**:
```
ti.gateway.base/
├── core/                    # 核心接口
│   ├── cache/              # 缓存接口
│   └── config/             # 配置接口
├── storage/                # 存储实现
│   └── configmap/          # ConfigMap存储
│       ├── config/         # 自动配置
│       ├── impl/           # 实现类
│       └── model/          # 数据模型
├── validation/             # 配置验证
│   └── schema/             # YAML Schema
└── util/                   # 工具类
```

**核心功能**:
- ConfigMap 存储实现
- 数据模型定义
- YAML Schema 验证
- 配置缓存管理

#### ti-gateway-admin
**职责**: 管理界面模块，提供 Web UI 和 REST API

**主要包结构**:
```
ti.gateway.admin/
├── config/                 # 配置类
├── controller/             # 控制器
│   ├── web/               # Web控制器
│   └── api/               # REST API控制器
├── service/               # 业务服务
├── model/                 # 数据模型
└── static/                # 静态资源
```

**核心功能**:
- Web 管理界面
- REST API 端点
- 配置管理服务
- 用户认证授权

#### ti-gateway-kubernetes
**职责**: Kubernetes 集成模块，主应用入口

**主要包结构**:
```
ti.gateway.kubernetes/
├── config/                 # 配置类
├── controller/             # 控制器
├── service/               # 业务服务
├── integration/           # 集成组件
│   ├── ingress/           # Ingress集成
│   ├── service/           # 服务发现
│   └── configmap/         # ConfigMap集成
└── filter/                # 自定义过滤器
```

**核心功能**:
- 主应用启动
- Ingress 控制器
- 服务发现集成
- 配置热更新

## 数据流架构

### 配置数据流

```mermaid
sequenceDiagram
    participant USER as 用户
    participant ADMIN as Admin UI
    participant BASE as ti-gateway-base
    participant CM as ConfigMap
    participant K8S as Kubernetes API
    participant GW as Gateway Core
    
    USER->>ADMIN: 修改配置
    ADMIN->>BASE: 保存配置
    BASE->>CM: 更新 ConfigMap
    CM->>K8S: 触发变更事件
    K8S->>GW: 通知配置变更
    GW->>GW: 重新加载配置
```

### 请求处理流程

```mermaid
sequenceDiagram
    participant Client
    participant Ingress
    participant Gateway
    participant RouteLocator
    participant FilterChain
    participant Backend
    
    Client->>Ingress: HTTP Request
    Ingress->>Gateway: 转发请求
    Gateway->>RouteLocator: 查找路由
    RouteLocator-->>Gateway: 返回路由
    Gateway->>FilterChain: 执行过滤器
    FilterChain->>Backend: 转发请求
    Backend-->>FilterChain: 返回响应
    FilterChain-->>Gateway: 处理响应
    Gateway-->>Ingress: 返回响应
    Ingress-->>Client: 返回响应
```

## 存储架构

### ConfigMap 存储

TiGateway 使用 Kubernetes ConfigMap 作为主要配置存储：

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-app-config
data:
  application.yml: |
    spring:
      cloud:
        gateway:
          routes:
          - id: example-route
            uri: http://example-service
            predicates:
            - Path=/api/**
  routes.yaml: |
    # 路由配置
  filters.yaml: |
    # 过滤器配置
```

### CRD 资源

支持自定义资源定义：

```yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: example-gateway
spec:
  routes:
  - id: example-route
    uri: http://example-service
    predicates:
    - Path=/api/**
```

## 安全架构

### 认证和授权

```mermaid
graph TB
    subgraph "认证层"
        JWT[JWT Token]
        OAuth[OAuth2]
        Basic[Basic Auth]
    end
    
    subgraph "授权层"
        RBAC[RBAC]
        ACL[ACL]
        Policy[Policy Engine]
    end
    
    subgraph "安全过滤器"
        AuthFilter[认证过滤器]
        AuthzFilter[授权过滤器]
        SecurityFilter[安全过滤器]
    end
    
    JWT --> AuthFilter
    OAuth --> AuthFilter
    Basic --> AuthFilter
    
    AuthFilter --> RBAC
    AuthFilter --> ACL
    AuthFilter --> Policy
    
    RBAC --> AuthzFilter
    ACL --> AuthzFilter
    Policy --> AuthzFilter
    
    AuthzFilter --> SecurityFilter
```

### 网络安全

- **TLS 终止**: 支持 HTTPS 和 TLS 终止
- **网络策略**: 集成 Kubernetes NetworkPolicy
- **服务网格**: 支持 Istio 等服务网格
- **防火墙**: 支持网络防火墙规则

## 监控架构

### 指标收集

```mermaid
graph TB
    subgraph "指标源"
        Gateway[Gateway Core]
        Admin[Admin Server]
        K8S[Kubernetes API]
    end
    
    subgraph "收集层"
        Micrometer[Micrometer]
        Actuator[Spring Actuator]
    end
    
    subgraph "存储层"
        Prometheus[Prometheus]
        InfluxDB[InfluxDB]
    end
    
    subgraph "可视化层"
        Grafana[Grafana]
        Dashboard[Custom Dashboard]
    end
    
    Gateway --> Micrometer
    Admin --> Micrometer
    K8S --> Actuator
    
    Micrometer --> Prometheus
    Actuator --> Prometheus
    
    Prometheus --> Grafana
    InfluxDB --> Grafana
    
    Grafana --> Dashboard
```

### 日志架构

- **结构化日志**: 使用 JSON 格式输出日志
- **日志聚合**: 支持 ELK、Fluentd 等日志聚合
- **链路追踪**: 集成 Zipkin、Jaeger 等追踪系统
- **审计日志**: 记录所有配置变更和操作

## 扩展架构

### 插件系统

TiGateway 支持插件扩展：

```mermaid
graph TB
    subgraph "核心系统"
        Core[Gateway Core]
        PluginManager[Plugin Manager]
    end
    
    subgraph "插件接口"
        FilterPlugin[Filter Plugin]
        PredicatePlugin[Predicate Plugin]
        LoadBalancerPlugin[LoadBalancer Plugin]
    end
    
    subgraph "插件实现"
        CustomFilter[Custom Filter]
        CustomPredicate[Custom Predicate]
        CustomLB[Custom LoadBalancer]
    end
    
    Core --> PluginManager
    PluginManager --> FilterPlugin
    PluginManager --> PredicatePlugin
    PluginManager --> LoadBalancerPlugin
    
    FilterPlugin --> CustomFilter
    PredicatePlugin --> CustomPredicate
    LoadBalancerPlugin --> CustomLB
```

### 自定义组件

- **自定义过滤器**: 实现 GatewayFilter 接口
- **自定义谓词**: 实现 RoutePredicateFactory 接口
- **自定义负载均衡器**: 实现 LoadBalancer 接口
- **自定义配置源**: 实现 RouteDefinitionLocator 接口

## 部署架构

### 容器化部署

```mermaid
graph TB
    subgraph "Kubernetes Cluster"
        subgraph "Namespace: tigateway"
            subgraph "Deployment"
                Pod1[TiGateway Pod 1]
                Pod2[TiGateway Pod 2]
                Pod3[TiGateway Pod 3]
            end
            
            subgraph "Services"
                GatewaySvc[Gateway Service]
                AdminSvc[Admin Service]
                MgmtSvc[Management Service]
            end
            
            subgraph "ConfigMaps"
                ConfigCM[Config ConfigMap]
                RouteCM[Route ConfigMap]
            end
        end
    end
    
    subgraph "External"
        LB[Load Balancer]
        Ingress[Ingress Controller]
    end
    
    LB --> Ingress
    Ingress --> GatewaySvc
    GatewaySvc --> Pod1
    GatewaySvc --> Pod2
    GatewaySvc --> Pod3
    
    Pod1 --> ConfigCM
    Pod2 --> ConfigCM
    Pod3 --> ConfigCM
```

### 高可用部署

- **多副本**: 支持水平扩展
- **负载均衡**: 使用 Kubernetes Service
- **故障转移**: 自动故障检测和转移
- **滚动更新**: 零停机部署

## 性能架构

### 响应式架构

TiGateway 基于 Spring WebFlux 的响应式架构：

- **非阻塞 I/O**: 使用 Netty 非阻塞 I/O
- **背压处理**: 支持响应式流背压
- **资源优化**: 高效的线程和内存使用
- **并发处理**: 支持高并发请求处理

### 缓存架构

```mermaid
graph TB
    subgraph "缓存层"
        L1Cache[L1 内存缓存]
        L2Cache[L2 Redis缓存]
        L3Cache[L3 分布式缓存]
    end
    
    subgraph "数据源"
        ConfigMap[ConfigMap]
        K8SAPI[Kubernetes API]
        External[外部服务]
    end
    
    ConfigMap --> L1Cache
    K8SAPI --> L2Cache
    External --> L3Cache
    
    L1Cache --> L2Cache
    L2Cache --> L3Cache
```

## 总结

TiGateway 的架构设计充分考虑了云原生环境的特点，具有以下优势：

1. **云原生**: 完全基于 Kubernetes 原生资源
2. **可扩展**: 模块化设计，支持水平扩展
3. **高可用**: 多副本部署，自动故障转移
4. **可观测**: 完整的监控和日志系统
5. **安全**: 多层次安全防护
6. **性能**: 响应式架构，高性能处理

这种架构设计使得 TiGateway 能够很好地适应现代云原生应用的需求，为微服务架构提供可靠的网关服务。
