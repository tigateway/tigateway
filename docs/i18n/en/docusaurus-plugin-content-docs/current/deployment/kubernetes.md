# Kubernetes Deployment

This guide covers deploying TiGateway in Kubernetes environments, from basic deployments to production-ready configurations with high availability, monitoring, and security.

## Prerequisites

### Kubernetes Requirements

- **Kubernetes**: v1.19 or later
- **kubectl**: v1.19 or later
- **Helm**: v3.0 or later (optional)
- **Storage**: Persistent storage for configuration and logs
- **Network**: LoadBalancer or Ingress controller

### Resource Requirements

| Environment | CPU | Memory | Storage | Replicas |
|-------------|-----|--------|---------|----------|
| Development | 100m | 256Mi | 1Gi | 1 |
| Staging | 200m | 512Mi | 2Gi | 2 |
| Production | 500m | 1Gi | 5Gi | 3+ |

## Basic Deployment

### 1. Create Namespace

```bash
kubectl create namespace tigateway
```

### 2. Deploy TiGateway

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tigateway
  namespace: tigateway
  labels:
    app: tigateway
    version: v1.0.0
spec:
  replicas: 2
  selector:
    matchLabels:
      app: tigateway
  template:
    metadata:
      labels:
        app: tigateway
        version: v1.0.0
    spec:
      containers:
      - name: tigateway
        image: tigateway/tigateway:1.0.0
        ports:
        - containerPort: 8080
          name: gateway
        - containerPort: 8081
          name: admin
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: SERVER_PORT
          value: "8080"
        - name: MANAGEMENT_SERVER_PORT
          value: "8081"
        resources:
          requests:
            memory: "256Mi"
            cpu: "100m"
          limits:
            memory: "512Mi"
            cpu: "200m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

### 3. Create Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: tigateway
  namespace: tigateway
  labels:
    app: tigateway
spec:
  selector:
    app: tigateway
  ports:
  - port: 8080
    targetPort: 8080
    name: gateway
  - port: 8081
    targetPort: 8081
    name: admin
  type: ClusterIP
```

### 4. Apply Configuration

```bash
kubectl apply -f tigateway-deployment.yaml
kubectl apply -f tigateway-service.yaml
```

## Production Deployment

### 1. Production Deployment Manifest

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tigateway
  namespace: tigateway
  labels:
    app: tigateway
    version: v1.0.0
    environment: production
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  selector:
    matchLabels:
      app: tigateway
  template:
    metadata:
      labels:
        app: tigateway
        version: v1.0.0
        environment: production
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8081"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      serviceAccountName: tigateway
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
      containers:
      - name: tigateway
        image: tigateway/tigateway:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8080
          name: gateway
        - containerPort: 8081
          name: admin
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes,production"
        - name: SERVER_PORT
          value: "8080"
        - name: MANAGEMENT_SERVER_PORT
          value: "8081"
        - name: CONFIG_STORAGE_TYPE
          value: "configmap"
        - name: LOG_LEVEL
          value: "INFO"
        - name: JAVA_OPTS
          value: "-Xms512m -Xmx1g -XX:+UseG1GC"
        resources:
          requests:
            memory: "512Mi"
            cpu: "200m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
          readOnly: true
        - name: logs-volume
          mountPath: /app/logs
      volumes:
      - name: config-volume
        configMap:
          name: tigateway-config
      - name: logs-volume
        emptyDir: {}
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
                - key: app
                  operator: In
                  values:
                  - tigateway
              topologyKey: kubernetes.io/hostname
```

### 2. Production Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: tigateway
  namespace: tigateway
  labels:
    app: tigateway
    environment: production
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: nlb
    service.beta.kubernetes.io/aws-load-balancer-backend-protocol: tcp
spec:
  selector:
    app: tigateway
  ports:
  - port: 80
    targetPort: 8080
    name: gateway
    protocol: TCP
  - port: 443
    targetPort: 8080
    name: gateway-https
    protocol: TCP
  - port: 8081
    targetPort: 8081
    name: admin
    protocol: TCP
  type: LoadBalancer
  loadBalancerSourceRanges:
  - 10.0.0.0/8
  - 172.16.0.0/12
  - 192.168.0.0/16
