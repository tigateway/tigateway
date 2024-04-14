package ti.gateway.admin.core.cache;

public interface AppServerCache {

    /**
     * 获取应用服务信息
     *
     * @param appId
     * @return
     */
    AppServer get(String appId);

}
