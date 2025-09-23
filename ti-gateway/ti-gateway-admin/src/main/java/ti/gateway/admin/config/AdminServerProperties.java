package ti.gateway.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Admin服务器配置属性
 */
@ConfigurationProperties(prefix = "admin.server")
public class AdminServerProperties {

    /**
     * 是否启用Admin服务器
     */
    private boolean enabled = true;

    /**
     * Admin服务器端口
     */
    private int port = 8081;

    /**
     * Admin服务器上下文路径
     */
    private String contextPath = "/admin";

    /**
     * 服务器名称
     */
    private String name = "tigateway-admin";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
