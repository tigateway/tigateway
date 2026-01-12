# TiGateway 业务示例 Chart

这是一个标准的 Helm Chart，通过模板化方式生成 TiGateway CRD 配置，支持多种业务场景。所有配置都通过 `values.yaml` 进行管理，无需硬编码。

## 📦 Chart 信息

- **Chart Name**: `tigateway-examples`
- **Version**: `1.0.0`
- **Type**: 业务示例 Chart（生成 CRD 配置）

> ⚠️ **注意**: 这不是用于部署 TiGateway 的 Chart，而是用于生成业务路由配置的示例 Chart。如需部署 TiGateway，请使用 `charts/tigateway` Chart。

## 🚀 快速开始

### 前置条件

1. 已安装 TiGateway CRD：
   ```bash
   helm install tigateway-crds ../tigateway-crds
   ```

2. 已部署 TiGateway 实例：
   ```bash
   helm install tigateway ../tigateway
   ```

### 安装示例配置

```bash
# 使用默认配置（基础示例）
helm install my-examples . --namespace default

# 启用微服务场景
helm install my-examples . \
  --set scenarios.microservices.enabled=true \
  --namespace default

# 启用多个场景
helm install my-examples . \
  --set scenarios.microservices.enabled=true \
  --set scenarios.externalServices.enabled=true \
  --namespace default

# 使用自定义 values 文件
helm install my-examples . -f my-values.yaml
```

## 📋 目录结构

标准的 Helm Chart 目录结构：

```
charts/tigateway-examples/
├── Chart.yaml                    # Chart 元数据
├── values.yaml                   # 默认配置值（可配置）
├── .helmignore                   # Helm 忽略文件
├── README.md                     # 本文件
│
├── templates/                    # Helm 模板目录（必需）
│   ├── _helpers.tpl              # 模板辅助函数
│   ├── NOTES.txt                 # 安装后提示
│   ├── tigateway.yaml            # TiGateway 实例模板
│   ├── tigateway-route-config.yaml # 路由配置模板
│   ├── tigateway-mapping.yaml    # 映射关系模板
│   ├── ingress.yaml              # Ingress 模板
│   └── scenarios/                # 业务场景模板
│       ├── microservices.yaml    # 微服务场景
│       ├── external-services.yaml # 外部服务场景
│       └── multi-tenant.yaml     # 多租户场景
│
└── examples/                      # 硬编码示例（仅参考）
    ├── README.md                 # 示例说明
    ├── basic-examples.yaml       # 基础示例（合并）
    ├── microservices.yaml        # 微服务场景示例
    ├── external-services.yaml    # 外部服务场景示例
    └── multi-tenant.yaml         # 多租户场景示例
```

## ⚙️ 配置说明

### 基础配置

通过 `values.yaml` 配置基础资源：

```yaml
gateway:
  enabled: true
  name: my-gateway
  namespace: default
  count: 2

routes:
  enabled: true
  name: my-gateway-routes
  routes:
    - title: "My Route"
      uri: "lb://my-service"
      predicates:
        - "Path=/api/**"
```

### 业务场景配置

#### 微服务场景

```yaml
scenarios:
  microservices:
    enabled: true
    namespace: default
    services:
      - name: user-service
        routes:
          - title: "User API"
            uri: "lb://user-service"
            predicates:
              - "Path=/api/users/**"
```

#### 外部服务场景

```yaml
scenarios:
  externalServices:
    enabled: true
    services:
      - title: "GitHub Proxy"
        uri: "https://api.github.com"
        predicates:
          - "Path=/github/**"
```

#### 多租户场景

```yaml
scenarios:
  multiTenant:
    enabled: true
    tenants:
      - name: tenant-a
        namespace: tenant-a
        routes:
          - title: "Tenant A API"
            uri: "lb://tenant-a-service"
```

## 📖 使用示例

### 示例 1: 基础配置

```bash
# 1. 查看默认配置
helm show values .

# 2. 安装基础配置
helm install my-examples .

# 3. 查看生成的资源
kubectl get tigateway
kubectl get tigatewayrouteconfigs
kubectl get tigatewaymappings
```

### 示例 2: 启用微服务场景

```bash
# 创建自定义 values 文件
cat > my-microservices.yaml <<EOF
scenarios:
  microservices:
    enabled: true
    namespace: production
    services:
      - name: user-service
        routes:
          - title: "User API"
            uri: "lb://user-service"
            predicates:
              - "Path=/api/users/**"
            filters:
              - "StripPrefix=2"
EOF

# 安装
helm install my-microservices . -f my-microservices.yaml
```

### 示例 3: 多场景组合

```bash
helm install my-examples . \
  --set scenarios.microservices.enabled=true \
  --set scenarios.externalServices.enabled=true \
  --set gateway.name=prod-gateway \
  --set global.namespace=production
```

## 🔍 验证和调试

### 查看生成的资源

```bash
# 查看所有资源
helm get manifest my-examples

# 查看特定资源
kubectl get tigatewayrouteconfigs -o yaml
kubectl get tigatewaymappings -o yaml
```

### 模板渲染测试

```bash
# 渲染模板（不安装）
helm template my-examples .

# 渲染特定场景
helm template my-examples . \
  --set scenarios.microservices.enabled=true

# 验证模板语法
helm lint .
```

### 查看路由状态

```bash
# 通过 Actuator 端点查看路由
curl http://tigateway-service:8090/actuator/gateway/routes
curl http://tigateway-service:8090/actuator/ingress/routes
```

## 🎯 配置最佳实践

### 1. 使用命名空间隔离

```yaml
global:
  namespace: production

scenarios:
  microservices:
    namespace: production
```

### 2. 环境特定配置

```bash
# 开发环境
helm install dev-examples . \
  --set global.namespace=dev \
  --set gateway.count=1

# 生产环境
helm install prod-examples . \
  --set global.namespace=production \
  --set gateway.count=3
```

### 3. 版本控制

将自定义的 `values.yaml` 文件提交到版本控制系统：

```bash
# 保存配置
helm get values my-examples > my-values.yaml
git add my-values.yaml
```

## 📚 相关资源

- [TiGateway CRD 文档](../tigateway-crds/docs/user-manual.md)
- [TiGateway 部署 Chart](../tigateway/README.md)
- [硬编码示例参考](examples/README.md) - 查看硬编码示例作为参考

## 🔄 与硬编码示例的区别

| 特性 | Helm Chart（本 Chart） | 硬编码示例（examples/） |
|------|----------------------|----------------------|
| 配置方式 | values.yaml | 直接编辑 YAML |
| 可复用性 | ✅ 高 | ❌ 低 |
| 环境管理 | ✅ 支持多环境 | ❌ 需手动复制 |
| 版本控制 | ✅ 易于管理 | ⚠️ 需手动管理 |
| 推荐使用 | ✅ 生产环境 | 📖 学习参考 |

## 🆘 获取帮助

- 查看模板: `helm template my-examples .`
- 验证配置: `helm lint .`
- 查看值: `helm show values .`
- 提交 Issue: https://github.com/tigateway/tigateway/issues

## 📄 许可证

本项目采用 Apache 2.0 许可证。
