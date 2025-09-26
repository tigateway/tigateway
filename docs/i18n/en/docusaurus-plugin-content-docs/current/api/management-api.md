# TiGateway Management API Documentation

## Overview

TiGateway provides a comprehensive Management API for administrative operations, including route management, configuration updates, monitoring data access, and system administration. This API is designed for administrative interfaces, monitoring systems, and automated management tools.

## Base URL

```
http://localhost:8081/actuator/tigateway
```

## Authentication

### JWT Token Authentication

```bash
# Get authentication token
curl -X POST http://localhost:8081/actuator/tigateway/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'

# Use token in subsequent requests
curl -H "Authorization: Bearer <token>" \
  http://localhost:8081/actuator/tigateway/routes
```

### API Key Authentication

```bash
# Use API key in header
curl -H "X-API-Key: your-api-key" \
  http://localhost:8081/actuator/tigateway/routes
```

## 1. Route Management API

### 1.1 Get All Routes

```http
GET /actuator/tigateway/routes
```

**Response:**
```json
{
  "routes": [
    {
      "id": "user-service-route",
      "uri": "http://user-service:8080",
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
        "description": "User service route",
        "tags": ["user", "api"]
      },
      "status": "ACTIVE",
      "createdAt": "2024-09-23T10:00:00Z",
      "updatedAt": "2024-09-23T10:00:00Z"
    }
  ],
  "total": 1,
  "page": 0,
  "size": 20
}
```

### 1.2 Get Route by ID

```http
GET /actuator/tigateway/routes/{routeId}
```

**Response:**
```json
{
  "id": "user-service-route",
  "uri": "http://user-service:8080",
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
    "description": "User service route",
    "tags": ["user", "api"]
  },
  "status": "ACTIVE",
  "createdAt": "2024-09-23T10:00:00Z",
  "updatedAt": "2024-09-23T10:00:00Z"
}
```

### 1.3 Create Route

```http
POST /actuator/tigateway/routes
Content-Type: application/json
```

**Request Body:**
```json
{
  "id": "new-route",
  "uri": "http://new-service:8080",
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
  ],
  "order": 1,
  "metadata": {
    "description": "New service route",
    "tags": ["new", "api"]
  }
}
```

**Response:**
```json
{
  "id": "new-route",
  "uri": "http://new-service:8080",
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
  ],
  "order": 1,
  "metadata": {
    "description": "New service route",
    "tags": ["new", "api"]
  },
  "status": "ACTIVE",
  "createdAt": "2024-09-23T10:00:00Z",
  "updatedAt": "2024-09-23T10:00:00Z"
}
```

### 1.4 Update Route

```http
PUT /actuator/tigateway/routes/{routeId}
Content-Type: application/json
```

**Request Body:**
```json
{
  "uri": "http://updated-service:8080",
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
        "name": "X-Service",
        "value": "updated-service"
      }
    }
  ],
  "metadata": {
    "description": "Updated service route",
    "tags": ["updated", "api"]
  }
}
```

### 1.5 Delete Route

```http
DELETE /actuator/tigateway/routes/{routeId}
```

**Response:**
```json
{
  "message": "Route deleted successfully",
  "routeId": "route-to-delete"
}
```

### 1.6 Refresh Routes

```http
POST /actuator/tigateway/routes/refresh
```

**Response:**
```json
{
  "message": "Routes refreshed successfully",
  "timestamp": "2024-09-23T10:00:00Z",
  "routesCount": 25
}
```

## 2. Configuration Management API

### 2.1 Get Configuration

```http
GET /actuator/tigateway/config
```

**Response:**
```json
{
  "config": {
    "routes": [
      {
        "id": "user-service-route",
        "uri": "http://user-service:8080",
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
    ],
    "globalFilters": [
      {
        "name": "AddRequestHeader",
        "args": {
          "name": "X-Gateway",
          "value": "TiGateway"
        }
      }
    ],
    "loadBalancer": {
      "type": "ROUND_ROBIN",
      "healthCheck": {
        "enabled": true,
        "path": "/health",
        "interval": "10s",
        "timeout": "5s"
      }
    }
  },
  "version": "1.0.0",
  "lastUpdated": "2024-09-23T10:00:00Z"
}
```

### 2.2 Update Configuration

```http
PUT /actuator/tigateway/config
Content-Type: application/json
```

