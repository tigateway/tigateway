# Architecture

TiGateway is built on a modern, cloud-native architecture that leverages Spring Cloud Gateway and Kubernetes to provide high-performance, scalable API gateway capabilities.

## Architecture Overview

```mermaid
graph TB
    subgraph "Client Layer"
        Client[Client Applications]
        Browser[Web Browser]
        Mobile[Mobile Apps]
    end
    
    subgraph "TiGateway Layer"
        Gateway[TiGateway Core]
        Admin[Admin Interface]
        WebUI[Web UI]
        Operator[TiGateway Operator]
    end
    
    subgraph "Kubernetes Layer"
        K8S[Kubernetes API]
        ConfigMap[ConfigMap Storage]
        Service[Kubernetes Services]
        Ingress[Ingress Controller]
    end
    
    subgraph "Backend Services"
        Service1[User Service]
        Service2[Order Service]
        Service3[Payment Service]
        Service4[Notification Service]
    end
    
    subgraph "Infrastructure"
        Redis[Redis Cache]
        Prometheus[Prometheus]
        Grafana[Grafana]
        ELK[ELK Stack]
    end
    
    Client --> Gateway
    Browser --> WebUI
    WebUI --> Admin
    Admin --> Gateway
    Operator --> Gateway
    Gateway --> K8S
    Gateway --> ConfigMap
    Gateway --> Service
    Gateway --> Ingress
    Service --> Service1
    Service --> Service2
    Service --> Service3
    Service --> Service4
    Gateway --> Redis
    Gateway --> Prometheus
    Prometheus --> Grafana
    Gateway --> ELK
```

## Core Components

### 1. TiGateway Core

The main gateway engine built on Spring Cloud Gateway, providing:

- **Request Routing**: Intelligent routing based on path, header, and query parameters
- **Load Balancing**: Multiple load balancing algorithms (Round Robin, Least Connections, Random)
- **Circuit Breaker**: Built-in circuit breaker pattern for fault tolerance
- **Rate Limiting**: Request rate limiting and throttling capabilities
- **Security**: Authentication, authorization, and security policies

```yaml
# TiGateway Core Configuration
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - AddRequestHeader=X-Service,user-service
            - CircuitBreaker=user-service-cb,forward:/fallback
```

### 2. Admin Interface

RESTful API and Web UI for gateway management:

- **Configuration Management**: Dynamic route and filter configuration
- **Monitoring Dashboard**: Real-time metrics and health status
- **User Management**: Authentication and authorization
- **System Administration**: Logs, metrics, and system settings

```java
@RestController
@RequestMapping("/admin/api")
public class AdminController {
    
    @GetMapping("/routes")
    public Flux<RouteDefinition> getRoutes() {
        return routeService.getAllRoutes();
    }
    
    @PostMapping("/routes")
    public Mono<RouteDefinition> createRoute(@RequestBody RouteDefinition route) {
        return routeService.createRoute(route);
    }
}
```

### 3. TiGateway Operator

Kubernetes operator for managing TiGateway instances:

- **Custom Resource Management**: TiGateway, TiGatewayMapping, TiGatewayRouteConfig
- **Lifecycle Management**: Deployment, scaling, and updates
- **Configuration Synchronization**: Automatic configuration updates
- **Health Monitoring**: Continuous health checks and recovery

```yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: tigateway-instance
  namespace: tigateway
spec:
  replicas: 3
  image: tigateway/tigateway:1.0.0
  resources:
    requests:
      memory: "256Mi"
      cpu: "100m"
    limits:
      memory: "1Gi"
      cpu: "500m"
```

## Data Flow

### Request Processing Flow

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant Admin
    participant Backend
    participant ConfigMap
    
    Client->>Gateway: HTTP Request
    Gateway->>ConfigMap: Load Configuration
    Gateway->>Gateway: Route Matching
    Gateway->>Gateway: Apply Filters
    Gateway->>Backend: Forward Request
    Backend->>Gateway: Response
    Gateway->>Gateway: Apply Response Filters
    Gateway->>Client: HTTP Response
    
    Admin->>Gateway: Configuration Update
    Gateway->>ConfigMap: Update Configuration
    Gateway->>Gateway: Reload Routes
