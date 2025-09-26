# 使用单点登录

Spring Cloud Gateway for Kubernetes 支持使用单点登录 (SSO) 和支持 OpenID Connect Discovery 协议的 OpenID 身份提供程序进行身份验证和授权。

除了使用 SSO 身份验证工作流程之外，您还可以设置过滤器以支持：

* 基于范围的访问控制
* 基于角色的访问控制

## 配置单点登录 (SSO)

您可以配置 Spring Cloud Gateway for Kubernetes 以使用 OpenID 身份提供者通过单点登录 (SSO) 对请求进行身份验证。

要将网关实例配置为使用 SSO：

1. 创建一个名为 的文件sso-credentials.txt，包括以下属性：

```txt
scope=openid,profile,email
client-id={your_client_id}
client-secret={your_client_secret}
issuer-uri={your-issuer-uri}
```

对于client-id、client-secret和issuer-uri值，使用来自您的 OpenID 身份提供者的值。对于该scope值，请使用要包含在 JWT 身份令牌中的范围列表。此列表应基于您的身份提供者允许的范围。

issuer-uri配置应遵循 Spring Boot 约定，如官方Spring Boot 文档中所述：

提供者需要配置一个 issuer-uri，它是它声明为其颁发者标识符的 URI。例如，如果提供的 issuer-uri 是“https://example.com”，那么将向“https://example.com/.well-known/openid-configuration”发出 OpenID 提供者配置请求。结果应为 OpenID 提供者配置响应。

请注意，只能使用支持 OpenID Connect Discovery 协议的授权服务器。

2. 配置外部授权服务器以允许重定向回网关。请参阅您的授权服务器的文档并添加https://<gateway-external-url-or-ip-address>/login/oauth2/code/sso到允许的重定向 URI 列表中。

3. 在 Spring Cloud Gateway for Kubernetes 命名空间中，使用sso-credentials.txt在上一步中创建的文件创建 Kubernetes 机密：

```shell
$ kubectl create secret generic my-sso-credentials --from-env-file=./sso-credentials.txt
```

4. kubectl describe使用命令检查机密。验证Data密钥列是否包含上面列出的所有必需属性。

5. 在字段的SpringCloudGateway定义中添加 SSO 密码。spec.sso.secret在对象routes列表中SpringCloudGatewayRouteConfig，将设置添加ssoEnabled: true到必须具有经过身份验证的访问的每个路由。请参阅以下更新gateway-config.yaml和route-config.yaml文件：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  api:
    serverUrl: https://my-gateway.my-example-domain.com
    title: Animal Rescue APIs
    description: Make and track adoption requests for animals that need to be rescued.
    version: "1.0"
  sso:
    secret: my-sso-credentials
```

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGatewayRouteConfig
metadata:
  name: my-gateway-routes
spec:
  routes:
  - uri: https://github.com
    ssoEnabled: true
    predicates:
      - Path=/github/**
    filters:
      - StripPrefix=1
```

ssoEnabled设置为时，网关实例将对所有配置为仅允许经过true身份验证的访问的 API 路由使用 SSO。

6. 应用更新的 Gateway 和 RouteConfig 定义文件：

```shell
$ kubectl apply -f gateway-config.yaml
$ kubectl apply -f route-config.yaml
```

## 更新单点登录凭据

要更新网关的 SSO 凭据：

1. 通过删除旧秘密来更新秘密（例如my-sso-credentials）中的值，然后重新创建它：

```shell
$ kubectl delete secret my-sso-credentials
$ kubectl create secret generic my-sso-credentials --from-env-file=./sso-credentials-updated.txt
```

或者，使用新的 base64 编码值编辑现有密钥：

```shell
$ echo $NEW_CLIENT_SECRET | base64 | pbcopy
$ kubectl edit secret my-sso-credentials
```

2. Rollout 重新启动网关 statefulset 以强制执行秘密更新：

```shell
kubectl rollout restart statefulset my-gateway
```

有关更多详细信息，请参阅使用 Okta 身份提供程序的 Animal Rescue 演示应用程序的SSO 设置指南。

## OpenAPI 安全方案 (SSO)

当在任何路由上SSOEnabled设置为时，两个（参见https://swagger.io/docs/specification/authentication）在生成的 OpenAPI 规范中注册为组件：truesecurityScheme

* AuthBearer启用对话框以提供承载授权标头
* OpenId以启用从 OIDC 配置中获取令牌并将其添加为标头的对话框

并且，这些方案绑定到任何这些路线。其他路线不受影响，不适用该方案。