**Request Body:**
```json
{
  "config": {
    "routes": [
      {
        "id": "updated-route",
        "uri": "http://updated-service:8080",
        "predicates": [
          {
            "name": "Path",
            "args": {
              "pattern": "/api/updated/**"
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
  }
}
```

### 2.3 Validate Configuration

```http
POST /actuator/tigateway/config/validate
Content-Type: application/json
```

**Request Body:**
```json
{
  "config": {
    "routes": [
      {
        "id": "test-route",
        "uri": "http://test-service:8080",
        "predicates": [
          {
            "name": "Path",
            "args": {
              "pattern": "/api/test/**"
            }
          }
        ]
      }
    ]
  }
}
```

**Response:**
```json
{
  "valid": true,
  "errors": [],
  "warnings": []
}
```

## 3. Monitoring API

### 3.1 Get System Metrics

```http
GET /actuator/tigateway/metrics
```

**Response:**
```json
{
  "timestamp": "2024-09-23T10:00:00Z",
  "metrics": {
    "requests": {
      "total": 10000,
      "success": 9500,
      "error": 500,
      "rate": 100.5,
      "ratePerSecond": 100.5
    },
    "responseTime": {
      "avg": 45.2,
      "p50": 35.0,
      "p95": 120.0,
      "p99": 250.0,
      "max": 500.0,
      "min": 5.0
    },
    "routes": {
      "active": 25,
      "inactive": 5,
      "error": 2,
      "total": 32
    },
    "system": {
      "cpuUsage": 45.2,
      "memoryUsage": 68.5,
      "gcCount": 10,
      "gcTime": 150,
      "threadCount": 50
    }
  }
}
```

### 3.2 Get Route Metrics

```http
GET /actuator/tigateway/metrics/routes
```

**Response:**
```json
{
  "timestamp": "2024-09-23T10:00:00Z",
  "routes": [
    {
      "routeId": "user-service-route",
      "metrics": {
        "requests": {
          "total": 5000,
          "success": 4800,
          "error": 200,
          "rate": 50.0
        },
        "responseTime": {
          "avg": 40.0,
          "p50": 30.0,
          "p95": 100.0,
          "p99": 200.0
        }
      }
    },
    {
      "routeId": "order-service-route",
      "metrics": {
        "requests": {
          "total": 3000,
          "success": 2900,
          "error": 100,
          "rate": 30.0
        },
        "responseTime": {
          "avg": 50.0,
          "p50": 40.0,
          "p95": 120.0,
          "p99": 250.0
        }
      }
    }
  ]
}
```

### 3.3 Get Health Status

```http
GET /actuator/tigateway/health
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2024-09-23T10:00:00Z",
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
```

## 4. Log Management API

### 4.1 Get Logs

```http
GET /actuator/tigateway/logs?level=INFO&limit=100&offset=0
```

**Query Parameters:**
- `level`: Log level (DEBUG, INFO, WARN, ERROR)
- `limit`: Maximum number of logs to return
- `offset`: Offset for pagination
- `logger`: Logger name filter
- `startTime`: Start time filter (ISO 8601 format)
- `endTime`: End time filter (ISO 8601 format)

**Response:**
```json
{
  "logs": [
    {
      "level": "INFO",
      "logger": "ti.gateway.route.RouteService",
      "message": "Route created successfully",
      "thread": "http-nio-8080-exec-1",
      "timestamp": "2024-09-23T10:00:00.123Z",
      "context": {
        "routeId": "user-service-route",
        "operation": "create",
        "duration": 45
      }
    }
  ],
  "total": 1000,
  "page": 0,
  "size": 100
}
```

### 4.2 Update Log Level

```http
PUT /actuator/tigateway/logs/level
Content-Type: application/json
```

**Request Body:**
```json
{
  "logger": "ti.gateway.route.RouteService",
  "level": "DEBUG"
}
```

**Response:**
```json
{
  "message": "Log level updated successfully",
  "logger": "ti.gateway.route.RouteService",
  "level": "DEBUG"
}
```

## 5. System Administration API

### 5.1 Get System Info

```http
GET /actuator/tigateway/system
```

**Response:**
```json
{
  "system": {
    "version": "1.0.0",
    "buildTime": "2024-09-23T10:00:00Z",
    "gitCommit": "abc123def456",
    "javaVersion": "17.0.2",
    "springBootVersion": "2.7.0",
    "springCloudVersion": "2021.0.3"
  },
  "environment": {
    "profiles": ["kubernetes", "production"],
    "configSources": ["configmap", "application.yml"],
    "activeConfig": "tigateway-config"
  },
  "runtime": {
    "uptime": "2d 5h 30m",
    "startTime": "2024-09-21T04:30:00Z",
    "jvmUptime": 180000000
  }
}
```

