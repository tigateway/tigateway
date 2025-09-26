# Web UI Guide

This guide covers TiGateway's web-based user interface, including the dashboard, route management, service monitoring, and configuration management through the web UI.

## Overview

TiGateway provides a comprehensive web-based user interface:

- **Dashboard**: Real-time system overview and metrics
- **Route Management**: Visual route configuration and management
- **Service Monitoring**: Service health and performance monitoring
- **Configuration Management**: Gateway configuration through UI
- **User Management**: User authentication and authorization
- **Real-time Updates**: Live updates of system status

## Accessing the Web UI

### Default Access

The web UI is available at:
```
http://localhost:8080/ui
```

### Authentication

The web UI supports multiple authentication methods:

```yaml
ui:
  security:
    enabled: true
    authentication:
      type: jwt  # jwt, basic, oauth2
      jwt:
        secret: ${UI_JWT_SECRET}
        expiration: 3600
      basic:
        username: admin
        password: ${UI_PASSWORD}
```

### Login Process

1. Navigate to the web UI URL
2. Enter your credentials
3. Select your preferred language (if available)
4. Access the main dashboard

## Dashboard

### System Overview

The main dashboard displays:

- **System Status**: Overall gateway health
- **Request Metrics**: Total requests, success rate, error rate
- **Response Times**: Average, P95, P99 response times
- **Active Connections**: Current active connections
- **Service Status**: Status of all discovered services

### Real-time Metrics

- **Request Rate**: Requests per second
- **Error Rate**: Error percentage
- **Response Time**: Average response time
- **Throughput**: Data transfer rate
- **Memory Usage**: JVM memory usage
- **CPU Usage**: System CPU usage

### Charts and Graphs

- **Time Series Charts**: Performance trends over time
- **Pie Charts**: Request distribution by service
- **Bar Charts**: Top endpoints by request count
- **Heat Maps**: Error distribution by time and service

## Route Management

### Route Overview

The route management interface shows:

- **Route List**: All configured routes
- **Route Status**: Active, inactive, or error status
- **Route Metrics**: Request count, response time, error rate
- **Route Configuration**: Predicates, filters, and target services

### Creating Routes

1. Click "Add Route" button
2. Fill in route configuration:
   - **Route ID**: Unique identifier
   - **Target URI**: Backend service endpoint
   - **Predicates**: Route matching conditions
   - **Filters**: Request/response processing
   - **Order**: Route priority
3. Click "Save" to create the route

### Editing Routes

1. Select a route from the list
2. Click "Edit" button
3. Modify route configuration
4. Click "Save" to apply changes

### Route Testing

1. Select a route
2. Click "Test" button
3. Enter test parameters:
   - **Method**: HTTP method
   - **Path**: Request path
   - **Headers**: Request headers
   - **Body**: Request body (if applicable)
4. Click "Send" to test the route

### Route Monitoring

- **Request Count**: Total requests processed
- **Success Rate**: Percentage of successful requests
- **Error Rate**: Percentage of failed requests
- **Response Time**: Average response time
- **Last Activity**: Last request timestamp

## Service Discovery

### Service Overview

The service discovery interface displays:

- **Service List**: All discovered services
- **Service Instances**: Individual service instances
- **Health Status**: Service health information
- **Load Balancing**: Load balancing configuration

### Service Details

For each service, you can view:

- **Service Name**: Service identifier
- **Instances**: List of service instances
- **Health Status**: UP, DOWN, or UNKNOWN
- **Metadata**: Service metadata and tags
- **Load Balancer**: Load balancing strategy

### Service Management

- **Add Instance**: Manually add service instances
- **Remove Instance**: Remove unhealthy instances
- **Health Check**: Configure health check settings
- **Load Balancing**: Configure load balancing strategy

## Configuration Management

### Gateway Configuration

The configuration interface allows you to:

- **View Configuration**: Current gateway configuration
- **Edit Configuration**: Modify gateway settings
- **Validate Configuration**: Check configuration validity
- **Apply Changes**: Apply configuration changes
- **Rollback Changes**: Revert to previous configuration

### Configuration Sections

- **Global Settings**: Gateway-wide configuration
- **Route Configuration**: Individual route settings
- **Filter Configuration**: Custom filter settings
- **Security Configuration**: Authentication and authorization
- **Monitoring Configuration**: Metrics and logging settings

### Configuration Validation

