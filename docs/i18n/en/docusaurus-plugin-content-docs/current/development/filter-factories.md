# TiGateway Filter Factories Development Guide

## Overview

This guide provides detailed instructions for developing filter factories in TiGateway. You'll learn how to create custom filters for request processing, response processing, path processing, security, rate limiting, and caching.

## Basic Filter Factory Structure

### Abstract Filter Factory

```java
@Component
public class CustomFilterFactory extends AbstractGatewayFilterFactory<CustomFilterFactory.Config> {
    
    public CustomFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Filter logic implementation
            return chain.filter(exchange);
        };
    }
    
    @Data
    public static class Config {
        // Configuration properties
        private String property1;
        private String property2;
    }
}
```

### Filter Types

#### Pre Filter (Request Processing)

```java
@Component
@Slf4j
public class RequestHeaderFilterFactory extends AbstractGatewayFilterFactory<RequestHeaderFilterFactory.Config> {
    
    public RequestHeaderFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Pre-processing: Modify request
            ServerHttpRequest request = exchange.getRequest();
            
            ServerHttpRequest modifiedRequest = request.mutate()
                .header(config.getHeaderName(), config.getHeaderValue())
                .build();
            
            ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
            
            log.debug("Added header {}: {} to request", config.getHeaderName(), config.getHeaderValue());
            
            return chain.filter(modifiedExchange);
        };
    }
    
    @Data
    public static class Config {
        private String headerName = "X-Custom-Header";
        private String headerValue = "custom-value";
    }
}
```

#### Post Filter (Response Processing)

```java
@Component
@Slf4j
public class ResponseHeaderFilterFactory extends AbstractGatewayFilterFactory<ResponseHeaderFilterFactory.Config> {
    
    public ResponseHeaderFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    // Post-processing: Modify response
                    ServerHttpResponse response = exchange.getResponse();
                    response.getHeaders().add(config.getHeaderName(), config.getHeaderValue());
                    
                    log.debug("Added header {}: {} to response", config.getHeaderName(), config.getHeaderValue());
                }));
        };
    }
    
    @Data
    public static class Config {
        private String headerName = "X-Response-Header";
        private String headerValue = "response-value";
    }
}
```

#### Mixed Filter (Request and Response Processing)

```java
@Component
@Slf4j
public class MixedProcessingFilterFactory extends AbstractGatewayFilterFactory<MixedProcessingFilterFactory.Config> {
    
    public MixedProcessingFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            long startTime = System.currentTimeMillis();
            
            // Pre-processing
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Start-Time", String.valueOf(startTime))
                .build();
            
            ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
            
            return chain.filter(modifiedExchange)
                .then(Mono.fromRunnable(() -> {
                    // Post-processing
                    long duration = System.currentTimeMillis() - startTime;
                    ServerHttpResponse response = exchange.getResponse();
                    response.getHeaders().add("X-Processing-Time", String.valueOf(duration));
                    
                    log.debug("Request processed in {}ms", duration);
                }));
        };
    }
    
    @Data
    public static class Config {
        private boolean enableTiming = true;
        private String timingHeader = "X-Processing-Time";
    }
}
```

## Request Processing Filters

### Request Header Filters

#### Add Request Header Filter

```java
@Component
@Slf4j
public class AddRequestHeaderFilterFactory extends AbstractGatewayFilterFactory<AddRequestHeaderFilterFactory.Config> {
    
    public AddRequestHeaderFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Add multiple headers
            ServerHttpRequest.Builder requestBuilder = request.mutate();
            config.getHeaders().forEach(requestBuilder::header);
            
            ServerHttpRequest modifiedRequest = requestBuilder.build();
            ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
            
            log.debug("Added headers to request: {}", config.getHeaders());
            
            return chain.filter(modifiedExchange);
        };
    }
    
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("headers");
    }
    
    @Data
    public static class Config {
        private Map<String, String> headers = new HashMap<>();
    }
}
```

#### Remove Request Header Filter

```java
@Component
@Slf4j
public class RemoveRequestHeaderFilterFactory extends AbstractGatewayFilterFactory<RemoveRequestHeaderFilterFactory.Config> {
    
    public RemoveRequestHeaderFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Remove specified headers
            ServerHttpRequest.Builder requestBuilder = request.mutate();
            config.getHeaderNames().forEach(requestBuilder::removeHeader);
            
            ServerHttpRequest modifiedRequest = requestBuilder.build();
            ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
            
            log.debug("Removed headers from request: {}", config.getHeaderNames());
            
            return chain.filter(modifiedExchange);
        };
    }
    
    @Data
    public static class Config {
        private List<String> headerNames = new ArrayList<>();
    }
}
```

