# Authentication and Authorization

This guide covers TiGateway's authentication and authorization capabilities, including JWT token validation, OAuth2 integration, role-based access control (RBAC), and security best practices.

## Overview

TiGateway provides comprehensive security features:

- **JWT Authentication**: Token-based authentication with JWT
- **OAuth2 Integration**: OAuth2 resource server support
- **Role-Based Access Control**: Fine-grained permission management
- **API Key Authentication**: Simple API key-based authentication
- **Multi-tenant Security**: Tenant isolation and security
- **Security Headers**: CORS, CSRF, and security headers

## JWT Authentication

### JWT Configuration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${JWT_JWK_SET_URI:http://auth-service:8080/.well-known/jwks.json}
          issuer-uri: ${JWT_ISSUER_URI:http://auth-service:8080}
          audience: ${JWT_AUDIENCE:api-gateway}
```

### JWT Token Provider

```java
@Component
public class JwtTokenProvider {
    
    private final JwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;
    
    public JwtTokenProvider(JwtDecoder jwtDecoder, JwtEncoder jwtEncoder) {
        this.jwtDecoder = jwtDecoder;
        this.jwtEncoder = jwtEncoder;
    }
    
    public boolean validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return !isTokenExpired(jwt);
        } catch (JwtException e) {
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getClaimAsString("sub");
    }
    
    public List<String> getRolesFromToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getClaimAsStringList("roles");
    }
    
    public String getTenantFromToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getClaimAsString("tenant");
    }
    
    private boolean isTokenExpired(Jwt jwt) {
        Instant expiration = jwt.getExpiresAt();
        return expiration != null && expiration.isBefore(Instant.now());
    }
}
```

### JWT Authentication Filter

```java
@Component
@Order(-100)
public class JwtAuthenticationFilter implements GlobalFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final List<String> publicPaths = Arrays.asList(
            "/actuator/health",
            "/actuator/info",
            "/api/auth/login",
            "/api/auth/refresh"
    );
    
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        String token = extractToken(request);
        
        if (token == null) {
            return unauthorized(exchange, "Missing authentication token");
        }
        
        if (!jwtTokenProvider.validateToken(token)) {
            return unauthorized(exchange, "Invalid authentication token");
        }
        
        // Add user information to request
        String username = jwtTokenProvider.getUsernameFromToken(token);
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);
        String tenant = jwtTokenProvider.getTenantFromToken(token);
        
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User", username)
                .header("X-Roles", String.join(",", roles))
                .header("X-Tenant", tenant)
                .build();
        
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        
        return chain.filter(modifiedExchange);
    }
    
    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }
    
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Unauthorized");
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        
        String body = new ObjectMapper().writeValueAsString(error);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
```

## OAuth2 Integration

### OAuth2 Resource Server Configuration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${OAUTH2_JWK_SET_URI}
          issuer-uri: ${OAUTH2_ISSUER_URI}
          audience: ${OAUTH2_AUDIENCE}
```

### OAuth2 Security Configuration

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")
                        .pathMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .csrf(csrf -> csrf.disable())
                .build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
                .build();
    }
    
    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
```

### OAuth2 Token Validation

```java
@Component
public class OAuth2TokenValidator implements GlobalFilter, Ordered {
    
    private final ReactiveJwtDecoder jwtDecoder;
    
    public OAuth2TokenValidator(ReactiveJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String token = extractToken(request);
        
        if (token == null) {
            return unauthorized(exchange);
        }
        
        return jwtDecoder.decode(token)
                .flatMap(jwt -> {
                    // Add JWT claims to request headers
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User", jwt.getClaimAsString("sub"))
                            .header("X-Roles", String.join(",", jwt.getClaimAsStringList("roles")))
                            .header("X-Tenant", jwt.getClaimAsString("tenant"))
                            .build();
                    
                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(modifiedRequest)
                            .build();
                    
                    return chain.filter(modifiedExchange);
                })
                .onErrorResume(JwtException.class, e -> unauthorized(exchange));
    }
    
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Unauthorized\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -100;
    }
}
```

## Role-Based Access Control (RBAC)

### RBAC Configuration

```yaml
security:
  rbac:
    enabled: true
    default-role: USER
    roles:
      ADMIN:
        permissions:
          - "user:read"
          - "user:write"
          - "user:delete"
          - "admin:read"
          - "admin:write"
          - "admin:delete"
      USER:
        permissions:
          - "user:read"
          - "user:write"
      GUEST:
        permissions:
          - "user:read"
