# TiGateway Monitoring and Observability

## Overview

This document describes how to configure monitoring and observability features in TiGateway, including metrics collection, logging, tracing, health checks, and alerting.

## Metrics Configuration

### Prometheus Integration

#### Basic Configuration

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
        step: 30s
        descriptions: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
      slo:
        http.server.requests: 50ms, 100ms, 200ms, 500ms
```

#### Advanced Prometheus Configuration

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
        step: 30s
        descriptions: true
        histogram-flavor: prometheus
        pushgateway:
          enabled: false
          base-url: http://localhost:9091
          job: tigateway
          grouping-key:
            instance: ${spring.application.name}
    tags:
      application: ${spring.application.name}
      region: ${spring.cloud.kubernetes.discovery.region:unknown}
      zone: ${spring.cloud.kubernetes.discovery.zone:unknown}
```

### Custom Metrics

#### Creating Custom Metrics

```java
@Component
public class CustomMetrics {
    
    private final Counter customRequestCounter;
    private final Timer customRequestTimer;
    private final Gauge customActiveConnections;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.customRequestCounter = Counter.builder("tigateway.custom.requests")
            .description("Custom request counter")
            .tag("service", "tigateway")
            .register(meterRegistry);
            
        this.customRequestTimer = Timer.builder("tigateway.custom.request.duration")
            .description("Custom request duration")
            .register(meterRegistry);
            
        this.customActiveConnections = Gauge.builder("tigateway.custom.active.connections")
            .description("Active connections")
            .register(meterRegistry, this, CustomMetrics::getActiveConnections);
    }
    
    public void incrementRequestCounter() {
        customRequestCounter.increment();
    }
    
    public void recordRequestDuration(Duration duration) {
        customRequestTimer.record(duration);
    }
    
    private double getActiveConnections() {
        // Implementation to get active connections
        return 0.0;
    }
}
```

#### Using Custom Metrics in Filters

```java
@Component
public class MetricsFilter implements GlobalFilter, Ordered {
    
    private final CustomMetrics customMetrics;
    
    public MetricsFilter(CustomMetrics customMetrics) {
        this.customMetrics = customMetrics;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange)
            .doOnSuccess(result -> {
                long duration = System.currentTimeMillis() - startTime;
                customMetrics.recordRequestDuration(Duration.ofMillis(duration));
                customMetrics.incrementRequestCounter();
            });
    }
    
    @Override
    public int getOrder() {
        return -100;
    }
}
```

### Grafana Dashboard

#### Dashboard Configuration

```json
{
  "dashboard": {
    "title": "TiGateway Monitoring",
    "panels": [
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])",
            "legendFormat": "{{uri}}"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))",
            "legendFormat": "95th percentile"
          }
        ]
      }
    ]
  }
}
```

## Logging Configuration

### Structured Logging

#### Logback Configuration

```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="!prod">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
    
    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>/app/logs/tigateway.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>/app/logs/tigateway.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

#### Application Logging Configuration

```yaml
logging:
  level:
    ti.gateway: INFO
    org.springframework.cloud.gateway: DEBUG
    org.springframework.web.reactive: DEBUG
    reactor.netty: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### ELK Stack Integration

#### Logstash Configuration

```ruby
# logstash.conf
input {
  beats {
    port => 5044
  }
}

filter {
  if [fields][service] == "tigateway" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:thread}\] %{LOGLEVEL:level} %{DATA:logger} - %{GREEDYDATA:message}" }
    }
    
    date {
      match => [ "timestamp", "yyyy-MM-dd HH:mm:ss" ]
    }
    
    mutate {
      add_field => { "service" => "tigateway" }
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "tigateway-logs-%{+YYYY.MM.dd}"
  }
}
```

#### Filebeat Configuration

```yaml
# filebeat.yml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /app/logs/tigateway*.log
  fields:
    service: tigateway
  fields_under_root: true

output.logstash:
  hosts: ["logstash:5044"]

processors:
  - add_host_metadata:
      when.not.contains.tags: forwarded
```

## Tracing Configuration

### Zipkin Integration

#### Basic Configuration

```yaml
spring:
  sleuth:
    zipkin:
      base-url: http://zipkin:9411
    sampler:
      probability: 0.1
  cloud:
    gateway:
      httpclient:
        wiretap: true
      httpserver:
        wiretap: true
```

#### Advanced Tracing Configuration

```yaml
spring:
  sleuth:
    zipkin:
      base-url: http://zipkin:9411
      sender:
        type: web
    sampler:
      probability: 0.1
    web:
      client:
        enabled: true
      server:
        enabled: true
    kafka:
      enabled: true
    redis:
      enabled: true
    jdbc:
      enabled: true
```

### Jaeger Integration

#### Configuration

```yaml
spring:
  sleuth:
    jaeger:
      endpoint: http://jaeger:14268/api/traces
      sampler:
        type: const
        param: 1
      sender:
        type: http
        endpoint: http://jaeger:14268/api/traces
```

#### Custom Tracing

