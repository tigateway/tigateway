# Migration Guide

This guide helps you migrate from older versions of TiGateway to the latest version, including configuration changes, API updates, and breaking changes.

## Overview

This migration guide covers:

- **Version 1.x to 2.0**: Major architecture changes
- **Version 2.0 to 2.1**: Enhanced features and improvements
- **Configuration Migration**: YAML format changes
- **API Migration**: Endpoint and parameter changes
- **Plugin Migration**: Extension system updates
- **Breaking Changes**: Incompatible changes

## Migration from 1.x to 2.0

### Major Changes

Version 2.0 introduces significant architectural improvements:

- **Spring Cloud Gateway 4.x**: Updated to latest version
- **Java 17+**: Minimum Java version requirement
- **Configuration Format**: Changed from properties to YAML
- **Plugin System**: Complete rewrite of extension architecture
- **Authentication**: Updated authentication flow

### Configuration Migration

#### Old Format (1.x)

```properties
# application.properties
gateway.routes[0].id=user-service-route
gateway.routes[0].uri=http://user-service:8080
gateway.routes[0].predicates[0]=Path=/api/users/**
gateway.routes[0].filters[0]=StripPrefix=2

security.jwt.secret=your-secret
security.jwt.expiration=3600

monitoring.prometheus.enabled=true
monitoring.prometheus.endpoint=/actuator/prometheus
```

#### New Format (2.0)

```yaml
# application.yml
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

security:
  oauth2:
    resourceserver:
      jwt:
        jwk-set-uri: http://auth-service:8080/.well-known/jwks.json
        issuer-uri: http://auth-service:8080

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

### Authentication Migration

#### Old JWT Configuration (1.x)

```properties
security.jwt.secret=your-secret
security.jwt.expiration=3600
security.jwt.audience=api-gateway
```

#### New JWT Configuration (2.0)

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${JWT_JWK_SET_URI:http://auth-service:8080/.well-known/jwks.json}
          issuer-uri: ${JWT_ISSUER_URI:http://auth-service:8080}
          audience: ${JWT_AUDIENCE:api-gateway}
          clock-skew: 60s
          cache-ttl: 600s
```

### Plugin Migration

#### Old Plugin System (1.x)

```java
@Component
public class CustomFilter implements GatewayFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Filter logic
        return chain.filter(exchange);
    }
}
```

#### New Plugin System (2.0)

```java
@Component
public class CustomGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomGatewayFilterFactory.Config> {
    
    public CustomGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Filter logic with configuration
            return chain.filter(exchange);
        };
    }
    
    @Data
    public static class Config {
        private boolean enabled = true;
        private String value = "default";
    }
}
```

### Service Discovery Migration

#### Old Configuration (1.x)

```properties
eureka.client.service-url.defaultZone=http://eureka:8761/eureka/
eureka.instance.prefer-ip-address=true
```

#### New Configuration (2.0)

```yaml
spring:
  cloud:
    discovery:
      client:
        enabled: true
        service-id: tigateway
        eureka:
          instance:
            health-check-url: http://${spring.cloud.client.ip-address}:${server.port}/actuator/health
            health-check-url-path: /actuator/health
            prefer-ip-address: true
            lease-renewal-interval-in-seconds: 30
            lease-expiration-duration-in-seconds: 90
```

## Migration from 2.0 to 2.1

### New Features

Version 2.1 introduces:

- **Kubernetes Native Features**: CRDs and operator support
- **Enhanced Security**: OAuth2 PKCE support
- **Advanced Monitoring**: Custom metrics and tracing
- **Load Balancing Improvements**: Health-aware load balancing

### Kubernetes Integration

#### Enable Kubernetes Support

```yaml
spring:
  cloud:
    kubernetes:
      discovery:
        enabled: true
        service-name: tigateway
        namespace: default
        health-check-path: /actuator/health
        health-check-interval: 10s
        health-check-timeout: 5s
```

#### CRD Configuration

```yaml
apiVersion: gateway.tigateway.io/v1
kind: GatewayRoute
metadata:
  name: user-service-route
spec:
  route:
    id: user-service-route
    uri: lb://user-service
    predicates:
      - Path=/api/users/**
    filters:
      - StripPrefix=2
```