```

### RBAC Filter

```java
@Component
@Order(-99)
public class RbacFilter implements GlobalFilter {
    
    private final RbacConfig rbacConfig;
    
    public RbacFilter(RbacConfig rbacConfig) {
        this.rbacConfig = rbacConfig;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        
        // Extract user roles from request headers
        String rolesHeader = request.getHeaders().getFirst("X-Roles");
        if (rolesHeader == null) {
            return forbidden(exchange, "No roles found in request");
        }
        
        List<String> userRoles = Arrays.asList(rolesHeader.split(","));
        
        // Check if user has required permissions
        if (!hasPermission(userRoles, path, method)) {
            return forbidden(exchange, "Insufficient permissions");
        }
        
        return chain.filter(exchange);
    }
    
    private boolean hasPermission(List<String> userRoles, String path, String method) {
        // Map path and method to required permission
        String requiredPermission = mapToPermission(path, method);
        
        // Check if any user role has the required permission
        return userRoles.stream()
                .anyMatch(role -> rbacConfig.hasPermission(role, requiredPermission));
    }
    
    private String mapToPermission(String path, String method) {
        // Simple mapping logic - can be made more sophisticated
        if (path.startsWith("/api/admin/")) {
            return "admin:" + method.toLowerCase();
        } else if (path.startsWith("/api/users/")) {
            return "user:" + method.toLowerCase();
        }
        return "read"; // Default permission
    }
    
    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Forbidden");
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        
        String body = new ObjectMapper().writeValueAsString(error);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
```

### RBAC Configuration Class

```java
@ConfigurationProperties(prefix = "security.rbac")
@Data
public class RbacConfig {
    
    private boolean enabled = true;
    private String defaultRole = "USER";
    private Map<String, RoleConfig> roles = new HashMap<>();
    
    public boolean hasPermission(String role, String permission) {
        RoleConfig roleConfig = roles.get(role);
        if (roleConfig == null) {
            return false;
        }
        return roleConfig.getPermissions().contains(permission);
    }
    
    @Data
    public static class RoleConfig {
        private List<String> permissions = new ArrayList<>();
    }
}
```

## API Key Authentication

### API Key Configuration

```yaml
security:
  api-key:
    enabled: true
    header-name: X-API-Key
    keys:
      - key: "admin-key-123"
        roles: ["ADMIN"]
        permissions: ["*"]
      - key: "user-key-456"
        roles: ["USER"]
        permissions: ["user:read", "user:write"]
      - key: "guest-key-789"
        roles: ["GUEST"]
        permissions: ["user:read"]
```

### API Key Filter

```java
@Component
@Order(-98)
public class ApiKeyFilter implements GlobalFilter {
    
    private final ApiKeyConfig apiKeyConfig;
    
    public ApiKeyFilter(ApiKeyConfig apiKeyConfig) {
        this.apiKeyConfig = apiKeyConfig;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!apiKeyConfig.isEnabled()) {
            return chain.filter(exchange);
        }
        
        ServerHttpRequest request = exchange.getRequest();
        String apiKey = request.getHeaders().getFirst(apiKeyConfig.getHeaderName());
        
        if (apiKey == null) {
            return unauthorized(exchange, "Missing API key");
        }
        
        ApiKeyConfig.KeyConfig keyConfig = apiKeyConfig.getKeyConfig(apiKey);
        if (keyConfig == null) {
            return unauthorized(exchange, "Invalid API key");
        }
        
        // Add API key information to request headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-API-Key-Roles", String.join(",", keyConfig.getRoles()))
                .header("X-API-Key-Permissions", String.join(",", keyConfig.getPermissions()))
                .build();
        
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        
        return chain.filter(modifiedExchange);
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Unauthorized");
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        
        String body = new ObjectMapper().writeValueAsString(error);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
```

### API Key Configuration Class

```java
@ConfigurationProperties(prefix = "security.api-key")
@Data
public class ApiKeyConfig {
    
