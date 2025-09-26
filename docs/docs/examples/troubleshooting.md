# TiGateway 故障排除指南

## 概述

本文档提供了 TiGateway 常见问题的诊断和解决方案，帮助您快速定位和解决系统运行中遇到的问题。

## 常见问题分类

### 🔧 启动问题
- 应用启动失败
- 端口冲突
- 依赖注入错误
- 配置错误

### 🌐 网络问题
- 服务无法访问
- 路由不生效
- 负载均衡问题
- 超时问题

### 🔐 安全问题
- 认证失败
- 授权错误
- 证书问题
- 权限不足

### 📊 性能问题
- 响应缓慢
- 内存泄漏
- CPU 使用率高
- 连接池耗尽

### 🗄️ 数据问题
- 配置丢失
- 数据不一致
- 缓存问题
- 存储连接失败

## 启动问题诊断

### 1. 应用启动失败

#### 问题现象
```bash
Error: A JNI error has occurred, please check your installation and try again
Exception in thread "main" java.lang.UnsupportedClassVersionError
```

#### 诊断步骤
```bash
# 1. 检查 Java 版本
java -version
mvn -version

# 2. 检查 JAVA_HOME 设置
echo $JAVA_HOME

# 3. 清理并重新编译
mvn clean compile

# 4. 检查依赖冲突
mvn dependency:tree
```

#### 解决方案
```bash
# 设置正确的 Java 版本
export JAVA_HOME=/path/to/java11
export PATH=$JAVA_HOME/bin:$PATH

# 重新编译项目
mvn clean compile -DskipTests

# 启动应用
mvn spring-boot:run -pl ti-gateway-kubernetes
```

### 2. 端口冲突

#### 问题现象
```bash
reactor.netty.ChannelBindException: Failed to bind on [0.0.0.0:8080]
```

#### 诊断步骤
```bash
# 1. 检查端口占用
lsof -i :8080
lsof -i :8081
lsof -i :8090

# 2. 检查进程
ps aux | grep java
ps aux | grep tigateway

# 3. 检查网络连接
netstat -tulpn | grep :8080
```

#### 解决方案
```bash
# 方案1: 终止占用端口的进程
kill -9 <PID>

# 方案2: 使用不同端口启动
mvn spring-boot:run -pl ti-gateway-kubernetes \
  -Dspring-boot.run.arguments="--server.port=8082"

# 方案3: 修改配置文件
# 在 application.yml 中修改端口配置
server:
  port: 8082
```

### 3. 依赖注入错误

#### 问题现象
```bash
No qualifying bean of type 'xxx' available: expected single matching bean but found 2
```

#### 诊断步骤
```bash
# 1. 检查 Bean 定义
grep -r "@Bean" src/main/java/
grep -r "@Component" src/main/java/
grep -r "@Service" src/main/java/

# 2. 检查自动配置
grep -r "@EnableAutoConfiguration" src/main/java/
grep -r "spring.factories" src/main/resources/
```

#### 解决方案
```java
// 方案1: 使用 @Primary 注解
@Bean
@Primary
public MyService myService() {
    return new MyServiceImpl();
}

// 方案2: 使用 @Qualifier 注解
@Autowired
@Qualifier("myService")
private MyService myService;

// 方案3: 排除冲突的自动配置
@SpringBootApplication(exclude = {
    ConflictingAutoConfiguration.class
})
public class GatewayApplication {
    // ...
}
```

## 网络问题诊断

### 1. 服务无法访问

#### 问题现象
```bash
curl: (7) Failed to connect to localhost:8080: Connection refused
```

#### 诊断步骤
```bash
# 1. 检查服务状态
curl http://localhost:8080/actuator/health
curl http://localhost:8081/admin/health
curl http://localhost:8090/actuator/health

# 2. 检查应用日志
tail -f logs/tigateway.log

# 3. 检查网络连接
telnet localhost 8080
```

