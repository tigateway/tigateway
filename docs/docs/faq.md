# 常见问题

TiGateway 常见问题解答提供了用户在使用过程中可能遇到的各种问题的解决方案，帮助您快速解决疑问和问题。

## 基础问题

### Q1: 什么是 TiGateway？

**A**: TiGateway 是一个基于 Spring Cloud Gateway 的 Kubernetes 原生 API 网关，提供了以下核心功能：

- **路由管理**: 动态路由配置和管理
- **过滤器**: 丰富的过滤器支持，包括认证、限流、熔断等
- **Kubernetes 集成**: 原生支持 Kubernetes 环境，包括 CRD、ConfigMap、Ingress 等
- **管理界面**: 提供 Web UI 和 REST API 进行管理
- **监控告警**: 完整的监控指标和告警机制
- **安全防护**: 多层次的安全防护体系

### Q2: TiGateway 与 Spring Cloud Gateway 有什么区别？

**A**: TiGateway 在 Spring Cloud Gateway 基础上进行了增强：

| 特性 | Spring Cloud Gateway | TiGateway |
|------|---------------------|-----------|
| 基础框架 | Spring Cloud Gateway | Spring Cloud Gateway |
| Kubernetes 支持 | 基础支持 | 原生深度集成 |
| 管理界面 | 无 | Web UI + REST API |
| 配置管理 | 配置文件 | ConfigMap + CRD |
| 监控告警 | 基础指标 | 完整监控体系 |
| 安全防护 | 基础安全 | 多层次安全防护 |
| 扩展性 | 插件机制 | 扩展框架 |

### Q3: 如何开始使用 TiGateway？

**A**: 可以通过以下步骤开始使用：

1. **安装部署**: 参考 [安装部署指南](03-installation.md)
2. **快速开始**: 参考 [快速开始指南](02-quick-start.md)
3. **配置路由**: 参考 [路由和断言](05-routes-and-predicates.md)
4. **添加过滤器**: 参考 [过滤器详解](06-filters.md)
5. **管理配置**: 参考 [配置管理](12-configuration.md)

## 安装部署问题

### Q4: 如何在不同环境中部署 TiGateway？

**A**: TiGateway 支持多种部署方式：

#### Docker 部署
```bash
# 拉取镜像
docker pull tigateway/tigateway:latest

# 运行容器
docker run -d \
  --name tigateway \
  -p 8080:8080 \
  -p 8081:8081 \
  -p 8090:8090 \
  -e SPRING_PROFILES_ACTIVE=docker \
  tigateway/tigateway:latest
```

#### Kubernetes 部署
```bash
# 使用 Helm 部署
helm install tigateway ./helm/tigateway

# 使用 kubectl 部署
kubectl apply -f k8s/
```

#### 本地开发部署
```bash
# 克隆代码
git clone https://github.com/tigateway/tigateway.git

# 编译运行
mvn clean package
java -jar target/tigateway-1.0.0.jar
```

### Q5: 部署时遇到端口冲突怎么办？

**A**: 可以通过以下方式解决端口冲突：

#### 修改配置文件
```yaml
# application.yml
server:
  port: 8080  # 修改为其他端口

tigateway:
  ports:
    gateway: 8080
    admin: 8081
    management: 8090
```

#### 使用环境变量
```bash
export SERVER_PORT=8080
export TIGATEWAY_ADMIN_PORT=8081
export TIGATEWAY_MANAGEMENT_PORT=8090
```

#### Docker 端口映射
```bash
docker run -d \
  --name tigateway \
  -p 9080:8080 \
  -p 9081:8081 \
  -p 9090:8090 \
  tigateway/tigateway:latest
```

### Q6: 如何配置数据库连接？

**A**: 可以通过以下方式配置数据库：

#### 配置文件方式
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tigateway
    username: tigateway
    password: tigateway
    driver-class-name: org.postgresql.Driver
```

#### 环境变量方式
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/tigateway
export DATABASE_USERNAME=tigateway
export DATABASE_PASSWORD=tigateway
```

#### Kubernetes ConfigMap 方式
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-config
data:
  application.yml: |
    spring:
      datasource:
        url: jdbc:postgresql://postgres:5432/tigateway
        username: tigateway
        password: tigateway
```

## 配置问题

### Q7: 如何配置路由？

**A**: 可以通过多种方式配置路由：

#### 配置文件方式
```yaml
# application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - AddRequestHeader=X-Service-Name,user-service
```

#### ConfigMap 方式
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-routes
data:
  routes.yml: |
    routes:
      - id: user-service
        uri: lb://user-service
        predicates:
          - Path=/api/users/**
```

