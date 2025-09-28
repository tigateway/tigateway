# TiGateway Coding Standards

## Overview

This document outlines the coding standards and best practices for the TiGateway project, ensuring code quality, consistency, and maintainability across the development team.

## Java Coding Standards

### Naming Conventions

#### Classes and Interfaces

```java
// Interface naming - end with functional description
public interface RouteDefinitionLocator {}

// Implementation class naming - end with Impl
public class IngressRouteDefinitionLocatorImpl implements RouteDefinitionLocator {}

// Abstract class naming - start with Abstract
public abstract class AbstractRouteDefinitionLocator implements RouteDefinitionLocator {}

// Exception class naming - end with Exception
public class RouteConfigurationException extends RuntimeException {}

// Configuration class naming - end with Configuration
public class GatewayConfiguration {}

// Utility class naming - end with Utils
public class RouteUtils {}
```

#### Methods and Variables

```java
public class RouteService {
    
    // Method naming - use camelCase, descriptive verbs
    public RouteDefinition findRouteById(String routeId) {}
    public void updateRouteConfiguration(RouteConfig config) {}
    public boolean isRouteActive(String routeId) {}
    
    // Variable naming - use camelCase, descriptive nouns
    private final RouteRepository routeRepository;
    private String currentRouteId;
    private boolean isConfigurationValid;
    
    // Constants - use UPPER_SNAKE_CASE
    private static final String DEFAULT_ROUTE_PREFIX = "/api";
    private static final int MAX_RETRY_ATTEMPTS = 3;
}
```

#### Package Naming

```java
// Package structure
package cn.tigateway.core.route;           // Core functionality
package cn.tigateway.core.filter;          // Filter implementations
package cn.tigateway.core.predicate;       // Predicate implementations
package cn.tigateway.config;               // Configuration classes
package cn.tigateway.exception;            // Exception classes
package cn.tigateway.util;                 // Utility classes
package cn.tigateway.integration.kubernetes; // Integration modules
```

### Code Structure

#### Class Organization

```java
/**
 * TiGateway Route Definition Locator Implementation
 * <p>Responsible for dynamically creating Spring Cloud Gateway route definitions from Kubernetes Ingress resources.</p>
 * @author TiGateway Team
 */
@Component
@Slf4j
public class IngressRouteDefinitionLocator implements RouteDefinitionLocator {
    
    // 1. Static fields
    private static final String DEFAULT_NAMESPACE = "default";
    
    // 2. Instance fields
    private final KubernetesClient kubernetesClient;
    private final RouteDefinitionConverter converter;
    
    // 3. Constructor
    public IngressRouteDefinitionLocator(KubernetesClient kubernetesClient,
                                       RouteDefinitionConverter converter) {
        this.kubernetesClient = kubernetesClient;
        this.converter = converter;
    }
    
    // 4. Public methods
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return getIngressResources()
            .map(converter::convert)
            .doOnError(error -> log.error("Error getting route definitions", error));
    }
    
    // 5. Private methods
    private Flux<Ingress> getIngressResources() {
        return Flux.fromIterable(kubernetesClient.network().v1().ingresses().list().getItems());
    }
}
```

#### Method Structure

```java
public class RouteService {
    
    /**
     * Creates a new route definition with validation
     * @param routeConfig the route configuration
     * @return the created route definition
     * @throws RouteConfigurationException if configuration is invalid
     */
    public RouteDefinition createRoute(RouteConfig routeConfig) {
        // 1. Input validation
        validateRouteConfig(routeConfig);
        
        // 2. Business logic
        RouteDefinition routeDefinition = buildRouteDefinition(routeConfig);
        
        // 3. Persistence
        RouteDefinition savedRoute = routeRepository.save(routeDefinition);
        
        // 4. Post-processing
        notifyRouteCreated(savedRoute);
        
        return savedRoute;
    }
    
    private void validateRouteConfig(RouteConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Route configuration cannot be null");
        }
        if (StringUtils.isBlank(config.getId())) {
            throw new RouteConfigurationException("Route ID is required");
        }
    }
}
```

### Comments and Documentation