```

### Configuration Management Flow

```mermaid
sequenceDiagram
    participant User
    participant WebUI
    participant Admin
    participant Gateway
    participant ConfigMap
    participant K8S
    
    User->>WebUI: Update Configuration
    WebUI->>Admin: API Request
    Admin->>Admin: Validate Configuration
    Admin->>ConfigMap: Update ConfigMap
    Admin->>K8S: Watch ConfigMap Changes
    K8S->>Gateway: ConfigMap Update Event
    Gateway->>Gateway: Reload Configuration
    Gateway->>Admin: Configuration Applied
    Admin->>WebUI: Success Response
    WebUI->>User: Configuration Updated
```

## Deployment Architecture

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tigateway
  namespace: tigateway
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
        - name: CONFIG_STORAGE_TYPE
          value: "configmap"
        resources:
          requests:
            memory: "256Mi"
            cpu: "100m"
          limits:
            memory: "1Gi"
            cpu: "500m"
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

### Service Configuration

```yaml
apiVersion: v1
kind: Service
metadata:
  name: tigateway
  namespace: tigateway
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
  type: LoadBalancer
```

## Security Architecture

### Authentication and Authorization

```mermaid
graph TB
    subgraph "Authentication Layer"
        JWT[JWT Authentication]
        OAuth2[OAuth2 Authentication]
        Basic[Basic Authentication]
    end
    
    subgraph "Authorization Layer"
        RBAC[Role-Based Access Control]
        ABAC[Attribute-Based Access Control]
        Policy[Policy Engine]
    end
    
    subgraph "Security Filters"
        RateLimit[Rate Limiting]
        CORS[CORS Filter]
        CSRF[CSRF Protection]
        XSS[XSS Protection]
    end
    
    Client --> JWT
    Client --> OAuth2
    Client --> Basic
    JWT --> RBAC
    OAuth2 --> ABAC
    Basic --> Policy
    RBAC --> RateLimit
    ABAC --> CORS
    Policy --> CSRF
    RateLimit --> XSS
```

### Security Configuration

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          gateway:
            client-id: ${OAUTH2_CLIENT_ID}
            client-secret: ${OAUTH2_CLIENT_SECRET}
            scope: read,write
        provider:
          gateway:
            authorization-uri: ${OAUTH2_AUTHORIZATION_URI}
            token-uri: ${OAUTH2_TOKEN_URI}
            user-info-uri: ${OAUTH2_USER_INFO_URI}
```

## Monitoring Architecture

### Metrics Collection

```mermaid
graph TB
    subgraph "TiGateway"
        Gateway[Gateway Core]
        Admin[Admin Interface]
    end
    
    subgraph "Metrics Collection"
        Micrometer[Micrometer]
        Prometheus[Prometheus Metrics]
        Custom[Custom Metrics]
    end
    
    subgraph "Storage & Visualization"
        PrometheusDB[Prometheus DB]
        Grafana[Grafana]
        AlertManager[Alert Manager]
    end
    
    Gateway --> Micrometer
    Admin --> Micrometer
    Micrometer --> Prometheus
    Micrometer --> Custom
    Prometheus --> PrometheusDB
    Custom --> PrometheusDB
    PrometheusDB --> Grafana
    PrometheusDB --> AlertManager
```

### Monitoring Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: tigateway
      environment: ${SPRING_PROFILES_ACTIVE:default}
```

## Scalability Architecture

### Horizontal Scaling

```mermaid
graph TB
    subgraph "Load Balancer"
        LB[External Load Balancer]
    end
    
    subgraph "TiGateway Cluster"
        Gateway1[TiGateway Instance 1]
        Gateway2[TiGateway Instance 2]
        Gateway3[TiGateway Instance 3]
        GatewayN[TiGateway Instance N]
    end
    
    subgraph "Backend Services"
        Service1[Service 1]
        Service2[Service 2]
        Service3[Service 3]
    end
    
    LB --> Gateway1
    LB --> Gateway2
    LB --> Gateway3
    LB --> GatewayN
    Gateway1 --> Service1
    Gateway2 --> Service2
    Gateway3 --> Service3
    GatewayN --> Service1
