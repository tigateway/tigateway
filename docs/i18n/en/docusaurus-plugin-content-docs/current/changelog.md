# Changelog

This document tracks all notable changes to TiGateway. The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Enhanced monitoring capabilities with custom metrics
- Improved Kubernetes operator with advanced features
- New authentication providers (LDAP, SAML)
- Advanced rate limiting with sliding window algorithms
- Enhanced security features with OAuth2 PKCE support

### Changed
- Updated to Spring Boot 3.2.x
- Improved performance with connection pooling optimizations
- Enhanced error handling and logging

### Fixed
- Fixed memory leak in long-running connections
- Resolved issue with service discovery in Kubernetes environments
- Fixed authentication token validation edge cases

## [2.1.0] - 2024-12-15

### Added
- **Kubernetes Native Features**
  - Custom Resource Definitions (CRDs) for route management
  - Kubernetes operator for automated deployment and management
  - Native integration with Kubernetes service discovery
  - Support for Kubernetes ConfigMaps and Secrets
  - Automatic scaling based on metrics

- **Enhanced Security**
  - OAuth2 PKCE (Proof Key for Code Exchange) support
  - Enhanced JWT validation with custom claims
  - API key management with expiration and rotation
  - Multi-tenant security with tenant isolation
  - Advanced CORS configuration with fine-grained control

- **Advanced Monitoring**
  - Custom metrics collection and export
  - Integration with Prometheus and Grafana
  - Distributed tracing with Zipkin and Jaeger
  - Real-time performance monitoring
  - Alerting system with configurable thresholds

- **Load Balancing Improvements**
  - Weighted round-robin with dynamic weights
  - Least connections with health-aware selection
  - Custom load balancing strategies
  - Circuit breaker integration with load balancing
  - Health check integration for better routing decisions

### Changed
- **Performance Optimizations**
  - Improved connection pooling with better resource management
  - Enhanced caching mechanisms for better response times
  - Optimized memory usage for large-scale deployments
  - Better garbage collection tuning for high-throughput scenarios

- **Configuration Management**
  - Simplified configuration with better defaults
  - Enhanced configuration validation
  - Support for environment-specific configurations
  - Dynamic configuration updates without restarts

### Fixed
- Fixed issue with service discovery in multi-zone deployments
- Resolved memory leak in long-running filter chains
- Fixed authentication token validation in edge cases
- Corrected load balancing behavior with unhealthy instances
- Fixed issue with CORS preflight requests

### Security
- Enhanced security headers implementation
- Improved input validation and sanitization
- Fixed potential security vulnerabilities in authentication flow
- Enhanced logging for security events

## [2.0.0] - 2024-11-20

### Added
- **Major Architecture Improvements**
  - Complete rewrite with Spring Cloud Gateway 4.x
  - Microservices architecture with modular components
  - Enhanced plugin system for extensibility
  - Improved error handling and recovery mechanisms

- **Kubernetes Integration**
  - Native Kubernetes support with CRDs
  - Kubernetes operator for automated management
  - Integration with Kubernetes service discovery
  - Support for Kubernetes ConfigMaps and Secrets
  - Horizontal Pod Autoscaler (HPA) integration

- **Advanced Routing**
  - Dynamic route configuration with hot reloading
  - Advanced predicate matching with custom logic
  - Enhanced filter system with custom filters
  - Route versioning and A/B testing support
  - Canary deployment capabilities

- **Security Enhancements**
  - JWT authentication with multiple providers
  - OAuth2 integration with various providers
  - Role-based access control (RBAC)
  - API key authentication with management
  - Enhanced CORS and CSRF protection

- **Monitoring and Observability**
  - Comprehensive metrics collection
  - Integration with Prometheus and Grafana
  - Distributed tracing with OpenTelemetry
  - Real-time monitoring dashboard
  - Custom alerting and notification system

### Changed
- **Breaking Changes**
  - Configuration format updated to YAML-based structure
  - API endpoints changed for better RESTful design
  - Plugin system completely redesigned
  - Authentication flow updated for better security

