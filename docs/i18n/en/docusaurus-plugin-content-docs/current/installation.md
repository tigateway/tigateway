# Installation

This guide covers different installation methods for TiGateway, from development environments to production deployments.

## Prerequisites

### System Requirements

- **Kubernetes**: v1.19 or later
- **kubectl**: v1.19 or later
- **Helm**: v3.0 or later (for Helm installation)
- **Docker**: v20.10 or later (for local development)

### Resource Requirements

| Component | CPU | Memory | Storage |
|-----------|-----|--------|---------|
| Gateway | 100m | 256Mi | 1Gi |
| Admin | 50m | 128Mi | 512Mi |
| Operator | 50m | 128Mi | 512Mi |

## Installation Methods

### 1. Helm Installation (Recommended)

Helm is the recommended way to install TiGateway as it provides easy configuration management and upgrades.

#### Add Helm Repository

```bash
helm repo add tigateway https://tigateway.github.io/helm-charts
helm repo update
```

#### Basic Installation

```bash
# Create namespace
kubectl create namespace tigateway

# Install TiGateway
helm install tigateway tigateway/tigateway \
  --namespace tigateway
```

#### Custom Configuration

```bash
# Install with custom values
helm install tigateway tigateway/tigateway \
  --namespace tigateway \
  --set replicaCount=3 \
  --set image.tag=1.0.0 \
  --set service.type=LoadBalancer \
  --set admin.enabled=true
```

#### Production Installation

```bash
# Create values file for production
cat > tigateway-values.yaml << EOF
replicaCount: 3

image:
  repository: tigateway/tigateway
  tag: "1.0.0"
  pullPolicy: IfNotPresent

service:
  type: LoadBalancer
  port: 8080

admin:
  enabled: true
  service:
    type: ClusterIP
    port: 8081

resources:
  limits:
    cpu: 500m
    memory: 1Gi
  requests:
    cpu: 100m
    memory: 256Mi

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70

monitoring:
  enabled: true
  serviceMonitor:
    enabled: true

security:
  enabled: true
  tls:
    enabled: true
EOF

# Install with production values
helm install tigateway tigateway/tigateway \
  --namespace tigateway \
  --values tigateway-values.yaml
```

### 2. YAML Manifests Installation

For environments where Helm is not available or for custom deployments.

#### Download Manifests

```bash
# Clone the repository
git clone https://github.com/tigateway/tigateway.git
cd tigateway

# Or download specific manifests
curl -O https://raw.githubusercontent.com/tigateway/tigateway/main/helm/tigateway/templates/deployment.yaml
curl -O https://raw.githubusercontent.com/tigateway/tigateway/main/helm/tigateway/templates/service.yaml
curl -O https://raw.githubusercontent.com/tigateway/tigateway/main/helm/tigateway/templates/configmap.yaml
```

#### Apply Manifests

```bash
# Create namespace
kubectl create namespace tigateway

# Apply CRDs first
kubectl apply -f helm/tigateway-crds/templates/ -n tigateway

# Apply TiGateway components
kubectl apply -f helm/tigateway/templates/ -n tigateway
```

### 3. Operator Installation

For advanced use cases with custom resource management.

#### Install the Operator

```bash
# Install TiGateway Operator
kubectl apply -f https://raw.githubusercontent.com/tigateway/tigateway/main/ti-gateway-operator/deploy/operator.yaml
```

#### Create TiGateway Instance

```yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: tigateway-instance
  namespace: tigateway
spec:
  replicas: 2
  image: tigateway/tigateway:1.0.0
  resources:
    requests:
      memory: "256Mi"
      cpu: "100m"
    limits:
      memory: "1Gi"
      cpu: "500m"
  config:
    storage:
      configmap:
        enabled: true
        name: tigateway-config
```

Apply the instance:
```bash
kubectl apply -f tigateway-instance.yaml
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Gateway port | `8080` |
| `ADMIN_PORT` | Admin interface port | `8081` |
| `SPRING_PROFILES_ACTIVE` | Spring profiles | `kubernetes` |
| `CONFIG_STORAGE_TYPE` | Configuration storage type | `configmap` |
| `LOG_LEVEL` | Logging level | `INFO` |

### Configuration Files

#### application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: tigateway
  profiles:
    active: kubernetes
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always

logging:
  level:
    ti.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
```

#### ConfigMap Configuration

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-config
  namespace: tigateway
