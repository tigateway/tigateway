# TiGateway MCP Server

TiGateway MCP Server provides Model Context Protocol (MCP) support for TiGateway API Gateway, allowing AI assistants and other MCP clients to interact with TiGateway through a standardized protocol.

## Features

- **Route Management**: List, create, update, delete, and test routes
- **Service Management**: List services and check health status
- **Metrics Collection**: Retrieve various metrics and statistics
- **Configuration Access**: Get current TiGateway configuration
- **Log Access**: Retrieve and filter logs
- **Resource Management**: Access TiGateway resources through MCP protocol

## Available Tools

### Route Management
- `tigateway_list_routes` - List all routes
- `tigateway_create_route` - Create a new route
- `tigateway_update_route` - Update an existing route
- `tigateway_delete_route` - Delete a route
- `tigateway_test_route` - Test a route

### Service Management
- `tigateway_list_services` - List all services
- `tigateway_service_health` - Check service health

### Monitoring
- `tigateway_get_metrics` - Get metrics and statistics
- `tigateway_get_config` - Get configuration
- `tigateway_get_logs` - Get logs

## Available Resources

- `tigateway://routes` - All routes configuration
- `tigateway://services` - All services information
- `tigateway://config` - Current configuration
- `tigateway://metrics` - Current metrics

## Quick Start

### 1. Build the Application

```bash
mvn clean package
```

### 2. Run the Server

```bash
java -jar target/ti-gateway-mcp-1.0.0.jar
```

The server will start on port 8082 by default.

### 3. Test the Server

```bash
# Health check
curl http://localhost:8082/mcp/health

# Server info
curl http://localhost:8082/mcp/info

# List tools
curl -X POST http://localhost:8082/mcp/request \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/list"
  }'
```

## Configuration

### Application Properties

```yaml
tigateway:
  mcp:
    enabled: true
    port: 8082
    context-path: /mcp
    authentication-enabled: false
    kubernetes:
      namespace: default
      api-server-url: https://kubernetes.default.svc
    metrics:
      enabled: true
      collection-interval: 30
```

### Environment Variables

- `KUBERNETES_SERVICE_HOST` - Kubernetes API server host
- `TIGATEWAY_MCP_PORT` - MCP server port
- `TIGATEWAY_MCP_AUTH_TOKEN` - Authentication token (if enabled)

## MCP Client Integration

### Example MCP Request

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/call",
  "params": {
    "name": "tigateway_list_routes",
    "arguments": {
      "namespace": "default",
      "filter": "user"
    }
  }
}
```

### Example MCP Response

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "success": true,
    "data": [
      {
        "name": "user-service-route",
        "namespace": "default",
        "path": "/api/users/**",
        "service": "user-service",
        "port": 8080,
        "status": "active"
      }
    ],
    "count": 1,
    "namespace": "default"
  }
}
```

## Development

### Running Tests

```bash
mvn test
```

### Building Docker Image

```bash
docker build -t tigateway-mcp:1.0.0 .
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tigateway-mcp
spec:
  replicas: 1
  selector:
    matchLabels:
      app: tigateway-mcp
  template:
    metadata:
      labels:
        app: tigateway-mcp
    spec:
      containers:
      - name: tigateway-mcp
        image: tigateway-mcp:1.0.0
        ports:
        - containerPort: 8082
        env:
        - name: KUBERNETES_SERVICE_HOST
          value: "https://kubernetes.default.svc"
```

## API Reference

### Endpoints

- `POST /mcp/request` - Handle MCP requests
- `GET /mcp/health` - Health check
- `GET /mcp/info` - Server information

### MCP Methods

- `initialize` - Initialize MCP connection
- `tools/list` - List available tools
- `tools/call` - Call a tool
- `resources/list` - List available resources
- `resources/read` - Read a resource

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.
