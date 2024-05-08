package ti.gateway.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ti.gateway.admin.domain.version.Version;

import java.time.ZonedDateTime;

/**
 * @version 1.0
 * @date 2024/5/8 15:23
 */
@RestController
public class VersionController {

    @GetMapping("/api/version")
    public Version version() {
        // Static data creation for the Version
        return new Version("1.0.0", "royalwang", ZonedDateTime.now());
    }

}
