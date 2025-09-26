# TiGateway 过滤器工厂开发指南

## 概述

过滤器工厂是 TiGateway 的核心扩展点之一，允许开发者创建自定义的请求和响应处理逻辑。本文档详细说明了如何开发各种类型的过滤器工厂。

## 过滤器工厂基础

### 1. 基本结构

所有过滤器工厂都需要实现 `GatewayFilterFactory` 接口或继承 `AbstractGatewayFilterFactory` 类：

```java
@Component
public class CustomFilterFactory extends AbstractGatewayFilterFactory<CustomFilterFactory.Config> {

    public CustomFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 过滤器逻辑实现
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // 配置属性
    }
}
```

### 2. 过滤器类型

根据执行时机，过滤器可以分为：

- **Pre 过滤器**: 在请求发送到下游服务之前执行
- **Post 过滤器**: 在响应返回给客户端之前执行
- **混合过滤器**: 同时处理请求和响应

## 请求处理过滤器

### 1. 请求头处理

```java
@Component
public class AddRequestHeaderFilterFactory extends AbstractGatewayFilterFactory<AddRequestHeaderFilterFactory.Config> {

    public AddRequestHeaderFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpRequest.Builder builder = request.mutate();
            
            // 添加请求头
            config.getHeaders().forEach((name, value) -> {
                builder.header(name, value);
            });
            
            ServerHttpRequest modifiedRequest = builder.build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    public static class Config {
        private Map<String, String> headers = new HashMap<>();

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
    }
}
```

### 2. 请求参数处理

```java
@Component
public class AddRequestParameterFilterFactory extends AbstractGatewayFilterFactory<AddRequestParameterFilterFactory.Config> {

    public AddRequestParameterFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            URI originalUri = request.getURI();
            
            // 构建新的查询参数
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(originalUri);
            config.getParameters().forEach((name, value) -> {
                uriBuilder.queryParam(name, value);
            });
            
            URI newUri = uriBuilder.build().toUri();
            ServerHttpRequest modifiedRequest = request.mutate().uri(newUri).build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    public static class Config {
        private Map<String, String> parameters = new HashMap<>();

        public Map<String, String> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }
    }
}
```

### 3. 请求体处理

```java
@Component
public class ModifyRequestBodyFilterFactory extends AbstractGatewayFilterFactory<ModifyRequestBodyFilterFactory.Config> {

    public ModifyRequestBodyFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            if (request.getHeaders().getContentLength() > 0) {
                return DataBufferUtils.join(request.getBody())
                    .flatMap(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        
                        // 修改请求体
                        String modifiedBody = modifyRequestBody(new String(bytes), config);
                        
                        // 创建新的请求
                        ServerHttpRequest newRequest = request.mutate()
                            .body(modifiedBody)
                            .build();
                        
                        return chain.filter(exchange.mutate().request(newRequest).build());
                    });
            }
            
            return chain.filter(exchange);
        };
    }

    private String modifyRequestBody(String originalBody, Config config) {
        // 实现请求体修改逻辑
        if (config.getTransformType().equals("uppercase")) {
            return originalBody.toUpperCase();
        } else if (config.getTransformType().equals("lowercase")) {
            return originalBody.toLowerCase();
        }
        return originalBody;
    }

    public static class Config {
        private String transformType = "none";
        private Map<String, String> replacements = new HashMap<>();

        public String getTransformType() {
            return transformType;
        }

        public void setTransformType(String transformType) {
            this.transformType = transformType;
        }

        public Map<String, String> getReplacements() {
            return replacements;
        }

        public void setReplacements(Map<String, String> replacements) {
            this.replacements = replacements;
        }
    }
}
```

## 响应处理过滤器

### 1. 响应头处理

```java
@Component
public class AddResponseHeaderFilterFactory extends AbstractGatewayFilterFactory<AddResponseHeaderFilterFactory.Config> {

    public AddResponseHeaderFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    
                    // 添加响应头
                    config.getHeaders().forEach((name, value) -> {
                        response.getHeaders().add(name, value);
                    });
                }));
        };
    }

    public static class Config {
        private Map<String, String> headers = new HashMap<>();

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
    }
}
```

### 2. 响应体处理