```

## Configuration Management

### 1. ConfigMap for Configuration

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-config
  namespace: tigateway
  labels:
    app: tigateway
data:
  application.yml: |
    server:
      port: 8080
      compression:
        enabled: true
    
    management:
      server:
        port: 8081
      endpoints:
        web:
          exposure:
            include: "*"
      endpoint:
        health:
          show-details: always
      metrics:
        export:
          prometheus:
            enabled: true
    
    spring:
      application:
        name: tigateway
      profiles:
        active: kubernetes,production
      cloud:
        gateway:
          discovery:
            locator:
              enabled: true
              lower-case-service-id: true
          routes:
            - id: user-service-route
              uri: lb://user-service
              predicates:
                - Path=/api/users/**
              filters:
                - StripPrefix=2
                - AddRequestHeader=X-Gateway,TiGateway
                - CircuitBreaker=user-service-cb,forward:/fallback
            - id: order-service-route
              uri: lb://order-service
              predicates:
                - Path=/api/orders/**
              filters:
                - StripPrefix=2
                - AddRequestHeader=X-Gateway,TiGateway
                - CircuitBreaker=order-service-cb,forward:/fallback
          default-filters:
            - AddRequestHeader=X-Request-ID,${random.uuid}
            - AddResponseHeader=X-Response-Time,${timestamp}
    
    logging:
      level:
        ti.gateway: INFO
        org.springframework.cloud.gateway: INFO
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
        file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
      file:
        name: logs/tigateway.log
        max-size: 100MB
        max-history: 30
```

### 2. Secret for Sensitive Data

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: tigateway-secrets
  namespace: tigateway
  labels:
    app: tigateway
type: Opaque
data:
  # Base64 encoded values
  database-password: <base64-encoded-password>
  jwt-secret: <base64-encoded-jwt-secret>
  oauth2-client-secret: <base64-encoded-oauth2-secret>
```

## Security Configuration

### 1. ServiceAccount and RBAC

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: tigateway
  namespace: tigateway
  labels:
    app: tigateway
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: tigateway
rules:
- apiGroups: [""]
  resources: ["services", "endpoints", "configmaps", "secrets"]
  verbs: ["get", "list", "watch"]
- apiGroups: ["apps"]
  resources: ["deployments", "replicasets"]
  verbs: ["get", "list", "watch"]
- apiGroups: ["tigateway.cn"]
  resources: ["tigateways", "tigatewayrouteconfigs", "tigatewaymappings"]
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

### 2. Network Policies

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: tigateway-network-policy
  namespace: tigateway
  labels:
    app: tigateway
spec:
  podSelector:
    matchLabels:
      app: tigateway
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    - namespaceSelector:
        matchLabels:
          name: monitoring
    ports:
    - protocol: TCP
      port: 8080
    - protocol: TCP
      port: 8081
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: backend-services
    ports:
    - protocol: TCP
      port: 8080
    - protocol: TCP
      port: 443
  - to: []
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
```

### 3. Pod Security Policy

```yaml
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: tigateway-psp
  labels:
    app: tigateway
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
    - ALL
  volumes:
    - 'configMap'
    - 'emptyDir'
    - 'projected'
    - 'secret'
    - 'downwardAPI'
    - 'persistentVolumeClaim'
  runAsUser:
    rule: 'MustRunAsNonRoot'
  seLinux:
    rule: 'RunAsAny'
  fsGroup:
    rule: 'RunAsAny'
```

## Monitoring and Observability

### 1. ServiceMonitor for Prometheus

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: tigateway-monitor
  namespace: tigateway
  labels:
    app: tigateway
    release: prometheus
spec:
  selector:
    matchLabels:
      app: tigateway
  endpoints:
  - port: admin
    path: /actuator/prometheus
    interval: 30s
    scrapeTimeout: 10s
    honorLabels: true
    metricRelabelings:
    - sourceLabels: [__name__]
      regex: 'jvm_.*'
      action: drop
```

### 2. Grafana Dashboard

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-grafana-dashboard
  namespace: monitoring
  labels:
    grafana_dashboard: "1"
data:
  tigateway-dashboard.json: |
    {
      "dashboard": {
        "id": null,
        "title": "TiGateway Dashboard",
        "tags": ["tigateway", "gateway", "api"],
        "timezone": "browser",
        "panels": [
          {
            "id": 1,
            "title": "Request Rate",
            "type": "graph",
            "targets": [
              {
                "expr": "rate(http_requests_total[5m])",
                "legendFormat": "{{method}} {{route}}"
              }
            ]
          },
          {
            "id": 2,
            "title": "Response Time",
            "type": "graph",
            "targets": [
              {
                "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
                "legendFormat": "95th percentile"
              }
            ]
          }
        ]
      }
    }
```

## Auto-scaling

### 1. Horizontal Pod Autoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: tigateway-hpa
  namespace: tigateway
  labels:
    app: tigateway
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: tigateway
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: "100"
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
```

### 2. Vertical Pod Autoscaler

```yaml
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: tigateway-vpa
  namespace: tigateway
  labels:
    app: tigateway
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: tigateway
  updatePolicy:
    updateMode: "Auto"
  resourcePolicy:
    containerPolicies:
    - containerName: tigateway
      maxAllowed:
        cpu: 1
        memory: 2Gi
      minAllowed:
        cpu: 100m
        memory: 128Mi
      controlledResources: ["cpu", "memory"]
