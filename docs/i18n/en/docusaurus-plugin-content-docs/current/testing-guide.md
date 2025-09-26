# Testing Guide

This guide covers testing strategies for TiGateway, including unit testing, integration testing, performance testing, and end-to-end testing approaches.

## Overview

TiGateway testing includes:

- **Unit Testing**: Individual component testing
- **Integration Testing**: Component interaction testing
- **Performance Testing**: Load and stress testing
- **End-to-End Testing**: Complete workflow testing
- **Contract Testing**: API contract validation
- **Security Testing**: Security vulnerability testing

## Unit Testing

### Filter Testing

```java
@ExtendWith(MockitoExtension.class)
class CustomFilterTest {
    
    private CustomFilter customFilter;
    private ServerWebExchange exchange;
    private GatewayFilterChain chain;
    
    @BeforeEach
    void setUp() {
        customFilter = new CustomFilter();
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));
        chain = mock(GatewayFilterChain.class);
    }
    
    @Test
    void shouldAddCustomHeader() {
        // Given
        when(chain.filter(any())).thenReturn(Mono.empty());
        
        // When
        customFilter.filter(exchange, chain).block();
        
        // Then
        verify(chain).filter(any());
        assertThat(exchange.getRequest().getHeaders().getFirst("X-Custom"))
                .isEqualTo("processed");
    }
    
    @Test
    void shouldHandleErrors() {
        // Given
        when(chain.filter(any())).thenReturn(Mono.error(new RuntimeException("Test error")));
        
        // When & Then
        assertThatThrownBy(() -> customFilter.filter(exchange, chain).block())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test error");
    }
}
```

### Predicate Testing

```java
@ExtendWith(MockitoExtension.class)
class CustomPredicateTest {
    
    private CustomPredicateFactory predicateFactory;
    private CustomPredicateFactory.Config config;
    
    @BeforeEach
    void setUp() {
        predicateFactory = new CustomPredicateFactory();
        config = new CustomPredicateFactory.Config();
        config.setValue("test-value");
    }
    
    @Test
    void shouldMatchWhenHeaderMatches() {
        // Given
        ServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Custom-Header", "test-value")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Predicate<ServerWebExchange> predicate = predicateFactory.apply(config);
        boolean result = predicate.test(exchange);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void shouldNotMatchWhenHeaderDoesNotMatch() {
        // Given
        ServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Custom-Header", "other-value")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Predicate<ServerWebExchange> predicate = predicateFactory.apply(config);
        boolean result = predicate.test(exchange);
        
        // Then
        assertThat(result).isFalse();
    }
}
```

### Load Balancer Testing

```java
@ExtendWith(MockitoExtension.class)
class CustomLoadBalancerTest {
    
    private CustomLoadBalancerStrategy loadBalancer;
    private List<ServiceInstance> instances;
    
    @BeforeEach
    void setUp() {
        loadBalancer = new CustomLoadBalancerStrategy();
        instances = Arrays.asList(
                createServiceInstance("service-1", "host1", 8080),
                createServiceInstance("service-2", "host2", 8080),
                createServiceInstance("service-3", "host3", 8080)
        );
    }
    
    @Test
    void shouldChooseInstance() {
        // When
        ServiceInstance chosen = loadBalancer.choose(instances);
        
        // Then
        assertThat(chosen).isNotNull();
        assertThat(instances).contains(chosen);
    }
    
    @Test
    void shouldReturnNullForEmptyList() {
        // When
        ServiceInstance chosen = loadBalancer.choose(Collections.emptyList());
        
        // Then
        assertThat(chosen).isNull();
    }
    
    private ServiceInstance createServiceInstance(String id, String host, int port) {
        return new DefaultServiceInstance(id, "test-service", host, port, false);
    }
}
```

## Integration Testing

