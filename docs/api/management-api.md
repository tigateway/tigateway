# TiGateway 管理 API 文档

## 概述

TiGateway 管理 API 提供了完整的系统管理功能，包括路由管理、配置管理、监控管理、用户管理、系统管理等。本文档详细说明了管理 API 的端点、参数、响应格式和使用方法。

## 1. 基础信息

### 1.1 基础 URL

```
生产环境: https://tigateway.example.com/admin/api/v1
开发环境: http://localhost:8081/admin/api/v1
```

### 1.2 认证方式

TiGateway 管理 API 支持多种认证方式：

- **JWT Token**: `Authorization: Bearer <token>`
- **API Key**: `X-API-Key: <api-key>`
- **Basic Auth**: `Authorization: Basic <base64-encoded-credentials>`

### 1.3 响应格式

所有 API 响应都遵循统一格式：

```json
{
    "code": "SUCCESS",
    "message": "操作成功",
    "data": {
        // 响应数据
    },
    "timestamp": "2024-09-23T10:00:00Z",
    "requestId": "req-123456789"
}
```

### 1.4 错误响应

```json
{
    "code": "ERROR_CODE",
    "message": "错误描述",
    "details": {
        "field": "fieldName",
        "reason": "具体错误原因"
    },
    "timestamp": "2024-09-23T10:00:00Z",
    "requestId": "req-123456789"
}
```

## 2. 路由管理 API

### 2.1 路由 CRUD 操作

#### 2.1.1 获取所有路由

```http
GET /admin/api/v1/routes
```

**查询参数:**

| 参数 | 类型 | 必需 | 描述 |
|------|------|------|------|
| page | integer | 否 | 页码，默认 1 |
| size | integer | 否 | 每页大小，默认 20 |
| sort | string | 否 | 排序字段 |
| direction | string | 否 | 排序方向 (asc/desc) |
| status | string | 否 | 路由状态过滤 |
| keyword | string | 否 | 搜索关键词 |

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取路由列表成功",
    "data": {
        "content": [
            {
                "id": "user-service-route",
                "uri": "http://user-service:8080",
                "predicates": ["Path=/api/users/**"],
                "filters": ["StripPrefix=2"],
                "status": "ACTIVE",
                "order": 0,
                "createdAt": "2024-09-23T10:00:00Z",
                "updatedAt": "2024-09-23T10:00:00Z"
            }
        ],
        "totalElements": 1,
        "totalPages": 1,
        "currentPage": 1,
        "pageSize": 20,
        "hasNext": false,
        "hasPrevious": false
    }
}
```

#### 2.1.2 获取单个路由

```http
GET /admin/api/v1/routes/{routeId}
```

**路径参数:**

| 参数 | 类型 | 必需 | 描述 |
|------|------|------|------|
| routeId | string | 是 | 路由 ID |

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取路由成功",
    "data": {
        "id": "user-service-route",
        "uri": "http://user-service:8080",
        "predicates": ["Path=/api/users/**"],
        "filters": ["StripPrefix=2"],
        "status": "ACTIVE",
        "order": 0,
        "metadata": {
            "description": "用户服务路由",
            "tags": ["user", "api"]
        },
        "createdAt": "2024-09-23T10:00:00Z",
        "updatedAt": "2024-09-23T10:00:00Z"
    }
}
```

#### 2.1.3 创建路由

```http
POST /admin/api/v1/routes
```

**请求体:**

```json
{
    "id": "new-route",
    "uri": "http://new-service:8080",
    "predicates": ["Path=/api/new/**"],
    "filters": ["StripPrefix=2"],
    "order": 0,
    "metadata": {
        "description": "新服务路由",
        "tags": ["new", "api"]
    }
}
```

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "路由创建成功",
    "data": {
        "id": "new-route",
        "uri": "http://new-service:8080",
        "predicates": ["Path=/api/new/**"],
        "filters": ["StripPrefix=2"],
        "status": "ACTIVE",
        "order": 0,
        "createdAt": "2024-09-23T10:00:00Z",
        "updatedAt": "2024-09-23T10:00:00Z"
    }
}
```

#### 2.1.4 更新路由

```http
PUT /admin/api/v1/routes/{routeId}
```

**请求体:**

```json
{
    "uri": "http://updated-service:8080",
    "predicates": ["Path=/api/updated/**"],
    "filters": ["StripPrefix=2", "AddRequestHeader=X-Service,updated"],
    "order": 1,
    "metadata": {
        "description": "更新的服务路由",
        "tags": ["updated", "api"]
    }
}
```

#### 2.1.5 删除路由

```http
DELETE /admin/api/v1/routes/{routeId}
```

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "路由删除成功",
    "data": null
}
```

