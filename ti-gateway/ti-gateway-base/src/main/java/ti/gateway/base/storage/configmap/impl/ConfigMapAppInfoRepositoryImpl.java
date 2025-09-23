package ti.gateway.base.storage.configmap.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ti.gateway.base.storage.configmap.ConfigMapAppInfoRepository;
import ti.gateway.base.storage.configmap.config.ConfigMapStorageProperties;
import ti.gateway.base.storage.configmap.model.ConfigMapAppInfo;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ConfigMap应用信息仓库实现
 */
@Repository
public class ConfigMapAppInfoRepositoryImpl implements ConfigMapAppInfoRepository {

    private static final Logger log = LoggerFactory.getLogger(ConfigMapAppInfoRepositoryImpl.class);

    @Autowired
    private CoreV1Api coreV1Api;

    @Autowired
    private ConfigMapStorageProperties properties;

    @Autowired
    private ApiClient apiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, ConfigMapAppInfo> cache = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        if (properties.isAutoRefresh()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "configmap-refresh-thread");
                t.setDaemon(true);
                return t;
            });
            scheduler.scheduleAtFixedRate(this::refreshCache, 0, properties.getRefreshInterval(), TimeUnit.SECONDS);
        }
        
        // 初始化时加载数据
        refreshCache();
    }

    @Override
    public ConfigMapAppInfo findByAppKey(String appKey) {
        if (properties.isCacheEnabled()) {
            return cache.get(appKey);
        } else {
            return loadFromConfigMap(appKey);
        }
    }

    @Override
    public Set<String> findAllAppKeys() {
        if (properties.isCacheEnabled()) {
            return cache.keySet();
        } else {
            return loadAllAppKeysFromConfigMap();
        }
    }

    @Override
    public boolean save(ConfigMapAppInfo appInfo) {
        try {
            String appKey = appInfo.getAppKey();
            String key = properties.getAppInfoKeyPrefix() + appKey;
            String value = objectMapper.writeValueAsString(appInfo);

            V1ConfigMap configMap = getOrCreateConfigMap();
            if (configMap.getData() == null) {
                configMap.setData(new HashMap<>());
            }
            configMap.getData().put(key, value);

            coreV1Api.replaceNamespacedConfigMap(
                properties.getName(),
                properties.getNamespace(),
                configMap,
                null, null, null, null
            );

            // 更新缓存
            if (properties.isCacheEnabled()) {
                cache.put(appKey, appInfo);
            }

            log.info("Successfully saved app info for key: {}", appKey);
            return true;
        } catch (Exception e) {
            log.error("Failed to save app info for key: {}", appInfo.getAppKey(), e);
            return false;
        }
    }

    @Override
    public boolean delete(String appKey) {
        try {
            String key = properties.getAppInfoKeyPrefix() + appKey;
            
            V1ConfigMap configMap = getOrCreateConfigMap();
            if (configMap.getData() != null && configMap.getData().containsKey(key)) {
                configMap.getData().remove(key);
                
                coreV1Api.replaceNamespacedConfigMap(
                    properties.getName(),
                    properties.getNamespace(),
                    configMap,
                    null, null, null, null
                );

                // 更新缓存
                if (properties.isCacheEnabled()) {
                    cache.remove(appKey);
                }

                log.info("Successfully deleted app info for key: {}", appKey);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to delete app info for key: {}", appKey, e);
            return false;
        }
    }

    @Override
    public List<ConfigMapAppInfo> findAll() {
        if (properties.isCacheEnabled()) {
            return new ArrayList<>(cache.values());
        } else {
            return loadAllFromConfigMap();
        }
    }

    @Override
    public void refreshCache() {
        if (!properties.isCacheEnabled()) {
            return;
        }

        try {
            Map<String, ConfigMapAppInfo> newCache = new ConcurrentHashMap<>();
            List<ConfigMapAppInfo> allApps = loadAllFromConfigMap();
            
            for (ConfigMapAppInfo appInfo : allApps) {
                newCache.put(appInfo.getAppKey(), appInfo);
            }
            
            cache.clear();
            cache.putAll(newCache);
            
            log.debug("Refreshed cache with {} app infos", newCache.size());
        } catch (Exception e) {
            log.error("Failed to refresh cache", e);
        }
    }

    @Override
    public boolean configMapExists() {
        try {
            coreV1Api.readNamespacedConfigMap(properties.getName(), properties.getNamespace(), null);
            return true;
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                return false;
            }
            throw new RuntimeException("Failed to check ConfigMap existence", e);
        }
    }

    private ConfigMapAppInfo loadFromConfigMap(String appKey) {
        try {
            V1ConfigMap configMap = getOrCreateConfigMap();
            if (configMap.getData() == null) {
                return null;
            }

            String key = properties.getAppInfoKeyPrefix() + appKey;
            String value = configMap.getData().get(key);
            if (value == null) {
                return null;
            }

            return objectMapper.readValue(value, ConfigMapAppInfo.class);
        } catch (Exception e) {
            log.error("Failed to load app info from ConfigMap for key: {}", appKey, e);
            return null;
        }
    }

    private Set<String> loadAllAppKeysFromConfigMap() {
        Set<String> appKeys = new HashSet<>();
        try {
            V1ConfigMap configMap = getOrCreateConfigMap();
            if (configMap.getData() == null) {
                return appKeys;
            }

            String prefix = properties.getAppInfoKeyPrefix();
            for (String key : configMap.getData().keySet()) {
                if (key.startsWith(prefix)) {
                    appKeys.add(key.substring(prefix.length()));
                }
            }
        } catch (Exception e) {
            log.error("Failed to load all app keys from ConfigMap", e);
        }
        return appKeys;
    }

    private List<ConfigMapAppInfo> loadAllFromConfigMap() {
        List<ConfigMapAppInfo> appInfos = new ArrayList<>();
        try {
            V1ConfigMap configMap = getOrCreateConfigMap();
            if (configMap.getData() == null) {
                return appInfos;
            }

            String prefix = properties.getAppInfoKeyPrefix();
            for (Map.Entry<String, String> entry : configMap.getData().entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    try {
                        ConfigMapAppInfo appInfo = objectMapper.readValue(entry.getValue(), ConfigMapAppInfo.class);
                        appInfos.add(appInfo);
                    } catch (Exception e) {
                        log.warn("Failed to parse app info for key: {}", entry.getKey(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load all app infos from ConfigMap", e);
        }
        return appInfos;
    }

    private V1ConfigMap getOrCreateConfigMap() throws ApiException {
        try {
            return coreV1Api.readNamespacedConfigMap(properties.getName(), properties.getNamespace(), null);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                if (properties.isCreateDefaultConfig()) {
                    return createDefaultConfigMap();
                } else {
                    throw new RuntimeException("ConfigMap not found and auto-creation is disabled", e);
                }
            }
            throw e;
        }
    }

    private V1ConfigMap createDefaultConfigMap() throws ApiException {
        log.info("Creating default ConfigMap: {}", properties.getName());

        V1ConfigMap configMap = new V1ConfigMap();
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(properties.getName());
        metadata.setNamespace(properties.getNamespace());
        configMap.setMetadata(metadata);

        Map<String, String> data = new HashMap<>();
        
        // 创建默认应用配置
        ConfigMapAppInfo defaultApp = createDefaultAppInfo();
        String defaultAppKey = properties.getAppInfoKeyPrefix() + defaultApp.getAppKey();
        try {
            data.put(defaultAppKey, objectMapper.writeValueAsString(defaultApp));
        } catch (Exception e) {
            log.error("Failed to serialize default app info", e);
        }

        configMap.setData(data);

        return coreV1Api.createNamespacedConfigMap(properties.getNamespace(), configMap, null, null, null, null);
    }

    private ConfigMapAppInfo createDefaultAppInfo() {
        ConfigMapStorageProperties.DefaultAppConfig defaultConfig = properties.getDefaultApp();
        
        ConfigMapAppInfo appInfo = new ConfigMapAppInfo();
        appInfo.setAppKey(defaultConfig.getAppKey());
        appInfo.setAppSecret(defaultConfig.getAppSecret());
        appInfo.setName(defaultConfig.getName());
        appInfo.setDesc(defaultConfig.getDesc());
        appInfo.setType(defaultConfig.getType());
        appInfo.setStatus(defaultConfig.getStatus());
        appInfo.setCtime(LocalDateTime.now());
        appInfo.setMtime(LocalDateTime.now());

        // 创建默认服务列表
        Set<ConfigMapAppInfo.ConfigMapAppServer> servers = new HashSet<>();
        for (String serverCode : defaultConfig.getDefaultServers()) {
            ConfigMapAppInfo.ConfigMapAppServer server = new ConfigMapAppInfo.ConfigMapAppServer();
            server.setServerCode(serverCode);
            server.setAppKey(defaultConfig.getAppKey());
            server.setServerIps("*");
            server.setStatus((byte) 1);
            server.setCtime(LocalDateTime.now());
            server.setMtime(LocalDateTime.now());
            servers.add(server);
        }
        appInfo.setServers(servers);

        return appInfo;
    }
}