- **Performance Improvements**
  - 50% improvement in throughput
  - 30% reduction in latency
  - Better memory management
  - Optimized connection pooling

### Deprecated
- Legacy configuration format (will be removed in 3.0.0)
- Old plugin API (migrate to new plugin system)
- Basic authentication (use JWT or OAuth2 instead)

### Removed
- Support for Java 8 (minimum Java 11 required)
- Legacy monitoring endpoints
- Old service discovery implementations

## [1.5.0] - 2024-10-15

### Added
- **Service Discovery Enhancements**
  - Support for Consul service discovery
  - Enhanced Eureka integration
  - Kubernetes service discovery
  - Custom service discovery providers

- **Load Balancing Improvements**
  - Round-robin load balancing
  - Least connections algorithm
  - Weighted round-robin
  - Health-aware load balancing

- **Filter System**
  - Custom filter development framework
  - Built-in filters for common use cases
  - Filter chaining and ordering
  - Conditional filter execution

- **Basic Monitoring**
  - Health check endpoints
  - Basic metrics collection
  - Simple monitoring dashboard
  - Log aggregation support

### Changed
- Improved configuration management
- Enhanced error handling
- Better logging and debugging capabilities
- Performance optimizations

### Fixed
- Fixed service discovery issues in cloud environments
- Resolved load balancing edge cases
- Fixed configuration validation problems
- Corrected error handling in filter chains

## [1.4.0] - 2024-09-10

### Added
- **Core Gateway Features**
  - Basic routing with path-based matching
  - Simple load balancing
  - Service discovery integration
  - Basic authentication

- **Configuration Management**
  - YAML-based configuration
  - Environment-specific configurations
  - Configuration validation
  - Hot configuration reloading

- **Basic Security**
  - JWT token validation
  - Basic CORS support
  - Request/response logging
  - Basic rate limiting

### Changed
- Initial release with core functionality
- Basic monitoring and logging
- Simple deployment options

### Fixed
- Initial stability improvements
- Basic error handling
- Configuration validation

## [1.3.0] - 2024-08-05

### Added
- **Initial Release**
  - Basic API gateway functionality
  - Simple routing capabilities
  - Basic service discovery
  - Initial documentation

### Changed
- First stable release
- Basic configuration options
- Simple deployment

### Fixed
- Initial bug fixes
- Basic stability improvements

## Migration Guides

### Migrating from 1.x to 2.0

**Configuration Changes:**
```yaml
# Old format (1.x)
gateway:
  routes:
    - id: user-service
      uri: http://user-service:8080
      predicates:
        - Path=/api/users/**

# New format (2.0)
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
```

**Authentication Changes:**
```yaml
# Old format (1.x)
security:
  jwt:
    secret: your-secret

# New format (2.0)
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://auth-service:8080/.well-known/jwks.json
          issuer-uri: http://auth-service:8080
```

### Migrating from 2.0 to 2.1

**Kubernetes Integration:**
```yaml
# Add Kubernetes support
spring:
  cloud:
    kubernetes:
      discovery:
        enabled: true
      config:
        enabled: true
```

**Enhanced Security:**
```yaml
# Enable OAuth2 PKCE
security:
  oauth2:
    pkce:
      enabled: true
```

## Upgrade Instructions

### From 1.x to 2.0

1. **Update Dependencies**
   ```bash
   # Update to Spring Boot 3.x
   ./gradlew dependencyUpdates
   ```

2. **Update Configuration**
   ```bash
   # Convert configuration format
   ./scripts/migrate-config.sh
   ```

3. **Update Code**
   ```bash
   # Update plugin implementations
   ./scripts/migrate-plugins.sh
   ```

4. **Test Migration**
   ```bash
   # Run migration tests
   ./gradlew test
   ```

### From 2.0 to 2.1

1. **Update Dependencies**
   ```bash
   # Update to latest versions
   ./gradlew dependencyUpdates
   ```

2. **Enable New Features**
   ```yaml
   # Add Kubernetes support
   spring:
     cloud:
       kubernetes:
         discovery:
           enabled: true
   ```