### 2.2 路由操作 API

#### 2.2.1 启用/禁用路由

```http
POST /admin/api/v1/routes/{routeId}/enable
POST /admin/api/v1/routes/{routeId}/disable
```

#### 2.2.2 刷新路由

```http
POST /admin/api/v1/routes/refresh
```

#### 2.2.3 批量操作

```http
POST /admin/api/v1/routes/batch
```

**请求体:**

```json
{
    "action": "enable",
    "routeIds": ["route1", "route2", "route3"]
}
```

## 3. 配置管理 API

### 3.1 配置 CRUD 操作

#### 3.1.1 获取配置列表

```http
GET /admin/api/v1/configs
```

**查询参数:**

| 参数 | 类型 | 必需 | 描述 |
|------|------|------|------|
| type | string | 否 | 配置类型 |
| page | integer | 否 | 页码 |
| size | integer | 否 | 每页大小 |

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取配置列表成功",
    "data": {
        "content": [
            {
                "id": "route-config-001",
                "type": "ROUTE_CONFIG",
                "name": "用户服务配置",
                "description": "用户服务相关路由配置",
                "version": "1.0.0",
                "status": "ACTIVE",
                "createdAt": "2024-09-23T10:00:00Z",
                "updatedAt": "2024-09-23T10:00:00Z"
            }
        ],
        "totalElements": 1,
        "totalPages": 1,
        "currentPage": 1,
        "pageSize": 20
    }
}
```

#### 3.1.2 获取配置详情

```http
GET /admin/api/v1/configs/{configId}
```

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取配置成功",
    "data": {
        "id": "route-config-001",
        "type": "ROUTE_CONFIG",
        "name": "用户服务配置",
        "description": "用户服务相关路由配置",
        "version": "1.0.0",
        "status": "ACTIVE",
        "content": {
            "routes": [
                {
                    "id": "user-service-route",
                    "uri": "http://user-service:8080",
                    "predicates": ["Path=/api/users/**"],
                    "filters": ["StripPrefix=2"]
                }
            ],
            "globalFilters": [
                {
                    "name": "AddRequestHeader",
                    "args": {
                        "name": "X-Gateway",
                        "value": "TiGateway"
                    }
                }
            ]
        },
        "createdAt": "2024-09-23T10:00:00Z",
        "updatedAt": "2024-09-23T10:00:00Z"
    }
}
```

#### 3.1.3 创建配置

```http
POST /admin/api/v1/configs
```

**请求体:**

```json
{
    "type": "ROUTE_CONFIG",
    "name": "新配置",
    "description": "新配置描述",
    "content": {
        "routes": [
            {
                "id": "new-route",
                "uri": "http://new-service:8080",
                "predicates": ["Path=/api/new/**"]
            }
        ]
    }
}
```

#### 3.1.4 更新配置

```http
PUT /admin/api/v1/configs/{configId}
```

#### 3.1.5 删除配置

```http
DELETE /admin/api/v1/configs/{configId}
```

### 3.2 配置操作 API

#### 3.2.1 应用配置

```http
POST /admin/api/v1/configs/{configId}/apply
```

#### 3.2.2 回滚配置

```http
POST /admin/api/v1/configs/{configId}/rollback
```

#### 3.2.3 验证配置

```http
POST /admin/api/v1/configs/validate
```

**请求体:**

```json
{
    "type": "ROUTE_CONFIG",
    "content": {
        "routes": [
            {
                "id": "test-route",
                "uri": "http://test-service:8080",
                "predicates": ["Path=/api/test/**"]
            }
        ]
    }
}
```

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "配置验证成功",
    "data": {
        "valid": true,
        "errors": [],
        "warnings": []
    }
}
```

## 4. 监控管理 API

### 4.1 指标查询

#### 4.1.1 获取系统指标

```http
GET /admin/api/v1/metrics/system
```

**查询参数:**

| 参数 | 类型 | 必需 | 描述 |
|------|------|------|------|
| timeRange | string | 否 | 时间范围 (1h, 6h, 24h, 7d) |
| interval | string | 否 | 数据间隔 (1m, 5m, 15m, 1h) |

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取系统指标成功",
    "data": {
        "timestamp": "2024-09-23T10:00:00Z",
        "metrics": {
            "cpu": {
                "usage": 45.2,
                "load": [1.2, 1.5, 1.8]
            },
            "memory": {
                "used": 2048,
                "total": 4096,
                "usage": 50.0
            },
            "disk": {
                "used": 10240,
                "total": 50000,
                "usage": 20.5
            },
            "network": {
                "bytesIn": 1024000,
                "bytesOut": 2048000
            }
        }
    }
}
```

