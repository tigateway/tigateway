# TiGateway Configuration Schema

TiGateway配置Schema是一套标准化的YAML配置规范，用于定义和管理TiGateway网关的配置。该Schema基于JSON Schema Draft 7规范，提供了完整的配置验证和转换功能。

## 特性

- **标准化配置**: 基于JSON Schema Draft 7的标准化配置格式
- **类型安全**: 完整的类型定义和验证
- **自动验证**: 配置加载时自动验证格式和内容
- **双向转换**: 支持YAML配置与ConfigMap存储之间的双向转换
- **REST API**: 提供完整的配置管理REST API
- **版本控制**: 支持配置版本管理和升级

## Schema结构

### 根级别配置

```yaml
metadata:          # 配置元数据
applications:      # 应用配置列表
services:          # 服务配置列表
routes:            # 路由配置列表
middlewares:       # 中间件配置列表
security:          # 安全配置
monitoring:        # 监控配置
```

### 配置元数据

```yaml
metadata:
  name: "配置名称"
  version: "1.0.0"
  description: "配置描述"
  created_at: "2024-01-01T00:00:00Z"
  updated_at: "2024-01-01T00:00:00Z"
```

### 应用配置

```yaml
applications:
  - id: "应用唯一标识"
    name: "应用名称"
    description: "应用描述"
    type: "web|mobile|api|admin"
    status: "active|inactive|maintenance"
    credentials:
      app_key: "应用密钥"
      app_secret: "应用密钥"
      algorithm: "HMAC-SHA256|HMAC-SHA1|MD5"
    permissions:
      - resource: "资源标识"
        actions: ["read", "write", "execute", "admin"]
    rate_limits:
      requests_per_second: 100
      requests_per_minute: 1000
      requests_per_hour: 10000
      burst_size: 20
      strategy: "fixed_window|sliding_window|token_bucket"
    tags: ["标签1", "标签2"]
```

### 服务配置

```yaml
services:
  - id: "服务唯一标识"
    name: "服务名称"
    description: "服务描述"
    type: "http|tcp|udp|grpc"
    endpoints:
      - url: "服务端点URL"
        weight: 1
        health_check_path: "/health"
    health_check:
      enabled: true
      interval: 30
      timeout: 5
      retries: 3
    load_balancing:
      strategy: "round_robin|weighted_round_robin|least_connections|random|ip_hash"
      sticky_session: false
    circuit_breaker:
      enabled: true
      failure_threshold: 5
      success_threshold: 3
      timeout: 60
    retry_policy:
      enabled: true
      max_attempts: 3
      backoff_strategy: "fixed|exponential|linear"
      initial_delay: 1000
      max_delay: 10000
    timeout:
      connect: 5000
      read: 30000
      write: 30000
```

### 路由配置

```yaml
routes:
  - id: "路由唯一标识"
    name: "路由名称"
    description: "路由描述"
    path: "/api/users"
    methods: ["GET", "POST", "PUT", "DELETE"]
    host: "api.example.com"
    priority: 100
    service_id: "user-service"
    middlewares: ["auth", "rate-limit"]
    predicates:
      - type: "path|method|header|query|host|remote_addr|weight"
        config:
          # 谓词特定配置
    filters:
      - type: "add_request_header|add_response_header|rewrite_path|strip_prefix|add_prefix|circuit_breaker|retry|rate_limit"
        config:
          # 过滤器特定配置
```

### 中间件配置

```yaml
middlewares:
  - id: "中间件唯一标识"
    name: "中间件名称"
    type: "auth|rate_limit|cors|logging|metrics|transform|cache"
    enabled: true
    order: 1
    config:
      # 中间件特定配置参数
```

### 安全配置

```yaml
security:
  cors:
    enabled: true
    allowed_origins: ["https://example.com"]
    allowed_methods: ["GET", "POST", "PUT", "DELETE"]
    allowed_headers: ["*"]
    exposed_headers: ["X-Response-Time"]
    allow_credentials: true
    max_age: 3600
  authentication:
    enabled: true
    type: "jwt|oauth2|basic|api_key|custom"
    config:
      # 认证特定配置
  authorization:
    enabled: true
    type: "rbac|abac|custom"
    config:
      # 授权特定配置
  ssl:
    enabled: true
    certificate: "SSL证书内容"
    private_key: "私钥内容"
```

### 监控配置

```yaml
monitoring:
  metrics:
    enabled: true
    exporters: ["prometheus", "influxdb"]
    interval: 30
  tracing:
    enabled: true
    exporters: ["jaeger", "zipkin"]
    sampling_rate: 0.1
  logging:
    level: "TRACE|DEBUG|INFO|WARN|ERROR"
    format: "json|text"
    outputs: ["console", "file", "syslog", "kafka", "elasticsearch"]
```

## 使用方法

### 1. 配置验证

