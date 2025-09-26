# TiGateway 自定义组件开发指南

## 概述

TiGateway 提供了丰富的扩展点，允许开发者创建自定义的路由谓词、过滤器和其他组件。本文档详细说明了如何开发自定义组件来扩展 TiGateway 的功能。

## 开发环境准备

### 1. 项目依赖

确保你的项目包含必要的依赖：

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2. 开发工具

- **IDE**: IntelliJ IDEA 或 Eclipse
- **Java**: JDK 11 或更高版本
- **Maven**: 3.6 或更高版本
- **Spring Boot**: 2.6.x 或更高版本

## 自定义路由谓词工厂

### 1. 基本结构

自定义路由谓词工厂需要实现 `RoutePredicateFactory` 接口或继承 `AbstractRoutePredicateFactory` 类：

```java
@Component
public class CustomRoutePredicateFactory extends AbstractRoutePredicateFactory<CustomRoutePredicateFactory.Config> {

    public CustomRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            // 实现谓词逻辑
            ServerHttpRequest request = exchange.getRequest();
            return matches(config, request);
        };
    }

    public static class Config {
        // 配置属性
        private String value;
        private boolean enabled = true;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
```

### 2. 实际示例：API 版本谓词

```java
@Component
public class ApiVersionPredicateFactory extends AbstractRoutePredicateFactory<ApiVersionPredicateFactory.Config> {

    public ApiVersionPredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            if (!config.isEnabled()) {
                return true;
            }

            ServerHttpRequest request = exchange.getRequest();
            String apiVersion = request.getHeaders().getFirst("X-API-Version");
            
            if (apiVersion == null) {
                return false;
            }

            return config.getVersions().contains(apiVersion);
        };
    }

    public static class Config {
        private List<String> versions = new ArrayList<>();
        private boolean enabled = true;

        public List<String> getVersions() {
            return versions;
        }

        public void setVersions(List<String> versions) {
            this.versions = versions;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
```