### Enhanced Security

#### OAuth2 PKCE Support

```yaml
security:
  oauth2:
    pkce:
      enabled: true
      code-challenge-method: S256
      code-verifier-length: 128
```

#### Enhanced JWT Configuration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          custom-claims:
            roles: "roles"
            tenant: "tenant"
            permissions: "permissions"
```

### Advanced Monitoring

#### Custom Metrics

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
        step: 10s
      influx:
        enabled: false
        uri: http://influxdb:8086
        db: tigateway
        step: 10s
```

#### Distributed Tracing

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
    zipkin:
      tracing:
        endpoint: http://zipkin:9411/api/v2/spans
    wavefront:
      tracing:
        endpoint: http://wavefront:8080/api/v2/spans
```

## Configuration Migration Tools

### Automated Migration Script

```bash
#!/bin/bash
# migrate-config.sh

echo "Migrating TiGateway configuration from 1.x to 2.0..."

# Convert properties to YAML
if [ -f "application.properties" ]; then
    echo "Converting application.properties to application.yml..."
    python3 -c "
import yaml
import re

# Read properties file
with open('application.properties', 'r') as f:
    content = f.read()

# Convert to YAML structure
config = {}
for line in content.split('\n'):
    if line.strip() and not line.startswith('#'):
        key, value = line.split('=', 1)
        keys = key.split('.')
        
        # Build nested structure
        current = config
        for k in keys[:-1]:
            if k not in current:
                current[k] = {}
            current = current[k]
        current[keys[-1]] = value

# Write YAML file
with open('application.yml', 'w') as f:
    yaml.dump(config, f, default_flow_style=False)

print('Configuration migrated successfully!')
"
fi

echo "Migration completed!"
```

### Manual Migration Checklist

#### Pre-Migration

- [ ] Backup current configuration
- [ ] Review breaking changes
- [ ] Test migration in development environment
- [ ] Update dependencies
- [ ] Review plugin compatibility

#### Configuration Migration

- [ ] Convert properties to YAML format
- [ ] Update authentication configuration
- [ ] Migrate service discovery settings
- [ ] Update monitoring configuration
- [ ] Convert plugin configurations

#### Post-Migration

- [ ] Test all routes and filters
- [ ] Verify authentication works
- [ ] Check service discovery
- [ ] Validate monitoring metrics
- [ ] Test custom plugins

## Breaking Changes

### Version 2.0 Breaking Changes

#### Configuration Format

**Before (1.x):**
```properties
gateway.routes[0].id=route-id
gateway.routes[0].uri=http://service:8080
```

**After (2.0):**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: route-id
          uri: lb://service
```

#### API Endpoints

**Before (1.x):**
```
GET /admin/routes
POST /admin/routes
DELETE /admin/routes/{id}
```

**After (2.0):**
```
GET /actuator/gateway/routes
POST /actuator/gateway/routes
DELETE /actuator/gateway/routes/{id}
```

#### Plugin API

**Before (1.x):**
```java
@Component
public class CustomFilter implements GatewayFilter {
    // Simple filter implementation
}
```

**After (2.0):**
```java
@Component
public class CustomGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {
    // Factory-based filter implementation
}
```

### Version 2.1 Breaking Changes

#### Kubernetes CRDs

**Before (2.0):**
```yaml
# Route configuration in application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: route-id
          uri: lb://service
```

**After (2.1):**
```yaml
# Route configuration via CRD
apiVersion: gateway.tigateway.io/v1
kind: GatewayRoute
metadata:
  name: route-id
spec:
  route:
    id: route-id
    uri: lb://service
```

#### Enhanced Security Headers

**Before (2.0):**
```yaml
security:
  headers:
    enabled: true
```

**After (2.1):**
```yaml
security:
  headers:
    enabled: true
    content-security-policy: "default-src 'self'"
    x-frame-options: DENY
    x-content-type-options: nosniff
    strict-transport-security: "max-age=31536000; includeSubDomains"
```

