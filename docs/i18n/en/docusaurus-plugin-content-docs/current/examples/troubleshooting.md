# TiGateway Troubleshooting Guide

## Overview

This document provides diagnostic and resolution guidance for common TiGateway issues, helping you quickly identify and resolve problems encountered during system operation.

## Common Issue Categories

### 🔧 Startup Issues
- Application startup failure
- Port conflicts
- Dependency injection errors
- Configuration errors

### 🌐 Network Issues
- Service unavailability
- Routes not taking effect
- Load balancing problems
- Timeout issues

### 🔐 Security Issues
- Authentication failures
- Authorization errors
- Certificate issues
- Insufficient permissions

### 📊 Performance Issues
- Slow response times
- Memory leaks
- High CPU usage
- Connection pool exhaustion

### 🗄️ Data Issues
- Configuration loss
- Data inconsistency
- Cache problems
- Storage connection failures

## Startup Issue Diagnosis

### 1. Application Startup Failure

#### Problem Symptoms
```bash
Error: A JNI error has occurred, please check your installation and try again
Exception in thread "main" java.lang.UnsupportedClassVersionError
```

#### Diagnostic Steps
```bash
# 1. Check Java version
java -version
mvn -version

# 2. Check JAVA_HOME setting
echo $JAVA_HOME

# 3. Clean and recompile
mvn clean compile

# 4. Check dependency conflicts
mvn dependency:tree
```

#### Solutions
```bash
# Set correct Java version
export JAVA_HOME=/path/to/java11
export PATH=$JAVA_HOME/bin:$PATH

# Recompile project
mvn clean compile -DskipTests

# Start application
mvn spring-boot:run -pl ti-gateway-kubernetes
```

### 2. Port Conflicts

#### Problem Symptoms
```bash
reactor.netty.ChannelBindException: Failed to bind on [0.0.0.0:8080]
```

#### Diagnostic Steps
```bash
# 1. Check port usage
lsof -i :8080
lsof -i :8081
lsof -i :8090

# 2. Check processes
ps aux | grep java
ps aux | grep tigateway

# 3. Check network connections
netstat -tulpn | grep :8080
```

#### Solutions
```bash
# Kill processes using the port
sudo kill -9 $(lsof -t -i:8080)

# Change port in configuration
echo "server.port=8081" >> application.yml

# Use different ports for different services
server.port=8080
admin.server.port=8081
management.server.port=8090
```

### 3. Dependency Injection Errors

#### Problem Symptoms
```bash
org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type
```

#### Diagnostic Steps
```bash
# 1. Check component scanning
grep -r "@ComponentScan" src/
grep -r "@SpringBootApplication" src/

# 2. Check bean definitions
grep -r "@Bean" src/
grep -r "@Service" src/
grep -r "@Repository" src/

# 3. Check configuration classes
grep -r "@Configuration" src/
```

#### Solutions
```java
// Ensure proper component scanning
@SpringBootApplication
@ComponentScan(basePackages = {"ti.gateway", "org.springframework.cloud.gateway"})
public class TiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(TiGatewayApplication.class, args);
    }
}

// Add missing bean definitions
@Configuration
public class GatewayConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/users/**")
                        .uri("lb://user-service"))
                .build();
    }
}
```

### 4. Configuration Errors

#### Problem Symptoms
```bash
org.springframework.boot.context.properties.bind.BindException: Failed to bind properties
```

#### Diagnostic Steps
```bash
# 1. Check configuration files
ls -la src/main/resources/
cat application.yml
cat application-prod.yml

# 2. Validate configuration
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"

# 3. Check environment variables
env | grep SPRING
env | grep TIGATEWAY
```

#### Solutions
```yaml
# Fix configuration format
spring:
  application:
    name: tigateway
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**

# Use proper YAML indentation
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

## Network Issue Diagnosis

### 1. Service Unavailability

#### Problem Symptoms
```bash
java.net.ConnectException: Connection refused
org.springframework.web.client.ResourceAccessException: I/O error
```

#### Diagnostic Steps
```bash
# 1. Check service status
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/gateway/routes

# 2. Check service discovery
curl http://localhost:8080/actuator/gateway/globalfilters
curl http://localhost:8080/actuator/gateway/routefilters

# 3. Check network connectivity
ping backend-service
telnet backend-service 8080
nslookup backend-service
```

#### Solutions
```yaml
# Configure service discovery
spring:
  cloud:
    discovery:
      locator:
        enabled: true
        lower-case-service-id: true

# Add health checks
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
```

### 2. Routes Not Taking Effect

#### Problem Symptoms
```bash
404 Not Found
No route found for the given request
```

#### Diagnostic Steps
```bash
# 1. Check route configuration
curl http://localhost:8080/actuator/gateway/routes

# 2. Check route predicates
curl http://localhost:8080/actuator/gateway/routepredicates

