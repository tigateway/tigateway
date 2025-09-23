package ti.gateway.base.storage.configmap.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ConfigMap存储配置属性
 */
@ConfigurationProperties("spring.cloud.gateway.storage.configmap")
@Data
public class ConfigMapStorageProperties {

    /**
     * 是否启用ConfigMap存储
     */
    private boolean enabled = true;

    /**
     * ConfigMap名称
     */
    private String name = "tigateway-app-config";

    /**
     * ConfigMap命名空间
     */
    private String namespace = "default";

    /**
     * 应用信息在ConfigMap中的key前缀
     */
    private String appInfoKeyPrefix = "app.";

    /**
     * 服务信息在ConfigMap中的key前缀
     */
    private String serverInfoKeyPrefix = "server.";

    /**
     * 是否启用自动刷新
     */
    private boolean autoRefresh = true;

    /**
     * 刷新间隔（秒）
     */
    private long refreshInterval = 30;

    /**
     * 是否启用缓存
     */
    private boolean cacheEnabled = true;

    /**
     * 缓存过期时间（秒）
     */
    private long cacheExpiration = 300;

    /**
     * 是否在ConfigMap不存在时创建默认配置
     */
    private boolean createDefaultConfig = true;

    /**
     * 默认应用配置
     */
    private DefaultAppConfig defaultApp = new DefaultAppConfig();

    @Data
    public static class DefaultAppConfig {
        /**
         * 默认应用Key
         */
        private String appKey = "default-app";

        /**
         * 默认应用Secret
         */
        private String appSecret = "default-secret";

        /**
         * 默认应用名称
         */
        private String name = "Default Application";

        /**
         * 默认应用描述
         */
        private String desc = "Default application for TiGateway";

        /**
         * 默认应用类型
         */
        private Byte type = 1;

        /**
         * 默认应用状态
         */
        private Byte status = 1;

        /**
         * 默认服务列表
         */
        private String[] defaultServers = {"user-service", "order-service", "payment-service"};
    }
}
