# TiGateway WebSocket API Documentation

## Overview

TiGateway provides comprehensive WebSocket API support, including real-time route management, configuration update notifications, performance monitoring data streaming, and more. This document details the WebSocket API usage, message formats, and best practices.

## 1. WebSocket Connection

### 1.1 Connection Endpoint

```javascript
// WebSocket connection URL
const wsUrl = 'ws://localhost:8080/ws/tigateway';

// Connection with authentication
const wsUrlWithAuth = 'ws://localhost:8080/ws/tigateway?token=your-jwt-token';

// Establish connection
const websocket = new WebSocket(wsUrl);
```

### 1.2 Connection Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| token | string | No | JWT authentication token |
| clientId | string | No | Client identifier |
| subscriptions | string[] | No | List of subscribed topics |

### 1.3 Connection Example

```javascript
// Basic connection
const websocket = new WebSocket('ws://localhost:8080/ws/tigateway');

// Connection with parameters
const params = new URLSearchParams({
    token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
    clientId: 'admin-client-001',
    subscriptions: JSON.stringify(['routes', 'metrics', 'logs'])
});

const websocket = new WebSocket(`ws://localhost:8080/ws/tigateway?${params}`);
```

## 2. Message Format

### 2.1 Message Structure

```json
{
    "type": "message_type",
    "id": "unique_message_id",
    "timestamp": "2024-09-23T10:00:00Z",
    "data": {
        // Message data
    },
    "metadata": {
        "source": "tigateway",
        "version": "1.0.0"
    }
}
```

### 2.2 Message Types

| Type | Direction | Description |
|------|-----------|-------------|
| `ping` | Bidirectional | Heartbeat detection |
| `pong` | Bidirectional | Heartbeat response |
| `subscribe` | Client→Server | Subscribe to topic |
| `unsubscribe` | Client→Server | Unsubscribe from topic |
| `route_update` | Server→Client | Route update notification |
| `config_update` | Server→Client | Configuration update notification |
| `metrics` | Server→Client | Performance metrics data |
| `log` | Server→Client | Log message |
| `error` | Bidirectional | Error message |

## 3. Topic Subscription

### 3.1 Available Topics

| Topic | Description | Message Type |
|-------|-------------|--------------|
| `routes` | Route management | `route_update` |
| `config` | Configuration management | `config_update` |
| `metrics` | Performance metrics | `metrics` |
| `logs` | Log messages | `log` |
| `health` | Health status | `health_update` |
| `events` | System events | `system_event` |

### 3.2 Subscription Message

```json
{
    "type": "subscribe",
    "id": "sub-001",
    "data": {
        "topics": ["routes", "metrics"],
        "filters": {
            "routes": {
                "status": ["ACTIVE", "PENDING"]
            },
            "metrics": {
                "interval": 5000
            }
        }
    }
}
```

### 3.3 Unsubscribe

```json
{
    "type": "unsubscribe",
    "id": "unsub-001",
    "data": {
        "topics": ["logs"]
    }
}
```

## 4. Route Management API

### 4.1 Route Update Notification

```json
{
    "type": "route_update",
    "id": "route-update-001",
    "timestamp": "2024-09-23T10:00:00Z",
    "data": {
        "action": "created",
        "route": {
            "id": "user-service-route",
            "uri": "http://user-service:8080",
            "predicates": ["Path=/api/users/**"],
            "filters": ["StripPrefix=2"],
            "status": "ACTIVE",
            "order": 0,
            "createdAt": "2024-09-23T10:00:00Z",
            "updatedAt": "2024-09-23T10:00:00Z"
        }
    }
}
```

### 4.2 Route Operations

```json
// Create route
{
    "type": "route_create",
    "id": "create-001",
    "data": {
        "route": {
            "id": "new-route",
            "uri": "http://new-service:8080",
            "predicates": ["Path=/api/new/**"],
            "filters": ["StripPrefix=2"]
        }
    }
}