## Migration Best Practices

### Planning

1. **Review Changes**: Read the changelog and breaking changes
2. **Test Environment**: Set up a test environment for migration
3. **Backup**: Backup current configuration and data
4. **Dependencies**: Update all dependencies
5. **Documentation**: Review new documentation

### Execution

1. **Gradual Migration**: Migrate one component at a time
2. **Configuration**: Use migration tools when available
3. **Testing**: Test each component after migration
4. **Rollback Plan**: Have a rollback plan ready
5. **Monitoring**: Monitor system during migration

### Validation

1. **Functionality**: Verify all features work correctly
2. **Performance**: Check performance metrics
3. **Security**: Validate security configurations
4. **Monitoring**: Ensure monitoring works
5. **Documentation**: Update documentation

## Troubleshooting Migration Issues

### Common Issues

#### Configuration Not Loading

```bash
# Check YAML syntax
yamllint application.yml

# Validate configuration
java -jar tigateway.jar --spring.config.location=application.yml --validate

# Check configuration properties
curl http://localhost:8080/actuator/configprops
```

#### Authentication Failures

```bash
# Check JWT configuration
curl http://localhost:8080/actuator/configprops | grep -i jwt

# Test authentication
curl -H "Authorization: Bearer <token>" http://localhost:8080/actuator/health

# Check authentication logs
tail -f logs/tigateway.log | grep -i auth
```

#### Plugin Loading Issues

```bash
# Check plugin configuration
curl http://localhost:8080/actuator/beans | grep -i custom

# Check plugin logs
tail -f logs/tigateway.log | grep -i plugin

# Verify Spring configuration
curl http://localhost:8080/actuator/configprops | grep -i plugin
```

#### Service Discovery Problems

```bash
# Check service discovery
curl http://localhost:8080/actuator/health

# Check service instances
curl http://localhost:8761/eureka/apps

# Test service discovery
curl http://localhost:8080/actuator/gateway/routes
```

### Debug Commands

```bash
# Check application status
curl http://localhost:8080/actuator/health

# Check configuration
curl http://localhost:8080/actuator/configprops

# Check environment
curl http://localhost:8080/actuator/env

# Check metrics
curl http://localhost:8080/actuator/metrics

# Check routes
curl http://localhost:8080/actuator/gateway/routes
```

## Rollback Procedures

### Configuration Rollback

```bash
# Restore backup configuration
cp application.yml.backup application.yml

# Restart application
systemctl restart tigateway
```

### Database Rollback

```bash
# Restore database backup
pg_restore -h localhost -U tigateway -d tigateway backup.sql

# Or for MySQL
mysql -h localhost -u tigateway -p tigateway < backup.sql
```

### Plugin Rollback

```bash
# Remove new plugins
rm -rf plugins/new-plugin/

# Restore old plugins
cp -r plugins/backup/ plugins/

# Restart application
systemctl restart tigateway
```

## Migration Support

### Getting Help

- **Documentation**: [https://tigateway.github.io/tigateway](https://tigateway.github.io/tigateway)
- **GitHub Issues**: [https://github.com/tigateway/tigateway/issues](https://github.com/tigateway/tigateway/issues)
- **Discussions**: [https://github.com/tigateway/tigateway/discussions](https://github.com/tigateway/tigateway/discussions)
- **Community**: [https://github.com/tigateway/tigateway/discussions](https://github.com/tigateway/tigateway/discussions)

### Migration Resources

- **Migration Scripts**: Available in the repository
- **Configuration Examples**: Updated examples for each version
- **Video Tutorials**: Step-by-step migration videos
- **Community Support**: Active community help

## Next Steps

After completing migration:

1. **[Quick Start](./quick-start.md)** - Get started with new features
2. **[Configuration Guide](./configuration.md)** - Learn new configuration options
3. **[Troubleshooting Guide](./troubleshooting.md)** - Resolve common issues
4. **[Changelog](./changelog.md)** - Review version changes

---

**Need help with migration?** Check out our [GitHub Discussions](https://github.com/tigateway/tigateway/discussions) for community support and migration assistance.
