package ti.gateway.dashboard.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ti.gateway.dashboard.service.GatewayActuatorService;
import ti.gateway.dashboard.service.Route;

import java.util.List;

/**
 * @version 1.0
 * @date 2024/5/6 23:30
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    private final GatewayActuatorService gatewayActuatorService;

    public GatewayController(GatewayActuatorService gatewayActuatorService) {
        this.gatewayActuatorService = gatewayActuatorService;
    }

    @GetMapping("/globalfilters")
    public Mono<String> getGlobalFilters() {
        return gatewayActuatorService.getGlobalFilters();
    }

    @GetMapping("/routefilters")
    public Mono<String> getRouteFilters() {
        return gatewayActuatorService.getRouteFilters();
    }

    @PostMapping("/refresh")
    public Mono<Void> refreshRoutes() {
        return gatewayActuatorService.refreshRoutes();
    }

    @GetMapping("/routes")
    public Mono<List<Route>> getAllRoutes() {
        return gatewayActuatorService.getAllRoutes();
    }

    @GetMapping("/routes/{id}")
    public Mono<String> getRouteById(@PathVariable String id) {
        return gatewayActuatorService.getRouteById(id);
    }

    @PostMapping("/routes/{id}")
    public Mono<String> addRoute(@PathVariable String id, @RequestBody String routeDetails) {
        return gatewayActuatorService.addRoute(id, routeDetails);
    }

    @DeleteMapping("/routes/{id}")
    public Mono<Void> deleteRoute(@PathVariable String id) {
        return gatewayActuatorService.deleteRoute(id);
    }
}