#### 解决方案
```bash
# 1. 重启应用
mvn spring-boot:run -pl ti-gateway-kubernetes

# 2. 检查防火墙设置
sudo ufw status
sudo iptables -L

# 3. 检查 Kubernetes 服务
kubectl get svc -n tigateway
kubectl describe svc tigateway -n tigateway
```

### 2. 路由不生效

#### 问题现象
```bash
curl http://localhost:8080/api/users/123
# 返回 404 Not Found
```

#### 诊断步骤
```bash
# 1. 检查路由配置
curl http://localhost:8080/actuator/gateway/routes

# 2. 检查路由刷新
curl -X POST http://localhost:8080/actuator/gateway/refresh

# 3. 检查服务发现
curl http://localhost:8080/actuator/gateway/globalfilters
```

#### 解决方案
```yaml
# 1. 检查路由配置
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2

# 2. 手动刷新路由
curl -X POST http://localhost:8080/actuator/gateway/refresh

# 3. 检查服务注册
kubectl get svc
kubectl get endpoints
```

### 3. 负载均衡问题

#### 问题现象
```bash
# 请求总是路由到同一个实例
curl http://localhost:8080/api/test
# 多次请求都返回相同的响应
```

#### 诊断步骤
```bash
# 1. 检查服务实例
kubectl get pods -l app=backend-service
kubectl get endpoints backend-service

# 2. 检查负载均衡配置
curl http://localhost:8080/actuator/gateway/routes

# 3. 检查服务健康状态
kubectl get pods -o wide
```

#### 解决方案
```yaml
# 1. 配置负载均衡策略
spring:
  cloud:
    gateway:
      routes:
        - id: backend-service
          uri: lb://backend-service
          predicates:
            - Path=/api/**
          filters:
            - StripPrefix=1
            - name: LoadBalancer
              args:
                loadBalancerType: ROUND_ROBIN

# 2. 检查服务实例健康状态
livenessProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /ready
    port: 8080
  initialDelaySeconds: 5
  periodSeconds: 5
```

## 安全问题诊断

### 1. 认证失败

#### 问题现象
```bash
curl http://localhost:8080/api/secure
# 返回 401 Unauthorized
```

#### 诊断步骤
```bash
# 1. 检查认证配置
curl http://localhost:8090/actuator/configprops | grep -i auth

# 2. 检查 Token 格式
echo "Bearer your-token" | base64

# 3. 检查认证日志
grep -i "authentication" logs/tigateway.log
```

#### 解决方案
```bash
# 1. 使用正确的 Token 格式
curl -H "Authorization: Bearer your-jwt-token" \
  http://localhost:8080/api/secure

# 2. 检查 JWT 配置
spring:
  security:
    oauth2:
      client:
        provider:
          sso:
            issuer-uri: ${SSO_ISSUER_URI}

# 3. 验证 Token 有效性
curl -H "Authorization: Bearer your-token" \
  http://localhost:8080/actuator/health
```

### 2. 授权错误

#### 问题现象
```bash
curl -H "Authorization: Bearer valid-token" \
  http://localhost:8080/api/admin
# 返回 403 Forbidden
```

#### 诊断步骤
```bash
# 1. 检查用户权限
curl -H "Authorization: Bearer token" \
  http://localhost:8080/actuator/info

# 2. 检查角色配置
grep -r "roles" src/main/resources/

# 3. 检查权限日志
grep -i "authorization" logs/tigateway.log
```

#### 解决方案
```yaml
# 1. 配置正确的角色映射
sso:
  roles-attribute-name: roles
  groups-attribute-name: groups

# 2. 检查路由权限配置
spring:
  cloud:
    gateway:
      routes:
        - id: admin-route
          uri: lb://admin-service
          predicates:
            - Path=/api/admin/**
          filters:
            - StripPrefix=2
            - name: AuthorizationFilter
              args:
                required-roles: ["ADMIN", "SUPER_ADMIN"]
```

### 3. 证书问题

#### 问题现象
```bash
SSL handshake failed: PKIX path building failed
```

