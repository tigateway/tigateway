# TiGateway Data Flow Design

## Overview

TiGateway adopts a cloud-native architecture with data flow design based on Kubernetes native resources, supporting configuration hot updates, service discovery, and dynamic routing. This document details the data flow process within the system.

## Overall Data Flow Architecture

```mermaid
graph TB
    subgraph "Configuration Data Flow"
        CM[ConfigMap]
        CRD[Custom Resources]
        K8S_API[Kubernetes API]
    end
    
    subgraph "TiGateway Core"
        BASE[ti-gateway-base]
        ADMIN[ti-gateway-admin]
        K8S_MODULE[ti-gateway-kubernetes]
    end
    
    subgraph "Storage Layer"
        MEMORY[Memory Cache]
        REDIS[Redis Cache]
        DISK[Local Storage]
    end
    
    subgraph "Service Layer"
        SVC1[Service A]
        SVC2[Service B]
        SVC3[Service C]
    end
    
    subgraph "Client Layer"
        CLIENT[Client Apps]
        BROWSER[Web Browser]
    end
    
    CM --> BASE
    CRD --> K8S_MODULE
    K8S_API --> CRD
    K8S_API --> CM
    
    BASE --> MEMORY
    BASE --> REDIS
    ADMIN --> BASE
    K8S_MODULE --> BASE
    
    CLIENT --> K8S_MODULE
    BROWSER --> ADMIN
    K8S_MODULE --> SVC1
    K8S_MODULE --> SVC2
    K8S_MODULE --> SVC3
```

## Configuration Data Flow

### 1. Configuration Sources
```mermaid
sequenceDiagram
    participant USER as User
    participant ADMIN as Admin UI
    participant BASE as ti-gateway-base
    participant CM as ConfigMap
    participant K8S as Kubernetes API
    participant GW as Gateway Core
    
    USER->>ADMIN: Modify configuration
    ADMIN->>BASE: Save configuration
    BASE->>CM: Update ConfigMap
    CM->>K8S: Trigger change event
    K8S->>GW: Notify configuration change
    GW->>GW: Reload configuration
```

### 2. ConfigMap Configuration Flow
```yaml
# ConfigMap configuration structure
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-app-config
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
              filters:
                - StripPrefix=2
  routes.json: |
    [
      {
        "id": "user-service",
        "uri": "lb://user-service",
        "predicates": [{"name": "Path", "args": {"pattern": "/api/users/**"}}],
        "filters": [{"name": "StripPrefix", "args": {"parts": 2}}]
      }
    ]
```

### 3. Configuration Hot Update Mechanism
```java
@Component
public class ConfigMapWatcher {
    
    @EventListener
    public void handleConfigChange(ConfigMapChangeEvent event) {
        // 1. Validate configuration format
        if (validateConfig(event.getNewConfig())) {
            // 2. Update memory cache
            updateMemoryCache(event.getNewConfig());
            // 3. Notify route update
            notifyRouteUpdate(event.getNewConfig());
            // 4. Log configuration change
            logConfigChange(event);
        }
    }
}
```

## Route Data Flow

### 1. Route Discovery Process
```mermaid
sequenceDiagram
    participant K8S as Kubernetes API
    participant WATCHER as IngressWatcher
    participant LOCATOR as RouteDefinitionLocator
    participant GATEWAY as Gateway Core
    participant CLIENT as Client
    
    K8S->>WATCHER: Ingress change event
    WATCHER->>LOCATOR: Update route definition
    LOCATOR->>GATEWAY: Refresh route table
    CLIENT->>GATEWAY: Send request
    GATEWAY->>CLIENT: Return response
```

### 2. Dynamic Route Update
```java
@Service
public class DynamicRouteService {
    
    public void updateRoute(RouteDefinition route) {
        // 1. Validate route configuration
        validateRoute(route);
        
        // 2. Update route definition
        routeDefinitionLocator.updateRoute(route);
        
        // 3. Refresh gateway routes
        gatewayWebHandler.refresh();
        
        // 4. Log route change
        logRouteChange(route);
    }
}
```

### 3. Route Matching Process
```mermaid
flowchart TD
    REQUEST[Client Request] --> PREDICATE[Route Predicate Matching]
    PREDICATE --> |Match Success| FILTER[Filter Chain Processing]
    PREDICATE --> |Match Failed| NOT_FOUND[Return 404]
    FILTER --> TARGET[Target Service]
    TARGET --> RESPONSE[Return Response]
    RESPONSE --> CLIENT[Client]
```

## Service Discovery Data Flow

