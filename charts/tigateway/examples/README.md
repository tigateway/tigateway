# TiGateway 业务使用示例

本目录包含使用 TiGateway CRD 和 Kubernetes Ingress 的业务示例。

## 文件说明

### 1. `tigateway-mapping-example.yaml`
展示如何使用 `TiGatewayMapping` CRD 将路由配置映射到网关实例。

**使用场景**：
- 将多个路由配置关联到同一个网关实例
- 管理路由配置的优先级和启用状态

### 2. `tigateway-route-config-example.yaml`
展示如何使用 `TiGatewayRouteConfig` CRD 定义路由规则。

**使用场景**：
- 定义复杂的路由规则
- 配置 SSO、Token Relay 等高级功能
- 管理路由的标签和元数据

### 3. `ingress-example.yaml`
展示如何使用 Kubernetes Ingress 资源，TiGateway 会自动发现并转换为路由。

**使用场景**：
- 使用标准的 Kubernetes Ingress 定义路由
- 利用 TiGateway 的自动发现功能
- 与现有的 Kubernetes 工作流集成

## 使用方式

### 方式一：使用 TiGateway CRD（推荐）

```bash
# 1. 创建路由配置
kubectl apply -f tigateway-route-config-example.yaml

# 2. 创建映射关系
kubectl apply -f tigateway-mapping-example.yaml

# 3. 验证
kubectl get tigatewayrouteconfigs
kubectl get tigatewaymappings
```

### 方式二：使用 Kubernetes Ingress

```bash
# 1. 创建 Ingress 资源
kubectl apply -f ingress-example.yaml

# 2. TiGateway 会自动发现并转换为路由
# 3. 查看路由状态
curl http://tigateway-service:8090/actuator/ingress/routes
```

## 配置说明

### TiGatewayRouteConfig 主要字段

- `routes`: 路由列表
  - `uri`: 目标服务 URI（支持 `lb://service-name` 格式）
  - `predicates`: 路由断言（Path、Method 等）
  - `filters`: 路由过滤器（StripPrefix、AddRequestHeader 等）
  - `ssoEnabled`: 是否启用 SSO
  - `tokenRelay`: 是否启用 Token Relay

### TiGatewayMapping 主要字段

- `gatewayRef`: 引用的网关实例
- `routeConfigRef`: 引用的路由配置
- `priority`: 优先级（数字越大优先级越高）
- `enabled`: 是否启用

### Ingress 自动发现

TiGateway 会自动发现带有以下注解的 Ingress：
- `tigateway.cn/auto-discover: "true"`

支持的注解：
- `tigateway.cn/rewrite-target`: 路径重写目标
- `tigateway.cn/ssl-redirect`: 是否启用 SSL 重定向

## 更多示例

更多完整的示例请参考：
- `charts/tigateway-crds/examples/` - CRD 完整示例
- `docs/docs/examples/` - 文档中的示例