### 3. 使用自定义谓词

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: api-v1-route
          uri: lb://api-service-v1
          predicates:
            - name: ApiVersion
              args:
                versions: ["v1", "1.0"]
                enabled: true
            - Path=/api/**
```

## 自定义过滤器工厂

### 1. 基本结构

自定义过滤器工厂需要实现 `GatewayFilterFactory` 接口或继承 `AbstractGatewayFilterFactory` 类：

```java
@Component
public class CustomGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomGatewayFilterFactory.Config> {

    public CustomGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Pre 过滤器逻辑
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Custom-Header", config.getValue())
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    // Post 过滤器逻辑
                    ServerHttpResponse response = exchange.getResponse();
                    response.getHeaders().add("X-Response-Time", String.valueOf(System.currentTimeMillis()));
                }));
        };
    }

    public static class Config {
        private String value;
        private int timeout = 5000;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
}
```

### 2. 实际示例：请求日志过滤器

```java
@Component
public class RequestLoggingFilterFactory extends AbstractGatewayFilterFactory<RequestLoggingFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilterFactory.class);

    public RequestLoggingFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            if (config.isLogRequest()) {
                logger.info("Request: {} {}", request.getMethod(), request.getURI());
                logger.info("Headers: {}", request.getHeaders());
            }

            long startTime = System.currentTimeMillis();

            return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    if (config.isLogResponse()) {
                        long duration = System.currentTimeMillis() - startTime;
                        ServerHttpResponse response = exchange.getResponse();
                        logger.info("Response: {} - Duration: {}ms", 
                            response.getStatusCode(), duration);
                    }
                })
                .doOnError(throwable -> {
                    if (config.isLogError()) {
                        long duration = System.currentTimeMillis() - startTime;
                        logger.error("Request failed after {}ms: {}", duration, throwable.getMessage());
                    }
                });
        };
    }

    public static class Config {
        private boolean logRequest = true;
        private boolean logResponse = true;
        private boolean logError = true;
        private String logLevel = "INFO";

        // Getters and setters
        public boolean isLogRequest() {
            return logRequest;
        }

        public void setLogRequest(boolean logRequest) {
            this.logRequest = logRequest;
        }

        public boolean isLogResponse() {
            return logResponse;
        }

        public void setLogResponse(boolean logResponse) {
            this.logResponse = logResponse;
        }

        public boolean isLogError() {
            return logError;
        }

        public void setLogError(boolean logError) {
            this.logError = logError;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }
    }
}
```

### 3. 使用自定义过滤器

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: logging-route
          uri: lb://backend-service
          predicates:
            - Path=/api/**
          filters:
            - name: RequestLogging
              args:
                logRequest: true
                logResponse: true
                logError: true
                logLevel: INFO
```

## 自定义全局过滤器

### 1. 基本实现

```java
@Component
@Order(-1) // 设置过滤器顺序
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(CustomGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 添加请求 ID
        String requestId = UUID.randomUUID().toString();
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-Request-ID", requestId)
            .build();

        logger.info("Processing request: {} with ID: {}", request.getURI(), requestId);

        return chain.filter(exchange.mutate().request(modifiedRequest).build())
            .then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                response.getHeaders().add("X-Request-ID", requestId);
                logger.info("Completed request: {} with ID: {}", request.getURI(), requestId);
            }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
```

### 2. 认证过滤器示例

```java
@Component
@Order(-100)
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 跳过不需要认证的路径
        if (shouldSkipAuth(request.getPath().value())) {
            return chain.filter(exchange);
        }

        // 提取认证信息
        String token = extractToken(request);
        if (token == null) {
            return handleUnauthorized(exchange);
        }

        // 验证 Token
        return validateToken(token)
            .flatMap(user -> {
                // 设置用户上下文
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", user.getId())
                    .header("X-User-Roles", String.join(",", user.getRoles()))
                    .build();
                    
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            })
            .onErrorResume(e -> handleAuthenticationError(exchange, e));
    }

    private boolean shouldSkipAuth(String path) {
        return path.startsWith("/actuator") || 
               path.startsWith("/health") || 
               path.startsWith("/public");
    }

    private String extractToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Unauthorized\",\"message\":\"Missing or invalid token\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<User> validateToken(String token) {
        // 实现 Token 验证逻辑
        return Mono.just(new User("user123", Arrays.asList("USER", "ADMIN")));
    }

    private Mono<Void> handleAuthenticationError(ServerWebExchange exchange, Throwable error) {
        logger.error("Authentication error: {}", error.getMessage());
        return handleUnauthorized(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }

    // 用户类
    public static class User {
        private String id;
        private List<String> roles;

        public User(String id, List<String> roles) {
            this.id = id;
            this.roles = roles;
        }

        // Getters and setters
        public String getId() {
            return id;
        }

        public List<String> getRoles() {
            return roles;
        }
    }
}
```

## 自定义负载均衡器

### 1. 实现自定义负载均衡器

```java
@Component
public class CustomLoadBalancerClientFilter extends ReactiveLoadBalancerClientFilter {

    public CustomLoadBalancerClientFilter(ReactiveLoadBalancer.Factory<ServiceInstance> serviceInstanceFactory,
                                        LoadBalancerProperties properties) {
        super(serviceInstanceFactory, properties);
    }

    @Override
    protected ServiceInstance choose(ServerWebExchange exchange) {
        // 实现自定义负载均衡逻辑
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        if (userId != null) {
            // 基于用户 ID 的粘性会话
            return getStickyInstance(userId);
        }
        
        // 默认负载均衡
        return super.choose(exchange);
    }

    private ServiceInstance getStickyInstance(String userId) {
        // 实现粘性会话逻辑
        // 这里可以根据用户 ID 选择特定的服务实例
        return null; // 实际实现中返回具体的实例
    }
}
```

## 自定义配置属性

### 1. 创建配置属性类

```java
@ConfigurationProperties(prefix = "tigateway.custom")
@Data
public class TiGatewayCustomProperties {

    /**
     * 自定义功能是否启用
     */
    private boolean enabled = true;

    /**
     * 自定义超时时间
     */
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * 自定义重试次数
     */
    private int retryCount = 3;

    /**
     * 自定义配置映射
     */
    private Map<String, String> configMap = new HashMap<>();

    /**
     * 自定义服务配置
     */
    private List<ServiceConfig> services = new ArrayList<>();

    @Data
    public static class ServiceConfig {
        private String name;
        private String url;
        private boolean enabled = true;
        private Map<String, String> properties = new HashMap<>();
    }
}
```

### 2. 启用配置属性

```java
@Configuration
@EnableConfigurationProperties(TiGatewayCustomProperties.class)
public class TiGatewayCustomConfiguration {

