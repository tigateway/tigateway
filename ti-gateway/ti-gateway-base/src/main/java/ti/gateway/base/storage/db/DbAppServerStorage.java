package ti.gateway.base.storage.db;

import org.springframework.util.CollectionUtils;
import ti.gateway.base.core.cache.AppServer;
import ti.gateway.base.core.cache.AppServerStorage;
import ti.gateway.base.storage.db.mapper.TigaAppInfoMapper;
import ti.gateway.base.storage.db.mapper.TigaAppServerMapper;
import ti.gateway.base.storage.db.model.TigaAppInfo;
import ti.gateway.base.storage.db.model.TigaAppServer;
import ti.gateway.base.util.BeanCopierUtils;

import java.util.Objects;
import java.util.Set;

public class DbAppServerStorage implements AppServerStorage {

    private final TigaAppInfoMapper appInfoMapper;

    private final TigaAppServerMapper appServerMapper;

    public DbAppServerStorage(TigaAppInfoMapper appInfoMapper, TigaAppServerMapper appServerMapper) {
        this.appInfoMapper = appInfoMapper;
        this.appServerMapper = appServerMapper;
    }

    /**
     * 生成AppServer
     *
     * @param appId appId
     * @return AppServer
     */
    @Override
    public AppServer generateAppServer(String appId) {
        TigaAppInfo tigaAppInfo = appInfoMapper.selectByAppkey(appId);
        if (Objects.nonNull(tigaAppInfo)) {
            AppServer appServer = BeanCopierUtils.copierTargetBean(tigaAppInfo, TigaAppInfo.class, AppServer.class);
            Set<TigaAppServer> tigaAppServerSet = appServerMapper.selectListByAppkey(appId);
            if (!CollectionUtils.isEmpty(tigaAppServerSet)) {
                Set<AppServer.Server> servers = BeanCopierUtils.copierTargetBeanSet(tigaAppServerSet, TigaAppServer.class, AppServer.Server.class);
                appServer.setServers(servers);
            }
            return appServer;
        }
        return null;
    }

}
