# TiGateway ConfigMap Storage

TiGateway支持使用Kubernetes ConfigMap作为应用配置的存储后端，实现云原生架构。

## 特性

- **云原生存储**: 使用Kubernetes ConfigMap存储应用配置
- **自动刷新**: 支持配置自动刷新和缓存
- **高可用**: 利用Kubernetes的高可用特性
- **版本控制**: 支持ConfigMap的版本管理
- **RBAC支持**: 支持Kubernetes RBAC权限控制

## 配置

### 启用ConfigMap存储

在`application.yml`中配置：

```yaml
spring:
  cloud:
    gateway:
      storage:
        configmap:
          enabled: true
          name: tigateway-app-config
          namespace: default
          app-info-key-prefix: "app."
          server-info-key-prefix: "server."
          auto-refresh: true
          refresh-interval: 30
          cache-enabled: true
          cache-expiration: 300
          create-default-config: true
          default-app:
            app-key: default-app
            app-secret: default-secret
            name: Default Application
            desc: Default application for TiGateway
            type: 1
            status: 1
            default-servers:
              - user-service
              - order-service
              - payment-service
```

### ConfigMap结构

ConfigMap中的数据以JSON格式存储，每个应用对应一个key：

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-app-config
  namespace: default
data:
  app.default-app: |
    {
      "name": "Default Application",
      "desc": "Default application for TiGateway",
      "type": 1,
      "appKey": "default-app",
      "appSecret": "default-secret",
      "status": 1,
      "ctime": "2024-01-01T00:00:00",
      "mtime": "2024-01-01T00:00:00",
      "servers": [
        {
          "serverCode": "user-service",
          "appKey": "default-app",
          "serverIps": "*",
          "status": 1,
          "ctime": "2024-01-01T00:00:00",
          "mtime": "2024-01-01T00:00:00"
        }
      ]
    }
```

## 部署

### 1. 创建ConfigMap

```bash
# 应用示例ConfigMap
kubectl apply -f tigateway-app-config.yaml
```

### 2. 配置RBAC

创建ServiceAccount和权限：

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: tigateway-sa
  namespace: default
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: tigateway-configmap-role
  namespace: default
rules:
- apiGroups: [""]
  resources: ["configmaps"]
  verbs: ["get", "list", "watch", "create", "update", "patch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: tigateway-configmap-rolebinding
  namespace: default
subjects:
- kind: ServiceAccount
  name: tigateway-sa
  namespace: default
roleRef:
  kind: Role
  name: tigateway-configmap-role
  apiGroup: rbac.authorization.k8s.io
```

### 3. 部署TiGateway

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tigateway
  namespace: default
spec:
  replicas: 1
  selector:
    matchLabels:
      app: tigateway
  template:
    metadata:
      labels:
        app: tigateway
    spec:
      serviceAccountName: tigateway-sa
      containers:
      - name: tigateway
        image: tigateway:latest
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: configmap
        - name: SPRING_CLOUD_GATEWAY_STORAGE_CONFIGMAP_ENABLED
          value: "true"
```

## 管理应用配置

### 添加新应用

```bash
# 编辑ConfigMap
kubectl edit configmap tigateway-app-config -n default

# 或使用kubectl patch
kubectl patch configmap tigateway-app-config -n default --type merge -p '{
  "data": {
    "app.new-app": "{\"name\":\"New App\",\"appKey\":\"new-app\",\"appSecret\":\"new-secret\",\"status\":1,\"servers\":[]}"
  }
}'
```

### 更新应用配置

```bash
# 更新应用状态
kubectl patch configmap tigateway-app-config -n default --type merge -p '{
  "data": {
    "app.default-app": "{\"name\":\"Updated App\",\"status\":0}"
  }
}'
```

### 删除应用

```bash
# 删除应用配置
kubectl patch configmap tigateway-app-config -n default --type json -p='[
  {"op": "remove", "path": "/data/app.default-app"}
]'
```

## 监控和调试

### 查看ConfigMap内容

```bash
# 查看ConfigMap
kubectl get configmap tigateway-app-config -n default -o yaml

# 查看特定应用配置
kubectl get configmap tigateway-app-config -n default -o jsonpath='{.data.app\.default-app}'
```

### 查看TiGateway日志

```bash
# 查看应用日志
kubectl logs -l app=tigateway -n default

# 实时查看日志
kubectl logs -f -l app=tigateway -n default
```

## 最佳实践

1. **命名规范**: 使用有意义的ConfigMap和应用名称
2. **权限控制**: 使用RBAC限制ConfigMap访问权限
3. **版本管理**: 使用Git管理ConfigMap配置
4. **监控告警**: 监控ConfigMap变更和应用状态
5. **备份恢复**: 定期备份ConfigMap配置

## 故障排除

### 常见问题

1. **ConfigMap不存在**: 检查ConfigMap名称和命名空间
2. **权限不足**: 检查ServiceAccount和RBAC配置
3. **配置格式错误**: 检查JSON格式是否正确
4. **缓存问题**: 重启应用或等待缓存过期

### 调试命令

```bash
# 检查ConfigMap
kubectl describe configmap tigateway-app-config -n default

# 检查ServiceAccount
kubectl describe serviceaccount tigateway-sa -n default

# 检查权限
kubectl auth can-i get configmaps --as=system:serviceaccount:default:tigateway-sa -n default
```