data:
  application.yml: |
    server:
      port: 8080
    spring:
      cloud:
        gateway:
          routes:
            - id: default-route
              uri: lb://test-service
              predicates:
                - Path=/test/**
              filters:
                - StripPrefix=1
```

## Verification

### Check Installation Status

```bash
# Check pods
kubectl get pods -n tigateway

# Check services
kubectl get svc -n tigateway

# Check CRDs
kubectl get crd | grep tigateway
```

Expected output:
```
NAME                        READY   STATUS    RESTARTS   AGE
tigateway-7d4b8c9f6-abc123  1/1     Running   0          2m
tigateway-7d4b8c9f6-def456  1/1     Running   0          2m

NAME                TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
tigateway           LoadBalancer   10.96.123.45    <pending>     8080:30080/TCP   2m
tigateway-admin     ClusterIP      10.96.234.56    <none>        8081:30081/TCP   2m

NAME                                    CREATED AT
tigateways.tigateway.cn                 2024-09-23T10:00:00Z
tigatewaymappings.tigateway.cn          2024-09-23T10:00:00Z
tigatewayrouteconfigs.tigateway.cn      2024-09-23T10:00:00Z
```

### Health Checks

```bash
# Check gateway health
kubectl port-forward svc/tigateway 8080:8080 -n tigateway
curl http://localhost:8080/actuator/health

# Check admin health
kubectl port-forward svc/tigateway-admin 8081:8081 -n tigateway
curl http://localhost:8081/actuator/health
```

### Test Basic Functionality

```bash
# Create a test service
kubectl run test-service --image=nginx:alpine --port=80 -n tigateway
kubectl expose pod test-service --port=80 -n tigateway

# Create a test route
cat > test-route.yaml << EOF
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: test-route
  namespace: tigateway
spec:
  routes:
    - id: test-route
      uri: lb://test-service
      predicates:
        - name: Path
          args:
            pattern: "/test/**"
      filters:
        - name: StripPrefix
          args:
            parts: 1
EOF

kubectl apply -f test-route.yaml

# Test the route
GATEWAY_URL=$(kubectl get svc tigateway -n tigateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
curl http://$GATEWAY_URL/test/
```

## Upgrades

### Helm Upgrade

```bash
# Update repository
helm repo update

# Upgrade TiGateway
helm upgrade tigateway tigateway/tigateway \
  --namespace tigateway \
  --set image.tag=1.1.0
```

### Rolling Update

```bash
# Update deployment image
kubectl set image deployment/tigateway \
  tigateway=tigateway/tigateway:1.1.0 \
  -n tigateway

# Check rollout status
kubectl rollout status deployment/tigateway -n tigateway
```

## Uninstallation

### Helm Uninstall

```bash
helm uninstall tigateway -n tigateway
```

### Manual Cleanup

```bash
# Delete deployments
kubectl delete deployment tigateway tigateway-admin -n tigateway

# Delete services
kubectl delete svc tigateway tigateway-admin -n tigateway

# Delete CRDs
kubectl delete crd tigateways.tigateway.cn
kubectl delete crd tigatewaymappings.tigateway.cn
kubectl delete crd tigatewayrouteconfigs.tigateway.cn

# Delete namespace
kubectl delete namespace tigateway
```

## Troubleshooting

### Common Issues

#### Pods Not Starting

```bash
# Check pod status
kubectl describe pod <pod-name> -n tigateway

# Check logs
kubectl logs <pod-name> -n tigateway
```

#### Service Not Accessible

```bash
# Check service endpoints
kubectl get endpoints -n tigateway

# Check service configuration
kubectl describe svc tigateway -n tigateway
```

#### Configuration Issues

```bash
# Check ConfigMap
kubectl get configmap -n tigateway
kubectl describe configmap tigateway-config -n tigateway

# Check environment variables
kubectl exec <pod-name> -n tigateway -- env
```

### Log Analysis

```bash
# View gateway logs
kubectl logs -f deployment/tigateway -n tigateway

# View admin logs
kubectl logs -f deployment/tigateway-admin -n tigateway

# Search for errors
kubectl logs deployment/tigateway -n tigateway | grep -i error
```

### Performance Issues

```bash
# Check resource usage
kubectl top pods -n tigateway

# Check node resources
kubectl top nodes

# Check events
kubectl get events -n tigateway --sort-by='.lastTimestamp'
```

## Next Steps

After successful installation:

1. **[Configuration Guide](./configuration.md)** - Learn about configuration options
2. **[Quick Start](./quick-start.md)** - Create your first route
3. **[Production Setup](./deployment/kubernetes.md)** - Production deployment best practices
4. **[Monitoring Setup](./monitoring-and-metrics.md)** - Set up monitoring and alerting

---

**Installation complete?** Check out our [Quick Start Guide](./quick-start.md) to create your first route.
