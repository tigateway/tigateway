# REST API 参考

TiGateway 提供了完整的 REST API 接口，支持通过 HTTP 请求管理路由、配置、监控和用户等所有功能。

## API 概述

### 基础信息

- **Base URL**: `http://tigateway-admin:8081/api`
- **认证方式**: JWT Token (Bearer Token)
- **数据格式**: JSON
- **字符编码**: UTF-8
- **API 版本**: v1

### 认证

所有 API 请求都需要在请求头中包含有效的 JWT Token：

```http
Authorization: Bearer <your-jwt-token>
```

### 响应格式

所有 API 响应都遵循统一的格式：

```json
{
  "code": 200,
  "message": "Success",
  "data": {},
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 错误处理

错误响应格式：

```json
{
  "code": 400,
  "message": "Bad Request",
  "error": "Invalid request parameters",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## 路由管理 API

### 1. 获取路由列表

```http
GET /api/v1/routes
```

**查询参数**:
- `page`: 页码 (默认: 1)
- `size`: 每页大小 (默认: 20)
- `search`: 搜索关键词
- `status`: 路由状态 (active, inactive, error)

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "user-service-route",
        "name": "用户服务路由",
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
            "name": "AddRequestHeader",
            "args": {
              "name": "X-Service-Name",
              "value": "user-service"
            }
          }
        ],
        "status": "active",
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

### 2. 创建路由

```http
POST /api/v1/routes
```

**请求体**:
```json
{
  "id": "user-service-route",
  "name": "用户服务路由",
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
      "name": "AddRequestHeader",
      "args": {
        "name": "X-Service-Name",
        "value": "user-service"
      }
    }
  ],
  "metadata": {
    "description": "用户服务路由配置"
  }
}
```

**响应示例**:
```json
{
  "code": 201,
  "message": "Route created successfully",
  "data": {
    "id": "user-service-route",
    "status": "active"
  }
}
```

### 3. 获取路由详情

```http
GET /api/v1/routes/{routeId}
```

**路径参数**:
- `routeId`: 路由 ID

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "user-service-route",
    "name": "用户服务路由",
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
        "name": "AddRequestHeader",
        "args": {
          "name": "X-Service-Name",
          "value": "user-service"
        }
      }
    ],
    "status": "active",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

### 4. 更新路由

```http
PUT /api/v1/routes/{routeId}
```

**路径参数**:
- `routeId`: 路由 ID

**请求体**: 与创建路由相同

**响应示例**:
```json
{
  "code": 200,
  "message": "Route updated successfully",
  "data": {
    "id": "user-service-route",
    "status": "active"
  }
}
```

### 5. 删除路由

```http
DELETE /api/v1/routes/{routeId}
```

**路径参数**:
- `routeId`: 路由 ID

**响应示例**:
```json
{
  "code": 200,
  "message": "Route deleted successfully",
  "data": null
}
```

### 6. 批量操作路由

```http
POST /api/v1/routes/batch
```

**请求体**:
```json
{
  "action": "delete",
  "routeIds": ["route1", "route2", "route3"]
}
```

**支持的操作**:
- `delete`: 批量删除
- `activate`: 批量激活
- `deactivate`: 批量停用

## 过滤器管理 API

### 1. 获取全局过滤器列表

```http
GET /api/v1/filters/global
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "name": "AddRequestHeader",
      "args": {
        "name": "X-Global-Header",
        "value": "tigateway"
      },
      "order": 1
    }
  ]
}
```

### 2. 更新全局过滤器

```http
PUT /api/v1/filters/global
```

**请求体**:
```json
[
  {
    "name": "AddRequestHeader",
    "args": {
      "name": "X-Global-Header",
      "value": "tigateway"
    },
    "order": 1
  }
]
```

### 3. 获取可用过滤器类型

```http
GET /api/v1/filters/types
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "name": "AddRequestHeader",
      "description": "添加请求头",
      "args": [
        {
          "name": "name",
          "type": "string",
          "required": true,
          "description": "请求头名称"
        },
        {
          "name": "value",
          "type": "string",
          "required": true,
          "description": "请求头值"
        }
      ]
    }
  ]
}
```

## 配置管理 API

### 1. 获取系统配置

```http
GET /api/v1/config
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "server": {
      "port": 8080,
      "contextPath": "/api"
    },
    "connection": {
      "timeout": 30000,
      "readTimeout": 60000
    },
    "logging": {
      "level": "INFO",
      "maxFileSize": 100
    }
  }
}
```