#### 诊断步骤
```bash
# 1. 检查证书有效性
openssl x509 -in certificate.crt -text -noout

# 2. 检查证书链
openssl verify -CAfile ca.crt certificate.crt

# 3. 检查 TLS 配置
curl -v https://localhost:8080/actuator/health
```

#### 解决方案
```yaml
# 1. 配置正确的证书
apiVersion: v1
kind: Secret
metadata:
  name: tigateway-tls
type: kubernetes.io/tls
data:
  tls.crt: <base64-encoded-cert>
  tls.key: <base64-encoded-key>

# 2. 配置 TLS 设置
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: password
    key-store-type: PKCS12
```

## 性能问题诊断

### 1. 响应缓慢

#### 问题现象
```bash
# 请求响应时间超过 5 秒
time curl http://localhost:8080/api/slow
```

#### 诊断步骤
```bash
# 1. 检查系统资源
top
htop
free -h
df -h

# 2. 检查应用指标
curl http://localhost:8090/actuator/metrics
curl http://localhost:8090/actuator/metrics/jvm.memory.used
curl http://localhost:8090/actuator/metrics/http.server.requests

# 3. 检查慢查询日志
grep -i "slow" logs/tigateway.log
```

#### 解决方案
```yaml
# 1. 优化连接池配置
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 1000
          max-idle-time: 30s
          max-life-time: 60s
        connect-timeout: 5s
        response-timeout: 30s

# 2. 启用缓存
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000

# 3. 配置熔断器
spring:
  cloud:
    gateway:
      routes:
        - id: backend-route
          uri: lb://backend-service
          filters:
            - name: CircuitBreaker
              args:
                name: backend-circuit
                fallbackUri: forward:/fallback
```

### 2. 内存泄漏

#### 问题现象
```bash
# 内存使用持续增长
curl http://localhost:8090/actuator/metrics/jvm.memory.used
```

#### 诊断步骤
```bash
# 1. 检查内存使用
curl http://localhost:8090/actuator/metrics/jvm.memory.used
curl http://localhost:8090/actuator/metrics/jvm.memory.max

# 2. 生成堆转储
jcmd <pid> GC.run_finalization
jcmd <pid> VM.gc
jmap -dump:format=b,file=heap.hprof <pid>

# 3. 检查 GC 日志
grep -i "gc" logs/tigateway.log
```

#### 解决方案
```yaml
# 1. 调整 JVM 参数
JAVA_OPTS: "-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 2. 配置内存监控
management:
  endpoints:
    web:
      exposure:
        include: metrics,health,heapdump
  metrics:
    export:
      prometheus:
        enabled: true

# 3. 优化缓存配置
spring:
  cache:
    redis:
      time-to-live: 300000
      cache-null-values: false
```

### 3. CPU 使用率高

#### 问题现象
```bash
# CPU 使用率持续超过 80%
top -p <pid>
```

#### 诊断步骤
```bash
# 1. 检查 CPU 使用
top -p <pid>
htop

# 2. 生成线程转储
jstack <pid> > thread-dump.txt

# 3. 检查热点方法
jcmd <pid> JFR.start duration=60s filename=profile.jfr
```

#### 解决方案
```yaml
# 1. 优化线程池配置
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          type: elastic
          max-connections: 500

# 2. 启用异步处理
@Async
public CompletableFuture<Void> processAsync() {
    // 异步处理逻辑
}

# 3. 配置限流
spring:
  cloud:
    gateway:
      routes:
        - id: rate-limited-route
          uri: lb://backend-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
```

## 数据问题诊断

### 1. 配置丢失

#### 问题现象
```bash
# 应用启动后配置不生效
curl http://localhost:8080/actuator/gateway/routes
# 返回空列表
```

#### 诊断步骤
```bash
# 1. 检查 ConfigMap
kubectl get configmap tigateway-config -o yaml

# 2. 检查配置加载
curl http://localhost:8090/actuator/configprops

# 3. 检查配置变更日志
grep -i "config" logs/tigateway.log
```