- **Syntax Validation**: YAML/JSON syntax checking
- **Semantic Validation**: Configuration logic validation
- **Dependency Checking**: Service and filter dependencies
- **Impact Analysis**: Configuration change impact

## User Management

### User Overview

The user management interface shows:

- **User List**: All registered users
- **User Roles**: User roles and permissions
- **User Status**: Active, inactive, or locked
- **Last Login**: Last login timestamp

### User Operations

- **Add User**: Create new users
- **Edit User**: Modify user information
- **Delete User**: Remove users
- **Reset Password**: Reset user passwords
- **Manage Roles**: Assign user roles

### Role Management

- **Role List**: Available roles
- **Permissions**: Role permissions
- **Role Assignment**: Assign roles to users
- **Permission Management**: Manage role permissions

## Monitoring and Alerts

### Real-time Monitoring

- **System Metrics**: CPU, memory, disk usage
- **Application Metrics**: Request rate, response time, error rate
- **Service Metrics**: Service health and performance
- **Custom Metrics**: User-defined metrics

### Alert Configuration

- **Alert Rules**: Define alert conditions
- **Alert Channels**: Email, SMS, webhook notifications
- **Alert Severity**: Critical, warning, info levels
- **Alert History**: Past alerts and notifications

### Dashboard Customization

- **Widgets**: Add, remove, and configure widgets
- **Layout**: Customize dashboard layout
- **Refresh Rate**: Set data refresh intervals
- **Time Range**: Select time range for data display

## API Integration

### REST API Access

The web UI provides access to:

- **Route API**: Route management endpoints
- **Service API**: Service discovery endpoints
- **Configuration API**: Configuration management endpoints
- **Metrics API**: Metrics and monitoring endpoints

### API Documentation

- **Interactive Documentation**: Swagger UI integration
- **API Examples**: Sample requests and responses
- **Authentication**: API authentication methods
- **Rate Limiting**: API rate limiting information

## Best Practices

### UI Usage

1. **Regular Monitoring**: Check the dashboard regularly
2. **Route Testing**: Test routes before deploying
3. **Configuration Validation**: Validate changes before applying
4. **User Management**: Regularly review user access
5. **Alert Configuration**: Set up appropriate alerts

### Performance

1. **Refresh Rate**: Use appropriate refresh rates
2. **Data Filtering**: Filter data to improve performance
3. **Browser Compatibility**: Use supported browsers
4. **Network Optimization**: Optimize network usage
5. **Caching**: Use browser caching effectively

### Security

1. **Authentication**: Use strong authentication
2. **Authorization**: Implement proper authorization
3. **HTTPS**: Use HTTPS in production
4. **Session Management**: Manage user sessions properly
5. **Audit Logging**: Log user actions

## Troubleshooting

### Common Issues

#### UI Not Loading

```bash
# Check UI service status
curl http://localhost:8080/ui/health

# Check authentication configuration
curl http://localhost:8080/actuator/configprops | grep -i ui

# Check browser console for errors
# Open browser developer tools
```

#### Authentication Issues

```bash
# Test authentication
curl -H "Authorization: Bearer <token>" http://localhost:8080/ui/api/routes

# Check JWT configuration
curl http://localhost:8080/actuator/configprops | grep -i jwt

# Check user management
curl http://localhost:8080/ui/api/users
```

#### Performance Issues

```bash
# Check UI performance
curl http://localhost:8080/actuator/metrics/http.server.requests

# Check memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Check response times
curl http://localhost:8080/actuator/metrics/http.server.requests.duration
```

### Debug Commands

```bash
# Check UI configuration
curl http://localhost:8080/actuator/configprops | grep -i ui

# Test UI endpoints
curl http://localhost:8080/ui/api/routes

# Check UI logs
tail -f logs/tigateway.log | grep -i ui

# Check browser network tab
# Open browser developer tools -> Network tab
```

## Next Steps

After setting up the web UI:

1. **[Admin Interface](./administration/admin-interface.md)** - Advanced admin features
2. **[Monitoring Setup](../monitoring-and-metrics.md)** - Comprehensive monitoring
3. **[Troubleshooting Guide](../troubleshooting.md)** - Common UI issues
4. **[Security Best Practices](../security-best-practices.md)** - Secure UI practices

---

**Ready to set up advanced admin features?** Check out our [Admin Interface](./administration/admin-interface.md) guide for comprehensive administrative capabilities.