### Request Parameter Filters

#### Add Request Parameter Filter

```java
@Component
@Slf4j
public class AddRequestParameterFilterFactory extends AbstractGatewayFilterFactory<AddRequestParameterFilterFactory.Config> {
    
    public AddRequestParameterFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            URI originalUri = request.getURI();
            
            // Build new URI with additional parameters
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(originalUri);
            config.getParameters().forEach(uriBuilder::queryParam);
            
            URI modifiedUri = uriBuilder.build().toUri();
            ServerHttpRequest modifiedRequest = request.mutate()
                .uri(modifiedUri)
                .build();
            
            ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
            
            log.debug("Added parameters to request: {}", config.getParameters());
            
            return chain.filter(modifiedExchange);
        };
    }
    
    @Data
    public static class Config {
        private Map<String, String> parameters = new HashMap<>();
    }
}
```

### Request Body Filters

#### Request Body Transformation Filter

```java
@Component
@Slf4j
public class RequestBodyTransformFilterFactory extends AbstractGatewayFilterFactory<RequestBodyTransformFilterFactory.Config> {
    
    private final ObjectMapper objectMapper;
    
    public RequestBodyTransformFilterFactory(ObjectMapper objectMapper) {
        super(Config.class);
        this.objectMapper = objectMapper;
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            if (request.getHeaders().getContentType() != null && 
                request.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)) {
                
                return DataBufferUtils.join(request.getBody())
                    .defaultIfEmpty(DataBufferFactory.DEFAULT_ALLOCATOR.allocateBuffer(0))
                    .flatMap(dataBuffer -> {
                        try {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            
                            String jsonString = new String(bytes, StandardCharsets.UTF_8);
                            String transformedJson = transformJson(jsonString, config);
                            
                            byte[] transformedBytes = transformedJson.getBytes(StandardCharsets.UTF_8);
                            DataBuffer transformedDataBuffer = exchange.getResponse().bufferFactory().wrap(transformedBytes);
                            
                            ServerHttpRequest modifiedRequest = request.mutate()
                                .body(Flux.just(transformedDataBuffer))
                                .build();
                            
                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                            
                        } catch (Exception e) {
                            log.error("Error transforming request body", e);
                            return chain.filter(exchange);
                        }
                    });
            }
            
            return chain.filter(exchange);
        };
    }
    
    private String transformJson(String jsonString, Config config) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            
            // Apply transformations based on config
            if (config.isAddTimestamp()) {
                ((ObjectNode) jsonNode).put("timestamp", Instant.now().toString());
            }
            
            if (config.isAddRequestId()) {
                ((ObjectNode) jsonNode).put("requestId", UUID.randomUUID().toString());
            }
            
            if (config.getFieldMappings() != null) {
                config.getFieldMappings().forEach((oldField, newField) -> {
                    JsonNode value = jsonNode.get(oldField);
                    if (value != null) {
                        ((ObjectNode) jsonNode).remove(oldField);
                        ((ObjectNode) jsonNode).set(newField, value);
                    }
                });
            }
            
            return objectMapper.writeValueAsString(jsonNode);
            
        } catch (Exception e) {
            log.error("Error transforming JSON", e);
            return jsonString;
        }
    }
    
    @Data
    public static class Config {
        private boolean addTimestamp = false;
        private boolean addRequestId = false;
        private Map<String, String> fieldMappings = new HashMap<>();
    }
}
```

## Response Processing Filters

### Response Header Filters

#### Add Response Header Filter

```java
@Component
@Slf4j
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
                    
                    // Add multiple headers
                    config.getHeaders().forEach((name, value) -> {
                        response.getHeaders().add(name, value);
                    });
                    
                    log.debug("Added headers to response: {}", config.getHeaders());
                }));
        };
    }
    
    @Data
    public static class Config {
        private Map<String, String> headers = new HashMap<>();
    }
}
```

#### Remove Response Header Filter

```java
@Component
@Slf4j
public class RemoveResponseHeaderFilterFactory extends AbstractGatewayFilterFactory<RemoveResponseHeaderFilterFactory.Config> {
    
    public RemoveResponseHeaderFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    
                    // Remove specified headers
                    config.getHeaderNames().forEach(response.getHeaders()::remove);
                    
                    log.debug("Removed headers from response: {}", config.getHeaderNames());
                }));
        };
    }
    
    @Data
    public static class Config {
        private List<String> headerNames = new ArrayList<>();
    }
}
```

