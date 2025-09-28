# REST API Reference

TiGateway provides comprehensive REST API interfaces, supporting management of routes, configurations, monitoring, users, and all other features through HTTP requests.

## API Overview

### Basic Information

- **Base URL**: `http://tigateway-admin:8081/api`
- **Authentication**: JWT Token (Bearer Token)
- **Data Format**: JSON
- **Character Encoding**: UTF-8
- **API Version**: v1

### Authentication

All API requests require a valid JWT Token in the request header:

```http
Authorization: Bearer <your-jwt-token>
```

### Response Format

All API responses follow a unified format:

```json
{
  "code": 200,
  "message": "Success",
  "data": {},
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Error Handling

Error response format:

```json
{
  "code": 400,
  "message": "Bad Request",
  "error": "Invalid request parameters",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Route Management API

### 1. Get Route List

```http
GET /api/v1/routes
```

**Query Parameters**:
- `page`: Page number (default: 1)
- `size`: Page size (default: 20)
- `search`: Search keyword
- `status`: Route status (active, inactive, error)

**Response Example**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "user-service-route",
        "name": "User Service Route",
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

### 2. Create Route

```http
POST /api/v1/routes
```

**Request Body**:
```json
{
  "id": "user-service-route",
  "name": "User Service Route",
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
    "description": "User service route configuration"
  }
}
```

**Response Example**:
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

### 3. Get Route Details

```http
GET /api/v1/routes/{routeId}
```

**Path Parameters**:
- `routeId`: Route ID

**Response Example**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "user-service-route",
    "name": "User Service Route",
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

### 4. Update Route

```http
PUT /api/v1/routes/{routeId}
```

**Path Parameters**:
- `routeId`: Route ID

**Request Body**: Same as create route

**Response Example**:
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

### 5. Delete Route

```http
DELETE /api/v1/routes/{routeId}
```

**Path Parameters**:
- `routeId`: Route ID

**Response Example**:
```json
{
  "code": 200,
  "message": "Route deleted successfully",
  "data": null
}
```

### 6. Batch Route Operations

```http
POST /api/v1/routes/batch
```

**Request Body**:
```json
{
  "action": "delete",
  "routeIds": ["route1", "route2", "route3"]
}
```

**Supported Actions**:
- `delete`: Batch delete
- `activate`: Batch activate
- `deactivate`: Batch deactivate

## Filter Management API

### 1. Get Global Filter List

```http
GET /api/v1/filters/global
```

**Response Example**:
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

### 2. Update Global Filters

```http
PUT /api/v1/filters/global
```

**Request Body**:
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

### 3. Get Available Filter Types

```http
GET /api/v1/filters/types
```

**Response Example**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "name": "AddRequestHeader",
      "description": "Add request header",
      "args": [
        {
          "name": "name",
          "type": "string",
          "required": true,
          "description": "Header name"
        },
        {
          "name": "value",
          "type": "string",
          "required": true,
          "description": "Header value"
        }
      ]
    }
  ]
}
```

## Configuration Management API

### 1. Get System Configuration

```http
GET /api/v1/config
```

**Response Example**:
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

### 2. Update System Configuration

```http
PUT /api/v1/config
```

**Request Body**:
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

### 3. Get Configuration History

```http
GET /api/v1/config/history
```

**Query Parameters**:
- `page`: Page number (default: 1)
- `size`: Page size (default: 20)
- `startTime`: Start time
- `endTime`: End time

**Response Example**:
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
        "reason": "Port conflict"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 4. Rollback Configuration

```http
POST /api/v1/config/rollback/{version}
```

**Path Parameters**:
- `version`: Configuration version

**Request Body**:
```json
{
  "reason": "Rollback to stable version"
}
```

## Monitoring API

### 1. Get System Metrics

```http
GET /api/v1/metrics/system
```

**Response Example**:
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
   