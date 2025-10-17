package ti.gateway.base.storage.configmap.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ConfigMap应用信息模型
 * 用于Kubernetes ConfigMap存储的应用信息
 */
@Data
public class ConfigMapAppInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用描述
     */
    private String desc;

    /**
     * 应用类型 1 PC 2APP
     */
    private Byte type;

    /**
     * 应用Key
     */
    private String appKey;

    /**
     * 应用Secret
     */
    private String appSecret;

    /**
     * 应用状态 0不可用 1可用
     */
    private Byte status;

    /**
     * 创建时间
     */
    private LocalDateTime ctime;

    /**
     * 修改时间
     */
    private LocalDateTime mtime;

    /**
     * 服务列表
     */
    private java.util.Set<ConfigMapAppServer> servers;

    /**
     * ConfigMap应用服务信息
     */
    @Data
    public static class ConfigMapAppServer implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 服务编码
         */
        private String serverCode;

        /**
         * 应用Key
         */
        private String appKey;

        /**
         * 服务授权IP地址
         */
        private String serverIps;

        /**
         * 服务状态 0不可用 1可用
         */
        private Byte status;

        /**
         * 创建时间
         */
        private LocalDateTime ctime;

        /**
         * 修改时间
         */
        private LocalDateTime mtime;
    }
}
