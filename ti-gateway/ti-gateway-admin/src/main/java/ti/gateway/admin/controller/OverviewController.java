package ti.gateway.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ti.gateway.admin.domain.overview.ComponentStats;
import ti.gateway.admin.domain.overview.Features;
import ti.gateway.admin.domain.overview.Overview;
import ti.gateway.admin.domain.overview.ProtocolStats;

import java.util.List;

/**
 * @version 1.0
 * @date 2024/5/8 15:24
 */
@RestController
public class OverviewController {

    @GetMapping("/api/overview")
    public Overview overview() {
        // Create static data for the Overview
        ComponentStats httpRouters = new ComponentStats(18, 0, 0);
        ComponentStats httpServices = new ComponentStats(9, 0, 0);
        ComponentStats httpMiddlewares = new ComponentStats(0, 0, 0);
        ProtocolStats httpStats = new ProtocolStats(httpRouters, httpServices, httpMiddlewares);

        ComponentStats tcpRouters = new ComponentStats(1, 0, 0);
        ComponentStats tcpServices = new ComponentStats(1, 0, 0);
        ComponentStats tcpMiddlewares = new ComponentStats(0, 0, 0);
        ProtocolStats tcpStats = new ProtocolStats(tcpRouters, tcpServices, tcpMiddlewares);

        ComponentStats udpRouters = new ComponentStats(1, 0, 0);
        ComponentStats udpServices = new ComponentStats(1, 0, 0);
        ProtocolStats udpStats = new ProtocolStats(udpRouters, udpServices, null);

        Features features = new Features("", "Prometheus", false, false);
        List<String> providers = List.of("KubernetesIngress", "KubernetesCRD");

        Overview overview = new Overview(httpStats, tcpStats, udpStats, features, providers);
        return overview;
    }

}
