# Logging and Tracing

This guide covers TiGateway's logging and distributed tracing capabilities, including structured logging, log aggregation, distributed tracing, and monitoring integration.

## Overview

TiGateway provides comprehensive logging and tracing features:

- **Structured Logging**: JSON-formatted logs with consistent structure
- **Log Aggregation**: Centralized log collection and analysis
- **Distributed Tracing**: End-to-end request tracing across services
- **Correlation IDs**: Request correlation across service boundaries
- **Log Levels**: Configurable log levels for different components
- **Performance Monitoring**: Log-based performance analysis

## Logging Configuration

### Basic Logging Configuration

```yaml
logging:
  level:
    ti.gateway: INFO
    org.springframework.cloud.gateway: INFO
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/tigateway.log
    max-size: 100MB
    max-history: 30
```

### Structured Logging

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId},%X{spanId}] %-5level %logger{36} - %msg%n"
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
      total-size-cap: 1GB
```

### JSON Logging

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
  logback:
    configuration: classpath:logback-spring.xml
```

### Logback Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
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
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/tigateway.log</file>
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
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/tigateway.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

## Request Logging

### Request Logging Filter

```java
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        
        // Add request ID to MDC
        MDC.put("requestId", requestId);
        MDC.put("traceId", requestId);
        MDC.put("spanId", requestId);
        
        // Add request ID to headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .build();
        
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        
        log.info("Request started: {} {} - ID: {}", 
                request.getMethod(), request.getURI(), requestId);
        
        return chain.filter(modifiedExchange)
                .then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    ServerHttpResponse response = exchange.getResponse();
                    
                    log.info("Request completed: {} {} - ID: {} - Status: {} - Duration: {}ms",
                            request.getMethod(), request.getURI(), requestId, 
                            response.getStatusCode(), duration);
                    
                    // Clear MDC
                    MDC.clear();
                }));
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

### Advanced Request Logging

```java
@Component
public class AdvancedRequestLoggingFilter implements GlobalFilter, Ordered {
    
    private static final Logger log = LoggerFactory.getLogger(AdvancedRequestLoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String requestId = UUID.randomUUID().toString();
        String traceId = request.getHeaders().getFirst("X-Trace-ID");
        String spanId = request.getHeaders().getFirst("X-Span-ID");
        
        if (traceId == null) {
            traceId = requestId;
        }
        if (spanId == null) {
            spanId = requestId;
        }
        
        // Add to MDC
        MDC.put("requestId", requestId);
        MDC.put("traceId", traceId);
        MDC.put("spanId", spanId);
        MDC.put("method", request.getMethod().name());
        MDC.put("path", request.getURI().getPath());
        MDC.put("clientIp", getClientIp(request));
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange)
                .doOnSuccess(result -> {
                    long duration = System.currentTimeMillis() - startTime;
                    ServerHttpResponse response = exchange.getResponse();
                    
                    MDC.put("status", response.getStatusCode().toString());
                    MDC.put("duration", String.valueOf(duration));
                    
                    log.info("Request completed successfully");
                    
                    MDC.clear();
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    
                    MDC.put("status", "ERROR");
                    MDC.put("duration", String.valueOf(duration));
                    MDC.put("error", error.getMessage());
                    
                    log.error("Request failed", error);
                    
                    MDC.clear();
                });
    }
    
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddress().getAddress().getHostAddress();
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

## Distributed Tracing

### Tracing Configuration

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

### Custom Tracing

```java
@Component
public class CustomTracingFilter implements GlobalFilter, Ordered {
    
    private final Tracer tracer;
    
    public CustomTracingFilter(Tracer tracer) {
        this.tracer = tracer;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        Span span = tracer.nextSpan()
                .name("gateway-request")
                .tag("http.method", request.getMethod().name())
                .tag("http.url", request.getURI().toString())
                .tag("http.path", request.getURI().getPath())
                .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            return chain.filter(exchange)
                    .doOnSuccess(result -> {
                        ServerHttpResponse response = exchange.getResponse();
                        span.tag("http.status_code", response.getStatusCode().toString());
                        span.end();
                    })
                    .doOnError(error -> {
                        span.tag("error", error.getMessage());
                        span.end();
                    });
        }
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

### Trace Context Propagation

```java
@Component
public class TraceContextPropagationFilter implements GlobalFilter, Ordered {
    
    private final Tracer tracer;
    
