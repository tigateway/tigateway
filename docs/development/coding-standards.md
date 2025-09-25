# TiGateway 编码规范

## 概述

本文档定义了 TiGateway 项目的编码规范，包括 Java 代码规范、Spring Boot 开发规范、API 设计规范、文档规范等。遵循统一的编码规范有助于提高代码质量、可维护性和团队协作效率。

## 1. Java 编码规范

### 1.1 命名规范

#### 1.1.1 包命名
```java
// 基础包结构
package ti.gateway.{module}.{layer}.{component}

// 示例
package ti.gateway.kubernetes.ingress.controller;
package ti.gateway.admin.service.impl;
package ti.gateway.base.storage.configmap.model;
```

#### 1.1.2 类命名
```java
// 接口命名 - 以功能描述结尾
public interface RouteDefinitionLocator {}
public interface ConfigMapAppInfoRepository {}

// 实现类命名 - 以 Impl 结尾
public class IngressRouteDefinitionLocatorImpl implements RouteDefinitionLocator {}
public class ConfigMapAppInfoRepositoryImpl implements ConfigMapAppInfoRepository {}

// 配置类命名 - 以 Configuration 结尾
public class IngressConfiguration {}
public class AdminServerConfiguration {}

// 属性类命名 - 以 Properties 结尾
public class IngressProperties {}
public class AdminProperties {}

// 异常类命名 - 以 Exception 结尾
public class ConfigurationValidationException {}
public class RouteNotFoundException {}

// 枚举类命名 - 使用名词，单数形式
public enum RouteStatus {
    ACTIVE, INACTIVE, PENDING
}

public enum FilterType {
    PRE, POST, GLOBAL
}
```

#### 1.1.3 方法命名
```java
// 查询方法 - 以 get/find/query 开头
public List<Route> getRoutes() {}
public Route findRouteById(String id) {}
public Page<Route> queryRoutes(RouteQuery query) {}

// 创建方法 - 以 create/add 开头
public Route createRoute(RouteDefinition definition) {}
public void addRoute(Route route) {}

// 更新方法 - 以 update/modify 开头
public Route updateRoute(String id, RouteDefinition definition) {}
public void modifyRoute(Route route) {}

// 删除方法 - 以 delete/remove 开头
public void deleteRoute(String id) {}
public void removeRoute(Route route) {}

// 验证方法 - 以 validate/check 开头
public boolean validateRoute(Route route) {}
public void checkRouteExists(String id) {}

// 转换方法 - 以 transform/convert 开头
public Route transformToRoute(RouteDefinition definition) {}
public RouteDefinition convertToDefinition(Route route) {}
```

#### 1.1.4 变量命名
```java
// 常量 - 全大写，下划线分隔
public static final String DEFAULT_NAMESPACE = "default";
public static final int MAX_RETRY_COUNT = 3;

// 静态变量 - 驼峰命名
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);
private static final String CONFIG_PREFIX = "tigateway";

// 实例变量 - 驼峰命名
private final RouteLocator routeLocator;
private final ConfigMapAppInfoRepository repository;

// 局部变量 - 驼峰命名
String routeId = "user-service-route";
List<Route> activeRoutes = new ArrayList<>();
```

### 1.2 代码结构规范

#### 1.2.1 类结构顺序
```java
public class ExampleClass {
    // 1. 静态常量
    private static final String CONSTANT_VALUE = "value";
    
    // 2. 静态变量
    private static final Logger logger = LoggerFactory.getLogger(ExampleClass.class);
    
    // 3. 实例变量
    private final String field1;
    private final int field2;
    
    // 4. 构造函数
    public ExampleClass(String field1, int field2) {
        this.field1 = field1;
        this.field2 = field2;
    }
    
    // 5. 公共方法
    public String getField1() {
        return field1;
    }
    
    // 6. 受保护方法
    protected void protectedMethod() {}
    
    // 7. 私有方法
    private void privateMethod() {}
    
    // 8. 内部类
    public static class InnerClass {}
}
```

