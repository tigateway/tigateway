# Docker Deployment

This document describes how to deploy TiGateway using Docker.

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+

## Quick Start

### 1. Pull Image

```bash
docker pull tigateway/tigateway:latest
```

### 2. Run Container

```bash
docker run -d \
  --name tigateway \
  -p 8080:8080 \
  -p 9090:9090 \
  -e SPRING_PROFILES_ACTIVE=docker \
  tigateway/tigateway:latest
```

### 3. Verify Deployment

```bash
curl http://localhost:8080/actuator/health
```

## Docker Compose Deployment

Create a `docker-compose.yml` file:

```yaml
version: '3.8'

services:
  tigateway:
    image: tigateway/tigateway:latest
    container_name: tigateway
    ports:
      - "8080:8080"
      - "9090:9090"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_CLOUD_GATEWAY_ROUTES[0].ID=user-service
      - SPRING_CLOUD_GATEWAY_ROUTES[0].URI=lb://user-service
      - SPRING_CLOUD_GATEWAY_ROUTES[0].PREDICATES[0]=Path=/api/users/**
      - SPRING_CLOUD_GATEWAY_ROUTES[0].FILTERS[0]=StripPrefix=2
    volumes:
      - ./config:/app/config
      - ./logs:/app/logs
    networks:
      - tigateway-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  redis:
    image: redis:7-alpine
    container_name: tigateway-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - tigateway-network
    restart: unless-stopped

  user-service:
    image: user-service:latest
    container_name: user-service
    ports:
      - "8081:8080"
    networks:
      - tigateway-network
    restart: unless-stopped

volumes:
  redis-data:

networks:
  tigateway-network:
    driver: bridge
```

### Start Services

```bash
docker-compose up -d
```

### Stop Services

```bash
docker-compose down
```

## Production Deployment

### 1. Production Docker Compose

```yaml
version: '3.8'

services:
  tigateway:
    image: tigateway/tigateway:1.0.0
    container_name: tigateway
    ports:
      - "8080:8080"
      - "9090:9090"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
      - SPRING_CLOUD_GATEWAY_ROUTES[0].ID=user-service
      - SPRING_CLOUD_GATEWAY_ROUTES[0].URI=lb://user-service
      - SPRING_CLOUD_GATEWAY_ROUTES[0].PREDICATES[0]=Path=/api/users/**
      - SPRING_CLOUD_GATEWAY_ROUTES[0].FILTERS[0]=StripPrefix=2
      - SPRING_CLOUD_GATEWAY_ROUTES[0].FILTERS[1]=CircuitBreaker=user-service-cb
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - SPRING_REDIS_PASSWORD=${REDIS_PASSWORD}
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
      - MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=always
    volumes:
      - ./config:/app/config:ro
      - ./logs:/app/logs
      - /etc/localtime:/etc/localtime:ro
    networks:
      - tigateway-network
    restart: unless-stopped
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 1G
        reservations:
          cpus: '1.0'
          memory: 512M
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  redis:
    image: redis:7-alpine
    container_name: tigateway-redis
    ports:
      - "6379:6379"
    environment:
      - REDIS_PASSWORD=${REDIS_PASSWORD}
    command: redis-server --requirepass ${REDIS_PASSWORD} --appendonly yes
    volumes:
      - redis-data:/data
      - ./redis.conf:/usr/local/etc/redis/redis.conf:ro
    networks:
      - tigateway-network
    restart: unless-stopped
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  nginx:
    image: nginx:alpine
    container_name: tigateway-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
      - nginx-logs:/var/log/nginx
    networks:
      - tigateway-network
    restart: unless-stopped
    depends_on:
      - tigateway
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

volumes:
  redis-data:
  nginx-logs:

networks:
  tigateway-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

### 2. Environment Configuration

Create a `.env` file:

```bash
# Redis Configuration
REDIS_PASSWORD=your_secure_redis_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key

# Database Configuration
DB_HOST=postgres
DB_PORT=5432
DB_NAME=tigateway
DB_USER=tigateway
DB_PASSWORD=your_db_password

# Monitoring Configuration
PROMETHEUS_ENABLED=true
GRAFANA_ENABLED=true
```

### 3. Nginx Configuration

Create `nginx.conf`:

```nginx
events {
    worker_connections 1024;
}

