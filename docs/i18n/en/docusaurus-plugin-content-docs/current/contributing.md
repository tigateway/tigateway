# Contributing Guide

This guide explains how to contribute to TiGateway, including development setup, coding standards, testing requirements, and submission process.

## Overview

We welcome contributions to TiGateway! This guide covers:

- **Getting Started**: Development environment setup
- **Coding Standards**: Code style and conventions
- **Testing**: Testing requirements and guidelines
- **Documentation**: Documentation standards
- **Submitting Changes**: Pull request process
- **Community Guidelines**: Code of conduct

## Getting Started

### Prerequisites

- **Java 17+**: Required for development
- **Maven 3.6+** or **Gradle 7.0+**: Build tool
- **Git**: Version control
- **Docker**: For containerized development
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code

### Development Environment Setup

#### 1. Fork and Clone

```bash
# Fork the repository on GitHub, then clone your fork
git clone https://github.com/YOUR_USERNAME/tigateway.git
cd tigateway

# Add upstream remote
git remote add upstream https://github.com/tigateway/tigateway.git
```

#### 2. Build the Project

```bash
# Using Maven
./mvnw clean install

# Using Gradle
./gradlew build
```

#### 3. Run Tests

```bash
# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=CustomFilterTest

# Run with coverage
./mvnw test jacoco:report
```

#### 4. Start Development Server

```bash
# Run the application
./mvnw spring-boot:run

# Or with specific profile
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### IDE Configuration

#### IntelliJ IDEA

1. **Import Project**: Open the project in IntelliJ IDEA
2. **Configure Code Style**: Import code style from `.idea/codeStyles/`
3. **Install Plugins**: Install recommended plugins
4. **Configure Run Configuration**: Set up Spring Boot run configuration

#### Eclipse

1. **Import Project**: Import as Maven project
2. **Configure Code Style**: Import code style from `.eclipse/`
3. **Install Plugins**: Install Spring Tools Suite
4. **Configure Run Configuration**: Set up Spring Boot run configuration

#### VS Code

1. **Install Extensions**: Java Extension Pack, Spring Boot Extension Pack
2. **Configure Settings**: Use workspace settings
3. **Configure Launch**: Set up launch configuration

## Coding Standards

### Java Code Style

#### Naming Conventions

```java
// Classes: PascalCase
public class CustomFilterFactory {
    // Constants: UPPER_SNAKE_CASE
    private static final String DEFAULT_VALUE = "default";
    
    // Fields: camelCase
    private String filterName;
    
    // Methods: camelCase
    public void processRequest() {
        // Local variables: camelCase
        String requestId = generateRequestId();
    }
}
```

#### Code Formatting

```java
// Use 4 spaces for indentation
public class Example {
    
    // Method spacing
    public void method1() {
        // Implementation
    }
    
    public void method2() {
        // Implementation
    }
}
```

#### Documentation

```java
/**
 * Custom filter factory for processing requests.
 * 
 * @param <T> The configuration type
 * @author Your Name
 * @since 2.0.0
 */
public class CustomFilterFactory<T> extends AbstractGatewayFilterFactory<T> {
    
    /**
     * Creates a new filter instance.
     * 
     * @param config the filter configuration
     * @return the configured filter
     */
    @Override
    public GatewayFilter apply(T config) {
        return (exchange, chain) -> {
            // Implementation
            return chain.filter(exchange);
        };
    }
}
```

### YAML Configuration Style

```yaml
# Use 2 spaces for indentation
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
```

### Markdown Documentation Style

```markdown
# Title

Brief description of the section.

## Subtitle

Detailed explanation with examples.

### Code Example

```java
// Example code with comments
public class Example {
    // Implementation
}
```

### Configuration Example

```yaml
# Configuration example
spring:
  cloud:
    gateway:
      routes:
        - id: example-route
          uri: lb://example-service
```

## Testing Guidelines

### Unit Testing

#### Test Structure

