package ti.gateway.kubernetes.ingress;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Ingress相关配置属性
 */
@Component
@ConfigurationProperties(prefix = "spring.cloud.gateway.kubernetes.ingress")
public class IngressProperties {

    /**
     * 是否启用Ingress支持
     */
    private boolean enabled = false;

    /**
     * 监听的Kubernetes命名空间
     */
    private String namespace = "default";

    /**
     * 路由刷新间隔（秒）
     */
    private long refreshInterval = 30;

    /**
     * 是否启用路由缓存
     */
    private boolean cacheEnabled = true;

    /**
     * 路由缓存过期时间（秒）
     */
    private long cacheExpiration = 300;

    /**
     * 是否启用TLS支持
     */
    private boolean tlsEnabled = true;

    /**
     * 默认服务端口
     */
    private int defaultServicePort = 80;

    /**
     * 是否启用路径重写
     */
    private boolean pathRewriteEnabled = true;

    /**
     * 路径重写规则
     */
    private String pathRewritePattern = "/(.*)";

    /**
     * 路径重写替换
     */
    private String pathRewriteReplacement = "/$1";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public long getCacheExpiration() {
        return cacheExpiration;
    }

    public void setCacheExpiration(long cacheExpiration) {
        this.cacheExpiration = cacheExpiration;
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    public void setTlsEnabled(boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
    }

    public int getDefaultServicePort() {
        return defaultServicePort;
    }

    public void setDefaultServicePort(int defaultServicePort) {
        this.defaultServicePort = defaultServicePort;
    }

    public boolean isPathRewriteEnabled() {
        return pathRewriteEnabled;
    }

    public void setPathRewriteEnabled(boolean pathRewriteEnabled) {
        this.pathRewriteEnabled = pathRewriteEnabled;
    }

    public String getPathRewritePattern() {
        return pathRewritePattern;
    }

    public void setPathRewritePattern(String pathRewritePattern) {
        this.pathRewritePattern = pathRewritePattern;
    }

    public String getPathRewriteReplacement() {
        return pathRewriteReplacement;
    }

    public void setPathRewriteReplacement(String pathRewriteReplacement) {
        this.pathRewriteReplacement = pathRewriteReplacement;
    }
}
