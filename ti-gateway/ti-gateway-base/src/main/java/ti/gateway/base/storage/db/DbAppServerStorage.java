package ti.gateway.base.storage.db;

import org.springframework.util.CollectionUtils;
import ti.gateway.base.core.cache.AppServer;
import ti.gateway.base.core.cache.AppServerStorage;
import ti.gateway.base.storage.db.mapper.GwAppInfoMapper;
import ti.gateway.base.storage.db.mapper.GwAppServerMapper;
import ti.gateway.base.storage.db.model.GwAppInfo;
import ti.gateway.base.storage.db.model.GwAppServer;
import ti.gateway.util.BeanCopierUtils;

import java.util.Objects;
import java.util.Set;

public class DbAppServerStorage implements AppServerStorage {

    private GwAppInfoMapper gwAppInfoMapper;

    private GwAppServerMapper gwAppServerMapper;

    public DbAppServerStorage(GwAppInfoMapper gwAppInfoMapper, GwAppServerMapper gwAppServerMapper) {
        this.gwAppInfoMapper = gwAppInfoMapper;
        this.gwAppServerMapper = gwAppServerMapper;
    }

    /**
     * 生成AppServer
     *
     * @param appId
     * @return
     */
    @Override
    public AppServer generateAppServer(String appId) {
        GwAppInfo gwAppInfo = gwAppInfoMapper.selectByAppkey(appId);
        if (Objects.nonNull(gwAppInfo)) {
            AppServer appServer = BeanCopierUtils.copierTargetBean(gwAppInfo, GwAppInfo.class, AppServer.class);
            Set<GwAppServer> gwAppServers = gwAppServerMapper.selectListByAppkey(appId);
            if (!CollectionUtils.isEmpty(gwAppServers)) {
                Set<AppServer.Server> servers = BeanCopierUtils.copierTargetBeanSet(gwAppServers, GwAppServer.class, AppServer.Server.class);
                appServer.setServers(servers);
            }
            return appServer;
        }
        return null;
    }

}
