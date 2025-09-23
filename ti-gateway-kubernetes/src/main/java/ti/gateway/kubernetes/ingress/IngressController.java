package ti.gateway.kubernetes.ingress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Ingress管理控制器
 * 提供REST API来管理Ingress路由
 */
@RestController
@RequestMapping("/actuator/ingress")
@ConditionalOnProperty(
    value = "spring.cloud.gateway.kubernetes.ingress.enabled",
    havingValue = "true"
)
public class IngressController {

    private static final Logger logger = LoggerFactory.getLogger(IngressController.class);

    @Autowired
    private IngressWatcher ingressWatcher;

    @Autowired
    private IngressRouteDefinitionLocator routeDefinitionLocator;

    @Autowired
    private IngressProperties ingressProperties;

    /**
     * 获取Ingress配置信息
     */
    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", ingressProperties.isEnabled());
        config.put("namespace", ingressProperties.getNamespace());
        config.put("refreshInterval", ingressProperties.getRefreshInterval());
        config.put("cacheEnabled", ingressProperties.isCacheEnabled());
        config.put("tlsEnabled", ingressProperties.isTlsEnabled());
        config.put("defaultServicePort", ingressProperties.getDefaultServicePort());
        config.put("pathRewriteEnabled", ingressProperties.isPathRewriteEnabled());
        return config;
    }

    /**
     * 手动刷新路由
     */
    @PostMapping("/refresh")
    public Map<String, String> refreshRoutes() {
        logger.info("Manual route refresh requested");
        
        try {
            ingressWatcher.refreshRoutes();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Routes refreshed successfully");
            return response;
        } catch (Exception e) {
            logger.error("Failed to refresh routes: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to refresh routes: " + e.getMessage());
            return response;
        }
    }

    /**
     * 获取当前路由状态
     */
    @GetMapping("/routes")
    public Map<String, Object> getRoutes() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 这里可以添加获取当前路由信息的逻辑
            response.put("status", "success");
            response.put("message", "Routes retrieved successfully");
            response.put("routes", routeDefinitionLocator.getRoutes().collectList().block());
        } catch (Exception e) {
            logger.error("Failed to get routes: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to get routes: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ingress-controller");
        return response;
    }
}
