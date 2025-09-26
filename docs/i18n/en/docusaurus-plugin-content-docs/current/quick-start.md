# Quick Start

This guide will help you get TiGateway up and running in just a few minutes. We'll cover the basic setup and create your first route.

## Prerequisites

Before you begin, make sure you have the following installed:

- **Kubernetes cluster** (v1.19 or later)
- **kubectl** configured to access your cluster
- **Helm** (v3.0 or later)

## Option 1: Deploy with Helm (Recommended)

### 1. Add the TiGateway Helm Repository

```bash
helm repo add tigateway https://tigateway.github.io/helm-charts
helm repo update
```

### 2. Install TiGateway

```bash
# Create namespace
kubectl create namespace tigateway

# Install TiGateway
helm install tigateway tigateway/tigateway \
  --namespace tigateway \
  --set replicaCount=2
```

### 3. Verify Installation

```bash
# Check pods
kubectl get pods -n tigateway

# Check services
kubectl get svc -n tigateway
```

Expected output:
```
NAME                        READY   STATUS    RESTARTS   AGE
tigateway-7d4b8c9f6-abc123  1/1     Running   0          2m
tigateway-7d4b8c9f6-def456  1/1     Running   0          2m

NAME                TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
tigateway           LoadBalancer   10.96.123.45    <pending>     8080:30080/TCP   2m
tigateway-admin     ClusterIP      10.96.234.56    <none>        8081:30081/TCP   2m
```

## Option 2: Deploy with YAML Manifests

### 1. Clone the Repository

```bash
git clone https://github.com/tigateway/tigateway.git
cd tigateway
```

### 2. Deploy TiGateway

```bash
# Create namespace
kubectl create namespace tigateway

# Deploy TiGateway
kubectl apply -f helm/tigateway/templates/ -n tigateway
```

## Access TiGateway

### Get Gateway URL

```bash
# For LoadBalancer service
kubectl get svc tigateway -n tigateway

# For NodePort service
kubectl get svc tigateway -n tigateway -o jsonpath='{.spec.ports[0].nodePort}'
```

### Access Admin Interface

```bash
# Port forward to access admin interface
kubectl port-forward svc/tigateway-admin 8081:8081 -n tigateway
```

Then open [http://localhost:8081](http://localhost:8081) in your browser.

## Create Your First Route

### 1. Create a Test Service

First, let's create a simple test service:

```yaml
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
---
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
    targetPort: 80
```

Apply the service:
```bash
kubectl apply -f test-service.yaml
```

### 2. Create a Route Configuration

Create a route configuration using TiGateway's CRD:

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRouteConfig
metadata:
  name: test-route-config
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
```

Apply the route configuration:
```bash
kubectl apply -f test-route-config.yaml
```

### 3. Test the Route

```bash
# Get the gateway URL
GATEWAY_URL=$(kubectl get svc tigateway -n tigateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
GATEWAY_URL=${GATEWAY_URL:-localhost:8080}

# Test the route
curl http://$GATEWAY_URL/test/
```

You should see the default nginx welcome page.

## Using the Web UI

### 1. Access the Admin Interface

```bash
kubectl port-forward svc/tigateway-admin 8081:8081 -n tigateway
```

Open [http://localhost:8081](http://localhost:8081) in your browser.

### 2. Create a Route via UI

1. Navigate to **Routes** in the left sidebar
2. Click **Create Route**
3. Fill in the route details:
   - **Route ID**: `ui-test-route`
   - **Target URI**: `lb://test-service`
   - **Path Pattern**: `/ui-test/**`
   - **Filters**: Add `StripPrefix` with parts = 1
4. Click **Save**

### 3. Test the UI-Created Route

```bash
curl http://$GATEWAY_URL/ui-test/
```

## Configuration Management

### View Current Configuration

```bash
# View all routes
kubectl get tigatewayrouteconfigs -n tigateway

# View route details
kubectl describe tigatewayrouteconfig test-route-config -n tigateway
```

### Update Configuration

You can update routes in several ways:

1. **Via kubectl** (for CRD-based routes)
2. **Via Web UI** (for dynamic routes)
3. **Via REST API** (for programmatic access)

## Monitoring and Metrics

### Access Metrics

```bash
# Port forward to access metrics
kubectl port-forward svc/tigateway 8080:8080 -n tigateway

# View metrics
curl http://localhost:8080/actuator/metrics
```

### View Logs

```bash
# View gateway logs
kubectl logs -f deployment/tigateway -n tigateway

# View admin logs
kubectl logs -f deployment/tigateway-admin -n tigateway
```

## Next Steps

Congratulations! You've successfully deployed TiGateway and created your first route. Here's what you can do next:

### 1. Explore Advanced Features
- **[Configuration Guide](./configuration.md)** - Learn about advanced configuration options
- **[Route Management](./routes-and-predicates.md)** - Master route configuration
- **[Filter Configuration](./filters.md)** - Add powerful filters to your routes

### 2. Production Deployment
- **[Production Setup](./deployment/kubernetes.md)** - Production deployment best practices
- **[Security Configuration](./security-best-practices.md)** - Secure your gateway
- **[Monitoring Setup](./monitoring-and-metrics.md)** - Set up comprehensive monitoring

### 3. Integration
- **[Service Discovery](./service-discovery.md)** - Integrate with service discovery
- **[Authentication](./authentication-and-authorization.md)** - Add authentication and authorization
- **[API Documentation](./api/rest-api.md)** - Explore the management APIs

## Troubleshooting

### Common Issues

#### Gateway Not Starting
```bash
# Check pod status
kubectl get pods -n tigateway

# Check logs
kubectl logs deployment/tigateway -n tigateway
```

#### Routes Not Working
```bash
# Check route configuration
kubectl get tigatewayrouteconfigs -n tigateway

# Check gateway logs
kubectl logs deployment/tigateway -n tigateway | grep -i route
```

#### Admin UI Not Accessible
```bash
# Check admin service
kubectl get svc tigateway-admin -n tigateway

# Check admin logs
kubectl logs deployment/tigateway-admin -n tigateway
```

### Getting Help

- **Documentation**: Check our comprehensive documentation
- **GitHub Issues**: Report bugs and request features
- **Community Discussions**: Get help from the community
- **Stack Overflow**: Tag your questions with `tigateway`

---

**Ready to explore more?** Check out our [Configuration Guide](./configuration.md) to learn about advanced TiGateway features.