### Response Body Filters

#### Response Body Transformation Filter

```java
@Component
@Slf4j
public class ResponseBodyTransformFilterFactory extends AbstractGatewayFilterFactory<ResponseBodyTransformFilterFactory.Config> {
    
    private final ObjectMapper objectMapper;
    
    public ResponseBodyTransformFilterFactory(ObjectMapper objectMapper) {
        super(Config.class);
        this.objectMapper = objectMapper;
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    
                    if (response.getHeaders().getContentType() != null && 
                        response.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON)) {
                        
                        // Transform response body
                        transformResponseBody(response, config);
                    }
                }));
        };
    }
    
    private void transformResponseBody(ServerHttpResponse response, Config config) {
        // Implementation for response body transformation
        // This would require more complex handling of the response body stream
        log.debug("Transforming response body with config: {}", config);
    }
    
    @Data
    public static class Config {
        private boolean addTimestamp = false;
        private boolean addRequestId = false;
        private Map<String, String> fieldMappings = new HashMap<>();
    }
}
```

## Path Processing Filters

### Path Rewrite Filter

```java
@Component
@Slf4j
public class PathRewriteFilterFactory extends AbstractGatewayFilterFactory<PathRewriteFilterFactory.Config> {
    
    public PathRewriteFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            
            // Apply path rewrite rules
            String rewrittenPath = rewritePath(path, config);
            
            if (!path.equals(rewrittenPath)) {
                URI originalUri = request.getURI();
                URI modifiedUri = UriComponentsBuilder.fromUri(originalUri)
                    .replacePath(rewrittenPath)
                    .build()
                    .toUri();
                
                ServerHttpRequest modifiedRequest = request.mutate()
                    .uri(modifiedUri)
                    .build();
                
                ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();
                
                log.debug("Rewrote path from {} to {}", path, rewrittenPath);
                
                return chain.filter(modifiedExchange);
            }
            
            return chain.filter(exchange);
        };
    }
    
    private String rewritePath(String path, Config config) {
        String rewrittenPath = path;
        
        // Apply rewrite rules
        for (PathRewriteRule rule : config.getRules()) {
            if (rewrittenPath.matches(rule.getPattern())) {
                rewrittenPath = rewrittenPath.replaceAll(rule.getPattern(), rule.getReplacement());
                break; // Apply first matching rule
            }
        }
        
        return rewrittenPath;
    }
    
    @Data
    public static class Config {
        private List<PathRewriteRule> rules = new ArrayList<>();
    }
    
    @Data
    public static class PathRewriteRule {
        private String pattern;
        private String replacement;
    }
}
```

### Path Prefix Filter

```java
@Component
@Slf4j
public class PathPrefixFilterFactory extends AbstractGatewayFilterFactory<PathPrefixFilterFactory.Config> {
    
    public PathPrefixFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            
            String modifiedPath;
            if (config.isAddPrefix()) {
                modifiedPath = config.getPrefix() + path;
            } else {
                // Remove prefix
                if (path.startsWith(config.getPrefix())) {
                    modifiedPath = path.substring(config.getPrefix().length());
                } else {
                    modifiedPath = path;
                }
            }
            
            URI originalUri = request.getURI();
            URI modifiedUri = UriComponentsBuilder.fromUri(originalUri)
                .replacePath(modifiedPath)
                .build()
                .toUri();
            
            ServerHttpRequest modifiedRequest = request.mutate()
                .uri(modifiedUri)
                .build();
            
            ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
            
            log.debug("Modified path from {} to {}", path, modifiedPath);
            
            return chain.filter(modifiedExchange);
        };
    }
    
    @Data
    public static class Config {
        private String prefix = "/api";
        private boolean addPrefix = true;
    }
}
```

## Security Filters

### Authentication Filter