### 1. Kubernetes Service Discovery
```mermaid
sequenceDiagram
    participant K8S as Kubernetes API
    participant DISCOVERY as ServiceDiscovery
    participant CACHE as ServiceCache
    participant GATEWAY as Gateway Core
    
    K8S->>DISCOVERY: Service change event
    DISCOVERY->>CACHE: Update service cache
    CACHE->>GATEWAY: Notify service change
    GATEWAY->>GATEWAY: Update load balancer
```

### 2. Service Registration and Discovery
```java
@Component
public class KubernetesServiceDiscovery {
    
    @EventListener
    public void handleServiceChange(ServiceChangeEvent event) {
        switch (event.getType()) {
            case ADDED:
                addService(event.getService());
                break;
            case MODIFIED:
                updateService(event.getService());
                break;
            case DELETED:
                removeService(event.getService());
                break;
        }
    }
}
```

## Cache Data Flow

### 1. Multi-Level Cache Architecture
```mermaid
graph TB
    subgraph "Cache Levels"
        L1[L1: Local Cache<br/>Caffeine]
        L2[L2: Distributed Cache<br/>Redis]
        L3[L3: Persistent Storage<br/>ConfigMap]
    end
    
    subgraph "Data Access"
        READ[Read Request]
        WRITE[Write Request]
    end
    
    READ --> L1
    L1 --> |Cache Miss| L2
    L2 --> |Cache Miss| L3
    
    WRITE --> L3
    WRITE --> L2
    WRITE --> L1
```

### 2. Cache Update Strategy
```java
@Service
public class CacheManager {
    
    @Cacheable(value = "routes", key = "#routeId")
    public RouteDefinition getRoute(String routeId) {
        return routeRepository.findById(routeId);
    }
    
    @CacheEvict(value = "routes", key = "#route.id")
    public void updateRoute(RouteDefinition route) {
        routeRepository.save(route);
        // Asynchronously update other caches
        asyncUpdateCache(route);
    }
}
```

## Monitoring Data Flow

### 1. Metrics Collection Process
```mermaid
sequenceDiagram
    participant GATEWAY as Gateway Core
    participant METRICS as MetricsCollector
    participant PROMETHEUS as Prometheus
    participant GRAFANA as Grafana
    
    GATEWAY->>METRICS: Generate metrics data
    METRICS->>PROMETHEUS: Push metrics
    PROMETHEUS->>GRAFANA: Provide data source
    GRAFANA->>GRAFANA: Generate monitoring dashboard
```

### 2. Log Data Flow
```mermaid
graph TB
    subgraph "Log Collection"
        APP[Application Logs]
        ACCESS[Access Logs]
        ERROR[Error Logs]
    end
    
    subgraph "Log Processing"
        FLUENTD[Fluentd]
        LOGSTASH[Logstash]
    end
    
    subgraph "Log Storage"
        ELASTICSEARCH[Elasticsearch]
        KAFKA[Kafka]
    end
    
    subgraph "Log Analysis"
        KIBANA[Kibana]
        GRAFANA[Grafana]
    end
    
    APP --> FLUENTD
    ACCESS --> FLUENTD
    ERROR --> FLUENTD
    
    FLUENTD --> ELASTICSEARCH
    FLUENTD --> KAFKA
    
    ELASTICSEARCH --> KIBANA
    KAFKA --> GRAFANA
```

## Security Data Flow

### 1. Authentication and Authorization Process
```mermaid
sequenceDiagram
    participant CLIENT as Client
    participant GATEWAY as Gateway
    participant AUTH as Authentication Service
    participant AUTHZ as Authorization Service
    participant BACKEND as Backend Service
    
    CLIENT->>GATEWAY: Send request
    GATEWAY->>AUTH: Verify Token
    AUTH->>GATEWAY: Return user info
    GATEWAY->>AUTHZ: Check permissions
    AUTHZ->>GATEWAY: Return permission result
    GATEWAY->>BACKEND: Forward request
    BACKEND->>GATEWAY: Return response
    GATEWAY->>CLIENT: Return response
```

### 2. Security Configuration Data Flow
```java
@Component
public class SecurityConfigManager {
    
    public void updateSecurityConfig(SecurityConfig config) {
        // 1. Validate security configuration
        validateSecurityConfig(config);
        
        // 2. Update security policies
        updateSecurityPolicies(config);
        
        // 3. Refresh security filters
        refreshSecurityFilters();
        
        // 4. Audit security changes
        auditSecurityChange(config);
    }
}
```

## Data Consistency Guarantee