#### 4.1.2 获取应用指标

```http
GET /admin/api/v1/metrics/application
```

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取应用指标成功",
    "data": {
        "timestamp": "2024-09-23T10:00:00Z",
        "metrics": {
            "requests": {
                "total": 10000,
                "success": 9500,
                "error": 500,
                "rate": 100.5
            },
            "responseTime": {
                "avg": 45.2,
                "p50": 35.0,
                "p95": 120.0,
                "p99": 250.0,
                "max": 500.0
            },
            "routes": {
                "active": 25,
                "inactive": 5,
                "error": 2
            },
            "jvm": {
                "heapUsed": 512,
                "heapMax": 1024,
                "gcCount": 10,
                "gcTime": 150
            }
        }
    }
}
```

#### 4.1.3 获取路由指标

```http
GET /admin/api/v1/metrics/routes
```

**查询参数:**

| 参数 | 类型 | 必需 | 描述 |
|------|------|------|------|
| routeId | string | 否 | 路由 ID |
| timeRange | string | 否 | 时间范围 |

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取路由指标成功",
    "data": {
        "timestamp": "2024-09-23T10:00:00Z",
        "routes": [
            {
                "routeId": "user-service-route",
                "metrics": {
                    "requests": {
                        "total": 1000,
                        "success": 950,
                        "error": 50,
                        "rate": 10.5
                    },
                    "responseTime": {
                        "avg": 45.2,
                        "p95": 120.0,
                        "p99": 250.0
                    }
                }
            }
        ]
    }
}
```

### 4.2 告警管理

#### 4.2.1 获取告警列表

```http
GET /admin/api/v1/alerts
```

**查询参数:**

| 参数 | 类型 | 必需 | 描述 |
|------|------|------|------|
| status | string | 否 | 告警状态 (ACTIVE, RESOLVED) |
| severity | string | 否 | 告警级别 (LOW, MEDIUM, HIGH, CRITICAL) |
| page | integer | 否 | 页码 |
| size | integer | 否 | 每页大小 |

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取告警列表成功",
    "data": {
        "content": [
            {
                "id": "alert-001",
                "name": "高错误率告警",
                "description": "API 错误率超过阈值",
                "severity": "HIGH",
                "status": "ACTIVE",
                "condition": "error_rate > 0.05",
                "currentValue": 0.08,
                "threshold": 0.05,
                "triggeredAt": "2024-09-23T09:30:00Z",
                "resolvedAt": null
            }
        ],
        "totalElements": 1,
        "totalPages": 1,
        "currentPage": 1,
        "pageSize": 20
    }
}
```

#### 4.2.2 创建告警规则

```http
POST /admin/api/v1/alerts/rules
```

**请求体:**

```json
{
    "name": "响应时间告警",
    "description": "API 响应时间超过阈值",
    "severity": "MEDIUM",
    "condition": "response_time_p95 > 200",
    "threshold": 200,
    "duration": "5m",
    "enabled": true,
    "notifications": [
        {
            "type": "EMAIL",
            "recipients": ["admin@example.com"]
        },
        {
            "type": "WEBHOOK",
            "url": "https://hooks.slack.com/services/..."
        }
    ]
}
```

#### 4.2.3 更新告警规则

```http
PUT /admin/api/v1/alerts/rules/{ruleId}
```

#### 4.2.4 删除告警规则

```http
DELETE /admin/api/v1/alerts/rules/{ruleId}
```

#### 4.2.5 确认告警

```http
POST /admin/api/v1/alerts/{alertId}/acknowledge
```

## 5. 用户管理 API

### 5.1 用户 CRUD 操作

#### 5.1.1 获取用户列表

```http
GET /admin/api/v1/users
```

**查询参数:**

| 参数 | 类型 | 必需 | 描述 |
|------|------|------|------|
| page | integer | 否 | 页码 |
| size | integer | 否 | 每页大小 |
| role | string | 否 | 角色过滤 |
| status | string | 否 | 状态过滤 |
| keyword | string | 否 | 搜索关键词 |

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取用户列表成功",
    "data": {
        "content": [
            {
                "id": "user-001",
                "username": "admin",
                "email": "admin@example.com",
                "name": "管理员",
                "role": "ADMIN",
                "status": "ACTIVE",
                "lastLoginAt": "2024-09-23T09:00:00Z",
                "createdAt": "2024-09-01T10:00:00Z"
            }
        ],
        "totalElements": 1,
        "totalPages": 1,
        "currentPage": 1,
        "pageSize": 20
    }
}
```