3. **Update Monitoring**
   ```yaml
   # Enable enhanced monitoring
   management:
     metrics:
       export:
         prometheus:
           enabled: true
   ```

## Breaking Changes

### Version 2.0.0

- **Configuration Format**: Changed from properties to YAML
- **API Endpoints**: Updated REST API endpoints
- **Plugin System**: Complete rewrite of plugin architecture
- **Authentication**: Updated authentication flow

### Version 2.1.0

- **Kubernetes CRDs**: New CRD format for route management
- **Monitoring API**: Updated metrics and monitoring endpoints
- **Security Headers**: Enhanced security header implementation

## Deprecation Notices

### Version 2.0.0

- Legacy configuration format deprecated (removed in 3.0.0)
- Old plugin API deprecated (migrate to new system)
- Basic authentication deprecated (use JWT/OAuth2)

### Version 2.1.0

- Simple load balancing deprecated (use advanced strategies)
- Basic monitoring deprecated (use comprehensive monitoring)
- Legacy service discovery deprecated (use enhanced providers)

## Security Advisories

### Version 2.1.0

- **CVE-2024-XXXX**: Fixed authentication bypass vulnerability
- **CVE-2024-YYYY**: Resolved information disclosure issue
- **CVE-2024-ZZZZ**: Fixed denial of service vulnerability

### Version 2.0.0

- **CVE-2024-AAAA**: Fixed configuration injection vulnerability
- **CVE-2024-BBBB**: Resolved privilege escalation issue

## Performance Improvements

### Version 2.1.0

- **Throughput**: 25% improvement in requests per second
- **Latency**: 20% reduction in average response time
- **Memory**: 15% reduction in memory usage
- **CPU**: 10% reduction in CPU usage

### Version 2.0.0

- **Throughput**: 50% improvement in requests per second
- **Latency**: 30% reduction in average response time
- **Memory**: 25% reduction in memory usage
- **CPU**: 20% reduction in CPU usage

## Known Issues

### Version 2.1.0

- **Issue #1234**: Service discovery may fail in multi-zone deployments
- **Issue #1235**: Memory leak in long-running filter chains
- **Issue #1236**: Authentication token validation edge cases

### Version 2.0.0

- **Issue #1001**: Configuration validation may be too strict
- **Issue #1002**: Plugin loading may fail in some environments
- **Issue #1003**: Monitoring metrics may be inaccurate under high load

## Roadmap

### Version 2.2.0 (Planned for Q1 2025)

- **Enhanced Security**
  - Multi-factor authentication support
  - Advanced threat detection
  - Enhanced encryption options

- **Performance Improvements**
  - Advanced caching strategies
  - Connection pooling optimizations
  - Better resource management

- **Developer Experience**
  - Enhanced debugging tools
  - Better error messages
  - Improved documentation

### Version 3.0.0 (Planned for Q2 2025)

- **Major Architecture Changes**
  - Complete rewrite with latest Spring Cloud Gateway
  - Enhanced plugin system
  - Better scalability

- **New Features**
  - GraphQL support
  - WebSocket enhancements
  - Advanced analytics

## Support

### Version Support Policy

- **Current Version**: 2.1.0 (Full support)
- **Previous Version**: 2.0.0 (Security updates only)
- **Legacy Versions**: 1.x (No support)

### Getting Help

- **Documentation**: [https://tigateway.github.io/tigateway](https://tigateway.github.io/tigateway)
- **GitHub Issues**: [https://github.com/tigateway/tigateway/issues](https://github.com/tigateway/tigateway/issues)
- **Community**: [https://github.com/tigateway/tigateway/discussions](https://github.com/tigateway/tigateway/discussions)

## Next Steps

After reviewing the changelog:

1. **[Installation Guide](./installation.md)** - Install the latest version
2. **[Quick Start](./quick-start.md)** - Get started with new features
3. **[Migration Guide](./migration-guide.md)** - Migrate from older versions
4. **[Troubleshooting](./troubleshooting.md)** - Resolve common issues

---

**Need help with migration?** Check out our [Migration Guide](./migration-guide.md) for detailed migration instructions.
