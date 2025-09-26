# 路由和谓词

TiGateway 基于 Spring Cloud Gateway 构建，继承了其强大的路由和谓词功能，同时提供了 Kubernetes 原生的扩展。

## 路由概述

路由是 TiGateway 的核心概念，它定义了请求如何从客户端转发到后端服务。每个路由包含：

- **ID**: 路由的唯一标识符
- **URI**: 目标服务的地址
- **谓词**: 匹配条件
- **过滤器**: 请求和响应处理逻辑

## 路由配置方式

### 1. ConfigMap 配置

在 Kubernetes 环境中，路由主要通过 ConfigMap 配置：

```yaml
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
          - id: user-service
            uri: lb://user-service
            predicates:
            - Path=/api/users/**
            filters:
            - StripPrefix=2
            - AddRequestHeader=X-Gateway, TiGateway
          - id: order-service
            uri: lb://order-service
            predicates:
            - Path=/api/orders/**
            filters:
            - StripPrefix=2
            - CircuitBreaker=order-service
```

### 2. CRD 资源配置

使用 TiGateway 自定义资源：

```yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: example-gateway
  namespace: tigateway
spec:
  routes:
  - id: api-gateway
    uri: lb://api-service
    predicates:
    - Path=/api/**
    filters:
    - StripPrefix=1
    - AddRequestHeader=X-Gateway, TiGateway
```

### 3. Java DSL 配置

在代码中动态配置路由：

```java
@Configuration
public class RouteConfiguration {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r.path("/api/users/**")
                .filters(f -> f.stripPrefix(2)
                    .addRequestHeader("X-Gateway", "TiGateway"))
                .uri("lb://user-service"))
            .route("order-service", r -> r.path("/api/orders/**")
                .filters(f -> f.stripPrefix(2)
                    .circuitBreaker(c -> c.setName("order-service")))
                .uri("lb://order-service"))
            .build();
    }
}
```

## 路由谓词

谓词用于匹配请求，只有满足谓词条件的请求才会被路由到对应的服务。

### 1. Path 谓词

根据请求路径匹配：

```yaml
predicates:
- Path=/api/users/**
- Path=/api/orders/{segment}
- Path=/api/**,/web/**
```

**示例**:
```yaml
routes:
- id: user-api
  uri: lb://user-service
  predicates:
  - Path=/api/users/**
  filters:
  - StripPrefix=2
```

### 2. Method 谓词

根据 HTTP 方法匹配：

```yaml
predicates:
- Method=GET,POST
- Method=PUT,DELETE
```

**示例**:
```yaml
routes:
- id: read-only
  uri: lb://read-service
  predicates:
  - Method=GET
  - Path=/api/read/**
```

### 3. Host 谓词

根据 Host 头匹配：

```yaml
predicates:
- Host=api.example.com
- Host=*.example.com
- Host=api.example.com,web.example.com
```

**示例**:
```yaml
routes:
- id: api-host
  uri: lb://api-service
  predicates:
  - Host=api.example.com
  - Path=/**
```

### 4. Header 谓词

根据请求头匹配：

```yaml
predicates:
- Header=X-Request-Id, \d+
- Header=Authorization, Bearer.*
```

**示例**:
```yaml
routes:
- id: authenticated
  uri: lb://protected-service
  predicates:
  - Header=Authorization, Bearer.*
  - Path=/api/protected/**
```

### 5. Query 谓词

根据查询参数匹配：

```yaml
predicates:
- Query=version, v1
- Query=debug
- Query=user, \d+
```

**示例**:
```yaml
routes:
- id: versioned-api
  uri: lb://v1-service
  predicates:
  - Query=version, v1
  - Path=/api/**
```

### 6. Cookie 谓词

根据 Cookie 匹配：

```yaml
predicates:
- Cookie=session, .*
- Cookie=user, admin
```

**示例**:
```yaml
routes:
- id: admin-only
  uri: lb://admin-service
  predicates:
  - Cookie=role, admin
  - Path=/admin/**
```

### 7. RemoteAddr 谓词

根据客户端 IP 匹配：

```yaml
predicates:
- RemoteAddr=192.168.1.1/24
- RemoteAddr=10.0.0.0/8
```

**示例**:
```yaml
routes:
- id: internal-only
  uri: lb://internal-service
  predicates:
  - RemoteAddr=192.168.0.0/16
  - Path=/internal/**
```