### 2. 更新系统配置

```http
PUT /api/v1/config
```

**请求体**:
```json
{
  "server": {
    "port": 8080,
    "contextPath": "/api"
  },
  "connection": {
    "timeout": 30000,
    "readTimeout": 60000
  },
  "logging": {
    "level": "INFO",
    "maxFileSize": 100
  }
}
```

### 3. 获取配置历史

```http
GET /api/v1/config/history
```

**查询参数**:
- `page`: 页码 (默认: 1)
- `size`: 每页大小 (默认: 20)
- `startTime`: 开始时间
- `endTime`: 结束时间

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "config-1",
        "version": "1.0.1",
        "changes": {
          "server.port": {
            "old": 8080,
            "new": 8081
          }
        },
        "changedBy": "admin",
        "changedAt": "2024-01-01T00:00:00Z",
        "reason": "端口冲突"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 4. 回滚配置

```http
POST /api/v1/config/rollback/{version}
```

**路径参数**:
- `version`: 配置版本

**请求体**:
```json
{
  "reason": "回滚到稳定版本"
}
```

## 监控 API

### 1. 获取系统指标

```http
GET /api/v1/metrics/system
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "jvm": {
      "memory": {
        "used": 512000000,
        "max": 1024000000,
        "usage": 0.5
      },
      "threads": {
        "live": 50,
        "daemon": 10
      },
      "gc": {
        "collections": 100,
        "time": 5000
      }
    },
    "system": {
      "cpu": {
        "usage": 0.3
      },
      "memory": {
        "total": 8589934592,
        "free": 4294967296
      },
      "disk": {
        "total": 107374182400,
        "free": 53687091200
      }
    }
  }
}
```

### 2. 获取请求指标

```http
GET /api/v1/metrics/requests
```

**查询参数**:
- `startTime`: 开始时间
- `endTime`: 结束时间
- `interval`: 时间间隔 (1m, 5m, 1h, 1d)

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "totalRequests": 10000,
    "errorRequests": 100,
    "errorRate": 0.01,
    "avgResponseTime": 150,
    "p95ResponseTime": 300,
    "p99ResponseTime": 500,
    "activeRequests": 50,
    "timeSeries": [
      {
        "timestamp": "2024-01-01T00:00:00Z",
        "requests": 100,
        "errors": 1,
        "avgResponseTime": 150
      }
    ]
  }
}
```

### 3. 获取路由指标

```http
GET /api/v1/metrics/routes
```

**查询参数**:
- `routeId`: 路由 ID (可选)
- `startTime`: 开始时间
- `endTime`: 结束时间

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "routeId": "user-service-route",
      "totalRequests": 5000,
      "errorRequests": 50,
      "errorRate": 0.01,
      "avgResponseTime": 120,
      "p95ResponseTime": 250,
      "p99ResponseTime": 400
    }
  ]
}
```

### 4. 获取健康状态

```http
GET /api/v1/health
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "status": "UP",
    "components": {
      "database": {
        "status": "UP",
        "details": {
          "connection": "healthy"
        }
      },
      "redis": {
        "status": "UP",
        "details": {
          "connection": "healthy"
        }
      },
      "externalServices": {
        "status": "UP",
        "details": {
          "user-service": "healthy",
          "order-service": "healthy"
        }
      }
    }
  }
}
```

## 日志管理 API

### 1. 搜索日志

```http
GET /api/v1/logs/search
```

**查询参数**:
- `level`: 日志级别 (DEBUG, INFO, WARN, ERROR)
- `message`: 消息关键词
- `startTime`: 开始时间
- `endTime`: 结束时间
- `page`: 页码 (默认: 1)
- `size`: 每页大小 (默认: 20)

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "timestamp": "2024-01-01T00:00:00Z",
        "level": "INFO",
        "logger": "ti.gateway.RequestLoggingFilter",
        "message": "Request completed",
        "thread": "http-nio-8080-exec-1",
        "requestId": "req-123",
        "method": "GET",
        "path": "/api/users",
        "statusCode": 200,
        "duration": 150
      }
    ],
    "totalElements": 1000,
    "totalPages": 50
  }
}
```

### 2. 获取日志统计

```http
GET /api/v1/logs/statistics
```

**查询参数**:
- `startTime`: 开始时间
- `endTime`: 结束时间
- `interval`: 时间间隔

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "totalLogs": 10000,
    "levelDistribution": {
      "DEBUG": 1000,
      "INFO": 8000,
      "WARN": 800,
      "ERROR": 200
    },
    "timeSeries": [
      {
        "timestamp": "2024-01-01T00:00:00Z",
        "count": 100,
        "levels": {
          "DEBUG": 10,
          "INFO": 80,
          "WARN": 8,
          "ERROR": 2
        }
      }
    ]
  }
}
```

