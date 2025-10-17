package ti.gateway.base.core.cache;

public interface AppServerCache {

    /**
     * 获取应用服务信息
     *
     * @param appId 应用ID
     * @return AppServer
     */
    AppServer get(String appId);

}