#### 1.2.2 方法结构
```java
public Route createRoute(RouteDefinition definition) {
    // 1. 参数验证
    if (definition == null) {
        throw new IllegalArgumentException("Route definition cannot be null");
    }
    
    // 2. 业务逻辑
    Route route = transformToRoute(definition);
    validateRoute(route);
    
    // 3. 持久化
    Route savedRoute = routeRepository.save(route);
    
    // 4. 日志记录
    logger.info("Created route: {}", savedRoute.getId());
    
    // 5. 返回结果
    return savedRoute;
}
```

### 1.3 注释规范

#### 1.3.1 类注释
```java
/**
 * TiGateway 路由定义定位器实现
 * 
 * <p>负责从 Kubernetes Ingress 资源中动态创建 Spring Cloud Gateway 路由定义。
 * 支持实时监听 Ingress 资源变化，自动更新路由配置。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>监听 Kubernetes Ingress 资源变化</li>
 *   <li>将 Ingress 规则转换为 Gateway 路由定义</li>
 *   <li>支持路由缓存和性能优化</li>
 *   <li>提供路由刷新和更新机制</li>
 * </ul>
 * 
 * @author TiGateway Team
 * @version 1.0.0
 * @since 1.0.0
 * @see RouteDefinitionLocator
 * @see IngressProperties
 */
public class IngressRouteDefinitionLocator implements RouteDefinitionLocator {
}
```

#### 1.3.2 方法注释
```java
/**
 * 根据路由ID查找路由定义
 * 
 * <p>从缓存或数据源中查找指定ID的路由定义。如果路由不存在，
 * 将返回空值而不是抛出异常。</p>
 * 
 * @param routeId 路由ID，不能为null或空字符串
 * @return 路由定义，如果不存在则返回null
 * @throws IllegalArgumentException 如果routeId为null或空字符串
 * @see RouteDefinition
 * @since 1.0.0
 */
public RouteDefinition findRouteById(String routeId) {
    // 实现代码
}
```

#### 1.3.3 行内注释
```java
public void processRequest(ServerWebExchange exchange) {
    // 获取请求路径用于路由匹配
    String path = exchange.getRequest().getPath().value();
    
    // 检查路径是否匹配路由规则
    if (pathMatcher.match(pattern, path)) {
        // 执行路由转发逻辑
        forwardRequest(exchange, targetUri);
    } else {
        // 返回404错误
        return ServerResponse.notFound().build();
    }
}
```

## 2. Spring Boot 开发规范

### 2.1 注解使用规范

#### 2.1.1 组件注解
```java
// 服务层 - 使用 @Service
@Service
@Primary  // 当有多个实现时，标记主要实现
public class UserServiceImpl implements UserService {
}

// 数据访问层 - 使用 @Repository
@Repository
public class UserRepositoryImpl implements UserRepository {
}

// 控制器层 - 使用 @RestController
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {
}

// 配置类 - 使用 @Configuration
@Configuration
@EnableConfigurationProperties(AdminProperties.class)
@ConditionalOnProperty(name = "admin.server.enabled", havingValue = "true")
public class AdminServerConfiguration {
}
```

#### 2.1.2 依赖注入注解
```java
// 推荐使用构造函数注入
@Service
public class RouteService {
    private final RouteRepository routeRepository;
    private final RouteValidator routeValidator;
    
    // 构造函数注入
    public RouteService(RouteRepository routeRepository, 
                       RouteValidator routeValidator) {
        this.routeRepository = routeRepository;
        this.routeValidator = routeValidator;
    }
}

// 避免使用字段注入
@Service
public class BadExample {
    @Autowired  // 不推荐
    private RouteRepository routeRepository;
}
```

### 2.2 配置管理规范

#### 2.2.1 配置属性类
```java
/**
 * Admin 服务器配置属性
 */
@Data
@ConfigurationProperties(prefix = "admin.server")
@Validated
public class AdminServerProperties {
    
    /**
     * 是否启用Admin服务器
     */
    private boolean enabled = true;
    
    /**
     * Admin服务器端口
     */
    @Min(1024)
    @Max(65535)
    private int port = 8081;
    
    /**
     * Admin服务器上下文路径
     */
    @NotBlank
    private String contextPath = "/admin";
    
    /**
     * 服务器名称
     */
    @NotBlank
    private String name = "tigateway-admin";
}
```

