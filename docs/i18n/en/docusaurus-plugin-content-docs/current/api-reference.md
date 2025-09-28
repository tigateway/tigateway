# API Reference

The TiGateway API Reference provides comprehensive API interface documentation, including management APIs, monitoring APIs, configuration APIs, etc., to help developers quickly integrate and use TiGateway.

## API Overview

### API Architecture

```mermaid
graph TB
    subgraph "API Architecture"
        subgraph "Management API"
            ROUTE_API[Route Management API]
            FILTER_API[Filter Management API]
            CONFIG_API[Configuration Management API]
            USER_API[User Management API]
        end
        
        subgraph "Monitoring API"
            METRICS_API[Metrics API]
            HEALTH_API[Health Check API]
            LOG_API[Logging API]
            TRACE_API[Distributed Tracing API]
        end
        
        subgraph "Authentication API"
            AUTH_API[Authentication API]
            TOKEN_API[Token API]
            PERMISSION_API[Permission API]
            ROLE_API[Role API]
        end
        
        subgraph "Extension API"
            EXTENSION_API[Extension Management API]
            PLUGIN_API[Plugin API]
            INTEGRATION_API[Integration API]
            CUSTOM_API[Custom API]
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

### API Version Management

```mermaid
flowchart TD
    A[API Version] --> B[v1.0]
    A --> C[v1.1]
    A --> D[v2.0]
    
    B --> E[Basic Features]
    C --> F[Enhanced Features]
    D --> G[New Features]
    
    E --> H[Route Management]
    E --> I[Filter Management]
    E --> J[User Management]
    
    F --> K[Monitoring Enhancement]
    F --> L[Security Enhancement]
    F --> M[Performance Optimization]
    
    G --> N[Extension Support]
    G --> O[Cloud Native]
    G --> P[AI Integration]
```

## Management API

### 1. Route Management API

#### Get All Routes

```http
GET /api/v1/routes
Authorization: Bearer <token>
Content-Type: application/json
```

**Response Example**:
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

#### Create Route

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

**Response Example**:
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

#### Update Route

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

#### Delete Route

```http
DELETE /api/v1/routes/{routeId}
Authorization: Bearer <token>
```

**Response Example**:
```json
{
  "code": 200,
  "message": "Route deleted successfully",
  "data": null
}
```

### 2. Filter Management API

#### Get All Filters

```http
GET /api/v1/filters
Authorization: Bearer <token>
```

**Response Example**:
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

#### Create Filter

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

#### Update Filter

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

#### Delete Filter

```http
DELETE /api/v1/filters/{filterId}
Authorization: Bearer <token>
```

### 3. Configuration Management API

#### Get System Configuration

```http
GET /api/v1/config
Authorization: Bearer <token>
```

**Response Example**:
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

#### Update System Configuration

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

## Monitoring API

### 1. Metrics API

#### Get System Metrics

```http
GET /api/v1/metrics/system
Authorization: Bearer <token>
```

**Response Example**:
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

#### Get Request Metrics

```http
GET /api/v1/metrics/requests
Authorization: Bearer <token>
```

**Response Example**:
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

### 2. Health Check API

#### Get Health Status

```http
GET /api/v1/health
Authorization: Bearer <token>
```

**Response Example**:
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

#### Get Detailed Health Information

```http
GET /api/v1/health/detailed
Authorization: Bearer <token>
```

**Response Example**:
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

### 3. Logging API

#### Search Logs

```http
GET /api/v1/logs/search?query=error&level=ERROR&startTime=2024-01-01T00:00:00Z&endTime=2024-01-01T23:59:59Z&page=1&size=10
Authorization: Bearer <token>
```

**Response Example**:
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

#### Get Log Statistics

```http
GET /api/v1/logs/statistics?startTime=2024-01-01T00:00:00Z&endTime=2024-01-01T23:59:59Z
Authorization: Bearer <token>
```

**Response Example**:
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

## Authentication API

### 1. User Authentication API

#### User Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

**Response Example**:
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

#### Refresh Token

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response Example**:
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

#### User Logout

```http
POST /api/v1/auth/logout
Authorization: Bearer <token>
```

**Response Example**:
```json
{
  "code": 200,
  "message": "Logout successful",
  "data": null
}
```

### 2. User Management API

#### Get User Information

```http
GET /api/v1/users/me
Authorization: Bearer <token>
```

**Response Example**:
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

#### Get All Users

```http
GET /api/v1/users?page=1&size=10&search=admin
Authorization: Bearer <token>
```

**Response Example**:
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

#### Create User

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

#### Update User

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

#### Delete User

```http
DELETE /api/v1/users/{userId}
Authorization: Bearer <token>
```

### 3. Role Management API

#### Get All Roles

```http
GET /api/v1/roles
Authorization: Bearer <token>
```

**Response Example**:
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

#### Create Role

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

#### Update Role

```http
PUT /api/v1/roles/{roleId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "description": "Updated operator role",
  "permissions": ["read", "write", "delete"]
}
```

#### Delete Role

```http
DELETE /api/v1/roles/{roleId}
Authorization: Bearer <token>
```

## Extension API

### 1. Extension Management API

#### Get All Extensions

```http
GET /api/v1/extensions
Authorization: Bearer <token>
```

**Response Example**:
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

#### Start Extension

```http
POST /api/v1/extensions/{extensionId}/start
Authorization: Bearer <token>
```

**Response Example**:
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

#### Stop Extension

```http
POST /api/v1/extensions/{extensionId}/stop
Authorization: Bearer <token>
```

**Response Example**:
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

### 2. Plugin Management API

#### Get All Plugins

```http
GET /api/v1/plugins
Authorization: Bearer <token>
```

**Response Example**:
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

#### Install Plugin

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

#### Uninstall Plugin

```http
DELETE /api/v1/plugins/{pluginId}
Authorization: Bearer <token>
```

## Error Handling

### 1. Error Code Definition

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| 1000 | 200 | Success |
| 1001 | 400 | Request parameter error |
| 1002 | 401 | Unauthorized |
| 1003 | 403 | Forbidden |
| 1004 | 404 | Resource not found |
| 1005 | 409 | Resource conflict |
| 1006 | 422 | Request parameter validation failed |
| 1007 | 429 | Request rate limit exceeded |
| 1008 | 500 | Internal server error |
| 1009 | 502 | Bad gateway |
| 1010 | 503 | Service unavailable |
| 1011 | 504 | Gateway timeout |

### 2. Error Response Format

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

### 3. Error Handling Examples

#### Parameter Validation Error

```http
POST /api/v1/routes
Authorization: Bearer <token>
Content-Type: application/json