### 5.2 Get Environment Variables

```http
GET /actuator/tigateway/env
```

**Response:**
```json
{
  "environment": {
    "SPRING_PROFILES_ACTIVE": "kubernetes,production",
    "SERVER_PORT": "8080",
    "MANAGEMENT_SERVER_PORT": "8081",
    "CONFIG_STORAGE_TYPE": "configmap",
    "LOG_LEVEL": "INFO"
  }
}
```

### 5.3 Restart Application

```http
POST /actuator/tigateway/restart
```

**Response:**
```json
{
  "message": "Application restart initiated",
  "timestamp": "2024-09-23T10:00:00Z"
}
```

### 5.4 Shutdown Application

```http
POST /actuator/tigateway/shutdown
```

**Response:**
```json
{
  "message": "Application shutdown initiated",
  "timestamp": "2024-09-23T10:00:00Z"
}
```

## 6. Cache Management API

### 6.1 Get Cache Statistics

```http
GET /actuator/tigateway/cache
```

**Response:**
```json
{
  "caches": [
    {
      "name": "routeCache",
      "size": 25,
      "hitRate": 0.95,
      "missRate": 0.05,
      "evictionCount": 0,
      "loadTime": 45
    },
    {
      "name": "configCache",
      "size": 1,
      "hitRate": 0.98,
      "missRate": 0.02,
      "evictionCount": 0,
      "loadTime": 12
    }
  ]
}
```

### 6.2 Clear Cache

```http
DELETE /actuator/tigateway/cache/{cacheName}
```

**Response:**
```json
{
  "message": "Cache cleared successfully",
  "cacheName": "routeCache",
  "timestamp": "2024-09-23T10:00:00Z"
}
```

### 6.3 Clear All Caches

```http
DELETE /actuator/tigateway/cache
```

**Response:**
```json
{
  "message": "All caches cleared successfully",
  "timestamp": "2024-09-23T10:00:00Z"
}
```

## 7. Security Management API

### 7.1 Get Security Configuration

```http
GET /actuator/tigateway/security
```

**Response:**
```json
{
  "security": {
    "authentication": {
      "enabled": true,
      "type": "JWT",
      "jwtSecret": "***",
      "tokenExpiry": "1h"
    },
    "authorization": {
      "enabled": true,
      "rbac": {
        "enabled": true,
        "roles": ["ADMIN", "USER", "VIEWER"]
      }
    },
    "cors": {
      "enabled": true,
      "allowedOrigins": ["*"],
      "allowedMethods": ["GET", "POST", "PUT", "DELETE"],
      "allowedHeaders": ["*"]
    },
    "rateLimiting": {
      "enabled": true,
      "defaultLimit": 1000,
      "windowSize": "1m"
    }
  }
}
```

### 7.2 Update Security Configuration

```http
PUT /actuator/tigateway/security
Content-Type: application/json
```

**Request Body:**
```json
{
  "rateLimiting": {
    "enabled": true,
    "defaultLimit": 2000,
    "windowSize": "1m"
  }
}
```

## 8. Error Handling

### 8.1 Error Response Format

```json
{
  "error": {
    "code": "INVALID_ROUTE_CONFIG",
    "message": "Route configuration is invalid",
    "details": {
      "field": "uri",
      "value": "invalid-uri",
      "reason": "URI format is not valid"
    },
    "timestamp": "2024-09-23T10:00:00Z",
    "requestId": "req-001"
  }
}
```

### 8.2 Common Error Codes

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
| 400 | `INVALID_REQUEST` | Invalid request format |
| 401 | `UNAUTHORIZED` | Authentication required |
| 403 | `FORBIDDEN` | Insufficient permissions |
| 404 | `ROUTE_NOT_FOUND` | Route not found |
| 409 | `ROUTE_EXISTS` | Route already exists |
| 422 | `VALIDATION_ERROR` | Validation failed |
| 500 | `INTERNAL_ERROR` | Internal server error |

## 9. Rate Limiting

### 9.1 Rate Limit Headers

```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1640995200
X-RateLimit-Window: 60
```

### 9.2 Rate Limit Exceeded Response

