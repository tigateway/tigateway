package ti.gateway.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ti.gateway.admin.domain.version.Version;

import java.time.ZonedDateTime;

/**
 * 版本服务
 * @version 1.0
 * @date 2024/5/8 15:25
 */
@Service
public class VersionService {

    @Value("${spring.application.name:tigateway-admin}")
    private String applicationName;

    @Value("${tigateway.version:1.0.0}")
    private String version;

    @Value("${tigateway.codename:tigateway}")
    private String codename;

    /**
     * 获取版本信息
     */
    public Version getVersion() {
        return new Version(version, codename, ZonedDateTime.now());
    }
}