```java
@Component
public class ModifyResponseBodyFilterFactory extends AbstractGatewayFilterFactory<ModifyResponseBodyFilterFactory.Config> {

    public ModifyResponseBodyFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpResponse response = exchange.getResponse();
            DataBufferFactory bufferFactory = response.bufferFactory();
            
            return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    // 获取响应体并修改
                    if (response.getHeaders().getContentLength() > 0) {
                        // 实现响应体修改逻辑
                        String modifiedBody = modifyResponseBody(config);
                        DataBuffer buffer = bufferFactory.wrap(modifiedBody.getBytes());
                        response.getHeaders().setContentLength(modifiedBody.length());
                        response.writeWith(Mono.just(buffer));
                    }
                }));
        };
    }

    private String modifyResponseBody(Config config) {
        // 实现响应体修改逻辑
        return "Modified response body";
    }

    public static class Config {
        private String transformType = "none";
        private Map<String, String> replacements = new HashMap<>();

        public String getTransformType() {
            return transformType;
        }

        public void setTransformType(String transformType) {
            this.transformType = transformType;
        }

        public Map<String, String> getReplacements() {
            return replacements;
        }

        public void setReplacements(Map<String, String> replacements) {
            this.replacements = replacements;
        }
    }
}
```

## 路径处理过滤器

### 1. 路径重写

```java
@Component
public class RewritePathFilterFactory extends AbstractGatewayFilterFactory<RewritePathFilterFactory.Config> {

    public RewritePathFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            
            // 执行路径重写
            String newPath = rewritePath(path, config);
            
            if (!path.equals(newPath)) {
                URI newUri = request.getURI().resolve(newPath);
                ServerHttpRequest modifiedRequest = request.mutate().uri(newUri).build();
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }
            
            return chain.filter(exchange);
        };
    }

    private String rewritePath(String originalPath, Config config) {
        // 使用正则表达式重写路径
        return originalPath.replaceAll(config.getRegexp(), config.getReplacement());
    }

    public static class Config {
        private String regexp;
        private String replacement;

        public String getRegexp() {
            return regexp;
        }

        public void setRegexp(String regexp) {
            this.regexp = regexp;
        }

        public String getReplacement() {
            return replacement;
        }

        public void setReplacement(String replacement) {
            this.replacement = replacement;
        }
    }
}
```

### 2. 路径前缀处理

```java
@Component
public class PrefixPathFilterFactory extends AbstractGatewayFilterFactory<PrefixPathFilterFactory.Config> {

    public PrefixPathFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            
            // 添加路径前缀
            String newPath = config.getPrefix() + path;
            
            URI newUri = request.getURI().resolve(newPath);
            ServerHttpRequest modifiedRequest = request.mutate().uri(newUri).build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    public static class Config {
        private String prefix = "";

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }
}
```

## 安全过滤器

### 1. 认证过滤器

```java
@Component
public class AuthenticationFilterFactory extends AbstractGatewayFilterFactory<AuthenticationFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilterFactory.class);

    public AuthenticationFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // 检查是否需要认证
            if (!requiresAuthentication(request, config)) {
                return chain.filter(exchange);
            }
            
            // 提取认证信息
            String token = extractToken(request);
            if (token == null) {
                return handleUnauthorized(exchange);
            }
            
            // 验证 Token
            return validateToken(token, config)
                .flatMap(user -> {
                    // 设置用户上下文
                    ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-ID", user.getId())
                        .header("X-User-Roles", String.join(",", user.getRoles()))
                        .build();
                    
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .onErrorResume(e -> handleAuthenticationError(exchange, e));
        };
    }

    private boolean requiresAuthentication(ServerHttpRequest request, Config config) {
        String path = request.getURI().getPath();
        return config.getSecuredPaths().stream()
            .anyMatch(path::startsWith);
    }

    private String extractToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    private Mono<User> validateToken(String token, Config config) {
        // 实现 Token 验证逻辑
        // 这里可以调用认证服务或验证 JWT
        return Mono.just(new User("user123", Arrays.asList("USER")));
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Unauthorized\",\"message\":\"Missing or invalid token\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> handleAuthenticationError(ServerWebExchange exchange, Throwable error) {
        logger.error("Authentication error: {}", error.getMessage());
        return handleUnauthorized(exchange);
    }

    public static class Config {
        private List<String> securedPaths = new ArrayList<>();
        private String tokenHeader = "Authorization";
        private String tokenPrefix = "Bearer ";
        private Duration tokenTimeout = Duration.ofMinutes(30);

        public List<String> getSecuredPaths() {
            return securedPaths;
        }

        public void setSecuredPaths(List<String> securedPaths) {
            this.securedPaths = securedPaths;
        }

        public String getTokenHeader() {
            return tokenHeader;
        }

        public void setTokenHeader(String tokenHeader) {
            this.tokenHeader = tokenHeader;
        }

        public String getTokenPrefix() {
            return tokenPrefix;
        }

        public void setTokenPrefix(String tokenPrefix) {
            this.tokenPrefix = tokenPrefix;
        }

        public Duration getTokenTimeout() {
            return tokenTimeout;
        }

        public void setTokenTimeout(Duration tokenTimeout) {
            this.tokenTimeout = tokenTimeout;
        }
    }

    public static class User {
        private String id;
        private List<String> roles;

        public User(String id, List<String> roles) {
            this.id = id;
            this.roles = roles;
        }

        public String getId() {
            return id;
        }

        public List<String> getRoles() {
            return roles;
        }
    }
}
```