### 3. 导出日志

```http
POST /api/v1/logs/export
```

**请求体**:
```json
{
  "startTime": "2024-01-01T00:00:00Z",
  "endTime": "2024-01-02T00:00:00Z",
  "level": "ERROR",
  "format": "json"
}
```

**响应**: 返回文件下载链接

## 链路追踪 API

### 1. 搜索追踪

```http
GET /api/v1/traces/search
```

**查询参数**:
- `serviceName`: 服务名称
- `operationName`: 操作名称
- `startTime`: 开始时间
- `endTime`: 结束时间
- `traceId`: 追踪 ID

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "traceId": "trace-123",
      "serviceName": "tigateway",
      "operationName": "gateway-request",
      "startTime": "2024-01-01T00:00:00Z",
      "duration": 150,
      "spans": [
        {
          "spanId": "span-1",
          "operationName": "gateway-request",
          "startTime": "2024-01-01T00:00:00Z",
          "duration": 150,
          "tags": {
            "http.method": "GET",
            "http.url": "/api/users",
            "http.status_code": "200"
          }
        }
      ]
    }
  ]
}
```

### 2. 获取追踪详情

```http
GET /api/v1/traces/{traceId}
```

**路径参数**:
- `traceId`: 追踪 ID

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "traceId": "trace-123",
    "serviceName": "tigateway",
    "operationName": "gateway-request",
    "startTime": "2024-01-01T00:00:00Z",
    "duration": 150,
    "spans": [
      {
        "spanId": "span-1",
        "parentSpanId": null,
        "operationName": "gateway-request",
        "startTime": "2024-01-01T00:00:00Z",
        "duration": 150,
        "tags": {
          "http.method": "GET",
          "http.url": "/api/users",
          "http.status_code": "200"
        },
        "logs": [
          {
            "timestamp": "2024-01-01T00:00:00Z",
            "fields": {
              "event": "request_start"
            }
          }
        ]
      }
    ]
  }
}
```

### 3. 获取服务依赖

```http
GET /api/v1/traces/dependencies
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "parent": "tigateway",
      "child": "user-service",
      "callCount": 1000,
      "avgDuration": 120
    },
    {
      "parent": "tigateway",
      "child": "order-service",
      "callCount": 500,
      "avgDuration": 200
    }
  ]
}
```

## 用户管理 API

### 1. 获取用户列表

```http
GET /api/v1/users
```

**查询参数**:
- `page`: 页码 (默认: 1)
- `size`: 每页大小 (默认: 20)
- `search`: 搜索关键词
- `role`: 角色筛选

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "user-1",
        "username": "admin",
        "email": "admin@tigateway.com",
        "roles": ["ADMIN"],
        "status": "ACTIVE",
        "lastLogin": "2024-01-01T00:00:00Z",
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 2. 创建用户

```http
POST /api/v1/users
```

**请求体**:
```json
{
  "username": "newuser",
  "email": "newuser@tigateway.com",
  "password": "password123",
  "roles": ["USER"],
  "status": "ACTIVE"
}
```

### 3. 更新用户

```http
PUT /api/v1/users/{userId}
```

**路径参数**:
- `userId`: 用户 ID

**请求体**:
```json
{
  "email": "updated@tigateway.com",
  "roles": ["USER", "OPERATOR"],
  "status": "ACTIVE"
}
```

### 4. 删除用户

```http
DELETE /api/v1/users/{userId}
```

**路径参数**:
- `userId`: 用户 ID

### 5. 获取角色列表

```http
GET /api/v1/roles
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "name": "ADMIN",
      "description": "系统管理员",
      "permissions": ["*"]
    },
    {
      "name": "USER",
      "description": "普通用户",
      "permissions": ["route:read", "config:read"]
    }
  ]
}
```

### 6. 获取权限列表