# 3. Test route matching
curl -v http://localhost:8080/api/users/123
```

#### Solutions
```yaml
# Fix route configuration
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

# Add route debugging
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.springframework.web.reactive: DEBUG
```

### 3. Load Balancing Problems

#### Problem Symptoms
```bash
java.util.concurrent.TimeoutException
org.springframework.cloud.client.loadbalancer.LoadBalancerRequestFailedException
```

#### Diagnostic Steps
```bash
# 1. Check service instances
curl http://localhost:8080/actuator/gateway/routes/user-service

# 2. Check load balancer configuration
curl http://localhost:8080/actuator/gateway/globalfilters

# 3. Monitor service health
curl http://backend-service:8080/health
```

#### Solutions
```yaml
# Configure load balancer
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY,INTERNAL_SERVER_ERROR
                methods: GET,POST
```

### 4. Timeout Issues

#### Problem Symptoms
```bash
java.util.concurrent.TimeoutException: Did not observe any item or terminal signal
```

#### Diagnostic Steps
```bash
# 1. Check timeout configuration
grep -r "timeout" src/main/resources/
grep -r "connect-timeout" src/main/resources/

# 2. Monitor response times
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/api/users/123

# 3. Check backend service performance
curl -w "Time: %{time_total}s\n" http://backend-service:8080/api/users/123
```

#### Solutions
```yaml
# Configure timeouts
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 1000
        response-timeout: 5s
        pool:
          max-connections: 500
          max-idle-time: 30s
```

## Security Issue Diagnosis

### 1. Authentication Failures

#### Problem Symptoms
```bash
org.springframework.security.authentication.BadCredentialsException
401 Unauthorized
```

#### Diagnostic Steps
```bash
# 1. Check authentication configuration
grep -r "security" src/main/resources/
grep -r "oauth2" src/main/resources/

# 2. Test authentication endpoints
curl -v http://localhost:8080/oauth2/authorization/sso
curl -v -H "Authorization: Bearer token" http://localhost:8080/api/users/123

# 3. Check JWT token validity
echo "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." | base64 -d
```

#### Solutions
```yaml
# Configure OAuth2
spring:
  security:
    oauth2:
      client:
        provider:
          sso:
            issuer-uri: ${SSO_ISSUER_URI}
        registration:
          sso:
            client-id: ${SSO_CLIENT_ID}
            client-secret: ${SSO_CLIENT_SECRET}
            scope: openid,profile,email
```

### 2. Authorization Errors

#### Problem Symptoms
```bash
org.springframework.security.access.AccessDeniedException
403 Forbidden
```

#### Diagnostic Steps
```bash
# 1. Check user roles
curl -H "Authorization: Bearer token" http://localhost:8080/api/user/profile

# 2. Check authorization configuration
grep -r "@PreAuthorize" src/
grep -r "@Secured" src/

# 3. Verify role assignments
curl -H "Authorization: Bearer token" http://localhost:8080/actuator/health
```

#### Solutions
```java
// Configure method-level security
@PreAuthorize("hasRole('USER')")
@GetMapping("/api/users/{id}")
public ResponseEntity<User> getUser(@PathVariable String id) {
    // Implementation
}

// Configure global security
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange()
                .pathMatchers("/api/users/**").hasRole("USER")
                .pathMatchers("/admin/**").hasRole("ADMIN")
                .anyExchange().authenticated()
                .and()
                .oauth2Login()
                .and()
                .build();
    }
}
```

### 3. Certificate Issues

#### Problem Symptoms
```bash
javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException
```

#### Diagnostic Steps
```bash
# 1. Check certificate validity
openssl x509 -in certificate.crt -text -noout
openssl verify certificate.crt

# 2. Check certificate chain
openssl s_client -connect backend-service:443 -showcerts

# 3. Check truststore configuration
keytool -list -keystore truststore.jks
```

#### Solutions
```yaml
# Configure SSL
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tigateway
```

## Performance Issue Diagnosis

### 1. Slow Response Times

#### Problem Symptoms
```bash
High response times (> 1 second)
Timeout errors
```

#### Diagnostic Steps
```bash
# 1. Monitor response times
curl -w "Time: %{time_total}s\n" -o /dev/null -s http://localhost:8080/api/users/123

# 2. Check JVM metrics
curl http://localhost:8090/actuator/metrics/jvm.memory.used
curl http://localhost:8090/actuator/metrics/jvm.gc.pause

# 3. Profile application
jstack <pid>
jmap -histo <pid>
```

#### Solutions
```yaml
# Optimize JVM settings
JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Configure connection pooling
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 1000
          max-idle-time: 30s
          max-life-time: 60s
```

### 2. Memory Leaks

#### Problem Symptoms
```bash
java.lang.OutOfMemoryError: Java heap space
Increasing memory usage over time
```

#### Diagnostic Steps
```bash
# 1. Monitor memory usage
curl http://localhost:8090/actuator/metrics/jvm.memory.used
curl http://localhost:8090/actuator/metrics/jvm.memory.max

