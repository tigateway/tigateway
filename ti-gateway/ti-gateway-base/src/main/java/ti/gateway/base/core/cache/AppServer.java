package ti.gateway.base.core.cache;

import lombok.Data;

import java.util.Set;

@Data
public class AppServer {

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
    private String appkey;

    /**
     * 应用Secret
     */
    private String appsecret;

    /**
     * 应用状态 0不可用 1可用
     */
    private Byte status;
    /**
     * 授权服务信息
     */
    private Set<Server> servers;

    /**
     * Server
     */
    @Data
    public static class Server {
        /**
         * 服务编码
         */
        private String serverCode;

        /**
         * 应用Key
         */
        private String appkey;

        /**
         * 服务授权IP地址
         */
        private String serverIps;

        /**
         * 服务状态 0不可用 1可用
         */
        private Byte status;

        public String getServerCode() {
            return serverCode;
        }

        public void setServerCode(String serverCode) {
            this.serverCode = serverCode;
        }

        public String getAppkey() {
            return appkey;
        }

        public void setAppkey(String appkey) {
            this.appkey = appkey;
        }

        public String getServerIps() {
            return serverIps;
        }

        public void setServerIps(String serverIps) {
            this.serverIps = serverIps;
        }

        public Byte getStatus() {
            return status;
        }

        public void setStatus(Byte status) {
            this.status = status;
        }
    }


}