```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json

{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Rate limit exceeded",
    "details": {
      "limit": 1000,
      "window": "1m",
      "retryAfter": 60
    }
  }
}
```

## 10. Client Examples

### 10.1 JavaScript Client

```javascript
class TiGatewayManagementClient {
    constructor(baseUrl, options = {}) {
        this.baseUrl = baseUrl;
        this.options = {
            timeout: 30000,
            retries: 3,
            ...options
        };
        this.token = null;
    }
    
    async authenticate(username, password) {
        const response = await fetch(`${this.baseUrl}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });
        
        if (!response.ok) {
            throw new Error('Authentication failed');
        }
        
        const data = await response.json();
        this.token = data.token;
        return data;
    }
    
    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };
        
        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }
        
        const response = await fetch(url, {
            ...options,
            headers
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Request failed');
        }
        
        return response.json();
    }
    
    // Route management methods
    async getRoutes() {
        return this.request('/routes');
    }
    
    async getRoute(routeId) {
        return this.request(`/routes/${routeId}`);
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
    
    // Configuration management methods
    async getConfig() {
        return this.request('/config');
    }
    
    async updateConfig(config) {
        return this.request('/config', {
            method: 'PUT',
            body: JSON.stringify({ config })
        });
    }
    
    // Monitoring methods
    async getMetrics() {
        return this.request('/metrics');
    }
    
    async getRouteMetrics() {
        return this.request('/metrics/routes');
    }
    
    async getHealth() {
        return this.request('/health');
    }
}

// Usage example
const client = new TiGatewayManagementClient('http://localhost:8081/actuator/tigateway');

// Authenticate
await client.authenticate('admin', 'password');

// Get all routes
const routes = await client.getRoutes();
console.log('Routes:', routes);

// Create a new route
const newRoute = await client.createRoute({
    id: 'new-route',
    uri: 'http://new-service:8080',
    predicates: [
        {
            name: 'Path',
            args: {
                pattern: '/api/new/**'
            }
        }
    ],
    filters: [
        {
            name: 'StripPrefix',
            args: {
                parts: 2
            }
        }
    ]
});

console.log('Created route:', newRoute);
```

### 10.2 Java Client

```java
@Component
public class TiGatewayManagementClient {
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private String token;
    
    public TiGatewayManagementClient(@Value("${tigateway.management.url}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }
    
    public void authenticate(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
            baseUrl + "/auth/login",
            request,
            LoginResponse.class
        );
        
        if (response.getStatusCode().is2xxSuccessful()) {
            this.token = response.getBody().getToken();
        } else {
            throw new RuntimeException("Authentication failed");
        }
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        if (token != null) {
            headers.setBearerAuth(token);
        }
        
        return headers;
    }
    
    public RoutesResponse getRoutes() {
        HttpEntity<?> entity = new HttpEntity<>(createHeaders());
        
        ResponseEntity<RoutesResponse> response = restTemplate.exchange(
            baseUrl + "/routes",
            HttpMethod.GET,
            entity,
            RoutesResponse.class
        );
        
        return response.getBody();
    }
    
    public RouteResponse getRoute(String routeId) {
        HttpEntity<?> entity = new HttpEntity<>(createHeaders());
        
        ResponseEntity<RouteResponse> response = restTemplate.exchange(
            baseUrl + "/routes/" + routeId,
            HttpMethod.GET,
            entity,
            RouteResponse.class
        );
        
        return response.getBody();
    }
    
    public RouteResponse createRoute(CreateRouteRequest request) {
        HttpEntity<CreateRouteRequest> entity = new HttpEntity<>(request, createHeaders());
        
        ResponseEntity<RouteResponse> response = restTemplate.exchange(
            baseUrl + "/routes",
            HttpMethod.POST,
            entity,
            RouteResponse.class
        );
        
        return response.getBody();
    }
    
    public RouteResponse updateRoute(String routeId, UpdateRouteRequest request) {
        HttpEntity<UpdateRouteRequest> entity = new HttpEntity<>(request, createHeaders());
        
        ResponseEntity<RouteResponse> response = restTemplate.exchange(
            baseUrl + "/routes/" + routeId,
            HttpMethod.PUT,
            entity,
            RouteResponse.class
        );
        
        return response.getBody();
    }
    
    public void deleteRoute(String routeId) {
        HttpEntity<?> entity = new HttpEntity<>(createHeaders());
        
        restTemplate.exchange(
            baseUrl + "/routes/" + routeId,
            HttpMethod.DELETE,
            entity,
            Void.class
        );
    }
    
    public ConfigResponse getConfig() {
        HttpEntity<?> entity = new HttpEntity<>(createHeaders());
        
        ResponseEntity<ConfigResponse> response = restTemplate.exchange(
            baseUrl + "/config",
            HttpMethod.GET,
            entity,
            ConfigResponse.class
        );
        
        return response.getBody();
    }
    
    public void updateConfig(UpdateConfigRequest request) {
        HttpEntity<UpdateConfigRequest> entity = new HttpEntity<>(request, createHeaders());
        
        restTemplate.exchange(
            baseUrl + "/config",
            HttpMethod.PUT,
            entity,
            Void.class
        );
    }
    
    public MetricsResponse getMetrics() {
        HttpEntity<?> entity = new HttpEntity<>(createHeaders());
        
        ResponseEntity<MetricsResponse> response = restTemplate.exchange(
            baseUrl + "/metrics",
            HttpMethod.GET,
            entity,
            MetricsResponse.class
        );
        
        return response.getBody();
    }
    
    public HealthResponse getHealth() {
        HttpEntity<?> entity = new HttpEntity<>(createHeaders());
        
        ResponseEntity<HealthResponse> response = restTemplate.exchange(
            baseUrl + "/health",
            HttpMethod.GET,
            entity,
            HealthResponse.class
        );
        
        return response.getBody();
    }
}
```

## 11. Best Practices

### 11.1 Authentication and Security

```javascript
// Token refresh mechanism
class AuthenticatedClient extends TiGatewayManagementClient {
    constructor(baseUrl, options = {}) {
        super(baseUrl, options);
        this.refreshTimer = null;
        this.tokenExpiry = null;
    }
    
