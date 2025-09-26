# 监控和可观测性

本文档介绍如何配置 TiGateway 的监控和可观测性功能。

## 概述

TiGateway 提供了全面的监控和可观测性功能，包括：

- 指标监控 (Metrics)
- 日志收集 (Logging)
- 链路追踪 (Tracing)
- 健康检查 (Health Checks)

## 指标监控

### Prometheus 集成

TiGateway 内置了 Prometheus 指标支持。

#### 配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

#### 访问指标

```bash
# 获取 Prometheus 格式的指标
curl http://localhost:9090/actuator/prometheus
```

#### 关键指标

| 指标名称 | 描述 | 类型 |
|----------|------|------|
| `http_server_requests_seconds` | HTTP 请求耗时 | Histogram |
| `http_server_requests_total` | HTTP 请求总数 | Counter |
| `gateway_requests_total` | 网关请求总数 | Counter |
| `gateway_requests_duration_seconds` | 网关请求耗时 | Histogram |
| `gateway_route_requests_total` | 路由请求总数 | Counter |
| `jvm_memory_used_bytes` | JVM 内存使用量 | Gauge |
| `jvm_gc_pause_seconds` | GC 暂停时间 | Histogram |

### Grafana 仪表板

#### 导入仪表板

1. 下载仪表板 JSON 文件
2. 在 Grafana 中导入仪表板
3. 配置 Prometheus 数据源

#### 预置仪表板

- **TiGateway Overview**: 总体概览
- **TiGateway Routes**: 路由监控
- **TiGateway Performance**: 性能指标
- **JVM Metrics**: JVM 监控

## 日志收集

### 日志配置

```yaml
logging:
  level:
    root: INFO
    com.tigateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/tigateway/application.log
    max-size: 100MB
    max-history: 30
```

### 结构化日志

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
```

### ELK Stack 集成

#### Logstash 配置

```ruby
input {
  beats {
    port => 5044
  }
}

filter {
  if [fields][service] == "tigateway" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:thread}\] %{LOGLEVEL:level} \[%{DATA:traceId},%{DATA:spanId}\] %{DATA:logger} - %{GREEDYDATA:message}" }
    }
    
    date {
      match => [ "timestamp", "yyyy-MM-dd HH:mm:ss" ]
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

## 链路追踪

### Zipkin 集成

#### 配置

```yaml
spring:
  sleuth:
    zipkin:
      base-url: http://zipkin:9411
    sampler:
      probability: 1.0
```

#### 自定义追踪

```java
@RestController
public class GatewayController {
    
    @Autowired
    private Tracer tracer;
    
    @GetMapping("/api/example")
    public String example() {
        Span span = tracer.nextSpan()
            .name("custom-operation")
            .tag("operation", "example")
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            // 业务逻辑
            return "success";
        } finally {
            span.end();
        }
    }
}
```

### Jaeger 集成

#### 配置

```yaml
spring:
  sleuth:
    jaeger:
      endpoint: http://jaeger:14268/api/traces
    sampler:
      probability: 1.0
```

## 健康检查

### 内置健康检查

```yaml
management:
  endpoint:
    health:
      show-details: always
      show-components: always
  health:
    defaults:
      enabled: true
    redis:
      enabled: true
    db:
      enabled: true
```

### 自定义健康检查

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // 自定义健康检查逻辑
        if (isHealthy()) {
            return Health.up()
                .withDetail("status", "healthy")
                .withDetail("timestamp", System.currentTimeMillis())
                .build();
        } else {
            return Health.down()
                .withDetail("status", "unhealthy")
                .withDetail("error", "Service unavailable")
                .build();
        }
    }
    
    private boolean isHealthy() {
        // 实现健康检查逻辑
        return true;
    }
}
```

## 告警配置

### Prometheus 告警规则

```yaml
groups:
- name: tigateway
  rules:
  - alert: TiGatewayDown
    expr: up{job="tigateway"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "TiGateway is down"
      description: "TiGateway instance {{ $labels.instance }} is down"
  
  - alert: HighErrorRate
    expr: rate(http_server_requests_total{status=~"5.."}[5m]) > 0.1
    for: 2m
    labels:
      severity: warning
    annotations:
      summary: "High error rate"
      description: "Error rate is {{ $value }} errors per second"
  
  - alert: HighLatency
    expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High latency"
      description: "95th percentile latency is {{ $value }} seconds"
```

### Alertmanager 配置

```yaml
global:
  smtp_smarthost: 'localhost:587'
  smtp_from: 'alerts@example.com'

route:
  group_by: ['alertname']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'web.hook'

receivers:
- name: 'web.hook'
  email_configs:
  - to: 'admin@example.com'
    subject: 'TiGateway Alert: {{ .GroupLabels.alertname }}'
    body: |
      {{ range .Alerts }}
      Alert: {{ .Annotations.summary }}
      Description: {{ .Annotations.description }}
      {{ end }}
```

## 性能监控

### JVM 监控

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        jvm.gc.pause: true
        jvm.memory.used: true
```

### 自定义指标

```java
@Component
public class CustomMetrics {
    
    private final Counter customCounter;
    private final Timer customTimer;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.customCounter = Counter.builder("custom.operations.total")
            .description("Total number of custom operations")
            .register(meterRegistry);
            
        this.customTimer = Timer.builder("custom.operation.duration")
            .description("Duration of custom operations")
            .register(meterRegistry);
    }
    
    public void incrementCounter() {
        customCounter.increment();
    }
    
    public void recordTimer(Duration duration) {
        customTimer.record(duration);
    }
}
```

## 最佳实践

### 监控策略

1. **分层监控**: 应用层、系统层、基础设施层
2. **关键指标**: 延迟、吞吐量、错误率、饱和度
3. **告警策略**: 基于业务影响设置告警阈值
4. **数据保留**: 合理设置数据保留期

### 性能优化

1. **指标采样**: 合理设置采样率
2. **日志级别**: 生产环境使用 INFO 级别
3. **追踪采样**: 根据负载调整采样率
4. **资源监控**: 监控 CPU、内存、网络使用情况

### 故障排查

1. **日志分析**: 使用结构化日志便于分析
2. **指标趋势**: 观察指标变化趋势
3. **链路追踪**: 分析请求链路找出瓶颈
4. **健康检查**: 定期检查服务健康状态
