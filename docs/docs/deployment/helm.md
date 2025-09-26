# Helm 部署

本文档介绍如何使用 Helm 在 Kubernetes 中部署 TiGateway。

## 前提条件

- Kubernetes 1.20+
- Helm 3.0+
- kubectl 配置正确

## 快速开始

### 1. 添加 Helm 仓库

```bash
helm repo add tigateway https://tigateway.github.io/helm-charts
helm repo update
```

### 2. 安装 TiGateway

```bash
helm install tigateway tigateway/tigateway
```

### 3. 验证部署

```bash
kubectl get pods -l app.kubernetes.io/name=tigateway
```

## 配置选项

### 基本配置

```yaml
# values.yaml
replicaCount: 2

image:
  repository: tigateway/tigateway
  tag: "latest"
  pullPolicy: IfNotPresent

service:
  type: ClusterIP
  port: 8080
  managementPort: 9090

ingress:
  enabled: true
  className: "nginx"
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
  hosts:
    - host: tigateway.example.com
      paths:
        - path: /
          pathType: Prefix
  tls: []
```

### 高级配置

```yaml
# 资源配置
resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 500m
    memory: 512Mi

# 自动扩缩容
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80
  targetMemoryUtilizationPercentage: 80

# 持久化存储
persistence:
  enabled: true
  storageClass: "fast-ssd"
  accessMode: ReadWriteOnce
  size: 10Gi

# 监控配置
monitoring:
  enabled: true
  serviceMonitor:
    enabled: true
    interval: 30s
    scrapeTimeout: 10s
```

## 自定义安装

### 1. 下载 Chart

```bash
helm pull tigateway/tigateway --untar
```

### 2. 修改配置

编辑 `tigateway/values.yaml` 文件。

### 3. 安装

```bash
helm install tigateway ./tigateway -f custom-values.yaml
```

## 配置管理

### ConfigMap

```yaml
config:
  application.yml: |
    server:
      port: 8080
    management:
      port: 9090
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
    spring:
      cloud:
        gateway:
          routes:
            - id: example-route
              uri: http://httpbin.org
              predicates:
                - Path=/api/**
```

### Secret

```yaml
secrets:
  database:
    username: "admin"
    password: "secret"
  jwt:
    secret: "your-jwt-secret"
```

## 服务发现

### 自动服务发现

```yaml
discovery:
  enabled: true
  type: kubernetes
  namespace: default
```

### 手动配置

```yaml
routes:
  - id: user-service
    uri: lb://user-service
    predicates:
      - Path=/api/users/**
  - id: order-service
    uri: lb://order-service
    predicates:
      - Path=/api/orders/**
```

## 监控和日志

### Prometheus 集成

```yaml
monitoring:
  prometheus:
    enabled: true
    serviceMonitor:
      enabled: true
      namespace: monitoring
      labels:
        app: tigateway
```

### Grafana 仪表板

```bash
# 导入 Grafana 仪表板
kubectl apply -f https://raw.githubusercontent.com/tigateway/helm-charts/main/grafana-dashboard.yaml
```

### 日志收集

```yaml
logging:
  level:
    root: INFO
    com.tigateway: DEBUG
  appenders:
    - type: console
    - type: file
      file: /var/log/tigateway/application.log
```

## 高可用部署

### 多副本部署

```yaml
replicaCount: 3

podDisruptionBudget:
  enabled: true
  minAvailable: 2

affinity:
  podAntiAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
    - weight: 100
      podAffinityTerm:
        labelSelector:
          matchExpressions:
          - key: app.kubernetes.io/name
            operator: In
            values:
            - tigateway
        topologyKey: kubernetes.io/hostname
```

### 负载均衡

```yaml
service:
  type: LoadBalancer
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: nlb
```

## 升级和回滚

### 升级

```bash
helm upgrade tigateway tigateway/tigateway -f values.yaml
```

### 回滚

```bash
helm rollback tigateway 1
```

### 查看历史

```bash
helm history tigateway
```

## 故障排除

### 常见问题

1. **Pod 无法启动**
   ```bash
   kubectl describe pod <pod-name>
   kubectl logs <pod-name>
   ```

2. **服务无法访问**
   ```bash
   kubectl get svc tigateway
   kubectl get ingress tigateway
   ```

3. **配置问题**
   ```bash
   kubectl get configmap tigateway-config -o yaml
   ```

### 调试命令

```bash
# 查看所有资源
kubectl get all -l app.kubernetes.io/name=tigateway

# 进入 Pod
kubectl exec -it <pod-name> -- /bin/sh

# 查看事件
kubectl get events --sort-by=.metadata.creationTimestamp
```

## 生产环境建议

1. 使用具体的镜像标签
2. 配置资源限制和请求
3. 启用自动扩缩容
4. 配置 Pod 反亲和性
5. 启用监控和日志收集
6. 配置备份策略
7. 使用 TLS 加密
8. 定期更新和补丁