```java
@ExtendWith(MockitoExtension.class)
class CustomFilterTest {
    
    @Mock
    private GatewayFilterChain chain;
    
    @Mock
    private ServerWebExchange exchange;
    
    @Mock
    private ServerHttpRequest request;
    
    @Test
    @DisplayName("Should process request successfully")
    void shouldProcessRequest() {
        // Given
        CustomFilter filter = new CustomFilter();
        when(exchange.getRequest()).thenReturn(request);
        when(chain.filter(any())).thenReturn(Mono.empty());
        
        // When
        filter.filter(exchange, chain).block();
        
        // Then
        verify(chain).filter(any());
    }
    
    @Test
    @DisplayName("Should handle errors gracefully")
    void shouldHandleErrors() {
        // Given
        CustomFilter filter = new CustomFilter();
        when(exchange.getRequest()).thenReturn(request);
        when(chain.filter(any())).thenReturn(Mono.error(new RuntimeException("Test error")));
        
        // When & Then
        assertThatThrownBy(() -> filter.filter(exchange, chain).block())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test error");
    }
}
```

#### Test Coverage

- **Minimum Coverage**: 80% line coverage
- **Critical Paths**: 100% coverage for critical business logic
- **Edge Cases**: Test error conditions and edge cases
- **Integration**: Test component interactions

### Integration Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.cloud.gateway.routes[0].id=test-route",
        "spring.cloud.gateway.routes[0].uri=http://localhost:8081",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/test/**"
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
}
```

### Performance Testing

```java
@Test
@Timeout(value = 5, unit = TimeUnit.SECONDS)
void shouldProcessRequestsWithinTimeLimit() {
    // Given
    CustomFilter filter = new CustomFilter();
    int requestCount = 1000;
    
    // When
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < requestCount; i++) {
        filter.filter(exchange, chain).block();
    }
    long endTime = System.currentTimeMillis();
    
    // Then
    long duration = endTime - startTime;
    assertThat(duration).isLessThan(5000); // 5 seconds
}
```

## Documentation Standards

### Code Documentation

#### JavaDoc

```java
/**
 * Custom filter factory for processing requests with configuration.
 * 
 * <p>This factory creates filters that can be configured through YAML
 * configuration files. The filter supports various configuration options
 * including header manipulation, request modification, and response processing.
 * 
 * <p>Example configuration:
 * <pre>
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *         - id: custom-route
 *           uri: lb://service
 *           filters:
 *             - CustomFilter=enabled:true,headerName:X-Custom
 * </pre>
 * 
 * @param <T> The configuration type for this filter factory
 * @author Your Name
 * @since 2.0.0
 * @see GatewayFilter
 * @see AbstractGatewayFilterFactory
 */
public class CustomFilterFactory<T> extends AbstractGatewayFilterFactory<T> {
    
    /**
     * Creates a new filter instance with the given configuration.
     * 
     * @param config the filter configuration, must not be null
     * @return a configured filter instance
     * @throws IllegalArgumentException if config is null
     */
    @Override
    public GatewayFilter apply(T config) {
        // Implementation
    }
}
```

#### README Files

```markdown
# Custom Filter

A custom filter for TiGateway that provides advanced request processing capabilities.

## Features

- Request header manipulation
- Response modification
- Configurable behavior
- High performance

## Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: custom-route
          uri: lb://service
          filters:
            - CustomFilter=enabled:true,headerName:X-Custom
```

## Examples

### Basic Usage

```java
@Component
public class MyCustomFilter extends CustomFilterFactory<MyCustomFilter.Config> {
    // Implementation
}
```

## Testing

Run tests with:

```bash
./mvnw test -Dtest=CustomFilterTest
```

## Contributing

See [Contributing Guide](../contributing.md) for details.
```

### API Documentation

```yaml
# OpenAPI specification
openapi: 3.0.0
info:
  title: TiGateway API
  version: 2.1.0
  description: API Gateway for microservices

paths:
  /actuator/gateway/routes:
    get:
      summary: Get all routes
      description: Retrieve all configured routes
      responses:
        '200':
          description: Success
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Route'
```

## Submitting Changes

### Pull Request Process

#### 1. Create Feature Branch

```bash
# Create and switch to feature branch
git checkout -b feature/custom-filter

# Make your changes
git add .
git commit -m "feat: add custom filter with configuration support"
```

