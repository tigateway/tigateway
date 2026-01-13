# TiGateway Kubernetes 单元测试

本目录包含 `ti-gateway-kubernetes` 模块的单元测试。

## 测试统计

- **测试文件数**: 35
- **测试方法数**: 216+
- **覆盖模块**: 核心工具类、Gateway Filters、安全配置、JWT 处理、限流、CORS、错误处理等

## 测试结构

```
src/test/java/ti/gateway/kubernetes/
├── apikeys/
│   └── ApiKeyGlobalFilterTest.java          # API Key 全局过滤器测试
├── basicauth/
│   └── BasicAuthGatewayFilterFactoryTest.java  # 基础认证过滤器测试
├── circuitbreaker/
│   └── CircuitBreakerGatewayFilterFactoryTest.java  # 熔断器过滤器测试
├── core/
│   ├── KeyValueTest.java                    # KeyValue 实体类测试
│   ├── KeyValueConverterTest.java           # KeyValue 转换器测试
│   └── KeyValueConfigTest.java              # KeyValue 配置测试
├── cors/
│   ├── CorsGatewayFilterFactoryTest.java    # CORS 过滤器测试
│   └── CorsGatewayFilterConfigTest.java     # CORS 配置测试
├── error/
│   └── CustomErrorAttributesTest.java       # 自定义错误属性测试
├── header/
│   ├── AddRequestHeadersIfNotPresentGatewayFilterFactoryTest.java  # 添加请求头过滤器测试
│   ├── StoreHeaderGatewayFilterFactoryTest.java  # 存储头过滤器测试
│   └── AllowedRequestHeadersCountGatewayFilterFactoryTest.java  # 请求头计数验证过滤器测试
├── ip/
│   └── StoreIpAddressGatewayFilterFactoryTest.java  # 存储 IP 地址过滤器测试
├── parameter/
│   └── AllowedRequestQueryParamsCountGatewayFilterFactoryTest.java  # 查询参数计数验证过滤器测试
├── cookie/
│   └── AllowedRequestCookieCountGatewayFilterFactoryTest.java  # Cookie 计数验证过滤器测试
├── jwt/
│   ├── JwtHelperTest.java                   # JWT 工具类测试
│   ├── KeyParserTest.java                   # JWT 密钥解析器测试
│   ├── TokenVerifierTest.java               # Token 验证器测试
│   ├── JWTClaimHeaderGatewayFilterFactoryTest.java  # JWT Claim 头过滤器测试
│   └── JWTClaimRoutePredicateFactoryTest.java  # JWT Claim 路由谓词测试
├── ratelimit/
│   ├── DefaultRateLimiterTest.java          # 默认限流器测试
│   ├── Bucket4JRequestCounterTest.java      # Bucket4J 请求计数器测试
│   ├── RateLimitGatewayFilterFactoryTest.java  # 限流过滤器工厂测试
│   └── RateLimiterPropertiesTest.java        # 限流器属性测试
└── security/
    ├── CommonSecurityTest.java              # 通用安全配置测试
    ├── SecurityGatewayFilterTest.java       # 安全网关过滤器测试
    ├── RolesExtractorTest.java              # 角色提取器测试
    └── IdTokenRelayGatewayFilterFactoryTest.java  # ID Token 中继过滤器测试
```

## 运行测试

### 使用 Maven

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=JwtHelperTest

# 运行特定包下的所有测试
mvn test -Dtest=ti.gateway.kubernetes.jwt.*

