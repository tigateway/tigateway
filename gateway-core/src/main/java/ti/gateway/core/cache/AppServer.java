package ti.gateway.core.cache;

import java.util.Set;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getAppsecret() {
        return appsecret;
    }

    public void setAppsecret(String appsecret) {
        this.appsecret = appsecret;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Set<Server> getServers() {
        return servers;
    }

    public void setServers(Set<Server> servers) {
        this.servers = servers;
    }

}