    private boolean enabled = true;
    private String headerName = "X-API-Key";
    private Map<String, KeyConfig> keys = new HashMap<>();
    
    public KeyConfig getKeyConfig(String key) {
        return keys.get(key);
    }
    
    @Data
    public static class KeyConfig {
        private List<String> roles = new ArrayList<>();
        private List<String> permissions = new ArrayList<>();
    }
}
```

## Multi-tenant Security

### Tenant Isolation

```java
@Component
@Order(-97)
public class TenantIsolationFilter implements GlobalFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String tenant = request.getHeaders().getFirst("X-Tenant");
        
        if (tenant == null) {
            return badRequest(exchange, "Missing tenant information");
        }
        
        // Validate tenant access
        if (!isValidTenant(tenant)) {
            return forbidden(exchange, "Invalid tenant");
        }
        
        // Add tenant context to request
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Tenant-Context", tenant)
                .build();
        
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        
        return chain.filter(modifiedExchange);
    }
    
    private boolean isValidTenant(String tenant) {
        // Implement tenant validation logic
        return tenant != null && !tenant.trim().isEmpty();
    }
    
    private Mono<Void> badRequest(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().add("Content-Type", "application/json");
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        
        String body = new ObjectMapper().writeValueAsString(error);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
    
    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Forbidden");
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        
        String body = new ObjectMapper().writeValueAsString(error);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
```

## Security Headers

### CORS Configuration

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
            allowed-headers: "*"
            allow-credentials: true
            max-age: 3600
```

### Security Headers Filter

```java
@Component
@Order(-50)
public class SecurityHeadersFilter implements GlobalFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        
        // Add security headers
        response.getHeaders().add("X-Content-Type-Options", "nosniff");
        response.getHeaders().add("X-Frame-Options", "DENY");
        response.getHeaders().add("X-XSS-Protection", "1; mode=block");
        response.getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        response.getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");
        response.getHeaders().add("Content-Security-Policy", "default-src 'self'");
        
        return chain.filter(exchange);
    }
}
```

### CSRF Protection

```java
@Component
@Order(-49)
public class CsrfProtectionFilter implements GlobalFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        
        // Skip CSRF protection for safe methods
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return chain.filter(exchange);
        }
        
        // Check CSRF token
        String csrfToken = request.getHeaders().getFirst("X-CSRF-Token");
        if (csrfToken == null) {
            return forbidden(exchange, "Missing CSRF token");
        }
        
        // Validate CSRF token (implement your validation logic)
        if (!isValidCsrfToken(csrfToken)) {
            return forbidden(exchange, "Invalid CSRF token");
        }
        
        return chain.filter(exchange);
    }
    
    private boolean isValidCsrfToken(String token) {
        // Implement CSRF token validation logic
        return token != null && !token.trim().isEmpty();
    }
    
    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Forbidden");
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        
        String body = new ObjectMapper().writeValueAsString(error);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
```

## Rate Limiting and Security

### Rate Limiting Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rate-limited-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                redis-rate-limiter.requestedTokens: 1
                key-resolver: "#{@userKeyResolver}"
```

### Security Rate Limiting

```java
@Component
public class SecurityRateLimiter implements GlobalFilter, Ordered {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public SecurityRateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String clientIp = getClientIp(request);
        
        // Check rate limit for authentication attempts
        if (isAuthEndpoint(request.getURI().getPath())) {
            return checkAuthRateLimit(clientIp)
                    .flatMap(allowed -> {
                        if (allowed) {
                            return chain.filter(exchange);
                        } else {
                            return tooManyRequests(exchange);
                        }
                    });
        }
        
        return chain.filter(exchange);
    }
    
    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/auth/");
    }
    
