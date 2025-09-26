# API 参考

TiGateway API 参考提供了完整的 API 接口文档，包括管理 API、监控 API、配置 API 等，帮助开发者快速集成和使用 TiGateway。

## API 概述

### API 架构

```mermaid
graph TB
    subgraph "API 架构"
        subgraph "管理 API"
            ROUTE_API[路由管理 API]
            FILTER_API[过滤器管理 API]
            CONFIG_API[配置管理 API]
            USER_API[用户管理 API]
        end
        
        subgraph "监控 API"
            METRICS_API[指标 API]
            HEALTH_API[健康检查 API]
            LOG_API[日志 API]
            TRACE_API[链路追踪 API]
        end
        
        subgraph "认证 API"
            AUTH_API[认证 API]
            TOKEN_API[令牌 API]
            PERMISSION_API[权限 API]
            ROLE_API[角色 API]
        end
        
        subgraph "扩展 API"
            EXTENSION_API[扩展管理 API]
            PLUGIN_API[插件 API]
            INTEGRATION_API[集成 API]
            CUSTOM_API[自定义 API]
        end
    end
    
    ROUTE_API --> METRICS_API
    FILTER_API --> HEALTH_API
    CONFIG_API --> LOG_API
    USER_API --> TRACE_API
    
    METRICS_API --> AUTH_API
    HEALTH_API --> TOKEN_API
    LOG_API --> PERMISSION_API
    TRACE_API --> ROLE_API
    
    AUTH_API --> EXTENSION_API
    TOKEN_API --> PLUGIN_API
    PERMISSION_API --> INTEGRATION_API
    ROLE_API --> CUSTOM_API
```

### API 版本管理

```mermaid
flowchart TD
    A[API 版本] --> B[v1.0]
    A --> C[v1.1]
    A --> D[v2.0]
    
    B --> E[基础功能]
    C --> F[增强功能]
    D --> G[新特性]
    
    E --> H[路由管理]
    E --> I[过滤器管理]
    E --> J[用户管理]
    
    F --> K[监控增强]
    F --> L[安全增强]
    F --> M[性能优化]
    
    G --> N[扩展支持]
    G --> O[云原生]
    G --> P[AI 集成]
```

## 管理 API

### 1. 路由管理 API

#### 获取所有路由

```http
GET /api/v1/routes
Authorization: Bearer <token>
Content-Type: application/json
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "user-service-route",
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
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z",
        "createdBy": "admin",
        "updatedBy": "admin"
      },
      "status": "active"
    }
  ],
  "pagination": {
    "page": 1,
    "size": 10,
    "total": 1,
    "totalPages": 1
  }
}
```

#### 创建路由

```http
POST /api/v1/routes
Authorization: Bearer <token>
Content-Type: application/json

{
  "id": "order-service-route",
  "uri": "lb://order-service",
  "predicates": [
    {
      "name": "Path",
      "args": {
        "pattern": "/api/orders/**"
      }
    }
  ],
  "filters": [
    {
      "name": "AddRequestHeader",
      "args": {
        "name": "X-Service-Name",
        "value": "order-service"
      }
    }
  ],
  "metadata": {
    "description": "Order service route",
    "tags": ["order", "service"]
  }
}
```

**响应示例**：
```json
{
  "code": 201,
  "message": "Route created successfully",
  "data": {
    "id": "order-service-route",
    "uri": "lb://order-service",
    "predicates": [
      {
        "name": "Path",
        "args": {
          "pattern": "/api/orders/**"
        }
      }
    ],
    "filters": [
      {
        "name": "AddRequestHeader",
        "args": {
          "name": "X-Service-Name",
          "value": "order-service"
        }
      }
    ],
    "metadata": {
      "description": "Order service route",
      "tags": ["order", "service"],
      "createdAt": "2024-01-01T00:00:00Z",
      "createdBy": "admin"
    },
    "status": "active"
  }
}
```

#### 更新路由

```http
PUT /api/v1/routes/{routeId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "uri": "lb://order-service-v2",
  "predicates": [
    {
      "name": "Path",
      "args": {
        "pattern": "/api/v2/orders/**"
      }
    }
  ],
  "filters": [
    {
      "name": "AddRequestHeader",
      "args": {
        "name": "X-Service-Name",
        "value": "order-service-v2"
      }
    }
  ],
  "metadata": {
    "description": "Order service route v2",
    "tags": ["order", "service", "v2"]
  }
}
```

#### 删除路由

```http
DELETE /api/v1/routes/{routeId}
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Route deleted successfully",
  "data": null
}
```

### 2. 过滤器管理 API

#### 获取所有过滤器

