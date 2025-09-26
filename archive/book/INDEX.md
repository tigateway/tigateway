# TiGateway 用户指南 - 目录索引

## 快速导航

### 📚 第一部分：入门指南
- [1. TiGateway 简介](01-introduction.md) - 了解 TiGateway 的基本概念和特性
- [2. 快速开始](02-quick-start.md) - 快速部署和使用 TiGateway
- [3. 安装和部署](03-installation.md) - 详细的安装和部署指南

### 🏗️ 第二部分：核心概念
- [4. 架构概述](04-architecture.md) - 深入了解 TiGateway 的架构设计
- [5. 路由和谓词](05-routes-and-predicates.md) - 学习如何配置路由和谓词
- [6. 过滤器](06-filters.md) - 学习如何使用过滤器
- [7. 全局过滤器](07-global-filters.md) - 了解全局过滤器的使用

### ☸️ 第三部分：Kubernetes 集成
- [8. Kubernetes 原生特性](08-kubernetes-native.md) - 学习 Kubernetes 集成功能
- [9. CRD 资源管理](09-crd-resources.md) - 了解自定义资源定义
- [10. Ingress 集成](10-ingress-integration.md) - 学习 Ingress 集成
- [11. 服务发现](11-service-discovery.md) - 了解服务发现机制

### ⚙️ 第四部分：配置管理
- [12. 配置概述](12-configuration.md) - 了解配置系统
- [13. ConfigMap 存储](13-configmap-storage.md) - 学习 ConfigMap 存储
- [14. 动态配置更新](14-dynamic-configuration.md) - 了解配置热更新
- [15. 配置验证](15-configuration-validation.md) - 学习配置验证

### 🚀 第五部分：高级功能
- [16. 限流和熔断](16-rate-limiting-and-circuit-breaker.md) - 学习限流和熔断
- [17. 认证和授权](17-authentication-and-authorization.md) - 了解安全功能
- [18. 监控和指标](18-monitoring-and-metrics.md) - 学习监控功能
- [19. 日志和链路追踪](19-logging-and-tracing.md) - 了解可观测性

### 🖥️ 第六部分：管理界面
- [20. 管理界面概述](20-admin-interface.md) - 了解管理界面
- [21. Web UI 使用指南](21-web-ui-guide.md) - 学习 Web UI 使用
- [22. REST API 参考](22-rest-api-reference.md) - 了解 REST API

### 🔧 第七部分：运维指南
- [23. 健康检查](23-health-checks.md) - 学习健康检查
- [24. 故障排除](24-troubleshooting.md) - 了解故障排除
- [25. 性能调优](25-performance-tuning.md) - 学习性能优化
- [26. 安全最佳实践](26-security-best-practices.md) - 了解安全实践

### 👨‍💻 第八部分：开发指南
- [27. 自定义组件开发](27-custom-components.md) - 学习自定义开发
- [28. 扩展开发](28-extension-development.md) - 了解扩展开发
- [29. 测试指南](29-testing-guide.md) - 学习测试方法

### 📖 第九部分：参考文档
- [30. 配置属性参考](30-configuration-properties.md) - 配置属性大全
- [31. API 参考](31-api-reference.md) - API 接口文档
- [32. 常见问题](32-faq.md) - 常见问题解答

## 学习路径建议

### 🎯 初学者路径
1. [TiGateway 简介](01-introduction.md) - 了解基本概念
2. [快速开始](02-quick-start.md) - 快速体验
3. [架构概述](04-architecture.md) - 理解架构
4. [路由和谓词](05-routes-and-predicates.md) - 学习基础配置
5. [过滤器](06-filters.md) - 了解过滤器使用

### 🔧 运维人员路径
1. [安装和部署](03-installation.md) - 学习部署
2. [Kubernetes 原生特性](08-kubernetes-native.md) - 了解 K8s 集成
3. [配置管理](12-configuration.md) - 学习配置管理
4. [监控和指标](18-monitoring-and-metrics.md) - 了解监控
5. [故障排除](24-troubleshooting.md) - 学习故障处理