#### REST API 方式
```bash
curl -X POST http://localhost:8081/api/v1/routes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "id": "user-service",
    "uri": "lb://user-service",
    "predicates": [{"name": "Path", "args": {"pattern": "/api/users/**"}}]
  }'
```

### Q8: 如何配置过滤器？

**A**: 可以通过以下方式配置过滤器：

#### 全局过滤器
```yaml
# application.yml
spring:
  cloud:
    gateway:
      default-filters:
        - AddRequestHeader=X-Gateway-Name,TiGateway
        - AddResponseHeader=X-Response-Time,${responseTime}
```

#### 路由过滤器
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          filters:
            - AddRequestHeader=X-Service-Name,user-service
            - CircuitBreaker=user-service-cb
```

#### 自定义过滤器
```java
@Component
public class CustomHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomHeaderGatewayFilterFactory.Config> {
    // 实现自定义过滤器
}
```

### Q9: 如何配置认证和授权？

**A**: 可以通过以下方式配置认证和授权：

#### JWT 认证配置
```yaml
# application.yml
tigateway:
  security:
    authentication:
      jwt:
        enabled: true
        secret: your-jwt-secret
        expiration: 3600000
        issuer: tigateway
        audience: tigateway-api
```

#### OAuth2 认证配置
```yaml
tigateway:
  security:
    authentication:
      oauth2:
        enabled: true
        client-id: your-client-id
        client-secret: your-client-secret
        authorization-uri: https://auth.example.com/oauth/authorize
        token-uri: https://auth.example.com/oauth/token
```

#### RBAC 授权配置
```yaml
tigateway:
  security:
    authorization:
      rbac:
        enabled: true
        default-role: user
        role-hierarchy:
          admin: [operator, viewer]
          operator: [viewer]
          viewer: []
```

## 性能问题

### Q10: 如何优化 TiGateway 性能？

**A**: 可以通过以下方式优化性能：

#### JVM 优化
```bash
# JVM 参数优化
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:G1HeapRegionSize=16m"
```

#### 连接池优化
```yaml
# application.yml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 1000
          max-idle-time: 30s
          max-life-time: 60s
```

#### 缓存优化
```yaml
tigateway:
  cache:
    enabled: true
    default-ttl: 1800s
    default-max-size: 10000
```

### Q11: 如何配置限流？

**A**: 可以通过以下方式配置限流：

#### 全局限流
```yaml
tigateway:
  rate-limiting:
    global:
      enabled: true
      default-rate: 1000
      default-period: 60s
```

#### 路由限流
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
```

#### 自定义限流
```java
@Component
public class CustomRateLimiterGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomRateLimiterGatewayFilterFactory.Config> {
    // 实现自定义限流器
}
```

### Q12: 如何配置熔断？

**A**: 可以通过以下方式配置熔断：

#### 路由熔断
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          filters:
            - name: CircuitBreaker
              args:
                name: user-service-cb
                fallbackUri: forward:/fallback/user-service
```

#### 全局熔断配置
```yaml
tigateway:
  circuit-breaker:
    global:
      enabled: true
      default-failure-threshold: 50
      default-slow-call-threshold: 2000ms
      default-wait-duration: 60s
```

## 监控问题

### Q13: 如何配置监控？

**A**: 可以通过以下方式配置监控：

#### Prometheus 监控
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

#### Grafana 仪表板
```bash
# 导入 Grafana 仪表板
curl -X POST http://localhost:3000/api/dashboards/db \
  -H "Content-Type: application/json" \
  -d @grafana-dashboard.json
```

#### 自定义指标
```java
@Component
public class CustomMetrics {
    private final Counter customCounter;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.customCounter = Counter.builder("tigateway.custom.counter")
            .description("Custom counter")
            .register(meterRegistry);
    }
}
```

### Q14: 如何配置日志？

**A**: 可以通过以下方式配置日志：

#### 日志级别配置
```yaml
# application.yml
logging:
  level:
    root: INFO
    com.tigateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
```

#### 日志输出配置
```yaml
logging:
  file:
    name: logs/tigateway.log
    max-size: 100MB
    max-history: 30
```

#### 结构化日志
```yaml
tigateway:
  logging:
    enabled: true
    format: json
    output:
      console:
        enabled: true
        format: json
      file:
        enabled: true
        format: json
```

### Q15: 如何配置链路追踪？

**A**: 可以通过以下方式配置链路追踪：

#### Zipkin 配置
```yaml
# application.yml
spring:
  zipkin:
    base-url: http://localhost:9411
  sleuth:
    zipkin:
      base-url: http://localhost:9411
    sampler:
      probability: 0.1
```

#### Jaeger 配置
```yaml
tigateway:
  monitoring:
    tracing:
      enabled: true
      type: jaeger
      jaeger:
        endpoint: http://localhost:14268/api/traces