#### 2.2.2 配置类
```java
@Configuration
@EnableConfigurationProperties(AdminServerProperties.class)
@ConditionalOnProperty(
    name = "admin.server.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@Slf4j
public class AdminServerConfiguration {
    
    private final AdminServerProperties properties;
    
    public AdminServerConfiguration(AdminServerProperties properties) {
        this.properties = properties;
        log.info("Admin server configuration initialized, enabled: {}, port: {}", 
                properties.isEnabled(), properties.getPort());
    }
    
    @Bean
    @ConditionalOnMissingBean
    public AdminServer adminServer() {
        return new AdminServer(properties);
    }
}
```

### 2.3 异常处理规范

#### 2.3.1 自定义异常
```java
/**
 * 配置验证异常
 */
public class ConfigurationValidationException extends RuntimeException {
    
    private final String field;
    private final Object value;
    
    public ConfigurationValidationException(String message, String field, Object value) {
        super(message);
        this.field = field;
        this.value = value;
    }
    
    public ConfigurationValidationException(String message, String field, Object value, Throwable cause) {
        super(message, cause);
        this.field = field;
        this.value = value;
    }
    
    public String getField() {
        return field;
    }
    
    public Object getValue() {
        return value;
    }
}
```

#### 2.3.2 全局异常处理
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ConfigurationValidationException.class)
    public ResponseEntity<ErrorResponse> handleConfigurationValidation(
            ConfigurationValidationException ex) {
        log.error("Configuration validation failed: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .code("CONFIG_VALIDATION_ERROR")
                .message(ex.getMessage())
                .field(ex.getField())
                .value(ex.getValue())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRouteNotFound(RouteNotFoundException ex) {
        log.warn("Route not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .code("ROUTE_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.notFound().build();
    }
}
```

## 3. API 设计规范

### 3.1 RESTful API 规范

#### 3.1.1 URL 设计
```java
// 资源命名 - 使用名词，复数形式
GET    /api/v1/routes          // 获取所有路由
GET    /api/v1/routes/{id}     // 获取指定路由
POST   /api/v1/routes          // 创建路由
PUT    /api/v1/routes/{id}     // 更新路由
DELETE /api/v1/routes/{id}     // 删除路由

// 子资源
GET    /api/v1/routes/{id}/filters     // 获取路由的过滤器
POST   /api/v1/routes/{id}/filters     // 为路由添加过滤器

// 操作资源
POST   /api/v1/routes/refresh          // 刷新路由
POST   /api/v1/routes/{id}/enable      // 启用路由
POST   /api/v1/routes/{id}/disable     // 禁用路由
```

#### 3.1.2 HTTP 状态码使用
```java
@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {
    
    @GetMapping
    public ResponseEntity<List<Route>> getRoutes() {
        List<Route> routes = routeService.getAllRoutes();
        return ResponseEntity.ok(routes);  // 200 OK
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Route> getRoute(@PathVariable String id) {
        return routeService.findById(id)
                .map(route -> ResponseEntity.ok(route))  // 200 OK
                .orElse(ResponseEntity.notFound().build());  // 404 Not Found
    }
    
    @PostMapping
    public ResponseEntity<Route> createRoute(@Valid @RequestBody RouteRequest request) {
        Route route = routeService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED)  // 201 Created
                .location(URI.create("/api/v1/routes/" + route.getId()))
                .body(route);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable String id, 
                                           @Valid @RequestBody RouteRequest request) {
        Route route = routeService.updateRoute(id, request);
        return ResponseEntity.ok(route);  // 200 OK
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable String id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }
}
```

### 3.2 请求/响应规范

#### 3.2.1 请求对象
```java
/**
 * 路由创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {
    
    /**
     * 路由ID
     */
    @NotBlank(message = "路由ID不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "路由ID只能包含字母、数字、下划线和连字符")
    private String id;
    
    /**
     * 路由描述
     */
    @Size(max = 500, message = "路由描述不能超过500个字符")
    private String description;
    
    /**
     * 目标URI
     */
    @NotBlank(message = "目标URI不能为空")
    @URL(message = "目标URI格式不正确")
    private String uri;
    
    /**
     * 路径匹配规则
     */
    @NotEmpty(message = "路径匹配规则不能为空")
    private List<String> predicates;
    
    /**
     * 过滤器列表
     */
    private List<FilterRequest> filters;
    
    /**
     * 路由顺序
     */
    @Min(value = 0, message = "路由顺序不能小于0")
    private int order = 0;
}
```

#### 3.2.2 响应对象
```java
/**
 * 统一API响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 响应码
     */
    private String code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Instant timestamp;
    
    /**
     * 请求ID
     */
    private String requestId;
    
    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code("SUCCESS")
                .message("操作成功")
                .data(data)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
```

### 3.3 分页规范

#### 3.3.1 分页请求
```java
/**
 * 分页请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    /**
     * 页码，从1开始
     */
    @Min(value = 1, message = "页码不能小于1")
    private int page = 1;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 1000, message = "每页大小不能超过1000")
    private int size = 20;
    
    /**
     * 排序字段
     */
    private String sort;
    
    /**
     * 排序方向
     */
    @Pattern(regexp = "asc|desc", message = "排序方向只能是asc或desc")
    private String direction = "asc";
    
    /**
     * 搜索关键词
     */
    private String keyword;
}
```

#### 3.3.2 分页响应
```java
/**
 * 分页响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    /**
     * 数据列表
     */
    private List<T> content;
    
    /**
     * 总记录数
     */
    private long totalElements;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 当前页码
     */
    private int currentPage;
    
    /**
     * 每页大小
     */
    private int pageSize;
    
    /**
     * 是否有下一页
     */
    private boolean hasNext;
    
    /**
     * 是否有上一页
     */
    private boolean hasPrevious;
    
    /**
     * 是否第一页
     */
    private boolean first;
    
    /**
     * 是否最后一页
     */
    private boolean last;
}
```

## 4. 日志规范

### 4.1 日志级别使用

```java
@Slf4j
public class RouteService {
    
