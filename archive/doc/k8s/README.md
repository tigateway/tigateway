# Ti Gateway

Spring Cloud Gateway for Kubernetes 提供了一种简单而有效的方法来将 API 请求（内部或外部）路由到公开 API 的应用程序服务。

## 关于 Kubernetes 的 Spring Cloud Gateway

Spring Cloud Gateway for Kubernetes，基于开源的Spring Cloud Gateway项目，是应用开发者喜爱的API网关解决方案。Spring Cloud Gateway for Kubernetes 代表 API 开发团队处理跨领域问题，例如：单点登录 (SSO)、访问控制、速率限制、弹性、安全性等。使用现代云原生模式、您为 API 开发选择的任何编程语言以及与现有 CI/CD 管道策略的集成来加速 API 交付。

![](img.png)

运营商能够使用他们熟悉的原生 Kubernetes 工具链来管理他们所有的 API 网关。通过将 YAML 配置的资源配置应用到其目标 Kubernetes 集群来部署 API 网关。这些资源配置可用于安全地配置身份验证和授权信息，应用性能优化特征和资源利用参数。


## 建立在开源技术之上

Spring Cloud Gateway for Kubernetes 基于开源Spring Cloud Gateway项目。该项目提供了一个库，可用于创建您自己的 API 网关实现，以将 HTTP 流量路由到以任何编程语言编写的应用程序服务。它建立在其他 Spring 生态系统项目之上，包括 Spring Framework 5、Spring Boot 2 和 Project Reactor，并为路由各种客户端请求以及解决安全、监控和弹性等横切问题提供了有效的解决方案。

这个开源项目经过了实战考验，被数千个开发团队用来处理他们的 API 流量。基于其非阻塞 I/O 设计从头开始构建以提高性能，它可以以低延迟处理负载。


## 商业能力

Spring Cloud Gateway for Kubernetes 提供了 Spring Cloud Gateway 的实现，并集成了 Spring Security、Spring Session 等其他 Spring 生态系统项目。除了这些开源基础之外，该产品还包括以下仅供商业使用的功能：

* 具有自定义资源定义 (CRD) 的 Kubernetes 操作员，用于处理应用到具有 Kubernetes“本机”体验的集群的 API 网关自定义资源

* 商业容器图像

* 动态 API 路由配置

* 通过现有的持续集成 (CI) 和持续交付 (CD) 管道支持 API 更新

* 简单的单点登录 (SSO) 配置与商业 API 路由过滤器相结合，以启用身份验证和访问控制

* 其他商业 API 路由过滤器，用于将授权的 JSON Web 令牌 (JWT) 声明传输到应用程序服务、客户端证书授权、速率限制方法、断路器配置、支持通过 HTTP 基本身份验证凭据访问应用程序服务

* OpenAPI 版本 3 自动生成的文档

* 通过设置实例计数以实现水平可扩展性的高可用性配置

* API网关实例的内存和vCPU的垂直扩展配置

* 能够为 API 网关实例配置 JVM 性能优化以满足特定用例

* 随着 API 的发展，支持在本地开发和测试环境中部署资源

借助 Spring Cloud Gateway for Kubernetes，运营商可以专注于管理和监控目标环境中的 API 网关，而应用程序开发人员可以专注于 API 的公开方式，并应用适当的设计和横切关注点。API 使用者在访问通过 Spring Cloud Gateway for Kubernetes 公开的 API 时获得一致的体验。
