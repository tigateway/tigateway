# TiGateway Admin API

## Overview

TiGateway provides a comprehensive management API for gateway configuration, monitoring, and control. This document describes the Admin API endpoints, authentication methods, and usage examples.

## Authentication

### API Key Authentication

```http
GET /admin/api/v1/health
Authorization: Bearer your-api-key
```

### JWT Token Authentication

```http
GET /admin/api/v1/health
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## API Endpoints

### Health Check

#### Get System Health Status

```http
GET /admin/api/v1/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "gateway": {
      "status": "UP",
      "details": {
        "activeRoutes": 15,
        "totalRequests": 12345
      }
    },
    "database": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    }
  }
}
```

### Route Management

#### Get All Routes

```http
GET /admin/api/v1/routes
```

**Response:**
```json
{
  "status": "success",
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
          "name": "StripPrefix",
          "args": {
            "parts": 2
          }
        }
      ],
      "order": 0,
      "metadata": {
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    }
  ],
  "message": "Operation successful"
}
```

#### Create Route

```http
POST /admin/api/v1/routes
Content-Type: application/json

{
  "id": "new-service-route",
  "uri": "lb://new-service",
  "predicates": [
    {
      "name": "Path",
      "args": {
        "pattern": "/api/new/**"
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

#### Update Route

```http
PUT /admin/api/v1/routes/{id}
Content-Type: application/json

{
  "uri": "lb://updated-service",
  "predicates": [
    {
      "name": "Path",
      "args": {
        "pattern": "/api/updated/**"
      }
    }
  ]
}
```

#### Delete Route

```http
DELETE /admin/api/v1/routes/{id}
```

#### Refresh Routes

```http
POST /admin/api/v1/routes/refresh
```

### Filter Management

#### Get All Filters

```http
GET /admin/api/v1/filters
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "globalFilters": [
      {
        "name": "GlobalCorsFilter",
        "order": -100,
        "enabled": true
      }
    ],
    "routeFilters": [
      {
        "name": "StripPrefix",
        "description": "Strip prefix from request path",
        "factory": "StripPrefixGatewayFilterFactory"
      }
    ]
  },
  "message": "Operation successful"
}
```

#### Get Filter Details

```http
GET /admin/api/v1/filters/{name}
```

#### Update Global Filter

```http
PUT /admin/api/v1/filters/global/{name}
Content-Type: application/json

{
  "enabled": true,
  "order": -50,
  "config": {
    "allowedOrigins": "*",
    "allowedMethods": ["GET", "POST", "PUT", "DELETE"]
  }
}
```

### Metrics and Monitoring

#### Get System Metrics

```http
GET /admin/api/v1/metrics
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "system": {
      "cpuUsage": 45.2,
      "memoryUsage": 67.8,
      "diskUsage": 23.1
    },
    "gateway": {
      "totalRequests": 12345,
      "successfulRequests": 12000,
      "failedRequests": 345,
      "averageResponseTime": 150.5,
      "activeConnections": 25
    },
    "routes": [
      {
        "id": "user-service-route",
        "requests": 5000,
        "successRate": 98.5,
        "averageResponseTime": 120.3
      }
    ]
  },
  "message": "Operation successful"
}
```

#### Get Route Metrics

```http
GET /admin/api/v1/metrics/routes/{id}
```

#### Get Custom Metrics

```http
GET /admin/api/v1/metrics/custom
```

### Configuration Management

#### Get Current Configuration

```http
GET /admin/api/v1/config
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "server": {
      "port": 8080,
      "contextPath": "/"
    },
    "spring": {
      "cloud": {
        "gateway": {
          "routes": [
            {
              "id": "user-service",
              "uri": "lb://user-service",
              "predicates": ["Path=/api/users/**"],
              "filters": ["StripPrefix=2"]
            }
          ]
        }
      }
    }
  },
  "message": "Operation successful"
}
```

#### Update Configuration

```http
PUT /admin/api/v1/config
Content-Type: application/json

{
  "server": {
    "port": 8080
  },
  "spring": {
    "cloud": {
      "gateway": {
        "routes": [
          {
            "id": "updated-route",
            "uri": "lb://updated-service",
            "predicates": ["Path=/api/updated/**"]
          }
        ]
      }
    }
  }
}
```

#### Reload Configuration

```http
POST /admin/api/v1/config/reload
```

### Service Discovery

#### Get Discovered Services

```http
GET /admin/api/v1/services
```

**Response:**
```json
{
  "status": "success",
  "data": [
    {
      "name": "user-service",
      "instances": [
        {
          "host": "user-service-1",
          "port": 8080,
          "healthy": true,
          "metadata": {
            "version": "1.0.0",
            "region": "us-east-1"
          }
        }
      ],
      "loadBalancer": "round_robin"
    }
  ],
  "message": "Operation successful"
}
```

#### Get Service Health

```http
GET /admin/api/v1/services/{name}/health
```

### Log Management

#### Get Log Configuration

```http
GET /admin/api/v1/logs/config
```

#### Update Log Level

```http
PUT /admin/api/v1/logs/level
Content-Type: application/json

{
  "logger": "ti.gateway",
  "level": "DEBUG"
}
```

#### Get Recent Logs

```http
GET /admin/api/v1/logs/recent?lines=100&level=ERROR
```

## Response Format

### Success Response

```json
{
  "status": "success",
  "data": {},
  "message": "Operation successful"
}
```

### Error Response

```json
{
  "status": "error",
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Invalid request parameters",
    "details": "Route ID cannot be empty"
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Error Codes

### HTTP Status Codes

- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Authentication failed
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict
- `500 Internal Server Error` - Server error

### Custom Error Codes

- `INVALID_REQUEST` - Invalid request format
- `ROUTE_NOT_FOUND` - Route does not exist
- `FILTER_NOT_FOUND` - Filter does not exist
- `SERVICE_UNAVAILABLE` - Service temporarily unavailable
- `CONFIGURATION_ERROR` - Configuration error

## Rate Limiting

The Admin API implements rate limiting to prevent abuse:

- **Default Limit**: 100 requests per minute per IP
- **Admin Users**: 1000 requests per minute
- **Burst Capacity**: 200 requests per minute

## Security Considerations

### API Key Management

- Store API keys securely
- Rotate keys regularly
- Use HTTPS for all API calls
- Implement proper access controls

### Authentication Best Practices

- Use strong, unique API keys
- Implement token expiration
- Monitor API usage
- Log all API access

## Usage Examples

### Python Example

```python
import requests

# Set up authentication
headers = {
    'Authorization': 'Bearer your-api-key',
    'Content-Type': 'application/json'
}

# Get system health
response = requests.get(
    'http://localhost:8081/admin/api/v1/health',
    headers=headers
)
print(response.json())

# Create a new route
route_data = {
    "id": "python-test-route",
    "uri": "lb://test-service",
    "predicates": [
        {
            "name": "Path",
            "args": {
                "pattern": "/api/test/**"
            }
        }
    ]
}

response = requests.post(
    'http://localhost:8081/admin/api/v1/routes',
    headers=headers,
    json=route_data
)
print(response.json())
```

### cURL Examples

```bash
# Get system health
curl -H "Authorization: Bearer your-api-key" \
  http://localhost:8081/admin/api/v1/health

# Get all routes
curl -H "Authorization: Bearer your-api-key" \
  http://localhost:8081/admin/api/v1/routes

# Create a new route
curl -X POST \
  -H "Authorization: Bearer your-api-key" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "curl-test-route",
    "uri": "lb://test-service",
    "predicates": [{"name": "Path", "args": {"pattern": "/api/test/**"}}]
  }' \
  http://localhost:8081/admin/api/v1/routes

# Refresh routes
curl -X POST \
  -H "Authorization: Bearer your-api-key" \
  http://localhost:8081/admin/api/v1/routes/refresh
```

---

**Related Documentation**:
- [Admin Interface Guide](../admin-interface.md)
- [API Reference](../api-reference.md)
- [Authentication and Authorization](../authentication-and-authorization.md)
- [Monitoring and Metrics](../monitoring-and-metrics.md)
