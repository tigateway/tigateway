# 服务实例

这些主题描述了如何为 Kubernetes 服务实例创建和管理 Spring Cloud Gateway。

## 配置网关实例

要创建网关实例，您必须创建类型为 的资源SpringCloudGateway。的定义SpringCloudGateway指定：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name:            # Name given to this Gateway instance (required)
  labels:
    my-custom-label: hello # Labels defined in the Gateway resource will also be applied to the Gateway Pods for simplified management
  annotations:
    my-custom-annotation: my-value # Annotations defined on the Gateway resource will also be applied to the Gateway Pods for simplified management
spec:
  count:           # Number of container instances (pods) to scale Gateway for high availability (HA) configuration
  tls:             # Set a list of TLS-enabled hosts
    - hosts:       # Array of hostnames for which to perform TLS termination using the specified certificate
      secretName:  # Name of TLS secret to load certificate and key from
  sso:
    secret:        # Secret name to be used for SSO configuration
    roles-attribute-name:
                   # Roles attribute name used to extract user roles for Roles filter (default: 'roles')
    inactive-session-expiration-in-minutes:
                  # Time to life of inactive sessions in minutes, 0 means sessions won't expire.
  observability:
    metrics:
      wavefront:
        enabled:  # If wavefront metrics should be pushed
      prometheus:
        enabled:  # If a prometheus endpoint should be exposed
        annotations:
          enabled: # If scrapping annotations should be included in the Pod
    tracing:
      wavefront:
        enabled: # If wavefront traces should be pushed
    wavefront:
      secret:        # Secret name to be used for wavefront configuration
      source:        # The wavefront source (default: Gateway Pod name, `gateway-0`).
      application:   # The wavefront application (default: Gateway Namespace `namespace`).
      service:       # The wavefront service (default: Gateway name `my-gateway`).
  api:
    groupId:       # Unique identifier for the group of APIs available on the Gateway instance (default: normalized title of the Gateway instance)
    title:         # Title describing the context of the APIs available on the Gateway instance (default: name of the Gateway instance)
    description:   # Detailed description of the APIs available on the Gateway instance (default: `Generated OpenAPI 3 document that describes the API routes configured for '[Gateway instance name]' Spring Cloud Gateway instance deployed under '[namespace]' namespace.`)
    documentation: # Location of additional documentation for the APIs available on the Gateway instance
    version:       # Version of APIs available on this Gateway instance (default: `unspecified`)
    serverUrl:     # Base URL that API consumers will use to access APIs on the Gateway instance
    cors:
      allowedOrigins:          # Allowed origins to make cross-site requests, applied globally
      allowedOriginPatterns:   # Allowed origin patterns to make cross-site requests, applied globally
      allowedMethods:          # Allowed HTTP methods on cross-site requests, applied globally
      allowedHeaders:          # Allowed headers in cross-site request, applied globally
      maxAge:                  # How long, in seconds, the response from a pre-flight request can be cached by clients, applied globally
      allowCredentials:        # Whether user credentials are supported on cross-site requests, applied globally
      exposedHeaders:          # HTTP response headers to expose for cross-site requests, applied globally
      perRoute:                # A map of URL Patterns to Spring Framework CorsConfiguration, to configure CORS per route.

  java-opts:       # JRE parameters for Gateway instance to enhance performance

  env:             # Set a list of [configuration](https://cloud.spring.io/spring-cloud-gateway/reference/html/appendix.html#common-application-properties) environment variables to configure this Gateway instance
    - name:          # Name of the environment variable
      value:         # Value of environment variable

  extensions:                 # Additional configurations for global features (e.g. custom filters, Api Key,...)
    custom:                   # Array of custom extensions to load (name must match the ConfigMap name).
    secretsProviders:         # Array of secret providers. These are identified by a name and follow conventions similar to `volumes`. Currently only supports Vault.
    filters:
      apiKey:                 # API Key specific configurations
        enabled:
        secretsProviderName:
      jwtKey:                 # JWT Key specific configurations
        enabled:
        secretsProviderName:

  resources:
    requests:      # Requested amount of compute resources for the Gateway instance
      cpu:
      memory:
    limits:        # Maximum amount of compute resources allowed for the Gateway instance
      cpu:
      memory:

  livenessProbe:
    initialDelaySeconds: # Number of seconds after the container has started before probes are initiated
    failureThreshold:    # When a probe fails, Kubernetes will try failureThreshold times before giving up
    periodSeconds:       # How often (in seconds) to perform the probe
    timeoutSeconds:      # Number of seconds after which the probe times out
    successThreshold:    # Minimum consecutive successes for the probe to be considered successful after having failed
  livenessProbe:
    initialDelaySeconds:
    failureThreshold:
    periodSeconds:
    timeoutSeconds:
    successThreshold:
  startupProbe:
    initialDelaySeconds:
    failureThreshold:
    periodSeconds:
    timeoutSeconds:
    successThreshold:

  securityContext: # SecurityContext applied to the Gateway pod(s).
    fsGroup:       # Set to 1000 by default
    runAsGroup:
    runAsUser:

  serviceAccount:  # Name of the ServiceAccount associated to the Gateway instance
    name:

  service:         # Configuration of the Kubernetes service for the gateway
    type:          # Determines how the Service is exposed. Either ClusterIP, NodePort, or LoadBalancer. Defaults to ClusterIP.
    nodePort:      # The port on which this service is exposed when type=NodePort or LoadBalancer.
