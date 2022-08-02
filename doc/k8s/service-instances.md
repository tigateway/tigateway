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

| 范围                    | 	功能                                                                                  | 	例子                                                                            |
|-----------------------|--------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| allowedOrigins        | 	允许来源发出跨站点请求。特殊值“*”允许所有域。这些值将与 allowedOriginPatterns 中的值组合。                          | 	allowedOrigins: https://example.com                                           |
| allowedOriginPatterns | 	替代 allowedOrigins ，它支持更灵活的来源模式，除了端口列表外，主机名中的任何位置都带有“*”。这些值将与 allowedOrigins 中的值组合。	 | allowedOriginPatterns: <br/> - https://*.test.com:8080                         |
| allowedMethods	       | 允许跨站点请求的 HTTP 方法。特殊值“*”允许所有方法。如果未设置，则默认允许“GET”和“HEAD”。	                              | allowedMethods: <br/> - GET <br/> - PUT <br/> - POST                           |
| allowedHeaders	       | 跨站点请求中允许的标头。特殊值“*”允许实际请求发送任何标头。	                                                     | allowedHeaders:                                 <br/> - X-Custom-Header        |
| maxAge                | 	客户端可以缓存飞行前请求的响应多长时间（以秒为单位）。	                                                        | maxAge: 300                                                                    |
| allowCredentials	     | 跨站点请求是否支持用户凭据。有效值：`true`、`false`。	                                                   | allowCredentials: true                                                         |
| exposedHeaders	       | 为跨站点请求公开的 HTTP 响应标头。	                                                                | exposedHeaders:                                        <br/> - X-Custom-Header |

您还可以为每个路由配置 CORS 行为。但是，不得设置全局 CORS 配置。网关上定义的每个路由都应该在路由配置上具有匹配的路径谓词。

请注意，您还可以通过Cors 过滤器定义每个路由的 cors 行为。

