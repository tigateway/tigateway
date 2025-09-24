# 开发环境搭建

## 环境要求

### 基础环境
- **Java**: 11+ (推荐 OpenJDK 11)
- **Maven**: 3.6+
- **Docker**: 20.10+
- **Kubernetes**: 1.20+ (可选，用于集成测试)
- **Helm**: 3.0+ (可选，用于部署测试)

### 开发工具
- **IDE**: IntelliJ IDEA 或 Eclipse
- **Git**: 2.30+
- **Node.js**: 16+ (用于前端开发)

## 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/tigateway/tigateway.git
cd tigateway
```

### 2. 检查 Java 版本
```bash
java -version
# 应该显示 Java 11 或更高版本

mvn -version
# 确认 Maven 使用正确的 Java 版本
```

### 3. 构建项目
```bash
# 清理并编译
mvn clean compile

# 运行测试
mvn test

# 打包
mvn package -DskipTests
```

## 详细配置

### Java 环境配置

#### 设置 JAVA_HOME
```bash
# macOS (使用 Homebrew 安装的 OpenJDK)
export JAVA_HOME=/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home

# Linux
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk

# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-11
```

#### 验证 Java 配置
```bash
echo $JAVA_HOME
java -version
javac -version
```

### Maven 配置

#### settings.xml 配置
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/central</url>
    </mirror>
  </mirrors>
  
  <profiles>
    <profile>
      <id>jdk-11</id>
      <activation>
        <activeByDefault>true</activeByDefault>
        <jdk>11</jdk>
      </activation>
      <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <maven.compiler.compilerVersion>11</maven.compiler.compilerVersion>
      </properties>
    </profile>
  </profiles>
</settings>
```

### IDE 配置

#### IntelliJ IDEA 配置
1. **导入项目**
   - File → Open → 选择项目根目录
   - 选择 "Import project from external model" → Maven
   - 等待 Maven 导入完成

2. **Java SDK 配置**
   - File → Project Structure → Project
   - 设置 Project SDK 为 Java 11
   - 设置 Project language level 为 11

3. **Maven 配置**
   - File → Settings → Build → Build Tools → Maven
   - 设置 Maven home directory
   - 设置 User settings file 为你的 settings.xml

4. **代码格式化**
   - File → Settings → Editor → Code Style → Java
   - 导入项目中的 code-style.xml (如果有)

#### Eclipse 配置
1. **导入项目**
   - File → Import → Existing Maven Projects
   - 选择项目根目录
   - 等待 Maven 导入完成

2. **Java 配置**
   - Window → Preferences → Java → Installed JREs
   - 添加 Java 11 JRE
   - 设置为默认 JRE

### Docker 环境配置

#### 安装 Docker Desktop
```bash
# macOS (使用 Homebrew)
brew install --cask docker

# 启动 Docker Desktop
open /Applications/Docker.app
```

#### 验证 Docker 安装
```bash
docker --version
docker-compose --version
```

### Kubernetes 环境配置 (可选)

#### 本地 Kubernetes 集群
```bash
# 使用 minikube
brew install minikube
minikube start

# 使用 kind
brew install kind
kind create cluster

# 使用 Docker Desktop Kubernetes
# 在 Docker Desktop 设置中启用 Kubernetes
```

#### 验证 Kubernetes 配置
```bash
kubectl version --client
kubectl cluster-info
```

## 项目结构说明

```
tigateway/
├── pom.xml                          # 父 POM
├── README.md                        # 项目说明
├── docs/                           # 文档目录
├── ti-gateway-operator/            # Operator 模块
├── ti-gateway-kubernetes-extensions/ # 扩展模块
├── ti-gateway/                     # 主项目目录
│   ├── pom.xml                     # 子项目 POM
│   ├── ti-gateway-base/            # 基础模块
│   ├── ti-gateway-admin/           # 管理模块
│   └── ti-gateway-limit/           # 限流模块
├── ti-gateway-kubernetes/          # Kubernetes 集成模块
├── helm/                          # Helm Charts
│   ├── gateway/                   # Gateway Chart
│   └── tigateway-crds/            # CRDs Chart
└── webui/                         # 前端界面
```

## 开发工作流

### 1. 启动开发环境
```bash
# 启动主应用
mvn spring-boot:run -pl ti-gateway-kubernetes -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# 启动前端开发服务器 (在 webui 目录)
cd webui
npm install
npm run dev
```

### 2. 验证环境
```bash
# 检查主应用
curl http://localhost:8080/actuator/health

# 检查管理界面
curl http://localhost:8081/admin/health

# 检查监控端点
curl http://localhost:8090/actuator/health
```

### 3. 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -pl ti-gateway-base

# 运行集成测试
mvn verify -P integration-test
```

## 常见问题

### Java 版本问题
**问题**: `UnsupportedClassVersionError`
```bash
# 解决方案: 确保编译和运行时使用相同 Java 版本
mvn clean compile
mvn spring-boot:run -pl ti-gateway-kubernetes
```

### Maven 依赖问题
**问题**: 依赖下载失败
```bash
# 解决方案: 清理本地仓库并重新下载
mvn dependency:purge-local-repository
mvn clean install
```

### 端口冲突
**问题**: 端口被占用
```bash
# 解决方案: 查找并终止占用端口的进程
lsof -i :8080
kill -9 <PID>

# 或使用不同端口
mvn spring-boot:run -pl ti-gateway-kubernetes -Dspring-boot.run.arguments="--server.port=8082"
```

### Kubernetes 连接问题
**问题**: 无法连接 Kubernetes 集群
```bash
# 解决方案: 检查 kubectl 配置
kubectl config current-context
kubectl cluster-info

# 或禁用 Kubernetes 功能进行本地开发
# 在 application.yml 中设置:
# spring.kubernetes.discovery.enabled: false
# spring.kubernetes.config.enabled: false
```

## 开发工具推荐

### 代码质量工具
- **Checkstyle**: 代码风格检查
- **SpotBugs**: 静态代码分析
- **PMD**: 代码质量分析
- **JaCoCo**: 代码覆盖率

### 调试工具
- **Spring Boot DevTools**: 热重载
- **JRebel**: 热部署
- **Arthas**: Java 诊断工具

### 监控工具
- **Micrometer**: 指标收集
- **Prometheus**: 监控系统
- **Grafana**: 可视化面板

## 下一步

环境搭建完成后，建议阅读以下文档：
- [代码规范](./coding-standards.md)
- [测试指南](./testing.md)
- [快速开始](../examples/quick-start.md)

---

**相关文档**:
- [系统架构](../architecture/system-architecture.md)
- [模块设计](../architecture/module-design.md)
- [调试指南](./debugging.md)
