package ti.gateway.operator.springcloudgateway.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGateway;
import ti.gateway.operator.springcloudgateway.openapi.OpenApiGenerator;
import ti.gateway.operator.springcloudgateway.route.RoutesDefinition;
import ti.gateway.operator.springcloudgateway.route.RoutesDefinitionResolver;
import io.kubernetes.client.openapi.ApiException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.Collection;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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

