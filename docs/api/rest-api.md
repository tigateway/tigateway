# TiGateway REST API 文档

## 概述

TiGateway 提供了完整的 REST API 接口，用于管理网关配置、监控系统状态和操作路由规则。API 遵循 RESTful 设计原则，支持 JSON 格式的数据交换。

## 基础信息

### 服务端点
- **主 Gateway API**: `http://localhost:8080`
- **Admin API**: `http://localhost:8081/admin/api`
- **Management API**: `http://localhost:8090/actuator`

### 认证方式
```http
Authorization: Bearer <token>
Content-Type: application/json
```

### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2024-09-23T19:00:00Z"
}
```

## Gateway 核心 API

### 路由管理

#### 获取所有路由
```http
GET /actuator/gateway/routes
```

**响应示例**:
```json
[
  {
    "route_id": "user-service",
    "route_object": {
      "id": "user-service",
      "uri": "lb://user-service",
      "predicates": [
        {
          "name": "Path",
          "args": {
            "pattern": "/api/users/**"
          }
        }
      ],
      "filters": [
        {
          "name": "StripPrefix",
          "args": {
            "parts": 2
          }
        }
      ]
    }
  }
]
```

#### 获取特定路由
```http
GET /actuator/gateway/routes/{routeId}
```

#### 创建路由
```http
POST /actuator/gateway/routes/{routeId}
Content-Type: application/json

{
  "uri": "lb://user-service",
  "predicates": [
    {
      "name": "Path",
      "args": {
        "pattern": "/api/users/**"
      }
    }
  ],
  "filters": [
    {
      "name": "StripPrefix",
      "args": {
        "parts": 2
      }
    }
  ]
}
```

#### 删除路由
```http
DELETE /actuator/gateway/routes/{routeId}
```

#### 刷新路由
```http
POST /actuator/gateway/refresh
```

### 过滤器管理

#### 获取全局过滤器
```http
GET /actuator/gateway/globalfilters
```

**响应示例**:
```json
[
  {
    "name": "NettyWriteResponseFilter",
    "order": -1
  },
  {
    "name": "RouteToRequestUrlFilter",
    "order": 10000
  }
]
```

#### 获取路由过滤器
```http
GET /actuator/gateway/routefilters
```

## Admin 管理 API

### 应用信息管理

#### 获取应用列表
```http
GET /admin/api/apps
```

**查询参数**:
- `page`: 页码 (默认: 1)
- `size`: 每页大小 (默认: 10)
- `name`: 应用名称 (可选)
- `status`: 应用状态 (可选)

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "app-001",
        "name": "user-service",
        "description": "用户服务",
        "status": "ACTIVE",
        "version": "1.0.0",
        "createdAt": "2024-09-23T10:00:00Z",
        "updatedAt": "2024-09-23T10:00:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

#### 创建应用
```http
POST /admin/api/apps
Content-Type: application/json

{
  "name": "user-service",
  "description": "用户服务",
  "version": "1.0.0",
  "config": {
    "timeout": 30000,
    "retries": 3
  }
}
```

#### 更新应用
```http
PUT /admin/api/apps/{appId}
Content-Type: application/json

{
  "name": "user-service",
  "description": "用户服务 (更新)",
  "version": "1.1.0",
  "config": {
    "timeout": 30000,
    "retries": 3
  }
}
```

#### 删除应用
```http
DELETE /admin/api/apps/{appId}
```

### 路由配置管理

#### 获取路由配置
```http
GET /admin/api/routes
```

**查询参数**:
- `appId`: 应用 ID (可选)
- `status`: 路由状态 (可选)

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "route-001",
      "appId": "app-001",
      "name": "用户API路由",
      "path": "/api/users/**",
      "method": "GET,POST,PUT,DELETE",
      "target": "lb://user-service",
      "status": "ACTIVE",
      "filters": [
        {
          "name": "StripPrefix",
          "args": {
            "parts": 2
          }
        }
      ],
      "createdAt": "2024-09-23T10:00:00Z"
    }
  ]
}
```

#### 创建路由配置
```http
POST /admin/api/routes
Content-Type: application/json

{
  "appId": "app-001",
  "name": "用户API路由",
  "path": "/api/users/**",
  "method": "GET,POST,PUT,DELETE",
  "target": "lb://user-service",
  "filters": [
    {
      "name": "StripPrefix",
      "args": {
        "parts": 2
      }
    }
  ]
}
```

#### 更新路由配置
```http
PUT /admin/api/routes/{routeId}
Content-Type: application/json

{
  "name": "用户API路由 (更新)",
  "path": "/api/users/**",
  "method": "GET,POST,PUT,DELETE",
  "target": "lb://user-service",
  "filters": [
    {
      "name": "StripPrefix",
      "args": {
        "parts": 2
      }
    },
    {
      "name": "AddRequestHeader",
      "args": {
        "name": "X-Gateway",
        "value": "TiGateway"
      }
    }
  ]
}
```

