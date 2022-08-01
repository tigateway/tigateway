package ti.gateway.limit.sentinel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.cloud.gateway.sentinel")
public class ApiGatewaySentinelProperties {

    /**
     * 是否启用
     */
    private Boolean enabled = Boolean.TRUE;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

}
