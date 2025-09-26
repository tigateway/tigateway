# TiGateway 简介

## 什么是 TiGateway

TiGateway 是一个基于 Spring Cloud Gateway 的 Kubernetes 原生 API 网关解决方案。它通过自定义 CRD 资源提供云原生的 API 网关功能，完全基于 Kubernetes 和 ConfigMap 存储，无需传统数据库。

## 核心特性

### Kubernetes 原生
- **完全基于 Kubernetes**: 使用 Kubernetes 原生资源进行配置管理
- **ConfigMap 存储**: 基于 ConfigMap 的配置存储，支持 YAML Schema 验证
- **CRD 资源管理**: 使用 `tigateway.cn` API 组管理网关配置
- **RBAC 支持**: 完整的 Kubernetes 权限控制

### 动态路由
- **Ingress 自动发现**: 支持 Kubernetes Ingress 自动发现和动态路由配置
- **服务发现**: 自动发现 Kubernetes 服务
- **配置热更新**: 支持配置实时更新，无需重启服务

### 多端口架构
- **主网关**: 端口 8080，处理所有入站请求
- **管理界面**: 端口 8081，提供 Web UI 和 REST API
- **监控端点**: 端口 8090，提供健康检查和监控功能

### 技术特性
- **响应式架构**: 基于 Spring WebFlux 和 Reactor Netty
- **健康检查**: 完整的健康检查和监控端点
- **可扩展性**: 支持自定义扩展和过滤器

## 架构优势

### 云原生设计
TiGateway 采用云原生架构设计，具有以下优势：

- **无状态设计**: 所有配置存储在 Kubernetes 中，支持水平扩展
- **容器化部署**: 完全容器化，支持 Kubernetes 原生部署
- **服务网格集成**: 与 Kubernetes 服务网格无缝集成

### 高可用性
- **多副本支持**: 支持多副本部署和负载均衡
- **故障转移**: 自动故障检测和转移
- **零停机更新**: 支持滚动更新，零停机部署

### 可观测性
- **指标收集**: 内置 Prometheus 指标收集
- **链路追踪**: 支持分布式链路追踪
- **日志聚合**: 结构化日志输出，支持日志聚合

## 与 Spring Cloud Gateway 的关系

TiGateway 基于 Spring Cloud Gateway 构建，继承了其所有核心功能：

- **路由匹配**: 支持所有 Spring Cloud Gateway 的路由谓词
- **过滤器**: 支持所有内置过滤器和自定义过滤器
- **全局过滤器**: 支持全局过滤器链
- **响应式编程**: 基于 WebFlux 的响应式架构

### 扩展功能

在 Spring Cloud Gateway 基础上，TiGateway 提供了以下扩展：

- **Kubernetes 集成**: 原生 Kubernetes 资源管理
- **CRD 支持**: 自定义资源定义支持
- **ConfigMap 存储**: 基于 ConfigMap 的配置存储
- **管理界面**: 完整的 Web 管理界面

## 适用场景

### 微服务网关
- **API 聚合**: 将多个微服务 API 聚合为统一的入口
- **路由转发**: 智能路由转发和负载均衡
- **协议转换**: 支持 HTTP、HTTPS、WebSocket 等协议

### Kubernetes 环境
- **Ingress 替代**: 作为 Kubernetes Ingress 的高级替代方案
- **服务网格**: 与服务网格解决方案集成
- **多云部署**: 支持多云 Kubernetes 环境

### 企业级应用
- **安全网关**: 提供认证、授权和安全策略
- **监控网关**: 集成监控和链路追踪
- **合规网关**: 支持企业级合规要求

## 技术栈

### 核心框架
- **Spring Cloud Gateway**: 3.1.x
- **Spring Boot**: 2.6.3
- **Spring WebFlux**: 响应式 Web 框架
- **Reactor Netty**: 响应式网络框架

### Kubernetes 集成
- **Kubernetes Client**: 18.0.1
- **Fabric8 Kubernetes Client**: 用于 Kubernetes API 交互
- **Kubernetes ConfigMap**: 配置存储

### 监控和可观测性
- **Micrometer**: 指标收集
- **Prometheus**: 指标存储和查询
- **Spring Boot Actuator**: 健康检查和监控端点

## 版本要求

### 系统要求
- **Java**: 11 或更高版本
- **Kubernetes**: 1.20 或更高版本
- **内存**: 最少 512MB，推荐 1GB
- **CPU**: 最少 0.5 核，推荐 1 核

### 依赖组件
- **Kubernetes API Server**: 用于资源管理
- **ConfigMap**: 用于配置存储
- **Service**: 用于服务发现
- **Ingress**: 用于路由配置（可选）

## 下一步

现在您已经了解了 TiGateway 的基本概念和特性，可以继续阅读：

- [快速开始](quick-start.md) - 学习如何快速部署和使用 TiGateway
- [架构概述](architecture.md) - 深入了解 TiGateway 的架构设计
- [安装和部署](installation.md) - 学习如何在 Kubernetes 中部署 TiGateway