    @Autowired
    private TiGatewayCustomProperties properties;

    @Bean
    @ConditionalOnProperty(prefix = "tigateway.custom", name = "enabled", havingValue = "true")
    public CustomGlobalFilter customGlobalFilter() {
        return new CustomGlobalFilter(properties);
    }
}
```

## 测试自定义组件

### 1. 单元测试

```java
@ExtendWith(MockitoExtension.class)
class CustomRoutePredicateFactoryTest {

    private CustomRoutePredicateFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CustomRoutePredicateFactory();
    }

    @Test
    void testPredicateMatch() {
        // 准备测试数据
        CustomRoutePredicateFactory.Config config = new CustomRoutePredicateFactory.Config();
        config.setValue("test");
        config.setEnabled(true);

        // 创建模拟的 ServerWebExchange
        ServerWebExchange exchange = createMockExchange();

        // 执行测试
        Predicate<ServerWebExchange> predicate = factory.apply(config);
        boolean result = predicate.test(exchange);

        // 验证结果
        assertTrue(result);
    }

    private ServerWebExchange createMockExchange() {
        // 创建模拟的 ServerWebExchange
        // 实际实现中需要根据具体需求创建
        return null;
    }
}
```

### 2. 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "tigateway.custom.enabled=true",
    "tigateway.custom.timeout=10s"
})
class TiGatewayCustomIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCustomFilter() {
        // 发送请求
        ResponseEntity<String> response = restTemplate.getForEntity("/api/test", String.class);

        // 验证响应
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst("X-Custom-Header"));
    }
}
```

## 部署和配置

### 1. 打包自定义组件

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </exclude>
        </excludes>
    </configuration>
</plugin>
```

### 2. 配置自定义组件

```yaml
tigateway:
  custom:
    enabled: true
    timeout: 30s
    retry-count: 3
    config-map:
      key1: value1
      key2: value2
    services:
      - name: service1
        url: http://service1:8080
        enabled: true
        properties:
          timeout: 5000
          retry: 2
```

## 最佳实践

### 1. 命名规范

- 自定义过滤器工厂类名应以 `GatewayFilterFactory` 结尾
- 自定义谓词工厂类名应以 `RoutePredicateFactory` 结尾
- 使用有意义的名称，避免与内置组件冲突

### 2. 性能考虑

- 避免在过滤器中执行耗时操作
- 合理使用缓存
- 注意内存使用

### 3. 错误处理

- 提供适当的错误处理
- 记录详细的错误日志
- 提供降级机制

### 4. 配置管理

- 使用 `@ConfigurationProperties` 管理配置
- 提供合理的默认值
- 支持动态配置更新

### 5. 测试覆盖

- 编写完整的单元测试
- 提供集成测试
- 测试异常情况

---

**相关文档**:
- [Spring Cloud Gateway 集成](./spring-cloud-gateway-integration.md)
- [开发环境搭建](./setup.md)
- [故障排除](../examples/troubleshooting.md)
- [高级配置示例](../examples/advanced-config.md)