http {
    upstream tigateway {
        server tigateway:8080;
    }

    server {
        listen 80;
        server_name _;

        # Redirect HTTP to HTTPS
        return 301 https://$host$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name _;

        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
        ssl_prefer_server_ciphers off;

        # Security headers
        add_header X-Frame-Options DENY;
        add_header X-Content-Type-Options nosniff;
        add_header X-XSS-Protection "1; mode=block";
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

        # Rate limiting
        limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
        limit_req zone=api burst=20 nodelay;

        location / {
            proxy_pass http://tigateway;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_connect_timeout 30s;
            proxy_send_timeout 30s;
            proxy_read_timeout 30s;
        }

        location /health {
            proxy_pass http://tigateway/actuator/health;
            access_log off;
        }

        location /metrics {
            proxy_pass http://tigateway/actuator/prometheus;
            access_log off;
        }
    }
}
```

## Configuration Management

### 1. External Configuration

Create a `config/application.yml` file:

```yaml
spring:
  profiles:
    active: production
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: user-service-cb
                fallbackUri: forward:/fallback/user-service
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: order-service-cb
                fallbackUri: forward:/fallback/order-service
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
            allowCredentials: true
  redis:
    host: redis
    port: 6379
    password: ${REDIS_PASSWORD}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    com.tigateway: INFO
    org.springframework.cloud.gateway: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /app/logs/tigateway.log
    max-size: 100MB
    max-history: 30
```

### 2. Environment-Specific Configuration

Create `config/application-docker.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://user-service:8080
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
  redis:
    host: redis
    port: 6379
```

Create `config/application-production.yml`:

```yaml
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
            - name: CircuitBreaker
              args:
                name: user-service-cb
                fallbackUri: forward:/fallback/user-service
  redis:
    host: redis
    port: 6379
    password: ${REDIS_PASSWORD}
    ssl: true
```

## Monitoring and Logging

### 1. Health Checks

```yaml
# Health check configuration
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

### 2. Logging Configuration

```yaml
# Logging configuration
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

### 3. Metrics Collection

```yaml
# Metrics configuration
environment:
  - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
  - MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=always
```

## Security Configuration

### 1. SSL/TLS Configuration

```yaml
# SSL configuration
volumes:
  - ./ssl:/app/ssl:ro
environment:
  - SERVER_SSL_ENABLED=true
  - SERVER_SSL_KEY_STORE=/app/ssl/keystore.p12
  - SERVER_SSL_KEY_STORE_PASSWORD=${SSL_KEY_STORE_PASSWORD}
  - SERVER_SSL_KEY_STORE_TYPE=PKCS12
```

### 2. Security Headers

```yaml
# Security headers in nginx
add_header X-Frame-Options DENY;
add_header X-Content-Type-Options nosniff;
add_header X-XSS-Protection "1; mode=block";
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
```

## Troubleshooting

### 1. Common Issues

#### Container Won't Start

```bash
# Check container logs
docker logs tigateway

# Check container status
docker ps -a

# Check resource usage
docker stats tigateway
```

#### Health Check Failing

```bash
# Check health endpoint directly
curl http://localhost:8080/actuator/health

# Check container health
docker inspect tigateway | grep Health -A 10
```

#### Connection Issues

```bash
# Check network connectivity
docker network ls
docker network inspect tigateway-network

# Test connectivity between containers
docker exec tigateway ping redis
```

### 2. Debug Commands

```bash
# Enter container
docker exec -it tigateway /bin/bash

# Check configuration
docker exec tigateway cat /app/config/application.yml

# Check logs
docker exec tigateway tail -f /app/logs/tigateway.log

# Check environment variables
docker exec tigateway env
```

### 3. Performance Tuning

```yaml
# Resource limits
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 1G
    reservations:
      cpus: '1.0'
      memory: 512M

# JVM tuning
environment:
  - JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

## Best Practices

### 1. Container Security

- Use specific image tags instead of `latest`
- Run containers as non-root user
- Use read-only filesystems where possible
- Regularly update base images

### 2. Resource Management

- Set appropriate resource limits
- Monitor resource usage
- Use health checks for automatic recovery
- Implement proper logging rotation

### 3. Configuration Management

- Use environment variables for sensitive data
- Store configuration in external files
- Use secrets management for passwords
- Implement configuration validation

### 4. Monitoring and Observability

- Enable health checks
- Configure proper logging
- Set up metrics collection
- Implement distributed tracing

---

**Related Documentation**:
- [Kubernetes Deployment](./kubernetes.md)
- [Helm Deployment](./helm.md)
- [Monitoring Configuration](./monitoring.md)
