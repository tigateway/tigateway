# 管理 API

TiGateway 提供了丰富的管理 API，用于配置和监控网关的运行状态。

## 认证

所有管理 API 都需要认证。请确保在请求头中包含有效的认证信息。

## 端点

### 健康检查

```http
GET /actuator/health
```

返回网关的健康状态。

### 路由管理

```http
GET /actuator/gateway/routes
```

获取所有路由配置。

```http
POST /actuator/gateway/routes/{id}
```

创建新的路由。

```http
DELETE /actuator/gateway/routes/{id}
```

删除指定路由。

### 过滤器管理

```http
GET /actuator/gateway/filters
```

获取所有过滤器配置。

### 指标监控

```http
GET /actuator/metrics
```

获取网关运行指标。

## 响应格式

所有 API 响应都使用 JSON 格式：

```json
{
  "status": "success",
  "data": {},
  "message": "操作成功"
}
```

## 错误处理

当请求失败时，API 会返回相应的 HTTP 状态码和错误信息：

- `400 Bad Request` - 请求参数错误
- `401 Unauthorized` - 认证失败
- `404 Not Found` - 资源不存在
- `500 Internal Server Error` - 服务器内部错误
