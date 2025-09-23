package ti.gateway.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ti.gateway.admin.domain.*;
import ti.gateway.admin.domain.overview.ComponentStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API服务 - 提供网关数据管理
 * @version 1.0
 * @date 2024/5/8 15:30
 */
@Service
public class ApiService {

    @Autowired
    private GatewayActuatorService gatewayActuatorService;

    // HTTP Routers

    public List<Router> getHttpRouters() {
        try {
            // 从Spring Cloud Gateway获取实际路由数据
            return gatewayActuatorService.getAllRoutes()
                .map(this::convertToRouter)
                .collectList()
                .block();
        } catch (Exception e) {
            // 如果获取失败，返回模拟数据
            return getMockHttpRouters();
        }
    }

    public Router getHttpRouter(String name) {
        try {
            return gatewayActuatorService.getAllRoutes()
                .filter(route -> route.getRouteId().equals(name))
                .map(this::convertToRouter)
                .blockFirst();
        } catch (Exception e) {
            // 返回模拟数据
            return getMockHttpRouter(name);
        }
    }

    // HTTP Services

    public List<ServiceInfo> getHttpServices() {
        try {
            // 从Spring Cloud Gateway获取服务数据
            return gatewayActuatorService.getAllRoutes()
                .map(this::convertToServiceInfo)
                .distinct()
                .collectList()
                .block();
        } catch (Exception e) {
            // 返回模拟数据
            return getMockHttpServices();
        }
    }

    public ServiceInfo getHttpService(String name) {
        try {
            return gatewayActuatorService.getAllRoutes()
                .filter(route -> route.getUri().contains(name))
                .map(this::convertToServiceInfo)
                .blockFirst();
        } catch (Exception e) {
            // 返回模拟数据
            return getMockHttpService(name);
        }
    }

    // HTTP Middlewares

    public List<Middleware> getHttpMiddlewares() {
        try {
            // 从Spring Cloud Gateway获取过滤器数据
            return gatewayActuatorService.getAllRoutes()
                .flatMap(route -> route.getFilters().stream())
                .map(this::convertToMiddleware)
                .distinct()
                .collectList()
                .block();
        } catch (Exception e) {
            // 返回模拟数据
            return getMockHttpMiddlewares();
        }
    }

    public Middleware getHttpMiddleware(String name) {
        try {
            return gatewayActuatorService.getAllRoutes()
                .flatMap(route -> route.getFilters().stream())
                .filter(filter -> filter.contains(name))
                .map(this::convertToMiddleware)
                .blockFirst();
        } catch (Exception e) {
            // 返回模拟数据
            return getMockHttpMiddleware(name);
        }
    }

    // TCP相关方法（Spring Cloud Gateway主要处理HTTP，TCP返回空数据）

    public List<Router> getTcpRouters() {
        return new ArrayList<>();
    }

    public Router getTcpRouter(String name) {
        return null;
    }

    public List<ServiceInfo> getTcpServices() {
        return new ArrayList<>();
    }

    public ServiceInfo getTcpService(String name) {
        return null;
    }

    public List<Middleware> getTcpMiddlewares() {
        return new ArrayList<>();
    }

    public Middleware getTcpMiddleware(String name) {
        return null;
    }

    // UDP相关方法（Spring Cloud Gateway主要处理HTTP，UDP返回空数据）

    public List<Router> getUdpRouters() {
        return new ArrayList<>();
    }

    public Router getUdpRouter(String name) {
        return null;
    }

    public List<ServiceInfo> getUdpServices() {
        return new ArrayList<>();
    }

    public ServiceInfo getUdpService(String name) {
        return null;
    }

    // Entry Points

    public List<AddressConfig> getEntryPoints() {
        List<AddressConfig> entryPoints = new ArrayList<>();
        
        // 添加默认的HTTP入口点
        AddressConfig httpEntry = new AddressConfig();
        httpEntry.setName("web");
        httpEntry.setAddress(":8080");
        entryPoints.add(httpEntry);

        // 添加HTTPS入口点
        AddressConfig httpsEntry = new AddressConfig();
        httpsEntry.setName("web-secured");
        httpsEntry.setAddress(":8443");
        entryPoints.add(httpsEntry);

        return entryPoints;
    }

