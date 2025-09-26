# Troubleshooting Guide

This comprehensive troubleshooting guide helps you diagnose and resolve common issues with TiGateway. It covers deployment problems, configuration issues, performance problems, and security-related issues.

## Table of Contents

1. [Common Issues](#1-common-issues)
2. [Deployment Issues](#2-deployment-issues)
3. [Configuration Issues](#3-configuration-issues)
4. [Performance Issues](#4-performance-issues)
5. [Security Issues](#5-security-issues)
6. [Network Issues](#6-network-issues)
7. [Monitoring and Debugging](#7-monitoring-and-debugging)
8. [Log Analysis](#8-log-analysis)
9. [Recovery Procedures](#9-recovery-procedures)
10. [Best Practices](#10-best-practices)

## 1. Common Issues

### 1.1 Gateway Not Starting

#### Symptoms
- TiGateway fails to start
- Application exits immediately
- Port binding errors

#### Diagnosis
```bash
# Check application logs
tail -f logs/tigateway.log

# Check if port is already in use
netstat -tulpn | grep :8080
lsof -i :8080

# Check Java process
ps aux | grep tigateway
jps -l | grep tigateway
```

#### Solutions

**Port Already in Use:**
```bash
# Find process using port 8080
sudo lsof -i :8080

# Kill the process
sudo kill -9 <PID>

# Or change port in application.yml
server:
  port: 8081
```

**Configuration Issues:**
```bash
# Validate configuration
java -jar tigateway.jar --spring.config.location=application.yml --debug

# Check configuration syntax
yaml-lint application.yml
```

**Memory Issues:**
```bash
# Check available memory
free -h
df -h

# Increase JVM heap size
java -Xms512m -Xmx2g -jar tigateway.jar
```

### 1.2 Routes Not Working

#### Symptoms
- Requests return 404 Not Found
- Routes not matching
- Services not reachable

#### Diagnosis
```bash
# Check route configuration
curl http://localhost:8080/actuator/gateway/routes

# Test route matching
curl -v http://localhost:8080/api/users/123

# Check service discovery
curl http://localhost:8080/actuator/health
```

#### Solutions

**Route Configuration Issues:**
```yaml
# Check route configuration
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: http://user-service:8080  # Check if service is reachable
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
```

**Service Discovery Issues:**
```bash
# Check service registration
curl http://localhost:8761/eureka/apps

# Check Consul services
curl http://localhost:8500/v1/catalog/services

# Check Kubernetes services
kubectl get services
kubectl get endpoints
```

### 1.3 Authentication Failures

#### Symptoms
- 401 Unauthorized errors
- JWT token validation failures
- Authentication service unavailable

#### Diagnosis
```bash
# Check authentication logs
tail -f logs/tigateway.log | grep -i "auth"

# Test JWT token
curl -H "Authorization: Bearer <token>" http://localhost:8080/actuator/health

# Check authentication service
curl http://auth-service:8080/actuator/health
```

#### Solutions

**JWT Token Issues:**
```bash
# Decode JWT token
echo "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." | base64 -d

# Check token expiration
jwt-decode <token>

# Verify JWT secret
openssl rand -base64 32
```

**Authentication Service Issues:**
```bash
# Check authentication service health
curl http://auth-service:8080/actuator/health

# Check authentication service logs
kubectl logs deployment/auth-service

# Restart authentication service
kubectl rollout restart deployment/auth-service
```

## 2. Deployment Issues

### 2.1 Kubernetes Deployment Issues

#### Symptoms
- Pods not starting
- Services not accessible
- ConfigMaps not loading

#### Diagnosis
```bash
# Check pod status
kubectl get pods -n tigateway
kubectl describe pod <pod-name> -n tigateway

# Check service status
kubectl get svc -n tigateway
kubectl describe svc tigateway -n tigateway

# Check ConfigMap
kubectl get configmap -n tigateway
kubectl describe configmap tigateway-config -n tigateway
```

#### Solutions

**Pod Startup Issues:**
```bash
# Check pod logs
kubectl logs <pod-name> -n tigateway

# Check resource limits
kubectl describe pod <pod-name> -n tigateway | grep -A 5 "Limits:"

# Check image pull issues
kubectl describe pod <pod-name> -n tigateway | grep -A 5 "Events:"
```

**Service Connectivity Issues:**
```bash
# Check service endpoints
kubectl get endpoints -n tigateway

# Test service connectivity
kubectl exec -it <pod-name> -n tigateway -- curl http://tigateway:8080/actuator/health

# Check network policies
kubectl get networkpolicy -n tigateway
```

### 2.2 Docker Deployment Issues

#### Symptoms
- Container not starting
- Port mapping issues
- Volume mount problems

#### Diagnosis
```bash
# Check container status
docker ps -a
docker logs <container-id>

# Check port mapping
docker port <container-id>

# Check volume mounts
docker inspect <container-id> | grep -A 10 "Mounts"
```

#### Solutions

**Container Startup Issues:**
```bash
# Check container logs
docker logs <container-id>

# Check image
docker images | grep tigateway

# Rebuild image
docker build -t tigateway:latest .
```

**Port Mapping Issues:**
```bash
# Check port availability
netstat -tulpn | grep :8080

# Change port mapping
docker run -p 8081:8080 tigateway:latest
```

### 2.3 Helm Deployment Issues

#### Symptoms
- Helm chart installation fails
- Values not applied correctly
- Upgrade issues

#### Diagnosis
```bash
# Check Helm releases
helm list -n tigateway

# Check Helm values
helm get values tigateway -n tigateway

# Check Helm status
helm status tigateway -n tigateway
```

#### Solutions

**Installation Issues:**
```bash
# Check Helm chart
helm lint ./helm/tigateway

# Install with debug
helm install tigateway ./helm/tigateway -n tigateway --debug --dry-run

# Install with specific values
helm install tigateway ./helm/tigateway -n tigateway -f values.yaml
```

**Upgrade Issues:**
```bash
# Check upgrade history
helm history tigateway -n tigateway

# Rollback to previous version
helm rollback tigateway 1 -n tigateway

# Upgrade with specific values
helm upgrade tigateway ./helm/tigateway -n tigateway -f values.yaml
```

## 3. Configuration Issues

### 3.1 Route Configuration Problems

#### Symptoms
- Routes not matching
- Incorrect routing behavior
- Filter chain issues

#### Diagnosis
```bash
# Check route configuration
curl http://localhost:8080/actuator/gateway/routes

# Test route matching
curl -v http://localhost:8080/api/users/123

# Check route order
curl http://localhost:8080/actuator/gateway/routes | jq '.[] | {id, order}'
```

#### Solutions

**Route Matching Issues:**
```yaml
# Check predicate configuration
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: http://user-service:8080
          predicates:
            - Path=/api/users/**  # Check path pattern
            - Method=GET,POST     # Check HTTP methods
            - Header=X-API-Version,v1  # Check header values
```

**Filter Chain Issues:**
```yaml
# Check filter order and configuration
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: http://user-service:8080
          filters:
            - StripPrefix=2  # Check prefix stripping
            - AddRequestHeader=X-Service,user-service  # Check header addition
            - CircuitBreaker=user-service-cb,forward:/fallback  # Check circuit breaker
```

### 3.2 Service Discovery Configuration

#### Symptoms
- Services not discovered
- Load balancing not working
- Service health check failures

#### Diagnosis
```bash
# Check service discovery
curl http://localhost:8080/actuator/health

# Check service registry
curl http://localhost:8761/eureka/apps

# Check service health
curl http://user-service:8080/actuator/health
```

#### Solutions

**Service Registration Issues:**
```yaml
# Check service registration configuration
spring:
  cloud:
    discovery:
      locator:
        enabled: true
        lower-case-service-id: true
        predicates:
          - name: Path
            args:
              pattern: "'/'+serviceId+'/**'"
```

**Load Balancing Issues:**
```yaml
# Check load balancing configuration
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: lb://user-service  # Check service name
          predicates:
            - Path=/api/users/**
```

### 3.3 Security Configuration Issues

#### Symptoms
- Authentication failures
- Authorization errors
- SSL/TLS issues

#### Diagnosis
```bash
# Check security configuration
curl http://localhost:8080/actuator/configprops | grep -i security

# Test SSL connection
openssl s_client -connect localhost:8080 -servername localhost

# Check certificate
openssl x509 -in certificate.crt -text -noout
```

#### Solutions

**SSL/TLS Issues:**
```yaml
# Check SSL configuration
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tigateway
```

**Authentication Configuration:**
```yaml
# Check authentication configuration
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${OAUTH2_JWK_SET_URI}
          issuer-uri: ${OAUTH2_ISSUER_URI}
```

## 4. Performance Issues

### 4.1 High Response Times

#### Symptoms
- Slow API responses
- High latency
- Timeout errors

#### Diagnosis
```bash
# Check response time metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Check JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Check system metrics
curl http://localhost:8080/actuator/metrics/system.cpu.usage
```

#### Solutions

**JVM Tuning:**
```bash
# Increase heap size
java -Xms1g -Xmx4g -jar tigateway.jar

# Use G1GC
java -XX:+UseG1GC -jar tigateway.jar

# Tune GC parameters
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar tigateway.jar
```

**Connection Pool Tuning:**
```yaml
# Tune HTTP client connection pool
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 1000
        response-timeout: 5000
        pool:
          max-connections: 500
          max-idle-time: 30s
          max-life-time: 60s
```

### 4.2 High Memory Usage

#### Symptoms
- Out of memory errors
- High memory consumption
- GC pressure

#### Diagnosis
```bash
# Check memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Check GC metrics
curl http://localhost:8080/actuator/metrics/jvm.gc.pause

# Generate heap dump
jcmd <pid> GC.run_finalization
jcmd <pid> VM.gc
jmap -dump:format=b,file=heap.hprof <pid>
```

#### Solutions

**Memory Optimization:**
```bash
# Increase heap size
java -Xms2g -Xmx8g -jar tigateway.jar

# Use G1GC for large heaps
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar tigateway.jar

# Tune GC parameters
java -XX:+UseG1GC -XX:G1HeapRegionSize=16m -jar tigateway.jar
```

**Application Optimization:**
```yaml
# Optimize caching
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000
      cache-null-values: false
```

### 4.3 High CPU Usage

#### Symptoms
- High CPU utilization
- Slow response times
- System overload

#### Diagnosis
```bash
# Check CPU usage
top -p <pid>
htop -p <pid>

# Check thread dump
jstack <pid>

# Check CPU metrics
curl http://localhost:8080/actuator/metrics/system.cpu.usage
```

#### Solutions

**CPU Optimization:**
```bash
# Use G1GC
java -XX:+UseG1GC -jar tigateway.jar

# Tune GC parameters
java -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -jar tigateway.jar

# Use parallel GC
java -XX:+UseParallelGC -jar tigateway.jar
```

**Application Optimization:**
```yaml
# Optimize thread pool
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
```

## 5. Security Issues

### 5.1 Authentication Failures

#### Symptoms
- 401 Unauthorized errors
- JWT token validation failures
- Authentication service unavailable

#### Diagnosis
```bash
# Check authentication logs
tail -f logs/tigateway.log | grep -i "auth"

# Test JWT token
curl -H "Authorization: Bearer <token>" http://localhost:8080/actuator/health

# Check authentication service
curl http://auth-service:8080/actuator/health
```

#### Solutions

**JWT Token Issues:**
```bash
# Decode JWT token
echo "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." | base64 -d

# Check token expiration
jwt-decode <token>

# Verify JWT secret
openssl rand -base64 32
```

**Authentication Service Issues:**
```bash
# Check authentication service health
curl http://auth-service:8080/actuator/health

# Check authentication service logs
kubectl logs deployment/auth-service

# Restart authentication service
kubectl rollout restart deployment/auth-service
```

### 5.2 Authorization Failures

#### Symptoms
- 403 Forbidden errors
- Permission denied
- Role-based access issues

#### Diagnosis
```bash
# Check authorization logs
tail -f logs/tigateway.log | grep -i "authz"

# Check user permissions
curl -H "Authorization: Bearer <token>" http://localhost:8080/actuator/health

# Check RBAC configuration
curl http://localhost:8080/actuator/configprops | grep -i rbac
```

#### Solutions

**RBAC Configuration:**
```yaml
# Check RBAC configuration
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${OAUTH2_JWK_SET_URI}
          issuer-uri: ${OAUTH2_ISSUER_URI}
```

**Permission Issues:**
```bash
# Check user roles
curl -H "Authorization: Bearer <token>" http://localhost:8080/actuator/health

# Check role mapping
curl http://localhost:8080/actuator/configprops | grep -i role
```

### 5.3 SSL/TLS Issues

#### Symptoms
- SSL handshake failures
- Certificate validation errors
- TLS version issues

#### Diagnosis
```bash
# Test SSL connection
openssl s_client -connect localhost:8080 -servername localhost

# Check certificate
openssl x509 -in certificate.crt -text -noout

# Check SSL configuration
curl -v https://localhost:8080/actuator/health
```

#### Solutions

**Certificate Issues:**
```bash
# Check certificate expiration
openssl x509 -in certificate.crt -text -noout | grep -A 2 "Validity"

# Generate new certificate
keytool -genkeypair -alias tigateway -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 365
```

**SSL Configuration:**
```yaml
# Check SSL configuration
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tigateway
    protocols: TLSv1.2,TLSv1.3
```

## 6. Network Issues

### 6.1 Connectivity Issues

#### Symptoms
- Connection timeouts
- Network unreachable
- DNS resolution failures

#### Diagnosis
```bash
# Test connectivity
ping user-service
telnet user-service 8080

# Check DNS resolution
nslookup user-service
dig user-service

# Check network routes
traceroute user-service
```

#### Solutions

**DNS Issues:**
```bash
# Check DNS configuration
cat /etc/resolv.conf

# Test DNS resolution
nslookup user-service
dig user-service

# Check service discovery
curl http://localhost:8761/eureka/apps
```

**Network Configuration:**
```yaml
# Check network configuration
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 1000
        response-timeout: 5000
```

### 6.2 Load Balancing Issues

#### Symptoms
- Uneven load distribution
- Service instances not receiving traffic
- Load balancer health check failures

#### Diagnosis
```bash
# Check service instances
curl http://localhost:8761/eureka/apps/user-service

# Check load balancer health
curl http://localhost:8080/actuator/health

# Test load balancing
for i in {1..10}; do curl http://localhost:8080/api/users/123; done
```

#### Solutions

**Load Balancer Configuration:**
```yaml
# Check load balancer configuration
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
```

**Service Health Checks:**
```yaml
# Check service health configuration
spring:
  cloud:
    loadbalancer:
      health-check:
        enabled: true
        path: /actuator/health
        interval: 10s
```

## 7. Monitoring and Debugging

### 7.1 Health Checks

#### Symptoms
- Health check failures
- Service not responding
- Monitoring alerts

#### Diagnosis
```bash
# Check health status
curl http://localhost:8080/actuator/health

# Check specific health indicators
curl http://localhost:8080/actuator/health/db
curl http://localhost:8080/actuator/health/redis

# Check health details
curl http://localhost:8080/actuator/health | jq '.'
```

#### Solutions

**Health Check Configuration:**
```yaml
# Check health check configuration
management:
  endpoint:
    health:
      show-details: always
      show-components: always
  health:
    defaults:
      enabled: true
    db:
      enabled: true
    redis:
      enabled: true
```

### 7.2 Metrics Collection

#### Symptoms
- Metrics not appearing
- Prometheus scraping failures
- Grafana dashboard issues

#### Diagnosis
```bash
# Check metrics endpoint
curl http://localhost:8080/actuator/prometheus

# Check Prometheus targets
curl http://prometheus:9090/api/v1/targets

# Check metrics configuration
curl http://localhost:8080/actuator/metrics
```

#### Solutions

**Metrics Configuration:**
```yaml
# Check metrics configuration
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

**Prometheus Configuration:**
```yaml
# Check Prometheus configuration
scrape_configs:
  - job_name: 'tigateway'
    static_configs:
      - targets: ['tigateway:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
```

### 7.3 Logging Issues

#### Symptoms
- Logs not appearing
- Log level issues
- Log rotation problems

#### Diagnosis
```bash
# Check log files
ls -la logs/
tail -f logs/tigateway.log

# Check log configuration
curl http://localhost:8080/actuator/loggers

# Check log level
curl http://localhost:8080/actuator/loggers/ti.gateway
```

#### Solutions

**Log Configuration:**
```yaml
# Check log configuration
logging:
  level:
    ti.gateway: INFO
    org.springframework.cloud.gateway: INFO
  file:
    name: logs/tigateway.log
    max-size: 100MB
    max-history: 30
```

**Log Rotation:**
```bash
# Check log rotation
logrotate -d /etc/logrotate.d/tigateway

# Force log rotation
logrotate -f /etc/logrotate.d/tigateway
```

## 8. Log Analysis

### 8.1 Common Log Patterns

#### Error Patterns
```bash
# Authentication errors
grep -i "authentication" logs/tigateway.log

# Authorization errors
grep -i "authorization" logs/tigateway.log

# Connection errors
grep -i "connection" logs/tigateway.log

# Timeout errors
grep -i "timeout" logs/tigateway.log
```

#### Performance Patterns
```bash
# Slow requests
grep -i "slow" logs/tigateway.log

# High memory usage
grep -i "memory" logs/tigateway.log

# GC issues
grep -i "gc" logs/tigateway.log
```

### 8.2 Log Analysis Tools

#### ELK Stack
```bash
# Check Elasticsearch
curl http://elasticsearch:9200/_cluster/health

# Check Logstash
curl http://logstash:9600/_node/stats

# Check Kibana
curl http://kibana:5601/api/status
```

#### Fluentd
```bash
# Check Fluentd status
curl http://fluentd:24220/api/plugins.json

# Check Fluentd logs
tail -f /var/log/fluentd/fluentd.log
```

## 9. Recovery Procedures

### 9.1 Service Recovery

#### Restart Services
```bash
# Restart TiGateway
kubectl rollout restart deployment/tigateway -n tigateway

# Restart with Docker
docker restart tigateway-container

# Restart with systemd
sudo systemctl restart tigateway
```

#### Rollback Procedures
```bash
# Rollback Kubernetes deployment
kubectl rollout undo deployment/tigateway -n tigateway

# Rollback Helm release
helm rollback tigateway 1 -n tigateway

# Rollback Docker image
docker tag tigateway:previous tigateway:latest
```

### 9.2 Data Recovery

#### Configuration Recovery
```bash
# Restore configuration from backup
kubectl apply -f config-backup.yaml

# Restore from Git
git checkout HEAD~1 -- application.yml

# Restore from ConfigMap
kubectl create configmap tigateway-config --from-file=application.yml
```

#### Database Recovery
```bash
# Restore database from backup
pg_restore -d tigateway_db backup.sql

# Restore Redis data
redis-cli --rdb backup.rdb
```

### 9.3 Disaster Recovery

#### Backup Procedures
```bash
# Backup configuration
kubectl get configmap tigateway-config -o yaml > config-backup.yaml

# Backup database
pg_dump tigateway_db > backup.sql

# Backup Redis
redis-cli BGSAVE
```

#### Recovery Testing
```bash
# Test backup restoration
kubectl apply -f config-backup.yaml

# Test database restoration
psql tigateway_db < backup.sql

# Test Redis restoration
redis-cli --rdb backup.rdb
```

## 10. Best Practices

### 10.1 Prevention

#### Regular Maintenance
- **Monitor system health**: Set up comprehensive monitoring
- **Regular backups**: Backup configuration and data regularly
- **Update dependencies**: Keep dependencies up to date
- **Test changes**: Test changes in staging environment
- **Document procedures**: Document troubleshooting procedures

#### Configuration Management
- **Version control**: Use version control for configuration
- **Environment separation**: Separate configuration by environment
- **Validation**: Validate configuration before deployment
- **Rollback plans**: Have rollback plans ready
- **Change tracking**: Track all configuration changes

### 10.2 Monitoring

#### Health Monitoring
- **Health checks**: Implement comprehensive health checks
- **Metrics collection**: Collect relevant metrics
- **Alerting**: Set up appropriate alerting
- **Logging**: Implement structured logging
- **Tracing**: Use distributed tracing

#### Performance Monitoring
- **Response times**: Monitor response times
- **Throughput**: Monitor request throughput
- **Resource usage**: Monitor CPU, memory, disk usage
- **Error rates**: Monitor error rates
- **Capacity planning**: Plan for capacity growth

### 10.3 Security

#### Security Monitoring
- **Authentication failures**: Monitor authentication failures
- **Authorization failures**: Monitor authorization failures
- **Suspicious activity**: Monitor for suspicious activity
- **Security events**: Log security events
- **Vulnerability scanning**: Regular vulnerability scanning

#### Security Best Practices
- **Least privilege**: Use principle of least privilege
- **Regular updates**: Keep security patches up to date
- **Access control**: Implement proper access control
- **Encryption**: Use encryption for sensitive data
- **Audit logging**: Implement audit logging

## Next Steps

After resolving issues:

1. **[Monitoring Setup](../monitoring-and-metrics.md)** - Set up comprehensive monitoring
2. **[Security Best Practices](../security-best-practices.md)** - Implement security measures
3. **[Performance Tuning](../performance-tuning.md)** - Optimize performance
4. **[Production Deployment](../deployment/kubernetes.md)** - Deploy in production

---

**Need more help?** Check out our [Monitoring Setup](../monitoring-and-metrics.md) guide for comprehensive monitoring solutions.
