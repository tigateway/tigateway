# Admin Interface

This guide covers TiGateway's administrative interface, including the web UI, REST API, and command-line tools for managing routes, monitoring performance, and configuring the gateway.

## Overview

TiGateway provides comprehensive administrative capabilities:

- **Web UI**: User-friendly web interface for gateway management
- **REST API**: Programmatic access to gateway configuration
- **Command Line Tools**: CLI for automation and scripting
- **Real-time Monitoring**: Live metrics and performance data
- **Configuration Management**: Dynamic configuration updates
- **User Management**: Role-based access control for administrators

## Web UI

### Accessing the Admin Interface

The admin interface is available at:
```
http://localhost:8080/admin
```

### Authentication

The admin interface supports multiple authentication methods:

```yaml
admin:
  security:
    enabled: true
    authentication:
      type: jwt  # jwt, basic, oauth2
      jwt:
        secret: ${ADMIN_JWT_SECRET}
        expiration: 3600
      basic:
        username: admin
        password: ${ADMIN_PASSWORD}
      oauth2:
        client-id: ${OAUTH2_CLIENT_ID}
        client-secret: ${OAUTH2_CLIENT_SECRET}
        authorization-url: ${OAUTH2_AUTHORIZATION_URL}
        token-url: ${OAUTH2_TOKEN_URL}
```

### Dashboard

The main dashboard provides:

- **System Overview**: Gateway status, request metrics, error rates
- **Route Management**: View, create, edit, and delete routes
- **Service Discovery**: Monitor discovered services and their health
- **Performance Metrics**: Real-time performance data and charts
- **Logs**: System logs and audit trails
- **Configuration**: Gateway configuration management

### Route Management

#### Viewing Routes

The route management interface displays:
- Route ID and configuration
- Predicates and filters
- Target service information
- Route status and metrics
- Last modified timestamp

#### Creating Routes

```yaml
# Route creation form
route:
  id: new-route
  uri: lb://user-service
  predicates:
    - Path=/api/users/**
  filters:
    - StripPrefix=2
    - AddRequestHeader=X-Service,user-service
  order: 0
```

#### Editing Routes

Routes can be edited in real-time:
- Modify predicates and filters
- Update target services
- Change route order
- Enable/disable routes

#### Deleting Routes

Routes can be deleted with confirmation:
- Soft delete (disable) for temporary removal
- Hard delete for permanent removal
- Bulk operations for multiple routes

### Service Discovery

#### Service Overview

The service discovery interface shows:
- Discovered services and instances
- Service health status
- Load balancing configuration
- Service metrics and performance

#### Service Management

- **Add Services**: Manually add service instances
- **Remove Services**: Remove unhealthy or deprecated services
- **Health Checks**: Configure and monitor health checks
- **Load Balancing**: Configure load balancing strategies

### Performance Monitoring

#### Real-time Metrics

- **Request Rate**: Requests per second
- **Response Time**: Average, P95, P99 response times
- **Error Rate**: Error percentage and types
- **Throughput**: Data transfer rates
- **Connection Count**: Active connections

#### Historical Data

- **Time Series Charts**: Performance trends over time
- **Alert History**: Past alerts and notifications
- **Capacity Planning**: Resource usage trends
- **Performance Analysis**: Detailed performance reports

### Configuration Management

#### Gateway Configuration

- **Global Settings**: Gateway-wide configuration
- **Route Configuration**: Individual route settings
- **Filter Configuration**: Custom filter settings
- **Security Configuration**: Authentication and authorization

#### Configuration Validation

- **Syntax Validation**: YAML/JSON syntax checking
- **Semantic Validation**: Configuration logic validation
- **Dependency Checking**: Service and filter dependencies
- **Impact Analysis**: Configuration change impact

## REST API

### Authentication

All admin API endpoints require authentication:

```bash
# JWT Authentication
curl -H "Authorization: Bearer <token>" http://localhost:8080/admin/api/routes

# Basic Authentication
curl -u admin:password http://localhost:8080/admin/api/routes
```

### Route Management API

#### List Routes

```bash
GET /admin/api/routes
```

**Response:**
```json
{
  "routes": [
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
      "status": "ACTIVE"
    }
  ]
}
```

#### Create Route

```bash
POST /admin/api/routes
Content-Type: application/json

{
  "id": "new-route",
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
  ],
  "order": 0
}
```

#### Update Route

