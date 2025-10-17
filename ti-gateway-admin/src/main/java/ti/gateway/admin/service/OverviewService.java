package ti.gateway.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ti.gateway.admin.domain.overview.ComponentStats;
import ti.gateway.admin.domain.overview.Features;
import ti.gateway.admin.domain.overview.Overview;
import ti.gateway.admin.domain.overview.ProtocolStats;

import java.util.Arrays;
import java.util.List;

/**
 * 概览服务
 * @version 1.0
 * @date 2024/5/8 15:24
 */
@Service
public class OverviewService {

    @Autowired
    private GatewayActuatorService gatewayActuatorService;

    /**
     * 获取概览数据
     */
    public Overview getOverview() {
        // 从Spring Cloud Gateway获取实际的路由数据
        try {
            // 获取HTTP路由统计
            ComponentStats httpRouters = getHttpRoutersStats();
            ComponentStats httpServices = getHttpServicesStats();
            ComponentStats httpMiddlewares = getHttpMiddlewaresStats();
            ProtocolStats httpStats = new ProtocolStats(httpRouters, httpServices, httpMiddlewares);

            // TCP和UDP暂时返回空数据，因为Spring Cloud Gateway主要处理HTTP
            ComponentStats tcpRouters = new ComponentStats(0, 0, 0);
            ComponentStats tcpServices = new ComponentStats(0, 0, 0);
            ComponentStats tcpMiddlewares = new ComponentStats(0, 0, 0);
            ProtocolStats tcpStats = new ProtocolStats(tcpRouters, tcpServices, tcpMiddlewares);

            ComponentStats udpRouters = new ComponentStats(0, 0, 0);
            ComponentStats udpServices = new ComponentStats(0, 0, 0);
            ProtocolStats udpStats = new ProtocolStats(udpRouters, udpServices, null);

            // 特性配置
            Features features = new Features("", "Prometheus", true, false);
            
            // 支持的提供者
            List<String> providers = Arrays.asList(
                "SpringCloudGateway",
                "KubernetesIngress", 
                "KubernetesCRD",
                "File",
                "Http"
            );

            return new Overview(httpStats, tcpStats, udpStats, features, providers);
        } catch (Exception e) {
            // 如果获取数据失败，返回默认数据
            return getDefaultOverview();
        }
    }

    /**
     * 获取HTTP路由器统计
     */
    private ComponentStats getHttpRoutersStats() {
        try {
            // 这里应该从Spring Cloud Gateway获取实际的路由数量
            // 暂时返回模拟数据
            return new ComponentStats(5, 0, 0);
        } catch (Exception e) {
            return new ComponentStats(0, 0, 0);
        }
    }

    /**
     * 获取HTTP服务统计
     */
    private ComponentStats getHttpServicesStats() {
        try {
            // 这里应该从Spring Cloud Gateway获取实际的服务数量
            // 暂时返回模拟数据
            return new ComponentStats(3, 0, 0);
        } catch (Exception e) {
            return new ComponentStats(0, 0, 0);
        }
    }

    /**
     * 获取HTTP中间件统计
     */
    private ComponentStats getHttpMiddlewaresStats() {
        try {
            // 这里应该从Spring Cloud Gateway获取实际的中间件数量
            // 暂时返回模拟数据
            return new ComponentStats(2, 0, 0);
        } catch (Exception e) {
            return new ComponentStats(0, 0, 0);
        }
    }

    /**
     * 获取默认概览数据
     */
    private Overview getDefaultOverview() {
        ComponentStats httpRouters = new ComponentStats(0, 0, 0);
        ComponentStats httpServices = new ComponentStats(0, 0, 0);
        ComponentStats httpMiddlewares = new ComponentStats(0, 0, 0);
        ProtocolStats httpStats = new ProtocolStats(httpRouters, httpServices, httpMiddlewares);

        ComponentStats tcpRouters = new ComponentStats(0, 0, 0);
        ComponentStats tcpServices = new ComponentStats(0, 0, 0);
        ComponentStats tcpMiddlewares = new ComponentStats(0, 0, 0);
        ProtocolStats tcpStats = new ProtocolStats(tcpRouters, tcpServices, tcpMiddlewares);

        ComponentStats udpRouters = new ComponentStats(0, 0, 0);
        ComponentStats udpServices = new ComponentStats(0, 0, 0);
        ProtocolStats udpStats = new ProtocolStats(udpRouters, udpServices, null);

        Features features = new Features("", "Prometheus", false, false);
        List<String> providers = Arrays.asList("SpringCloudGateway");

        return new Overview(httpStats, tcpStats, udpStats, features, providers);
    }
}
