# 用于 Kubernetes 的 Spring Cloud Gateway

## 主要特征

Spring Cloud Gateway for Kubernetes 包括以下主要功能：

* Polyglot 支持以任何语言编写的希望在网关实例上公开 HTTP 端点的应用程序服务的可路由性
* 包括用于处理应用于集群的 API 网关自定义资源和 Kubernetes “原生”体验的 Kubernetes 运算符
* 用于管理、创建和动态更新实例上的 API 路由的商业容器镜像
* 动态应用程序路由配置，为持续集成 (CI) 和持续交付 (CD) 管道启用 API 路由更新
* 网关定义的单点登录 (SSO) 配置与商业 SSO 路由过滤器相结合
* 每个 API 网关实例的简化 OpenID Connect (OIDC) 单点登录 (SSO) 配置
* 用于 SSO 身份验证、基于角色的访问控制、范围授权、授权令牌中继、客户端证书授权、速率限制和断路器的商业 API 路由过滤器
* 用于设置 API 网关实例的计数、内存和 vCPU 的高可用性配置
* 有权为 API 网关实例特定用例配置 JVM 性能优化
* 启用本地开发和测试以验证 API 路由配置，然后再升级到生产环境

## 对于运营商
有关安装和管理 Spring Cloud Gateway for Kubernetes 的信息，请参阅操作员指南。

## 对于开发者
有关创建和管理网关实例并将它们连接到客户端应用程序的信息，请参阅开发人员指南。

## 产品支持版本
下表提供了有关 Spring Cloud Gateway for Kubernetes 的版本和版本支持信息。

| 　元素                | 	细节                     |
|--------------------|-------------------------|
| Spring Cloud OSS版本 | 	2021.0.2               |
| Spring Boot OSS 版本 | 	2.6.8                  |
| 支持的 IaaS           | 	Kubernetes 1.19 - 1.23 |