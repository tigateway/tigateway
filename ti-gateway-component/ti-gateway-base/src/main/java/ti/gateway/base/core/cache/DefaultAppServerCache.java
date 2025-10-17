package ti.gateway.base.core.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ti.gateway.base.core.config.ApiGatewayCacheProperties;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * Default impl {@link AppServerCache}
 */
public class DefaultAppServerCache implements AppServerCache {

    private static final Logger log = LoggerFactory.getLogger(DefaultAppServerCache.class);

    private final ConcurrentMap<String, AppServer> readOnlyCacheMap = new ConcurrentHashMap<>();

    private final LoadingCache<String, Value> readWriteCacheMap;

    private final long serverCacheUpdateIntervalMs;

    private final boolean shouldUseReadOnlyServerCache;

    private final boolean skip;

    private AppServerStorage appServerStorage;

    private ScheduledExecutorService appServerExecutorService = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("APP-Server-schedule-pool-%d").daemon(true).build());

    public DefaultAppServerCache(ApiGatewayCacheProperties apiGatewayCacheProperties, AppServerStorage appServerStorage) {
        this.appServerStorage = appServerStorage;
        this.shouldUseReadOnlyServerCache = apiGatewayCacheProperties.getShouldUseReadOnlyServerCache();
        this.serverCacheUpdateIntervalMs = apiGatewayCacheProperties.getServerCacheUpdateIntervalMs();
        this.skip = apiGatewayCacheProperties.getSkip();
        this.readWriteCacheMap =
                CacheBuilder.newBuilder().initialCapacity(apiGatewayCacheProperties.getInitialCapacityOfServerCache())
                        .expireAfterWrite(apiGatewayCacheProperties.getServerCacheAutoExpirationInSeconds(), TimeUnit.SECONDS)
                        .removalListener((RemovalListener<String, Value>) notification -> {
                            //此处不删除readOnlyCacheMap缓存当心跳时删除
                            String removedKey = notification.getKey();
                        })
                        .build(new CacheLoader<String, Value>() {
                            @Override
                            public Value load(String key) throws Exception {
                                AppServer value = appServerStorage.generateAppServer(key);
                                return new Value(value);
                            }
                        });

        if (shouldUseReadOnlyServerCache) {
            appServerExecutorService.scheduleAtFixedRate(getCacheUpdateTask(),
                    serverCacheUpdateIntervalMs, serverCacheUpdateIntervalMs, TimeUnit.MILLISECONDS);
        }

    }


    /**
     * 更新缓存
     *
     * @return Runnable
     */
    private Runnable getCacheUpdateTask() {
        return () -> {
            log.debug("Updating the client cache from server cache");
            for (String key : readOnlyCacheMap.keySet()) {
                if (log.isDebugEnabled()) {
                    log.debug("Updating the client cache from server cache for appid : {} ",
                            key);
                }
                try {
                    Value value = readWriteCacheMap.get(key);
                    AppServer cacheValue = value.getAppServer();
                    AppServer currentCacheValue = readOnlyCacheMap.get(key);
                    if (cacheValue != currentCacheValue) {
                        readOnlyCacheMap.put(key, cacheValue);
                    }
                } catch (Throwable th) {
                    log.error("Error while updating the client cache from server cache for key {}", key, th);
                }
            }
        };
    }

    /**
     * 获取应用服务信息
     *
     * @param appId 应用ID
     * @return AppServer
     */
    @Override
    public AppServer get(String appId) {
        AppServer payload = null;
        Value value = null;
        if (skip) {
            return appServerStorage.generateAppServer(appId);
        }
        try {
            if (shouldUseReadOnlyServerCache) {
                final AppServer currentPayload = readOnlyCacheMap.get(appId);
                if (currentPayload != null) {
                    payload = currentPayload;
                } else {
                    value = readWriteCacheMap.get(appId);
                    payload = value.getAppServer();
                    if (payload != null) {
                        readOnlyCacheMap.put(appId, payload);
                    }
                }
            } else {
                value = readWriteCacheMap.get(appId);
                payload = value.getAppServer();
            }
        } catch (Throwable t) {
            log.error("Cannot get value for key : {}", appId, t);
        }
        return payload;
    }

    /**
     * The class that stores payload in both compressed and uncompressed form.
     */
    public class Value {
        private AppServer appServer;

        public Value(AppServer appServer) {
            this.appServer = appServer;
        }

        public AppServer getAppServer() {
            return appServer;
        }

        public void setAppServer(AppServer appServer) {
            this.appServer = appServer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Value value = (Value) o;
            return Objects.equals(appServer, value.appServer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(appServer);
        }
    }

}
