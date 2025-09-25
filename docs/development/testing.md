# TiGateway 测试指南

## 概述

本文档提供了 TiGateway 项目的完整测试指南，包括单元测试、集成测试、端到端测试、性能测试等。通过系统化的测试策略确保代码质量和系统稳定性。

## 1. 测试策略

### 1.1 测试金字塔

```mermaid
graph TD
    A[端到端测试 E2E] --> B[集成测试 Integration]
    B --> C[单元测试 Unit]
    
    subgraph "测试数量"
        D[少量 E2E 测试] --> E[适量集成测试]
        E --> F[大量单元测试]
    end
    
    subgraph "测试速度"
        G[慢速 E2E 测试] --> H[中速集成测试]
        H --> I[快速单元测试]
    end
```

### 1.2 测试类型说明

| 测试类型 | 目的 | 范围 | 执行频率 | 维护成本 |
|---------|------|------|----------|----------|
| 单元测试 | 验证单个组件功能 | 单个类/方法 | 每次提交 | 低 |
| 集成测试 | 验证组件间交互 | 多个组件 | 每次构建 | 中 |
| 端到端测试 | 验证完整业务流程 | 整个系统 | 每次发布 | 高 |
| 性能测试 | 验证系统性能指标 | 整个系统 | 定期执行 | 中 |

## 2. 单元测试

### 2.1 测试框架选择

```xml
<!-- Maven 依赖 -->
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2.2 测试类结构

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("路由服务测试")
class RouteServiceTest {
    
    @Mock
    private RouteRepository routeRepository;
    
    @Mock
    private RouteValidator routeValidator;
    
    @Mock
    private RouteEventPublisher eventPublisher;
    
    @InjectMocks
    private RouteServiceImpl routeService;
    
    private RouteRequest validRequest;
    private Route expectedRoute;
    
    @BeforeEach
    void setUp() {
        validRequest = RouteRequest.builder()
                .id("test-route")
                .uri("http://example.com")
                .predicates(List.of("Path=/test/**"))
                .build();
        
        expectedRoute = Route.builder()
                .id("test-route")
                .uri("http://example.com")
                .predicates(List.of("Path=/test/**"))
                .status(RouteStatus.ACTIVE)
                .build();
    }
    
    @Nested
    @DisplayName("创建路由测试")
    class CreateRouteTests {
        
        @Test
        @DisplayName("应该成功创建路由当请求有效时")
        void should_create_route_successfully_when_request_is_valid() {
            // Given
            when(routeValidator.validate(any(Route.class))).thenReturn(true);
            when(routeRepository.save(any(Route.class))).thenReturn(expectedRoute);
            
            // When
            Route result = routeService.createRoute(validRequest);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("test-route");
            assertThat(result.getUri()).isEqualTo("http://example.com");
            assertThat(result.getStatus()).isEqualTo(RouteStatus.ACTIVE);
            
            verify(routeValidator).validate(any(Route.class));
            verify(routeRepository).save(any(Route.class));
            verify(eventPublisher).publishRouteCreated(any(Route.class));
        }
        
        @Test
        @DisplayName("应该抛出异常当路由ID已存在时")
        void should_throw_exception_when_route_id_already_exists() {
            // Given
            when(routeRepository.existsById("test-route")).thenReturn(true);
            
            // When & Then
            assertThatThrownBy(() -> routeService.createRoute(validRequest))
                    .isInstanceOf(RouteAlreadyExistsException.class)
                    .hasMessage("Route with id 'test-route' already exists");
            
            verify(routeRepository).existsById("test-route");
            verify(routeRepository, never()).save(any(Route.class));
        }
        
        @Test
        @DisplayName("应该抛出异常当路由验证失败时")
        void should_throw_exception_when_route_validation_fails() {
            // Given
            when(routeValidator.validate(any(Route.class))).thenReturn(false);
            
            // When & Then
            assertThatThrownBy(() -> routeService.createRoute(validRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Route validation failed");
            
            verify(routeValidator).validate(any(Route.class));
            verify(routeRepository, never()).save(any(Route.class));
        }
    }
    
    @Nested
    @DisplayName("查询路由测试")
    class QueryRouteTests {
        
        @Test
        @DisplayName("应该返回路由当ID存在时")
        void should_return_route_when_id_exists() {
            // Given
            when(routeRepository.findById("test-route")).thenReturn(Optional.of(expectedRoute));
            
            // When
            Optional<Route> result = routeService.findById("test-route");
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedRoute);
            
            verify(routeRepository).findById("test-route");
        }
        
        @Test
        @DisplayName("应该返回空当ID不存在时")
        void should_return_empty_when_id_not_exists() {
            // Given
            when(routeRepository.findById("non-existent")).thenReturn(Optional.empty());
            
            // When
            Optional<Route> result = routeService.findById("non-existent");
            
            // Then
            assertThat(result).isEmpty();
            
            verify(routeRepository).findById("non-existent");
        }
    }
}
```

