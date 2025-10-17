# TiGateway - Spring Cloud Gateway for Kubernetes

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.20+-blue.svg)](https://kubernetes.io/)

TiGateway 是一个基于 Spring Cloud Gateway 的 Kubernetes 原生网关解决方案，通过自定义 CRD 资源提供云原生的 API 网关功能。

## 🚀 项目特性

### 核心功能
- **Kubernetes 原生**: 完全基于 Kubernetes 和 ConfigMap 存储，无需传统数据库
- **自定义 CRD 资源**: 使用 `tigateway.cn` API 组管理网关配置
- **动态路由**: 支持 Kubernetes Ingress 自动发现和动态路由配置
- **多端口架构**: 主网关、管理界面、MCP 服务、监控端点独立部署
- **云原生存储**: 基于 ConfigMap 的配置存储，支持 YAML Schema 验证
- **AI 原生支持**: 集成 LLM 缓存、内容审核、多模型适配等 AI 功能
- **MCP 协议**: 支持 Model Context Protocol，提供 AI 驱动的管理接口

### 技术特性
- **响应式架构**: 基于 Spring WebFlux 和 Reactor Netty
- **服务发现**: 自动发现 Kubernetes 服务
- **配置热更新**: 支持配置实时更新
- **健康检查**: 完整的健康检查和监控端点
- **RBAC 支持**: 完整的 Kubernetes 权限控制

## 🏗️ 项目架构

### 模块结构
```
ti-gateway/
├── ti-gateway-base/                    # 基础模块
│   ├── ConfigMap 存储实现
│   ├── 数据模型定义
│   └── YAML Schema 验证
├── ti-gateway-admin/                   # 管理界面模块
│   ├── 独立端口 8081
│   ├── Web UI 管理界面
│   └── REST API 端点
├── ti-gateway-kubernetes/              # Kubernetes 集成模块
│   ├── 主应用 (端口 8080)
│   ├── Ingress 控制器
│   └── 服务发现集成
├── ti-gateway-mcp/                     # MCP 服务模块
│   ├── Model Context Protocol 支持
│   ├── AI 驱动的管理接口
│   └── 独立端口 8082
├── ti-gateway-ai-native/               # AI 原生网关模块
│   ├── LLM 缓存和模板
│   ├── 内容审核和限流
│   ├── 多模型适配
│   └── AI 可观测性
└── ti-gateway-kubernetes-extensions/   # 扩展模块
    └── 自定义扩展功能
```

### 服务端口
- **主 Gateway**: `8080` - 网关核心服务
- **Admin 管理界面**: `8081` - 独立管理服务
- **MCP 服务**: `8082` - AI 驱动的管理接口
- **Management 端点**: `8090` - 监控和健康检查

## 📋 自定义 CRD 资源

### API 组: `tigateway.cn`

#### 1. TiGateway
主网关资源，定义网关实例的基本配置。

```yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: tigateway-instance
spec:
  replicas: 3
  image: tigateway:latest
  resources:
    requests:
      memory: "512Mi"
      cpu: "250m"
    limits:
      memory: "1Gi"
      cpu: "500m"
```

#### 2. TiGatewayMapping
路由映射资源，定义服务间的路由规则。

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayMapping
metadata:
  name: api-mapping
spec:
  source:
    service: frontend-service
    port: 80
  target:
    service: backend-service
    port: 8080
  rules:
    - path: /api/v1/*
      methods: [GET, POST]
```

#### 3. TiGatewayRouteConfig
路由配置资源，定义详细的路由策略。

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: api-route-config
spec:
  routes:
    - id: user-service
      uri: lb://user-service
      predicates:
        - Path=/api/users/**
      filters:
        - StripPrefix=2
        - AddRequestHeader=X-Gateway, TiGateway
```

#### 4. IngressClass
Kubernetes IngressClass 资源，定义 `tigateway` 控制器。

```yaml
apiVersion: networking.k8s.io/v1
kind: IngressClass
metadata:
  name: tigateway
spec:
  controller: tigateway.cn/ingress-controller
```

## 🛠️ 技术栈

### 后端技术
- **Spring Cloud Gateway 4.0.0+**: 网关核心框架
- **Spring Boot 3.2.0**: 应用框架
- **Spring WebFlux**: 响应式 Web 框架
- **Kubernetes Java Client 18.0.1**: Kubernetes API 集成
- **Spring Cloud Kubernetes**: Kubernetes 服务发现和配置
- **Reactor Netty**: 响应式网络层
- **Jackson**: JSON/YAML 处理
- **Guava**: 缓存和工具库

### 前端技术
- **React 18**: 用户界面框架
- **TypeScript**: 类型安全的 JavaScript
- **Vite**: 现代化构建工具

### 部署技术
- **Docker**: 容器化
- **Helm Charts**: Kubernetes 包管理
- **RBAC**: 基于角色的访问控制

## 🚀 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- Docker
- Kubernetes 1.20+
- Helm 3.0+

### 1. 克隆项目
```bash
git clone https://github.com/your-org/tigateway.git
cd tigateway
```

### 2. 构建项目
```bash
mvn clean package -DskipTests
```

### 3. 安装 CRDs
```bash
# 使用 Helm 安装 CRDs
helm install tigateway-crds ./helm/tigateway-crds

# 或直接应用 YAML
kubectl apply -f helm/tigateway-crds/templates/
```

### 4. 部署 Gateway
```bash
# 使用 Helm 部署
helm install tigateway ./helm/gateway

# 或直接应用 YAML
kubectl apply -f helm/gateway/
```

### 5. 验证部署
```bash
# 检查 Pod 状态
kubectl get pods -l app=tigateway

# 检查服务状态
kubectl get svc tigateway

# 检查 CRDs
kubectl get crd | grep tigateway
```

## 📖 使用指南

### 创建网关实例
```yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: my-gateway
spec:
  replicas: 2
  image: tigateway:1.0.0
  config:
    storage:
      configmap:
        enabled: true
        name: tigateway-config
        namespace: default
```

### 配置路由映射
```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayMapping
metadata:
  name: web-to-api
spec:
  source:
    service: web-frontend
    port: 80
  target:
    service: api-backend
    port: 8080
  rules:
    - path: /api/**
      methods: [GET, POST, PUT, DELETE]
```

### 使用 Kubernetes Ingress
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: api-ingress
  annotations:
    kubernetes.io/ingress.class: tigateway
spec:
  rules:
    - host: api.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: api-service
                port:
                  number: 80
```

## 🔧 配置说明

### 应用配置 (application.yml)
```yaml
spring:
  cloud:
    gateway:
      kubernetes:
        ingress:
          enabled: true
          namespace: default
          refresh-interval: 30
      storage:
        configmap:
          enabled: true
          name: tigateway-app-config
          namespace: default

admin:
  server:
    enabled: true
    port: 8081
    context-path: /admin

management:
  server:
    port: 8090
  endpoints:
    web:
      exposure:
        include: "*"
```

### ConfigMap 配置示例
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
            - id: test-route
              uri: https://httpbin.org
              predicates:
                - Path=/test/**
              filters:
                - StripPrefix=1
```

## 📊 监控和健康检查

### 健康检查端点
- **主应用**: `http://localhost:8080/actuator/health`
- **管理界面**: `http://localhost:8081/admin/health`
- **监控端点**: `http://localhost:8090/actuator/health`

### 监控指标
- **Gateway 路由**: `/actuator/gateway/routes`
- **全局过滤器**: `/actuator/gateway/globalfilters`
- **路由过滤器**: `/actuator/gateway/routefilters`

## 🐳 Docker 部署

### 构建镜像
```bash
docker build -t tigateway:latest ./ti-gateway-kubernetes
```

### 运行容器
```bash
docker run -d \
  --name tigateway \
  -p 8080:8080 \
  -p 8081:8081 \
  -p 8090:8090 \
  tigateway:latest
```

## 🔐 安全配置

### RBAC 配置
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: tigateway
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: tigateway-role
rules:
  - apiGroups: [""]
    resources: ["configmaps", "services", "endpoints"]
    verbs: ["get", "list", "watch"]
  - apiGroups: ["networking.k8s.io"]
    resources: ["ingresses"]
    verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: tigateway-role-binding
subjects:
  - kind: ServiceAccount
    name: tigateway
roleRef:
  kind: Role
  name: tigateway-role
  apiGroup: rbac.authorization.k8s.io
```

## 🧪 开发和测试

### 本地开发
```bash
# 启动开发环境
mvn spring-boot:run -pl ti-gateway-kubernetes -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# 运行测试
mvn test

# 代码质量检查
mvn checkstyle:check
mvn spotbugs:check
```

### 集成测试
```bash
# 运行集成测试
mvn verify -P integration-test

# 使用 Testcontainers 测试
mvn test -Dtest=*IntegrationTest
```

## 📚 API 文档

### REST API 端点
- **管理 API**: `http://localhost:8081/admin/api/`
- **Gateway API**: `http://localhost:8080/actuator/gateway/`
- **监控 API**: `http://localhost:8090/actuator/`

### OpenAPI 文档
访问 `http://localhost:8081/swagger-ui.html` 查看完整的 API 文档。

## 🤝 贡献指南

### 开发流程
1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

### 代码规范
- 遵循 Java 编码规范
- 使用 Lombok 减少样板代码
- 编写单元测试和集成测试
- 更新相关文档

## 📄 许可证

本项目采用 Apache 2.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🆘 支持和帮助

### 常见问题
- **Java 版本问题**: 确保使用 Java 17 或更高版本
- **端口冲突**: 检查 8080、8081、8090 端口是否被占用
- **Kubernetes 连接**: 确保 kubectl 配置正确

### 获取帮助
- 📖 [文档](docs/)
- 🐛 [问题报告](https://github.com/your-org/tigateway/issues)
- 💬 [讨论区](https://github.com/your-org/tigateway/discussions)
- 📧 [邮件支持](mailto:support@tigateway.cn)

## 🗺️ 路线图

### 即将发布的功能
- [ ] 支持 gRPC 路由
- [ ] 集成 Prometheus 监控
- [ ] 支持 WebSocket 代理
- [ ] 多集群支持
- [ ] 配置模板化

### 已实现的功能
- [x] AI 原生网关支持 (ti-gateway-ai-native)
- [x] MCP 协议集成 (ti-gateway-mcp)
- [x] LLM 缓存和模板管理
- [x] 内容审核和 Token 限流
- [x] 多模型适配和代理
- [x] AI 可观测性和统计

### 长期计划
- [ ] 支持 Service Mesh 集成
- [ ] 可视化配置界面
- [ ] 自动扩缩容
- [ ] 多租户支持

---

**TiGateway** - 让 Spring Cloud Gateway 在 Kubernetes 中更简单、更强大！ 🚀