```http
GET /api/v1/filters
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "add-header-filter",
      "name": "AddRequestHeader",
      "type": "gateway",
      "description": "Add request header filter",
      "args": {
        "name": "X-Custom-Header",
        "value": "TiGateway"
      },
      "order": 1,
      "status": "active",
      "metadata": {
        "createdAt": "2024-01-01T00:00:00Z",
        "createdBy": "admin"
      }
    }
  ]
}
```

#### 创建过滤器

```http
POST /api/v1/filters
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "CustomHeader",
  "type": "gateway",
  "description": "Custom header filter",
  "args": {
    "headerName": "X-Custom-Header",
    "headerValue": "TiGateway"
  },
  "order": 1
}
```

#### 更新过滤器

```http
PUT /api/v1/filters/{filterId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "args": {
    "headerName": "X-Custom-Header",
    "headerValue": "TiGateway-v2"
  },
  "order": 2
}
```

#### 删除过滤器

```http
DELETE /api/v1/filters/{filterId}
Authorization: Bearer <token>
```

### 3. 配置管理 API

#### 获取系统配置

```http
GET /api/v1/config
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "gateway": {
      "enabled": true,
      "defaultFilters": [
        {
          "name": "AddRequestHeader",
          "args": {
            "name": "X-Gateway-Name",
            "value": "TiGateway"
          }
        }
      ]
    },
    "security": {
      "authentication": {
        "enabled": true,
        "type": "jwt"
      },
      "authorization": {
        "enabled": true,
        "type": "rbac"
      }
    },
    "monitoring": {
      "metrics": {
        "enabled": true,
        "export": {
          "prometheus": {
            "enabled": true
          }
        }
      }
    }
  }
}
```

#### 更新系统配置

```http
PUT /api/v1/config
Authorization: Bearer <token>
Content-Type: application/json

{
  "gateway": {
    "enabled": true,
    "defaultFilters": [
      {
        "name": "AddRequestHeader",
        "args": {
          "name": "X-Gateway-Name",
          "value": "TiGateway-v2"
        }
      }
    ]
  }
}
```

## 监控 API

### 1. 指标 API

#### 获取系统指标

```http
GET /api/v1/metrics/system
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "jvm": {
      "memory": {
        "heap": {
          "used": 512000000,
          "max": 1073741824,
          "usage": 0.48
        },
        "nonHeap": {
          "used": 128000000,
          "max": -1,
          "usage": -1
        }
      },
      "gc": {
        "young": {
          "count": 100,
          "time": 5000
        },
        "old": {
          "count": 10,
          "time": 2000
        }
      },
      "threads": {
        "live": 50,
        "daemon": 10,
        "peak": 60
      }
    },
    "system": {
      "cpu": {
        "usage": 0.25,
        "load": 1.5
      },
      "memory": {
        "total": 8589934592,
        "free": 4294967296,
        "usage": 0.5
      },
      "disk": {
        "total": 107374182400,
        "free": 53687091200,
        "usage": 0.5
      }
    }
  }
}
```

#### 获取请求指标

```http
GET /api/v1/metrics/requests
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "total": {
      "count": 10000,
      "rate": 100.5,
      "duration": {
        "avg": 150.5,
        "p50": 120.0,
        "p95": 300.0,
        "p99": 500.0,
        "max": 1000.0
      }
    },
    "byRoute": [
      {
        "routeId": "user-service-route",
        "count": 5000,
        "rate": 50.0,
        "duration": {
          "avg": 120.0,
          "p50": 100.0,
          "p95": 250.0,
          "p99": 400.0,
          "max": 800.0
        }
      }
    ],
    "byStatus": {
      "200": 9500,
      "400": 200,
      "500": 300
    }
  }
}
```

### 2. 健康检查 API

#### 获取健康状态

```http
GET /api/v1/health
Authorization: Bearer <token>
```

**响应示例**：
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
          "url": "jdbc:postgresql://localhost:5432/tigateway",
          "driver": "PostgreSQL"
        }
      },
      "redis": {
        "status": "UP",
        "details": {
          "host": "localhost",
          "port": 6379
        }
      },
      "externalServices": {
        "status": "UP",
        "details": {
          "user-service": "UP",
          "order-service": "UP"
        }
      }
    },
    "timestamp": "2024-01-01T00:00:00Z"
  }
}
```

#### 获取详细健康信息

```http
GET /api/v1/health/detailed
Authorization: Bearer <token>
```

**响应示例**：
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
          "url": "jdbc:postgresql://localhost:5432/tigateway",
          "driver": "PostgreSQL",
          "version": "13.0",
          "connectionCount": 5,
          "maxConnections": 20
        }
      },
      "redis": {
        "status": "UP",
        "details": {
          "host": "localhost",
          "port": 6379,
          "version": "6.2.0",
          "memory": {
            "used": 1048576,
            "peak": 2097152
          },
          "clients": 10
        }
      }
    },
    "timestamp": "2024-01-01T00:00:00Z"
  }
}
```