### 2.3 测试数据管理

```java
// 测试数据构建器
public class RouteTestDataBuilder {
    
    public static RouteRequest.RouteRequestBuilder validRouteRequest() {
        return RouteRequest.builder()
                .id("test-route")
                .uri("http://example.com")
                .predicates(List.of("Path=/test/**"))
                .filters(List.of())
                .order(0);
    }
    
    public static Route.RouteBuilder validRoute() {
        return Route.builder()
                .id("test-route")
                .uri("http://example.com")
                .predicates(List.of("Path=/test/**"))
                .filters(List.of())
                .order(0)
                .status(RouteStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }
    
    public static RouteRequest invalidRouteRequest() {
        return RouteRequest.builder()
                .id("")  // 无效ID
                .uri("invalid-uri")  // 无效URI
                .predicates(List.of())  // 空谓词
                .build();
    }
}

// 使用测试数据构建器
@Test
void should_create_route_with_valid_data() {
    // Given
    RouteRequest request = RouteTestDataBuilder.validRouteRequest().build();
    Route expectedRoute = RouteTestDataBuilder.validRoute().build();
    
    when(routeRepository.save(any(Route.class))).thenReturn(expectedRoute);
    
    // When
    Route result = routeService.createRoute(request);
    
    // Then
    assertThat(result).isEqualTo(expectedRoute);
}
```

### 2.4 参数化测试

```java
@ParameterizedTest
@ValueSource(strings = {
    "user-service",
    "order-service", 
    "payment-service"
})
@DisplayName("应该成功创建不同服务的路由")
void should_create_routes_for_different_services(String serviceName) {
    // Given
    RouteRequest request = RouteRequest.builder()
            .id(serviceName + "-route")
            .uri("http://" + serviceName + ":8080")
            .predicates(List.of("Path=/api/" + serviceName + "/**"))
            .build();
    
    when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> {
        Route route = invocation.getArgument(0);
        return route.toBuilder().id(route.getId()).build();
    });
    
    // When
    Route result = routeService.createRoute(request);
    
    // Then
    assertThat(result.getId()).isEqualTo(serviceName + "-route");
    assertThat(result.getUri()).isEqualTo("http://" + serviceName + ":8080");
}

@ParameterizedTest
@CsvSource({
    "user-service, /api/users/**, http://user-service:8080",
    "order-service, /api/orders/**, http://order-service:8080",
    "payment-service, /api/payments/**, http://payment-service:8080"
})
@DisplayName("应该根据CSV数据创建路由")
void should_create_routes_from_csv_data(String serviceName, String path, String uri) {
    // Given
    RouteRequest request = RouteRequest.builder()
            .id(serviceName + "-route")
            .uri(uri)
            .predicates(List.of("Path=" + path))
            .build();
    
    // When & Then
    assertThatCode(() -> routeService.createRoute(request))
            .doesNotThrowAnyException();
}
```

## 3. 集成测试

