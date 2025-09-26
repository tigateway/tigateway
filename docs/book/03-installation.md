# 安装和部署

本指南将详细介绍如何在 Kubernetes 环境中安装和部署 TiGateway。

## 部署前准备

### 系统要求

#### Kubernetes 集群要求
- **Kubernetes 版本**: 1.20 或更高
- **集群类型**: 支持 ConfigMap 和 CRD 的集群
- **网络**: 支持 Service 和 Ingress 的网络配置
- **存储**: 支持 ConfigMap 存储

#### 资源要求
- **CPU**: 最少 0.5 核，推荐 1 核
- **内存**: 最少 512MB，推荐 1GB
- **存储**: 最少 1GB，推荐 2GB

#### 权限要求
- **RBAC**: 需要创建 ServiceAccount、Role 和 RoleBinding
- **ConfigMap**: 需要读写 ConfigMap 的权限
- **Service**: 需要创建和管理 Service 的权限
- **Ingress**: 需要创建和管理 Ingress 的权限（可选）

### 环境检查

在开始部署之前，请检查以下环境：

```bash
# 检查 Kubernetes 版本
kubectl version --short

# 检查集群状态
kubectl cluster-info

# 检查节点状态
kubectl get nodes

# 检查命名空间
kubectl get namespaces
```

## 部署方式

### 方式一：使用 Helm 部署（推荐）

#### 1. 添加 Helm 仓库

```bash
# 添加 TiGateway Helm 仓库
helm repo add tigateway https://your-helm-repo.com
helm repo update

# 查看可用的 Chart
helm search repo tigateway
```

#### 2. 创建命名空间

```bash
# 创建命名空间
kubectl create namespace tigateway

# 或者使用 Helm 创建
helm install tigateway-ns tigateway/tigateway-namespace --namespace tigateway
```

#### 3. 配置 Helm Values

创建 `values.yaml` 文件：

```yaml
# values.yaml
image:
  repository: your-registry/tigateway
  tag: latest
  pullPolicy: IfNotPresent

replicaCount: 3

service:
  type: ClusterIP
  ports:
    gateway: 8080
    admin: 8081
    management: 8090

ingress:
  enabled: true
  className: "nginx"
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
  hosts:
    - host: gateway.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: tigateway-tls
      hosts:
        - gateway.example.com

resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 500m
    memory: 512Mi

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80
  targetMemoryUtilizationPercentage: 80

config:
  application:
    server:
      port: 8080
    spring:
      cloud:
        gateway:
          discovery:
            locator:
              enabled: true
          httpclient:
            connect-timeout: 1000
            response-timeout: 5s

monitoring:
  enabled: true
  serviceMonitor:
    enabled: true
    interval: 30s
    scrapeTimeout: 10s

security:
  rbac:
    enabled: true
  podSecurityPolicy:
    enabled: true
```

#### 4. 安装 TiGateway

```bash
# 使用自定义 values 安装
helm install tigateway tigateway/tigateway \
  --namespace tigateway \
  --values values.yaml

# 或者使用默认配置安装
helm install tigateway tigateway/tigateway \
  --namespace tigateway
```

#### 5. 验证安装

```bash
# 检查 Pod 状态
kubectl get pods -n tigateway

# 检查服务状态
kubectl get svc -n tigateway

# 检查 ConfigMap
kubectl get configmap -n tigateway

# 检查 Helm 发布状态
helm list -n tigateway
```

### 方式二：使用 YAML 文件部署

#### 1. 创建命名空间和 RBAC

```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: tigateway
  labels:
    name: tigateway
---
# rbac.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: tigateway
  namespace: tigateway
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: tigateway
rules:
- apiGroups: [""]
  resources: ["configmaps", "services", "endpoints", "secrets"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
- apiGroups: ["networking.k8s.io"]
  resources: ["ingresses", "ingressclasses"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
- apiGroups: ["tigateway.cn"]
  resources: ["tigateways", "tigatewaymappings"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: tigateway
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: tigateway
subjects:
- kind: ServiceAccount
  name: tigateway
  namespace: tigateway
```

```bash
# 应用 RBAC 配置
kubectl apply -f namespace.yaml
kubectl apply -f rbac.yaml
```

#### 2. 创建 ConfigMap

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-app-config
  namespace: tigateway
data:
  application.yml: |
    server:
      port: 8080
    
    spring:
      application:
        name: tigateway
      cloud:
        gateway:
          discovery:
            locator:
              enabled: true
              lower-case-service-id: true
          httpclient:
            connect-timeout: 1000
            response-timeout: 5s
            pool:
              max-connections: 500
              max-idle-time: 30s
          metrics:
            enabled: true
    
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus,gateway
      endpoint:
        health:
          show-details: always
      metrics:
        export:
          prometheus:
            enabled: true
    
    logging:
      level:
        ti.gateway: INFO
        org.springframework.cloud.gateway: DEBUG