```java
@Autowired
private ConfigSchemaValidator schemaValidator;

// 验证YAML配置
ValidationResult result = schemaValidator.validateYaml(yamlContent);
if (result.isValid()) {
    // 配置有效
} else {
    // 配置无效，查看错误信息
    System.out.println(result.getErrorMessage());
}
```

### 2. 配置转换

```java
@Autowired
private ConfigTransformer configTransformer;

// 将YAML配置转换为ConfigMap应用信息
List<ConfigMapAppInfo> appInfos = configTransformer.transformYamlToConfigMapApps(yamlContent);

// 将ConfigMap应用信息转换为YAML配置
String yamlContent = configTransformer.transformConfigMapAppsToYaml(appInfos);
```

### 3. Schema存储

```java
@Autowired
private ConfigMapSchemaStorage schemaStorage;

// 从YAML加载配置
int loadedCount = schemaStorage.loadFromYaml(yamlContent);

// 导出配置为YAML
String exportedYaml = schemaStorage.exportToYaml();

// 验证配置
ValidationResult result = schemaStorage.validateYaml(yamlContent);
```

## REST API

### 获取Schema信息

```bash
GET /api/config/schema/info
```

### 验证配置

```bash
# 验证YAML配置
POST /api/config/schema/validate/yaml
Content-Type: application/json

{
  "content": "yaml配置内容"
}

# 验证JSON配置
POST /api/config/schema/validate/json
Content-Type: application/json

{
  "content": "json配置内容"
}
```

### 加载配置

```bash
# 从YAML加载配置
POST /api/config/schema/load/yaml
Content-Type: application/json

{
  "content": "yaml配置内容"
}

# 从JSON加载配置
POST /api/config/schema/load/json
Content-Type: application/json

{
  "content": "json配置内容"
}
```

### 导出配置

```bash
# 导出为YAML格式
GET /api/config/schema/export/yaml
```

### 配置管理

```bash
# 获取配置统计信息
GET /api/config/schema/stats

# 刷新配置缓存
POST /api/config/schema/refresh

# 清空所有配置
DELETE /api/config/schema/clear

# 健康检查
GET /api/config/schema/health
```

## 配置示例

### 最小配置

```yaml
metadata:
  name: "minimal-config"
  version: "1.0.0"

applications:
  - id: "default-app"
    name: "Default Application"
    type: "web"
    credentials:
      app_key: "default-key"
      app_secret: "default-secret"
```

### 完整配置

参考 `tigateway-standard-config.yaml` 文件，该文件包含了所有配置项的完整示例。

## 最佳实践

### 1. 配置组织

- 使用有意义的配置名称和版本号
- 按功能模块组织配置
- 使用标签对配置进行分类

### 2. 安全配置

- 使用强密码和密钥
- 定期轮换密钥
- 启用SSL/TLS加密
- 配置适当的CORS策略

### 3. 监控配置

- 启用指标收集和链路追踪
- 配置适当的日志级别
- 设置告警阈值

### 4. 性能配置

- 配置适当的限流策略
- 启用熔断器和重试机制
- 优化负载均衡策略

### 5. 版本管理

- 使用语义化版本号
- 记录配置变更历史
- 支持配置回滚

## 故障排除

### 常见问题

1. **配置验证失败**
   - 检查YAML语法是否正确
   - 验证必填字段是否完整
   - 检查数据类型是否匹配

2. **转换失败**
   - 确保配置符合Schema规范
   - 检查字段映射是否正确
   - 验证数据格式是否有效

3. **API调用失败**
   - 检查请求格式是否正确
   - 验证认证信息是否有效
   - 查看错误日志获取详细信息

### 调试命令

```bash
# 验证配置文件
curl -X POST http://localhost:8080/api/config/schema/validate/yaml \
  -H "Content-Type: application/json" \
  -d '{"content": "yaml配置内容"}'

# 获取配置统计
curl http://localhost:8080/api/config/schema/stats

# 健康检查
curl http://localhost:8080/api/config/schema/health
```

## 扩展开发

### 自定义中间件

```yaml
middlewares:
  - id: "custom-middleware"
    name: "Custom Middleware"
    type: "custom"
    config:
      custom_param1: "value1"
      custom_param2: "value2"
```

### 自定义过滤器

```yaml
filters:
  - type: "custom_filter"
    config:
      custom_config: "value"
```

### 自定义谓词

```yaml
predicates:
  - type: "custom_predicate"
    config:
      custom_condition: "value"
```

## 版本历史

- **v1.0.0**: 初始版本，支持基本的应用、服务、路由和中间件配置
- **v1.1.0**: 添加安全配置和监控配置支持
- **v1.2.0**: 增强中间件和过滤器配置选项
- **v1.3.0**: 添加配置验证和转换API

## 贡献指南

1. Fork项目仓库
2. 创建特性分支
3. 提交更改
4. 创建Pull Request
5. 等待代码审查

## 许可证

本项目采用MIT许可证，详情请参阅LICENSE文件。