### Gateway Integration Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.cloud.gateway.routes[0].id=test-route",
        "spring.cloud.gateway.routes[0].uri=http://localhost:${wiremock.server.port}",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/test/**",
        "spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1"
})
class GatewayIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();
    
    @Test
    void shouldRouteRequestToBackend() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"Hello World\"}")));
        
        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/test", String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"message\": \"Hello World\"}");
    }
    
    @Test
    void shouldHandleBackendError() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));
        
        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/test", String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

### Service Discovery Integration Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.cloud.discovery.client.enabled=true",
        "spring.cloud.discovery.locator.enabled=true"
})
class ServiceDiscoveryIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @MockBean
    private DiscoveryClient discoveryClient;
    
    @Test
    void shouldDiscoverServices() {
        // Given
        ServiceInstance instance = new DefaultServiceInstance(
                "test-instance", "test-service", "localhost", 8080, false);
        when(discoveryClient.getInstances("test-service"))
                .thenReturn(Collections.singletonList(instance));
        
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/test-service/health", String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## Performance Testing

### Load Testing with JMeter

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan testname="TiGateway Load Test">
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup testname="Thread Group">
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <stringProp name="ThreadGroup.duration">300</stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
      </ThreadGroup>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### Gatling Performance Test

```scala
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class TiGatewayPerformanceTest extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .userAgentHeader("Gatling Performance Test")

  val scn = scenario("TiGateway Performance Test")
    .exec(http("Get Users")
      .get("/api/users/123")
      .check(status.is(200))
      .check(jsonPath("$.id").is("123")))
    .pause(1)
    .exec(http("Create User")
      .post("/api/users")
      .header("Content-Type", "application/json")
      .body(StringBody("""{"name": "Test User", "email": "test@example.com"}"""))
      .check(status.is(201))
      .check(jsonPath("$.id").saveAs("userId")))
    .pause(1)
    .exec(http("Update User")
      .put("/api/users/${userId}")
      .header("Content-Type", "application/json")
      .body(StringBody("""{"name": "Updated User", "email": "updated@example.com"}"""))
      .check(status.is(200)))

  setUp(
    scn.inject(
      rampUsers(100) during (10 seconds),
      constantUsers(100) during (300 seconds)
    )
  ).protocols(httpProtocol)
}
```

### Apache Bench Testing

```bash
# Basic load test
ab -n 10000 -c 100 http://localhost:8080/api/users/123

# Load test with keep-alive
ab -n 10000 -c 100 -k http://localhost:8080/api/users/123

# Load test with specific headers
ab -n 10000 -c 100 -H "Authorization: Bearer token" http://localhost:8080/api/users/123

# Load test with POST data
ab -n 1000 -c 10 -p postdata.json -T application/json http://localhost:8080/api/users
```

## End-to-End Testing

### Selenium E2E Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TiGatewayE2ETest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCompleteUserWorkflow() {
        // Create user
        User user = new User("Test User", "test@example.com");
        ResponseEntity<User> createResponse = restTemplate.postForEntity(
                "/api/users", user, User.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        String userId = createResponse.getBody().getId();
        
        // Get user
        ResponseEntity<User> getResponse = restTemplate.getForEntity(
                "/api/users/" + userId, User.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("Test User");
        
        // Update user
        user.setName("Updated User");
        restTemplate.put("/api/users/" + userId, user);
        
        // Verify update
        ResponseEntity<User> updatedResponse = restTemplate.getForEntity(
                "/api/users/" + userId, User.class);
        assertThat(updatedResponse.getBody().getName()).isEqualTo("Updated User");
        
        // Delete user
        restTemplate.delete("/api/users/" + userId);
        
        // Verify deletion
        ResponseEntity<User> deleteResponse = restTemplate.getForEntity(
                "/api/users/" + userId, User.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```

### Contract Testing with Pact

```java
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "user-service")
class UserServiceContractTest {
    
    @Pact(consumer = "tigateway")
    public RequestResponsePact getUserPact(PactDslWithProvider builder) {
        return builder
                .given("user exists")
                .uponReceiving("a request for user")
                .path("/users/123")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                        .stringType("id", "123")
                        .stringType("name", "Test User")
                        .stringType("email", "test@example.com"))
                .toPact();
    }
    
    @Test
    @PactTestFor(pactMethod = "getUserPact")
    void shouldGetUser(MockServer mockServer) {
        // Given
        String url = mockServer.getUrl() + "/users/123";
        
        // When
        ResponseEntity<User> response = new RestTemplate()
                .getForEntity(url, User.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo("123");
        assertThat(response.getBody().getName()).isEqualTo("Test User");
    }
}
```

## Security Testing

### Authentication Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldRequireAuthentication() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/protected", String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
    
    @Test
    void shouldAcceptValidToken() {
        // Given
        String token = generateValidToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/protected", HttpMethod.GET, entity, String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    
    @Test
    void shouldRejectInvalidToken() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid-token");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/protected", HttpMethod.GET, entity, String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
```

### Authorization Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthorizationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldAllowAdminAccess() {
        // Given
        String adminToken = generateAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/admin/users", HttpMethod.GET, entity, String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    
    @Test
    void shouldDenyUserAccessToAdmin() {
        // Given
        String userToken = generateUserToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/admin/users", HttpMethod.GET, entity, String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
```

## Test Configuration

### Test Properties

```yaml
# application-test.yml
spring:
  profiles:
    active: test
  cloud:
    gateway:
      routes:
        - id: test-route
          uri: http://localhost:8081
          predicates:
            - Path=/test/**
          filters:
            - StripPrefix=1
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8082/.well-known/jwks.json

logging:
  level:
    ti.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
```

### Test Configuration Class

```java
@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public DiscoveryClient mockDiscoveryClient() {
        return mock(DiscoveryClient.class);
    }
    
    @Bean
    @Primary
    public LoadBalancerClient mockLoadBalancerClient() {
        return mock(LoadBalancerClient.class);
    }
    
    @Bean
    public WireMockServer wireMockServer() {
        return new WireMockServer(wireMockConfig().dynamicPort());
    }
}
```

## Best Practices

### Testing Strategy

1. **Test Pyramid**: Follow the test pyramid approach
2. **Test Isolation**: Ensure tests are independent
3. **Test Data**: Use consistent test data
4. **Test Environment**: Use dedicated test environments
5. **Test Automation**: Automate all tests

### Performance Testing

1. **Baseline Metrics**: Establish performance baselines
2. **Load Testing**: Test under expected load
3. **Stress Testing**: Test beyond expected capacity
4. **Monitoring**: Monitor during performance tests
5. **Analysis**: Analyze performance test results

### Security Testing

1. **Authentication**: Test all authentication methods
2. **Authorization**: Test role-based access control
3. **Input Validation**: Test input validation
4. **Error Handling**: Test error handling
5. **Vulnerability Scanning**: Regular vulnerability scans

## Troubleshooting

### Common Issues

#### Test Failures

```bash
# Check test logs
tail -f logs/test.log

# Run specific test
mvn test -Dtest=CustomFilterTest

# Run with debug
mvn test -Dtest=CustomFilterTest -Ddebug=true
```

#### Performance Test Issues

```bash
# Check JMeter logs
tail -f jmeter.log

# Check Gatling logs
tail -f gatling.log

# Check system resources
top
htop
```

#### Integration Test Issues

```bash
# Check WireMock status
curl http://localhost:8081/__admin/health

# Check test database
psql -h localhost -U testuser -d testdb

# Check test services
curl http://localhost:8082/health
```

### Debug Commands

```bash
# Run tests with verbose output
mvn test -X

# Run specific test class
mvn test -Dtest=CustomFilterTest

# Run tests with specific profile
mvn test -Dspring.profiles.active=test

# Check test coverage
mvn jacoco:report
```

## Next Steps

After setting up testing:

1. **[Monitoring Setup](../monitoring-and-metrics.md)** - Monitor test results
2. **[Troubleshooting Guide](../troubleshooting.md)** - Common testing issues
3. **[Performance Tuning](../performance-tuning.md)** - Optimize test performance
4. **[Security Best Practices](../security-best-practices.md)** - Secure testing practices

---

**Ready to set up monitoring?** Check out our [Monitoring Setup](../monitoring-and-metrics.md) guide for comprehensive monitoring solutions.