#### 5.1.2 获取用户详情

```http
GET /admin/api/v1/users/{userId}
```

#### 5.1.3 创建用户

```http
POST /admin/api/v1/users
```

**请求体:**

```json
{
    "username": "newuser",
    "email": "newuser@example.com",
    "name": "新用户",
    "role": "USER",
    "password": "password123"
}
```

#### 5.1.4 更新用户

```http
PUT /admin/api/v1/users/{userId}
```

#### 5.1.5 删除用户

```http
DELETE /admin/api/v1/users/{userId}
```

### 5.2 用户操作 API

#### 5.2.1 重置密码

```http
POST /admin/api/v1/users/{userId}/reset-password
```

#### 5.2.2 启用/禁用用户

```http
POST /admin/api/v1/users/{userId}/enable
POST /admin/api/v1/users/{userId}/disable
```

#### 5.2.3 分配角色

```http
POST /admin/api/v1/users/{userId}/roles
```

**请求体:**

```json
{
    "roles": ["USER", "OPERATOR"]
}
```

## 6. 系统管理 API

### 6.1 系统信息

#### 6.1.1 获取系统信息

```http
GET /admin/api/v1/system/info
```

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取系统信息成功",
    "data": {
        "version": "1.0.0",
        "buildTime": "2024-09-23T10:00:00Z",
        "gitCommit": "abc123def456",
        "javaVersion": "11.0.16",
        "springBootVersion": "2.7.14",
        "uptime": "7d 12h 30m",
        "environment": "production"
    }
}
```

#### 6.1.2 获取系统状态

```http
GET /admin/api/v1/system/status
```

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取系统状态成功",
    "data": {
        "status": "UP",
        "components": {
            "database": {
                "status": "UP",
                "details": {
                    "connectionCount": 5,
                    "responseTime": 12
                }
            },
            "redis": {
                "status": "UP",
                "details": {
                    "memoryUsage": "45.2MB",
                    "hitRate": 0.95
                }
            },
            "routes": {
                "status": "UP",
                "details": {
                    "activeRoutes": 25,
                    "totalRoutes": 27
                }
            }
        }
    }
}
```

### 6.2 系统操作

#### 6.2.1 重启系统

```http
POST /admin/api/v1/system/restart
```

#### 6.2.2 刷新配置

```http
POST /admin/api/v1/system/refresh-config
```

#### 6.2.3 清理缓存

```http
POST /admin/api/v1/system/clear-cache
```

**请求体:**

```json
{
    "cacheTypes": ["routes", "configs", "users"]
}
```

### 6.3 日志管理

#### 6.3.1 获取日志列表

```http
GET /admin/api/v1/logs
```

**查询参数:**

| 参数 | 类型 | 必需 | 描述 |
|------|------|------|------|
| level | string | 否 | 日志级别 |
| logger | string | 否 | 日志记录器 |
| startTime | string | 否 | 开始时间 |
| endTime | string | 否 | 结束时间 |
| page | integer | 否 | 页码 |
| size | integer | 否 | 每页大小 |

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取日志列表成功",
    "data": {
        "content": [
            {
                "timestamp": "2024-09-23T10:00:00.123Z",
                "level": "INFO",
                "logger": "ti.gateway.route.RouteService",
                "message": "Route created successfully",
                "thread": "http-nio-8080-exec-1",
                "context": {
                    "routeId": "user-service-route",
                    "operation": "create"
                }
            }
        ],
        "totalElements": 100,
        "totalPages": 5,
        "currentPage": 1,
        "pageSize": 20
    }
}
```

#### 6.3.2 下载日志

```http
GET /admin/api/v1/logs/download
```

**查询参数:**

| 参数 | 类型 | 必需 | 描述 |
|------|------|------|------|
| startTime | string | 是 | 开始时间 |
| endTime | string | 是 | 结束时间 |
| level | string | 否 | 日志级别 |
| format | string | 否 | 下载格式 (json, text) |

## 7. 权限管理 API

### 7.1 角色管理

#### 7.1.1 获取角色列表

```http
GET /admin/api/v1/roles
```

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取角色列表成功",
    "data": [
        {
            "id": "admin",
            "name": "管理员",
            "description": "系统管理员角色",
            "permissions": [
                "ROUTE_CREATE",
                "ROUTE_UPDATE",
                "ROUTE_DELETE",
                "CONFIG_MANAGE",
                "USER_MANAGE"
            ],
            "createdAt": "2024-09-01T10:00:00Z"
        }
    ]
}
```