# 生成测试覆盖率报告（需要配置 JaCoCo）
mvn test jacoco:report
```

### 使用 IDE

- **IntelliJ IDEA**: 右键点击测试类或测试方法，选择 "Run 'TestName'"
- **Eclipse**: 右键点击测试类，选择 "Run As" -> "JUnit Test"

## 测试覆盖范围

### 已覆盖的类

1. **CommonSecurity** - 通用安全配置
   - 测试安全配置的基本功能

2. **JwtHelper** - JWT 工具类
   - 测试 Header 值清理
   - 测试 Claim 提取（String, List, Array）
   - 测试类型转换

3. **KeyValue** - 键值对实体
   - 测试构造函数和 getter 方法
   - 测试边界情况（null, 空字符串）

4. **KeyValueConverter** - 键值对转换器
   - 测试有效格式转换
   - 测试异常情况处理

5. **SecurityGatewayFilter** - 安全网关过滤器
   - 测试过滤器链执行
   - 测试错误处理

6. **ApiKeyGlobalFilter** - API Key 全局过滤器
   - 测试有效 API Key 验证
   - 测试无效 API Key 拒绝
   - 测试缺失 API Key 处理

7. **CustomErrorAttributes** - 自定义错误属性
   - 测试错误状态码提取
   - 测试堆栈跟踪包含/排除
   - 测试异常类型处理

## 测试最佳实践

1. **命名规范**: 测试类名以 `Test` 结尾，测试方法名描述性强
2. **独立性**: 每个测试方法应该独立，不依赖其他测试的执行顺序
3. **Mock 使用**: 使用 Mockito 模拟外部依赖
4. **断言清晰**: 使用明确的断言，提供有意义的错误消息
5. **边界测试**: 测试正常情况、边界情况和异常情况

## 已添加的测试（新增）

8. **KeyParser** - JWT 密钥解析器
   - 测试 RSA 算法解析
   - 测试 HS256/HS384/HS512 算法解析
   - 测试无效算法异常处理

9. **TokenVerifier** - Token 验证器
   - 测试 RSA 公钥验证
   - 测试 MAC 密钥验证
   - 测试不支持的密钥类型异常
   - 测试空/无效 token 处理

10. **RolesExtractor** - 角色提取器
    - 测试从 Claims Map 提取角色
    - 测试简单角色和列表角色
    - 测试嵌套路径角色提取
    - 测试空值和边界情况

11. **CorsGatewayFilterFactory** - CORS 过滤器
    - 测试过滤器创建和执行
    - 测试配置类和方法

12. **DefaultRateLimiter** - 默认限流器
    - 测试允许/拒绝请求
    - 测试缺失密钥处理
    - 测试响应头设置

13. **BasicAuthGatewayFilterFactory** - 基础认证过滤器
    - 测试过滤器创建和应用
    - 测试 Authorization 头添加
    - 测试配置类和方法

14. **JWTClaimHeaderGatewayFilterFactory** - JWT Claim 头过滤器
    - 测试过滤器创建和执行
    - 测试从 Session Token 提取 Claim
    - 测试从 Authorization Header 提取 Claim
    - 测试配置验证

15. **CircuitBreakerGatewayFilterFactory** - 熔断器过滤器
    - 测试自定义器创建
    - 测试配置类和方法
    - 测试 Resilience4J 扩展配置

16. **KeyValueConfig** - 键值对配置
    - 测试数组设置和获取
    - 测试空值和边界情况

17. **CorsGatewayFilterConfig** - CORS 配置
    - 测试各种 CORS 设置（allowCredentials, allowedHeaders, allowedMethods 等）
    - 测试多配置组合
    - 测试配置解析

18. **JWTClaimRoutePredicateFactory** - JWT Claim 路由谓词
    - 测试谓词创建和应用
    - 测试配置类和方法
    - 测试空头处理

19. **IdTokenRelayGatewayFilterFactory** - ID Token 中继过滤器
    - 测试过滤器创建和执行
    - 测试无认证情况处理

20. **AddRequestHeadersIfNotPresentGatewayFilterFactory** - 添加请求头过滤器
    - 测试缺失头添加
    - 测试已存在头不覆盖
    - 测试多个头处理

21. **StoreIpAddressGatewayFilterFactory** - 存储 IP 地址过滤器
    - 测试 IP 地址提取和存储
    - 测试配置类和方法

22. **Bucket4JRequestCounter** - Bucket4J 请求计数器
    - 测试令牌消费
    - 测试多次消费
    - 测试响应结构

23. **RateLimitGatewayFilterFactory** - 限流过滤器工厂
    - 测试过滤器创建和应用
    - 测试 Claim、Header、IP 密钥位置解析
    - 测试配置类和方法

24. **RateLimiterProperties** - 限流器属性
    - 测试默认值
    - 测试 getter/setter 方法
    - 测试密钥位置解析（claim, header, IPs）
    - 测试边界情况

25. **StoreHeaderGatewayFilterFactory** - 存储头过滤器
    - 测试头提取和存储
    - 测试多个追踪头处理
    - 测试配置类和方法

26. **RolesSecurityGatewayFilter** - 角色安全过滤器
    - 测试过滤器执行
    - 测试错误处理

27. **AllowedRequestQueryParamsCountGatewayFilterFactory** - 查询参数计数验证过滤器
    - 测试允许的参数数量
    - 测试超出限制的处理
    - 测试错误响应

28. **AllowedRequestHeadersCountGatewayFilterFactory** - 请求头计数验证过滤器
    - 测试允许的头数量
    - 测试超出限制的处理
    - 测试错误响应

29. **AllowedRequestCookieCountGatewayFilterFactory** - Cookie 计数验证过滤器
    - 测试允许的 Cookie 数量
    - 测试超出限制的处理
    - 测试错误响应

30. **IngressProperties** - Ingress 配置属性
    - 测试默认值
    - 测试所有 getter/setter 方法
    - 测试配置组合

31. **KeyValueGatewayFilterFactory** - 键值对过滤器工厂基类
    - 测试快捷类型
    - 测试字段顺序
    - 测试配置类创建

32. **RewriteAllResponseHeadersGatewayFilterFactory** - 重写所有响应头过滤器
    - 测试过滤器创建和应用
    - 测试配置类和方法

33. **RewriteResponseBodyGatewayFilterFactory** - 重写响应体过滤器
    - 测试过滤器创建和应用
    - 测试空体和空键值处理
    - 测试配置类和方法

34. **ScgFallbackHeadersGatewayFilterFactory** - SCG 回退头过滤器
    - 测试快捷字段顺序
    - 测试名称方法
    - 测试配置类

35. **HazelcastBucket4JRequestCounterFactory** - Hazelcast Bucket4J 请求计数器工厂
    - 测试请求计数器创建
    - 测试不同限制和持续时间
    - 测试边界情况

## 待添加的测试

以下类还需要添加单元测试：

- [ ] `IngressRouteDefinitionLocator` - Ingress 路由定义定位器（需要 Kubernetes 客户端 Mock）
- [ ] `IngressWatcher` - Ingress 监听器（需要 Kubernetes Watch API Mock）
- [ ] `RewriteJsonAttributesResponseBodyGatewayFilterFactory` - 重写 JSON 属性响应体过滤器
- [ ] `RemoveJsonAttributesResponseBodyGatewayFilterFactory` - 移除 JSON 属性响应体过滤器
- [ ] `SessionConfiguration` - 会话配置
- [ ] `HazelcastReactiveSessionRepository` - Hazelcast 会话仓库

## 测试依赖

测试使用以下主要依赖：

- **JUnit 5** - 测试框架
- **Mockito** - Mock 框架
- **Spring Boot Test** - Spring Boot 测试支持
- **Reactor Test** - Reactor 响应式流测试
- **Spring Security Test** - Spring Security 测试支持

## 持续集成

在 CI/CD 流程中，测试会自动运行：

```bash
mvn clean test
```

如果测试失败，构建将中断。