### 👨‍💻 开发人员路径
1. [架构概述](04-architecture.md) - 理解架构
2. [自定义组件开发](27-custom-components.md) - 学习自定义开发
3. [扩展开发](28-extension-development.md) - 了解扩展开发
4. [测试指南](29-testing-guide.md) - 学习测试方法
5. [API 参考](31-api-reference.md) - 了解 API 接口

### 🏢 企业用户路径
1. [TiGateway 简介](01-introduction.md) - 了解产品特性
2. [架构概述](04-architecture.md) - 理解技术架构
3. [认证和授权](17-authentication-and-authorization.md) - 了解安全功能
4. [安全最佳实践](26-security-best-practices.md) - 学习安全实践
5. [性能调优](25-performance-tuning.md) - 了解性能优化

## 功能特性索引

### 🚀 核心功能
- **路由管理**: [路由和谓词](05-routes-and-predicates.md)
- **过滤器**: [过滤器](06-filters.md), [全局过滤器](07-global-filters.md)
- **配置管理**: [配置概述](12-configuration.md), [ConfigMap 存储](13-configmap-storage.md)
- **服务发现**: [服务发现](11-service-discovery.md)

### ☸️ Kubernetes 集成
- **CRD 支持**: [CRD 资源管理](09-crd-resources.md)
- **Ingress 集成**: [Ingress 集成](10-ingress-integration.md)
- **原生特性**: [Kubernetes 原生特性](08-kubernetes-native.md)

### 🔒 安全功能
- **认证授权**: [认证和授权](17-authentication-and-authorization.md)
- **安全实践**: [安全最佳实践](26-security-best-practices.md)
- **网络安全**: [Kubernetes 原生特性](08-kubernetes-native.md#安全集成)

### 📊 监控和可观测性
- **指标监控**: [监控和指标](18-monitoring-and-metrics.md)
- **日志追踪**: [日志和链路追踪](19-logging-and-tracing.md)
- **健康检查**: [健康检查](23-health-checks.md)

### 🛠️ 运维管理
- **管理界面**: [管理界面概述](20-admin-interface.md)
- **故障排除**: [故障排除](24-troubleshooting.md)
- **性能调优**: [性能调优](25-performance-tuning.md)

### 🔧 高级功能
- **限流熔断**: [限流和熔断](16-rate-limiting-and-circuit-breaker.md)
- **动态配置**: [动态配置更新](14-dynamic-configuration.md)
- **配置验证**: [配置验证](15-configuration-validation.md)

## 快速参考

### 📋 常用配置
```yaml
# 基本路由配置
spring:
  cloud:
    gateway:
      routes:
      - id: example-route
        uri: lb://example-service
        predicates:
        - Path=/api/**
        filters:
        - StripPrefix=1
```

### 🔧 常用命令
```bash
# 查看路由
kubectl get configmap tigateway-route-config -o yaml

# 重启服务
kubectl rollout restart deployment/tigateway

# 查看日志
kubectl logs -f deployment/tigateway
```

### 📚 相关资源
- [Spring Cloud Gateway 官方文档](https://spring.io/projects/spring-cloud-gateway)
- [Kubernetes 官方文档](https://kubernetes.io/docs/)
- [项目 GitHub 仓库](https://github.com/your-org/tigateway)

## 贡献指南

如果您发现文档中的错误或有改进建议，请：

1. 提交 Issue 描述问题
2. 提交 Pull Request 进行修改
3. 遵循现有的文档风格和格式

## 版本信息

- **当前版本**: 1.0.0
- **基于**: Spring Cloud Gateway 3.1.x
- **Kubernetes 版本**: 1.20+
- **Java 版本**: 11+

---

**开始您的 TiGateway 之旅！** 🚀

选择适合您的学习路径，开始探索 TiGateway 的强大功能。