### 8. Weight 谓词

用于流量分配：

```yaml
routes:
- id: service-v1
  uri: lb://service-v1
  predicates:
  - Weight=group1, 80
  - Path=/api/**
- id: service-v2
  uri: lb://service-v2
  predicates:
  - Weight=group1, 20
  - Path=/api/**
```

## Kubernetes 原生谓词

TiGateway 提供了 Kubernetes 原生的谓词扩展：

### 1. Namespace 谓词

根据 Kubernetes 命名空间匹配：

```yaml
predicates:
- Namespace=production
- Namespace=staging,development
```

### 2. Service 谓词

根据 Kubernetes 服务匹配：

```yaml
predicates:
- Service=user-service
- Service=order-service,payment-service
```

### 3. Label 谓词

根据 Kubernetes 标签匹配：

```yaml
predicates:
- Label=app=frontend
- Label=version=v1,version=v2
```

### 4. Ingress 谓词

根据 Kubernetes Ingress 匹配：

```yaml
predicates:
- Ingress=api-ingress
- Ingress=web-ingress,admin-ingress
```

## 谓词组合

多个谓词可以通过逻辑 AND 组合：

```yaml
routes:
- id: complex-route
  uri: lb://complex-service
  predicates:
  - Path=/api/**
  - Method=GET,POST
  - Header=Authorization, Bearer.*
  - Query=version, v1
  - Host=api.example.com
```

## 动态路由

### 基于服务发现的路由

TiGateway 支持基于 Kubernetes 服务发现的动态路由：

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
          predicates:
          - name: Path
            args:
              pattern: "'/'+serviceId+'/**'"
          filters:
          - name: RewritePath
            args:
              regexp: "'/' + serviceId + '/?(?<remaining>.*)'"
              replacement: "'/${remaining}'"
```

### 基于 Ingress 的路由

自动从 Kubernetes Ingress 创建路由：

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: api-ingress
  namespace: tigateway
  annotations:
    kubernetes.io/ingress.class: "tigateway"
spec:
  rules:
  - host: api.example.com
    http:
      paths:
      - path: /users
        pathType: Prefix
        backend:
          service:
            name: user-service
            port:
              number: 80
      - path: /orders
        pathType: Prefix
        backend:
          service:
            name: order-service
            port:
              number: 80
```

## 路由优先级

路由按照配置顺序和优先级进行匹配：

```yaml
routes:
- id: high-priority
  uri: lb://high-priority-service
  predicates:
  - Path=/api/vip/**
  order: 1
- id: normal-priority
  uri: lb://normal-service
  predicates:
  - Path=/api/**
  order: 2
```

## 路由元数据

可以为路由添加元数据：

```yaml
routes:
- id: metadata-route
  uri: lb://service
  predicates:
  - Path=/api/**
  metadata:
    description: "API Gateway Route"
    version: "v1"
    owner: "team-a"
    tags:
    - "api"
    - "public"
```

## 路由监控

### 路由指标

TiGateway 提供路由级别的监控指标：

```java
@Component
public class RouteMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> routeTimers = new ConcurrentHashMap<>();
    
    public RouteMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordRouteRequest(String routeId, long duration) {
        Timer timer = routeTimers.computeIfAbsent(routeId, 
            id -> Timer.builder("gateway.route.duration")
                .tag("route", id)
                .register(meterRegistry));
        timer.record(duration, TimeUnit.MILLISECONDS);
    }
    
    public void recordRouteError(String routeId, String error) {
        Counter.builder("gateway.route.errors")
            .tag("route", routeId)
            .tag("error", error)
            .register(meterRegistry)
            .increment();
    }
}
```

### 路由健康检查

```java
@Component
public class RouteHealthIndicator implements HealthIndicator {
    
    @Autowired
    private RouteLocator routeLocator;
    
    @Override
    public Health health() {
        try {
            List<Route> routes = routeLocator.getRoutes().collectList().block();
            
            Map<String, Object> details = new HashMap<>();
            details.put("totalRoutes", routes.size());
            details.put("activeRoutes", routes.stream()
                .filter(route -> route.getUri() != null)
                .count());
            
            return Health.up()
                .withDetails(details)
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 路由配置验证

### YAML Schema 验证

```yaml
# schema/route-schema.yaml
type: object
properties:
  spring:
    type: object
    properties:
      cloud:
        type: object
        properties:
          gateway:
            type: object
            properties:
              routes:
                type: array
                items:
                  type: object
                  required: ["id", "uri", "predicates"]
                  properties:
                    id:
                      type: string
                      minLength: 1
                    uri:
                      type: string
                      pattern: "^(https?|lb)://.*"
                    predicates:
                      type: array
                      minItems: 1
                    filters:
                      type: array
                    metadata:
                      type: object