    public Route createRoute(RouteRequest request) {
        // DEBUG - 详细的调试信息
        log.debug("Creating route with request: {}", request);
        
        try {
            // INFO - 重要的业务操作
            log.info("Creating route: {}", request.getId());
            
            Route route = transformToRoute(request);
            validateRoute(route);
            
            Route savedRoute = routeRepository.save(route);
            
            // INFO - 操作成功
            log.info("Successfully created route: {}", savedRoute.getId());
            
            return savedRoute;
            
        } catch (ValidationException e) {
            // WARN - 业务警告，不影响系统运行
            log.warn("Route validation failed for {}: {}", request.getId(), e.getMessage());
            throw e;
            
        } catch (Exception e) {
            // ERROR - 系统错误，需要关注
            log.error("Failed to create route: {}", request.getId(), e);
            throw new RouteCreationException("Failed to create route", e);
        }
    }
}
```

### 4.2 日志格式规范

```java
// 使用结构化日志
log.info("Route created successfully, routeId: {}, targetUri: {}, predicates: {}", 
         routeId, targetUri, predicates);

// 使用MDC添加上下文信息
MDC.put("routeId", routeId);
MDC.put("userId", userId);
log.info("Processing route request");
MDC.clear();

// 记录性能指标
long startTime = System.currentTimeMillis();
// ... 业务逻辑
long duration = System.currentTimeMillis() - startTime;
log.info("Route processing completed, duration: {}ms", duration);
```

## 5. 测试规范

### 5.1 单元测试规范

#### 5.1.1 测试类命名
```java
// 测试类命名：被测试类名 + Test
public class RouteServiceTest {
}

// 集成测试命名：被测试类名 + IntegrationTest
public class RouteServiceIntegrationTest {
}

