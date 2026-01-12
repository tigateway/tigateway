# TiGateway CRDs Helm Chart

This Helm chart installs the Custom Resource Definitions (CRDs) for TiGateway, a Kubernetes-native API Gateway solution.

## Overview

TiGateway CRDs provide a declarative way to manage API Gateway configurations in Kubernetes. The chart includes three main CRDs:

- **TiGateway**: Main gateway instance configuration
- **TiGatewayMapping**: Maps route configurations to gateway instances
- **TiGatewayRouteConfig**: Defines routing rules and configurations

## Features

- **Kubernetes Native**: Full integration with Kubernetes APIs
- **Ingress Integration**: Automatic discovery and management of Kubernetes Ingress resources
- **IngressClass Support**: Native support for Kubernetes IngressClass with `tigateway` controller
- **Declarative Configuration**: YAML-based configuration management
- **Extensible**: Support for custom extensions and filters
- **Observability**: Built-in metrics, tracing, and monitoring support
- **Security**: SSO, TLS, and authentication support

## Installation

### Prerequisites

- Kubernetes 1.19+
- Helm 3.0+

### Install CRDs

```bash
# Add the TiGateway Helm repository
helm repo add tigateway https://charts.tigateway.cn
helm repo update

# Install the CRDs
helm install tigateway-crds tigateway/tigateway-crds -n tigateway-system --create-namespace
```

### Verify Installation

```bash
# Check that CRDs are installed
kubectl get crd | grep tigateway.cn

# Expected output:
# tigateways.tigateway.cn
# tigatewaymappings.tigateway.cn
# tigatewayrouteconfigs.tigateway.cn
```

## Configuration

### TiGateway CRD

The main gateway instance configuration:

```yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: my-gateway
  namespace: default
spec:
  count: 2
  resources:
    limits:
      cpu: 500m
      memory: 1Gi
    requests:
      cpu: 200m
      memory: 512Mi
  ingress:
    enabled: true
    namespace: default
    refreshInterval: 30
    cacheEnabled: true
    tlsEnabled: true
  api:
    title: "My API Gateway"
    description: "API Gateway for microservices"
    version: "1.0.0"
    serverUrl: "https://api.example.com"
    cors:
      allowedOrigins:
        - "https://app.example.com"
      allowedMethods:
        - "GET"
        - "POST"
        - "PUT"
        - "DELETE"
  observability:
    metrics:
      prometheus:
        enabled: true
    tracing:
      wavefront:
        enabled: false
```

### TiGatewayRouteConfig CRD

Define routing rules and configurations:

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: api-routes
  namespace: default
spec:
  service:
    name: api-service
    namespace: default
    port: 8080
    predicates:
      - "Path=/api/**"
    filters:
      - "StripPrefix=1"
  routes:
    - title: "User API"
      description: "User management endpoints"
      uri: "lb://user-service"
      predicates:
        - "Path=/api/users/**"
      filters:
        - "StripPrefix=2"
      order: 1
      ssoEnabled: true
      tags:
        - "user"
        - "api"
  ingress:
    autoDiscovery: true
    namespace: default
    refreshInterval: 30
```

### TiGatewayMapping CRD

Map route configurations to gateway instances:

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayMapping
metadata:
  name: api-mapping
  namespace: default
spec:
  gatewayRef:
    name: my-gateway
    namespace: default
  routeConfigRef:
    name: api-routes
    namespace: default
  priority: 100
  enabled: true
```

## Ingress Integration

TiGateway automatically discovers and manages Kubernetes Ingress resources using the `tigateway` IngressClass:

### IngressClass Configuration

The chart automatically creates an IngressClass named `tigateway`:

```yaml
apiVersion: networking.k8s.io/v1
kind: IngressClass
metadata:
  name: tigateway
  annotations:
    ingressclass.kubernetes.io/is-default-class: "false"
    tigateway.cn/description: "TiGateway Ingress Controller"
spec:
  controller: tigateway.cn/ingress-controller
```

### Using TiGateway IngressClass

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: api-ingress
  namespace: default
  annotations:
    tigateway.cn/auto-discover: "true"
    tigateway.cn/rewrite-target: /$2
    tigateway.cn/ssl-redirect: "true"
    tigateway.cn/proxy-body-size: "10m"
spec:
  ingressClassName: tigateway
  tls:
    - hosts:
        - "api.example.com"
      secretName: "api-tls"
  rules:
    - host: api.example.com
      http:
        paths:
          - path: /api(/|$)(.*)
            pathType: Prefix
            backend:
              service:
                name: api-service
                port:
                  number: 8080
```

### TiGateway-specific Annotations

TiGateway supports the following annotations for Ingress resources:

| Annotation | Description | Default |
|------------|-------------|---------|
| `tigateway.cn/auto-discover` | Enable automatic route discovery | `false` |
| `tigateway.cn/rewrite-target` | Path rewrite target | `/` |
| `tigateway.cn/ssl-redirect` | Enable SSL redirect | `false` |
| `tigateway.cn/force-ssl-redirect` | Force SSL redirect | `false` |
| `tigateway.cn/proxy-body-size` | Maximum request body size | `1m` |
| `tigateway.cn/proxy-connect-timeout` | Proxy connect timeout | `60s` |
| `tigateway.cn/proxy-send-timeout` | Proxy send timeout | `60s` |
| `tigateway.cn/proxy-read-timeout` | Proxy read timeout | `60s` |
| `tigateway.cn/rate-limit` | Rate limit requests per window | `100` |
| `tigateway.cn/rate-limit-window` | Rate limit time window | `1m` |

## Advanced Configuration

### Custom Extensions

```yaml
spec:
  extensions:
    custom:
      - "my-custom-filter"
    secretsProviders:
      - name: vault
        vault:
          path: "secret/my-app"
          roleName: "my-role"
          authPath: "kubernetes"
```

### Security Configuration

```yaml
spec:
  sso:
    secret: "sso-secret"
    roles-attribute-name: "roles"
    inactive-session-expiration-in-minutes: 30
  tls:
    - hosts:
        - "api.example.com"
      secretName: "api-tls"
```

### Observability

```yaml
spec:
  observability:
    metrics:
      prometheus:
        enabled: true
        serviceMonitor:
          enabled: true
          labels:
            app: tigateway
    tracing:
      wavefront:
        enabled: true
    wavefront:
      secret: "wavefront-secret"
      source: "tigateway"
      application: "api-gateway"
      service: "gateway"
```

## Uninstallation

```bash
# Remove the CRDs (this will also remove all TiGateway resources)
helm uninstall tigateway-crds -n tigateway-system

# Or keep CRDs but remove the chart
helm uninstall tigateway-crds -n tigateway-system --keep-history
```

## Troubleshooting

### Check CRD Status

```bash
# Verify CRDs are installed
kubectl get crd | grep tigateway.cn

# Check CRD details
kubectl describe crd tigateways.tigateway.cn
```

### Validate Resources

```bash
# Validate a TiGateway resource
kubectl apply --dry-run=client -f my-gateway.yaml

# Check resource status
kubectl get tigateway -n default
kubectl describe tigateway my-gateway -n default
```

### Common Issues

1. **CRD not found**: Ensure the CRDs are installed before creating resources
2. **Validation errors**: Check the resource YAML against the CRD schema
3. **Ingress not discovered**: Verify the namespace and label selectors are correct

## Support

For more information and support:

- Documentation: https://docs.tigateway.cn
- GitHub: https://github.com/tigateway/tigateway
- Issues: https://github.com/tigateway/tigateway/issues