// Update route
{
    "type": "route_update",
    "id": "update-001",
    "data": {
        "routeId": "existing-route",
        "updates": {
            "uri": "http://updated-service:8080",
            "filters": ["StripPrefix=2", "AddRequestHeader=X-Service,new-service"]
        }
    }
}

// Delete route
{
    "type": "route_delete",
    "id": "delete-001",
    "data": {
        "routeId": "route-to-delete"
    }
}
```

## 5. Configuration Management API

### 5.1 Configuration Update Notification

```json
{
    "type": "config_update",
    "id": "config-update-001",
    "timestamp": "2024-09-23T10:00:00Z",
    "data": {
        "configType": "route_config",
        "action": "updated",
        "config": {
            "id": "user-service-config",
            "routes": [
                {
                    "id": "user-service-route",
                    "uri": "http://user-service:8080",
                    "predicates": ["Path=/api/users/**"]
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
}
```

### 5.2 Configuration Operations

```json
// Get configuration
{
    "type": "config_get",
    "id": "get-config-001",
    "data": {
        "configType": "route_config",
        "configId": "user-service-config"
    }
}

// Update configuration
{
    "type": "config_update",
    "id": "update-config-001",
    "data": {
        "configType": "route_config",
        "configId": "user-service-config",
        "config": {
            "routes": [
                {
                    "id": "user-service-route",
                    "uri": "http://user-service-v2:8080",
                    "predicates": ["Path=/api/users/**"]
                }
            ]
        }
    }
}
```

## 6. Performance Monitoring API

### 6.1 Metrics Data Streaming

```json
{
    "type": "metrics",
    "id": "metrics-001",
    "timestamp": "2024-09-23T10:00:00Z",
    "data": {
        "timestamp": "2024-09-23T10:00:00Z",
        "metrics": {
            "requests": {
                "total": 1000,
                "success": 950,
                "error": 50,
                "rate": 100.5
            },
            "response_time": {
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
            "system": {
                "cpu_usage": 45.2,
                "memory_usage": 68.5,
                "gc_count": 10,
                "gc_time": 150
            }
        }
    }
}
```

### 6.2 Metrics Query

```json
// Query metrics
{
    "type": "metrics_query",
    "id": "query-001",
    "data": {
        "metric": "response_time",
        "timeRange": {
            "start": "2024-09-23T09:00:00Z",
            "end": "2024-09-23T10:00:00Z"
        },
        "aggregation": "avg",
        "groupBy": ["route_id"]
    }
}
```

## 7. Log API

### 7.1 Log Message Streaming

```json
{
    "type": "log",
    "id": "log-001",
    "timestamp": "2024-09-23T10:00:00Z",
    "data": {
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
}
```

### 7.2 Log Query

```json
// Query logs
{
    "type": "log_query",
    "id": "log-query-001",
    "data": {
        "level": ["ERROR", "WARN"],
        "logger": "ti.gateway.*",
        "timeRange": {
            "start": "2024-09-23T09:00:00Z",
            "end": "2024-09-23T10:00:00Z"
        },
        "limit": 100,
        "filters": {
            "message": "route.*error"
        }
    }
}
```

## 8. Health Status API

### 8.1 Health Status Streaming

```json
{
    "type": "health_update",
    "id": "health-001",
    "timestamp": "2024-09-23T10:00:00Z",
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

## 9. Error Handling

### 9.1 Error Message Format

```json
{
    "type": "error",
    "id": "error-001",
    "timestamp": "2024-09-23T10:00:00Z",
    "data": {
        "code": "INVALID_ROUTE_CONFIG",
        "message": "Route configuration is invalid",
        "details": {
            "field": "uri",
            "value": "invalid-uri",
            "reason": "URI format is not valid"
        },
        "requestId": "req-001"
    }
}
```

### 9.2 Common Error Codes

| Error Code | Description | Solution |
|------------|-------------|----------|
| `INVALID_MESSAGE_FORMAT` | Invalid message format | Check JSON format |
| `UNAUTHORIZED` | Unauthorized access | Provide valid authentication token |
| `INVALID_SUBSCRIPTION` | Invalid subscription topic | Check topic name |
| `ROUTE_NOT_FOUND` | Route not found | Check route ID |
| `INVALID_ROUTE_CONFIG` | Invalid route configuration | Check route configuration parameters |
| `CONFIG_VALIDATION_ERROR` | Configuration validation failed | Check configuration parameters |

## 10. Client Implementation Examples

### 10.1 JavaScript Client

```javascript
class TiGatewayWebSocketClient {
    constructor(url, options = {}) {
        this.url = url;
        this.options = options;
        this.websocket = null;
        this.subscriptions = new Set();
        this.messageHandlers = new Map();
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 1000;
    }
    
    connect() {
        return new Promise((resolve, reject) => {
            try {
                this.websocket = new WebSocket(this.url);
                
                this.websocket.onopen = (event) => {
                    console.log('WebSocket connected');
                    this.reconnectAttempts = 0;
                    resolve(event);
                };
                
                this.websocket.onmessage = (event) => {
                    this.handleMessage(event.data);
                };
                
                this.websocket.onclose = (event) => {
                    console.log('WebSocket disconnected');
                    this.handleReconnect();
                };
                
                this.websocket.onerror = (error) => {
                    console.error('WebSocket error:', error);
                    reject(error);
                };
                
            } catch (error) {
                reject(error);
            }
        });
    }
    
    handleMessage(data) {
        try {
            const message = JSON.parse(data);
            const handler = this.messageHandlers.get(message.type);
            
            if (handler) {
                handler(message);
            } else {
                console.log('Unhandled message type:', message.type);
            }
        } catch (error) {
            console.error('Failed to parse message:', error);
        }
    }
    
    send(message) {
        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
            this.websocket.send(JSON.stringify(message));
        } else {
            console.error('WebSocket is not connected');
        }
    }
    
    subscribe(topics, filters = {}) {
        const message = {
            type: 'subscribe',
            id: `sub-${Date.now()}`,
            data: {
                topics: Array.isArray(topics) ? topics : [topics],
                filters: filters
            }
        };
        
        this.send(message);
        topics.forEach(topic => this.subscriptions.add(topic));
    }
    
    unsubscribe(topics) {
        const message = {
            type: 'unsubscribe',
            id: `unsub-${Date.now()}`,
            data: {
                topics: Array.isArray(topics) ? topics : [topics]
            }
        };
        
        this.send(message);
        topics.forEach(topic => this.subscriptions.delete(topic));
    }
    
    onMessageType(type, handler) {
        this.messageHandlers.set(type, handler);
    }
    
    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
            
            setTimeout(() => {
                this.connect().then(() => {
                    // Resubscribe to previous topics
                    if (this.subscriptions.size > 0) {
                        this.subscribe(Array.from(this.subscriptions));
                    }
                });
            }, this.reconnectInterval * this.reconnectAttempts);
        }
    }
    
    disconnect() {
        if (this.websocket) {
            this.websocket.close();
        }
    }
}

// Usage example
const client = new TiGatewayWebSocketClient('ws://localhost:8080/ws/tigateway');

client.onMessageType('route_update', (message) => {
    console.log('Route updated:', message.data);
});

client.onMessageType('metrics', (message) => {
    console.log('Metrics:', message.data.metrics);
});

client.onMessageType('error', (message) => {
    console.error('Error:', message.data);
});

// Connect and subscribe
client.connect().then(() => {
    client.subscribe(['routes', 'metrics']);
});
```

### 10.2 Java Client

```java
@Component
public class TiGatewayWebSocketClient {
    
    private final WebSocketClient webSocketClient;
    private final ObjectMapper objectMapper;
    private WebSocketSession session;
    private final Set<String> subscriptions = new HashSet<>();
    private final Map<String, Consumer<WebSocketMessage>> messageHandlers = new HashMap<>();
    
    public TiGatewayWebSocketClient() {
        this.webSocketClient = new StandardWebSocketClient();
        this.objectMapper = new ObjectMapper();
    }
    
    public void connect(String url) {
        try {
            WebSocketHandler handler = new WebSocketHandler() {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) {
                    TiGatewayWebSocketClient.this.session = session;
                    log.info("WebSocket connected");
                    
                    // Resubscribe to previous topics
                    if (!subscriptions.isEmpty()) {
                        subscribe(new ArrayList<>(subscriptions));
                    }
                }
                
                @Override
                public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
                    handleMessage((String) message.getPayload());
                }
                
                @Override
                public void handleTransportError(WebSocketSession session, Throwable exception) {
                    log.error("WebSocket transport error", exception);
                }
                
                @Override
                public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
                    log.info("WebSocket disconnected: {}", closeStatus);
                    TiGatewayWebSocketClient.this.session = null;
                }
                
                @Override
                public boolean supportsPartialMessages() {
                    return false;
                }
            };
            
            webSocketClient.execute(handler, url);
            
        } catch (Exception e) {
            log.error("Failed to connect to WebSocket", e);
        }
    }
    
    private void handleMessage(String payload) {
        try {
            JsonNode message = objectMapper.readTree(payload);
            String type = message.get("type").asText();
            
            Consumer<WebSocketMessage> handler = messageHandlers.get(type);
            if (handler != null) {
                handler.accept(new WebSocketMessage(message));
            } else {
                log.debug("Unhandled message type: {}", type);
            }
            
        } catch (Exception e) {
            log.error("Failed to handle message", e);
        }
    }
    
    public void send(WebSocketMessage message) {
        if (session != null && session.isOpen()) {
            try {
                String payload = objectMapper.writeValueAsString(message.toJsonNode());
                session.sendMessage(new TextMessage(payload));
            } catch (Exception e) {
                log.error("Failed to send message", e);
            }
        } else {
            log.error("WebSocket is not connected");
        }
    }
    
    public void subscribe(List<String> topics) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type("subscribe")
                .id("sub-" + System.currentTimeMillis())
                .data(Map.of("topics", topics))
                .build();
        
        send(message);
        subscriptions.addAll(topics);
    }
    
    public void unsubscribe(List<String> topics) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type("unsubscribe")
                .id("unsub-" + System.currentTimeMillis())
                .data(Map.of("topics", topics))
                .build();
        
        send(message);
        subscriptions.removeAll(topics);
    }
    
    public void onMessageType(String type, Consumer<WebSocketMessage> handler) {
        messageHandlers.put(type, handler);
    }
    
    public void disconnect() {
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                log.error("Failed to close WebSocket session", e);
            }
        }
    }
}

// Usage example
@Configuration
public class WebSocketClientConfig {
    
    @Bean
    public TiGatewayWebSocketClient webSocketClient() {
        TiGatewayWebSocketClient client = new TiGatewayWebSocketClient();
        
        // Set message handlers
        client.onMessageType("route_update", message -> {
            log.info("Route updated: {}", message.getData());
        });
        
        client.onMessageType("metrics", message -> {
            log.info("Metrics: {}", message.getData());
        });
        
        client.onMessageType("error", message -> {
            log.error("Error: {}", message.getData());
        });
        
        return client;
    }
}
```

## 11. Best Practices

### 11.1 Connection Management

```javascript
// Connection retry mechanism
class WebSocketManager {
    constructor(url, options = {}) {
        this.url = url;
        this.options = {
            maxReconnectAttempts: 5,
            reconnectInterval: 1000,
            heartbeatInterval: 30000,
            ...options
        };
        this.client = null;
        this.reconnectTimer = null;
        this.heartbeatTimer = null;
    }
    
    connect() {
        this.client = new TiGatewayWebSocketClient(this.url);
        
        this.client.onMessageType('pong', () => {
            // Received heartbeat response
        });
        
        this.client.connect().then(() => {
            this.startHeartbeat();
            this.clearReconnectTimer();
        }).catch(() => {
            this.scheduleReconnect();
        });
    }
    
    startHeartbeat() {
        this.heartbeatTimer = setInterval(() => {
            this.client.send({
                type: 'ping',
                id: `ping-${Date.now()}`
            });
        }, this.options.heartbeatInterval);
    }
    
    scheduleReconnect() {
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
        }
        
        this.reconnectTimer = setTimeout(() => {
            this.connect();
        }, this.options.reconnectInterval);
    }
    
    clearReconnectTimer() {
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }
    }
}
```

### 11.2 Message Processing

```javascript
// Message queue processing
class MessageQueue {
    constructor() {
        this.queue = [];
        this.processing = false;
    }
    
    add(message) {
        this.queue.push(message);
        this.process();
    }
    
    async process() {
        if (this.processing || this.queue.length === 0) {
            return;
        }
        
        this.processing = true;
        
        while (this.queue.length > 0) {
            const message = this.queue.shift();
            await this.handleMessage(message);
        }
        
        this.processing = false;
    }
    
    async handleMessage(message) {
        try {
            // Process message
            await this.processMessage(message);
        } catch (error) {
            console.error('Failed to process message:', error);
            // Optionally re-queue or discard message
        }
    }
}
```

### 11.3 Error Handling

```javascript
// Error handling strategy
class ErrorHandler {
    static handleError(error, message) {
        switch (error.code) {
            case 'UNAUTHORIZED':
                // Re-authenticate
                this.reauthenticate();
                break;
            case 'INVALID_MESSAGE_FORMAT':
                // Log error, don't retry
                console.error('Invalid message format:', message);
                break;
            case 'ROUTE_NOT_FOUND':
                // Log error, may need to refresh route list
                console.warn('Route not found:', error.details);
                break;
            default:
                // Generic error handling
                console.error('Unknown error:', error);
        }
    }
    
    static reauthenticate() {
        // Implement re-authentication logic
        const newToken = this.getNewToken();
        this.updateConnection(newToken);
    }
}
```

## 12. Security Considerations

### 12.1 Authentication and Authorization

```javascript
// JWT token management
class TokenManager {
    constructor() {
        this.token = null;
        this.refreshToken = null;
        this.tokenExpiry = null;
    }
    
    setToken(token) {
        this.token = token;
        this.tokenExpiry = this.getTokenExpiry(token);
    }
    
    getTokenExpiry(token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return new Date(payload.exp * 1000);
        } catch (error) {
            return null;
        }
    }
    
    isTokenValid() {
        if (!this.token || !this.tokenExpiry) {
            return false;
        }
        
        return new Date() < this.tokenExpiry;
    }
    
    async refreshTokenIfNeeded() {
        if (!this.isTokenValid()) {
            await this.refreshToken();
        }
    }
}
```

### 12.2 Message Encryption

```javascript
// Message encryption (optional)
class MessageEncryption {
    static encrypt(message, key) {
        // Implement message encryption
        return CryptoJS.AES.encrypt(JSON.stringify(message), key).toString();
    }
    
    static decrypt(encryptedMessage, key) {
        // Implement message decryption
        const bytes = CryptoJS.AES.decrypt(encryptedMessage, key);
        return JSON.parse(bytes.toString(CryptoJS.enc.Utf8));
    }
}
```

---

**Related Documentation**:
- [REST API Documentation](./rest-api.md)
- [Management API Documentation](./management-api.md)
- [CRD API Documentation](./crd-api.md)
- [Quick Start Guide](../examples/quick-start.md)