// 测试方法命名：should_期望结果_when_条件
@Test
public void should_return_route_when_valid_id_provided() {
    // 测试实现
}

@Test
public void should_throw_exception_when_invalid_id_provided() {
    // 测试实现
}
```

#### 5.1.2 测试结构
```java
@ExtendWith(MockitoExtension.class)
class RouteServiceTest {
    
    @Mock
    private RouteRepository routeRepository;
    
    @Mock
    private RouteValidator routeValidator;
    
    @InjectMocks
    private RouteService routeService;
    
    @Test
    @DisplayName("应该成功创建路由当请求有效时")
    void should_create_route_successfully_when_request_is_valid() {
        // Given - 准备测试数据
        RouteRequest request = RouteRequest.builder()
                .id("test-route")
                .uri("http://example.com")
                .predicates(List.of("Path=/test/**"))
                .build();
        
        Route expectedRoute = Route.builder()
                .id("test-route")
                .uri("http://example.com")
                .build();
        
        when(routeValidator.validate(any(Route.class))).thenReturn(true);
        when(routeRepository.save(any(Route.class))).thenReturn(expectedRoute);
        
        // When - 执行测试
        Route result = routeService.createRoute(request);
        
        // Then - 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-route");
        assertThat(result.getUri()).isEqualTo("http://example.com");
        
        verify(routeValidator).validate(any(Route.class));
        verify(routeRepository).save(any(Route.class));
    }
}
```

### 5.2 集成测试规范

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.gateway.kubernetes.ingress.enabled=true",
    "spring.kubernetes.discovery.enabled=false"
})
class RouteServiceIntegrationTest {
    
    @Autowired
    private RouteService routeService;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("应该通过API成功创建路由")
    void should_create_route_via_api() {
        // Given
        RouteRequest request = RouteRequest.builder()
                .id("api-test-route")
                .uri("http://example.com")
                .predicates(List.of("Path=/api/test/**"))
                .build();
        
        // When
        ResponseEntity<ApiResponse<Route>> response = restTemplate.postForEntity(
                "/api/v1/routes", request, new ParameterizedTypeReference<ApiResponse<Route>>() {});
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getCode()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData().getId()).isEqualTo("api-test-route");
    }
}
```

## 6. 文档规范

### 6.1 README 文档规范

```markdown
# 模块名称

## 概述
简要描述模块的功能和用途。

## 功能特性
- 特性1：描述
- 特性2：描述

## 快速开始

### 环境要求
- Java 11+
- Spring Boot 2.7+
- Maven 3.6+

### 安装步骤
1. 克隆代码
2. 编译项目
3. 运行测试

### 配置说明
```yaml
# 配置示例
module:
  property: value
```

## API 文档
[API 文档链接]

## 开发指南
[开发指南链接]

## 贡献指南
[贡献指南链接]

## 许可证
[许可证信息]
```

### 6.2 代码文档规范

```java
/**
 * 路由服务实现类
 * 
 * <p>提供路由的CRUD操作，包括创建、查询、更新和删除路由。
 * 支持路由验证、缓存管理和事件发布。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>路由创建和更新</li>
 *   <li>路由查询和删除</li>
 *   <li>路由验证和缓存</li>
 *   <li>路由事件发布</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Autowired
 * private RouteService routeService;
 * 
 * RouteRequest request = RouteRequest.builder()
 *     .id("user-service")
 *     .uri("http://user-service:8080")
 *     .predicates(List.of("Path=/api/users/**"))
 *     .build();
 * 
 * Route route = routeService.createRoute(request);
 * }</pre>
 * 
 * @author TiGateway Team
 * @version 1.0.0
 * @since 1.0.0
 * @see Route
 * @see RouteRequest
 * @see RouteRepository
 */
@Service
public class RouteServiceImpl implements RouteService {
}
```

## 7. 性能规范

### 7.1 性能优化原则