```bash
PUT /admin/api/routes/{routeId}
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

```bash
DELETE /admin/api/routes/{routeId}
```

### Service Discovery API

#### List Services

```bash
GET /admin/api/services
```

**Response:**
```json
{
  "services": [
    {
      "name": "user-service",
      "instances": [
        {
          "id": "user-service-1",
          "host": "user-service-1",
          "port": 8080,
          "health": "UP",
          "metadata": {
            "version": "1.0.0",
            "zone": "us-east-1"
          }
        }
      ],
      "loadBalancer": {
        "strategy": "ROUND_ROBIN",
        "healthCheck": {
          "enabled": true,
          "path": "/actuator/health",
          "interval": "10s"
        }
      }
    }
  ]
}
```

#### Add Service Instance

```bash
POST /admin/api/services/{serviceName}/instances
Content-Type: application/json

{
  "id": "new-instance",
  "host": "new-instance",
  "port": 8080,
  "metadata": {
    "version": "1.0.0",
    "zone": "us-west-1"
  }
}
```

#### Remove Service Instance

```bash
DELETE /admin/api/services/{serviceName}/instances/{instanceId}
```

### Metrics API

#### System Metrics

```bash
GET /admin/api/metrics/system
```

**Response:**
```json
{
  "metrics": {
    "requests": {
      "total": 10000,
      "rate": 100.5,
      "errors": 50,
      "errorRate": 0.005
    },
    "responseTime": {
      "average": 150.5,
      "p95": 300.0,
      "p99": 500.0
    },
    "connections": {
      "active": 100,
      "total": 1000
    }
  }
}
```

#### Route Metrics

```bash
GET /admin/api/metrics/routes/{routeId}
```

**Response:**
```json
{
  "routeId": "user-service-route",
  "metrics": {
    "requests": {
      "total": 5000,
      "rate": 50.2,
      "errors": 25,
      "errorRate": 0.005
    },
    "responseTime": {
      "average": 120.0,
      "p95": 250.0,
      "p99": 400.0
    }
  }
}
```

### Configuration API

#### Get Configuration

```bash
GET /admin/api/configuration
```

**Response:**
```json
{
  "configuration": {
    "gateway": {
      "httpClient": {
        "connectTimeout": 1000,
        "responseTimeout": 5000
      },
      "loadBalancer": {
        "defaultStrategy": "ROUND_ROBIN"
      }
    },
    "security": {
      "authentication": {
        "enabled": true,
        "type": "jwt"
      }
    }
  }
}
```

#### Update Configuration

```bash
PUT /admin/api/configuration
Content-Type: application/json

{
  "gateway": {
    "httpClient": {
      "connectTimeout": 2000,
      "responseTimeout": 10000
    }
  }
}
```

## Command Line Tools

### Gateway CLI

The gateway CLI provides command-line access to admin functions:

```bash
# Install CLI
npm install -g @tigateway/cli

# Configure CLI
tigateway config set endpoint http://localhost:8080
tigateway config set token <jwt-token>
```

### Route Management

```bash
# List routes
tigateway routes list

# Create route
tigateway routes create --id new-route --uri lb://new-service --path "/api/new/**"

# Update route
tigateway routes update new-route --uri lb://updated-service

# Delete route
tigateway routes delete new-route

# Enable/disable route
tigateway routes enable new-route
tigateway routes disable new-route
```

### Service Management

```bash
# List services
tigateway services list

# Add service instance
tigateway services add user-service --host user-service-1 --port 8080

# Remove service instance
tigateway services remove user-service --instance user-service-1

# Check service health
tigateway services health user-service
```

### Configuration Management

```bash
# Get configuration
tigateway config get

# Update configuration
tigateway config set gateway.httpClient.connectTimeout 2000

# Validate configuration
tigateway config validate

# Backup configuration
tigateway config backup --file backup.yaml

# Restore configuration
tigateway config restore --file backup.yaml
```

### Monitoring

```bash
# Get system metrics
tigateway metrics system

# Get route metrics
tigateway metrics route user-service-route

# Get service metrics
tigateway metrics service user-service

