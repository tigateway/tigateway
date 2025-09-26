# FAQ

This guide answers frequently asked questions about TiGateway, covering common issues, configuration questions, and troubleshooting scenarios.

## General Questions

### What is TiGateway?

TiGateway is a high-performance, cloud-native API gateway built on Spring Cloud Gateway. It provides:

- **Route Management**: Dynamic routing with predicates and filters
- **Load Balancing**: Multiple load balancing strategies
- **Service Discovery**: Integration with Eureka, Consul, Kubernetes
- **Security**: JWT authentication, OAuth2, RBAC
- **Monitoring**: Comprehensive metrics and tracing
- **Kubernetes Native**: Full Kubernetes integration

### What are the system requirements?

**Minimum Requirements:**
- Java 11 or higher
- 2GB RAM
- 1 CPU core
- 1GB disk space

**Recommended Requirements:**
- Java 17 or higher
- 4GB RAM
- 2 CPU cores
- 10GB disk space

### How does TiGateway compare to other API gateways?

| Feature | TiGateway | Kong | Zuul | Spring Cloud Gateway |
|---------|-----------|------|------|---------------------|
| Performance | High | High | Medium | High |
| Kubernetes Native | Yes | Yes | No | Yes |
| Service Discovery | Yes | Yes | Yes | Yes |
| Load Balancing | Yes | Yes | Yes | Yes |
| Security | JWT, OAuth2 | JWT, OAuth2 | Basic | JWT, OAuth2 |
| Monitoring | Prometheus, Grafana | Yes | Basic | Micrometer |
| Configuration | YAML, CRDs | YAML, DB | YAML | YAML |

## Installation and Setup

### How do I install TiGateway?

**Docker Installation:**
```bash
docker run -d \
  --name tigateway \
  -p 8080:8080 \
  -v /path/to/config:/app/config \
  tigateway/tigateway:latest
```

**Kubernetes Installation:**
```bash
kubectl apply -f https://raw.githubusercontent.com/tigateway/tigateway/main/kubernetes/deployment.yaml
```

**Helm Installation:**
```bash
helm repo add tigateway https://tigateway.github.io/helm-charts
helm install tigateway tigateway/tigateway
```

### How do I configure TiGateway?

TiGateway uses YAML configuration files:

```yaml
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
```

### How do I enable service discovery?

**Eureka:**
```yaml
spring:
  cloud:
    discovery:
      client:
        enabled: true
        service-id: tigateway
```

**Consul:**
```yaml
spring:
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
```

**Kubernetes:**
```yaml
spring:
  cloud:
    kubernetes:
      discovery:
        enabled: true
```

## Configuration

### How do I add a new route?

