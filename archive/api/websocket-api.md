# TiGateway WebSocket API 文档

## 概述

TiGateway 提供了完整的 WebSocket API 支持，包括实时路由管理、配置更新通知、性能监控数据推送等功能。本文档详细说明了 WebSocket API 的使用方法、消息格式和最佳实践。

## 1. WebSocket 连接

### 1.1 连接端点

```javascript
// WebSocket 连接 URL
const wsUrl = 'ws://localhost:8080/ws/tigateway';

// 带认证的连接
const wsUrlWithAuth = 'ws://localhost:8080/ws/tigateway?token=your-jwt-token';

// 建立连接
const websocket = new WebSocket(wsUrl);
```

### 1.2 连接参数

| 参数 | 类型 | 必需 | 描述 |
|------|------|------|------|
| token | string | 否 | JWT 认证令牌 |
| clientId | string | 否 | 客户端标识符 |
| subscriptions | string[] | 否 | 订阅的主题列表 |

### 1.3 连接示例

```javascript
// 基本连接
const websocket = new WebSocket('ws://localhost:8080/ws/tigateway');

// 带参数连接
const params = new URLSearchParams({
    token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
    clientId: 'admin-client-001',
    subscriptions: JSON.stringify(['routes', 'metrics', 'logs'])
});

const websocket = new WebSocket(`ws://localhost:8080/ws/tigateway?${params}`);
```

## 2. 消息格式

### 2.1 消息结构

```json
{
    "type": "message_type",
    "id": "unique_message_id",
    "timestamp": "2024-09-23T10:00:00Z",
    "data": {
        // 消息数据
    },
    "metadata": {
        "source": "tigateway",
        "version": "1.0.0"
    }
}
```

### 2.2 消息类型

| 类型 | 方向 | 描述 |
|------|------|------|
| `ping` | 双向 | 心跳检测 |
| `pong` | 双向 | 心跳响应 |
| `subscribe` | 客户端→服务端 | 订阅主题 |
| `unsubscribe` | 客户端→服务端 | 取消订阅 |
| `route_update` | 服务端→客户端 | 路由更新通知 |
| `config_update` | 服务端→客户端 | 配置更新通知 |
| `metrics` | 服务端→客户端 | 性能指标数据 |
| `log` | 服务端→客户端 | 日志消息 |
| `error` | 双向 | 错误消息 |

## 3. 主题订阅

### 3.1 可用主题

| 主题 | 描述 | 消息类型 |
|------|------|----------|
| `routes` | 路由管理 | `route_update` |
| `config` | 配置管理 | `config_update` |
| `metrics` | 性能指标 | `metrics` |
| `logs` | 日志消息 | `log` |
| `health` | 健康状态 | `health_update` |
| `events` | 系统事件 | `system_event` |

### 3.2 订阅消息

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

### 3.3 取消订阅

```json
{
    "type": "unsubscribe",
    "id": "unsub-001",
    "data": {
        "topics": ["logs"]
    }
}
```

## 4. 路由管理 API

### 4.1 路由更新通知

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

### 4.2 路由操作

```json
// 创建路由
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

// 更新路由
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

// 删除路由
{
    "type": "route_delete",
    "id": "delete-001",
    "data": {
        "routeId": "route-to-delete"
    }
}
```

## 5. 配置管理 API

### 5.1 配置更新通知

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

### 5.2 配置操作

```json
// 获取配置
{
    "type": "config_get",
    "id": "get-config-001",
    "data": {
        "configType": "route_config",
        "configId": "user-service-config"
    }
}

// 更新配置
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

## 6. 性能监控 API

### 6.1 指标数据推送

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

### 6.2 指标查询

```json
// 查询指标
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

## 7. 日志 API

### 7.1 日志消息推送

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

### 7.2 日志查询

```json
// 查询日志
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

## 8. 健康状态 API

### 8.1 健康状态推送

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

## 9. 错误处理

### 9.1 错误消息格式

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

### 9.2 常见错误代码

| 错误代码 | 描述 | 解决方案 |
|----------|------|----------|
| `INVALID_MESSAGE_FORMAT` | 消息格式无效 | 检查 JSON 格式 |
| `UNAUTHORIZED` | 未授权访问 | 提供有效的认证令牌 |
| `INVALID_SUBSCRIPTION` | 无效的订阅主题 | 检查主题名称 |
| `ROUTE_NOT_FOUND` | 路由不存在 | 检查路由 ID |
| `INVALID_ROUTE_CONFIG` | 路由配置无效 | 检查路由配置参数 |
| `CONFIG_VALIDATION_ERROR` | 配置验证失败 | 检查配置参数 |

## 10. 客户端实现示例

### 10.1 JavaScript 客户端

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
                    // 重新订阅之前的主题
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

// 使用示例
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

// 连接并订阅
client.connect().then(() => {
    client.subscribe(['routes', 'metrics']);
});
```

### 10.2 Java 客户端

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
                    
                    // 重新订阅之前的主题
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

// 使用示例
@Configuration
public class WebSocketClientConfig {
    
    @Bean
    public TiGatewayWebSocketClient webSocketClient() {
        TiGatewayWebSocketClient client = new TiGatewayWebSocketClient();
        
        // 设置消息处理器
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

## 11. 最佳实践

### 11.1 连接管理

```javascript
// 连接重试机制
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
            // 收到心跳响应
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

### 11.2 消息处理

```javascript
// 消息队列处理
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
            // 处理消息
            await this.processMessage(message);
        } catch (error) {
            console.error('Failed to process message:', error);
            // 可以选择重新入队或丢弃消息
        }
    }
}
```

### 11.3 错误处理

```javascript
// 错误处理策略
class ErrorHandler {
    static handleError(error, message) {
        switch (error.code) {
            case 'UNAUTHORIZED':
                // 重新认证
                this.reauthenticate();
                break;
            case 'INVALID_MESSAGE_FORMAT':
                // 记录错误，不重试
                console.error('Invalid message format:', message);
                break;
            case 'ROUTE_NOT_FOUND':
                // 记录错误，可能需要刷新路由列表
                console.warn('Route not found:', error.details);
                break;
            default:
                // 通用错误处理
                console.error('Unknown error:', error);
        }
    }
    
    static reauthenticate() {
        // 实现重新认证逻辑
        const newToken = this.getNewToken();
        this.updateConnection(newToken);
    }
}
```

## 12. 安全考虑

### 12.1 认证和授权

```javascript
// JWT 令牌管理
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

### 12.2 消息加密

```javascript
// 消息加密（可选）
class MessageEncryption {
    static encrypt(message, key) {
        // 实现消息加密
        return CryptoJS.AES.encrypt(JSON.stringify(message), key).toString();
    }
    
    static decrypt(encryptedMessage, key) {
        // 实现消息解密
        const bytes = CryptoJS.AES.decrypt(encryptedMessage, key);
        return JSON.parse(bytes.toString(CryptoJS.enc.Utf8));
    }
}
```

---

**相关文档**:
- [REST API 文档](./rest-api.md)
- [管理 API 文档](./management-api.md)
- [CRD API 文档](./crd-api.md)
- [快速开始指南](../examples/quick-start.md)
