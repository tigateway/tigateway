package ti.gateway.base.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.cloud.gateway.server")
@Data
public class ApiGatewayServerProperties {

    /**
     * skip 授权服务校验
     */
    private Boolean skip = Boolean.FALSE;

}
