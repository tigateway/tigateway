package ti.gateway.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ti.gateway.admin.domain.Router;
import ti.gateway.admin.domain.ServiceInfo;
import ti.gateway.admin.domain.Middleware;
import ti.gateway.admin.domain.AddressConfig;
import ti.gateway.admin.service.ApiService;

import java.util.List;

/**
 * API控制器 - 提供网关管理接口
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:50
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ApiService apiService;

    // HTTP routers

    @GetMapping("/http/routers")
    public ResponseEntity<List<Router>> listHttpRouters() {
        List<Router> routers = apiService.getHttpRouters();
        return ResponseEntity.ok(routers);
    }

    @GetMapping("/http/routers/{name}")
    public ResponseEntity<Router> getHttpRouter(@PathVariable String name) {
        Router router = apiService.getHttpRouter(name);
        return ResponseEntity.ok(router);
    }

    // HTTP services

    @GetMapping("/http/services")
    public ResponseEntity<List<ServiceInfo>> listHttpServices() {
        List<ServiceInfo> services = apiService.getHttpServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/http/services/{name}")
    public ResponseEntity<ServiceInfo> getHttpService(@PathVariable String name) {
        ServiceInfo service = apiService.getHttpService(name);
        return ResponseEntity.ok(service);
    }

    // HTTP middlewares

    @GetMapping("/http/middlewares")
    public ResponseEntity<List<Middleware>> listHttpMiddlewares() {
        List<Middleware> middlewares = apiService.getHttpMiddlewares();
        return ResponseEntity.ok(middlewares);
    }

    @GetMapping("/http/middlewares/{name}")
    public ResponseEntity<Middleware> getHttpMiddleware(@PathVariable String name) {
        Middleware middleware = apiService.getHttpMiddleware(name);
        return ResponseEntity.ok(middleware);
    }

    // TCP routers

    @GetMapping("/tcp/routers")
    public ResponseEntity<List<Router>> listTcpRouters() {
        List<Router> routers = apiService.getTcpRouters();
        return ResponseEntity.ok(routers);
    }

    @GetMapping("/tcp/routers/{name}")
    public ResponseEntity<Router> getTcpRouter(@PathVariable String name) {
        Router router = apiService.getTcpRouter(name);
        return ResponseEntity.ok(router);
    }

    // TCP services

    @GetMapping("/tcp/services")
    public ResponseEntity<List<ServiceInfo>> listTcpServices() {
        List<ServiceInfo> services = apiService.getTcpServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/tcp/services/{name}")
    public ResponseEntity<ServiceInfo> getTcpService(@PathVariable String name) {
        ServiceInfo service = apiService.getTcpService(name);
        return ResponseEntity.ok(service);
    }

    // TCP middlewares

    @GetMapping("/tcp/middlewares")
    public ResponseEntity<List<Middleware>> listTcpMiddlewares() {
        List<Middleware> middlewares = apiService.getTcpMiddlewares();
        return ResponseEntity.ok(middlewares);
    }

    @GetMapping("/tcp/middlewares/{name}")
    public ResponseEntity<Middleware> getTcpMiddleware(@PathVariable String name) {
        Middleware middleware = apiService.getTcpMiddleware(name);
        return ResponseEntity.ok(middleware);
    }

    // UDP routers

    @GetMapping("/udp/routers")
    public ResponseEntity<List<Router>> listUdpRouters() {
        List<Router> routers = apiService.getUdpRouters();
        return ResponseEntity.ok(routers);
    }

    @GetMapping("/udp/routers/{name}")
    public ResponseEntity<Router> getUdpRouter(@PathVariable String name) {
        Router router = apiService.getUdpRouter(name);
        return ResponseEntity.ok(router);
    }

    // UDP services

    @GetMapping("/udp/services")
    public ResponseEntity<List<ServiceInfo>> listUdpServices() {
        List<ServiceInfo> services = apiService.getUdpServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/udp/services/{name}")
    public ResponseEntity<ServiceInfo> getUdpService(@PathVariable String name) {
        ServiceInfo service = apiService.getUdpService(name);
        return ResponseEntity.ok(service);
    }

    // Entry points

    @GetMapping("/entrypoints")
    public ResponseEntity<List<AddressConfig>> listEntryPoints() {
        List<AddressConfig> entryPoints = apiService.getEntryPoints();
        return ResponseEntity.ok(entryPoints);
    }

    @GetMapping("/entrypoints/{name}")
    public ResponseEntity<AddressConfig> getEntryPoint(@PathVariable String name) {
        AddressConfig entryPoint = apiService.getEntryPoint(name);
        return ResponseEntity.ok(entryPoint);
    }
}

