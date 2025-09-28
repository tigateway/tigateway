# Helm Deployment

This document describes how to deploy TiGateway in Kubernetes using Helm.

## Prerequisites

- Kubernetes 1.20+
- Helm 3.0+
- kubectl configured correctly

## Quick Start

### 1. Add Helm Repository

```bash
helm repo add tigateway https://tigateway.github.io/helm-charts
helm repo update
```

### 2. Install TiGateway

```bash
helm install tigateway tigateway/tigateway
```

### 3. Verify Deployment

```bash
kubectl get pods -l app.kubernetes.io/name=tigateway
```

## Configuration Options

### Basic Configuration

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
  tls:
    - secretName: tigateway-tls
      hosts:
        - tigateway.example.com

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

nodeSelector: {}

tolerations: []

affinity: {}
```

### Advanced Configuration

```yaml
# values.yaml
replicaCount: 3

image:
  repository: tigateway/tigateway
  tag: "1.0.0"
  pullPolicy: IfNotPresent
  pullSecrets:
    - name: tigateway-registry-secret

service:
  type: LoadBalancer
  port: 8080
  managementPort: 9090
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: nlb

ingress:
  enabled: true
  className: "nginx"
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
  hosts:
    - host: tigateway.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: tigateway-tls
      hosts:
        - tigateway.example.com

resources:
  limits:
    cpu: 2000m
    memory: 2Gi
  requests:
    cpu: 1000m
    memory: 1Gi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 20
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 70

podSecurityContext:
  fsGroup: 2000
  runAsNonRoot: true
  runAsUser: 1000

securityContext:
  allowPrivilegeEscalation: false
  capabilities:
    drop:
    - ALL
  readOnlyRootFilesystem: true
  runAsNonRoot: true
  runAsUser: 1000

nodeSelector:
  kubernetes.io/os: linux

tolerations:
  - key: "node-role.kubernetes.io/master"
    operator: "Exists"
    effect: "NoSchedule"

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

