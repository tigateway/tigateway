package ti.gateway.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ti.gateway.admin.domain.version.Version;
import ti.gateway.admin.service.VersionService;

/**
 * 版本控制器
 * @version 1.0
 * @date 2024/5/8 15:23
 */
@RestController
public class VersionController {

    @Autowired
    private VersionService versionService;

    @GetMapping("/api/version")
    public Version version() {
        return versionService.getVersion();
    }

}
