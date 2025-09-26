# CRD 资源管理

TiGateway 提供了完整的自定义资源定义（CRD）支持，允许用户通过 Kubernetes 原生方式管理网关配置。

## CRD 概述

### 什么是 CRD

自定义资源定义（Custom Resource Definition，CRD）是 Kubernetes 的扩展机制，允许用户定义自己的资源类型。TiGateway 利用 CRD 提供了声明式的网关配置管理。

### TiGateway CRD 架构

```mermaid
graph TB
    subgraph "TiGateway CRD 架构"
        subgraph "API 组"
            API[tigateway.cn/v1]
        end
        
        subgraph "资源类型"
            TG[TiGateway]
            TGM[TiGatewayMapping]
            TGR[TiGatewayRoute]
            TGC[TiGatewayConfig]
        end
        
        subgraph "控制器"
            TC[TiGateway Controller]
            TMC[TiGatewayMapping Controller]
            TRC[TiGatewayRoute Controller]
            TCC[TiGatewayConfig Controller]
        end
        
        subgraph "存储"
            ETCD[etcd]
        end
    end
    
    API --> TG
    API --> TGM
    API --> TGR
    API --> TGC
    
    TG --> TC
    TGM --> TMC
    TGR --> TRC
    TGC --> TCC
    
    TC --> ETCD
    TMC --> ETCD
    TRC --> ETCD
    TCC --> ETCD
```

## 核心 CRD 资源

### 1. TiGateway

TiGateway 是主要的网关资源，定义了网关实例的配置。

#### 资源定义

```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: tigateways.tigateway.cn
spec:
  group: tigateway.cn
  versions:
  - name: v1
    served: true
    storage: true
    schema:
      openAPIV3Schema:
        type: object
        properties:
          spec:
            type: object
            properties:
              replicas:
                type: integer
                minimum: 1
                maximum: 10
                default: 3
              image:
                type: object
                properties:
                  repository:
                    type: string
                  tag:
                    type: string
                  pullPolicy:
                    type: string
                    enum: ["Always", "IfNotPresent", "Never"]
              service:
                type: object
                properties:
                  type:
                    type: string
                    enum: ["ClusterIP", "NodePort", "LoadBalancer"]
                  ports:
                    type: object
                    properties:
                      gateway:
                        type: integer
                        default: 8080
                      admin:
                        type: integer
                        default: 8081
                      management:
                        type: integer
                        default: 8090
              resources:
                type: object
                properties:
                  limits:
                    type: object
                    properties:
                      cpu:
                        type: string
                      memory:
                        type: string
                  requests:
                    type: object
                    properties:
                      cpu:
                        type: string
                      memory:
                        type: string
              config:
                type: object
                properties:
                  routes:
                    type: array
                    items:
                      type: object
                      properties:
                        id:
                          type: string
                        uri:
                          type: string
                        predicates:
                          type: array
                        filters:
                          type: array
                        metadata:
                          type: object
            required: ["replicas", "image", "service"]
          status:
            type: object
            properties:
              phase:
                type: string
                enum: ["Pending", "Running", "Failed", "Terminating"]
              replicas:
                type: integer
              readyReplicas:
                type: integer
              conditions:
                type: array
                items:
                  type: object
                  properties:
                    type:
                      type: string
                    status:
                      type: string
                    lastTransitionTime:
                      type: string
                      format: date-time
                    reason:
                      type: string
                    message:
                      type: string
    additionalPrinterColumns:
    - name: Phase
      type: string
      jsonPath: .status.phase
    - name: Replicas
      type: integer
      jsonPath: .status.replicas
    - name: Ready
      type: integer
      jsonPath: .status.readyReplicas
    - name: Age
      type: date
      jsonPath: .metadata.creationTimestamp
```

#### 使用示例