```

## 安全问题

### Q16: 如何配置 HTTPS？

**A**: 可以通过以下方式配置 HTTPS：

#### 证书配置
```yaml
# application.yml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: password
    key-store-type: PKCS12
    key-alias: tigateway
```

#### Kubernetes TLS 配置
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: tigateway-tls
type: kubernetes.io/tls
data:
  tls.crt: <base64-encoded-cert>
  tls.key: <base64-encoded-key>
```

### Q17: 如何配置防火墙？

**A**: 可以通过以下方式配置防火墙：

#### 网络策略
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: tigateway-network-policy
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
    ports:
    - protocol: TCP
      port: 8080
```

#### 防火墙规则
```yaml
tigateway:
  security:
    network:
      firewall:
        enabled: true
        allowed-ports:
          - 8080
          - 8081
          - 8090
        denied-ports:
          - 22
          - 3306
          - 5432
```

### Q18: 如何配置数据加密？

**A**: 可以通过以下方式配置数据加密：

#### 传输加密
```yaml
tigateway:
  security:
    network:
      tls:
        enabled: true
        version: TLSv1.2
        ciphers:
          - ECDHE-RSA-AES128-GCM-SHA256
          - ECDHE-RSA-AES256-GCM-SHA384
```

#### 存储加密
```yaml
tigateway:
  security:
    encryption:
      enabled: true
      algorithm: AES
      key: your-encryption-key
      key-size: 256
```

## 故障排除

### Q19: 如何排查路由不生效的问题？

**A**: 可以通过以下步骤排查：

1. **检查路由配置**
```bash
# 查看路由配置
curl http://localhost:8081/api/v1/routes

# 检查特定路由
curl http://localhost:8081/api/v1/routes/user-service
```

2. **检查断言配置**
```bash
# 验证断言语法
curl -X POST http://localhost:8081/api/v1/routes/validate \
  -H "Content-Type: application/json" \
  -d '{"predicates": [{"name": "Path", "args": {"pattern": "/api/users/**"}}]}'
```

3. **检查日志**
```bash
# 查看网关日志
kubectl logs -f deployment/tigateway

# 查看路由匹配日志
kubectl logs -f deployment/tigateway | grep "route"
```

### Q20: 如何排查认证失败的问题？

**A**: 可以通过以下步骤排查：

1. **检查认证配置**
```bash
# 查看认证配置
curl http://localhost:8081/api/v1/config/security

# 检查 JWT 配置
curl http://localhost:8081/api/v1/config/security/jwt
```

2. **验证令牌**
```bash
# 验证 JWT 令牌
curl -X POST http://localhost:8081/api/v1/auth/validate \
  -H "Content-Type: application/json" \
  -d '{"token": "your-jwt-token"}'
```

3. **检查权限**
```bash
# 检查用户权限
curl http://localhost:8081/api/v1/users/me \
  -H "Authorization: Bearer your-jwt-token"
```

### Q21: 如何排查性能问题？

**A**: 可以通过以下步骤排查：

1. **检查系统指标**
```bash
# 查看系统指标
curl http://localhost:8090/actuator/metrics

# 查看 JVM 指标
curl http://localhost:8090/actuator/metrics/jvm.memory.used
```

2. **检查请求指标**
```bash
# 查看请求指标
curl http://localhost:8090/actuator/metrics/tigateway.requests.total

# 查看响应时间
curl http://localhost:8090/actuator/metrics/tigateway.requests.duration
```

3. **分析性能瓶颈**
```bash
# 生成性能报告
curl http://localhost:8081/api/v1/performance/report

# 查看慢请求
curl http://localhost:8081/api/v1/performance/slow-requests
```

## 扩展开发问题

### Q22: 如何开发自定义过滤器？

**A**: 可以通过以下步骤开发：

1. **创建过滤器类**
```java
@Component
public class CustomHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomHeaderGatewayFilterFactory.Config> {
    
    public CustomHeaderGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return new CustomHeaderGatewayFilter(config);
    }
    
    @Data
    public static class Config {
        private String headerName;
        private String headerValue;
    }
}
```

2. **实现过滤器逻辑**
```java
public class CustomHeaderGatewayFilter implements GatewayFilter {
    
    private final Config config;
    
    public CustomHeaderGatewayFilter(Config config) {
        this.config = config;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder builder = request.mutate();
        builder.header(config.getHeaderName(), config.getHeaderValue());
        return chain.filter(exchange.mutate().request(builder.build()).build());
    }
}
```

3. **配置过滤器**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          filters:
            - name: CustomHeader
              args:
                headerName: X-Custom-Header
                headerValue: CustomValue
```

