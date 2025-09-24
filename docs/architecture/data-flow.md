# TiGateway 数据流设计

## 概述

TiGateway 采用云原生架构，数据流设计基于 Kubernetes 原生资源，支持配置热更新、服务发现和动态路由。本文档详细说明了数据在系统中的流转过程。

## 整体数据流架构

```mermaid
graph TB
    subgraph "配置数据流"
        CM[ConfigMap]
        CRD[Custom Resources]
        K8S_API[Kubernetes API]
    end
    
    subgraph "TiGateway 核心"
        BASE[ti-gateway-base]
        ADMIN[ti-gateway-admin]
        K8S_MODULE[ti-gateway-kubernetes]
    end
    
    subgraph "存储层"
        MEMORY[内存缓存]
        REDIS[Redis缓存]
        DISK[本地存储]
    end
    
    subgraph "服务层"
        SVC1[Service A]
        SVC2[Service B]
        SVC3[Service C]
    end
    
    subgraph "客户端"
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

## 配置数据流

### 1. 配置来源
```mermaid
sequenceDiagram
    participant USER as 用户
    participant ADMIN as Admin UI
    participant BASE as ti-gateway-base
    participant CM as ConfigMap
    participant K8S as Kubernetes API
    participant GW as Gateway Core
    
    USER->>ADMIN: 修改配置
    ADMIN->>BASE: 保存配置
    BASE->>CM: 更新 ConfigMap
    CM->>K8S: 触发变更事件
    K8S->>GW: 通知配置变更
    GW->>GW: 重新加载配置
```

### 2. ConfigMap 配置流
```yaml
# ConfigMap 配置结构
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

### 3. 配置热更新机制
```java
@Component
public class ConfigMapWatcher {
    
    @EventListener
    public void handleConfigChange(ConfigMapChangeEvent event) {
        // 1. 验证配置格式
        if (validateConfig(event.getNewConfig())) {
            // 2. 更新内存缓存
            updateMemoryCache(event.getNewConfig());
            // 3. 通知路由更新
            notifyRouteUpdate(event.getNewConfig());
            // 4. 记录变更日志
            logConfigChange(event);
        }
    }
}
```

## 路由数据流

### 1. 路由发现流程
```mermaid
sequenceDiagram
    participant K8S as Kubernetes API
    participant WATCHER as IngressWatcher
    participant LOCATOR as RouteDefinitionLocator
    participant GATEWAY as Gateway Core
    participant CLIENT as Client
    
    K8S->>WATCHER: Ingress 变更事件
    WATCHER->>LOCATOR: 更新路由定义
    LOCATOR->>GATEWAY: 刷新路由表
    CLIENT->>GATEWAY: 发送请求
    GATEWAY->>CLIENT: 返回响应
```

### 2. 动态路由更新
```java
@Service
public class DynamicRouteService {
    
    public void updateRoute(RouteDefinition route) {
        // 1. 验证路由配置
        validateRoute(route);
        
        // 2. 更新路由定义
        routeDefinitionLocator.updateRoute(route);
        
        // 3. 刷新网关路由
        gatewayWebHandler.refresh();
        
        // 4. 记录路由变更
        logRouteChange(route);
    }
}
```

### 3. 路由匹配流程
```mermaid
flowchart TD
    REQUEST[客户端请求] --> PREDICATE[路由谓词匹配]
    PREDICATE --> |匹配成功| FILTER[过滤器链处理]
    PREDICATE --> |匹配失败| NOT_FOUND[返回 404]
    FILTER --> TARGET[目标服务]
    TARGET --> RESPONSE[返回响应]
    RESPONSE --> CLIENT[客户端]
```

## 服务发现数据流

### 1. Kubernetes 服务发现
```mermaid
sequenceDiagram
    participant K8S as Kubernetes API
    participant DISCOVERY as ServiceDiscovery
    participant CACHE as ServiceCache
    participant GATEWAY as Gateway Core
    
    K8S->>DISCOVERY: 服务变更事件
    DISCOVERY->>CACHE: 更新服务缓存
    CACHE->>GATEWAY: 通知服务变更
    GATEWAY->>GATEWAY: 更新负载均衡器
```

### 2. 服务注册与发现
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

## 缓存数据流

### 1. 多级缓存架构
```mermaid
graph TB
    subgraph "缓存层级"
        L1[L1: 本地缓存<br/>Caffeine]
        L2[L2: 分布式缓存<br/>Redis]
        L3[L3: 持久化存储<br/>ConfigMap]
    end
    
    subgraph "数据访问"
        READ[读取请求]
        WRITE[写入请求]
    end
    
    READ --> L1
    L1 --> |缓存未命中| L2
    L2 --> |缓存未命中| L3
    
    WRITE --> L3
    WRITE --> L2
    WRITE --> L1
```

### 2. 缓存更新策略
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
        // 异步更新其他缓存
        asyncUpdateCache(route);
    }
}
```

## 监控数据流

### 1. 指标收集流程
```mermaid
sequenceDiagram
    participant GATEWAY as Gateway Core
    participant METRICS as MetricsCollector
    participant PROMETHEUS as Prometheus
    participant GRAFANA as Grafana
    
    GATEWAY->>METRICS: 生成指标数据
    METRICS->>PROMETHEUS: 推送指标
    PROMETHEUS->>GRAFANA: 提供数据源
    GRAFANA->>GRAFANA: 生成监控面板