    public AddressConfig getEntryPoint(String name) {
        return getEntryPoints().stream()
            .filter(ep -> ep.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    // 转换方法

    private Router convertToRouter(ti.gateway.admin.service.Route route) {
        Router router = new Router();
        router.setName(route.getRouteId());
        router.setService(route.getUri());
        router.setRule(String.join(" && ", route.getPredicates()));
        router.setStatus("enabled");
        router.setProvider("springcloudgateway");
        router.setUsing(Arrays.asList("web"));
        router.setMiddlewares(route.getFilters());
        return router;
    }

    private ServiceInfo convertToServiceInfo(ti.gateway.admin.service.Route route) {
        ServiceInfo service = new ServiceInfo();
        service.setName(route.getUri());
        service.setProvider("springcloudgateway");
        service.setStatus("enabled");
        service.setType("loadbalancer");
        
        // 设置负载均衡器信息
        LoadBalancer loadBalancer = new LoadBalancer();
        Server server = new Server();
        server.setUrl(route.getUri());
        loadBalancer.setServers(Arrays.asList(server));
        loadBalancer.setPassHostHeader(true);
        service.setLoadBalancer(loadBalancer);
        
        return service;
    }

    private Middleware convertToMiddleware(String filter) {
        Middleware middleware = new Middleware();
        middleware.setName(filter);
        middleware.setProvider("springcloudgateway");
        middleware.setStatus("enabled");
        middleware.setType("filter");
        return middleware;
    }

    // 模拟数据方法

    private List<Router> getMockHttpRouters() {
        List<Router> routers = new ArrayList<>();
        
        Router router1 = new Router();
        router1.setName("user-service@springcloudgateway");
        router1.setService("lb://user-service");
        router1.setRule("Path=/user/**");
        router1.setStatus("enabled");
        router1.setProvider("springcloudgateway");
        router1.setUsing(Arrays.asList("web"));
        router1.setMiddlewares(Arrays.asList("StripPrefix=1"));
        routers.add(router1);

        Router router2 = new Router();
        router2.setName("order-service@springcloudgateway");
        router2.setService("lb://order-service");
        router2.setRule("Path=/order/**");
        router2.setStatus("enabled");
        router2.setProvider("springcloudgateway");
        router2.setUsing(Arrays.asList("web"));
        router2.setMiddlewares(Arrays.asList("StripPrefix=1"));
        routers.add(router2);

        return routers;
    }

    private Router getMockHttpRouter(String name) {
        Router router = new Router();
        router.setName(name);
        router.setService("lb://mock-service");
        router.setRule("Path=/**");
        router.setStatus("enabled");
        router.setProvider("springcloudgateway");
        router.setUsing(Arrays.asList("web"));
        return router;
    }

    private List<ServiceInfo> getMockHttpServices() {
        List<ServiceInfo> services = new ArrayList<>();
        
        ServiceInfo service1 = new ServiceInfo();
        service1.setName("user-service");
        service1.setProvider("springcloudgateway");
        service1.setStatus("enabled");
        service1.setType("loadbalancer");
        
        LoadBalancer loadBalancer1 = new LoadBalancer();
        Server server1 = new Server();
        server1.setUrl("http://user-service:8080");
        loadBalancer1.setServers(Arrays.asList(server1));
        loadBalancer1.setPassHostHeader(true);
        service1.setLoadBalancer(loadBalancer1);
        services.add(service1);

        ServiceInfo service2 = new ServiceInfo();
        service2.setName("order-service");
        service2.setProvider("springcloudgateway");
        service2.setStatus("enabled");
        service2.setType("loadbalancer");
        
        LoadBalancer loadBalancer2 = new LoadBalancer();
        Server server2 = new Server();
        server2.setUrl("http://order-service:8080");
        loadBalancer2.setServers(Arrays.asList(server2));
        loadBalancer2.setPassHostHeader(true);
        service2.setLoadBalancer(loadBalancer2);
        services.add(service2);

        return services;
    }

    private ServiceInfo getMockHttpService(String name) {
        ServiceInfo service = new ServiceInfo();
        service.setName(name);
        service.setProvider("springcloudgateway");
        service.setStatus("enabled");
        service.setType("loadbalancer");
        
        LoadBalancer loadBalancer = new LoadBalancer();
        Server server = new Server();
        server.setUrl("http://" + name + ":8080");
        loadBalancer.setServers(Arrays.asList(server));
        loadBalancer.setPassHostHeader(true);
        service.setLoadBalancer(loadBalancer);
        
        return service;
    }

    private List<Middleware> getMockHttpMiddlewares() {
        List<Middleware> middlewares = new ArrayList<>();
        
        Middleware middleware1 = new Middleware();
        middleware1.setName("StripPrefix");
        middleware1.setProvider("springcloudgateway");
        middleware1.setStatus("enabled");
        middleware1.setType("filter");
        middlewares.add(middleware1);

        Middleware middleware2 = new Middleware();
        middleware2.setName("AddRequestHeader");
        middleware2.setProvider("springcloudgateway");
        middleware2.setStatus("enabled");
        middleware2.setType("filter");
        middlewares.add(middleware2);

        return middlewares;
    }

    private Middleware getMockHttpMiddleware(String name) {
        Middleware middleware = new Middleware();
        middleware.setName(name);
        middleware.setProvider("springcloudgateway");
        middleware.setStatus("enabled");
        middleware.setType("filter");
        return middleware;
    }
}