### Q23: 如何开发自定义断言？

**A**: 可以通过以下步骤开发：

1. **创建断言类**
```java
@Component
public class CustomTimeRangePredicateFactory extends AbstractRoutePredicateFactory<CustomTimeRangePredicateFactory.Config> {
    
    public CustomTimeRangePredicateFactory() {
        super(Config.class);
    }
    
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new CustomTimeRangePredicate(config);
    }
    
    @Data
    public static class Config {
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
```

2. **实现断言逻辑**
```java
public class CustomTimeRangePredicate implements Predicate<ServerWebExchange> {
    
    private final LocalTime startTime;
    private final LocalTime endTime;
    
    public CustomTimeRangePredicate(Config config) {
        this.startTime = config.getStartTime();
        this.endTime = config.getEndTime();
    }
    
    @Override
    public boolean test(ServerWebExchange exchange) {
        LocalTime currentTime = LocalTime.now();
        return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
    }
}
```

3. **配置断言**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - name: CustomTimeRange
              args:
                startTime: 09:00
                endTime: 17:00
```

### Q24: 如何开发自定义扩展？

**A**: 可以通过以下步骤开发：

1. **创建扩展接口**
```java
public interface CustomExtension extends Extension {
    
    String getName();
    
    String getVersion();
    
    void initialize(ExtensionContext context);
    
    void start();
    
    void stop();
}
```

2. **实现扩展类**
```java
@Component
public class CustomMonitoringExtension implements CustomExtension {
    
    private String name;
    private String version;
    private ExtensionContext context;
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getVersion() {
        return version;
    }
    
    @Override
    public void initialize(ExtensionContext context) {
        this.context = context;
        // 初始化逻辑
    }
    
    @Override
    public void start() {
        // 启动逻辑
    }
    
    @Override
    public void stop() {
        // 停止逻辑
    }
}
```

3. **配置扩展**
```yaml
tigateway:
  extensions:
    configs:
      - name: custom-monitoring
        version: 1.0.0
        type: MONITOR
        enabled: true
        class: com.tigateway.extension.CustomMonitoringExtension
```

## 最佳实践

### Q25: 生产环境部署的最佳实践是什么？

**A**: 生产环境部署的最佳实践包括：

1. **资源规划**
```yaml
# 资源配置
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

2. **高可用部署**
```yaml
# 多副本部署
replicas: 3

# 反亲和性
affinity:
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
    - labelSelector:
        matchExpressions:
        - key: app
          operator: In
          values:
          - tigateway
      topologyKey: kubernetes.io/hostname
```

3. **监控告警**
```yaml
# 监控配置
monitoring:
  enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
  health:
    enabled: true
    probes:
      enabled: true
```

### Q26: 如何确保系统安全？

**A**: 可以通过以下方式确保系统安全：

1. **网络安全**
```yaml
# 网络策略
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: tigateway-network-policy
spec:
  podSelector:
    matchLabels:
      app: tigateway
  policyTypes:
  - Ingress
  - Egress
```

2. **认证授权**
```yaml
# 安全配置
tigateway:
  security:
    authentication:
      enabled: true
      type: jwt
    authorization:
      enabled: true
      type: rbac
```

3. **数据加密**
```yaml
# 加密配置
tigateway:
  security:
    encryption:
      enabled: true
      algorithm: AES
      key: your-encryption-key
```

### Q27: 如何优化系统性能？

**A**: 可以通过以下方式优化性能：

1. **JVM 优化**
```bash
# JVM 参数
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:G1HeapRegionSize=16m"
```

2. **连接池优化**
```yaml
# 连接池配置
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 1000
          max-idle-time: 30s
          max-life-time: 60s
```

3. **缓存优化**
```yaml
# 缓存配置
tigateway:
  cache:
    enabled: true
    default-ttl: 1800s
    default-max-size: 10000
```

## 总结

TiGateway 常见问题解答涵盖了用户在使用过程中可能遇到的各种问题：

1. **基础问题**: 产品介绍、特性对比、快速开始
2. **安装部署问题**: 不同环境部署、端口冲突、数据库配置
3. **配置问题**: 路由配置、过滤器配置、认证授权配置
4. **性能问题**: 性能优化、限流配置、熔断配置
5. **监控问题**: 监控配置、日志配置、链路追踪配置
6. **安全问题**: HTTPS 配置、防火墙配置、数据加密配置
7. **故障排除**: 路由问题、认证问题、性能问题排查
8. **扩展开发问题**: 自定义过滤器、断言、扩展开发
9. **最佳实践**: 生产环境部署、系统安全、性能优化

通过常见问题解答，用户可以快速解决使用过程中遇到的问题，提高使用效率和体验。
