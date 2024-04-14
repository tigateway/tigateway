package ti.gateway.base.core.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * memory server storage
 */
public class DefaultAppServerStorage implements AppServerStorage {

    private static final Logger log = LoggerFactory.getLogger(DefaultAppServerStorage.class);

    private final ConcurrentMap<String, AppServer> memoryCache = new ConcurrentHashMap<>();

    /**
     * 生成AppServer
     *
     * @param appId
     * @return
     */
    @Override
    public AppServer generateAppServer(String appId) {
        return memoryCache.get(appId);
    }

}
