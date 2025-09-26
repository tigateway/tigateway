# TiGateway 用户指南

## 概述

TiGateway 是一个基于 Spring Cloud Gateway 的 Kubernetes 原生 API 网关解决方案。本指南将帮助您了解、部署和使用 TiGateway。

## 目录

### 第一部分：入门指南
1. [TiGateway 简介](01-introduction.md)
2. [快速开始](02-quick-start.md)
3. [安装和部署](03-installation.md)

### 第二部分：核心概念
4. [架构概述](04-architecture.md)
5. [路由和谓词](05-routes-and-predicates.md)
6. [过滤器](06-filters.md)
7. [全局过滤器](07-global-filters.md)

### 第三部分：Kubernetes 集成
8. [Kubernetes 原生特性](08-kubernetes-native.md)
9. [CRD 资源管理](09-crd-resources.md)
10. [Ingress 集成](10-ingress-integration.md)
11. [服务发现](11-service-discovery.md)

### 第四部分：配置管理
12. [配置概述](12-configuration.md)
13. [ConfigMap 存储](13-configmap-storage.md)
14. [动态配置更新](14-dynamic-configuration.md)
15. [配置验证](15-configuration-validation.md)

### 第五部分：高级功能
16. [限流和熔断](16-rate-limiting-and-circuit-breaker.md)
17. [认证和授权](17-authentication-and-authorization.md)
18. [监控和指标](18-monitoring-and-metrics.md)
19. [日志和链路追踪](19-logging-and-tracing.md)

### 第六部分：管理界面
20. [管理界面概述](20-admin-interface.md)
21. [Web UI 使用指南](21-web-ui-guide.md)
22. [REST API 参考](22-rest-api-reference.md)

### 第七部分：运维指南
23. [健康检查](23-health-checks.md)
24. [故障排除](24-troubleshooting.md)
25. [性能调优](25-performance-tuning.md)
26. [安全最佳实践](26-security-best-practices.md)

### 第八部分：开发指南
27. [自定义组件开发](27-custom-components.md)
28. [扩展开发](28-extension-development.md)
29. [测试指南](29-testing-guide.md)

### 第九部分：参考文档
30. [配置属性参考](30-configuration-properties.md)
31. [API 参考](31-api-reference.md)
32. [常见问题](32-faq.md)

## 文档风格说明

本指南采用与 Spring Cloud Gateway 官方文档相同的风格和结构：

- **简洁明了**: 每个主题都有清晰的说明和示例
- **代码示例**: 提供完整的 YAML 和 Java 代码示例
- **最佳实践**: 包含生产环境的使用建议
- **故障排除**: 提供常见问题的解决方案

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
