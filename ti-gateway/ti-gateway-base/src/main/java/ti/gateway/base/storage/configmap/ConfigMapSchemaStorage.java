package ti.gateway.base.storage.configmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ti.gateway.base.config.schema.ConfigSchemaValidator;
import ti.gateway.base.config.schema.ConfigTransformer;
import ti.gateway.base.storage.configmap.model.ConfigMapAppInfo;

import java.util.List;

/**
 * 基于Schema的ConfigMap存储实现
 * 支持标准化YAML配置的验证和转换
 */
@Component
public class ConfigMapSchemaStorage {

    private static final Logger log = LoggerFactory.getLogger(ConfigMapSchemaStorage.class);

    @Autowired
    private ConfigMapAppInfoRepository configMapAppInfoRepository;

    @Autowired
    private ConfigSchemaValidator schemaValidator;

    @Autowired
    private ConfigTransformer configTransformer;

    /**
     * 从YAML配置加载应用信息
     *
     * @param yamlContent YAML配置内容
     * @return 加载的应用信息数量
     */
    public int loadFromYaml(String yamlContent) {
        try {
            log.info("Loading applications from YAML configuration");

            // 验证配置
            ConfigSchemaValidator.ValidationResult validationResult = schemaValidator.validateYaml(yamlContent);
            if (!validationResult.isValid()) {
                log.error("YAML configuration validation failed: {}", validationResult.getErrorMessage());
                throw new IllegalArgumentException("Invalid YAML configuration: " + validationResult.getErrorMessage());
            }

            // 转换配置
            List<ConfigMapAppInfo> appInfos = configTransformer.transformYamlToConfigMapApps(yamlContent);

            // 保存到ConfigMap
            int savedCount = 0;
            for (ConfigMapAppInfo appInfo : appInfos) {
                if (configMapAppInfoRepository.save(appInfo)) {
                    savedCount++;
                    log.info("Successfully saved application: {}", appInfo.getAppKey());
                } else {
                    log.warn("Failed to save application: {}", appInfo.getAppKey());
                }
            }

            log.info("Loaded {} applications from YAML configuration", savedCount);
            return savedCount;
        } catch (Exception e) {
            log.error("Failed to load applications from YAML configuration", e);
            throw new RuntimeException("Failed to load applications from YAML", e);
        }
    }

    /**
     * 从JSON配置加载应用信息
     *
     * @param jsonContent JSON配置内容
     * @return 加载的应用信息数量
     */
    public int loadFromJson(String jsonContent) {
        try {
            log.info("Loading applications from JSON configuration");

            // 验证配置
            ConfigSchemaValidator.ValidationResult validationResult = schemaValidator.validateJson(jsonContent);
            if (!validationResult.isValid()) {
                log.error("JSON configuration validation failed: {}", validationResult.getErrorMessage());
                throw new IllegalArgumentException("Invalid JSON configuration: " + validationResult.getErrorMessage());
            }

            // 转换配置
            List<ConfigMapAppInfo> appInfos = configTransformer.transformJsonToConfigMapApps(jsonContent);

            // 保存到ConfigMap
            int savedCount = 0;
            for (ConfigMapAppInfo appInfo : appInfos) {
                if (configMapAppInfoRepository.save(appInfo)) {
                    savedCount++;
                    log.info("Successfully saved application: {}", appInfo.getAppKey());
                } else {
                    log.warn("Failed to save application: {}", appInfo.getAppKey());
                }
            }

            log.info("Loaded {} applications from JSON configuration", savedCount);
            return savedCount;
        } catch (Exception e) {
            log.error("Failed to load applications from JSON configuration", e);
            throw new RuntimeException("Failed to load applications from JSON", e);
        }
    }

    /**
     * 导出当前配置为YAML格式
     *
     * @return YAML配置字符串
     */
    public String exportToYaml() {
        try {
            log.info("Exporting current configuration to YAML");

            List<ConfigMapAppInfo> appInfos = configMapAppInfoRepository.findAll();
            String yamlContent = configTransformer.transformConfigMapAppsToYaml(appInfos);

            log.info("Exported {} applications to YAML format", appInfos.size());
            return yamlContent;
        } catch (Exception e) {
            log.error("Failed to export configuration to YAML", e);
            throw new RuntimeException("Failed to export configuration to YAML", e);
        }
    }

    /**
     * 验证YAML配置
     *
     * @param yamlContent YAML配置内容
     * @return 验证结果
     */
    public ConfigSchemaValidator.ValidationResult validateYaml(String yamlContent) {
        return schemaValidator.validateYaml(yamlContent);
    }

    /**
     * 验证JSON配置
     *
     * @param jsonContent JSON配置内容
     * @return 验证结果
     */
    public ConfigSchemaValidator.ValidationResult validateJson(String jsonContent) {
        return schemaValidator.validateJson(jsonContent);
    }

    /**
     * 获取Schema信息
     *
     * @return Schema信息
     */
    public ConfigSchemaValidator.SchemaInfo getSchemaInfo() {
        return schemaValidator.getSchemaInfo();
    }

    /**
     * 刷新配置缓存
     */
    public void refreshCache() {
        configMapAppInfoRepository.refreshCache();
        log.info("Configuration cache refreshed");
    }

    /**
     * 获取当前应用数量
     *
     * @return 应用数量
     */
    public int getApplicationCount() {
        return configMapAppInfoRepository.findAll().size();
    }

    /**
     * 检查配置是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return configMapAppInfoRepository.findAll().isEmpty();
    }

    /**
     * 清空所有配置
     *
     * @return 清空的应用数量
     */
    public int clearAll() {
        try {
            List<ConfigMapAppInfo> appInfos = configMapAppInfoRepository.findAll();
            int clearedCount = 0;

            for (ConfigMapAppInfo appInfo : appInfos) {
                if (configMapAppInfoRepository.delete(appInfo.getAppKey())) {
                    clearedCount++;
                }
            }

            log.info("Cleared {} applications from configuration", clearedCount);
            return clearedCount;
        } catch (Exception e) {
            log.error("Failed to clear configuration", e);
            throw new RuntimeException("Failed to clear configuration", e);
        }
    }
}