# 2. Generate heap dump
jmap -dump:format=b,file=heap.hprof <pid>

# 3. Analyze memory usage
jhat heap.hprof
```

#### Solutions
```yaml
# Increase heap size
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC"

# Configure garbage collection
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"

# Monitor memory usage
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

### 3. High CPU Usage

#### Problem Symptoms
```bash
High CPU usage (> 80%)
Slow system response
```

#### Diagnostic Steps
```bash
# 1. Check CPU usage
top -p <pid>
htop

# 2. Profile CPU usage
jstack <pid>
jcmd <pid> Thread.print

# 3. Monitor thread activity
curl http://localhost:8090/actuator/metrics/jvm.threads.live
```

#### Solutions
```yaml
# Optimize thread pool
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          type: elastic
          max-connections: 500

# Configure async processing
spring:
  task:
    execution:
      pool:
        core-size: 8
        max-size: 16
        queue-capacity: 100
```

## Data Issue Diagnosis

### 1. Configuration Loss

#### Problem Symptoms
```bash
Routes not found after restart
Configuration changes not persisted
```

#### Diagnostic Steps
```bash
# 1. Check configuration storage
kubectl get configmap tigateway-config -o yaml
kubectl get secrets tigateway-secrets

# 2. Check configuration files
ls -la /app/config/
cat /app/config/application.yml

# 3. Verify configuration loading
curl http://localhost:8080/actuator/configprops
```

#### Solutions
```yaml
# Use ConfigMap for configuration
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-config
data:
  application.yml: |
    spring:
      cloud:
        gateway:
          routes:
            - id: user-service
              uri: lb://user-service
              predicates:
                - Path=/api/users/**
```

### 2. Data Inconsistency

#### Problem Symptoms
```bash
Stale data in cache
Inconsistent service responses
```

#### Diagnostic Steps
```bash
# 1. Check cache status
curl http://localhost:8090/actuator/caches
curl http://localhost:8090/actuator/metrics/cache.gets

# 2. Clear cache
curl -X POST http://localhost:8090/actuator/caches/{cacheName}

# 3. Check data sources
curl http://localhost:8090/actuator/health
```

#### Solutions
```yaml
# Configure cache TTL
spring:
  cache:
    redis:
      time-to-live: 600000
      cache-null-values: false

# Add cache invalidation
@CacheEvict(value = "routes", allEntries = true)
public void refreshRoutes() {
    // Implementation
}
```

## Monitoring and Diagnostic Tools

### 1. Health Checks

```bash
# Basic health check
curl http://localhost:8090/actuator/health

# Detailed health information
curl http://localhost:8090/actuator/health | jq

# Custom health indicators
curl http://localhost:8090/actuator/health/custom
```

### 2. Metrics Monitoring

```bash
# JVM metrics
curl http://localhost:8090/actuator/metrics/jvm.memory.used
curl http://localhost:8090/actuator/metrics/jvm.gc.pause

# Gateway metrics
curl http://localhost:8090/actuator/metrics/gateway.requests
curl http://localhost:8090/actuator/metrics/gateway.route.requests

# Custom metrics
curl http://localhost:8090/actuator/metrics/custom.metric
```

### 3. Log Analysis

```bash
# View application logs
kubectl logs -f deployment/tigateway

# Search for errors
kubectl logs deployment/tigateway | grep ERROR

# Filter by time range
kubectl logs deployment/tigateway --since=1h | grep ERROR
```

## FAQ

### Q: How to enable debug logging?

A: Add the following to your configuration:

```yaml
logging:
  level:
    ti.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
    org.springframework.web.reactive: DEBUG
    reactor.netty: DEBUG
```

### Q: How to check route configuration?

A: Use the following endpoints:

```bash
# List all routes
curl http://localhost:8080/actuator/gateway/routes

# Get specific route
curl http://localhost:8080/actuator/gateway/routes/{routeId}

# Refresh routes
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

### Q: How to troubleshoot authentication issues?

A: Follow these steps:

1. Check OAuth2 configuration
2. Verify JWT token validity
3. Check user roles and permissions
4. Review security logs

### Q: How to optimize performance?

A: Consider these optimizations:

1. Tune JVM parameters
2. Configure connection pooling
3. Enable caching
4. Monitor and profile the application

### Q: How to handle high availability?

A: Implement these strategies:

1. Use multiple gateway instances
2. Configure load balancing
3. Implement circuit breakers
4. Set up monitoring and alerting

---

**Related Documentation**:
- [Configuration Guide](../configuration/configuration.md)
- [Monitoring Guide](../deployment/monitoring.md)
- [Security Guide](../security/security-best-practices.md)
- [Performance Tuning Guide](../performance/performance-tuning.md)