```

## Ingress Configuration

### 1. Ingress with TLS

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tigateway-ingress
  namespace: tigateway
  labels:
    app: tigateway
  annotations:
    kubernetes.io/ingress.class: "nginx"
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
    nginx.ingress.kubernetes.io/proxy-connect-timeout: "60"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "60"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "60"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - tigateway.example.com
    secretName: tigateway-tls
  rules:
  - host: tigateway.example.com
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

### 2. Ingress with Rate Limiting

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tigateway-ingress-rate-limited
  namespace: tigateway
  annotations:
    kubernetes.io/ingress.class: "nginx"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
    nginx.ingress.kubernetes.io/rate-limit-connections: "10"
    nginx.ingress.kubernetes.io/rate-limit-requests: "100"
spec:
  rules:
  - host: tigateway.example.com
    http:
      paths:
      - path: /api/
        pathType: Prefix
        backend:
          service:
            name: tigateway
            port:
              number: 8080
```

## Backup and Recovery

### 1. Configuration Backup

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: tigateway-config-backup
  namespace: tigateway
  labels:
    app: tigateway
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: bitnami/kubectl:latest
            command:
            - /bin/sh
            - -c
            - |
              kubectl get configmap tigateway-config -n tigateway -o yaml > /backup/tigateway-config-$(date +%Y%m%d).yaml
              kubectl get secret tigateway-secrets -n tigateway -o yaml > /backup/tigateway-secrets-$(date +%Y%m%d).yaml
            volumeMounts:
            - name: backup-volume
              mountPath: /backup
          volumes:
          - name: backup-volume
            persistentVolumeClaim:
              claimName: tigateway-backup-pvc
          restartPolicy: OnFailure
```

### 2. Disaster Recovery

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-dr-config
  namespace: tigateway
data:
  backup-strategy: |
    - type: scheduled
      schedule: "0 2 * * *"
      retention: 7d
    - type: on-demand
      trigger: manual
      retention: 30d
  restore-strategy: |
    - type: point-in-time
      max-age: 24h
    - type: latest
      fallback: true
```

## Troubleshooting

### Common Issues

#### Pod Not Starting

```bash
# Check pod status
kubectl get pods -n tigateway
kubectl describe pod <pod-name> -n tigateway

# Check logs
kubectl logs <pod-name> -n tigateway
kubectl logs <pod-name> -n tigateway --previous
```

#### Service Not Accessible

```bash
# Check service endpoints
kubectl get endpoints -n tigateway
kubectl describe service tigateway -n tigateway

# Check ingress
kubectl get ingress -n tigateway
kubectl describe ingress tigateway-ingress -n tigateway
```

#### Configuration Issues

```bash
# Check ConfigMap
kubectl get configmap tigateway-config -n tigateway -o yaml

# Check environment variables
kubectl exec <pod-name> -n tigateway -- env
```

### Debug Commands

```bash
# Port forward for local access
kubectl port-forward svc/tigateway 8080:8080 -n tigateway
kubectl port-forward svc/tigateway 8081:8081 -n tigateway

# Check resource usage
kubectl top pods -n tigateway
kubectl top nodes

# Check events
kubectl get events -n tigateway --sort-by='.lastTimestamp'
```

## Best Practices

### 1. Resource Planning

```yaml
# Resource requirements for different environments
environments:
  development:
    replicas: 1
    resources:
      requests:
        memory: "128Mi"
        cpu: "50m"
      limits:
        memory: "256Mi"
        cpu: "100m"
  
  staging:
    replicas: 2
    resources:
      requests:
        memory: "256Mi"
        cpu: "100m"
      limits:
        memory: "512Mi"
        cpu: "200m"
  
  production:
    replicas: 3
    resources:
      requests:
        memory: "512Mi"
        cpu: "200m"
      limits:
        memory: "1Gi"
        cpu: "500m"
```

### 2. Security Best Practices

- Use non-root containers
- Implement network policies
- Use RBAC for service accounts
- Encrypt secrets
- Regular security updates

### 3. Monitoring Best Practices

- Set up comprehensive monitoring
- Configure alerting rules
- Monitor resource usage
- Track performance metrics
- Set up log aggregation

## Next Steps

After deploying TiGateway in Kubernetes:

1. **[Configuration Guide](../configuration.md)** - Configure TiGateway for your needs
2. **[Monitoring Setup](../monitoring-and-metrics.md)** - Set up comprehensive monitoring
3. **[Security Configuration](../security-best-practices.md)** - Secure your deployment
4. **[Troubleshooting](../troubleshooting.md)** - Common issues and solutions

---

**Deployment complete?** Check out our [Configuration Guide](../configuration.md) to configure TiGateway for your specific needs.