### 3.1 Spring Boot 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.cloud.gateway.kubernetes.ingress.enabled=true",
    "spring.kubernetes.discovery.enabled=false",
    "logging.level.ti.gateway=DEBUG"
})
@ActiveProfiles("test")
class RouteControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private RouteRepository routeRepository;
    
    @LocalServerPort
    private int port;
    
    @BeforeEach
    void setUp() {
        routeRepository.deleteAll();
    }
    
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
                "/api/v1/routes", 
                request, 
                new ParameterizedTypeReference<ApiResponse<Route>>() {}
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getCode()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData().getId()).isEqualTo("api-test-route");
        
        // 验证数据库中的数据
        Optional<Route> savedRoute = routeRepository.findById("api-test-route");
        assertThat(savedRoute).isPresent();
    }
    
    @Test
    @DisplayName("应该返回400错误当请求无效时")
    void should_return_400_when_request_is_invalid() {
        // Given
        RouteRequest invalidRequest = RouteRequest.builder()
                .id("")  // 无效ID
                .uri("invalid-uri")  // 无效URI
                .build();
        
        // When
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/v1/routes",
                invalidRequest,
                ErrorResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
    }
}
```

### 3.2 数据库集成测试

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RouteRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private RouteRepository routeRepository;
    
    @Test
    @DisplayName("应该根据状态查找路由")
    void should_find_routes_by_status() {
        // Given
        Route activeRoute = Route.builder()
                .id("active-route")
                .uri("http://example.com")
                .status(RouteStatus.ACTIVE)
                .build();
        
        Route inactiveRoute = Route.builder()
                .id("inactive-route")
                .uri("http://example.com")
                .status(RouteStatus.INACTIVE)
                .build();
        
        entityManager.persistAndFlush(activeRoute);
        entityManager.persistAndFlush(inactiveRoute);
        
        // When
        List<Route> activeRoutes = routeRepository.findByStatus(RouteStatus.ACTIVE);
        
        // Then
        assertThat(activeRoutes).hasSize(1);
        assertThat(activeRoutes.get(0).getId()).isEqualTo("active-route");
    }
    
    @Test
    @DisplayName("应该根据URI模式查找路由")
    void should_find_routes_by_uri_pattern() {
        // Given
        Route route1 = Route.builder()
                .id("route1")
                .uri("http://user-service:8080")
                .build();
        
        Route route2 = Route.builder()
                .id("route2")
                .uri("http://order-service:8080")
                .build();
        
        entityManager.persistAndFlush(route1);
        entityManager.persistAndFlush(route2);
        
        // When
        List<Route> userServiceRoutes = routeRepository.findByUriContaining("user-service");
        
        // Then
        assertThat(userServiceRoutes).hasSize(1);
        assertThat(userServiceRoutes.get(0).getId()).isEqualTo("route1");
    }
}
```

