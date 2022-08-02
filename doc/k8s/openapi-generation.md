# OpenAPI 自动生成

请参阅下文，了解如何提供 API Gateway 元数据以及如何使用 API 路由配置来自动生成 OpenAPI v3 文档。

## 访问生成的 OpenAPI v3 文档

Spring Cloud Gateway for Kubernetes operator 管理 Kubernetes 集群上的所有 API Gateway 实例。当您将任何或SpringCloudGateway自定义资源应用到 Kubernetes 集群时，操作员将采取行动使环境与这些请求资源更改相协调。除了处理自定义资源协调之外，操作员还具有符合 OpenAPI v3 的自动生成文档端点。您可以通过使用入口公开服务来访问此端点，然后访问其端点。应用到命名空间中服务的示例入口如下所示：SpringCloudGatewayRouteConfigSpringCloudGatewayMappingscg-operator/openapiscg-operatorspring-cloud-gateway

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: scg-openapi
  namespace: spring-cloud-gateway
  annotations:
    kubernetes.io/ingress.class: contour
spec:
  rules:
  - host: scg-openapi.mydomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: scg-operator
            port:
              number: 80
```

现在，您可以访问自动生成的 OpenAPI v3 端点，方法是转到https://scg-openapi.mydomain.com/openapi. 应用程序开发人员可以提供他们的 API 路由配置以在 API 网关实例上公开，然后这些 API 路由将输入生成文档。这导致基于 API 路由配置谓词、过滤器和元数据跨所有服务实例及其公开的 API 的一致 API。

需要注意的是，将为每个 API Gateway 实例生成一个单独的 OpenAPI v3 文档，并且/openapi端点为此 Kubernetes 集群上的所有实例提供这些文档的数组。

## 配置 OpenAPI 元数据

配置 API Gateway 实例时可以定义以下描述性元数据：

* serverUrl：此网关实例的可公开访问的面向用户的 URL。请务必注意，此配置不会为此 URL 创建新的路由映射，这仅用于元数据的目的，以显示在 OpenAPI 生成的文档中。
* title：描述网关实例上可用 API 上下文的标题（默认值Spring Cloud Gateway for K8S：）
* description：网关实例上可用API的详细描述（默认Generated OpenAPI 3 document that describes the API routes configured for '[Gateway instance name]' Spring Cloud Gateway instance deployed under '[namespace]' namespace.：）
* version：此网关实例上可用的 API 版本（默认值unspecified：）
* documentation：网关实例上可用 API 的其他文档的位置

以下是使用此描述性元数据的 API 网关配置示例：

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGateway
metadata:
  name: my-gateway
spec:
  api:
    serverUrl: https://gateway.example.org
    title: My Exciting APIs
    description: Lots of new exciting APIs that you can use for examples!
    version: 0.1.0
    documentation: https://docs.example.org
```

这将在/openapi运算符的端点中显示为：

```yaml
"info": {
  "title": "My Exciting APIs",
  "description": "Lots of new exciting APIs that you can use for examples!",
  "version": "0.1.0"
},
"externalDocs": {
  "url": "https://docs.example.org"
},
"servers": [
  {
    "url": "https://gateway.example.org"
  }
],
```

PUT/POST/PATCH 请求正文模式
对于 PUT、POST 和 PATCH 操作，您可以添加请求正文对象的 OpenAPI Schema 。

如下例所示，将model.requestBody属性添加到具有正确信息的路由中。

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGatewayRouteConfig
metadata:
  name: myapp-route-config
spec:
  service:
    name: myapp-service
  routes:
    - id: example-route-id
      predicates:
        - Path=/users/**
      model:
        requestBody:
          description: User to add
          content:
            'application/json':
              schema:
                type: object
                description: User schema
                properties:
                  name:
                    type: string
                  age:
                    type: integer
                    format: int32
                required:
                  - name
```

该模型以及可用的 HTTP 方法和标头将在paths.

```yaml
"paths": {
    "/users/**": {
        "summary": "example-route-id",
        "get": {
            "responses": {
                "200": {
                    "description": "Ok"
                }
            }
        },
        "post": {
            "requestBody": {
                "description": "User to add",
                "content": {
                    "application/json": {
                        "schema": {
                            "required": [
                                "name"
                            ],
                            "type": "object",
                            "properties": {
                                "name": {
                                    "type": "string"
                                },
                                "age": {
                                    "type": "integer",
                                    "format": "int32"
                                }
                            },
                            "description": "User schema"
                        }
                    }
                }
            },
            "responses": {
                "200": {
                    "description": "Ok"
                }
            }
        }
```

## 自定义 HTTP 响应

为了为您的路径添加自定义 HTTP 响应，您可以添加 OpenAPI Schema of Responses objects。

如下例所示，将model.responses属性添加到具有正确信息的路由中。

```yaml
apiVersion: "tanzu.vmware.com/v1"
kind: SpringCloudGatewayRouteConfig
metadata:
  name: myapp-route-config
spec:
  service:
    name: myapp-service
  routes:
    - id: example-route-id
      predicates:
        - Path=/users/**
      model:
        responses:
          200:
            description: "Obtain a list of users"
            content:
              application/json:
                schema:
                  type: object
                  description: User schema
                  properties:
                    name:
                      type: string
                    age:
                      type: integer
                      format: int32
          3XX:
            description: "Redirection applied"
            headers:
              X-Redirected-From:
                schema:
                  type: string
                  description: URL from which the request was redirected.
          default:
            description: "Unexpected error"
```

如果您不提供任何 HTTP 响应，则操作员将默认200 Ok为每个路径的操作生成一个响应。一些过滤器可能会添加自定义响应以记录其内部功能。您也可以通过将它们包含在本节中来覆盖这些响应。