```http
GET /api/v1/permissions
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "resource": "route",
      "actions": ["read", "write", "delete"]
    },
    {
      "resource": "config",
      "actions": ["read", "write"]
    }
  ]
}
```

## 认证 API

### 1. 用户登录

```http
POST /api/v1/auth/login
```

**请求体**:
```json
{
  "username": "admin",
  "password": "password123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "user": {
      "id": "user-1",
      "username": "admin",
      "email": "admin@tigateway.com",
      "roles": ["ADMIN"]
    }
  }
}
```

### 2. 刷新 Token

```http
POST /api/v1/auth/refresh
```

**请求头**:
```http
Authorization: Bearer <current-token>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Token refreshed successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  }
}
```

### 3. 用户登出

```http
POST /api/v1/auth/logout
```

**请求头**:
```http
Authorization: Bearer <token>
```

### 4. 获取当前用户信息

```http
GET /api/v1/auth/me
```

**请求头**:
```http
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "user-1",
    "username": "admin",
    "email": "admin@tigateway.com",
    "roles": ["ADMIN"],
    "permissions": ["*"]
  }
}
```

## WebSocket API

### 1. 实时监控连接

```javascript
const ws = new WebSocket('ws://tigateway-admin:8081/ws/monitoring');

ws.onopen = function() {
  console.log('WebSocket 连接已建立');
};

ws.onmessage = function(event) {
  const data = JSON.parse(event.data);
  console.log('收到数据:', data);
};

ws.onclose = function() {
  console.log('WebSocket 连接已关闭');
};
```

**消息格式**:
```json
{
  "type": "metrics",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": {
    "requests": 100,
    "errors": 1,
    "avgResponseTime": 150
  }
}
```

### 2. 实时日志连接

```javascript
const ws = new WebSocket('ws://tigateway-admin:8081/ws/logs');

ws.onmessage = function(event) {
  const logEntry = JSON.parse(event.data);
  console.log('收到日志:', logEntry);
};
```

## 错误码参考

| 错误码 | 说明 | 描述 |
|--------|------|------|
| 200 | Success | 请求成功 |
| 201 | Created | 资源创建成功 |
| 400 | Bad Request | 请求参数错误 |
| 401 | Unauthorized | 未授权访问 |
| 403 | Forbidden | 权限不足 |
| 404 | Not Found | 资源不存在 |
| 409 | Conflict | 资源冲突 |
| 422 | Unprocessable Entity | 请求格式正确但语义错误 |
| 500 | Internal Server Error | 服务器内部错误 |
| 503 | Service Unavailable | 服务不可用 |

## 最佳实践

### 1. 请求优化

```javascript
// 使用连接池
const axios = require('axios');
const client = axios.create({
  baseURL: 'http://tigateway-admin:8081/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 添加请求拦截器
client.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// 添加响应拦截器
client.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      // 处理认证失败
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 2. 错误处理

```javascript
async function createRoute(routeData) {
  try {
    const response = await client.post('/routes', routeData);
    return response.data;
  } catch (error) {
    if (error.response) {
      // 服务器响应错误
      const { code, message, error: errorDetail } = error.response.data;
      throw new Error(`${message}: ${errorDetail}`);
    } else if (error.request) {
      // 网络错误
      throw new Error('网络连接失败，请检查网络设置');
    } else {
      // 其他错误
      throw new Error('请求配置错误');
    }
  }
}
```

### 3. 分页处理

```javascript
async function getRoutes(page = 1, size = 20, search = '') {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString()
  });
  
  if (search) {
    params.append('search', search);
  }
  
  const response = await client.get(`/routes?${params}`);
  return response.data;
}
```

## 总结

TiGateway 的 REST API 提供了完整的管理功能：

1. **路由管理**: 创建、更新、删除和查询路由
2. **过滤器管理**: 管理全局过滤器和过滤器类型
3. **配置管理**: 系统配置和历史管理
4. **监控 API**: 系统指标、请求指标和健康状态
5. **日志管理**: 日志搜索、统计和导出
6. **链路追踪**: 追踪搜索和依赖分析
7. **用户管理**: 用户、角色和权限管理
8. **认证 API**: 登录、刷新和登出
9. **WebSocket**: 实时监控和日志推送

通过 REST API，开发者可以轻松集成 TiGateway 的管理功能到自己的应用中，实现自动化的网关管理。