### 2. 授权过滤器

```java
@Component
public class AuthorizationFilterFactory extends AbstractGatewayFilterFactory<AuthorizationFilterFactory.Config> {

    public AuthorizationFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // 获取用户角色
            String userRoles = request.getHeaders().getFirst("X-User-Roles");
            if (userRoles == null) {
                return handleForbidden(exchange);
            }
            
            // 检查权限
            if (!hasRequiredRoles(userRoles, config.getRequiredRoles())) {
                return handleForbidden(exchange);
            }
            
            return chain.filter(exchange);
        };
    }

    private boolean hasRequiredRoles(String userRoles, List<String> requiredRoles) {
        List<String> roles = Arrays.asList(userRoles.split(","));
        return requiredRoles.stream().anyMatch(roles::contains);
    }

    private Mono<Void> handleForbidden(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Forbidden\",\"message\":\"Insufficient permissions\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        private List<String> requiredRoles = new ArrayList<>();
        private String roleHeader = "X-User-Roles";

        public List<String> getRequiredRoles() {
            return requiredRoles;
        }

        public void setRequiredRoles(List<String> requiredRoles) {
            this.requiredRoles = requiredRoles;
        }

        public String getRoleHeader() {
            return roleHeader;
        }

        public void setRoleHeader(String roleHeader) {
            this.roleHeader = roleHeader;
        }
    }
}
```

## 限流过滤器

### 1. 基于 Redis 的限流

```java
@Component
public class RedisRateLimitFilterFactory extends AbstractGatewayFilterFactory<RedisRateLimitFilterFactory.Config> {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisRateLimitFilterFactory(RedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String key = generateKey(exchange, config);
            
            return checkRateLimit(key, config)
                .flatMap(allowed -> {
                    if (allowed) {
                        return chain.filter(exchange);
                    } else {
                        return handleRateLimitExceeded(exchange);
                    }
                });
        };
    }

    private String generateKey(ServerWebExchange exchange, Config config) {
        ServerHttpRequest request = exchange.getRequest();
        String clientId = request.getHeaders().getFirst("X-Client-ID");
        if (clientId == null) {
            clientId = request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "rate_limit:" + config.getKeyPrefix() + ":" + clientId;
    }

    private Mono<Boolean> checkRateLimit(String key, Config config) {
        return Mono.fromCallable(() -> {
            String script = buildLuaScript();
            List<String> keys = Arrays.asList(key);
            List<String> args = Arrays.asList(
                String.valueOf(config.getWindowSize()),
                String.valueOf(config.getMaxRequests())
            );
            
            Long result = redisTemplate.execute(
                RedisScript.of(script, Long.class),
                keys,
                args.toArray()
            );
            
            return result != null && result > 0;
        });
    }

    private String buildLuaScript() {
        return """
            local key = KEYS[1]
            local window = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])
            local current = redis.call('GET', key)
            if current == false then
                redis.call('SET', key, 1)
                redis.call('EXPIRE', key, window)
                return 1
            else
                local count = tonumber(current)
                if count < limit then
                    redis.call('INCR', key)
                    return 1
                else
                    return 0
                end
            end
            """;
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("Retry-After", "60");
        
        String body = "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        private String keyPrefix = "default";
        private int windowSize = 60; // 秒
        private int maxRequests = 100;

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        public int getWindowSize() {
            return windowSize;
        }

        public void setWindowSize(int windowSize) {
            this.windowSize = windowSize;
        }

        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }
    }
}
```

## 缓存过滤器

### 1. 响应缓存

