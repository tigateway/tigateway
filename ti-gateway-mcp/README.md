# TiGateway MCP Server

TiGateway MCP (Model Context Protocol) Server 是一个集成到TiGateway主应用中的MCP服务器，提供AI驱动的网关管理功能。

## 功能特性

- **集成式设计**: MCP服务集成到TiGateway主应用中，无需单独部署
- **独立端口支持**: 可选择在独立端口运行MCP服务
- **RESTful API**: 提供HTTP接口访问MCP功能
- **Kubernetes集成**: 支持Kubernetes资源管理
- **监控和日志**: 提供指标收集和日志查询功能
- **配置管理**: 支持动态配置管理

## 配置说明

### 基本配置

在 `application.yml` 中配置MCP服务：

```yaml
tigateway:
  mcp:
    enabled: true                    # 启用MCP服务
    independent-port: false          # 是否使用独立端口
    port: 8082                      # MCP服务端口（独立端口模式）
    context-path: /mcp              # 上下文路径
    allowed-origins:                # CORS允许的源
      - "*"
    authentication-enabled: false   # 是否启用认证
```

### 独立端口模式

当 `independent-port: true` 时，MCP服务将在独立端口运行：

```yaml
tigateway:
  mcp:
    enabled: true
    independent-port: true
    port: 8082
```

### Kubernetes配置

```yaml
tigateway:
  mcp:
    kubernetes:
      namespace: default
      api-server-url: ""
      service-account-token-path: "/var/run/secrets/kubernetes.io/serviceaccount/token"
      ca-cert-path: "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt"
```

### 监控配置

```yaml
tigateway:
  mcp:
    metrics:
      enabled: true
      collection-interval: 30
      retention-period: 24
```

## API接口

### 健康检查

```http
GET /mcp/health
```

响应示例：
```json
{
  "status": "healthy",
  "service": "TiGateway MCP Server",
  "version": "1.0.0",
  "timestamp": 1703123456789
}
```

### 服务信息

```http
GET /mcp/info
```

响应示例：
```json
{
  "name": "TiGateway MCP Server",
  "version": "1.0.0",
  "description": "MCP server for TiGateway API Gateway management",
  "protocolVersion": "2024-11-05",
  "capabilities": {
    "tools": {
      "listChanged": true
    },
    "resources": {
      "subscribe": false,
      "listChanged": true
    }
  }
}
```

### MCP请求处理

```http
POST /mcp/request
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/list",
  "params": {}
}
```

## 可用工具

MCP服务器提供以下工具：

1. **路由管理**
   - `tigateway_list_routes`: 列出所有路由
   - `tigateway_create_route`: 创建新路由
   - `tigateway_update_route`: 更新路由
   - `tigateway_delete_route`: 删除路由
   - `tigateway_test_route`: 测试路由

2. **服务管理**
   - `tigateway_list_services`: 列出服务
   - `tigateway_service_health`: 检查服务健康状态

3. **监控和日志**
   - `tigateway_get_metrics`: 获取指标
   - `tigateway_get_config`: 获取配置
   - `tigateway_get_logs`: 获取日志

## 使用方式

### 1. 集成模式（默认）

MCP服务集成在主应用中，通过主应用的端口访问：

```bash
# 启动TiGateway主应用
java -jar ti-gateway-kubernetes-1.0.0.jar

# 访问MCP服务
curl http://localhost:8080/mcp/health
```

### 2. 独立端口模式

MCP服务在独立端口运行：

```yaml
tigateway:
  mcp:
    independent-port: true
    port: 8082
```

```bash
# 启动TiGateway主应用
java -jar ti-gateway-kubernetes-1.0.0.jar

# 访问MCP服务（独立端口）
curl http://localhost:8082/mcp/health
```

## 开发说明

### 项目结构

```
ti-gateway-mcp/
├── src/main/java/ti/gateway/mcp/
│   ├── config/          # 配置类
│   ├── controller/      # REST控制器
│   ├── model/          # 数据模型
│   ├── server/         # MCP服务器核心
│   ├── service/        # 业务服务
│   └── tools/          # 工具定义和执行器
└── src/main/resources/
    └── application.yml # 配置文件
```

### 添加新工具

1. 在 `TiGatewayTools.java` 中定义新工具
2. 在 `TiGatewayToolExecutor.java` 中实现工具执行逻辑
3. 在相应的服务类中实现具体功能

### 条件配置

MCP服务使用条件配置，只有在 `tigateway.mcp.enabled=true` 时才会加载相关组件。

## 注意事项

1. **端口冲突**: 确保MCP服务端口不与主应用端口冲突
2. **权限配置**: 在生产环境中建议启用认证
3. **资源限制**: 根据实际需求调整监控和日志的保留策略
4. **网络访问**: 确保MCP服务端口在防火墙中正确配置

## 故障排除

### 常见问题

1. **MCP服务未启动**
   - 检查 `tigateway.mcp.enabled` 配置
   - 查看应用日志中的错误信息

2. **端口冲突**
   - 检查端口是否被其他服务占用
   - 修改 `tigateway.mcp.port` 配置

3. **CORS问题**
   - 检查 `tigateway.mcp.allowed-origins` 配置
   - 确保客户端域名在允许列表中

### 日志配置

```yaml
logging:
  level:
    ti.gateway.mcp: DEBUG
```

## 版本历史

- **1.0.0**: 初始版本，支持基本的MCP协议和工具
- 集成到TiGateway主应用
- 支持独立端口模式
- 提供Kubernetes、监控、配置和日志管理工具