### 3. 日志 API

#### 搜索日志

```http
GET /api/v1/logs/search?query=error&level=ERROR&startTime=2024-01-01T00:00:00Z&endTime=2024-01-01T23:59:59Z&page=1&size=10
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "timestamp": "2024-01-01T12:00:00Z",
      "level": "ERROR",
      "logger": "com.tigateway.gateway.GatewayFilter",
      "message": "Filter execution failed",
      "thread": "http-nio-8080-exec-1",
      "exception": "java.lang.RuntimeException: Filter error",
      "context": {
        "requestId": "req-123",
        "userId": "user-456",
        "routeId": "user-service-route"
      }
    }
  ],
  "pagination": {
    "page": 1,
    "size": 10,
    "total": 1,
    "totalPages": 1
  }
}
```

#### 获取日志统计

```http
GET /api/v1/logs/statistics?startTime=2024-01-01T00:00:00Z&endTime=2024-01-01T23:59:59Z
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "total": 10000,
    "byLevel": {
      "ERROR": 100,
      "WARN": 500,
      "INFO": 9000,
      "DEBUG": 400
    },
    "byLogger": {
      "com.tigateway.gateway": 5000,
      "com.tigateway.admin": 3000,
      "com.tigateway.security": 2000
    },
    "byHour": [
      {
        "hour": "00:00",
        "count": 100
      },
      {
        "hour": "01:00",
        "count": 150
      }
    ]
  }
}
```

## 认证 API

### 1. 用户认证 API

#### 用户登录

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "user": {
      "id": "user-123",
      "username": "admin",
      "email": "admin@tigateway.com",
      "roles": ["admin"],
      "permissions": ["*"]
    }
  }
}
```

#### 刷新令牌

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应示例**：
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

#### 用户登出

```http
POST /api/v1/auth/logout
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Logout successful",
  "data": null
}
```

### 2. 用户管理 API

#### 获取用户信息

```http
GET /api/v1/users/me
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "user-123",
    "username": "admin",
    "email": "admin@tigateway.com",
    "firstName": "Admin",
    "lastName": "User",
    "roles": ["admin"],
    "permissions": ["*"],
    "status": "active",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

#### 获取所有用户

```http
GET /api/v1/users?page=1&size=10&search=admin
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "user-123",
      "username": "admin",
      "email": "admin@tigateway.com",
      "firstName": "Admin",
      "lastName": "User",
      "roles": ["admin"],
      "status": "active",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "size": 10,
    "total": 1,
    "totalPages": 1
  }
}
```

#### 创建用户

```http
POST /api/v1/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@tigateway.com",
  "password": "password",
  "firstName": "New",
  "lastName": "User",
  "roles": ["user"]
}
```

#### 更新用户

```http
PUT /api/v1/users/{userId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "email": "updated@tigateway.com",
  "firstName": "Updated",
  "lastName": "User",
  "roles": ["user", "operator"]
}
```

#### 删除用户

```http
DELETE /api/v1/users/{userId}
Authorization: Bearer <token>
```

### 3. 角色管理 API

#### 获取所有角色

```http
GET /api/v1/roles
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "role-123",
      "name": "admin",
      "description": "Administrator role",
      "permissions": ["*"],
      "users": 1,
      "createdAt": "2024-01-01T00:00:00Z"
    },
    {
      "id": "role-456",
      "name": "user",
      "description": "User role",
      "permissions": ["read"],
      "users": 10,
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### 创建角色

```http
POST /api/v1/roles
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "operator",
  "description": "Operator role",
  "permissions": ["read", "write"]
}
```

#### 更新角色

```http
PUT /api/v1/roles/{roleId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "description": "Updated operator role",
  "permissions": ["read", "write", "delete"]
}
```

#### 删除角色

```http
DELETE /api/v1/roles/{roleId}
Authorization: Bearer <token>
```

## 扩展 API

### 1. 扩展管理 API

#### 获取所有扩展

```http
GET /api/v1/extensions
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "extension-123",
      "name": "custom-filter",
      "version": "1.0.0",
      "description": "Custom filter extension",
      "type": "FILTER",
      "status": "STARTED",
      "config": {
        "enabled": true,
        "order": 1
      },
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### 启动扩展

```http
POST /api/v1/extensions/{extensionId}/start
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Extension started successfully",
  "data": {
    "id": "extension-123",
    "status": "STARTED"
  }
}
```

#### 停止扩展

```http
POST /api/v1/extensions/{extensionId}/stop
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Extension stopped successfully",
  "data": {
    "id": "extension-123",
    "status": "STOPPED"
  }
}
```

### 2. 插件管理 API

#### 获取所有插件

```http
GET /api/v1/plugins
Authorization: Bearer <token>
```

