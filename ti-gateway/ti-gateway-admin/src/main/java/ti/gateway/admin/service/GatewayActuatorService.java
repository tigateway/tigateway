package ti.gateway.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @version 1.0
 * @date 2024/5/6 23:30
 */
@Service
public class GatewayActuatorService {

    private final WebClient webClient;

    public GatewayActuatorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/actuator").build();
    }

    public Mono<String> fetchGatewayRoutes() {
        return webClient.get()
                .uri("/gateway/routes")
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getGlobalFilters() {
        return webClient.get().uri("/gateway/globalfilters").retrieve().bodyToMono(String.class);
    }

    public Mono<String> getRouteFilters() {
        return webClient.get().uri("/gateway/routefilters").retrieve().bodyToMono(String.class);
    }

    public Mono<Void> refreshRoutes() {
        return webClient.post().uri("/gateway/refresh").retrieve().bodyToMono(Void.class);
    }

    public Mono<List<Route>> getAllRoutes() {
        return webClient.get()
                .uri("/gateway/routes")
                .retrieve()
                .bodyToFlux(Route.class)
                .collectList();
    }

    public Mono<String> getRouteById(String id) {
        return webClient.get().uri("/gateway/routes/" + id).retrieve().bodyToMono(String.class);
    }

    public Mono<String> addRoute(String id, String routeDetails) {
        return webClient.post().uri("/gateway/routes/" + id)
                .bodyValue(routeDetails).retrieve().bodyToMono(String.class);
    }

    public Mono<Void> deleteRoute(String id) {
        return webClient.delete().uri("/gateway/routes/" + id).retrieve().bodyToMono(Void.class);
    }
}