#### 7.1.2 创建角色

```http
POST /admin/api/v1/roles
```

**请求体:**

```json
{
    "id": "operator",
    "name": "操作员",
    "description": "系统操作员角色",
    "permissions": [
        "ROUTE_VIEW",
        "ROUTE_UPDATE",
        "CONFIG_VIEW"
    ]
}
```

### 7.2 权限管理

#### 7.2.1 获取权限列表

```http
GET /admin/api/v1/permissions
```

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "获取权限列表成功",
    "data": [
        {
            "id": "ROUTE_CREATE",
            "name": "创建路由",
            "description": "创建新路由的权限",
            "resource": "ROUTE",
            "action": "CREATE"
        },
        {
            "id": "ROUTE_UPDATE",
            "name": "更新路由",
            "description": "更新路由的权限",
            "resource": "ROUTE",
            "action": "UPDATE"
        }
    ]
}
```

## 8. 备份和恢复 API

### 8.1 备份管理

#### 8.1.1 创建备份

```http
POST /admin/api/v1/backups
```

**请求体:**

```json
{
    "name": "backup-2024-09-23",
    "description": "系统配置备份",
    "includeData": true,
    "includeConfigs": true,
    "includeRoutes": true
}
```

**响应示例:**

```json
{
    "code": "SUCCESS",
    "message": "备份创建成功",
    "data": {
        "id": "backup-001",
        "name": "backup-2024-09-23",
        "status": "IN_PROGRESS",
        "createdAt": "2024-09-23T10:00:00Z"
    }
}
```

#### 8.1.2 获取备份列表

```http
GET /admin/api/v1/backups
```

#### 8.1.3 下载备份

```http
GET /admin/api/v1/backups/{backupId}/download
```

### 8.2 恢复管理

#### 8.2.1 恢复备份

```http
POST /admin/api/v1/backups/{backupId}/restore
```

**请求体:**

```json
{
    "includeData": true,
    "includeConfigs": true,
    "includeRoutes": true,
    "confirm": true
}
```

## 9. API 使用示例

### 9.1 JavaScript 示例

```javascript
class TiGatewayAdminClient {
    constructor(baseUrl, token) {
        this.baseUrl = baseUrl;
        this.token = token;
    }
    
    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const config = {
            headers: {
                'Authorization': `Bearer ${this.token}`,
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };
        
        const response = await fetch(url, config);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Request failed');
        }
        