```

以下是一个示例网关实例配置文件：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  count: 3
  api:
    title: My Exciting APIs
    description: Lots of new exciting APIs that you can use for examples!
    version: 0.1.0
    serverUrl: https://gateway.example.com
  env:
    - name: spring.cloud.gateway.httpclient.connect-timeout
      value: "90s"
```

## 配置外部访问

每个网关实例都有一个类型的关联服务ClusterIP。您可以通过常见的 Kubernetes 方法（例如入口路由或端口转发）公开此服务。请查阅您的云提供商的文档以了解您可以使用的 Ingress 选项。

### 使用入口资源

在添加入口之前，请确保根据云提供商文档在 Kubernetes 集群中运行入口控制器。

要使用 Ingress 资源公开网关实例：

1. 在创建网关实例的命名空间中，找到ClusterIP与网关实例关联的服务。您可以将此服务用作 Ingress 后端，也可以将其更改为不同的服务类型。

2. ingress-config.yaml使用以下 YAML 定义创建一个名为 的文件：
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress 
metadata: 
  name: my-gateway-ingress
  namespace: my-namespace
  annotations: 
    kubernetes.io/ingress.class: contour 
spec: 
  rules: 
    - host: my-gateway.my-example-domain.com 
      http: 
        paths:
          - path: /
            pathType: Prefix
            backend: 
              service:
                name: my-gateway 
                port:
                  number: 80 
```
对于hostandserviceName值，替换为您想要的主机名和服务名。

此示例 Ingress 资源配置使用Project Contour Ingress Controller。如果您希望使用另一个 Ingress 实现，您可以调整示例配置。

3. 应用 Ingress 定义文件。Ingress 资源将在与 Gateway 实例相同的命名空间中创建。

4. 检查新创建的 Ingress 资源：
```shell
$ kubectl -n my-namespace get ingress my-gateway-ingress

NAME                  CLASS    HOSTS                                     ADDRESS       PORTS   AGE
my-gateway-ingress    <none>   my-gateway.my-example-domain.com          34.69.53.79    80     2m51s
```

```shell
$ kubectl -n my-namespace describe ingress my-gateway-ingress

Name:             my-gateway-ingress
Namespace:        my-namespace 
Address:          34.69.53.79 
Default backend:  default-http-backend:80 (<error: endpoints "default-http-backend" not found>) 
Rules: 
  Host                                     Path  Backends 
  ----                                     ----  -------- 
  my-gateway.my-example-domain.com
                                           /   my-gateway:80 () 