    async authenticate(username, password) {
        const response = await super.authenticate(username, password);
        this.tokenExpiry = new Date(response.expiresAt);
        this.scheduleTokenRefresh();
        return response;
    }
    
    scheduleTokenRefresh() {
        if (this.refreshTimer) {
            clearTimeout(this.refreshTimer);
        }
        
        const refreshTime = this.tokenExpiry.getTime() - Date.now() - 60000; // 1 minute before expiry
        
        this.refreshTimer = setTimeout(() => {
            this.refreshToken();
        }, refreshTime);
    }
    
    async refreshToken() {
        try {
            const response = await this.request('/auth/refresh', {
                method: 'POST'
            });
            
            this.token = response.token;
            this.tokenExpiry = new Date(response.expiresAt);
            this.scheduleTokenRefresh();
        } catch (error) {
            console.error('Token refresh failed:', error);
            // Handle token refresh failure
        }
    }
}
```

### 11.2 Error Handling and Retry

```javascript
// Retry mechanism with exponential backoff
class RetryableClient extends TiGatewayManagementClient {
    async request(endpoint, options = {}) {
        let lastError;
        
        for (let attempt = 0; attempt < this.options.retries; attempt++) {
            try {
                return await super.request(endpoint, options);
            } catch (error) {
                lastError = error;
                
                if (attempt < this.options.retries - 1) {
                    const delay = Math.pow(2, attempt) * 1000; // Exponential backoff
                    await new Promise(resolve => setTimeout(resolve, delay));
                }
            }
        }
        
        throw lastError;
    }
}
```

### 11.3 Caching and Performance

```javascript
// Response caching
class CachedClient extends TiGatewayManagementClient {
    constructor(baseUrl, options = {}) {
        super(baseUrl, options);
        this.cache = new Map();
        this.cacheTimeout = options.cacheTimeout || 300000; // 5 minutes
    }
    
    async request(endpoint, options = {}) {
        const cacheKey = `${endpoint}:${JSON.stringify(options)}`;
        const cached = this.cache.get(cacheKey);
        
        if (cached && Date.now() - cached.timestamp < this.cacheTimeout) {
            return cached.data;
        }
        
        const data = await super.request(endpoint, options);
        
        this.cache.set(cacheKey, {
            data,
            timestamp: Date.now()
        });
        
        return data;
    }
    
    clearCache() {
        this.cache.clear();
    }
}
```

---

**Related Documentation**:
- [REST API Documentation](./rest-api.md)
- [WebSocket API Documentation](./websocket-api.md)
- [CRD API Documentation](./crd-api.md)
- [Administration Guide](../administration/admin-interface.md)