    public TraceContextPropagationFilter(Tracer tracer) {
        this.tracer = tracer;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Span currentSpan = tracer.currentSpan();
        
        if (currentSpan != null) {
            ServerHttpRequest request = exchange.getRequest();
            
            // Propagate trace context to downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Trace-ID", currentSpan.context().traceId())
                    .header("X-Span-ID", currentSpan.context().spanId())
                    .header("X-Parent-Span-ID", currentSpan.context().parentId())
                    .build();
            
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();
            
            return chain.filter(modifiedExchange);
        }
        
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

## Log Aggregation

### ELK Stack Integration

```yaml
logging:
  logback:
    configuration: classpath:logback-spring.xml
```

### Logback ELK Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
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
    
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>logstash:5000</destination>
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
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="LOGSTASH"/>
    </root>
</configuration>
```

### Fluentd Integration

```yaml
logging:
  logback:
    configuration: classpath:logback-fluentd.xml
```

### Logback Fluentd Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <appender name="FLUENT" class="ch.qos.logback.more.appenders.DataFluentAppender">
        <tag>tigateway</tag>
        <remoteHost>fluentd</remoteHost>
        <port>24224</port>
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
        <appender-ref ref="FLUENT"/>
    </root>
</configuration>
```

## Performance Monitoring

### Performance Logging

```java
@Component
public class PerformanceLoggingFilter implements GlobalFilter, Ordered {
    
    private static final Logger log = LoggerFactory.getLogger(PerformanceLoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        long startTime = System.nanoTime();
        
        return chain.filter(exchange)
                .doOnSuccess(result -> {
                    long duration = System.nanoTime() - startTime;
                    double durationMs = duration / 1_000_000.0;
                    
                    if (durationMs > 1000) { // Log slow requests
                        log.warn("Slow request detected: {} {} - Duration: {}ms", 
                                request.getMethod(), path, durationMs);
                    }
                    
                    log.debug("Request processed: {} {} - Duration: {}ms", 
                            request.getMethod(), path, durationMs);
                })
                .doOnError(error -> {
                    long duration = System.nanoTime() - startTime;
                    double durationMs = duration / 1_000_000.0;
                    
                    log.error("Request failed: {} {} - Duration: {}ms - Error: {}", 
                            request.getMethod(), path, durationMs, error.getMessage());
                });
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

### Metrics Integration

```java
@Component
public class MetricsLoggingFilter implements GlobalFilter, Ordered {
    
    private final MeterRegistry meterRegistry;
    private final Timer requestTimer;
    private final Counter requestCounter;
    
    public MetricsLoggingFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.requestTimer = Timer.builder("gateway.requests.duration")
                .description("Request duration")
                .register(meterRegistry);
        
        this.requestCounter = Counter.builder("gateway.requests.total")
                .description("Total requests")
                .register(meterRegistry);
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        return chain.filter(exchange)
                .doOnSuccess(result -> {
                    sample.stop(requestTimer.tags("path", path, "method", method, "status", "success"));
                    requestCounter.increment(Tags.of("path", path, "method", method, "status", "success"));
                })
                .doOnError(error -> {
                    sample.stop(requestTimer.tags("path", path, "method", method, "status", "error"));
                    requestCounter.increment(Tags.of("path", path, "method", method, "status", "error"));
                });
    }
    
    @Override
    public int getOrder() {
        return -1;
    }
}
```

## Best Practices

### Logging Best Practices

1. **Structured Logging**: Use structured logging with consistent fields
2. **Log Levels**: Use appropriate log levels (DEBUG, INFO, WARN, ERROR)
3. **Correlation IDs**: Include correlation IDs for request tracing
4. **Performance**: Avoid expensive operations in logging
5. **Security**: Don't log sensitive information

### Tracing Best Practices

1. **Sampling**: Use appropriate sampling rates for production
2. **Context Propagation**: Propagate trace context to downstream services
3. **Span Naming**: Use consistent span naming conventions
4. **Tags**: Add relevant tags to spans for better analysis
5. **Error Handling**: Handle tracing errors gracefully

### Monitoring Best Practices

1. **Centralized Logging**: Use centralized log aggregation
2. **Real-time Monitoring**: Monitor logs in real-time
3. **Alerting**: Set up alerts for critical errors
4. **Dashboards**: Create dashboards for log analysis
5. **Retention**: Configure appropriate log retention policies

## Troubleshooting

### Common Issues

#### Logs Not Appearing

```bash
# Check log configuration
curl http://localhost:8080/actuator/configprops | grep -i logging

# Check log files
ls -la logs/
tail -f logs/tigateway.log

# Check log level
curl http://localhost:8080/actuator/loggers
```

#### Tracing Issues

```bash
# Check tracing configuration
curl http://localhost:8080/actuator/configprops | grep -i tracing

# Check trace endpoints
curl http://localhost:8080/actuator/health

# Check Zipkin connection
curl http://zipkin:9411/api/v2/services
```

#### Performance Issues

```bash
# Check log performance
curl http://localhost:8080/actuator/metrics/logback.events

# Monitor log file size
du -h logs/tigateway.log

# Check disk space
df -h
```

### Debug Commands

```bash
# Check logging configuration
curl http://localhost:8080/actuator/loggers

# Change log level
curl -X POST http://localhost:8080/actuator/loggers/ti.gateway \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# Check tracing status
curl http://localhost:8080/actuator/health

# View recent logs
tail -f logs/tigateway.log | grep -i error
```

## Next Steps

After configuring logging and tracing:

1. **[Monitoring Setup](../monitoring-and-metrics.md)** - Set up comprehensive monitoring
2. **[Troubleshooting Guide](../troubleshooting.md)** - Common logging and tracing issues
3. **[Performance Tuning](../performance-tuning.md)** - Optimize logging performance
4. **[Security Best Practices](../security-best-practices.md)** - Secure logging practices

---

**Ready to set up monitoring?** Check out our [Monitoring Setup](../monitoring-and-metrics.md) guide for comprehensive monitoring solutions.