```

如示例输出所示，my-gateway.my-example-domain.comIngress 定义中的虚拟主机映射到my-gateway后端的服务。

5. 确保您可以将 Ingress 定义主机名（在本例中为my-gateway.my-example-domain.com）解析为 Ingress 资源的 IP 地址。

IP 地址显示在命令Address输出的字段中。kubectl describe

对于本地测试，请使用以下命令打开您的 /etc/hosts 文件。

```shell
sudo vim /etc/hosts
```

通过向主机文件添加一行来解析主机名。

```shell
34.69.53.79     my-gateway.my-example-domain.com 
```

对于扩展评估，您可以创建一个通配符 DNS A 记录，将域名上的任何虚拟主机（例如*.my-example-domain.com）映射到 Ingress 资源。

6. 您现在应该能够使用 Web 浏览器或 HTTP 客户端（例如 HTTPie 或 cURL）通过 Ingress 资源连接到您的网关实例。
```shell
$ http my-gateway.my-example-domain.com/github
$ http my-gateway.my-example-domain.com/github/<YOUR_GITHUB_USERNAME>
```

这些请求应从 GitHub 主页 ( https://github.com) 或 GitHub 网站上的请求路径接收响应。

7. 测试 SSO 配置，例如使用 HTTPie 等 HTTP 客户端：

```shell
$ http my-gateway.my-example-domain.com/github
```

此请求应导致302HTTP 状态代码响应，重定向到 SSO 登录页面。如果您使用网络浏览器访问路由my-gateway.my-example-domain.com/github，您将被重定向到 SSO 登录页面。验证后，您将被重定向到 GitHub 主页。

### TLS 直通
如果您想在网关实例上启用TLS 终止，您需要将请求路由到网关服务的端口 443，而不是端口 80。

您还需要配置 Ingress 以允许 TLS 直通 - 此配置取决于 Ingress 实现。

例如，要使用 Contour 执行此操作，而不是使用 Ingress API，您需要使用TLS 直通选项创建一个 HTTPProxy 实例：

```yaml
apiVersion: projectcontour.io/v1
kind: HTTPProxy
metadata:
  name: my-gateway-httpproxy
spec:
  virtualhost:
    fqdn: my-gateway.my-example-domain.com
    tls:
      passthrough: true
  tcpproxy:
    services:
      - name: my-gateway
        port: 443
```

## 网关执行器管理端口

Kubernetes 实例的 Spring Cloud Gateway 是使用Spring Boot 执行器管理端口创建的。根据 HA 配置，每个网关实例 pod 上的管理端口为 8090。此管理端口可用于使用以下端点进行监控：

* /actuator/info- 显示版本和其他网关实例信息
* /actuator/health- 将网关实例健康指示器显示为状态值UP或DOWN
* /actuator/gateway/routes- 检索网关实例上当前可用的所有 API 路由列表
* /actuator/gateway/globalfilters- 检索网关实例上启用的全局过滤器列表
* /actuator/gateway/routefilters- 检索网关实例上可用的路由过滤器列表

## 配置高可用性

您可以为 Kubernetes 配置 Spring Cloud Gateway 以在高可用性下运行多个实例，就像使用普通 Kubernetes 资源一样。

在网关运行时，您可以使用它kubectl scale来修改副本的数量。例如，给定一个具有 1 个副本的网关，以下将副本数增加到 4 个。

```shell
$ kubectl scale scg my-gateway --replicas=4
```

并将数字减少回原始值。

```shell
$ kubectl scale scg my-gateway --replicas=1
```

在初始配置中，您可以使用参数指定副本spec.count数。以下示例将副本计数配置为 3。

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  count: 3
```

只要描述符中没有引入其他更改，您就可以安全地修改spec.count并重新申请以增加或减少副本的数量。

要验证您的更改，请使用kubectl get pods检查 pod 是否与计数匹配。

### 配置 TLS 终止

您可以配置网关实例以执行 TLS 终止，对不同的路由使用不同的证书。

证书及其关联的私钥是从Kubernetes TLS secrets加载的。为您希望网关提供的每个证书创建一个 TLS 类型机密。最简单的方法是使用kubectlPEM 编码的证书和密钥文件：