    private Mono<Boolean> checkAuthRateLimit(String clientIp) {
        String key = "auth_rate_limit:" + clientIp;
        return Mono.fromCallable(() -> {
            String count = redisTemplate.opsForValue().get(key);
            if (count == null) {
                redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
                return true;
            }
            
            int attempts = Integer.parseInt(count);
            if (attempts >= 5) { // Max 5 attempts per minute
                return false;
            }
            
            redisTemplate.opsForValue().increment(key);
            return true;
        });
    }
    
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null) {
            return xRealIp;
        }
        
        return request.getRemoteAddress().getAddress().getHostAddress();
    }
    
    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Too Many Requests");
        error.put("message", "Rate limit exceeded");
        error.put("timestamp", System.currentTimeMillis());
        
        String body = new ObjectMapper().writeValueAsString(error);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -48;
    }
}
```

## Security Monitoring

### Security Event Logging

```java
@Component
public class SecurityEventLogger implements GlobalFilter, Ordered {
    
    private static final Logger log = LoggerFactory.getLogger(SecurityEventLogger.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        return chain.filter(exchange)
                .doOnSuccess(result -> {
                    logSecurityEvent(request, "SUCCESS", null);
                })
                .doOnError(error -> {
                    logSecurityEvent(request, "ERROR", error.getMessage());
                });
    }
    
    private void logSecurityEvent(ServerHttpRequest request, String status, String error) {
        Map<String, Object> event = new HashMap<>();
        event.put("timestamp", System.currentTimeMillis());
        event.put("status", status);
        event.put("method", request.getMethod().name());
        event.put("path", request.getURI().getPath());
        event.put("clientIp", getClientIp(request));
        event.put("userAgent", request.getHeaders().getFirst("User-Agent"));
        event.put("error", error);
        
        log.info("Security event: {}", new ObjectMapper().writeValueAsString(event));
    }
    
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddress().getAddress().getHostAddress();
    }
    
    @Override
    public int getOrder() {
        return -47;
    }
}
```

## Best Practices

### Security Configuration

1. **Use HTTPS**: Always use HTTPS in production
2. **Validate tokens**: Always validate JWT tokens
3. **Implement RBAC**: Use role-based access control
4. **Rate limiting**: Implement rate limiting for security endpoints
5. **Log security events**: Log all security-related events

### Token Management

1. **Short expiration**: Use short token expiration times
2. **Refresh tokens**: Implement refresh token mechanism
3. **Token revocation**: Implement token revocation
4. **Secure storage**: Store tokens securely
5. **Regular rotation**: Rotate tokens regularly

### Access Control

1. **Principle of least privilege**: Grant minimum required permissions
2. **Regular audits**: Audit permissions regularly
3. **Role separation**: Separate roles by function
4. **Multi-factor authentication**: Use MFA where possible
5. **Session management**: Implement proper session management

## Troubleshooting

### Common Issues

#### Authentication Failures

```bash
# Check JWT configuration
curl http://localhost:8080/actuator/configprops | grep -i jwt

# Test JWT token
curl -H "Authorization: Bearer <token>" http://localhost:8080/actuator/health

# Check authentication logs
tail -f logs/tigateway.log | grep -i auth
```

#### Authorization Issues

```bash
# Check RBAC configuration
curl http://localhost:8080/actuator/configprops | grep -i rbac

# Test role-based access
curl -H "X-Roles: USER" http://localhost:8080/api/admin/users

# Check authorization logs
tail -f logs/tigateway.log | grep -i authz
```

#### Token Validation Issues

```bash
# Check JWT decoder
curl http://localhost:8080/actuator/health

# Validate JWT token
jwt-decode <token>

# Check JWT configuration
curl http://localhost:8080/actuator/configprops | grep -i jwt
```

### Debug Commands

```bash
# Check security configuration
curl http://localhost:8080/actuator/configprops | grep -i security

# Test authentication
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/users/123

# Test API key authentication
curl -H "X-API-Key: <key>" http://localhost:8080/api/users/123

# Check rate limiting
curl http://localhost:8080/actuator/metrics/redis.rate.limiter.requests
```

## Next Steps

After implementing authentication and authorization:

1. **[Security Best Practices](../security-best-practices.md)** - Comprehensive security guidelines
2. **[Monitoring Setup](../monitoring-and-metrics.md)** - Monitor security events
3. **[Troubleshooting Guide](../troubleshooting.md)** - Common security issues
4. **[Performance Tuning](../performance-tuning.md)** - Optimize security performance

---

**Ready to implement comprehensive security?** Check out our [Security Best Practices](../security-best-practices.md) guide for advanced security implementation.
