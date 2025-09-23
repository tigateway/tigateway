package ti.gateway.kubernetes.ingress;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1IngressList;
import io.kubernetes.client.openapi.models.V1IngressRule;
import io.kubernetes.client.openapi.models.V1IngressTLS;
import io.kubernetes.client.openapi.models.V1HTTPIngressPath;
import io.kubernetes.client.openapi.models.V1IngressBackend;
import io.kubernetes.client.openapi.models.V1IngressServiceBackend;
import io.kubernetes.client.openapi.models.V1ServiceBackendPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ingress路由定义定位器
 * 从Kubernetes Ingress资源中读取路由配置并转换为Spring Cloud Gateway路由
 */
@Component
public class IngressRouteDefinitionLocator implements RouteLocator {

    private static final Logger logger = LoggerFactory.getLogger(IngressRouteDefinitionLocator.class);

    private final NetworkingV1Api networkingV1Api;
    private final RouteLocatorBuilder routeLocatorBuilder;
    private final String namespace;
    private final Map<String, Route> routeCache = new ConcurrentHashMap<>();

    public IngressRouteDefinitionLocator(
            NetworkingV1Api networkingV1Api,
            RouteLocatorBuilder routeLocatorBuilder,
            @Value("${spring.cloud.gateway.kubernetes.ingress.namespace:default}") String namespace) {
        this.networkingV1Api = networkingV1Api;
        this.routeLocatorBuilder = routeLocatorBuilder;
        this.namespace = namespace;
    }

    @Override
    public Flux<Route> getRoutes() {
        return Flux.fromIterable(loadRoutesFromIngress());
    }

    /**
     * 从Kubernetes Ingress资源加载路由
     */
    private List<Route> loadRoutesFromIngress() {
        List<Route> routes = new ArrayList<>();
        
        try {
            V1IngressList ingressList = networkingV1Api.listNamespacedIngress(
                namespace, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null, 
                null,
                    false
            );

            for (V1Ingress ingress : ingressList.getItems()) {
                routes.addAll(convertIngressToRoutes(ingress));
            }

            logger.info("Loaded {} routes from {} Ingress resources", routes.size(), ingressList.getItems().size());
            
        } catch (ApiException e) {
            logger.error("Failed to load Ingress resources: {}", e.getMessage(), e);
        }

        return routes;
    }

    /**
     * 将Ingress资源转换为Spring Cloud Gateway路由
     */
    private List<Route> convertIngressToRoutes(V1Ingress ingress) {
        List<Route> routes = new ArrayList<>();
        
        if (ingress.getMetadata() == null) {
            logger.warn("Ingress metadata is null, skipping");
            return routes;
        }
        
        String ingressName = ingress.getMetadata().getName();
        String ingressNamespace = ingress.getMetadata().getNamespace();
        
        if (ingressName == null || ingressNamespace == null) {
            logger.warn("Ingress name or namespace is null, skipping");
            return routes;
        }

        if (ingress.getSpec() == null || ingress.getSpec().getRules() == null) {
            return routes;
        }

        for (V1IngressRule rule : ingress.getSpec().getRules()) {
            String host = rule.getHost();
            
            if (rule.getHttp() != null && rule.getHttp().getPaths() != null) {
                for (V1HTTPIngressPath path : rule.getHttp().getPaths()) {
                    Route route = buildRoute(ingressName, ingressNamespace, host, path, ingress);
                    if (route != null) {
                        routes.add(route);
                    }
                }
            }
        }

        return routes;
    }

    /**
     * 构建单个路由
     */
    private Route buildRoute(String ingressName, String ingressNamespace, String host, 
                           V1HTTPIngressPath path, V1Ingress ingress) {
        try {
            String routeId = generateRouteId(ingressName, ingressNamespace, host, path.getPath());
            final String pathPattern = path.getPath();
            final String pathType = path.getPathType();
            
            if (pathPattern == null) {
                logger.warn("Path pattern is null for ingress {}, skipping", ingressName);
                return null;
            }
            
            // 处理路径类型
            final String finalPathPattern;
            if ("Prefix".equals(pathType)) {
                finalPathPattern = pathPattern + "/**";
            } else if ("Exact".equals(pathType)) {
                finalPathPattern = pathPattern;
            } else {
                finalPathPattern = pathPattern + "/**";
            }

            // 构建路由
            RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes()
                .route(routeId, predicate -> {
                    // 添加路径谓词
                    var booleanSpec = "Prefix".equals(pathType) ? 
                        predicate.path(finalPathPattern.replace("/**", "") + "/**") :
                        "Exact".equals(pathType) ? 
                        predicate.path(finalPathPattern) :
                        predicate.path(finalPathPattern + "/**");
                    
                    // 添加主机谓词
                    if (host != null && !host.isEmpty()) {
                        booleanSpec = booleanSpec.and().host(host);
                    }
                    
                    return booleanSpec.uri(buildBackendUri(path.getBackend(), ingressNamespace));
                });

            // 添加TLS支持
            if (ingress.getSpec() != null && ingress.getSpec().getTls() != null) {
                for (V1IngressTLS tls : ingress.getSpec().getTls()) {
                    if (tls.getHosts() != null && tls.getHosts().contains(host)) {
                        // 可以在这里添加TLS相关的配置
                        logger.debug("TLS configured for host: {}", host);
                    }
                }
            }

            return builder.build().getRoutes().blockFirst();
            
        } catch (Exception e) {
            logger.error("Failed to build route for ingress {}: {}", ingressName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建后端服务URI
     */
    private String buildBackendUri(V1IngressBackend backend, String namespace) {
        if (backend == null) {
            return "lb://default-service";
        }

        if (backend.getService() != null) {
            V1IngressServiceBackend serviceBackend = backend.getService();
            if (serviceBackend == null) {
                return "lb://default-service";
            }
            
            String serviceName = serviceBackend.getName();
            V1ServiceBackendPort port = serviceBackend.getPort();
            
            String portStr = "80";
            if (port != null) {
                if (port.getName() != null) {
                    portStr = port.getName();
                } else if (port.getNumber() != null) {
                    portStr = port.getNumber().toString();
                }
            }

            // 使用Spring Cloud LoadBalancer格式
            return String.format("lb://%s:%s", serviceName, portStr);
        }

        return "lb://default-service";
    }

    /**
     * 生成路由ID
     */
    private String generateRouteId(String ingressName, String namespace, String host, String path) {
        return String.format("ingress-%s-%s-%s-%s", 
            namespace, 
            ingressName, 
            host != null ? host.replace(".", "-") : "default",
            path.replace("/", "-").replace("*", "wildcard")
        );
    }

    /**
     * 刷新路由缓存
     */
    public void refreshRoutes() {
        routeCache.clear();
        logger.info("Route cache cleared, routes will be reloaded on next request");
    }
}