```java
@Component
public class ResponseCacheFilterFactory extends AbstractGatewayFilterFactory<ResponseCacheFilterFactory.Config> {

    private final CacheManager cacheManager;

    public ResponseCacheFilterFactory(CacheManager cacheManager) {
        super(Config.class);
        this.cacheManager = cacheManager;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String cacheKey = generateCacheKey(exchange);
            Cache cache = cacheManager.getCache(config.getCacheName());
            
            // 尝试从缓存获取
            Cache.ValueWrapper cached = cache.get(cacheKey);
            if (cached != null) {
                return writeCachedResponse(exchange, (CachedResponse) cached.get());
            }
            
            // 缓存未命中，继续处理并缓存响应
            return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    cacheResponse(exchange, cacheKey, config);
                }));
        };
    }

    private String generateCacheKey(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        return request.getMethod().name() + ":" + request.getURI().getPath();
    }

    private Mono<Void> writeCachedResponse(ServerWebExchange exchange, CachedResponse cachedResponse) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.valueOf(cachedResponse.getStatusCode()));
        
        cachedResponse.getHeaders().forEach((name, value) -> {
            response.getHeaders().add(name, value);
        });
        
        DataBuffer buffer = response.bufferFactory().wrap(cachedResponse.getBody());
        return response.writeWith(Mono.just(buffer));
    }

    private void cacheResponse(ServerWebExchange exchange, String cacheKey, Config config) {
        ServerHttpResponse response = exchange.getResponse();
        
        if (shouldCache(response, config)) {
            CachedResponse cachedResponse = new CachedResponse(
                response.getStatusCode().value(),
                response.getHeaders().toSingleValueMap(),
                "cached response body" // 实际实现中需要获取响应体
            );
            
            Cache cache = cacheManager.getCache(config.getCacheName());
            cache.put(cacheKey, cachedResponse);
        }
    }

    private boolean shouldCache(ServerHttpResponse response, Config config) {
        return response.getStatusCode().is2xxSuccessful() &&
               config.getCacheableStatusCodes().contains(response.getStatusCode().value());
    }

    public static class Config {
        private String cacheName = "response-cache";
        private List<Integer> cacheableStatusCodes = Arrays.asList(200, 201, 202);
        private Duration ttl = Duration.ofMinutes(5);

        public String getCacheName() {
            return cacheName;
        }

        public void setCacheName(String cacheName) {
            this.cacheName = cacheName;
        }

        public List<Integer> getCacheableStatusCodes() {
            return cacheableStatusCodes;
        }

        public void setCacheableStatusCodes(List<Integer> cacheableStatusCodes) {
            this.cacheableStatusCodes = cacheableStatusCodes;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }

    public static class CachedResponse {
        private int statusCode;
        private Map<String, String> headers;
        private String body;

        public CachedResponse(int statusCode, Map<String, String> headers, String body) {
            this.statusCode = statusCode;
            this.headers = headers;
            this.body = body;
        }

        // Getters
        public int getStatusCode() {
            return statusCode;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getBody() {
            return body;
        }
    }
}
```

## 使用自定义过滤器

### 1. 配置过滤器

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: custom-filter-route
          uri: lb://backend-service
          predicates:
            - Path=/api/**
          filters:
            - name: AddRequestHeader
              args:
                headers:
                  X-Gateway: TiGateway
                  X-Version: 1.0
            - name: Authentication
              args:
                securedPaths:
                  - /api/admin
                  - /api/user
                tokenHeader: Authorization
                tokenPrefix: Bearer
            - name: RedisRateLimit
              args:
                keyPrefix: api
                windowSize: 60
                maxRequests: 100
```

### 2. 测试过滤器

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomFilterIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCustomFilter() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer valid-token");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/test", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst("X-Gateway"));
    }
}
```

## 最佳实践

### 1. 性能优化

- 避免在过滤器中执行耗时操作
- 合理使用缓存
- 注意内存使用

### 2. 错误处理

- 提供适当的错误处理
- 记录详细的错误日志
- 提供降级机制

### 3. 配置管理

- 使用 `@ConfigurationProperties` 管理配置
- 提供合理的默认值
- 支持动态配置更新

### 4. 测试覆盖

- 编写完整的单元测试
- 提供集成测试
- 测试异常情况

---

**相关文档**:
- [自定义组件开发](./custom-components.md)
- [Spring Cloud Gateway 集成](./spring-cloud-gateway-integration.md)
- [开发环境搭建](./setup.md)
- [故障排除](../examples/troubleshooting.md)