```java
@Component
public class CustomTracingFilter implements GlobalFilter, Ordered {
    
    private final Tracer tracer;
    
    public CustomTracingFilter(Tracer tracer) {
        this.tracer = tracer;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Span span = tracer.nextSpan()
            .name("gateway-request")
            .tag("http.method", exchange.getRequest().getMethod().name())
            .tag("http.url", exchange.getRequest().getURI().toString())
            .start();
            
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            return chain.filter(exchange)
                .doOnSuccess(result -> {
                    span.tag("http.status_code", 
                        String.valueOf(exchange.getResponse().getStatusCode().value()));
                })
                .doOnError(error -> {
                    span.tag("error", true);
                    span.tag("error.message", error.getMessage());
                })
                .doFinally(signalType -> span.end());
        }
    }
    
    @Override
    public int getOrder() {
        return -200;
    }
}
```

## Health Checks

### Custom Health Indicators

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RestTemplate restTemplate;
    
    public CustomHealthIndicator(RedisTemplate<String, String> redisTemplate,
                                RestTemplate restTemplate) {
        this.redisTemplate = redisTemplate;
        this.restTemplate = restTemplate;
    }
    
    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        // Check Redis connection
        try {
            redisTemplate.opsForValue().get("health-check");
            builder.withDetail("redis", "UP");
        } catch (Exception e) {
            builder.down()
                .withDetail("redis", "DOWN")
                .withDetail("redis.error", e.getMessage());
        }
        
        // Check external service
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                "http://external-service/health", String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                builder.withDetail("external-service", "UP");
            } else {
                builder.down()
                    .withDetail("external-service", "DOWN")
                    .withDetail("external-service.status", response.getStatusCode().value());
            }
        } catch (Exception e) {
            builder.down()
                .withDetail("external-service", "DOWN")
                .withDetail("external-service.error", e.getMessage());
        }
        
        return builder.build();
    }
}
```

### Kubernetes Health Checks

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tigateway
spec:
  template:
    spec:
      containers:
      - name: tigateway
        image: tigateway:latest
        ports:
        - containerPort: 8080
        - containerPort: 8081
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        startupProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 10
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 12
```

## Alerting

### Prometheus Alert Rules

```yaml
# alerts.yml
groups:
- name: tigateway
  rules:
  - alert: TiGatewayDown
    expr: up{job="tigateway"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "TiGateway instance is down"
      description: "TiGateway instance {{ $labels.instance }} has been down for more than 1 minute."
      
  - alert: HighErrorRate
    expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) > 0.05
    for: 2m
    labels:
      severity: warning
    annotations:
      summary: "High error rate detected"
      description: "Error rate is {{ $value | humanizePercentage }} for the last 5 minutes."
      
  - alert: HighResponseTime
    expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High response time detected"
      description: "95th percentile response time is {{ $value }}s for the last 5 minutes."
```

### Alertmanager Configuration

```yaml
# alertmanager.yml
global:
  smtp_smarthost: 'localhost:587'
  smtp_from: 'alerts@tigateway.com'

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'

receivers:
- name: 'web.hook'
  webhook_configs:
  - url: 'http://webhook:5001/'
    send_resolved: true

- name: 'email'
  email_configs:
  - to: 'admin@tigateway.com'
    subject: 'TiGateway Alert: {{ .GroupLabels.alertname }}'
    body: |
      {{ range .Alerts }}
      Alert: {{ .Annotations.summary }}
      Description: {{ .Annotations.description }}
      {{ end }}
```

## Performance Monitoring

### JVM Metrics

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,heapdump
  metrics:
    export:
      prometheus:
        enabled: true
    jvm:
      memory:
        enabled: true
      gc:
        enabled: true
      threads:
        enabled: true
```

### Custom Performance Metrics

```java
@Component
public class PerformanceMetrics {
    
    private final Timer requestTimer;
    private final Counter errorCounter;
    private final Gauge activeConnections;
    
    public PerformanceMetrics(MeterRegistry meterRegistry) {
        this.requestTimer = Timer.builder("tigateway.performance.request.duration")
            .description("Request processing duration")
            .register(meterRegistry);
            
        this.errorCounter = Counter.builder("tigateway.performance.errors")
            .description("Error count")
            .register(meterRegistry);
            
        this.activeConnections = Gauge.builder("tigateway.performance.active.connections")
            .description("Active connections")
            .register(meterRegistry, this, PerformanceMetrics::getActiveConnections);
    }
    
    public void recordRequestDuration(Duration duration) {
        requestTimer.record(duration);
    }
    
    public void incrementErrorCount() {
        errorCounter.increment();
    }
    
    private double getActiveConnections() {
        // Implementation to get active connections
        return 0.0;
    }
}
```

## Monitoring Best Practices

### 1. Metric Naming

- Use consistent naming conventions
- Include service name prefix
- Use descriptive names
- Follow Prometheus naming guidelines

### 2. Logging Best Practices

- Use structured logging
- Include correlation IDs
- Log at appropriate levels
- Avoid logging sensitive data

### 3. Tracing Best Practices

- Use consistent span names
- Include relevant tags
- Set appropriate sampling rates
- Correlate with logs and metrics

### 4. Health Check Best Practices

- Check all critical dependencies
- Provide detailed health information
- Use appropriate timeouts
- Implement graceful degradation

---

**Related Documentation**:
- [Configuration Properties](../configuration/configuration-properties.md)
- [Performance Tuning](../performance-tuning.md)
- [Troubleshooting Guide](../troubleshooting.md)
- [Kubernetes Deployment](./kubernetes.md)
