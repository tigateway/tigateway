# 快速开始

本指南将帮助您在几分钟内快速部署和运行 TiGateway。

## 前提条件

在开始之前，请确保您已经安装了以下组件：

- **Kubernetes 集群**: 版本 1.20 或更高
- **kubectl**: 配置为连接到您的 Kubernetes 集群
- **Helm**: 版本 3.0 或更高（可选，用于 Helm 安装）

## 方法一：使用 Helm 安装（推荐）

### 1. 添加 Helm 仓库

```bash
helm repo add tigateway https://your-helm-repo.com
helm repo update
```

### 2. 安装 TiGateway

```bash
# 创建命名空间
kubectl create namespace tigateway

# 安装 TiGateway
helm install tigateway tigateway/tigateway \
  --namespace tigateway \
  --set image.repository=your-registry/tigateway \
  --set image.tag=latest
```

### 3. 验证安装

```bash
# 检查 Pod 状态
kubectl get pods -n tigateway

# 检查服务状态
kubectl get svc -n tigateway
```

## 方法二：使用 YAML 文件安装

### 1. 下载部署文件

```bash
# 克隆仓库
git clone https://github.com/your-org/tigateway.git
cd tigateway

# 或者直接下载部署文件
curl -O https://raw.githubusercontent.com/your-org/tigateway/main/helm/gateway/gateway-deployment.yaml
```

### 2. 创建命名空间和 RBAC

```bash
# 创建命名空间
kubectl create namespace tigateway

# 应用 RBAC 配置
kubectl apply -f helm/gateway/gateway-rbac.yaml -n tigateway
```

### 3. 部署 TiGateway

```bash
# 部署 ConfigMap
kubectl apply -f helm/gateway/gateway-configmap.yaml -n tigateway

# 部署服务
kubectl apply -f helm/gateway/gateway-service.yaml -n tigateway

# 部署主应用
kubectl apply -f helm/gateway/gateway-deployment.yaml -n tigateway
```

## 配置基本路由

### 1. 创建测试服务

首先创建一个简单的测试服务：

```yaml
apiVersion: v1
kind: Service
metadata:
  name: test-service
  namespace: tigateway
spec:
  selector:
    app: test-service
  ports:
  - port: 80
    targetPort: 8080
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: test-service
  namespace: tigateway
spec:
  replicas: 1
  selector:
    matchLabels:
      app: test-service
  template:
    metadata:
      labels:
        app: test-service
    spec:
      containers:
      - name: test-service
        image: nginx:alpine
        ports:
        - containerPort: 80
```

### 2. 配置路由

创建 ConfigMap 配置基本路由：

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-route-config
  namespace: tigateway
data:
  routes.yaml: |
    spring:
      cloud:
        gateway:
          routes:
          - id: test-route
            uri: http://test-service
            predicates:
            - Path=/test/**
            filters:
            - StripPrefix=1
```

### 3. 更新 TiGateway 配置

```bash
# 更新 ConfigMap
kubectl apply -f route-config.yaml -n tigateway

# 重启 TiGateway 以加载新配置
kubectl rollout restart deployment/tigateway -n tigateway
```

## 访问服务

### 1. 获取访问地址

```bash
# 获取服务信息
kubectl get svc -n tigateway

# 如果使用 LoadBalancer
kubectl get svc tigateway -n tigateway

# 如果使用 NodePort
kubectl get svc tigateway -n tigateway -o jsonpath='{.spec.ports[0].nodePort}'
```

### 2. 测试路由

```bash
# 测试基本路由
curl http://your-gateway-ip:8080/test/

# 测试管理界面
curl http://your-gateway-ip:8081/

# 测试健康检查
curl http://your-gateway-ip:8090/actuator/health
```

## 使用 Ingress 访问

### 1. 创建 Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tigateway-ingress
  namespace: tigateway
  annotations:
    kubernetes.io/ingress.class: "tigateway"
spec:
  rules:
  - host: gateway.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: tigateway
            port:
              number: 8080
```

### 2. 配置域名解析

将 `gateway.example.com` 解析到您的 Kubernetes 集群 IP 地址。

### 3. 测试访问

```bash
# 通过域名访问
curl http://gateway.example.com/test/
```

## 管理界面

### 1. 访问管理界面

打开浏览器访问：`http://your-gateway-ip:8081`

### 2. 基本功能

管理界面提供以下功能：

- **路由管理**: 查看和管理路由配置
- **服务发现**: 查看 Kubernetes 服务
- **配置管理**: 编辑和更新配置
- **监控面板**: 查看系统指标和状态

### 3. 创建路由

在管理界面中：

1. 点击 "路由管理"
2. 点击 "新建路由"
3. 填写路由信息：
   - **路由 ID**: `my-route`
   - **目标 URI**: `http://my-service`
   - **路径匹配**: `/api/**`
4. 点击 "保存"

## 监控和健康检查

### 1. 健康检查端点

```bash
# 基本健康检查
curl http://your-gateway-ip:8090/actuator/health

# 详细信息
curl http://your-gateway-ip:8090/actuator/health | jq
```

### 2. 指标端点

```bash
# Prometheus 指标
curl http://your-gateway-ip:8090/actuator/prometheus

# 应用信息
curl http://your-gateway-ip:8090/actuator/info
```

### 3. 配置查看

```bash
# 查看当前配置
curl http://your-gateway-ip:8090/actuator/gateway/routes
```

## 常见问题

### Q: Pod 启动失败

**A**: 检查以下项目：

```bash
# 查看 Pod 日志
kubectl logs -f deployment/tigateway -n tigateway

# 检查资源限制
kubectl describe pod -l app=tigateway -n tigateway

# 检查 ConfigMap
kubectl get configmap -n tigateway
```

### Q: 路由不生效

**A**: 检查配置：

```bash
# 检查路由配置
kubectl get configmap tigateway-route-config -n tigateway -o yaml

# 检查服务发现
kubectl get svc -n tigateway

# 重启服务
kubectl rollout restart deployment/tigateway -n tigateway
```

### Q: 无法访问管理界面

**A**: 检查服务配置：

```bash
# 检查服务端口
kubectl get svc tigateway -n tigateway

# 检查端口转发
kubectl port-forward svc/tigateway 8081:8081 -n tigateway
```

## 下一步

现在您已经成功部署了 TiGateway，可以继续学习：

- [架构概述](architecture.md) - 深入了解 TiGateway 的架构
- [路由和谓词](routes-and-predicates.md) - 学习如何配置路由
- [过滤器](filters.md) - 学习如何使用过滤器
- [Kubernetes 原生特性](kubernetes-native.md) - 学习 Kubernetes 集成功能
