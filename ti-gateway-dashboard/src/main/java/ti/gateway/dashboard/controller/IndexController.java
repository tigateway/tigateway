package ti.gateway.dashboard.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ti.gateway.dashboard.domain.overview.ComponentStats;
import ti.gateway.dashboard.domain.overview.Features;
import ti.gateway.dashboard.domain.overview.Overview;
import ti.gateway.dashboard.domain.overview.ProtocolStats;
import ti.gateway.dashboard.domain.version.Version;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
public class IndexController {

    @RequestMapping({
            "/",
            "",
            "/index"
    })
    public String index() {
        return "Hello, TiGateway!";
    }


    @RequestMapping("/api/overview")
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

    @RequestMapping("/api/version")
    public Version version() {
        // Static data creation for the Version
        return new Version("1.0.0", "royalwang", ZonedDateTime.now());
    }

}