#### 2. Push and Create PR

```bash
# Push to your fork
git push origin feature/custom-filter

# Create pull request on GitHub
```

#### 3. PR Template

```markdown
## Description

Brief description of changes.

## Type of Change

- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing

- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Checklist

- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] Tests pass
- [ ] No breaking changes (or documented)

## Related Issues

Fixes #123
```

### Commit Message Format

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

#### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes
- **refactor**: Code refactoring
- **test**: Test changes
- **chore**: Build process or auxiliary tool changes

#### Examples

```
feat(filter): add custom filter with configuration support

Add a new custom filter that supports YAML configuration
with options for header manipulation and request processing.

Fixes #123
```

```
fix(gateway): resolve route matching issue

Fix issue where routes with complex predicates were not
matching correctly in certain scenarios.

Closes #456
```

### Code Review Process

#### Review Checklist

- [ ] Code follows style guidelines
- [ ] Tests are comprehensive
- [ ] Documentation is updated
- [ ] No breaking changes
- [ ] Performance impact considered
- [ ] Security implications reviewed

#### Review Guidelines

1. **Be Constructive**: Provide helpful feedback
2. **Be Specific**: Point out exact issues
3. **Be Respectful**: Maintain professional tone
4. **Be Thorough**: Check all aspects of the code
5. **Be Timely**: Respond within 48 hours

## Community Guidelines

### Code of Conduct

#### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors.

#### Expected Behavior

- Use welcoming and inclusive language
- Be respectful of differing viewpoints
- Accept constructive criticism gracefully
- Focus on what is best for the community
- Show empathy towards other community members

#### Unacceptable Behavior

- Harassment, trolling, or inflammatory comments
- Personal attacks or political discussions
- Public or private harassment
- Publishing private information without permission
- Other unprofessional conduct

### Getting Help

#### Communication Channels

- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: General questions and discussions
- **Discord**: Real-time chat and community support
- **Email**: Security issues and private matters

#### Response Times

- **Critical Issues**: Within 24 hours
- **Bug Reports**: Within 48 hours
- **Feature Requests**: Within 1 week
- **General Questions**: Within 3 days

## Development Workflow

### Branch Strategy

```
main
├── develop
│   ├── feature/custom-filter
│   ├── feature/oauth2-support
│   └── bugfix/route-matching
├── release/2.1.0
└── hotfix/security-patch
```

### Release Process

1. **Feature Development**: Develop features in feature branches
2. **Integration**: Merge features into develop branch
3. **Testing**: Comprehensive testing in develop branch
4. **Release**: Create release branch from develop
5. **Stabilization**: Bug fixes in release branch
6. **Release**: Tag and release from release branch
7. **Hotfixes**: Critical fixes in hotfix branches

### CI/CD Pipeline

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Run tests
      run: ./mvnw test
    - name: Generate coverage report
      run: ./mvnw jacoco:report
    - name: Upload coverage
      uses: codecov/codecov-action@v3
```

## Troubleshooting

### Common Issues

#### Build Failures

```bash
# Clean and rebuild
./mvnw clean install

# Check Java version
java -version

# Check Maven version
./mvnw -version
```

#### Test Failures

```bash
# Run specific test
./mvnw test -Dtest=CustomFilterTest

# Run with debug
./mvnw test -Dtest=CustomFilterTest -Ddebug=true

# Check test logs
tail -f target/surefire-reports/*.txt
```

#### IDE Issues

```bash
# Refresh Maven project
# IntelliJ: Maven -> Reload All Maven Projects
# Eclipse: Right-click project -> Maven -> Reload Projects

# Clean IDE cache
# IntelliJ: File -> Invalidate Caches and Restart
# Eclipse: Project -> Clean -> Clean all projects
```

## Next Steps

After contributing:

1. **[Documentation](./documentation.md)** - Learn about documentation standards
2. **[Testing](./testing.md)** - Understand testing requirements
3. **[Release Process](./release-process.md)** - Learn about releases
4. **[Community](./community.md)** - Join the community

---

**Ready to contribute?** Check out our [GitHub Issues](https://github.com/tigateway/tigateway/issues) for open tasks and start contributing!