#### Class Documentation

```java
/**
 * TiGateway Route Definition Locator Implementation
 * 
 * <p>This class implements the Spring Cloud Gateway RouteDefinitionLocator interface
 * to provide dynamic route definitions based on Kubernetes Ingress resources.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Automatic discovery of Ingress resources</li>
 *   <li>Real-time route updates</li>
 *   <li>Support for custom annotations</li>
 * </ul>
 * 
 * @author TiGateway Team
 * @since 1.0.0
 * @see RouteDefinitionLocator
 * @see Ingress
 */
public class IngressRouteDefinitionLocator implements RouteDefinitionLocator {
    // Implementation
}
```

#### Method Documentation

```java
/**
 * Converts a Kubernetes Ingress resource to a Spring Cloud Gateway RouteDefinition
 * 
 * @param ingress the Kubernetes Ingress resource
 * @return the converted RouteDefinition, or null if conversion fails
 * @throws RouteConversionException if the ingress contains invalid configuration
 */
public RouteDefinition convert(Ingress ingress) {
    // Implementation
}
```

#### Inline Comments

```java
public class RouteProcessor {
    
    public void processRoutes(List<RouteDefinition> routes) {
        // Filter out disabled routes
        List<RouteDefinition> activeRoutes = routes.stream()
            .filter(route -> route.getMetadata().containsKey("enabled"))
            .filter(route -> Boolean.parseBoolean(route.getMetadata().get("enabled")))
            .collect(Collectors.toList());
        
        // Process each route asynchronously
        activeRoutes.parallelStream()
            .forEach(this::processRoute);
    }
    
    private void processRoute(RouteDefinition route) {
        try {
            // Validate route configuration
            validateRoute(route);
            
            // Apply route filters
            applyFilters(route);
            
            // Register with gateway
            registerRoute(route);
            
        } catch (Exception e) {
            // Log error but continue processing other routes
            log.error("Failed to process route: {}", route.getId(), e);
        }
    }
}
```

## Spring Boot Standards

### Configuration Classes

```java
@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
@ConditionalOnProperty(name = "tigateway.kubernetes.enabled", havingValue = "true")
@Slf4j
public class KubernetesGatewayConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public KubernetesClient kubernetesClient(GatewayProperties properties) {
        Config config = Config.autoConfigure(properties.getKubernetes().getContext());
        return new DefaultKubernetesClient(config);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public RouteDefinitionLocator routeDefinitionLocator(KubernetesClient kubernetesClient) {
        return new IngressRouteDefinitionLocator(kubernetesClient);
    }
}
```

### Service Classes

```java
@Service
@Transactional
@Slf4j
public class RouteManagementService {
    
    private final RouteRepository routeRepository;
    private final RouteEventPublisher eventPublisher;
    
    public RouteManagementService(RouteRepository routeRepository,
                                RouteEventPublisher eventPublisher) {
        this.routeRepository = routeRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @EventListener
    @Async
    public void handleRouteConfigurationChange(RouteConfigurationChangeEvent event) {
        log.info("Handling route configuration change: {}", event.getRouteId());
        
        try {
            updateRouteConfiguration(event.getRouteId(), event.getConfiguration());
            eventPublisher.publishRouteUpdatedEvent(event.getRouteId());
        } catch (Exception e) {
            log.error("Failed to update route configuration", e);
            eventPublisher.publishRouteUpdateFailedEvent(event.getRouteId(), e);
        }
    }
}
```

### Repository Classes

```java
@Repository
public interface RouteRepository extends JpaRepository<RouteDefinition, String> {
    
    /**
     * Find routes by namespace
     * @param namespace the namespace
     * @return list of routes in the namespace
     */
    List<RouteDefinition> findByNamespace(String namespace);
    
    /**
     * Find active routes
     * @return list of active routes
     */
    @Query("SELECT r FROM RouteDefinition r WHERE r.metadata.enabled = true")
    List<RouteDefinition> findActiveRoutes();
    
    /**
     * Find routes by service name
     * @param serviceName the service name
     * @return list of routes for the service
     */
    List<RouteDefinition> findByServiceName(String serviceName);
}
```