```yaml
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: example-gateway
  namespace: tigateway
  labels:
    app: tigateway
    version: v1.0.0
spec:
  replicas: 3
  image:
    repository: tigateway/tigateway
    tag: v1.0.0
    pullPolicy: IfNotPresent
  service:
    type: ClusterIP
    ports:
      gateway: 8080
      admin: 8081
      management: 8090
  resources:
    limits:
      cpu: 1000m
      memory: 1Gi
    requests:
      cpu: 500m
      memory: 512Mi
  config:
    routes:
    - id: user-service
      uri: lb://user-service
      predicates:
      - Path=/api/users/**
      filters:
      - StripPrefix=2
      - AddRequestHeader=X-Gateway, TiGateway
      metadata:
        description: "用户服务路由"
        version: "v1"
        tags:
        - "user"
        - "api"
    - id: order-service
      uri: lb://order-service
      predicates:
      - Path=/api/orders/**
      filters:
      - StripPrefix=2
      - CircuitBreaker=order-service
      metadata:
        description: "订单服务路由"
        version: "v1"
        tags:
        - "order"
        - "api"
```

### 2. TiGatewayMapping

TiGatewayMapping 定义了服务映射关系，用于自动发现和路由配置。

#### 资源定义

```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: tigatewaymappings.tigateway.cn
spec:
  group: tigateway.cn
  versions:
  - name: v1
    served: true
    storage: true
    schema:
      openAPIV3Schema:
        type: object
        properties:
          spec:
            type: object
            properties:
              gateway:
                type: string
                description: "关联的 TiGateway 名称"
              mappings:
                type: array
                items:
                  type: object
                  properties:
                    service:
                      type: string
                      description: "Kubernetes 服务名称"
                    namespace:
                      type: string
                      description: "服务所在的命名空间"
                    path:
                      type: string
                      description: "路由路径"
                    targetPath:
                      type: string
                      description: "目标路径"
                    predicates:
                      type: array
                      description: "路由谓词"
                    filters:
                      type: array
                      description: "路由过滤器"
                    metadata:
                      type: object
                      description: "路由元数据"
                  required: ["service", "path"]
              selector:
                type: object
                properties:
                  matchLabels:
                    type: object
                    additionalProperties:
                      type: string
                  matchExpressions:
                    type: array
                    items:
                      type: object
                      properties:
                        key:
                          type: string
                        operator:
                          type: string
                          enum: ["In", "NotIn", "Exists", "DoesNotExist"]
                        values:
                          type: array
                          items:
                            type: string
            required: ["gateway", "mappings"]
          status:
            type: object
            properties:
              phase:
                type: string
                enum: ["Pending", "Active", "Failed"]
              mappedServices:
                type: integer
              lastSyncTime:
                type: string
                format: date-time
              conditions:
                type: array
                items:
                  type: object
                  properties:
                    type:
                      type: string
                    status:
                      type: string
                    lastTransitionTime:
                      type: string
                      format: date-time
                    reason:
                      type: string
                    message:
                      type: string
    additionalPrinterColumns:
    - name: Gateway
      type: string
      jsonPath: .spec.gateway
    - name: Phase
      type: string
      jsonPath: .status.phase
    - name: Mapped Services
      type: integer
      jsonPath: .status.mappedServices
    - name: Age
      type: date
      jsonPath: .metadata.creationTimestamp
```

#### 使用示例

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayMapping
metadata:
  name: service-mapping
  namespace: tigateway
