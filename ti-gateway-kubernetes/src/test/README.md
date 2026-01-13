# TiGateway Kubernetes 单元测试

本目录包含 `ti-gateway-kubernetes` 模块的单元测试。

## 测试结构

```
src/test/java/ti/gateway/kubernetes/
├── apikeys/
│   └── ApiKeyGlobalFilterTest.java          # API Key 全局过滤器测试
├── core/
│   ├── KeyValueTest.java                    # KeyValue 实体类测试
│   └── KeyValueConverterTest.java           # KeyValue 转换器测试
├── error/
│   └── CustomErrorAttributesTest.java       # 自定义错误属性测试
├── jwt/
│   └── JwtHelperTest.java                   # JWT 工具类测试
└── security/
    ├── CommonSecurityTest.java              # 通用安全配置测试
    └── SecurityGatewayFilterTest.java       # 安全网关过滤器测试
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

## 待添加的测试

以下类还需要添加单元测试：

- [ ] `KeyParser` - JWT 密钥解析器
- [ ] `RolesExtractor` - 角色提取器
- [ ] `TokenVerifier` - Token 验证器
- [ ] `IngressRouteDefinitionLocator` - Ingress 路由定义定位器
- [ ] `IngressWatcher` - Ingress 监听器
- [ ] `RateLimiter` 相关类
- [ ] `CircuitBreakerGatewayFilterFactory` - 熔断器过滤器
- [ ] `CorsGatewayFilterFactory` - CORS 过滤器

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