        return data;
    }
    
    // 路由管理
    async getRoutes(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/routes?${queryString}`);
    }
    
    async createRoute(route) {
        return this.request('/routes', {
            method: 'POST',
            body: JSON.stringify(route)
        });
    }
    
    async updateRoute(routeId, updates) {
        return this.request(`/routes/${routeId}`, {
            method: 'PUT',
            body: JSON.stringify(updates)
        });
    }
    
    async deleteRoute(routeId) {
        return this.request(`/routes/${routeId}`, {
            method: 'DELETE'
        });
    }
    
    // 配置管理
    async getConfigs(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/configs?${queryString}`);
    }
    
    async createConfig(config) {
        return this.request('/configs', {
            method: 'POST',
            body: JSON.stringify(config)
        });
    }
    
    // 监控管理
    async getSystemMetrics(timeRange = '1h') {
        return this.request(`/metrics/system?timeRange=${timeRange}`);
    }
    
    async getApplicationMetrics() {
        return this.request('/metrics/application');
    }
    
    // 用户管理
    async getUsers(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/users?${queryString}`);
    }
    
    async createUser(user) {
        return this.request('/users', {
            method: 'POST',
            body: JSON.stringify(user)
        });
    }
}

// 使用示例
const client = new TiGatewayAdminClient('http://localhost:8081/admin/api/v1', 'your-jwt-token');

// 获取路由列表
client.getRoutes({ page: 1, size: 20 })
    .then(response => {
        console.log('Routes:', response.data.content);
    })
    .catch(error => {
        console.error('Error:', error.message);
    });

// 创建新路由
client.createRoute({
    id: 'new-route',
    uri: 'http://new-service:8080',
    predicates: ['Path=/api/new/**'],
    filters: ['StripPrefix=2']
})
    .then(response => {
        console.log('Route created:', response.data);
    })
    .catch(error => {
        console.error('Error:', error.message);
    });
```

### 9.2 Python 示例

```python
import requests
import json
from typing import Dict, List, Optional

class TiGatewayAdminClient:
    def __init__(self, base_url: str, token: str):
        self.base_url = base_url
        self.token = token
        self.session = requests.Session()
        self.session.headers.update({
            'Authorization': f'Bearer {token}',
            'Content-Type': 'application/json'
        })
    
    def request(self, endpoint: str, method: str = 'GET', data: Optional[Dict] = None, params: Optional[Dict] = None):
        url = f"{self.base_url}{endpoint}"
        
        response = self.session.request(
            method=method,
            url=url,
            json=data,
            params=params
        )
        
        response.raise_for_status()
        return response.json()
    
    # 路由管理
    def get_routes(self, page: int = 1, size: int = 20, **kwargs) -> Dict:
        params = {'page': page, 'size': size, **kwargs}
        return self.request('/routes', params=params)
    
    def create_route(self, route: Dict) -> Dict:
        return self.request('/routes', method='POST', data=route)
    
    def update_route(self, route_id: str, updates: Dict) -> Dict:
        return self.request(f'/routes/{route_id}', method='PUT', data=updates)
    
    def delete_route(self, route_id: str) -> Dict:
        return self.request(f'/routes/{route_id}', method='DELETE')
    
    # 配置管理
    def get_configs(self, page: int = 1, size: int = 20, **kwargs) -> Dict:
        params = {'page': page, 'size': size, **kwargs}
        return self.request('/configs', params=params)
    
    def create_config(self, config: Dict) -> Dict:
        return self.request('/configs', method='POST', data=config)
    
    # 监控管理
    def get_system_metrics(self, time_range: str = '1h') -> Dict:
        return self.request(f'/metrics/system?timeRange={time_range}')
    
    def get_application_metrics(self) -> Dict:
        return self.request('/metrics/application')
    
    # 用户管理
    def get_users(self, page: int = 1, size: int = 20, **kwargs) -> Dict:
        params = {'page': page, 'size': size, **kwargs}
        return self.request('/users', params=params)
    
    def create_user(self, user: Dict) -> Dict:
        return self.request('/users', method='POST', data=user)

# 使用示例
client = TiGatewayAdminClient('http://localhost:8081/admin/api/v1', 'your-jwt-token')

# 获取路由列表
try:
    response = client.get_routes(page=1, size=20)
    print('Routes:', response['data']['content'])
except requests.exceptions.RequestException as e:
    print('Error:', e)

# 创建新路由
try:
    new_route = {
        'id': 'new-route',
        'uri': 'http://new-service:8080',
        'predicates': ['Path=/api/new/**'],
        'filters': ['StripPrefix=2']
    }
    response = client.create_route(new_route)
    print('Route created:', response['data'])
except requests.exceptions.RequestException as e:
    print('Error:', e)
```

## 10. 错误处理

### 10.1 常见错误代码

| 错误代码 | HTTP 状态码 | 描述 |
|----------|-------------|------|
| `UNAUTHORIZED` | 401 | 未授权访问 |
| `FORBIDDEN` | 403 | 权限不足 |
| `NOT_FOUND` | 404 | 资源不存在 |
| `VALIDATION_ERROR` | 400 | 参数验证失败 |
| `CONFLICT` | 409 | 资源冲突 |
| `INTERNAL_ERROR` | 500 | 内部服务器错误 |

### 10.2 错误处理示例

```javascript
async function handleApiCall(apiCall) {
    try {
        const response = await apiCall();
        return response.data;
    } catch (error) {
        if (error.response) {
            const { status, data } = error.response;
            
            switch (status) {
                case 401:
                    // 重新认证
                    await reauthenticate();
                    return handleApiCall(apiCall);
                case 403:
                    console.error('权限不足:', data.message);
                    break;
                case 404:
                    console.error('资源不存在:', data.message);
                    break;
                case 400:
                    console.error('参数错误:', data.details);
                    break;
                default:
                    console.error('请求失败:', data.message);
            }
        } else {
            console.error('网络错误:', error.message);
        }
        
        throw error;
    }
}
```

---

**相关文档**:
- [REST API 文档](./rest-api.md)
- [WebSocket API 文档](./websocket-api.md)
- [CRD API 文档](./crd-api.md)
- [快速开始指南](../examples/quick-start.md)
