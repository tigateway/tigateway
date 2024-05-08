package ti.gateway.base.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * app info
 */
@ConfigurationProperties("spring.cloud.gateway.app")
@Data
public class ApiGatewayAppProperties {

    /**
     * appId name
     */
    private String appId = "X-GW-APPID";
    /**
     * Timestamp Name
     */
    private String timestamp = "X-GW-Timestamp";
    /**
     * Sign
     */
    private String sign = "X-GW-SIGN";
    /**
     * 默认5分钟
     */
    private Long durationMt = 1000 * 60 * 5L;
    /**
     * skip
     */
    private Boolean skip = Boolean.FALSE;

}