```shell
kubectl create secret tls my-tls-secret-name --cert=path/to/tls.crt --key=path/to/tls.key
```

该tls.crt文件可以包含多个与服务器证书连接在一起的 CA 证书，以表示完整的信任链。

该tls.key文件应包含 PKCS#8 或 PKCS#1 格式的服务器证书的私钥。

接下来，创建一个引用您的 TLS 证书的网关资源。spec.tls数组中的每个条目都包含一个secretName引用包含您要提供的证书/密钥对的 TLS 机密的一个，以及一个hosts. 当请求到达引用这些主机之一的网关时，网关将提供来自匹配密钥的证书。

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: test-gateway-tls
spec:
  count: 1
  tls:
    - hosts:
        - host-a.my-tls-gateway.my-example-domain.com
        - host-b.my-tls-gateway.my-example-domain.com
      secretName: tls-secret-1
    - hosts:
        - host-c.my-tls-gateway.my-example-domain.com
        - host-d.my-tls-gateway.my-example-domain.com
      secretName: tls-secret-2
```

您向网关发出请求的客户端必须支持Server Name Indication，以便将请求的主机作为 TLS 握手的一部分传递给网关。

要验证一切是否按预期工作，您可以使用openssl检查为每个配置的主机返回的证书。例如：

```shell
openssl s_client -showcerts -servername host-a.my-tls-gateway.my-example-domain.com -connect <your ingress ip>:443 | openssl x509 -text
```

where<your ingress ip>应替换为启用 TLS 直通的入口的外部 IP。

## 配置环境变量

您可以定义环境变量映射以使用该spec.env属性配置 API 网关。以下示例配置从 API 网关到应用程序服务的连接超时和 Spring Framework Web 包日志记录级别。

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: gateway-demo
spec:
  # ...
  env:
    - name: spring.cloud.gateway.httpclient.connect-timeout
      value: "90s"
    - name: logging.level.org.springframework.web
      value: debug
```

## 禁用 SecureHeaders 全局过滤器
Spring Cloud Gateway 服务实例的后备应用程序具有默认全局启用的自定义 SecureHeaders 过滤器。此过滤器将以下标头添加到响应中：

| 启用安全标头                     | 	默认值                                           |
|----------------------------|------------------------------------------------|
| Cache-Control	             | no-cache, no-store, max-age=0, must-revalidate |
| Pragma	                    | no-cache                                       |
| Expires	                   | 0                                              |
| X-Content-Type-Options	    | nosniff                                        |
| Strict-Transport-Security	 | max-age=631138519                              |
| X-Frame-Options	           | DENY                                           |
| X-XSS-Protection	          | 1; mode=block                                  |

如果您不希望将任何安全标头添加到响应中，您可以通过设置disable-secure-headers为禁用整个网关实例的全局过滤器true：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  env:
    - name: spring.cloud.gateway.secure-headers.disabled
      value: "true"
```

要禁用给定路由的特定标头，您可以对路由使用RemoveResponseHeader过滤器。例如，要删除X-Frame-Options路由的标头，您可以运行：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGatewayRouteConfig
metadata:
  name: my-gateway-routes
spec:
  routes:
    - uri: https://httpbin.org
      predicates:
        - Path=/remove-cache-control/**
      filters:
        - StripPrefix=1
        - RemoveResponseHeader=X-Frame-Options
```

要为所有路由全局禁用特定标头，您可以根据SecureHeaders过滤器文档在网关上设置环境变量：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  env:
    - name: spring.cloud.gateway.filter.secure-headers.disable
      value: "x-frame-options"
```

## 配置跨域资源共享 (CORS)

您可以定义一个全局 CORS 行为，该行为将应用于映射到它的所有路由配置。

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  api:
    cors:
      allowedOrigins:
        - "https://foo.example.com"
      allowedMethods:
        - "GET"
        - "PUT"
        - "POST"
      allowedHeaders:
        - '*'
```

可以在spec.api.cors块中配置以下参数：