下面的示例为/get/**和/example/**路由配置 CORS 行为：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  api:
    cors:
      perRoute:
        '[/get/**]':
          allowedOrigins:
            - "https://foo.example.com"
          allowedMethods:
            - "GET"
            - "PUT"
            - "POST"
          allowedHeaders:
            - '*'
        '[/example/**]':
          allowedOrigins:
            - "https://bar.example.com"
          allowedMethods:
            - "GET"
            - "POST"
          allowedHeaders:
            - '*'
```

每个路由都可以配置与上表相同的参数。

这是一个匹配的路由配置：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGatewayRouteConfig
metadata:
  name: my-gateway-routes
spec:
  routes:
    - uri: https://httpbin.org
      predicates:
        - Path=/get/**
      filters:
        - StripPrefix=1
    - uri: https://httpbin.org
      predicates:
        - Path=/example/**
      filters:
        - StripPrefix=1
```

注意：为避免由于下游服务也在进行 CORS 处理而导致重复的标头（例如，接收多个“Access-Control-Allow-Origin”或多个“Access-Control-Allow-Credentials”）导致浏览器调用失败，请在这两个标头会自动删除，网关中配置的标头将始终占主导地位。

## 配置 Java 环境选项
对于 JVM 调优，可以JAVA_OPTS在 Spring Cloud Gateway for K8s 配置中定义 Java 环境选项 ()。

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  count: 2
  java-opts: -XX:+PrintFlagsFinal -Xmx512m
```

这将重新启动 pod 并将选项应用到底层网关实例。

## 配置会话过期
如果您需要能够在一定时间（例如 10 分钟）后丢弃非活动会话，只需添加inactive-session-expiration-in-minutes配置即可。

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  sso:
    secret: my-sso-credentials
    inactive-session-expiration-in-minutes: 10
```

这不会修改任何授权服务器令牌过期（或 ttl）配置。它只影响网关内部管理的会话信息。

与其他 Kubernetes 资源类似，您可以选择在spec.resources.

默认情况下，每个实例都初始化为：

| 资源    | 	已请求  | 	限制  |
|-------|-------|------|
| 记忆	   | 256米	 | 512米 |
| 中央处理器 | 	500m | 	2   |

但是您可以更改它，如下例所示。请注意，低于要求可能会导致问题，因此不推荐。

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  resources:
    requests:
      memory: "512Mi"
      cpu: "1"
    limits:
      memory: "1Gi"
      cpu: "2"
```

## 配置探针
与其他 Kubernetes 资源类似，您可以选择为网关配置livenessProbe、readinessProbe和startupProbe,。

默认情况下，每个实例都初始化为：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  livenessProbe:
    initialDelaySeconds: 5
    failureThreshold: 10
    periodSeconds: 3
    timeoutSeconds: 1
    successThreshold: 1
  readinessProbe:
    initialDelaySeconds: 5
    failureThreshold: 10
    periodSeconds: 3
    timeoutSeconds: 1
    successThreshold: 1
  startupProbe:
    initialDelaySeconds: 10
    failureThreshold: 30
    periodSeconds: 3
    timeoutSeconds: 1
    successThreshold: 1
```

但是您可以更改它们以更好地满足您的要求。

## 配置可观察性
Spring Cloud Gateway for Kubernetes 可以配置公开跟踪并根据不同的监控信号生成一组指标和跟踪，以帮助理解总体行为。

注意：指标和跟踪是相互独立的。

### 向 Wavefront 公开指标
要向Wavefront公开指标，我们需要Secret使用以下数据创建 awavefront.api-token和wavefront.uri，分别表示 Wavefront 的 API 令牌和 Wavefront 的 URI 端点。例如：

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: metrics-wavefront-secret
data:
  wavefront.api-token: "NWU3ZCFmNjYtODlkNi00N2Y5LWE0YTMtM2U3OTVmM2Y3MTZk"
  wavefront.uri: "aHR0cHM6Ly92bAdhcmUud2F2ZWZyb250LmNvbQ=="
```

然后，在SpringCloudGateway种类中，引用在该部分下的上一步中创建的秘密metrics。例如：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: test-gateway-metrics
spec:
  observability:
    metrics:
      wavefront:
        enabled: true
    wavefront:
      secret: metrics-wavefront-secret
      source: my-source
      application: my-shopping-application
      service: gateway-service
```

应用配置后，Wavefront 将开始接收Spring Cloud Gateway默认提供的指标。

注意：如果您还使用波前进行跟踪，请确保在两个规范中指定相同的密钥和源。

使用 Spring Cloud Gateway for Kubernetes Dashboard for Wavefront
Spring Cloud Gateway for Kubernetes 有一个预构建的仪表板，您可以在 Wavefront 中使用。

如果您使用的是VMware 的 Wavefront，那么您可以克隆和自定义已经创建的Spring Cloud Gateway for Kubernetes Dashboard。

或者，用于 Kubernetes 发布工件的 Spring Cloud Gateway 包含一个仪表板，可以在仪表板发布文件夹中找到该仪表板。

要导入它，我们需要创建一个 API Token并执行以下命令：

```shell
curl -XPOST 'https://vmware.wavefront.com/api/v2/dashboard' --header "Authorization: Bearer ${WAVEFRONT_API_TOKEN}" --header "Content-Type: application/json" -d "@wavefront-spring-cloud-gateway-for-kubernetes.json"
```

### 向 Prometheus 公开指标

为了向Prometheus公开指标，我们需要在SpringCloudGateway类型中添加一个 prometheus 部分，如果我们希望将报废注释添加到网关 pod 中，例如：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: test-gateway-metrics
spec:
  observability:
    metrics:
      prometheus:
        enabled: true
```

应用配置后，Prometheus 执行器端点将可用。

如果除此之外，我们希望将报废注释添加到所有 Spring Cloud Gateway Pod，我们应该创建我们的 Prometheus 配置，并将其annotations设置为true，例如：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: test-gateway-metrics-with-annotations
spec:
  observability:
    metrics:
      prometheus:
        enabled: true
        annotations:
          enabled: true
```

这将为每个 Spring Cloud Gateway Pod 添加以下注解：

```yaml
   annotations:
     prometheus.io/scrape: "true"
     prometheus.io/path: "/actuator/prometheus"
     prometheus.io/port: "8090"
```

使用 Spring Cloud Gateway for Kubernetes Dashboard for Grafana
Spring Cloud Gateway for Kubernetes 发布工件包含一个 Grafana 仪表板，可以在仪表板发布文件夹中找到。要导入它，您可以按照如何导入指南。

### 将跟踪暴露于波前
要向Wavefront公开跟踪，我们需要Secret使用以下数据创建 awavefront.api-token和wavefront.uri，分别表示 Wavefront 的 API 令牌和 Wavefront 的 URI 端点。例如：

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: tracing-wavefront-secret
data:
  wavefront.api-token: "NWU3ZCFmNjYtODlkNi00N2Y5LWE0YTMtM2U3OTVmM2Y3MTZk"
  wavefront.uri: "aHR0cHM6Ly92bAdhcmUud2F2ZWZyb250LmNvbQ=="
```

然后，在SpringCloudGateway种类中，引用在该部分下的上一步中创建的秘密tracing。例如：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: test-gateway-tracing
spec:
  observability:
    tracing:
      wavefront:
        enabled: true
    wavefront:
      secret: tracing-wavefront-secret
      source: my-source
      application: my-shopping-application
      service: gateway-service
```

应用配置后，Wavefront 将开始接收跟踪

注意：如果您还使用波前作为指标，请确保在两个规范中指定相同的密钥和源。

### 将自定义标签应用到 Gateway Pod
可以将自定义标签添加到网关配置中。这些标签将传播到网关操作员创建的 Pod 中，例如：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: test-gateway-tracing
  labels:
    test-label: test
spec:
  count: 2
```

然后可以通过指定标签来列出 Pod：

```shell
 kubectl get pods -l test-label=test
```

## 自定义服务类型

默认情况下，网关使用 ClusterIP 服务公开。您可以通过指定spec.service.type. 您还可以通过指定spec.service.port. 如果未指定，将自动分配端口。

例如：
```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  service:
    type: NodePort
    nodePort: 32222
```

请注意，对于本地开发，您的集群需要配置为公开您的选择nodePort，然后才能从主机向节点发送流量。