### 3.3 WebFlux 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RouteControllerWebFluxTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private RouteService routeService;
    
    @Test
    @DisplayName("应该通过WebFlux成功创建路由")
    void should_create_route_via_webflux() {
        // Given
        RouteRequest request = RouteRequest.builder()
                .id("webflux-test-route")
                .uri("http://example.com")
                .predicates(List.of("Path=/api/test/**"))
                .build();
        
        Route expectedRoute = Route.builder()
                .id("webflux-test-route")
                .uri("http://example.com")
                .predicates(List.of("Path=/api/test/**"))
                .status(RouteStatus.ACTIVE)
                .build();
        
        when(routeService.createRoute(any(RouteRequest.class))).thenReturn(expectedRoute);
        
        // When & Then
        webTestClient.post()
                .uri("/api/v1/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody(ApiResponse.class)
                .value(response -> {
                    assertThat(response.getCode()).isEqualTo("SUCCESS");
                    assertThat(response.getData().getId()).isEqualTo("webflux-test-route");
                });
        
        verify(routeService).createRoute(any(RouteRequest.class));
    }
    
    @Test
    @DisplayName("应该返回路由列表")
    void should_return_route_list() {
        // Given
        List<Route> routes = List.of(
                Route.builder().id("route1").uri("http://service1").build(),
                Route.builder().id("route2").uri("http://service2").build()
        );
        
        when(routeService.getAllRoutes()).thenReturn(routes);
        
        // When & Then
        webTestClient.get()
                .uri("/api/v1/routes")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(response -> {
                    assertThat(response.getCode()).isEqualTo("SUCCESS");
                    assertThat(response.getData()).hasSize(2);
                });
    }
}
```

## 4. 端到端测试

### 4.1 TestContainers 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RouteE2ETest {
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @Container
    static GenericContainer<?> postgres = new GenericContainer<>("postgres:15-alpine")
            .withDatabaseName("tigateway")
            .withUsername("test")
            .withPassword("test")
            .withExposedPorts(5432);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
        registry.add("spring.datasource.url", () -> 
                "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/tigateway");
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");
    }
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("应该完成完整的路由管理流程")
    void should_complete_full_route_management_workflow() {
        // 1. 创建路由
        RouteRequest createRequest = RouteRequest.builder()
                .id("e2e-test-route")
                .uri("http://example.com")
                .predicates(List.of("Path=/api/e2e/**"))
                .build();
        
        ResponseEntity<ApiResponse<Route>> createResponse = restTemplate.postForEntity(
                "/api/v1/routes", createRequest, new ParameterizedTypeReference<ApiResponse<Route>>() {}
        );
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String routeId = createResponse.getBody().getData().getId();
        
        // 2. 查询路由
        ResponseEntity<ApiResponse<Route>> getResponse = restTemplate.getForEntity(
                "/api/v1/routes/" + routeId, new ParameterizedTypeReference<ApiResponse<Route>>() {}
        );
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getData().getId()).isEqualTo(routeId);
        
        // 3. 更新路由
        RouteRequest updateRequest = RouteRequest.builder()
                .id(routeId)
                .uri("http://updated-example.com")
                .predicates(List.of("Path=/api/updated/**"))
                .build();
        
        ResponseEntity<ApiResponse<Route>> updateResponse = restTemplate.exchange(
                "/api/v1/routes/" + routeId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                new ParameterizedTypeReference<ApiResponse<Route>>() {}
        );
        
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().getData().getUri()).isEqualTo("http://updated-example.com");
        
        // 4. 删除路由
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/v1/routes/" + routeId,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // 5. 验证路由已删除
        ResponseEntity<ApiResponse<Route>> getAfterDeleteResponse = restTemplate.getForEntity(
                "/api/v1/routes/" + routeId, new ParameterizedTypeReference<ApiResponse<Route>>() {}
        );
        
        assertThat(getAfterDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```

### 4.2 契约测试

```java
// 使用 Pact 进行契约测试
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "route-service")
class RouteServiceContractTest {
    
    @Pact(consumer = "tigateway-admin")
    public RequestResponsePact createRoutePact(PactDslWithProvider builder) {
        return builder
                .given("route service is available")
                .uponReceiving("a request to create a route")
                .path("/api/v1/routes")
                .method("POST")
                .headers("Content-Type", "application/json")
                .body("""
                    {
                        "id": "test-route",
                        "uri": "http://example.com",
                        "predicates": ["Path=/test/**"]
                    }
                    """)
                .willRespondWith()
                .status(201)
                .headers(Map.of("Content-Type", "application/json"))
                .body("""
                    {
                        "code": "SUCCESS",
                        "message": "Route created successfully",
                        "data": {
                            "id": "test-route",
                            "uri": "http://example.com",
                            "predicates": ["Path=/test/**"],
                            "status": "ACTIVE"
                        }
                    }
                    """)
                .toPact();
    }
    
    @Test
    @PactTestFor(pactMethod = "createRoutePact")
    void testCreateRoute(MockServer mockServer) {
        // Given
        String baseUrl = mockServer.getUrl();
        RouteServiceClient client = new RouteServiceClient(baseUrl);
        
        RouteRequest request = RouteRequest.builder()
                .id("test-route")
                .uri("http://example.com")
                .predicates(List.of("Path=/test/**"))
                .build();
        
        // When
        Route result = client.createRoute(request);
        
        // Then
        assertThat(result.getId()).isEqualTo("test-route");
        assertThat(result.getUri()).isEqualTo("http://example.com");
        assertThat(result.getStatus()).isEqualTo(RouteStatus.ACTIVE);
    }
}
```

