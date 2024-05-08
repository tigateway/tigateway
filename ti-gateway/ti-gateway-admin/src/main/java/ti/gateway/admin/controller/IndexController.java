package ti.gateway.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ti.gateway.admin.domain.overview.ComponentStats;
import ti.gateway.admin.domain.overview.Features;
import ti.gateway.admin.domain.overview.Overview;
import ti.gateway.admin.domain.overview.ProtocolStats;
import ti.gateway.admin.domain.version.Version;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
public class IndexController {

    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.HEAD})
    public String index() {
        return "Hello, TiGateway!";
    }

}