#### 解决方案
```bash
# 1. 重新创建 ConfigMap
kubectl apply -f configmap.yaml

# 2. 重启应用
kubectl rollout restart deployment/tigateway

# 3. 手动刷新配置
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

### 2. 数据不一致

#### 问题现象
```bash
# 不同实例返回不同的数据
curl http://localhost:8080/api/data
curl http://localhost:8081/api/data
# 返回不同的结果
```

#### 诊断步骤
```bash
# 1. 检查数据源
kubectl get configmap
kubectl get secrets

# 2. 检查缓存状态
curl http://localhost:8090/actuator/caches

# 3. 检查数据同步日志
grep -i "sync" logs/tigateway.log
```

#### 解决方案
```yaml
# 1. 配置数据同步
tigateway:
  cluster:
    sync:
      config:
        enabled: true
        interval: 30s
        strategy: PUSH

# 2. 清除缓存
curl -X DELETE http://localhost:8090/actuator/caches/routes

# 3. 强制数据刷新
curl -X POST http://localhost:8080/actuator/refresh
```

## 监控和诊断工具

### 1. 健康检查端点
```bash
# 应用健康状态
curl http://localhost:8090/actuator/health

# 详细健康信息
curl http://localhost:8090/actuator/health/detailed

# 就绪状态
curl http://localhost:8090/actuator/health/readiness

# 存活状态
curl http://localhost:8090/actuator/health/liveness
```

### 2. 指标监控
```bash
# 所有指标
curl http://localhost:8090/actuator/metrics

# JVM 内存使用
curl http://localhost:8090/actuator/metrics/jvm.memory.used

# HTTP 请求数
curl http://localhost:8090/actuator/metrics/http.server.requests

# 系统 CPU 使用
curl http://localhost:8090/actuator/metrics/system.cpu.usage
```

### 3. 配置信息
```bash
# 配置属性
curl http://localhost:8090/actuator/configprops

# 环境变量
curl http://localhost:8090/actuator/env

# 应用信息
curl http://localhost:8090/actuator/info

# 日志配置
curl http://localhost:8090/actuator/loggers
```

## 日志分析

### 1. 关键日志模式
```bash
# 错误日志
grep -i "error\|exception\|failed" logs/tigateway.log

# 认证日志
grep -i "authentication\|authorization" logs/tigateway.log

# 路由日志
grep -i "route\|gateway" logs/tigateway.log

# 性能日志
grep -i "slow\|timeout\|latency" logs/tigateway.log
```

### 2. 日志级别调整
```bash
# 动态调整日志级别
curl -X POST http://localhost:8090/actuator/loggers/ti.gateway \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# 查看当前日志级别
curl http://localhost:8090/actuator/loggers/ti.gateway
```

## 常见问题 FAQ

### Q1: 应用启动后无法访问管理界面
**A**: 检查以下配置：
- 确认 Admin 服务已启用：`admin.server.enabled=true`
- 检查端口配置：`admin.server.port=8081`
- 验证防火墙设置和端口占用

### Q2: 路由配置不生效
**A**: 按以下步骤排查：
- 检查路由配置格式是否正确
- 手动刷新路由：`POST /actuator/gateway/refresh`
- 查看路由列表：`GET /actuator/gateway/routes`
- 检查服务发现是否正常

### Q3: 认证失败问题
**A**: 检查以下项目：
- JWT Token 格式是否正确
- Token 是否过期
- 认证服务是否可访问
- 证书配置是否正确

### Q4: 性能问题
**A**: 优化建议：
- 调整连接池配置
- 启用缓存机制
- 配置熔断器
- 监控系统资源使用

### Q5: Kubernetes 部署问题
**A**: 检查以下配置：
- RBAC 权限设置
- 网络策略配置
- 服务发现配置
- ConfigMap 和 Secret 配置

---

**相关文档**:
- [快速开始](./quick-start.md)
- [基础配置示例](./basic-config.md)
- [高级配置示例](./advanced-config.md)
- [Kubernetes 部署](../deployment/kubernetes.md)
