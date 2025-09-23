package ti.gateway.base.config.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ti.gateway.base.storage.configmap.model.ConfigMapAppInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TiGateway配置转换器
 * 将标准化的YAML配置转换为ConfigMap存储格式
 */
@Component
public class ConfigTransformer {

    private static final Logger log = LoggerFactory.getLogger(ConfigTransformer.class);

    @Autowired
    private ConfigSchemaValidator schemaValidator;

    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public ConfigTransformer() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
    }

    /**
     * 将YAML配置转换为ConfigMap应用信息列表
     *
     * @param yamlContent YAML配置内容
     * @return ConfigMap应用信息列表
     */
    public List<ConfigMapAppInfo> transformYamlToConfigMapApps(String yamlContent) {
        try {
            // 首先验证配置
            ConfigSchemaValidator.ValidationResult validationResult = schemaValidator.validateYaml(yamlContent);
            if (!validationResult.isValid()) {
                log.error("Configuration validation failed: {}", validationResult.getErrorMessage());
                throw new IllegalArgumentException("Invalid configuration: " + validationResult.getErrorMessage());
            }

            JsonNode configNode = yamlMapper.readTree(yamlContent);
            return transformToConfigMapApps(configNode);
        } catch (Exception e) {
            log.error("Failed to transform YAML to ConfigMap apps", e);
            throw new RuntimeException("Failed to transform configuration", e);
        }
    }

    /**
     * 将JSON配置转换为ConfigMap应用信息列表
     *
     * @param jsonContent JSON配置内容
     * @return ConfigMap应用信息列表
     */
    public List<ConfigMapAppInfo> transformJsonToConfigMapApps(String jsonContent) {
        try {
            // 首先验证配置
            ConfigSchemaValidator.ValidationResult validationResult = schemaValidator.validateJson(jsonContent);
            if (!validationResult.isValid()) {
                log.error("Configuration validation failed: {}", validationResult.getErrorMessage());
                throw new IllegalArgumentException("Invalid configuration: " + validationResult.getErrorMessage());
            }

            JsonNode configNode = jsonMapper.readTree(jsonContent);
            return transformToConfigMapApps(configNode);
        } catch (Exception e) {
            log.error("Failed to transform JSON to ConfigMap apps", e);
            throw new RuntimeException("Failed to transform configuration", e);
        }
    }

    /**
     * 将配置节点转换为ConfigMap应用信息列表
     *
     * @param configNode 配置节点
     * @return ConfigMap应用信息列表
     */
    private List<ConfigMapAppInfo> transformToConfigMapApps(JsonNode configNode) {
        List<ConfigMapAppInfo> appInfos = new ArrayList<>();

        JsonNode applicationsNode = configNode.get("applications");
        if (applicationsNode != null && applicationsNode.isArray()) {
            for (JsonNode appNode : applicationsNode) {
                ConfigMapAppInfo appInfo = transformApplicationToConfigMapApp(appNode);
                if (appInfo != null) {
                    appInfos.add(appInfo);
                }
            }
        }

        log.info("Transformed {} applications to ConfigMap format", appInfos.size());
        return appInfos;
    }

    /**
     * 将应用节点转换为ConfigMap应用信息
     *
     * @param appNode 应用节点
     * @return ConfigMap应用信息
     */
    private ConfigMapAppInfo transformApplicationToConfigMapApp(JsonNode appNode) {
        try {
            ConfigMapAppInfo appInfo = new ConfigMapAppInfo();

            // 基本信息
            appInfo.setName(getStringValue(appNode, "name"));
            appInfo.setDesc(getStringValue(appNode, "description"));
            appInfo.setAppKey(getStringValue(appNode, "id"));
            appInfo.setType(transformAppType(getStringValue(appNode, "type")));
            appInfo.setStatus(transformAppStatus(getStringValue(appNode, "status")));

            // 凭据信息
            JsonNode credentialsNode = appNode.get("credentials");
            if (credentialsNode != null) {
                appInfo.setAppSecret(getStringValue(credentialsNode, "app_secret"));
            }

            // 时间信息
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            appInfo.setCtime(LocalDateTime.parse(currentTime));
            appInfo.setMtime(LocalDateTime.parse(currentTime));

            // 服务信息
            Set<ConfigMapAppInfo.ConfigMapAppServer> servers = transformPermissionsToServers(appNode);
            appInfo.setServers(servers);

            return appInfo;
        } catch (Exception e) {
            log.error("Failed to transform application node", e);
            return null;
        }
    }

    /**
     * 将权限信息转换为服务信息
     *
     * @param appNode 应用节点
     * @return 服务信息集合
     */
    private Set<ConfigMapAppInfo.ConfigMapAppServer> transformPermissionsToServers(JsonNode appNode) {
        Set<ConfigMapAppInfo.ConfigMapAppServer> servers = new HashSet<>();

        JsonNode permissionsNode = appNode.get("permissions");
        if (permissionsNode != null && permissionsNode.isArray()) {
            for (JsonNode permissionNode : permissionsNode) {
                String resource = getStringValue(permissionNode, "resource");
                if (resource != null) {
                    ConfigMapAppInfo.ConfigMapAppServer server = new ConfigMapAppInfo.ConfigMapAppServer();
                    server.setServerCode(resource);
                    server.setAppKey(getStringValue(appNode, "id"));
                    server.setServerIps("*"); // 默认允许所有IP
                    server.setStatus((byte) 1); // 默认启用

                    String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    server.setCtime(LocalDateTime.parse(currentTime));
                    server.setMtime(LocalDateTime.parse(currentTime));

                    servers.add(server);
                }
            }
        }

        return servers;
    }

    /**
     * 转换应用类型
     *
     * @param type 应用类型字符串
     * @return 应用类型字节
     */
    private Byte transformAppType(String type) {
        if (type == null) {
            return (byte) 1; // 默认PC类型
        }

        switch (type.toLowerCase()) {
            case "web":
            case "api":
            case "admin":
                return (byte) 1; // PC类型
            case "mobile":
                return (byte) 2; // 移动端类型
            default:
                return (byte) 1; // 默认PC类型
        }
    }

    /**
     * 转换应用状态
     *
     * @param status 应用状态字符串
     * @return 应用状态字节
     */
    private Byte transformAppStatus(String status) {
        if (status == null) {
            return (byte) 1; // 默认启用
        }

        switch (status.toLowerCase()) {
            case "active":
                return (byte) 1; // 启用
            case "inactive":
            case "maintenance":
                return (byte) 0; // 禁用
            default:
                return (byte) 1; // 默认启用
        }
    }

    /**
     * 获取字符串值
     *
     * @param node JSON节点
     * @param fieldName 字段名
     * @return 字符串值
     */
    private String getStringValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    /**
     * 获取整数值
     *
     * @param node JSON节点
     * @param fieldName 字段名
     * @return 整数值
     */
    private Integer getIntValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asInt() : null;
    }

    /**
     * 获取布尔值
     *
     * @param node JSON节点
     * @param fieldName 字段名
     * @return 布尔值
     */
    private Boolean getBooleanValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asBoolean() : null;
    }

    /**
     * 将ConfigMap应用信息转换为YAML配置
     *
     * @param appInfos ConfigMap应用信息列表
     * @return YAML配置字符串
     */
    public String transformConfigMapAppsToYaml(List<ConfigMapAppInfo> appInfos) {
        try {
            JsonNode rootNode = jsonMapper.createObjectNode();
            
            // 添加元数据
            JsonNode metadataNode = jsonMapper.createObjectNode();
            ((com.fasterxml.jackson.databind.node.ObjectNode) metadataNode)
                .put("name", "tigateway-config")
                .put("version", "1.0.0")
                .put("description", "TiGateway configuration generated from ConfigMap")
                .put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .put("updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            ((com.fasterxml.jackson.databind.node.ObjectNode) rootNode).set("metadata", metadataNode);

            // 添加应用配置
            JsonNode applicationsNode = jsonMapper.createArrayNode();
            for (ConfigMapAppInfo appInfo : appInfos) {
                JsonNode appNode = transformConfigMapAppToApplication(appInfo);
                ((com.fasterxml.jackson.databind.node.ArrayNode) applicationsNode).add(appNode);
            }
            ((com.fasterxml.jackson.databind.node.ObjectNode) rootNode).set("applications", applicationsNode);

            return yamlMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            log.error("Failed to transform ConfigMap apps to YAML", e);
            throw new RuntimeException("Failed to transform ConfigMap apps to YAML", e);
        }
    }

    /**
     * 将ConfigMap应用信息转换为应用节点
     *
     * @param appInfo ConfigMap应用信息
     * @return 应用节点
     */
    private JsonNode transformConfigMapAppToApplication(ConfigMapAppInfo appInfo) {
        com.fasterxml.jackson.databind.node.ObjectNode appNode = jsonMapper.createObjectNode();
        
        appNode.put("id", appInfo.getAppKey());
        appNode.put("name", appInfo.getName());
        appNode.put("description", appInfo.getDesc());
        appNode.put("type", transformAppTypeToString(appInfo.getType()));
        appNode.put("status", transformAppStatusToString(appInfo.getStatus()));

        // 添加凭据信息
        com.fasterxml.jackson.databind.node.ObjectNode credentialsNode = jsonMapper.createObjectNode();
        credentialsNode.put("app_key", appInfo.getAppKey());
        credentialsNode.put("app_secret", appInfo.getAppSecret());
        appNode.set("credentials", credentialsNode);

        // 添加权限信息
        if (appInfo.getServers() != null && !appInfo.getServers().isEmpty()) {
            com.fasterxml.jackson.databind.node.ArrayNode permissionsNode = jsonMapper.createArrayNode();
            for (ConfigMapAppInfo.ConfigMapAppServer server : appInfo.getServers()) {
                com.fasterxml.jackson.databind.node.ObjectNode permissionNode = jsonMapper.createObjectNode();
                permissionNode.put("resource", server.getServerCode());
                permissionNode.putArray("actions").add("read").add("write");
                permissionsNode.add(permissionNode);
            }
            appNode.set("permissions", permissionsNode);
        }

        return appNode;
    }

    /**
     * 转换应用类型为字符串
     *
     * @param type 应用类型字节
     * @return 应用类型字符串
     */
    private String transformAppTypeToString(Byte type) {
        if (type == null) {
            return "web";
        }

        switch (type) {
            case 1:
                return "web";
            case 2:
                return "mobile";
            default:
                return "web";
        }
    }

    /**
     * 转换应用状态为字符串
     *
     * @param status 应用状态字节
     * @return 应用状态字符串
     */
    private String transformAppStatusToString(Byte status) {
        if (status == null) {
            return "active";
        }

        switch (status) {
            case 1:
                return "active";
            case 0:
                return "inactive";
            default:
                return "active";
        }
    }
}