```java
@Component
@Slf4j
public class AuthenticationFilterFactory extends AbstractGatewayFilterFactory<AuthenticationFilterFactory.Config> {
    
    private final JwtUtil jwtUtil;
    
    public AuthenticationFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Extract token from request
            String token = extractToken(request, config);
            
            if (token == null) {
                return handleUnauthorized(exchange, "No authentication token provided");
            }
            
            try {
                // Validate token
                Claims claims = jwtUtil.validateToken(token);
                
                // Add user information to request
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", claims.getSubject())
                    .header("X-User-Roles", String.join(",", claims.get("roles", List.class)))
                    .build();
                
                ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();
                
                log.debug("Authenticated user: {}", claims.getSubject());
                
                return chain.filter(modifiedExchange);
                
            } catch (Exception e) {
                log.warn("Authentication failed: {}", e.getMessage());
                return handleUnauthorized(exchange, "Invalid authentication token");
            }
        };
    }
    
    private String extractToken(ServerHttpRequest request, Config config) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return request.getQueryParams().getFirst(config.getTokenParam());
    }
    
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }
    
    @Data
    public static class Config {
        private String tokenParam = "token";
        private boolean required = true;
    }
}
```

### Authorization Filter

```java
@Component
@Slf4j
public class AuthorizationFilterFactory extends AbstractGatewayFilterFactory<AuthorizationFilterFactory.Config> {
    
    public AuthorizationFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Extract user roles from request
            String userRoles = request.getHeaders().getFirst("X-User-Roles");
            
            if (userRoles == null) {
                return handleForbidden(exchange, "No user roles found");
            }
            
            List<String> roles = Arrays.asList(userRoles.split(","));
            
            // Check if user has required roles
            boolean hasRequiredRole = config.getRequiredRoles().stream()
                .anyMatch(roles::contains);
            
            if (!hasRequiredRole) {
                return handleForbidden(exchange, "Insufficient permissions");
            }
            
            log.debug("User authorized with roles: {}", roles);
            
            return chain.filter(exchange);
        };
    }
    
    private Mono<Void> handleForbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format("{\"error\": \"Forbidden\", \"message\": \"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }
    
    @Data
    public static class Config {
        private List<String> requiredRoles = new ArrayList<>();
    }
}
```

## Rate Limiting Filters

### Redis-based Rate Limiting Filter

```java
@Component
@Slf4j
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
                        return handleRateLimitExceeded(exchange, config);
                    }
                });
        };
    }
    
    private String generateKey(ServerWebExchange exchange, Config config) {
        ServerHttpRequest request = exchange.getRequest();
        
        switch (config.getKeyType()) {
            case IP:
                return "rate_limit:ip:" + getClientIp(request);
            case USER:
                String userId = request.getHeaders().getFirst("X-User-ID");
                return "rate_limit:user:" + (userId != null ? userId : "anonymous");
            case GLOBAL:
                return "rate_limit:global";
            default:
                return "rate_limit:default";
        }
    }
    
    private Mono<Boolean> checkRateLimit(String key, Config config) {
        return Mono.fromCallable(() -> {
            String script = """
                local key = KEYS[1]
                local limit = tonumber(ARGV[1])
                local window = tonumber(ARGV[2])
                local current = redis.call('GET', key)
                
                if current == false then
                    redis.call('SET', key, 1)
                    redis.call('EXPIRE', key, window)
                    return 1
                end
                
                if tonumber(current) < limit then
                    redis.call('INCR', key)
                    return 1
                end
                
                return 0
                """;
            
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);
            
            Long result = redisTemplate.execute(redisScript, 
                Collections.singletonList(key), 
                config.getLimit(), 
                config.getWindow());
            
            return result != null && result == 1;
        });
    }
    
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, Config config) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(config.getLimit()));
        response.getHeaders().add("X-RateLimit-Remaining", "0");
        response.getHeaders().add("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + config.getWindow() * 1000));
        
        String body = String.format("{\"error\": \"Rate limit exceeded\", \"limit\": %d, \"window\": %d}", 
            config.getLimit(), config.getWindow());
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }
    
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    
    @Data
    public static class Config {
        private int limit = 100;
        private int window = 60; // seconds
        private KeyType keyType = KeyType.IP;
        
        public enum KeyType {
            IP, USER, GLOBAL
        }
    }
}
```

## Cache Filters

### Response Cache Filter