### 1. Eventual Consistency Model
```mermaid
graph TB
    subgraph "Data Writing"
        WRITE[Write Operation]
        VALIDATE[Data Validation]
        PERSIST[Persistence]
        NOTIFY[Change Notification]
    end
    
    subgraph "Data Synchronization"
        SYNC[Data Sync]
        CONFLICT[Conflict Detection]
        RESOLVE[Conflict Resolution]
    end
    
    subgraph "Data Reading"
        READ[Read Operation]
        CACHE[Cache Query]
        FALLBACK[Fallback Query]
    end
    
    WRITE --> VALIDATE
    VALIDATE --> PERSIST
    PERSIST --> NOTIFY
    NOTIFY --> SYNC
    SYNC --> CONFLICT
    CONFLICT --> RESOLVE
    
    READ --> CACHE
    CACHE --> |Cache Miss| FALLBACK
```

### 2. Data Version Control
```java
@Entity
public class RouteDefinition {
    @Id
    private String id;
    private String uri;
    private List<PredicateDefinition> predicates;
    private List<FilterDefinition> filters;
    
    @Version
    private Long version;
    
    @LastModifiedDate
    private LocalDateTime lastModified;
    
    // Optimistic locking control
    public boolean updateWithVersion(RouteDefinition newRoute) {
        if (this.version.equals(newRoute.getVersion())) {
            this.uri = newRoute.getUri();
            this.predicates = newRoute.getPredicates();
            this.filters = newRoute.getFilters();
            this.version++;
            return true;
        }
        return false; // Version conflict
    }
}
```

## Performance Optimization Data Flow

### 1. Asynchronous Processing Flow
```mermaid
sequenceDiagram
    participant CLIENT as Client
    participant GATEWAY as Gateway
    participant ASYNC as Async Processor
    participant QUEUE as Message Queue
    participant WORKER as Worker Thread
    
    CLIENT->>GATEWAY: Send request
    GATEWAY->>ASYNC: Async processing
    ASYNC->>QUEUE: Put in queue
    QUEUE->>WORKER: Consume message
    WORKER->>GATEWAY: Processing complete
    GATEWAY->>CLIENT: Return response
```

### 2. Batch Processing Optimization
```java
@Component
public class BatchProcessor {
    
    @Scheduled(fixedDelay = 1000)
    public void processBatch() {
        List<RouteUpdate> updates = batchQueue.drainTo(100);
        if (!updates.isEmpty()) {
            // Batch update routes
            batchUpdateRoutes(updates);
            // Batch refresh cache
            batchRefreshCache(updates);
        }
    }
}
```

## Data Backup and Recovery

### 1. Configuration Backup Process
```mermaid
sequenceDiagram
    participant SCHEDULER as Scheduled Task
    participant BACKUP as Backup Service
    participant STORAGE as Storage Service
    participant RESTORE as Recovery Service
    
    SCHEDULER->>BACKUP: Trigger backup
    BACKUP->>STORAGE: Save backup
    STORAGE->>RESTORE: Provide recovery
    RESTORE->>RESTORE: Execute recovery
```

### 2. Data Recovery Mechanism
```java
@Service
public class DataRecoveryService {
    
    public void restoreFromBackup(String backupId) {
        // 1. Load backup data
        BackupData backup = loadBackup(backupId);
        
        // 2. Validate data integrity
        validateBackupData(backup);
        
        // 3. Execute data recovery
        restoreConfigData(backup.getConfig());
        restoreRouteData(backup.getRoutes());
        
        // 4. Refresh system state
        refreshSystemState();
    }
}
```

## Data Flow Monitoring

### 1. Data Flow Metrics
```java
@Component
public class DataFlowMetrics {
    
    private final Counter configUpdateCounter;
    private final Timer routeUpdateTimer;
    private final Gauge cacheHitRatio;
    
    public DataFlowMetrics(MeterRegistry meterRegistry) {
        this.configUpdateCounter = Counter.builder("tigateway.config.updates")
            .description("Number of configuration updates")
            .register(meterRegistry);
            
        this.routeUpdateTimer = Timer.builder("tigateway.routes.update.time")
            .description("Route update processing time")
            .register(meterRegistry);
            
        this.cacheHitRatio = Gauge.builder("tigateway.cache.hit.ratio")
            .description("Cache hit ratio")
            .register(meterRegistry, this, DataFlowMetrics::getCacheHitRatio);
    }
}
```

### 2. Data Flow Tracing
```java
@Component
public class DataFlowTracer {
    
    public void traceConfigUpdate(String configId, String operation) {
        Span span = tracer.nextSpan()
            .name("config-update")
            .tag("config.id", configId)
            .tag("operation", operation)
            .start();
            
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            // Execute configuration update
            performConfigUpdate(configId, operation);
        } finally {
            span.end();
        }
    }
}
```

---

**Related Documentation**:
- [System Architecture](./system-architecture.md)
- [Module Design](./module-design.md)
- [Security Architecture](./security.md)