# Configuration
config:
  spring:
    profiles:
      active: production
    cloud:
      gateway:
        routes:
          - id: user-service
            uri: lb://user-service
            predicates:
              - Path=/api/users/**
            filters:
              - StripPrefix=2
              - name: CircuitBreaker
                args:
                  name: user-service-cb
                  fallbackUri: forward:/fallback/user-service
        globalcors:
          cors-configurations:
            '[/**]':
              allowedOrigins: "*"
              allowedMethods: "*"
              allowedHeaders: "*"
              allowCredentials: true
    redis:
      host: redis-service
      port: 6379
      password: ${REDIS_PASSWORD}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms

  management:
    endpoints:
      web:
        exposure:
          include: health,info,metrics,prometheus
    endpoint:
      health:
        show-details: always
    metrics:
      export:
        prometheus:
          enabled: true

  logging:
    level:
      com.tigateway: INFO
      org.springframework.cloud.gateway: INFO
    pattern:
      console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file:
      name: /app/logs/tigateway.log
      max-size: 100MB
      max-history: 30

# Secrets
secrets:
  redis-password: "your-redis-password"
  jwt-secret: "your-jwt-secret"

# ConfigMaps
configMaps:
  application-yml: |
    spring:
      profiles:
        active: production
      cloud:
        gateway:
          routes:
            - id: user-service
              uri: lb://user-service
              predicates:
                - Path=/api/users/**
              filters:
                - StripPrefix=2
```

## Production Deployment

### 1. Production Values

```yaml
# values-production.yaml
replicaCount: 5

image:
  repository: tigateway/tigateway
  tag: "1.0.0"
  pullPolicy: IfNotPresent

service:
  type: LoadBalancer
  port: 8080
  managementPort: 9090
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: nlb
    service.beta.kubernetes.io/aws-load-balancer-cross-zone-load-balancing-enabled: "true"

ingress:
  enabled: true
  className: "nginx"
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
  hosts:
    - host: tigateway.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: tigateway-tls
      hosts:
        - tigateway.example.com

resources:
  limits:
    cpu: 4000m
    memory: 4Gi
  requests:
    cpu: 2000m
    memory: 2Gi

autoscaling:
  enabled: true
  minReplicas: 5
  maxReplicas: 50
  targetCPUUtilizationPercentage: 60
  targetMemoryUtilizationPercentage: 60

podSecurityContext:
  fsGroup: 2000
  runAsNonRoot: true
  runAsUser: 1000

securityContext:
  allowPrivilegeEscalation: false
  capabilities:
    drop:
    - ALL
  readOnlyRootFilesystem: true
  runAsNonRoot: true
  runAsUser: 1000

nodeSelector:
  kubernetes.io/os: linux
  node-role.kubernetes.io/worker: "true"

tolerations:
  - key: "node-role.kubernetes.io/master"
    operator: "Exists"
    effect: "NoSchedule"

affinity:
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
    - labelSelector:
        matchExpressions:
        - key: app.kubernetes.io/name
          operator: In
          values:
          - tigateway
      topologyKey: kubernetes.io/hostname

# Production configuration
config:
  spring:
    profiles:
      active: production
    cloud:
      gateway:
        routes:
          - id: user-service
            uri: lb://user-service
            predicates:
              - Path=/api/users/**
            filters:
              - StripPrefix=2
              - name: CircuitBreaker
                args:
                  name: user-service-cb
                  fallbackUri: forward:/fallback/user-service
                  failureThreshold: 5
                  waitDurationInOpenState: 60s
                  successThreshold: 3
        globalcors:
          cors-configurations:
            '[/**]':
              allowedOrigins: ["https://app.example.com"]
              allowedMethods: ["GET", "POST", "PUT", "DELETE"]
              allowedHeaders: ["*"]
              allowCredentials: true
    redis:
      host: redis-service
      port: 6379
      password: ${REDIS_PASSWORD}
      ssl: true
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 2
          max-wait: -1ms

  management:
    endpoints:
      web:
        exposure:
          include: health,info,metrics,prometheus
    endpoint:
      health:
        show-details: always
    metrics:
      export:
        prometheus:
          enabled: true

  logging:
    level:
      com.tigateway: INFO
      org.springframework.cloud.gateway: WARN
    pattern:
      console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file:
      name: /app/logs/tigateway.log
      max-size: 200MB
      max-history: 7

# Production secrets
secrets:
  redis-password: "production-redis-password"
  jwt-secret: "production-jwt-secret"
  ssl-keystore-password: "production-ssl-password"
```

### 2. Install Production Deployment

```bash
# Create namespace
kubectl create namespace tigateway

# Install with production values
helm install tigateway tigateway/tigateway \
  --namespace tigateway \
  --values values-production.yaml \
  --set secrets.redis-password="$(openssl rand -base64 32)" \
  --set secrets.jwt-secret="$(openssl rand -base64 64)"
```

## Monitoring Configuration

### 1. Prometheus Integration

```yaml
# values-monitoring.yaml
serviceMonitor:
  enabled: true
  namespace: monitoring
  interval: 30s
  scrapeTimeout: 10s
  path: /actuator/prometheus
  labels:
    app: tigateway
    release: prometheus

grafana:
  enabled: true
  dashboard:
    enabled: true
    namespace: monitoring
    labels:
      grafana_dashboard: "1"

# Additional monitoring configuration
config:
  management:
    endpoints:
      web:
        exposure:
          include: health,info,metrics,prometheus,env,configprops
    endpoint:
      health:
        show-details: always
    metrics:
      export:
        prometheus:
          enabled: true
      distribution:
        percentiles-histogram:
          http.server.requests: true
        percentiles:
          http.server.requests: 0.5, 0.95, 0.99
```

### 2. Install with Monitoring

```bash
helm install tigateway tigateway/tigateway \
  --namespace tigateway \
  --values values-monitoring.yaml
```

## Security Configuration

### 1. Security Values

```yaml
# values-security.yaml
podSecurityContext:
  fsGroup: 2000
  runAsNonRoot: true
  runAsUser: 1000
  seccompProfile:
    type: RuntimeDefault

securityContext:
  allowPrivilegeEscalation: false
  capabilities:
    drop:
    - ALL
  readOnlyRootFilesystem: true
  runAsNonRoot: true
  runAsUser: 1000

# Network policies
networkPolicy:
  enabled: true
  ingress:
    - from:
        - namespaceSelector:
            matchLabels:
              name: ingress-nginx
      ports:
        - protocol: TCP
          port: 8080
    - from:
        - namespaceSelector:
            matchLabels:
              name: monitoring
      ports:
        - protocol: TCP
          port: 9090

# Pod disruption budget
podDisruptionBudget:
  enabled: true
  minAvailable: 2

# Security configuration
config:
  spring:
    security:
      oauth2:
        client:
          provider:
            sso:
              issuer-uri: ${SSO_ISSUER_URI}
          registration:
            sso:
              client-id: ${SSO_CLIENT_ID}
              client-secret: ${SSO_CLIENT_SECRET}
              scope: openid,profile,email
```

### 2. Install with Security

```bash
helm install tigateway tigateway/tigateway \
  --namespace tigateway \
  --values values-security.yaml \
  --set secrets.sso-client-secret="your-sso-secret"
```

## Customization

### 1. Custom Routes

```yaml
# values-custom.yaml
config:
  spring:
    cloud:
      gateway:
        routes:
          - id: custom-service
            uri: lb://custom-service
            predicates:
              - Path=/api/custom/**
            filters:
              - StripPrefix=2
              - name: AddRequestHeader
                args:
                  name: X-Service-Name
                  value: custom-service
              - name: CircuitBreaker
                args:
                  name: custom-service-cb
                  fallbackUri: forward:/fallback/custom-service
```

### 2. Custom Filters

```yaml
# values-custom-filters.yaml
config:
  spring:
    cloud:
      gateway:
        default-filters:
          - name: AddRequestHeader
            args:
              name: X-Global-Request-ID
              value: "{{random.uuid}}"
          - name: AddResponseHeader
            args:
              name: X-Response-Time
              value: "{{responseTime}}"
```

## Troubleshooting

### 1. Common Issues

#### Pods Not Starting

```bash
# Check pod status
kubectl get pods -l app.kubernetes.io/name=tigateway

# Check pod logs
kubectl logs -l app.kubernetes.io/name=tigateway

# Check pod events
kubectl describe pod <pod-name>
```

#### Service Not Accessible

```bash
# Check service
kubectl get svc -l app.kubernetes.io/name=tigateway

# Check endpoints
kubectl get endpoints -l app.kubernetes.io/name=tigateway

# Test service connectivity
kubectl port-forward svc/tigateway 8080:8080
```

#### Ingress Issues

```bash
# Check ingress
kubectl get ingress -l app.kubernetes.io/name=tigateway

# Check ingress controller
kubectl get pods -n ingress-nginx

# Check ingress logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx
```

### 2. Debug Commands

```bash
# Check Helm release
helm list
helm status tigateway

# Check Helm values
helm get values tigateway

# Check Helm manifest
helm get manifest tigateway

# Upgrade with debug
helm upgrade tigateway tigateway/tigateway --debug --dry-run
```

### 3. Performance Tuning

```yaml
# values-performance.yaml
resources:
  limits:
    cpu: 4000m
    memory: 4Gi
  requests:
    cpu: 2000m
    memory: 2Gi

autoscaling:
  enabled: true
  minReplicas: 5
  maxReplicas: 50
  targetCPUUtilizationPercentage: 60
  targetMemoryUtilizationPercentage: 60

config:
  spring:
    cloud:
      gateway:
        httpclient:
          connect-timeout: 1000
          response-timeout: 5000
          pool:
            max-connections: 1000
            max-idle-time: 30s
            max-life-time: 60s
```

## Best Practices

### 1. Resource Management

- Set appropriate resource limits and requests
- Use horizontal pod autoscaling
- Monitor resource usage
- Implement proper resource quotas

### 2. Security

- Use non-root containers
- Implement network policies
- Use secrets for sensitive data
- Enable pod security policies

### 3. Monitoring

- Enable service monitoring
- Configure proper logging
- Set up alerting
- Use distributed tracing

### 4. Configuration Management

- Use ConfigMaps for configuration
- Use Secrets for sensitive data
- Implement configuration validation
- Use environment-specific values

---

**Related Documentation**:
- [Kubernetes Deployment](./kubernetes.md)
- [Docker Deployment](./docker.md)
- [Monitoring Configuration](./monitoring.md)