```java
// 1. 使用缓存减少重复计算
@Service
public class RouteService {
    
    @Cacheable(value = "routes", key = "#id")
    public Route findById(String id) {
        return routeRepository.findById(id);
    }
}

// 2. 使用异步处理提高响应速度
@Service
public class RouteService {
    
    @Async
    public CompletableFuture<Void> refreshRoutesAsync() {
        // 异步刷新路由
        return CompletableFuture.runAsync(this::refreshRoutes);
    }
}

// 3. 使用连接池管理资源
@Configuration
public class HttpClientConfiguration {
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                                .responseTimeout(Duration.ofSeconds(10))
                ))
                .build();
    }
}
```

### 7.2 内存管理规范

```java
// 1. 及时释放资源
public class ResourceManager {
    
    public void processData() {
        try (InputStream inputStream = getInputStream()) {
            // 处理数据
        } // 自动关闭资源
    }
}

// 2. 避免内存泄漏
@Service
public class EventService {
    
    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();
    
    public void addListener(EventListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(EventListener listener) {
        listeners.remove(listener);  // 及时移除监听器
    }
}
```

## 8. 安全规范

### 8.1 输入验证

```java
@RestController
@Validated
public class RouteController {
    
    @PostMapping
    public ResponseEntity<Route> createRoute(
            @Valid @RequestBody RouteRequest request) {
        // @Valid 注解确保请求参数验证
        return ResponseEntity.ok(routeService.createRoute(request));
    }
}

// 自定义验证器
@Component
public class RouteValidator {
    
    public boolean validate(Route route) {
        // 1. 检查路由ID格式
        if (!isValidRouteId(route.getId())) {
            throw new ValidationException("Invalid route ID format");
        }
        
        // 2. 检查URI格式
        if (!isValidUri(route.getUri())) {
            throw new ValidationException("Invalid URI format");
        }
        
        // 3. 检查路径规则
        if (!isValidPathPattern(route.getPredicates())) {
            throw new ValidationException("Invalid path pattern");
        }
        
        return true;
    }
}
```

### 8.2 敏感信息处理

```java
// 1. 不在日志中记录敏感信息
@Slf4j
public class AuthService {
    
    public void authenticate(String token) {
        // 错误示例：记录完整token
        // log.info("Authenticating token: {}", token);
        
        // 正确示例：只记录token的前几位
        log.info("Authenticating token: {}...", token.substring(0, 8));
    }
}

// 2. 使用配置加密
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    
    @JsonIgnore  // 序列化时忽略敏感字段
    private String secretKey;
    
    public String getSecretKey() {
        return secretKey;
    }
}
```

## 9. 版本控制规范

### 9.1 Git 提交规范

```bash
# 提交格式：<type>(<scope>): <subject>
# 
# type: feat, fix, docs, style, refactor, test, chore
# scope: 影响范围，如 route, config, security
# subject: 简短描述

# 示例
git commit -m "feat(route): 添加路由缓存功能"
git commit -m "fix(config): 修复配置验证问题"
git commit -m "docs(api): 更新API文档"
git commit -m "refactor(security): 重构认证逻辑"
```

### 9.2 分支管理规范

```bash
# 主分支
main          # 生产环境分支
develop       # 开发环境分支

# 功能分支
feature/route-cache     # 路由缓存功能
feature/config-validation # 配置验证功能

# 修复分支
hotfix/security-fix     # 安全修复
hotfix/performance-fix  # 性能修复

# 发布分支
release/v1.1.0          # 版本发布
```

## 10. 代码审查规范

### 10.1 审查清单

- [ ] 代码符合命名规范
- [ ] 代码结构清晰，职责单一
- [ ] 异常处理完善
- [ ] 日志记录适当
- [ ] 单元测试覆盖充分
- [ ] 性能考虑合理
- [ ] 安全性检查通过
- [ ] 文档更新完整

### 10.2 审查流程

1. **自检**：开发者完成代码后先进行自检
2. **提交**：提交代码到功能分支
3. **审查**：指定审查者进行代码审查
4. **修改**：根据审查意见修改代码
5. **合并**：审查通过后合并到主分支

---

**相关文档**:
- [开发环境搭建](./setup.md)
- [自定义组件开发](./custom-components.md)
- [测试指南](./testing.md)
- [调试指南](./debugging.md)
