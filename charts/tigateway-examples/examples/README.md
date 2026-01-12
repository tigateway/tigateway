# 硬编码示例参考

本目录包含硬编码的 YAML 示例文件，作为学习和参考使用。

> ⚠️ **注意**: 这些是硬编码的示例文件，仅用于参考。**强烈推荐使用 Helm Chart 模板方式**（通过 `values.yaml` 配置），请参考根目录的 `templates/` 和 `values.yaml`。

## 📁 文件说明

### 硬编码示例文件

- **`basic-examples.yaml`** - 基础示例（TiGateway、RouteConfig、Mapping、Ingress）
- **`microservices.yaml`** - 微服务架构硬编码示例
- **`external-services.yaml`** - 外部服务代理硬编码示例  
- **`multi-tenant.yaml`** - 多租户路由硬编码示例

## 🔄 使用方式对比

### 方式一：使用 Helm Chart（推荐）✅

```bash
# 使用模板化方式，通过 values.yaml 配置
helm install my-examples .. \
  --set scenarios.microservices.enabled=true

# 查看生成的配置
helm get manifest my-examples
```

**优点**:
- ✅ 可配置，无需硬编码
- ✅ 支持多环境（dev/staging/prod）
- ✅ 易于版本控制和复用
- ✅ 符合 Helm 最佳实践

### 方式二：直接使用硬编码示例（仅参考）📖

```bash
# 直接应用硬编码的 YAML（不推荐用于生产）
kubectl apply -f examples/microservices.yaml
```

**适用场景**:
- 📖 学习和理解 CRD 结构
- 📖 快速测试和验证
- 📖 作为模板参考

## 📚 如何迁移到 Helm Chart

### 步骤 1: 查看硬编码示例

```bash
cat examples/microservices.yaml
```

### 步骤 2: 配置 values.yaml

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
            # ... 其他配置
```

### 步骤 3: 使用 Helm 安装

```bash
helm install my-examples . -f my-values.yaml
```

## 🔗 相关资源

- [主 README](../README.md) - Helm Chart 使用说明
- [TiGateway CRD 文档](../../tigateway-crds/docs/user-manual.md)
