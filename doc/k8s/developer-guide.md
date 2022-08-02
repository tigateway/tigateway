# 开发指南

这些主题描述了如何使用 Spring Cloud Gateway for Kubernetes。

## 入门

本主题介绍如何快速开始使用 Spring Cloud Gateway for Kubernetes 为微服务架构提供 API 网关。

提示：本主题使用来自GitHub 上的 [spring-cloud-services-samples/animal-rescue](https://github.com/spring-cloud-services-samples/animal-rescue/) 存储库的示例应用程序。接下来，克隆存储库并检查README.md.

这将概述管理通过网关实例提供应用程序编程接口 (API) 的应用程序的路由配置。本概述假设已安装 Spring Cloud Gateway for Kubernetes 管理组件。

组件是

* Gateway Instances - 代表部署的每一个 Spring Cloud Gateways

* 路由配置 - 是一组可应用于一个或多个网关的路由

* 映射 -映射定义了哪些路由配置与哪些网关一起使用


## 创建网关实例
要为 Kubernetes 实例创建 Spring Cloud Gateway ，请gateway-config.yaml使用以下 YAML 定义创建一个名为 的文件：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
name: my-gateway
```

接下来，将此定义应用于您的 Kubernetes 集群：

```shell
$ kubectl apply -f gateway-config.yaml
```

此配置将创建一个新的网关实例。默认情况下，实例将与当前命名空间中的 ClusterIP 服务一起创建。要检查它的状态，您可以使用 Kubernetesget命令。

```shell
$ kubectl get scg my-gateway

NAME               READY   REASON
my-gateway         True    Created
```

要添加路由并将路由映射到网关，我们需要创建一个SpringCloudGatewayRouteConfig描述路由的SpringCloudGatewayMapping对象和一个将路由配置映射到网关的对象。

创建一个route-config.yaml使用以下 YAML 定义调用的文件：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGatewayRouteConfig
metadata:
  name: my-gateway-routes
spec:
  routes:
  - uri: https://github.com
    predicates:
      - Path=/github/**
    filters:
      - StripPrefix=1
```

创建一个mapping.yaml使用以下 YAML 定义调用的文件：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGatewayMapping
metadata:
  name: test-gateway-mapping
spec:
  gatewayRef:
    name: my-gateway
  routeConfigRef:
    name: my-gateway-routes
```

将这两个定义应用到您的 Kubernetes 集群。

该实例将包含一个路由 ( test-route)，它使用Path谓词定义网关内的路径，以及StripPrefix在重定向之前删除路径的过滤器。

要验证网关是否在本地运行，您可以端口转发 ClusterIP 服务。

```shell
$ kubectl -n=spring-cloud-gateway port-forward service/my-gateway 8080:80
```

您现在应该能够从 localhost:8080/github 访问网关。

有关启用对网关实例的外部访问的信息，请参阅配置外部访问。

## 部署客户端应用

在本节中，我们将描述一个使用Animal Rescue 后端 API 示例应用程序的示例场景。以下 YAML 将后端应用程序部署描述为 Kubernetes 上的服务。为了举例，我们假设目标命名空间animal-rescue在 Kubernetes 集群上。

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: animal-rescue-backend
spec:
  selector:
    matchLabels:
      app: animal-rescue-backend
  template:
    metadata:
      labels:
        app: animal-rescue-backend
    spec:
      containers:
        - name: animal-rescue-backend
          image: springcloudservices/animal-rescue-backend
          env:
            - name: spring.profiles.active
              value: k8s
          resources:
            requests:
              memory: "256Mi"
              cpu: "100m"
            limits:
              memory: "512Mi"
              cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: animal-rescue-backend
spec:
  ports:
    - port: 80
      targetPort: 8080
  selector:
    app: animal-rescue-backend
```

这假设在您的容器映像存储库中有一个可用的映像，名为springcloudservices/animal-rescue-backend. 要部署应用程序，请将 YAML 保存到名为的文件中animal-rescue-backend.yaml并运行以下命令。

```shell
$ kubectl apply -f animal-rescue-backend.yaml --namespace animal-rescue
```

## 将 API 路由添加到网关

现在 Animal Rescue 后端应用程序作为服务运行，animal-rescue-backend您可以描述要应用的路由配置my-gateway。

创建一个animal-rescue-backend-route-config.yaml使用以下定义调用的文件：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGatewayRouteConfig
metadata:
  name: animal-rescue-backend-route-config
spec:
  service:
    name: animal-rescue-backend
  routes:
    - predicates:
        - Path=/api/**
      filters:
        - StripPrefix=1
```

创建另一个animal-rescue-backend-mapping.yaml使用以下定义调用的文件：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGatewayMapping
metadata:
  name: animal-rescue-backend-mapping
spec:
  gatewayRef:
    name: my-gateway
  routeConfigRef:
    name: animal-rescue-backend-route-config
```

和对象类型由 Spring Cloud Gateway for Kubernetes 管理组件处理，以更新属性值中提供的所需网关SpringCloudGatewayMapping实例。属性值中提供了为配置路由流量的应用程序。SpringCloudGatewayRouteConfigspec.gatewayRefroutesspec.service

将这两个定义应用于您的 Kubernetes 集群。

```shell
$ kubectl apply -f animal-rescue-backend-route-config.yaml
$ kubectl apply -f animal-rescue-backend-mapping.yaml
```

假设my-gateway已经为 FQDN 申请了入口my-gateway.my-example-domain.com，则 Animal Rescue 后端 API 将在路径下可用my-gateway.my-example-domain.com/api/...。示例应用程序中可用的端点之一是GET /api/animals列出所有可用于收养请求的动物。现在应该可以使用以下命令访问此端点。

```shell
# Using https://httpie.io/
$ http my-gateway.my-example-domain.com/api/animals

# Using curl
$ curl my-gateway.my-example-domain.com/api/animals
```

如果您不使用入口，则可以端口转发网关：

```shell
$ kubectl port-forward service/my-gateway 8080:80
```

并使用另一个终端窗口，调用/api/animals端点：

```shell
# Using https://httpie.io/
$ http localhost:8080/api/animals

# Using curl
$ curl localhost:8080/api/animals
```

有关将应用程序的 API 路由添加到网关实例的更多信息，请参阅将路由添加到网关。

## 删除网关实例

delete使用 Kubernetes cli命令可以轻松删除网关实例。

```shell
$ kubectl delete scg my-gateway
```

之后，如果您列出现有的网关，kubectl get scg您会注意到它不再运行。

注意：删除网关不会删除相关的路由配置或映射。为此，您可以使用kubectl delete scgrc <routeconfig-name>或kubectl delete scgm <mapping-name>。