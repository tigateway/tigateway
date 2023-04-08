package ti.gateway.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:50
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    // HTTP routers

    @GetMapping("/http/routers")
    public String listHttpRouters() {
        // Replace this with your actual data source
        return "List of HTTP routers";
    }

    @GetMapping("/http/routers/{name}")
    public String getHttpRouter(@PathVariable String name) {
        // Replace this with your actual data source
        return "HTTP router: " + name;
    }

    // HTTP services

    @GetMapping("/http/services")
    public String listHttpServices() {
        // Replace this with your actual data source
        return "List of HTTP services";
    }

    @GetMapping("/http/services/{name}")
    public String getHttpService(@PathVariable String name) {
        // Replace this with your actual data source
        return "HTTP service: " + name;
    }

    // HTTP middlewares

    @GetMapping("/http/middlewares")
    public String listHttpMiddlewares() {
        // Replace this with your actual data source
        return "List of HTTP middlewares";
    }

    @GetMapping("/http/middlewares/{name}")
    public String getHttpMiddleware(@PathVariable String name) {
        // Replace this with your actual data source
        return "HTTP middleware: " + name;
    }

    // TCP routers

    @GetMapping("/tcp/routers")
    public String listTcpRouters() {
        // Replace this with your actual data source
        return "List of TCP routers";
    }

    @GetMapping("/tcp/routers/{name}")
    public String getTcpRouter(@PathVariable String name) {
        // Replace this with your actual data source
        return "TCP router: " + name;
    }

    // TCP services

    @GetMapping("/tcp/services")
    public String listTcpServices() {
        // Replace this with your actual data source
        return "List of TCP services";
    }

    @GetMapping("/tcp/services/{name}")
    public String getTcpService(@PathVariable String name) {
        // Replace this with your actual data source
        return "TCP service: " + name;
    }

    // Entry points

    @GetMapping("/entrypoints")
    public String listEntryPoints() {
        // Replace this with your actual data source
        return "List of entry points";
    }

    @GetMapping("/entrypoints/{name}")
    public String getEntryPoint(@PathVariable String name) {
        // Replace this with your actual data source
        return "Entry point: " + name;
    }

    // Traefik version

    @GetMapping("/version")
    public String getVersion() {
        // Replace this with your actual data source
        return "Traefik version";
    }

    // Debug and profiling endpoints are not implemented here, as they are part of Go's standard library
}

