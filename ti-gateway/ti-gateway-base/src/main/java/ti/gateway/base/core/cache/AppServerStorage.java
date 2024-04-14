package ti.gateway.base.core.cache;

/**
 * App Server 存储
 */
public interface AppServerStorage {

    /**
     * 生成AppServer
     * @param appId
     * @return
     */
    AppServer generateAppServer(String appId);

}