```

### 配置验证器

```java
@Component
public class RouteConfigValidator {
    
    @Autowired
    private YamlSchemaValidator schemaValidator;
    
    public ValidationResult validateRouteConfig(String config) {
        ValidationResult result = new ValidationResult();
        
        try {
            // 验证 YAML 语法
            Yaml yaml = new Yaml();
            Object data = yaml.load(config);
            
            // 验证 Schema
            schemaValidator.validate(data);
            
            // 验证路由配置
            validateRoutes(data);
            
            result.setValid(true);
            result.setMessage("路由配置验证通过");
        } catch (Exception e) {
            result.setValid(false);
            result.setMessage("路由配置验证失败: " + e.getMessage());
        }
        
        return result;
    }
    
    private void validateRoutes(Object data) {
        // 验证路由配置逻辑
        Map<String, Object> config = (Map<String, Object>) data;
        Map<String, Object> spring = (Map<String, Object>) config.get("spring");
        Map<String, Object> cloud = (Map<String, Object>) spring.get("cloud");
        Map<String, Object> gateway = (Map<String, Object>) cloud.get("gateway");
        List<Map<String, Object>> routes = (List<Map<String, Object>>) gateway.get("routes");
        
        Set<String> routeIds = new HashSet<>();
        for (Map<String, Object> route : routes) {
            String id = (String) route.get("id");
            if (routeIds.contains(id)) {
                throw new IllegalArgumentException("重复的路由 ID: " + id);
            }
            routeIds.add(id);
        }
    }
}
```

## 最佳实践

### 1. 路由命名规范

```yaml
# 推荐的命名规范
routes:
- id: user-service-api          # 服务名-功能-类型
- id: order-service-web         # 服务名-功能-类型
- id: payment-service-admin     # 服务名-功能-类型
```

### 2. 谓词顺序优化

```yaml
# 优化谓词顺序，将最具体的谓词放在前面
predicates:
- Path=/api/v1/users/**         # 最具体的路径
- Method=GET,POST               # HTTP 方法
- Header=Authorization, Bearer.* # 认证头
- Host=api.example.com          # 主机名
```

### 3. 路由分组

```yaml
# 按功能分组路由
routes:
# 用户相关路由
- id: user-service-read
  uri: lb://user-service
  predicates:
  - Path=/api/users/**
  - Method=GET
- id: user-service-write
  uri: lb://user-service
  predicates:
  - Path=/api/users/**
  - Method=POST,PUT,DELETE

# 订单相关路由
- id: order-service-read
  uri: lb://order-service
  predicates:
  - Path=/api/orders/**
  - Method=GET
- id: order-service-write
  uri: lb://order-service
  predicates:
  - Path=/api/orders/**
  - Method=POST,PUT,DELETE
```

### 4. 错误处理

```yaml
# 为每个路由配置错误处理
routes:
- id: service-with-fallback
  uri: lb://main-service
  predicates:
  - Path=/api/**
  filters:
  - CircuitBreaker=main-service
  - name: CircuitBreaker
    args:
      name: main-service
      fallbackUri: forward:/fallback
```

## 总结

TiGateway 的路由和谓词功能提供了强大的请求匹配和转发能力：

1. **多种配置方式**: 支持 ConfigMap、CRD 和 Java DSL 配置
2. **丰富的谓词**: 支持路径、方法、头部、查询参数等多种匹配条件
3. **Kubernetes 原生**: 提供命名空间、服务、标签等 Kubernetes 原生谓词
4. **动态路由**: 支持基于服务发现和 Ingress 的动态路由
5. **监控和验证**: 提供路由级别的监控和配置验证
6. **最佳实践**: 遵循命名规范和优化原则

这些功能使得 TiGateway 能够灵活地处理各种路由需求，为微服务架构提供可靠的网关服务。