spec:
  gateway: example-gateway
  mappings:
  - service: user-service
    namespace: default
    path: /users
    targetPath: /
    predicates:
    - Path=/api/users/**
    filters:
    - StripPrefix=2
    - AddRequestHeader=X-Service, user-service
    metadata:
      description: "用户服务映射"
      version: "v1"
  - service: order-service
    namespace: default
    path: /orders
    targetPath: /
    predicates:
    - Path=/api/orders/**
    filters:
    - StripPrefix=2
    - CircuitBreaker=order-service
    metadata:
      description: "订单服务映射"
      version: "v1"
  selector:
    matchLabels:
      app: microservice
      version: v1
```

### 3. TiGatewayRoute

TiGatewayRoute 定义了具体的路由配置，支持更细粒度的路由管理。

#### 资源定义

```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: tigatewayroutes.tigateway.cn
spec:
  group: tigateway.cn
  versions:
  - name: v1
    served: true
    storage: true
    schema:
      openAPIV3Schema:
        type: object
        properties:
          spec:
            type: object
            properties:
              gateway:
                type: string
                description: "关联的 TiGateway 名称"
              route:
                type: object
                properties:
                  id:
                    type: string
                    description: "路由 ID"
                  uri:
                    type: string
                    description: "目标 URI"
                  predicates:
                    type: array
                    items:
                      type: object
                      properties:
                        name:
                          type: string
                        args:
                          type: object
                  filters:
                    type: array
                    items:
                      type: object
                      properties:
                        name:
                          type: string
                        args:
                          type: object
                  metadata:
                    type: object
                  order:
                    type: integer
                    default: 0
                required: ["id", "uri", "predicates"]
              enabled:
                type: boolean
                default: true
              priority:
                type: integer
                default: 0
            required: ["gateway", "route"]
          status:
            type: object
            properties:
              phase:
                type: string
                enum: ["Pending", "Active", "Failed", "Disabled"]
              lastAppliedTime:
                type: string
                format: date-time
              conditions:
                type: array
                items:
                  type: object
                  properties:
                    type:
                      type: string
                    status:
                      type: string
                    lastTransitionTime:
                      type: string
                      format: date-time
                    reason:
                      type: string
                    message:
                      type: string
    additionalPrinterColumns:
    - name: Gateway
      type: string
      jsonPath: .spec.gateway
    - name: Route ID
      type: string
      jsonPath: .spec.route.id
    - name: Phase
      type: string
      jsonPath: .status.phase
    - name: Enabled
      type: boolean
      jsonPath: .spec.enabled
    - name: Age
      type: date
      jsonPath: .metadata.creationTimestamp
```

#### 使用示例

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayRoute
metadata:
  name: user-service-route
  namespace: tigateway
spec:
  gateway: example-gateway
  route:
    id: user-service
    uri: lb://user-service
    predicates:
    - name: Path
      args:
        pattern: /api/users/**
    - name: Method
      args:
        methods: GET,POST,PUT,DELETE
    filters:
    - name: StripPrefix
      args:
        parts: 2
    - name: AddRequestHeader
      args:
        name: X-Gateway
        value: TiGateway
    - name: CircuitBreaker
      args:
        name: user-service
        fallbackUri: forward:/fallback
    metadata:
      description: "用户服务路由"
      version: "v1"
      tags:
      - "user"
      - "api"
      - "crud"
    order: 1
  enabled: true
  priority: 100
```

### 4. TiGatewayConfig

TiGatewayConfig 定义了网关的全局配置，包括过滤器、安全设置等。

#### 资源定义

```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: tigatewayconfigs.tigateway.cn
spec:
  group: tigateway.cn
  versions:
  - name: v1
    served: true
    storage: true
    schema:
      openAPIV3Schema:
        type: object
        properties:
          spec:
            type: object
            properties:
              gateway:
                type: string
                description: "关联的 TiGateway 名称"
              globalFilters:
                type: array
                items:
                  type: object
                  properties:
                    name:
                      type: string
                    args:
                      type: object
                    order:
                      type: integer
                      default: 0
              defaultFilters:
                type: array
                items:
                  type: object
                  properties:
                    name:
                      type: string
                    args:
                      type: object
              security:
                type: object
                properties:
                  cors:
                    type: object
                    properties:
                      allowedOrigins:
                        type: array
                        items:
                          type: string
                      allowedMethods:
                        type: array
                        items:
                          type: string
                      allowedHeaders:
                        type: array
                        items:
                          type: string
                  rateLimit:
                    type: object
                    properties:
                      enabled:
                        type: boolean
                      requestsPerMinute:
                        type: integer
                      burstCapacity:
                        type: integer
                  authentication:
                    type: object
                    properties:
                      enabled:
                        type: boolean
                      type:
                        type: string
                        enum: ["jwt", "oauth2", "basic"]
                      config:
                        type: object
              monitoring:
                type: object
                properties:
                  enabled:
                    type: boolean
                  metrics:
                    type: object
                    properties:
                      enabled:
                        type: boolean
                      path:
                        type: string
                        default: "/actuator/prometheus"
                  tracing:
                    type: object
                    properties:
                      enabled:
                        type: boolean
                      endpoint:
                        type: string
            required: ["gateway"]
          status:
            type: object
            properties:
              phase:
                type: string
                enum: ["Pending", "Active", "Failed"]
              lastAppliedTime:
                type: string
                format: date-time
              conditions:
                type: array
                items:
                  type: object
                  properties:
                    type:
                      type: string
                    status:
                      type: string
                    lastTransitionTime:
                      type: string
                      format: date-time
                    reason:
                      type: string
                    message:
                      type: string
    additionalPrinterColumns:
    - name: Gateway
      type: string
      jsonPath: .spec.gateway
    - name: Phase
      type: string
      jsonPath: .status.phase
    - name: Age
      type: date
      jsonPath: .metadata.creationTimestamp
```

#### 使用示例

```yaml
apiVersion: tigateway.cn/v1
kind: TiGatewayConfig
metadata:
  name: gateway-config
  namespace: tigateway
spec:
  gateway: example-gateway
  globalFilters:
  - name: RequestLogging
    args:
      level: INFO
    order: -1000
  - name: RequestId
    args:
      headerName: X-Request-ID
    order: -999
  - name: Authentication
    args:
      type: jwt
      secret: ${JWT_SECRET}
    order: -100
  - name: RateLimit
    args:
      requestsPerMinute: 100
      burstCapacity: 200
    order: -50
  defaultFilters:
  - name: AddResponseHeader
    args:
      name: X-Gateway
      value: TiGateway
  - name: AddResponseHeader
    args:
      name: X-Response-Time
      value: ${response.time}
  security:
    cors:
      allowedOrigins:
      - "*"
      allowedMethods:
      - GET
      - POST
      - PUT
      - DELETE
      - OPTIONS
      allowedHeaders:
      - Content-Type
      - Authorization
      - X-Requested-With
    rateLimit:
      enabled: true
      requestsPerMinute: 100
      burstCapacity: 200
    authentication:
      enabled: true
      type: jwt
      config:
        secret: ${JWT_SECRET}
        expiration: 3600
  monitoring:
    enabled: true
    metrics:
      enabled: true
      path: "/actuator/prometheus"
    tracing:
      enabled: true
      endpoint: "http://jaeger:14268/api/traces"
```

## CRD 控制器

### 1. TiGateway 控制器

```java
@Controller
public class TiGatewayController {
    
    @Autowired
    private TiGatewayService tiGatewayService;
    
    @Autowired
    private KubernetesClient kubernetesClient;
    
    @KubernetesInformers
    public class TiGatewayInformer {
        
        @OnAdd
        public void onAdd(TiGateway tiGateway) {
            log.info("TiGateway added: {}", tiGateway.getMetadata().getName());
            tiGatewayService.createTiGateway(tiGateway);
        }
        
        @OnUpdate
        public void onUpdate(TiGateway oldTiGateway, TiGateway newTiGateway) {
            log.info("TiGateway updated: {}", newTiGateway.getMetadata().getName());
            tiGatewayService.updateTiGateway(oldTiGateway, newTiGateway);
        }
        
        @OnDelete
        public void onDelete(TiGateway tiGateway, boolean deletedFinalStateUnknown) {
            log.info("TiGateway deleted: {}", tiGateway.getMetadata().getName());
            tiGatewayService.deleteTiGateway(tiGateway);
        }
    }
    
    @Service
    public class TiGatewayService {
        
        public void createTiGateway(TiGateway tiGateway) {
            // 创建 Deployment
            createDeployment(tiGateway);
            
            // 创建 Service
            createService(tiGateway);
            
            // 创建 ConfigMap
            createConfigMap(tiGateway);
            
            // 更新状态
            updateStatus(tiGateway, "Running");
        }
        
        public void updateTiGateway(TiGateway oldTiGateway, TiGateway newTiGateway) {
            // 检查是否需要更新
            if (needsUpdate(oldTiGateway, newTiGateway)) {
                // 更新 Deployment
                updateDeployment(newTiGateway);
                
                // 更新 ConfigMap
                updateConfigMap(newTiGateway);
                
                // 更新状态
                updateStatus(newTiGateway, "Running");
            }
        }
        
        public void deleteTiGateway(TiGateway tiGateway) {
            // 删除相关资源
            deleteDeployment(tiGateway);
            deleteService(tiGateway);
            deleteConfigMap(tiGateway);
        }
        
        private void createDeployment(TiGateway tiGateway) {
            Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(tiGateway.getMetadata().getName())
                    .withNamespace(tiGateway.getMetadata().getNamespace())
                    .withLabels(tiGateway.getMetadata().getLabels())
                .endMetadata()
                .withNewSpec()
                    .withReplicas(tiGateway.getSpec().getReplicas())
                    .withNewSelector()
                        .addToMatchLabels("app", tiGateway.getMetadata().getName())
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .addToLabels("app", tiGateway.getMetadata().getName())
                        .endMetadata()
                        .withNewSpec()
                            .addNewContainer()
                                .withName("tigateway")
                                .withImage(tiGateway.getSpec().getImage().getRepository() + ":" + 
                                          tiGateway.getSpec().getImage().getTag())
                                .withImagePullPolicy(tiGateway.getSpec().getImage().getPullPolicy())
                                .addNewPort()
                                    .withContainerPort(tiGateway.getSpec().getService().getPorts().getGateway())
                                    .withName("gateway")
                                .endPort()
                                .addNewPort()
                                    .withContainerPort(tiGateway.getSpec().getService().getPorts().getAdmin())
                                    .withName("admin")
                                .endPort()
                                .addNewPort()
                                    .withContainerPort(tiGateway.getSpec().getService().getPorts().getManagement())
                                    .withName("management")
                                .endPort()
                                .withResources(tiGateway.getSpec().getResources())
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .build();
            
            kubernetesClient.apps().deployments()
                .inNamespace(tiGateway.getMetadata().getNamespace())
                .create(deployment);
        }
        
        private void updateStatus(TiGateway tiGateway, String phase) {
            TiGatewayStatus status = new TiGatewayStatus();
            status.setPhase(phase);
            status.setReplicas(tiGateway.getSpec().getReplicas());
            status.setReadyReplicas(getReadyReplicas(tiGateway));
            status.setConditions(getConditions(tiGateway));
            
            tiGateway.setStatus(status);
            kubernetesClient.customResources(TiGateway.class)
                .inNamespace(tiGateway.getMetadata().getNamespace())
                .updateStatus(tiGateway);
        }
    }
}
```

### 2. TiGatewayMapping 控制器

```java
@Controller
public class TiGatewayMappingController {
    
    @Autowired
    private TiGatewayMappingService mappingService;
    
    @KubernetesInformers
    public class TiGatewayMappingInformer {
        
        @OnAdd
        public void onAdd(TiGatewayMapping mapping) {
            log.info("TiGatewayMapping added: {}", mapping.getMetadata().getName());
            mappingService.createMapping(mapping);
        }
        
        @OnUpdate
        public void onUpdate(TiGatewayMapping oldMapping, TiGatewayMapping newMapping) {
            log.info("TiGatewayMapping updated: {}", newMapping.getMetadata().getName());
            mappingService.updateMapping(oldMapping, newMapping);
        }
        
        @OnDelete
        public void onDelete(TiGatewayMapping mapping, boolean deletedFinalStateUnknown) {
            log.info("TiGatewayMapping deleted: {}", mapping.getMetadata().getName());
            mappingService.deleteMapping(mapping);
        }
    }
    
    @Service
    public class TiGatewayMappingService {
        
        public void createMapping(TiGatewayMapping mapping) {
            // 发现服务
            List<Service> services = discoverServices(mapping);
            
            // 创建路由
            for (Service service : services) {
                createRouteFromService(mapping, service);
            }
            
            // 更新状态
            updateMappingStatus(mapping, "Active", services.size());
        }
        
        public void updateMapping(TiGatewayMapping oldMapping, TiGatewayMapping newMapping) {
            // 删除旧路由
            deleteRoutes(oldMapping);
            
            // 创建新路由
            createMapping(newMapping);
        }
        
        public void deleteMapping(TiGatewayMapping mapping) {
            // 删除相关路由
            deleteRoutes(mapping);
        }
        
        private List<Service> discoverServices(TiGatewayMapping mapping) {
            return kubernetesClient.services()
                .inNamespace(mapping.getSpec().getMappings().get(0).getNamespace())
                .withLabels(mapping.getSpec().getSelector().getMatchLabels())
                .list()
                .getItems();
        }
        
        private void createRouteFromService(TiGatewayMapping mapping, Service service) {
            // 根据服务创建路由配置
            RouteDefinition route = new RouteDefinition();
            route.setId(service.getMetadata().getName());
            route.setUri("lb://" + service.getMetadata().getName());
            
            // 设置谓词
            List<PredicateDefinition> predicates = new ArrayList<>();
            PredicateDefinition pathPredicate = new PredicateDefinition();
            pathPredicate.setName("Path");
            pathPredicate.addArg("pattern", "/" + service.getMetadata().getName() + "/**");
            predicates.add(pathPredicate);
            route.setPredicates(predicates);
            
            // 设置过滤器
            List<FilterDefinition> filters = new ArrayList<>();
            FilterDefinition rewriteFilter = new FilterDefinition();
            rewriteFilter.setName("RewritePath");
            rewriteFilter.addArg("regexp", "/" + service.getMetadata().getName() + "/?(?<remaining>.*)");
            rewriteFilter.addArg("replacement", "/${remaining}");
            filters.add(rewriteFilter);
            route.setFilters(filters);
            
            // 保存路由配置
            saveRouteConfig(mapping.getSpec().getGateway(), route);
        }
    }
}
```

## CRD 管理工具

### 1. kubectl 插件

```bash
# 安装 TiGateway kubectl 插件
kubectl krew install tigateway

# 查看所有 TiGateway 资源
kubectl tigateway get all

# 查看特定网关
kubectl tigateway get gateway example-gateway

# 查看网关状态
kubectl tigateway status example-gateway

# 查看网关路由
kubectl tigateway routes example-gateway

# 创建网关
kubectl tigateway create -f gateway.yaml

# 更新网关
kubectl tigateway update -f gateway.yaml

# 删除网关
kubectl tigateway delete example-gateway
```

### 2. 管理界面集成

```java
@RestController
@RequestMapping("/api/crd")
public class CRDController {
    
    @Autowired
    private KubernetesClient kubernetesClient;
    
    @GetMapping("/tigateways")
    public List<TiGateway> getTiGateways() {
        return kubernetesClient.customResources(TiGateway.class)
            .inNamespace("tigateway")
            .list()
            .getItems();
    }
    
    @GetMapping("/tigateways/{name}")
    public TiGateway getTiGateway(@PathVariable String name) {
        return kubernetesClient.customResources(TiGateway.class)
            .inNamespace("tigateway")
            .withName(name)
            .get();
    }
    
    @PostMapping("/tigateways")
    public TiGateway createTiGateway(@RequestBody TiGateway tiGateway) {
        return kubernetesClient.customResources(TiGateway.class)
            .inNamespace("tigateway")
            .create(tiGateway);
    }
    
    @PutMapping("/tigateways/{name}")
    public TiGateway updateTiGateway(@PathVariable String name, @RequestBody TiGateway tiGateway) {
        return kubernetesClient.customResources(TiGateway.class)
            .inNamespace("tigateway")
            .withName(name)
            .update(tiGateway);
    }
    
    @DeleteMapping("/tigateways/{name}")
    public void deleteTiGateway(@PathVariable String name) {
        kubernetesClient.customResources(TiGateway.class)
            .inNamespace("tigateway")
            .withName(name)
            .delete();
    }
}
```

## 最佳实践

### 1. 资源命名规范

```yaml
# 推荐的命名规范
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: tigateway-prod          # 环境-类型
  namespace: tigateway-prod     # 环境命名空间
  labels:
    app: tigateway
    environment: production
    version: v1.0.0
    team: platform
```

### 2. 资源组织

```yaml
# 按环境组织资源
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: tigateway-dev
  namespace: tigateway-dev
  labels:
    environment: development
---
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: tigateway-staging
  namespace: tigateway-staging
  labels:
    environment: staging
---
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: tigateway-prod
  namespace: tigateway-prod
  labels:
    environment: production
```

### 3. 配置管理

```yaml
# 使用 ConfigMap 管理配置
apiVersion: v1
kind: ConfigMap
metadata:
  name: tigateway-config
  namespace: tigateway
data:
  application.yml: |
    spring:
      cloud:
        gateway:
          global-filters:
          - name: RequestLogging
            args:
              level: INFO
          - name: RateLimit
            args:
              requests-per-minute: 100
---
apiVersion: tigateway.cn/v1
kind: TiGateway
metadata:
  name: example-gateway
spec:
  config:
    configMapRef:
      name: tigateway-config
```

### 4. 监控和告警

```yaml
# 配置监控
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: tigateway
  namespace: tigateway
spec:
  selector:
    matchLabels:
      app: tigateway
  endpoints:
  - port: management
    path: /actuator/prometheus
---
# 配置告警
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: tigateway-alerts
  namespace: tigateway
spec:
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
```

## 故障排除

### 1. 常见问题

#### CRD 未注册
```bash
# 检查 CRD 是否已注册
kubectl get crd | grep tigateway

# 重新注册 CRD
kubectl apply -f tigateway-crds.yaml
```

#### 控制器未启动
```bash
# 检查控制器状态
kubectl get pods -n tigateway-system

# 查看控制器日志
kubectl logs -f deployment/tigateway-controller -n tigateway-system
```

#### 资源状态异常
```bash
# 查看资源状态
kubectl describe tigateway example-gateway

# 查看事件
kubectl get events -n tigateway --sort-by='.lastTimestamp'
```

### 2. 调试命令

```bash
# 查看 CRD 定义
kubectl get crd tigateways.tigateway.cn -o yaml

# 查看资源详情
kubectl get tigateway example-gateway -o yaml

# 查看控制器日志
kubectl logs -f deployment/tigateway-controller -n tigateway-system
```

## 总结

TiGateway 的 CRD 资源管理提供了完整的 Kubernetes 原生配置管理能力：

1. **完整的 CRD 定义**: 支持 TiGateway、TiGatewayMapping、TiGatewayRoute、TiGatewayConfig 等资源
2. **声明式配置**: 通过 YAML 文件进行声明式配置管理
3. **自动控制器**: 提供完整的控制器实现，自动管理资源生命周期
4. **管理工具**: 提供 kubectl 插件和管理界面集成
5. **最佳实践**: 遵循 Kubernetes 资源管理最佳实践
6. **监控告警**: 支持完整的监控和告警配置

通过 CRD 资源管理，用户可以以 Kubernetes 原生的方式管理 TiGateway 配置，实现声明式的网关管理。
