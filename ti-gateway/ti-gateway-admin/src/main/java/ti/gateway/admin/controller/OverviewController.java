package ti.gateway.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ti.gateway.admin.domain.overview.ComponentStats;
import ti.gateway.admin.domain.overview.Features;
import ti.gateway.admin.domain.overview.Overview;
import ti.gateway.admin.domain.overview.ProtocolStats;
import ti.gateway.admin.service.OverviewService;

import java.util.List;

/**
 * 概览控制器
 * @version 1.0
 * @date 2024/5/8 15:24
 */
@RestController
public class OverviewController {

    @Autowired
    private OverviewService overviewService;

    @GetMapping("/api/overview")
    public Overview overview() {
        return overviewService.getOverview();
    }

}