```

### Auto-scaling Configuration

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: tigateway-hpa
  namespace: tigateway
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
```

## Performance Architecture

### Caching Strategy

```mermaid
graph TB
    subgraph "TiGateway"
        Gateway[Gateway Core]
        Cache[Local Cache]
    end
    
    subgraph "External Cache"
        Redis[Redis Cluster]
        Memcached[Memcached]
    end
    
    subgraph "Backend Services"
        Service1[Service 1]
        Service2[Service 2]
    end
    
    Gateway --> Cache
    Gateway --> Redis
    Gateway --> Memcached
    Cache --> Service1
    Redis --> Service2
    Memcached --> Service1
```

### Performance Configuration

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 1000
        response-timeout: 5000
        pool:
          max-connections: 500
          max-idle-time: 30s
          max-life-time: 60s
      filter:
        request-rate-limiter:
          redis-rate-limiter:
            replenish-rate: 100
            burst-capacity: 200
```

## High Availability Architecture

### Multi-Zone Deployment

```mermaid
graph TB
    subgraph "Zone A"
        GatewayA1[TiGateway A1]
        GatewayA2[TiGateway A2]
        ServiceA[Services A]
    end
    
    subgraph "Zone B"
        GatewayB1[TiGateway B1]
        GatewayB2[TiGateway B2]
        ServiceB[Services B]
    end
    
    subgraph "Zone C"
        GatewayC1[TiGateway C1]
        GatewayC2[TiGateway C2]
        ServiceC[Services C]
    end
    
    subgraph "Global Load Balancer"
        GLB[Global Load Balancer]
    end
    
    GLB --> GatewayA1
    GLB --> GatewayA2
    GLB --> GatewayB1
    GLB --> GatewayB2
    GLB --> GatewayC1
    GLB --> GatewayC2
```

### Disaster Recovery

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

## Integration Architecture

### Service Mesh Integration

```mermaid
graph TB
    subgraph "Service Mesh"
        Istio[Istio Control Plane]
        Envoy[Envoy Sidecar]
    end
    
    subgraph "TiGateway"
        Gateway[TiGateway Core]
    end
    
    subgraph "Applications"
        App1[Application 1]
        App2[Application 2]
    end
    
    Gateway --> Istio
    Gateway --> Envoy
    Envoy --> App1
    Envoy --> App2
```

### API Gateway Integration

```yaml
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: tigateway-gateway
  namespace: tigateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - tigateway.example.com
  - port:
      number: 443
      name: https
      protocol: HTTPS
    tls:
      mode: SIMPLE
      credentialName: tigateway-tls
    hosts:
    - tigateway.example.com
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

### 2. Configuration Management

```yaml
# Environment-specific configuration
spring:
  profiles:
    active: ${ENVIRONMENT:dev}
  config:
    import:
      - configmap:tigateway-config-${ENVIRONMENT}
      - optional:file:./config/override-${ENVIRONMENT}.yml
```

### 3. Monitoring and Alerting

```yaml
# Prometheus alerting rules
groups:
- name: tigateway
  rules:
  - alert: TiGatewayDown
    expr: up{job="tigateway"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "TiGateway instance is down"
      description: "TiGateway instance {{ $labels.instance }} has been down for more than 1 minute."
  
  - alert: HighErrorRate
    expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
    for: 2m
    labels:
      severity: warning
    annotations:
      summary: "High error rate detected"
      description: "Error rate is {{ $value }} errors per second."
```

## Next Steps

After understanding the architecture:

1. **[Installation Guide](./installation.md)** - Deploy TiGateway in your environment
2. **[Configuration Guide](./configuration.md)** - Configure TiGateway for your needs
3. **[Deployment Guide](./deployment/kubernetes.md)** - Production deployment best practices
4. **[Monitoring Setup](./monitoring-and-metrics.md)** - Set up comprehensive monitoring

---

**Ready to deploy?** Check out our [Installation Guide](./installation.md) to get started with TiGateway.