```java
@Component
@Slf4j
public class ResponseCacheFilterFactory extends AbstractGatewayFilterFactory<ResponseCacheFilterFactory.Config> {
    
    private final CacheManager cacheManager;
    
    public ResponseCacheFilterFactory(CacheManager cacheManager) {
        super(Config.class);
        this.cacheManager = cacheManager;
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String cacheKey = generateCacheKey(exchange, config);
            
            // Try to get from cache first
            return getFromCache(cacheKey, config)
                .cast(ServerHttpResponse.class)
                .switchIfEmpty(
                    // Cache miss, process request and cache response
                    chain.filter(exchange)
                        .then(Mono.fromRunnable(() -> {
                            ServerHttpResponse response = exchange.getResponse();
                            if (isCacheable(response, config)) {
                                cacheResponse(cacheKey, response, config);
                            }
                        }))
                        .then(Mono.just(exchange.getResponse()))
                );
        };
    }
    
    private String generateCacheKey(ServerWebExchange exchange, Config config) {
        ServerHttpRequest request = exchange.getRequest();
        StringBuilder keyBuilder = new StringBuilder();
        
        keyBuilder.append(request.getMethod().name())
            .append(":")
            .append(request.getURI().getPath());
        
        if (config.isIncludeQueryParams()) {
            keyBuilder.append(":").append(request.getURI().getQuery());
        }
        
        if (config.isIncludeHeaders()) {
            config.getHeaderNames().forEach(headerName -> {
                String headerValue = request.getHeaders().getFirst(headerName);
                if (headerValue != null) {
                    keyBuilder.append(":").append(headerName).append("=").append(headerValue);
                }
            });
        }
        
        return "cache:" + DigestUtils.md5Hex(keyBuilder.toString());
    }
    
    private Mono<ServerHttpResponse> getFromCache(String cacheKey, Config config) {
        return Mono.fromCallable(() -> {
            Cache cache = cacheManager.getCache(config.getCacheName());
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(cacheKey);
                if (wrapper != null) {
                    return (ServerHttpResponse) wrapper.get();
                }
            }
            return null;
        });
    }
    
    private void cacheResponse(String cacheKey, ServerHttpResponse response, Config config) {
        try {
            Cache cache = cacheManager.getCache(config.getCacheName());
            if (cache != null) {
                cache.put(cacheKey, response);
                log.debug("Cached response for key: {}", cacheKey);
            }
        } catch (Exception e) {
            log.warn("Failed to cache response for key: {}", cacheKey, e);
        }
    }
    
    private boolean isCacheable(ServerHttpResponse response, Config config) {
        HttpStatus status = response.getStatusCode();
        return status != null && status.is2xxSuccessful() && 
               config.getCacheableStatusCodes().contains(status.value());
    }
    
    @Data
    public static class Config {
        private String cacheName = "responseCache";
        private int ttl = 300; // seconds
        private boolean includeQueryParams = true;
        private boolean includeHeaders = false;
        private List<String> headerNames = new ArrayList<>();
        private List<Integer> cacheableStatusCodes = Arrays.asList(200, 201, 202);
    }
}
```

## Testing Filter Factories

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class RequestHeaderFilterFactoryTest {
    
    private RequestHeaderFilterFactory filterFactory;
    
    @BeforeEach
    void setUp() {
        filterFactory = new RequestHeaderFilterFactory();
    }
    
    @Test
    @DisplayName("Should add header to request")
    void shouldAddHeaderToRequest() {
        // Given
        RequestHeaderFilterFactory.Config config = new RequestHeaderFilterFactory.Config();
        config.setHeaderName("X-Test-Header");
        config.setHeaderValue("test-value");
        
        ServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        GatewayFilter filter = filterFactory.apply(config);
        Mono<Void> result = filter.filter(exchange, mock(GatewayFilterChain.class));
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        assertThat(exchange.getRequest().getHeaders().getFirst("X-Test-Header"))
            .isEqualTo("test-value");
    }
}
```

### Integration Tests

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilterFactoryIntegrationTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    @DisplayName("Should apply custom filter in route")
    void shouldApplyCustomFilterInRoute() {
        // When & Then
        webTestClient.get()
            .uri("/test")
            .header("X-Custom-Header", "test-value")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists("X-Response-Header");
    }
}
```

## Best Practices

### 1. Filter Design

- Keep filters focused and single-purpose
- Use reactive programming patterns
- Handle errors gracefully
- Implement proper logging

### 2. Configuration

- Provide sensible defaults
- Validate configuration values
- Support both programmatic and declarative configuration
- Use shortcut field order for simple configurations

### 3. Performance

- Minimize object creation in hot paths
- Use efficient data structures
- Implement proper caching where appropriate
- Monitor performance impact

### 4. Testing

- Write comprehensive unit tests
- Include integration tests
- Test error scenarios
- Use proper mocking and test utilities

---

**Related Documentation**:
- [Custom Components Development](./custom-components.md)
- [Predicate Factories Development](./predicate-factories.md)
- [Spring Cloud Gateway Integration](./spring-cloud-gateway-integration.md)
- [Testing Guide](./testing.md)