{
  "uri": "lb://user-service"
}
```

**Error Response**:
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

#### Resource Not Found Error

```http
GET /api/v1/routes/non-existent-route
Authorization: Bearer <token>
```

**Error Response**:
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

#### Insufficient Permission Error

```http
DELETE /api/v1/routes/user-service-route
Authorization: Bearer <token>
```

**Error Response**:
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

## API Best Practices

### 1. Authentication and Authorization

```http
# Use Bearer Token authentication
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Use API Key authentication
X-API-Key: your-api-key

# Use Basic authentication
Authorization: Basic YWRtaW46cGFzc3dvcmQ=
```

### 2. Request Headers

```http
# Content type
Content-Type: application/json

# Accept type
Accept: application/json

# Request ID (for tracing)
X-Request-ID: req-123456

# User ID (for auditing)
X-User-ID: user-123

# Client information
X-Client-Version: 1.0.0
X-Client-Platform: web
```

### 3. Pagination and Sorting

```http
# Pagination parameters
GET /api/v1/routes?page=1&size=10

# Sorting parameters
GET /api/v1/routes?sort=createdAt,desc

# Search parameters
GET /api/v1/routes?search=user&status=active

# Filter parameters
GET /api/v1/routes?tags=service,api
```

### 4. Response Handling

```javascript
// Handle success response
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

// Handle error response
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

## Summary

The TiGateway API Reference provides comprehensive API interface documentation:

1. **API Overview**: API architecture and version management
2. **Management API**: Route management, filter management, and configuration management
3. **Monitoring API**: Metrics API, health check API, and logging API
4. **Authentication API**: User authentication, user management, and role management
5. **Extension API**: Extension management and plugin management
6. **Error Handling**: Error code definition, error response format, and error handling examples
7. **API Best Practices**: Authentication and authorization, request headers, pagination and sorting, and response handling

Through the comprehensive API reference, developers can quickly integrate and use various features of TiGateway to build powerful API gateway solutions.
