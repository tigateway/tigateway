package ti.gateway.admin.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * cache info
 */
@ConfigurationProperties("spring.cloud.gateway.cache")
public class ApiGatewayCacheProperties {

    /**
     * 默认开启二级缓存
     */
    private boolean shouldUseReadOnlyServerCache = Boolean.TRUE;

    /**
     * 服务列表缓存自动过期时间
     */
    private Long serverCacheAutoExpirationInSeconds = 180L;
    /**
     * 初始化服务列表缓存容量
     */
    private Integer initialCapacityOfServerCache = 100;
    /**
     * 缓存更新时间 默认30秒
     */
    private Long serverCacheUpdateIntervalMs = 30 * 1000L;
    /**
     * 是否跳过缓存 skip
     */
    private Boolean skip = Boolean.FALSE;

    public boolean getShouldUseReadOnlyServerCache() {
        return shouldUseReadOnlyServerCache;
    }

    public void setShouldUseReadOnlyServerCache(boolean shouldUseReadOnlyServerCache) {
        this.shouldUseReadOnlyServerCache = shouldUseReadOnlyServerCache;
    }

    public Long getServerCacheAutoExpirationInSeconds() {
        return serverCacheAutoExpirationInSeconds;
    }

    public void setServerCacheAutoExpirationInSeconds(Long serverCacheAutoExpirationInSeconds) {
        this.serverCacheAutoExpirationInSeconds = serverCacheAutoExpirationInSeconds;
    }

    public Integer getInitialCapacityOfServerCache() {
        return initialCapacityOfServerCache;
    }

    public void setInitialCapacityOfServerCache(Integer initialCapacityOfServerCache) {
        this.initialCapacityOfServerCache = initialCapacityOfServerCache;
    }

    public Long getServerCacheUpdateIntervalMs() {
        return serverCacheUpdateIntervalMs;
    }

    public void setServerCacheUpdateIntervalMs(Long serverCacheUpdateIntervalMs) {
        this.serverCacheUpdateIntervalMs = serverCacheUpdateIntervalMs;
    }

    public Boolean getSkip() {
        return skip;
    }

    public void setSkip(Boolean skip) {
        this.skip = skip;
    }

}