## Configuration Management

### Properties Classes

```java
@ConfigurationProperties(prefix = "tigateway")
@Data
@Validated
public class GatewayProperties {
    
    @NotNull
    private Kubernetes kubernetes = new Kubernetes();
    
    @NotNull
    private Security security = new Security();
    
    @NotNull
    private Monitoring monitoring = new Monitoring();
    
    @Data
    public static class Kubernetes {
        private boolean enabled = true;
        private String namespace = "default";
        private String context;
        private Ingress ingress = new Ingress();
        
        @Data
        public static class Ingress {
            private String className;
            private Map<String, String> annotations = new HashMap<>();
        }
    }
    
    @Data
    public static class Security {
        private boolean enabled = true;
        private String jwtSecret;
        private Duration tokenExpiration = Duration.ofHours(1);
    }
    
    @Data
    public static class Monitoring {
        private boolean enabled = true;
        private String metricsPath = "/actuator/metrics";
        private Duration metricsInterval = Duration.ofSeconds(30);
    }
}
```

### Configuration Validation

```java
@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayConfigurationValidator {
    
    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        GatewayProperties properties = event.getApplicationContext()
            .getBean(GatewayProperties.class);
        
        validateConfiguration(properties);
    }
    
    private void validateConfiguration(GatewayProperties properties) {
        if (properties.getSecurity().isEnabled() && 
            StringUtils.isBlank(properties.getSecurity().getJwtSecret())) {
            throw new IllegalStateException("JWT secret is required when security is enabled");
        }
        
        if (properties.getKubernetes().isEnabled() && 
            StringUtils.isBlank(properties.getKubernetes().getNamespace())) {
            throw new IllegalStateException("Kubernetes namespace is required when Kubernetes is enabled");
        }
    }
}
```

## Exception Handling

### Custom Exceptions

```java
public class TiGatewayException extends RuntimeException {
    
    private final String errorCode;
    private final Map<String, Object> details;
    
    public TiGatewayException(String message) {
        super(message);
        this.errorCode = "TIGATEWAY_ERROR";
        this.details = new HashMap<>();
    }
    
    public TiGatewayException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }
    
    public TiGatewayException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }
    
    public TiGatewayException addDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }
}
```

### Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(TiGatewayException.class)
    public ResponseEntity<ErrorResponse> handleTiGatewayException(TiGatewayException e) {
        log.error("TiGateway error: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode(e.getErrorCode())
            .message(e.getMessage())
            .details(e.getDetails())
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
        log.warn("Validation error: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode("VALIDATION_ERROR")
            .message("Request validation failed")
            .details(Map.of("errors", e.getErrors()))
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
}
```

## RESTful API Design

### Controller Structure

```java
@RestController
@RequestMapping("/api/v1/routes")
@Validated
@Slf4j
public class RouteController {
    
    private final RouteService routeService;
    
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }
    
    @GetMapping
    public ResponseEntity<Page<RouteDefinition>> getRoutes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String namespace) {
        
        Page<RouteDefinition> routes = routeService.getRoutes(page, size, namespace);
        return ResponseEntity.ok(routes);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RouteDefinition> getRoute(@PathVariable String id) {
        RouteDefinition route = routeService.getRouteById(id);
        return ResponseEntity.ok(route);
    }
    
    @PostMapping
    public ResponseEntity<RouteDefinition> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        RouteDefinition route = routeService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(route);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RouteDefinition> updateRoute(
            @PathVariable String id,
            @Valid @RequestBody UpdateRouteRequest request) {
        
        RouteDefinition route = routeService.updateRoute(id, request);
        return ResponseEntity.ok(route);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable String id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Request/Response DTOs

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRouteRequest {
    
    @NotBlank(message = "Route ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "Route ID must contain only alphanumeric characters, hyphens, and underscores")
    private String id;
    
    @NotBlank(message = "URI is required")
    private String uri;
    
    @NotEmpty(message = "At least one predicate is required")
    private List<PredicateDefinition> predicates;
    
    private List<FilterDefinition> filters;
    
    private Map<String, Object> metadata;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    
    private String id;
    private String uri;
    private List<PredicateDefinition> predicates;
    private List<FilterDefinition> filters;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;
}
```

### Pagination

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
```

## Logging Standards

### Log Levels

```java
@Component
@Slf4j
public class RouteProcessor {
    
    public void processRoute(RouteDefinition route) {
        // TRACE - Very detailed information
        log.trace("Processing route: {}", route.getId());
        
        // DEBUG - Detailed information for debugging
        log.debug("Route configuration: {}", route);
        
        // INFO - General information
        log.info("Successfully processed route: {}", route.getId());
        
        // WARN - Warning messages
        log.warn("Route {} has deprecated configuration", route.getId());
        
        // ERROR - Error messages
        log.error("Failed to process route: {}", route.getId(), exception);
    }
}
```

### Structured Logging

```java
@Component
@Slf4j
public class RouteService {
    
    public RouteDefinition createRoute(CreateRouteRequest request) {
        MDC.put("routeId", request.getId());
        MDC.put("operation", "createRoute");
        
        try {
            log.info("Creating route with ID: {}", request.getId());
            
            RouteDefinition route = buildRouteDefinition(request);
            RouteDefinition savedRoute = routeRepository.save(route);
            
            log.info("Successfully created route: {}", savedRoute.getId());
            return savedRoute;
            
        } catch (Exception e) {
            log.error("Failed to create route: {}", request.getId(), e);
            throw new RouteCreationException("Failed to create route", e);
        } finally {
            MDC.clear();
        }
    }
}
```

## Testing Standards

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class RouteServiceTest {
    
    @Mock
    private RouteRepository routeRepository;
    
    @Mock
    private RouteEventPublisher eventPublisher;
    
    @InjectMocks
    private RouteService routeService;
    
    @Test
    @DisplayName("Should create route successfully")
    void shouldCreateRouteSuccessfully() {
        // Given
        CreateRouteRequest request = CreateRouteRequest.builder()
            .id("test-route")
            .uri("lb://test-service")
            .predicates(List.of(new PredicateDefinition("Path", "/api/test/**")))
            .build();
        
        RouteDefinition expectedRoute = RouteDefinition.builder()
            .id("test-route")
            .uri("lb://test-service")
            .build();
        
        when(routeRepository.save(any(RouteDefinition.class)))
            .thenReturn(expectedRoute);
        
        // When
        RouteDefinition result = routeService.createRoute(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-route");
        assertThat(result.getUri()).isEqualTo("lb://test-service");
        
        verify(routeRepository).save(any(RouteDefinition.class));
        verify(eventPublisher).publishRouteCreatedEvent("test-route");
    }
    
    @Test
    @DisplayName("Should throw exception when route ID is null")
    void shouldThrowExceptionWhenRouteIdIsNull() {
        // Given
        CreateRouteRequest request = CreateRouteRequest.builder()
            .id(null)
            .uri("lb://test-service")
            .build();
        
        // When & Then
        assertThatThrownBy(() -> routeService.createRoute(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Route ID cannot be null");
    }
}
```

### Integration Tests

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RouteControllerIntegrationTest {
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private RouteRepository routeRepository;
    
    @Test
    @DisplayName("Should create route via REST API")
    void shouldCreateRouteViaRestApi() {
        // Given
        CreateRouteRequest request = CreateRouteRequest.builder()
            .id("integration-test-route")
            .uri("lb://test-service")
            .predicates(List.of(new PredicateDefinition("Path", "/api/test/**")))
            .build();
        
        // When
        ResponseEntity<RouteResponse> response = restTemplate.postForEntity(
            "/api/v1/routes", request, RouteResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo("integration-test-route");
        
        // Verify persistence
        Optional<RouteDefinition> savedRoute = routeRepository.findById("integration-test-route");
        assertThat(savedRoute).isPresent();
    }
}
```

---

**Related Documentation**:
- [Development Setup](./setup.md)
- [Testing Guide](./testing.md)
- [Custom Components Development](./custom-components.md)
- [Spring Cloud Gateway Integration](./spring-cloud-gateway-integration.md)