Add a route to your configuration:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: new-route
          uri: lb://new-service
          predicates:
            - Path=/api/new/**
          filters:
            - StripPrefix=2
```

### How do I configure load balancing?

```yaml
spring:
  cloud:
    loadbalancer:
      configurations:
        round-robin:
          enable: true
        least-connections:
          enable: true
          strategy: LEAST_CONNECTIONS
```

### How do I enable authentication?

**JWT Authentication:**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://auth-service:8080/.well-known/jwks.json
          issuer-uri: http://auth-service:8080
```

**API Key Authentication:**
```yaml
security:
  api-key:
    enabled: true
    header-name: X-API-Key
    keys:
      - key: "your-api-key"
        roles: ["USER"]
```

### How do I configure monitoring?

**Prometheus:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  metrics:
    export:
      prometheus:
        enabled: true
```

**Grafana:**
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
        step: 10s
```

## Troubleshooting

### Why is my route not working?

**Common causes:**
1. **Incorrect path pattern**: Check your Path predicate
2. **Service not available**: Verify backend service is running
3. **Load balancer issues**: Check service discovery
4. **Filter problems**: Review filter configuration

**Debug steps:**
```bash
# Check route configuration
curl http://localhost:8080/actuator/gateway/routes

# Test route
curl -v http://localhost:8080/api/users/123

# Check service discovery
curl http://localhost:8080/actuator/health
```

### Why am I getting 404 errors?

**Possible causes:**
1. **Route not configured**: Add route to configuration
2. **Path mismatch**: Check Path predicate
3. **Service not found**: Verify service discovery
4. **Load balancer issues**: Check service instances

**Solutions:**
```bash
# Check all routes
curl http://localhost:8080/actuator/gateway/routes

# Check service instances
curl http://localhost:8080/actuator/health

# Test with correct path
curl http://localhost:8080/api/users/123
```

### Why is authentication failing?

**Common issues:**
1. **Invalid JWT token**: Check token format and expiration
2. **Wrong JWK endpoint**: Verify jwk-set-uri configuration
3. **Clock skew**: Check system time synchronization
4. **Missing claims**: Verify required claims in token

**Debug steps:**
```bash
# Check JWT configuration
curl http://localhost:8080/actuator/configprops | grep -i jwt

# Test authentication
curl -H "Authorization: Bearer <token>" http://localhost:8080/actuator/health

# Check authentication logs
tail -f logs/tigateway.log | grep -i auth
```

### Why is load balancing not working?

**Possible causes:**
1. **Service instances not found**: Check service discovery
2. **Health check failures**: Verify service health
3. **Load balancer configuration**: Check load balancer settings
4. **Network issues**: Test connectivity

**Solutions:**
```bash
# Check service discovery
curl http://localhost:8080/actuator/health

# Check service instances
curl http://localhost:8761/eureka/apps

# Test load balancing
for i in {1..10}; do curl http://localhost:8080/api/users/123; done
```

## Performance

### How can I improve performance?

**JVM Tuning:**
```bash
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar tigateway.jar
```

**Connection Pool Tuning:**
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 500
          max-idle-time: 30s
          max-life-time: 60s
```

**Caching:**
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000
```

### What are the performance benchmarks?

**Throughput:**
- **Simple routing**: 50,000+ RPS
- **With authentication**: 30,000+ RPS
- **With complex filters**: 20,000+ RPS

**Latency:**
- **P50**: < 10ms
- **P95**: < 50ms
- **P99**: < 100ms

**Resource Usage:**
- **CPU**: 20-40% under normal load
- **Memory**: 1-2GB under normal load
- **Network**: 100-500 Mbps

### How do I monitor performance?

**Metrics:**
```bash
# Check request metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Check JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Check custom metrics
curl http://localhost:8080/actuator/metrics/tigateway.requests
```

**Prometheus:**
```yaml
scrape_configs:
  - job_name: 'tigateway'
    static_configs:
      - targets: ['tigateway:8080']
    metrics_path: '/actuator/prometheus'
```

## Security

### How do I secure TiGateway?

**HTTPS:**
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
```

**Authentication:**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${JWT_JWK_SET_URI}
          issuer-uri: ${JWT_ISSUER_URI}
```

**Authorization:**
```yaml
security:
  rbac:
    enabled: true
    roles:
      ADMIN:
        permissions: ["*"]
      USER:
        permissions: ["user:read", "user:write"]
```

### How do I handle CORS?

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "https://example.com"
            allowed-methods: "GET,POST,PUT,DELETE"
            allowed-headers: "*"
            allow-credentials: true
```

### How do I implement rate limiting?

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rate-limited-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@userKeyResolver}"
```

## Kubernetes

### How do I deploy TiGateway on Kubernetes?

**Basic Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tigateway
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
        image: tigateway/tigateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
```

**Service:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: tigateway
spec:
  selector:
    app: tigateway
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

### How do I use ConfigMaps?

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-config
data:
  application.yml: |
    spring:
      cloud:
        gateway:
          routes:
            - id: user-service-route
              uri: lb://user-service
              predicates:
                - Path=/api/users/**
```

### How do I use Secrets?

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: tigateway-secrets
type: Opaque
data:
  jwt-secret: <base64-encoded-secret>
  api-key: <base64-encoded-key>
```

## Monitoring and Logging

### How do I set up monitoring?

**Prometheus:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  metrics:
    export:
      prometheus:
        enabled: true
```

**Grafana Dashboard:**
```json
{
  "dashboard": {
    "title": "TiGateway Dashboard",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])"
          }
        ]
      }
    ]
  }
}
```

### How do I configure logging?

```yaml
logging:
  level:
    ti.gateway: INFO
    org.springframework.cloud.gateway: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/tigateway.log
    max-size: 100MB
    max-history: 30
```

### How do I set up distributed tracing?

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://zipkin:9411/api/v2/spans
```

## Development

### How do I create custom filters?

```java
@Component
public class CustomFilter implements GatewayFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Custom logic here
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

### How do I create custom predicates?

```java
@Component
public class CustomPredicateFactory extends AbstractRoutePredicateFactory<CustomPredicateFactory.Config> {
    
    public CustomPredicateFactory() {
        super(Config.class);
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            // Custom logic here
            return true;
        };
    }
    
    @Data
    public static class Config {
        private String value;
    }
}
```

### How do I test TiGateway?

**Unit Tests:**
```java
@ExtendWith(MockitoExtension.class)
class CustomFilterTest {
    
    @Test
    void shouldProcessRequest() {
        // Test implementation
    }
}
```

**Integration Tests:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayIntegrationTest {
    
    @Test
    void shouldRouteRequest() {
        // Test implementation
    }
}
```

## Best Practices

### What are the best practices for configuration?

1. **Use environment-specific configs**: Separate dev, staging, prod
2. **Validate configuration**: Use configuration validation
3. **Use external configuration**: Store sensitive data externally
4. **Version control**: Track configuration changes
5. **Documentation**: Document all configuration options

### What are the best practices for security?

1. **Use HTTPS**: Always use HTTPS in production
2. **Strong authentication**: Use strong JWT secrets
3. **Role-based access**: Implement RBAC
4. **Regular updates**: Keep dependencies updated
5. **Audit logging**: Log security events

### What are the best practices for monitoring?

1. **Comprehensive metrics**: Collect all relevant metrics
2. **Alerting**: Set up appropriate alerts
3. **Dashboards**: Create monitoring dashboards
4. **Log aggregation**: Use centralized logging
5. **Performance monitoring**: Monitor performance continuously

## Support

### Where can I get help?

- **Documentation**: [https://tigateway.github.io/tigateway](https://tigateway.github.io/tigateway)
- **GitHub Issues**: [https://github.com/tigateway/tigateway/issues](https://github.com/tigateway/tigateway/issues)
- **Discussions**: [https://github.com/tigateway/tigateway/discussions](https://github.com/tigateway/tigateway/discussions)
- **Community**: [https://github.com/tigateway/tigateway/discussions](https://github.com/tigateway/tigateway/discussions)

### How do I report bugs?

1. **Check existing issues**: Search GitHub issues first
2. **Create new issue**: Use the bug report template
3. **Provide details**: Include version, configuration, logs
4. **Minimal reproduction**: Provide minimal reproduction steps

### How do I contribute?

1. **Fork the repository**: Fork on GitHub
2. **Create feature branch**: Create a new branch
3. **Make changes**: Implement your changes
4. **Add tests**: Add tests for your changes
5. **Submit pull request**: Submit PR with description

## Next Steps

After reading the FAQ:

1. **[Quick Start](./quick-start.md)** - Get started with TiGateway
2. **[Configuration Guide](./configuration.md)** - Advanced configuration
3. **[Troubleshooting Guide](./troubleshooting.md)** - Common issues
4. **[Security Best Practices](./security-best-practices.md)** - Security guidelines

---

**Still have questions?** Check out our [GitHub Discussions](https://github.com/tigateway/tigateway/discussions) for community support.