#### 删除路由配置
```http
DELETE /admin/api/routes/{routeId}
```

### 系统监控

#### 获取系统状态
```http
GET /admin/api/system/status
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP",
    "uptime": "2h 30m 15s",
    "version": "1.0.0",
    "javaVersion": "11.0.25",
    "memory": {
      "used": "512MB",
      "max": "1GB",
      "usage": "51.2%"
    },
    "routes": {
      "total": 10,
      "active": 8,
      "inactive": 2
    }
  }
}
```

#### 获取性能指标
```http
GET /admin/api/system/metrics
```

**查询参数**:
- `type`: 指标类型 (memory, cpu, routes, requests)
- `duration`: 时间范围 (1h, 6h, 24h, 7d)

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "type": "requests",
    "duration": "1h",
    "metrics": [
      {
        "timestamp": "2024-09-23T19:00:00Z",
        "value": 150
      },
      {
        "timestamp": "2024-09-23T19:05:00Z",
        "value": 180
      }
    ]
  }
}
```

## Management 监控 API

### 健康检查

#### 应用健康状态
```http
GET /actuator/health
```

**响应示例**:
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 100000000000,
        "free": 50000000000,
        "threshold": 10485760
      }
    },
    "kubernetes": {
      "status": "UP"
    }
  }
}
```

#### 就绪状态检查
```http
GET /actuator/health/readiness
```

#### 存活状态检查
```http
GET /actuator/health/liveness
```

### 应用信息

#### 应用基本信息
```http
GET /actuator/info
```

**响应示例**:
```json
{
  "app": {
    "name": "TiGateway",
    "version": "1.0.0",
    "description": "Spring Cloud Gateway for Kubernetes"
  },
  "build": {
    "version": "1.0.0",
    "time": "2024-09-23T10:00:00Z"
  },
  "git": {
    "commit": {
      "id": "abc123",
      "time": "2024-09-23T10:00:00Z"
    }
  }
}
```

### 指标监控

#### 获取所有指标
```http
GET /actuator/metrics
```

#### 获取特定指标
```http
GET /actuator/metrics/{metricName}
```

**示例**:
```http
GET /actuator/metrics/jvm.memory.used
```

**响应示例**:
```json
{
  "name": "jvm.memory.used",
  "description": "The amount of used memory",
  "baseUnit": "bytes",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 536870912
    }
  ]
}
```

### 配置管理

#### 获取配置属性
```http
GET /actuator/configprops
```

#### 获取环境变量
```http
GET /actuator/env
```

#### 获取日志配置
```http
GET /actuator/loggers
```

#### 动态修改日志级别
```http
POST /actuator/loggers/{loggerName}
Content-Type: application/json

{
  "configuredLevel": "DEBUG"
}
```

## 错误处理

### 错误响应格式
```json
{
  "code": 400,
  "message": "Bad Request",
  "error": "Validation failed",
  "details": [
    {
      "field": "name",
      "message": "Name is required"
    }
  ],
  "timestamp": "2024-09-23T19:00:00Z",
  "path": "/admin/api/apps"
}
```

### 常见错误码
- `400`: 请求参数错误
- `401`: 未授权
- `403`: 禁止访问
- `404`: 资源不存在
- `409`: 资源冲突
- `500`: 服务器内部错误

## API 使用示例

### 使用 curl

#### 创建应用
```bash
curl -X POST http://localhost:8081/admin/api/apps \
  -H "Content-Type: application/json" \
  -d '{
    "name": "user-service",
    "description": "用户服务",
    "version": "1.0.0"
  }'
```

#### 获取路由列表
```bash
curl http://localhost:8080/actuator/gateway/routes
```

#### 刷新路由配置
```bash
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

### 使用 JavaScript

```javascript
// 获取应用列表
async function getApps() {
  const response = await fetch('http://localhost:8081/admin/api/apps');
  const data = await response.json();
  return data.data;
}

// 创建路由
async function createRoute(routeConfig) {
  const response = await fetch('http://localhost:8081/admin/api/routes', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(routeConfig)
  });
  return response.json();
}
```

## API 版本控制

当前 API 版本: `v1`

### 版本策略
- URL 路径版本控制: `/api/v1/`
- 向后兼容性保证
- 废弃通知机制

### 版本升级
当 API 版本升级时，会：
1. 保持旧版本 API 可用
2. 提供迁移指南
3. 设置废弃时间表

---

**相关文档**:
- [CRD API](./crd-api.md)
- [管理 API](./admin-api.md)
- [WebSocket API](./websocket-api.md)
