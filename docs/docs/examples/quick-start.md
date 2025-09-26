# TiGateway 快速开始

本指南将帮助您在 5 分钟内快速搭建和运行 TiGateway。

## 前提条件

- Java 11+
- Maven 3.6+
- Docker (可选)
- Kubernetes 集群 (可选)

## 方式一：本地开发模式

### 1. 克隆项目
```bash
git clone https://github.com/tigateway/tigateway.git
cd tigateway
```

### 2. 构建项目
```bash
mvn clean compile
```

### 3. 启动应用
```bash
mvn spring-boot:run -pl ti-gateway-kubernetes -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### 4. 验证运行
```bash
# 检查主应用
curl http://localhost:8080/actuator/health

# 检查管理界面
curl http://localhost:8081/admin/health

# 检查监控端点
curl http://localhost:8090/actuator/health
```

## 方式二：Docker 模式

### 1. 构建镜像
```bash
docker build -t tigateway:latest ./ti-gateway-kubernetes
```

### 2. 运行容器
```bash
docker run -d \
  --name tigateway \
  -p 8080:8080 \
  -p 8081:8081 \
  -p 8090:8090 \
  tigateway:latest
```

### 3. 验证运行
```bash
# 检查容器状态
docker ps

# 检查应用健康状态
curl http://localhost:8080/actuator/health
```

## 方式三：Kubernetes 模式

### 1. 安装 CRDs
```bash
# 使用 Helm
helm install tigateway-crds ./helm/tigateway-crds

# 或直接应用 YAML
kubectl apply -f helm/tigateway-crds/templates/
```

### 2. 部署 Gateway
```bash
# 使用 Helm
helm install tigateway ./helm/gateway

# 或直接应用 YAML
kubectl apply -f helm/gateway/
```

### 3. 验证部署
```bash
# 检查 Pod 状态
kubectl get pods -l app=tigateway

# 检查服务状态
kubectl get svc tigateway

# 端口转发访问
kubectl port-forward svc/tigateway 8080:8080
kubectl port-forward svc/tigateway 8081:8081
kubectl port-forward svc/tigateway 8090:8090
```

## 基础配置示例

### 1. 创建测试路由

#### 使用配置文件
在 `application.yml` 中添加：
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: test-route
          uri: https://httpbin.org
          predicates:
            - Path=/test/**
          filters:
            - StripPrefix=1
```

#### 使用 API 创建
```bash
curl -X POST http://localhost:8080/actuator/gateway/routes/test-route \
  -H "Content-Type: application/json" \
  -d '{
    "uri": "https://httpbin.org",
    "predicates": [
      {
        "name": "Path",
        "args": {
          "pattern": "/test/**"
        }
      }
    ],
    "filters": [
      {
        "name": "StripPrefix",
        "args": {
          "parts": 1
        }
      }
    ]
  }'
```

### 2. 测试路由
```bash
# 测试路由是否工作
curl http://localhost:8080/test/get

# 应该返回 httpbin.org 的响应
```

### 3. 查看路由状态
```bash
# 获取所有路由
curl http://localhost:8080/actuator/gateway/routes

# 获取特定路由
curl http://localhost:8080/actuator/gateway/routes/test-route
```

## 管理界面使用

### 1. 访问管理界面
打开浏览器访问：`http://localhost:8081/admin`

### 2. 查看系统状态
```bash
curl http://localhost:8081/admin/api/system/status
```

### 3. 管理应用配置
```bash
# 创建应用
curl -X POST http://localhost:8081/admin/api/apps \
  -H "Content-Type: application/json" \
  -d '{
    "name": "demo-app",
    "description": "演示应用",
    "version": "1.0.0"
  }'

# 获取应用列表
curl http://localhost:8081/admin/api/apps
```

## 监控和运维

### 1. 健康检查
```bash
# 应用健康状态
curl http://localhost:8090/actuator/health

# 就绪状态
curl http://localhost:8090/actuator/health/readiness

# 存活状态
curl http://localhost:8090/actuator/health/liveness
```

### 2. 系统指标
```bash
# 获取所有指标
curl http://localhost:8090/actuator/metrics

# 获取 JVM 内存使用
curl http://localhost:8090/actuator/metrics/jvm.memory.used

# 获取 HTTP 请求数
curl http://localhost:8090/actuator/metrics/http.server.requests
```

### 3. 配置管理
```bash
# 查看配置属性
curl http://localhost:8090/actuator/configprops

# 查看环境变量
curl http://localhost:8090/actuator/env

# 查看日志配置
curl http://localhost:8090/actuator/loggers
```

## 常见问题解决

### 1. 端口冲突
```bash
# 查找占用端口的进程
lsof -i :8080
lsof -i :8081
lsof -i :8090

# 终止进程
kill -9 <PID>
```

### 2. Java 版本问题
```bash
# 检查 Java 版本
java -version

# 确保使用 Java 11+
export JAVA_HOME=/path/to/java11
```

### 3. Maven 依赖问题
```bash
# 清理并重新下载依赖
mvn clean
mvn dependency:purge-local-repository
mvn compile
```

### 4. Kubernetes 连接问题
如果不在 Kubernetes 环境中运行，可以禁用 Kubernetes 功能：
```yaml
spring:
  kubernetes:
    discovery:
      enabled: false
    config:
      enabled: false
```

## 下一步

快速开始完成后，建议：

1. **学习基础配置**: 查看 [基础配置示例](./basic-config.md)
2. **了解高级功能**: 阅读 [高级配置示例](./advanced-config.md)
3. **部署到生产环境**: 参考 [Kubernetes 部署](../deployment/kubernetes.md)
4. **监控和运维**: 查看 [监控运维指南](../deployment/monitoring.md)

## 获取帮助

- 📖 查看 [完整文档](https://github.com/tigateway/tigateway/blob/main/README.md)
- 🐛 提交 [Issue](https://github.com/tigateway/tigateway/issues)
- 💬 参与 [讨论](https://github.com/tigateway/tigateway/discussions)

---

**恭喜！** 您已经成功运行了 TiGateway！🎉