# Watch metrics (real-time)
tigateway metrics watch --interval 5s
```

## User Management

### User Roles

TiGateway supports role-based access control:

- **Admin**: Full access to all admin functions
- **Operator**: Read-only access to monitoring and configuration
- **Developer**: Limited access to route management
- **Viewer**: Read-only access to monitoring data

### User Management API

#### List Users

```bash
GET /admin/api/users
```

**Response:**
```json
{
  "users": [
    {
      "id": "admin",
      "username": "admin",
      "email": "admin@example.com",
      "roles": ["ADMIN"],
      "status": "ACTIVE",
      "lastLogin": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### Create User

```bash
POST /admin/api/users
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "roles": ["OPERATOR"]
}
```

#### Update User

```bash
PUT /admin/api/users/{userId}
Content-Type: application/json

{
  "email": "updated@example.com",
  "roles": ["ADMIN", "OPERATOR"]
}
```

#### Delete User

```bash
DELETE /admin/api/users/{userId}
```

### Role Management

#### List Roles

```bash
GET /admin/api/roles
```

**Response:**
```json
{
  "roles": [
    {
      "id": "ADMIN",
      "name": "Administrator",
      "description": "Full access to all admin functions",
      "permissions": [
        "routes:read",
        "routes:write",
        "routes:delete",
        "services:read",
        "services:write",
        "services:delete",
        "config:read",
        "config:write",
        "users:read",
        "users:write",
        "users:delete"
      ]
    }
  ]
}
```

## Security

### Access Control

The admin interface implements comprehensive security:

- **Authentication**: JWT, Basic Auth, OAuth2 support
- **Authorization**: Role-based access control
- **Audit Logging**: All admin operations are logged
- **Rate Limiting**: API rate limiting to prevent abuse
- **CSRF Protection**: Cross-site request forgery protection

### Security Configuration

```yaml
admin:
  security:
    enabled: true
    authentication:
      type: jwt
      jwt:
        secret: ${ADMIN_JWT_SECRET}
        expiration: 3600
    authorization:
      enabled: true
      defaultRole: VIEWER
    audit:
      enabled: true
      logLevel: INFO
    rateLimit:
      enabled: true
      requests: 100
      window: 60s
    csrf:
      enabled: true
      tokenHeader: X-CSRF-Token
```

### Audit Logging

All admin operations are logged with:

- **User Information**: Who performed the action
- **Action Details**: What action was performed
- **Resource Information**: Which resource was affected
- **Timestamp**: When the action occurred
- **IP Address**: Source IP address
- **Result**: Success or failure status

## Best Practices

### Admin Interface Usage

1. **Use HTTPS**: Always use HTTPS in production
2. **Strong Authentication**: Use strong passwords and JWT secrets
3. **Role-based Access**: Assign appropriate roles to users
4. **Regular Backups**: Backup configuration regularly
5. **Monitor Access**: Monitor admin interface access

### API Usage

1. **Rate Limiting**: Respect API rate limits
2. **Error Handling**: Implement proper error handling
3. **Authentication**: Always authenticate API requests
4. **Validation**: Validate input data
5. **Documentation**: Keep API documentation updated

### Security

1. **Access Control**: Implement proper access control
2. **Audit Logging**: Enable audit logging
3. **Regular Updates**: Keep admin interface updated
4. **Security Monitoring**: Monitor for security issues
5. **Incident Response**: Have incident response procedures

## Troubleshooting

### Common Issues

#### Admin Interface Not Accessible

```bash
# Check admin interface status
curl http://localhost:8080/admin/health

# Check authentication configuration
curl http://localhost:8080/actuator/configprops | grep -i admin

# Check security configuration
curl http://localhost:8080/actuator/configprops | grep -i security
```

#### Authentication Issues

```bash
# Test authentication
curl -H "Authorization: Bearer <token>" http://localhost:8080/admin/api/routes

# Check JWT configuration
curl http://localhost:8080/actuator/configprops | grep -i jwt

# Check user management
curl http://localhost:8080/admin/api/users
```

#### API Issues

```bash
# Test API endpoints
curl http://localhost:8080/admin/api/routes

# Check API configuration
curl http://localhost:8080/actuator/configprops | grep -i api

# Check rate limiting
curl http://localhost:8080/actuator/metrics/rate.limiter.requests
```

### Debug Commands

```bash
# Check admin interface logs
tail -f logs/tigateway.log | grep -i admin

# Check API logs
tail -f logs/tigateway.log | grep -i api

# Check security logs
tail -f logs/tigateway.log | grep -i security

# Check user management logs
tail -f logs/tigateway.log | grep -i user
```

## Next Steps

After setting up the admin interface:

1. **[Monitoring Setup](../monitoring-and-metrics.md)** - Set up comprehensive monitoring
2. **[Security Best Practices](../security-best-practices.md)** - Implement security measures
3. **[Troubleshooting Guide](../troubleshooting.md)** - Common admin interface issues
4. **[Performance Tuning](../performance-tuning.md)** - Optimize admin interface performance

---

**Ready to set up monitoring?** Check out our [Monitoring Setup](../monitoring-and-metrics.md) guide for comprehensive monitoring solutions.
