# Spring Cloud Gateway with Kubernetes Ingress Support

这个Helm chart部署了一个支持Kubernetes Ingress的Spring Cloud Gateway网关。

## 功能特性

- **Kubernetes Ingress集成**: 自动从Kubernetes Ingress资源读取路由配置
- **动态路由更新**: 实时监听Ingress资源变化并自动更新路由
- **TLS支持**: 支持HTTPS和TLS终止
- **负载均衡**: 集成Spring Cloud LoadBalancer
- **监控和健康检查**: 提供完整的监控和健康检查端点
- **配置管理**: 支持ConfigMap和Secret配置

## 安装

### 1. 添加Helm仓库（如果需要）

```bash
helm repo add your-repo https://your-helm-repo.com
helm repo update
```

### 2. 安装Gateway

```bash
# 使用默认配置安装
helm install my-gateway ./gateway

# 使用自定义配置安装
helm install my-gateway ./gateway -f custom-values.yaml

# 指定命名空间
helm install my-gateway ./gateway --namespace gateway-system --create-namespace
```

### 3. 验证安装

```bash
# 检查Pod状态
kubectl get pods -l app=gateway

# 检查服务状态
kubectl get svc -l app=gateway

# 检查Ingress状态
kubectl get ingress
```

## 配置

### 基本配置

```yaml
# values.yaml
namespace: default
replicaCount: 2

image:
  repository: royalwang/gateway
  tag: "1.0.0"

service:
  name: my-gateway
  type: ClusterIP
  port: 80
  managementPort: 8090

ingress:
  enabled: true
  host: gateway.local
  refreshInterval: 30
  cacheEnabled: true
  tls:
    enabled: false
```

### Ingress配置

```yaml
ingress:
  enabled: true                    # 启用Ingress支持
  namespace: default               # 监听的命名空间
  refreshInterval: 30              # 路由刷新间隔（秒）
  cacheEnabled: true               # 启用路由缓存
  cacheExpiration: 300             # 缓存过期时间（秒）
  tls:
    enabled: true                  # 启用TLS支持
    secretName: gateway-tls        # TLS证书Secret名称
  pathRewrite:
    enabled: true                  # 启用路径重写
    pattern: "/(.*)"               # 重写模式
    replacement: "/$1"             # 重写替换
```

## 使用示例

### 1. 创建示例应用

```bash
# 部署示例应用
kubectl apply -f example-ingress.yaml
```

### 2. 配置路由

创建Ingress资源来定义路由：

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-app-ingress
  namespace: default
spec:
  ingressClassName: nginx
  rules:
  - host: myapp.local
    http:
      paths:
      - path: /api(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: my-api-service
            port:
              number: 8080
      - path: /web(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: my-web-service
            port:
              number: 80
```

### 3. 访问应用

```bash
# 通过Ingress访问
curl -H "Host: myapp.local" http://gateway-service/api/users

# 通过管理端点查看路由状态
curl http://gateway-service:8090/actuator/ingress/routes
```

## 管理端点

Gateway提供以下管理端点：

- `/actuator/health` - 健康检查
- `/actuator/ingress/config` - Ingress配置信息
- `/actuator/ingress/refresh` - 手动刷新路由
- `/actuator/ingress/routes` - 查看当前路由
- `/actuator/gateway/routes` - Spring Cloud Gateway路由信息

## 监控和日志

### 日志配置

```yaml
logging:
  level:
    ingress: INFO
    gateway: INFO
    kubernetes: WARN
```

### Prometheus监控

Gateway自动暴露Prometheus指标：

```yaml
# 在values.yaml中配置
service:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8090"
    prometheus.io/path: "/actuator/prometheus"
```

## 故障排除

### 常见问题

1. **路由不更新**
   ```bash
   # 检查Ingress资源
   kubectl get ingress
   
   # 手动刷新路由
   curl -X POST http://gateway-service:8090/actuator/ingress/refresh
   ```

2. **权限问题**
   ```bash
   # 检查ServiceAccount权限
   kubectl describe clusterrolebinding gateway-ingress-reader-binding
   ```

3. **连接问题**
   ```bash
   # 检查服务状态
   kubectl get svc
   
   # 检查Pod日志
   kubectl logs -l app=gateway
   ```

### 调试模式

启用调试日志：

```yaml
logging:
  level:
    ingress: DEBUG
    gateway: DEBUG
    kubernetes: DEBUG
```

## 升级

```bash
# 升级到新版本
helm upgrade my-gateway ./gateway

# 回滚到之前版本
helm rollback my-gateway 1
```

## 卸载

```bash
# 卸载Gateway
helm uninstall my-gateway

# 删除相关资源
kubectl delete ingress --all
kubectl delete service --all
kubectl delete deployment --all
```

## 贡献

欢迎提交Issue和Pull Request来改进这个项目。

## 许可证

本项目采用MIT许可证。
