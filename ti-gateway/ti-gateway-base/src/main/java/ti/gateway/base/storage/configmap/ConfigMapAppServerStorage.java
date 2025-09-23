package ti.gateway.base.storage.configmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ti.gateway.base.core.cache.AppServer;
import ti.gateway.base.core.cache.AppServerStorage;
import ti.gateway.base.storage.configmap.model.ConfigMapAppInfo;

import java.util.Objects;
import java.util.Set;

/**
 * 基于ConfigMap的AppServer存储实现
 */
@Component
public class ConfigMapAppServerStorage implements AppServerStorage {

    private static final Logger log = LoggerFactory.getLogger(ConfigMapAppServerStorage.class);

    @Autowired
    private ConfigMapAppInfoRepository configMapAppInfoRepository;

    /**
     * 生成AppServer
     *
     * @param appId appId
     * @return AppServer
     */
    @Override
    public AppServer generateAppServer(String appId) {
        try {
            ConfigMapAppInfo configMapAppInfo = configMapAppInfoRepository.findByAppKey(appId);
            if (Objects.nonNull(configMapAppInfo)) {
                AppServer appServer = convertToAppServer(configMapAppInfo);
                log.debug("Generated AppServer for appId: {}", appId);
                return appServer;
            } else {
                log.warn("No app info found for appId: {}", appId);
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to generate AppServer for appId: {}", appId, e);
            return null;
        }
    }

    /**
     * 将ConfigMapAppInfo转换为AppServer
     */
    private AppServer convertToAppServer(ConfigMapAppInfo configMapAppInfo) {
        AppServer appServer = new AppServer();
        appServer.setName(configMapAppInfo.getName());
        appServer.setDesc(configMapAppInfo.getDesc());
        appServer.setType(configMapAppInfo.getType());
        appServer.setAppkey(configMapAppInfo.getAppKey());
        appServer.setAppsecret(configMapAppInfo.getAppSecret());
        appServer.setStatus(configMapAppInfo.getStatus());

        // 转换服务列表
        if (configMapAppInfo.getServers() != null) {
            Set<AppServer.Server> servers = new java.util.HashSet<>();
            for (ConfigMapAppInfo.ConfigMapAppServer configMapServer : configMapAppInfo.getServers()) {
                AppServer.Server server = new AppServer.Server();
                server.setServerCode(configMapServer.getServerCode());
                server.setAppkey(configMapServer.getAppKey());
                server.setServerIps(configMapServer.getServerIps());
                server.setStatus(configMapServer.getStatus());
                servers.add(server);
            }
            appServer.setServers(servers);
        }

        return appServer;
    }
}
