# TiGateway REST API Documentation

## Overview

TiGateway provides a comprehensive REST API for managing gateway configurations, monitoring system status, and operating route rules. The API follows RESTful design principles and supports JSON data exchange.

## Base Information

### Service Endpoints
- **Main Gateway API**: `http://localhost:8080`
- **Admin API**: `http://localhost:8081/admin/api`
- **Management API**: `http://localhost:8090/actuator`

### Authentication
```http
Authorization: Bearer <token>
Content-Type: application/json
```

### Response Format
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2024-09-23T19:00:00Z"
}
```

## Gateway Core API

### Route Management

#### Get All Routes
```http
GET /actuator/gateway/routes
```

**Response Example**:
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

#### Get Specific Route
```http
GET /actuator/gateway/routes/{routeId}
```

#### Create Route
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

#### Delete Route
```http
DELETE /actuator/gateway/routes/{routeId}
```

#### Refresh Routes
```http
POST /actuator/gateway/refresh
```

### Filter Management

#### Get Global Filters
```http
GET /actuator/gateway/globalfilters
```

**Response Example**:
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

#### Get Route Filters
```http
GET /actuator/gateway/routefilters
```

## Admin Management API

### Application Information Management

#### Get Application List
```http
GET /admin/api/apps
```

**Query Parameters**:
- `page`: Page number (default: 1)
- `size`: Page size (default: 10)
- `name`: Application name (optional)
- `status`: Application status (optional)

**Response Example**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "app-001",
        "name": "user-service",
        "description": "User Service",
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

#### Create Application
```http
POST /admin/api/apps
Content-Type: application/json

{
  "name": "user-service",
  "description": "User Service",
  "version": "1.0.0",
  "config": {
    "timeout": 30000,
    "retries": 3
  }
}
```

#### Update Application
```http
PUT /admin/api/apps/{appId}
Content-Type: application/json

{
  "name": "user-service",
  "description": "User Service (Updated)",
  "version": "1.1.0",
  "config": {
    "timeout": 30000,
    "retries": 3
  }
}
```

#### Delete Application
```http
DELETE /admin/api/apps/{appId}
```

### Route Configuration Management

#### Get Route Configuration
```http
GET /admin/api/routes
```

**Query Parameters**:
- `appId`: Application ID (optional)
- `status`: Route status (optional)

**Response Example**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "route-001",
      "appId": "app-001",
      "name": "User API Route",
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

#### Create Route Configuration
```http
POST /admin/api/routes
Content-Type: application/json

{
  "appId": "app-001",
  "name": "User API Route",
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

#### Update Route Configuration
```http
PUT /admin/api/routes/{routeId}
Content-Type: application/json

{
  "name": "User API Route (Updated)",
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

#### Delete Route Configuration
```http
DELETE /admin/api/routes/{routeId}
```

### System Monitoring

#### Get System Status
```http
GET /admin/api/system/status
```

**Response Example**:
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

#### Get Performance Metrics
```http
GET /admin/api/system/metrics
```

**Query Parameters**:
- `type`: Metric type (memory, cpu, routes, requests)
- `duration`: Time range (1h, 6h, 24h, 7d)

**Response Example**:
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

## Management Monitoring API

### Health Checks

#### Application Health Status
```http
GET /actuator/health
```

**Response Example**:
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

#### Readiness Status Check
```http
GET /actuator/health/readiness
```

#### Liveness Status Check
```http
GET /actuator/health/liveness
```

### Application Information

#### Application Basic Information
```http
GET /actuator/info
```

**Response Example**:
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

### Metrics Monitoring

#### Get All Metrics
```http
GET /actuator/metrics
```

#### Get Specific Metric
```http
GET /actuator/metrics/{metricName}
```

**Example**:
```http
GET /actuator/metrics/jvm.memory.used
```

**Response Example**:
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

### Configuration Management

#### Get Configuration Properties
```http
GET /actuator/configprops
```

#### Get Environment Variables
```http
GET /actuator/env
```

#### Get Logging Configuration
```http
GET /actuator/loggers
```

#### Dynamic Log Level Modification
```http
POST /actuator/loggers/{loggerName}
Content-Type: application/json

{
  "configuredLevel": "DEBUG"
}
```

## Error Handling

### Error Response Format
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

### Common Error Codes
- `400`: Bad Request
- `401`: Unauthorized
- `403`: Forbidden
- `404`: Not Found
- `409`: Conflict
- `500`: Internal Server Error

## API Usage Examples

### Using curl

#### Create Application
```bash
curl -X POST http://localhost:8081/admin/api/apps \
  -H "Content-Type: application/json" \
  -d '{
    "name": "user-service",
    "description": "User Service",
    "version": "1.0.0"
  }'
```

#### Get Route List
```bash
curl http://localhost:8080/actuator/gateway/routes
```

#### Refresh Route Configuration
```bash
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

### Using JavaScript

```javascript
// Get application list
async function getApps() {
  const response = await fetch('http://localhost:8081/admin/api/apps');
  const data = await response.json();
  return data.data;
}

// Create route
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

## API Version Control

Current API version: `v1`

### Version Strategy
- URL path versioning: `/api/v1/`
- Backward compatibility guarantee
- Deprecation notification mechanism

### Version Upgrade
When API version is upgraded:
1. Keep old version API available
2. Provide migration guide
3. Set deprecation timeline

---

**Related Documentation**:
- [CRD API](./crd-api.md)
- [Management API](./management-api.md)
- [WebSocket API](./websocket-api.md)