**响应示例**：
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": "plugin-123",
      "name": "rate-limiter-plugin",
      "version": "1.0.0",
      "description": "Rate limiter plugin",
      "type": "RATE_LIMITER",
      "status": "INSTALLED",
      "config": {
        "enabled": true,
        "defaultRate": 1000
      },
      "installedAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### 安装插件

```http
POST /api/v1/plugins
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "rate-limiter-plugin",
  "version": "1.0.0",
  "config": {
    "enabled": true,
    "defaultRate": 1000
  }
}
```

#### 卸载插件

```http
DELETE /api/v1/plugins/{pluginId}
Authorization: Bearer <token>
```

## 错误处理

### 1. 错误码定义

| 错误码 | HTTP 状态码 | 描述 |
|--------|-------------|------|
| 1000 | 200 | 成功 |
| 1001 | 400 | 请求参数错误 |
| 1002 | 401 | 未授权 |
| 1003 | 403 | 禁止访问 |
| 1004 | 404 | 资源不存在 |
| 1005 | 409 | 资源冲突 |
| 1006 | 422 | 请求参数验证失败 |
| 1007 | 429 | 请求频率限制 |
| 1008 | 500 | 服务器内部错误 |
| 1009 | 502 | 网关错误 |
| 1010 | 503 | 服务不可用 |
| 1011 | 504 | 网关超时 |

### 2. 错误响应格式

```json
{
  "code": 1001,
  "message": "Request parameter error",
  "details": "The 'id' parameter is required",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/v1/routes",
  "method": "POST"
}
```

### 3. 错误处理示例

#### 参数验证错误

```http
POST /api/v1/routes
Authorization: Bearer <token>
Content-Type: application/json

{
  "uri": "lb://user-service"
}
```

**错误响应**：
```json
{
  "code": 1006,
  "message": "Request parameter validation failed",
  "details": [
    {
      "field": "id",
      "message": "Route ID is required"
    },
    {
      "field": "predicates",
      "message": "At least one predicate is required"
    }
  ],
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/v1/routes",
  "method": "POST"
}
```

#### 资源不存在错误

```http
GET /api/v1/routes/non-existent-route
Authorization: Bearer <token>
```

**错误响应**：
```json
{
  "code": 1004,
  "message": "Resource not found",
  "details": "Route with ID 'non-existent-route' not found",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/v1/routes/non-existent-route",
  "method": "GET"
}
```

#### 权限不足错误

```http
DELETE /api/v1/routes/user-service-route
Authorization: Bearer <token>
```

**错误响应**：
```json
{
  "code": 1003,
  "message": "Access denied",
  "details": "Insufficient permissions to delete routes",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/v1/routes/user-service-route",
  "method": "DELETE"
}
```

## API 最佳实践

### 1. 认证和授权

```http
# 使用 Bearer Token 认证
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# 使用 API Key 认证
X-API-Key: your-api-key

# 使用 Basic 认证
Authorization: Basic YWRtaW46cGFzc3dvcmQ=
```

### 2. 请求头设置

```http
# 内容类型
Content-Type: application/json

# 接受类型
Accept: application/json

# 请求 ID（用于追踪）
X-Request-ID: req-123456

# 用户 ID（用于审计）
X-User-ID: user-123

# 客户端信息
X-Client-Version: 1.0.0
X-Client-Platform: web
```

### 3. 分页和排序

```http
# 分页参数
GET /api/v1/routes?page=1&size=10

# 排序参数
GET /api/v1/routes?sort=createdAt,desc

# 搜索参数
GET /api/v1/routes?search=user&status=active

# 过滤参数
GET /api/v1/routes?tags=service,api
```

### 4. 响应处理

```javascript
// 处理成功响应
fetch('/api/v1/routes')
  .then(response => response.json())
  .then(data => {
    if (data.code === 200) {
      console.log('Success:', data.data);
    } else {
      console.error('Error:', data.message);
    }
  })
  .catch(error => {
    console.error('Network error:', error);
  });

// 处理错误响应
fetch('/api/v1/routes')
  .then(response => {
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
    return response.json();
  })
  .then(data => {
    console.log('Success:', data.data);
  })
  .catch(error => {
    console.error('Error:', error.message);
  });
```

## 总结

TiGateway API 参考提供了完整的 API 接口文档：

1. **API 概述**: API 架构和版本管理
2. **管理 API**: 路由管理、过滤器管理和配置管理
3. **监控 API**: 指标 API、健康检查 API 和日志 API
4. **认证 API**: 用户认证、用户管理和角色管理
5. **扩展 API**: 扩展管理和插件管理
6. **错误处理**: 错误码定义、错误响应格式和错误处理示例
7. **API 最佳实践**: 认证授权、请求头设置、分页排序和响应处理

通过完整的 API 参考，开发者可以快速集成和使用 TiGateway 的各种功能，构建强大的 API 网关解决方案。