```

### 2. 日志数据流
```mermaid
graph TB
    subgraph "日志收集"
        APP[应用日志]
        ACCESS[访问日志]
        ERROR[错误日志]
    end
    
    subgraph "日志处理"
        FLUENTD[Fluentd]
        LOGSTASH[Logstash]
    end
    
    subgraph "日志存储"
        ELASTICSEARCH[Elasticsearch]
        KAFKA[Kafka]
    end
    
    subgraph "日志分析"
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

## 安全数据流

### 1. 认证授权流程
```mermaid
sequenceDiagram
    participant CLIENT as 客户端
    participant GATEWAY as Gateway
    participant AUTH as 认证服务
    participant AUTHZ as 授权服务
    participant BACKEND as 后端服务
    
    CLIENT->>GATEWAY: 发送请求
    GATEWAY->>AUTH: 验证 Token
    AUTH->>GATEWAY: 返回用户信息
    GATEWAY->>AUTHZ: 检查权限
    AUTHZ->>GATEWAY: 返回权限结果
    GATEWAY->>BACKEND: 转发请求
    BACKEND->>GATEWAY: 返回响应
    GATEWAY->>CLIENT: 返回响应
```

### 2. 安全配置数据流
```java
@Component
public class SecurityConfigManager {
    
    public void updateSecurityConfig(SecurityConfig config) {
        // 1. 验证安全配置
        validateSecurityConfig(config);
        
        // 2. 更新安全策略
        updateSecurityPolicies(config);
        
        // 3. 刷新安全过滤器
        refreshSecurityFilters();
        
        // 4. 记录安全变更
        auditSecurityChange(config);
    }
}
```

## 数据一致性保证

### 1. 最终一致性模型
```mermaid
graph TB
    subgraph "数据写入"
        WRITE[写入操作]
        VALIDATE[数据验证]
        PERSIST[持久化]
        NOTIFY[变更通知]
    end
    
    subgraph "数据同步"
        SYNC[数据同步]
        CONFLICT[冲突检测]
        RESOLVE[冲突解决]
    end
    
    subgraph "数据读取"
        READ[读取操作]
        CACHE[缓存查询]
        FALLBACK[降级查询]
    end
    
    WRITE --> VALIDATE
    VALIDATE --> PERSIST
    PERSIST --> NOTIFY
    NOTIFY --> SYNC
    SYNC --> CONFLICT
    CONFLICT --> RESOLVE
    
    READ --> CACHE
    CACHE --> |缓存未命中| FALLBACK
```

### 2. 数据版本控制
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
    
    // 乐观锁控制
    public boolean updateWithVersion(RouteDefinition newRoute) {
        if (this.version.equals(newRoute.getVersion())) {
            this.uri = newRoute.getUri();
            this.predicates = newRoute.getPredicates();
            this.filters = newRoute.getFilters();
            this.version++;
            return true;
        }
        return false; // 版本冲突
    }
}
```

## 性能优化数据流

### 1. 异步处理流程
```mermaid
sequenceDiagram
    participant CLIENT as 客户端
    participant GATEWAY as Gateway
    participant ASYNC as 异步处理器
    participant QUEUE as 消息队列
    participant WORKER as 工作线程
    
    CLIENT->>GATEWAY: 发送请求
    GATEWAY->>ASYNC: 异步处理
    ASYNC->>QUEUE: 放入队列
    QUEUE->>WORKER: 消费消息
    WORKER->>GATEWAY: 处理完成
    GATEWAY->>CLIENT: 返回响应
```

### 2. 批量处理优化
```java
@Component
public class BatchProcessor {
    
    @Scheduled(fixedDelay = 1000)
    public void processBatch() {
        List<RouteUpdate> updates = batchQueue.drainTo(100);
        if (!updates.isEmpty()) {
            // 批量更新路由
            batchUpdateRoutes(updates);
            // 批量刷新缓存
            batchRefreshCache(updates);
        }
    }
}
```

## 数据备份与恢复

### 1. 配置备份流程
```mermaid
sequenceDiagram
    participant SCHEDULER as 定时任务
    participant BACKUP as 备份服务
    participant STORAGE as 存储服务
    participant RESTORE as 恢复服务
    
    SCHEDULER->>BACKUP: 触发备份
    BACKUP->>STORAGE: 保存备份
    STORAGE->>RESTORE: 提供恢复
    RESTORE->>RESTORE: 执行恢复
```

### 2. 数据恢复机制
```java
@Service
public class DataRecoveryService {
    
    public void restoreFromBackup(String backupId) {
        // 1. 加载备份数据
        BackupData backup = loadBackup(backupId);
        
        // 2. 验证数据完整性
        validateBackupData(backup);
        
        // 3. 执行数据恢复
        restoreConfigData(backup.getConfig());
        restoreRouteData(backup.getRoutes());
        
        // 4. 刷新系统状态
        refreshSystemState();
    }
}
```

## 数据流监控

### 1. 数据流指标
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

### 2. 数据流追踪
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
            // 执行配置更新
            performConfigUpdate(configId, operation);
        } finally {
            span.end();
        }
    }
}
```

---

**相关文档**:
- [系统架构](./system-architecture.md)
- [模块设计](./module-design.md)
- [安全架构](./security.md)
