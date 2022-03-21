package com.vmware.tanzu.springcloudgateway.api;

@RestController
public class OpenApiController {
    private final OpenApiGenerator openApiGenerator;
    private final RoutesDefinitionResolver routesResolver;

    public OpenApiController(OpenApiGenerator openApiGenerator, RoutesDefinitionResolver routesResolver) {
        this.openApiGenerator = openApiGenerator;
        this.routesResolver = routesResolver;
    }

    @GetMapping(
            value = {"/openapi"},
            produces = {"application/json"}
    )
    public Mono<String> openApi() throws ApiException, JsonProcessingException {
        Map<V1SpringCloudGateway, RoutesDefinition> routesForGateways = this.routesResolver.getRoutesForGateways();
        Collection<OpenAPI> openAPIs = this.openApiGenerator.generate(routesForGateways);
        return Mono.just(Json.mapper().writeValueAsString(openAPIs));
    }
}