## 5. 性能测试

### 5.1 JMeter 性能测试

```xml
<!-- JMeter 测试计划示例 -->
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan testname="TiGateway Performance Test">
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup testname="Route Creation Load Test">
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">60</stringProp>
        <stringProp name="ThreadGroup.duration">300</stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.duration">300</stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
      </ThreadGroup>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### 5.2 Gatling 性能测试

```scala
// Gatling 性能测试脚本
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class RoutePerformanceTest extends Simulation {
  
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
  
  val createRouteScenario = scenario("Create Route Performance Test")
    .exec(
      http("Create Route")
        .post("/api/v1/routes")
        .body(StringBody("""
          {
            "id": "perf-test-route-${randomInt()}",
            "uri": "http://example.com",
            "predicates": ["Path=/api/test/**"]
          }
        """))
        .check(status.is(201))
        .check(jsonPath("$.data.id").saveAs("routeId"))
    )
    .pause(1)
    .exec(
      http("Get Route")
        .get("/api/v1/routes/${routeId}")
        .check(status.is(200))
    )
  
  setUp(
    createRouteScenario.inject(
      rampUsers(100) during (60 seconds),
      constantUsers(100) during (300 seconds)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.max.lt(1000),
     global.responseTime.mean.lt(500),
     global.successfulRequests.percent.gt(99)
   )
}
```

### 5.3 内存和CPU 性能测试

```java
@SpringBootTest
class PerformanceTest {
    
    @Autowired
    private RouteService routeService;
    
    @Test
    @DisplayName("应该在高并发下保持性能")
    void should_maintain_performance_under_high_concurrency() throws InterruptedException {
        // Given
        int threadCount = 100;
        int requestsPerThread = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        
        // When
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long startTime = System.currentTimeMillis();
                        
                        RouteRequest request = RouteRequest.builder()
                                .id("perf-test-" + Thread.currentThread().getId() + "-" + j)
                                .uri("http://example.com")
                                .predicates(List.of("Path=/test/**"))
                                .build();
                        
                        try {
                            routeService.createRoute(request);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                        
                        long responseTime = System.currentTimeMillis() - startTime;
                        responseTimes.add(responseTime);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.MINUTES);
        executor.shutdown();
        
        // Then
        int totalRequests = threadCount * requestsPerThread;
        assertThat(successCount.get()).isGreaterThan(totalRequests * 0.95); // 95% 成功率
        assertThat(errorCount.get()).isLessThan(totalRequests * 0.05); // 5% 错误率
        
        // 性能指标
        double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        
        assertThat(avgResponseTime).isLessThan(100); // 平均响应时间 < 100ms
        assertThat(maxResponseTime).isLessThan(1000); // 最大响应时间 < 1000ms
        
        System.out.printf("Performance Test Results:%n");
        System.out.printf("Total Requests: %d%n", totalRequests);
        System.out.printf("Success Count: %d%n", successCount.get());
        System.out.printf("Error Count: %d%n", errorCount.get());
        System.out.printf("Success Rate: %.2f%%%n", (double) successCount.get() / totalRequests * 100);
        System.out.printf("Average Response Time: %.2f ms%n", avgResponseTime);
        System.out.printf("Max Response Time: %d ms%n", maxResponseTime);
    }
}
```

## 6. 测试配置

### 6.1 测试配置文件

```yaml
# application-test.yml
spring:
  profiles:
    active: test
  
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        format_sql: true
  
  redis:
    host: localhost
    port: 6379
    database: 1
  
  cloud:
    gateway:
      kubernetes:
        ingress:
          enabled: false
      discovery:
        locator:
          enabled: false

# 测试特定配置
test:
  routes:
    default-uri: http://test-service:8080
    default-predicates:
      - Path=/test/**
  
  performance:
    thread-count: 10
    request-count: 1000
    timeout: 30s

# 日志配置
logging:
  level:
    ti.gateway: DEBUG
    org.springframework.web: DEBUG
    org.springframework.test: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

### 6.2 测试工具类

```java
@Component
public class TestDataFactory {
    
    public static RouteRequest createValidRouteRequest() {
        return RouteRequest.builder()
                .id("test-route-" + UUID.randomUUID().toString().substring(0, 8))
                .uri("http://test-service:8080")
                .predicates(List.of("Path=/test/**"))
                .filters(List.of())
                .order(0)
                .build();
    }
    
    public static Route createValidRoute() {
        return Route.builder()
                .id("test-route-" + UUID.randomUUID().toString().substring(0, 8))
                .uri("http://test-service:8080")
                .predicates(List.of("Path=/test/**"))
                .filters(List.of())
                .order(0)
                .status(RouteStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
    
    public static List<Route> createMultipleRoutes(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createValidRoute().toBuilder()
                        .id("test-route-" + i)
                        .build())
                .collect(Collectors.toList());
    }
}

@Component
public class TestAssertions {
    
    public static void assertRouteEquals(Route expected, Route actual) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getUri()).isEqualTo(expected.getUri());
        assertThat(actual.getPredicates()).isEqualTo(expected.getPredicates());
        assertThat(actual.getFilters()).isEqualTo(expected.getFilters());
        assertThat(actual.getOrder()).isEqualTo(expected.getOrder());
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
    }
    
    public static void assertApiResponseSuccess(ApiResponse<?> response) {
        assertThat(response.getCode()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("操作成功");
        assertThat(response.getTimestamp()).isNotNull();
    }
    
    public static void assertApiResponseError(ApiResponse<?> response, String expectedCode) {
        assertThat(response.getCode()).isEqualTo(expectedCode);
        assertThat(response.getMessage()).isNotBlank();
        assertThat(response.getTimestamp()).isNotNull();
    }
}
```

## 7. 测试最佳实践

### 7.1 测试命名规范

```java
// 测试类命名：被测试类名 + Test
public class RouteServiceTest {}

// 测试方法命名：should_期望结果_when_条件
@Test
void should_return_route_when_valid_id_provided() {}

@Test
void should_throw_exception_when_invalid_request() {}

// 使用 @DisplayName 提供更友好的测试名称
@Test
@DisplayName("应该成功创建路由当请求参数有效时")
void should_create_route_successfully_when_request_is_valid() {}
```

### 7.2 测试组织

```java
@ExtendWith(MockitoExtension.class)
class RouteServiceTest {
    
    // 使用 @Nested 组织相关测试
    @Nested
    @DisplayName("创建路由测试")
    class CreateRouteTests {
        
        @Test
        void should_create_route_successfully() {}
        
        @Test
        void should_throw_exception_when_id_exists() {}
    }
    
    @Nested
    @DisplayName("查询路由测试")
    class QueryRouteTests {
        
        @Test
        void should_return_route_when_found() {}
        
        @Test
        void should_return_empty_when_not_found() {}
    }
}
```

### 7.3 测试数据管理

```java
// 使用 @TestInstance 和 @BeforeAll 管理测试数据
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RouteServiceTest {
    
    private List<Route> testRoutes;
    
    @BeforeAll
    void setUpTestData() {
        testRoutes = TestDataFactory.createMultipleRoutes(10);
    }
    
    @Test
    void should_use_test_data() {
        // 使用预创建的测试数据
        assertThat(testRoutes).hasSize(10);
    }
}
```

## 8. 持续集成测试

### 8.1 GitHub Actions 配置

```yaml
# .github/workflows/test.yml
name: Test

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
      
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: tigateway_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports:
          - 5432:5432
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: Run unit tests
      run: mvn test
    
    - name: Run integration tests
      run: mvn verify -P integration-test
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit
    
    - name: Upload coverage reports
      uses: codecov/codecov-action@v3
      with:
        file: target/site/jacoco/jacoco.xml
```

### 8.2 测试覆盖率配置

```xml
<!-- Maven 配置 -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>INSTRUCTION</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

**相关文档**:
- [编码规范](./coding-standards.md)
- [开发环境搭建](./setup.md)
- [调试指南](./debugging.md)
- [自定义组件开发](./custom-components.md)