---
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
          - id: example-route
            uri: lb://example-service
            predicates:
            - Path=/api/**
            filters:
            - StripPrefix=1
            - AddRequestHeader=X-Gateway, TiGateway
```

```bash
# 应用 ConfigMap
kubectl apply -f configmap.yaml
```

#### 3. 创建部署

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tigateway
  namespace: tigateway
  labels:
    app: tigateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: tigateway
  template:
    metadata:
      labels:
        app: tigateway
    spec:
      serviceAccountName: tigateway
      containers:
      - name: tigateway
        image: your-registry/tigateway:latest
        ports:
        - containerPort: 8080
          name: gateway
        - containerPort: 8081
          name: admin
        - containerPort: 8090
          name: management
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: KUBERNETES_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        volumeMounts:
        - name: app-config
          mountPath: /app/config/application.yml
          subPath: application.yml
        - name: route-config
          mountPath: /app/config/routes.yaml
          subPath: routes.yaml
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8090
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8090
          initialDelaySeconds: 5
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        startupProbe:
          httpGet:
            path: /actuator/health/startup
            port: 8090
          initialDelaySeconds: 10
          periodSeconds: 5
          failureThreshold: 30
        resources:
          limits:
            cpu: 1000m
            memory: 1Gi
          requests:
            cpu: 500m
            memory: 512Mi
      volumes:
      - name: app-config
        configMap:
          name: tigateway-app-config
      - name: route-config
        configMap:
          name: tigateway-route-config
      imagePullSecrets:
      - name: tigateway-registry-secret
```

```bash
# 应用部署
kubectl apply -f deployment.yaml
```

#### 4. 创建服务

```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: tigateway
  namespace: tigateway
  labels:
    app: tigateway
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
    name: gateway
  - port: 8081
    targetPort: 8081
    protocol: TCP
    name: admin
  - port: 8090
    targetPort: 8090
    protocol: TCP
    name: management
  selector:
    app: tigateway
---
apiVersion: v1
kind: Service
metadata:
  name: tigateway-admin
  namespace: tigateway
  labels:
    app: tigateway-admin
spec:
  type: ClusterIP
  ports:
  - port: 8081
    targetPort: 8081
    protocol: TCP
    name: admin
  selector:
    app: tigateway
```

```bash
# 应用服务
kubectl apply -f service.yaml
```

#### 5. 创建 Ingress（可选）

```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tigateway-ingress
  namespace: tigateway
  annotations:
    kubernetes.io/ingress.class: "nginx"
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - gateway.example.com
    secretName: tigateway-tls
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
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tigateway-admin-ingress
  namespace: tigateway
  annotations:
    kubernetes.io/ingress.class: "nginx"
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - admin.gateway.example.com
    secretName: tigateway-admin-tls
  rules:
  - host: admin.gateway.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: tigateway-admin
            port:
              number: 8081
```

```bash
# 应用 Ingress
kubectl apply -f ingress.yaml
```

## 部署验证

### 1. 检查部署状态

```bash
# 检查 Pod 状态
kubectl get pods -n tigateway

# 检查服务状态
kubectl get svc -n tigateway

# 检查 ConfigMap
kubectl get configmap -n tigateway

# 检查 Ingress
kubectl get ingress -n tigateway
```

### 2. 检查日志

```bash
# 查看 Pod 日志
kubectl logs -f deployment/tigateway -n tigateway

# 查看特定容器的日志
kubectl logs -f deployment/tigateway -c tigateway -n tigateway
```

### 3. 健康检查

```bash
# 端口转发到本地
kubectl port-forward svc/tigateway 8080:8080 -n tigateway &
kubectl port-forward svc/tigateway 8081:8081 -n tigateway &
kubectl port-forward svc/tigateway 8090:8090 -n tigateway &

# 检查健康状态
curl http://localhost:8090/actuator/health

# 检查指标
curl http://localhost:8090/actuator/metrics

# 检查路由
curl http://localhost:8090/actuator/gateway/routes
```

### 4. 功能测试

```bash
# 测试网关功能
curl http://localhost:8080/api/test

# 测试管理界面
curl http://localhost:8081/

# 测试监控端点
curl http://localhost:8090/actuator/prometheus
```

## 配置管理

### 1. 更新配置

```bash
# 更新 ConfigMap
kubectl edit configmap tigateway-app-config -n tigateway

# 重启部署以应用新配置
kubectl rollout restart deployment/tigateway -n tigateway
```

### 2. 配置验证

```bash
# 检查配置是否正确加载
kubectl exec -it deployment/tigateway -n tigateway -- cat /app/config/application.yml

# 检查环境变量
kubectl exec -it deployment/tigateway -n tigateway -- env | grep SPRING
```

## 监控和日志

### 1. 启用 Prometheus 监控

```yaml
# service-monitor.yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: tigateway
  namespace: tigateway
  labels:
    app: tigateway
spec:
  selector:
    matchLabels:
      app: tigateway
  endpoints:
  - port: management
    path: /actuator/prometheus
    interval: 30s
    scrapeTimeout: 10s
```

```bash
# 应用 ServiceMonitor
kubectl apply -f service-monitor.yaml
```

### 2. 配置日志收集

```yaml
# fluentd-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: fluentd-config
  namespace: tigateway
data:
  fluent.conf: |
    <source>
      @type tail
      path /var/log/containers/tigateway*.log
      pos_file /var/log/fluentd-containers.log.pos
      tag kubernetes.*
      format json
      time_key time
      time_format %Y-%m-%dT%H:%M:%S.%NZ
    </source>
    
    <match kubernetes.**>
      @type elasticsearch
      host elasticsearch.logging.svc.cluster.local
      port 9200
      index_name tigateway
      type_name _doc
    </match>
```

## 故障排除

### 1. 常见问题

#### Pod 启动失败
```bash
# 检查 Pod 状态
kubectl describe pod -l app=tigateway -n tigateway

# 检查事件
kubectl get events -n tigateway --sort-by='.lastTimestamp'

# 检查日志
kubectl logs -l app=tigateway -n tigateway --previous
```

#### 配置加载失败
```bash
# 检查 ConfigMap
kubectl get configmap tigateway-app-config -n tigateway -o yaml

# 检查挂载
kubectl exec -it deployment/tigateway -n tigateway -- ls -la /app/config/

# 检查环境变量
kubectl exec -it deployment/tigateway -n tigateway -- env
```

#### 服务无法访问
```bash
# 检查服务
kubectl get svc -n tigateway

# 检查端点
kubectl get endpoints -n tigateway

# 检查网络策略
kubectl get networkpolicy -n tigateway
```

### 2. 调试命令

```bash
# 进入 Pod 调试
kubectl exec -it deployment/tigateway -n tigateway -- /bin/bash

# 检查网络连接
kubectl exec -it deployment/tigateway -n tigateway -- netstat -tlnp

# 检查进程
kubectl exec -it deployment/tigateway -n tigateway -- ps aux
```

## 升级和维护

### 1. 升级 TiGateway

```bash
# 使用 Helm 升级
helm upgrade tigateway tigateway/tigateway \
  --namespace tigateway \
  --values values.yaml

# 或者使用 kubectl 升级
kubectl set image deployment/tigateway tigateway=your-registry/tigateway:v2.0.0 -n tigateway
```

### 2. 回滚

```bash
# 使用 Helm 回滚
helm rollback tigateway 1 -n tigateway

# 或者使用 kubectl 回滚
kubectl rollout undo deployment/tigateway -n tigateway
```

### 3. 备份和恢复

```bash
# 备份 ConfigMap
kubectl get configmap -n tigateway -o yaml > tigateway-config-backup.yaml

# 恢复 ConfigMap
kubectl apply -f tigateway-config-backup.yaml
```

## 最佳实践

### 1. 生产环境部署

- **高可用**: 部署多个副本，使用反亲和性
- **资源限制**: 设置合理的 CPU 和内存限制
- **健康检查**: 配置完整的健康检查
- **监控**: 启用 Prometheus 监控和告警
- **日志**: 配置结构化日志和日志收集

### 2. 安全配置

- **RBAC**: 使用最小权限原则
- **网络策略**: 配置网络隔离
- **TLS**: 启用 TLS 加密
- **镜像安全**: 使用安全的基础镜像

### 3. 性能优化

- **资源调优**: 根据负载调整资源分配
- **连接池**: 优化 HTTP 客户端连接池
- **缓存**: 启用配置缓存
- **压缩**: 启用响应压缩

## 总结

本指南详细介绍了 TiGateway 的安装和部署过程：

1. **多种部署方式**: 支持 Helm 和 YAML 两种部署方式
2. **完整配置**: 包含 RBAC、ConfigMap、Service、Ingress 等完整配置
3. **监控集成**: 支持 Prometheus 监控和日志收集
4. **故障排除**: 提供常见问题的解决方案
5. **最佳实践**: 包含生产环境的部署建议

通过本指南，您可以成功在 Kubernetes 环境中部署和运行 TiGateway。
