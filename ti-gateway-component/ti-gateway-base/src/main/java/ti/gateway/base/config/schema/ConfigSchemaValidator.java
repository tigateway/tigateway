package ti.gateway.base.config.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * TiGateway配置Schema验证器
 * 用于验证YAML配置文件是否符合TiGateway Schema规范
 */
@Component
public class ConfigSchemaValidator {

    private static final Logger log = LoggerFactory.getLogger(ConfigSchemaValidator.class);

    private JsonSchema schema;
    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public ConfigSchemaValidator() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        try {
            loadSchema();
        } catch (Exception e) {
            log.error("Failed to load TiGateway configuration schema", e);
            throw new RuntimeException("Failed to initialize schema validator", e);
        }
    }

    /**
     * 加载Schema定义
     */
    private void loadSchema() throws IOException {
        ClassPathResource schemaResource = new ClassPathResource("schema/tigateway-config-schema.yaml");
        try (InputStream schemaStream = schemaResource.getInputStream()) {
            JsonNode schemaNode = yamlMapper.readTree(schemaStream);
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            this.schema = factory.getSchema(schemaNode);
            log.info("TiGateway configuration schema loaded successfully");
        }
    }

    /**
     * 验证YAML配置
     *
     * @param yamlContent YAML配置内容
     * @return 验证结果
     */
    public ValidationResult validateYaml(String yamlContent) {
        try {
            JsonNode configNode = yamlMapper.readTree(yamlContent);
            return validateJson(configNode);
        } catch (Exception e) {
            log.error("Failed to parse YAML content", e);
            return ValidationResult.error("Invalid YAML format: " + e.getMessage());
        }
    }

    /**
     * 验证JSON配置
     *
     * @param jsonContent JSON配置内容
     * @return 验证结果
     */
    public ValidationResult validateJson(String jsonContent) {
        try {
            JsonNode configNode = jsonMapper.readTree(jsonContent);
            return validateJson(configNode);
        } catch (Exception e) {
            log.error("Failed to parse JSON content", e);
            return ValidationResult.error("Invalid JSON format: " + e.getMessage());
        }
    }

    /**
     * 验证JsonNode配置
     *
     * @param configNode 配置节点
     * @return 验证结果
     */
    public ValidationResult validateJson(JsonNode configNode) {
        try {
            Set<ValidationMessage> errors = schema.validate(configNode);
            
            if (errors.isEmpty()) {
                log.debug("Configuration validation passed");
                return ValidationResult.success();
            } else {
                log.warn("Configuration validation failed with {} errors", errors.size());
                return ValidationResult.error(errors);
            }
        } catch (Exception e) {
            log.error("Schema validation failed", e);
            return ValidationResult.error("Schema validation failed: " + e.getMessage());
        }
    }

    /**
     * 验证配置文件
     *
     * @param configPath 配置文件路径
     * @return 验证结果
     */
    public ValidationResult validateFile(String configPath) {
        try {
            ClassPathResource configResource = new ClassPathResource(configPath);
            try (InputStream configStream = configResource.getInputStream()) {
                JsonNode configNode = yamlMapper.readTree(configStream);
                return validateJson(configNode);
            }
        } catch (Exception e) {
            log.error("Failed to validate config file: {}", configPath, e);
            return ValidationResult.error("Failed to validate config file: " + e.getMessage());
        }
    }

    /**
     * 获取Schema信息
     *
     * @return Schema信息
     */
    public SchemaInfo getSchemaInfo() {
        return new SchemaInfo(
            "TiGateway Configuration Schema",
            "1.0.0",
            "https://tigateway.cn/schemas/config/v1.0.0",
            "TiGateway网关配置文件的标准化Schema"
        );
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final Set<ValidationMessage> validationErrors;

        private ValidationResult(boolean valid, String errorMessage, Set<ValidationMessage> validationErrors) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.validationErrors = validationErrors;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, null);
        }

        public static ValidationResult error(Set<ValidationMessage> errors) {
            StringBuilder message = new StringBuilder("Validation failed:\n");
            for (ValidationMessage error : errors) {
                message.append("- ").append(error.getMessage()).append(" at ").append(error.getPath()).append("\n");
            }
            return new ValidationResult(false, message.toString(), errors);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Set<ValidationMessage> getValidationErrors() {
            return validationErrors;
        }

        @Override
        public String toString() {
            if (valid) {
                return "ValidationResult{valid=true}";
            } else {
                return "ValidationResult{valid=false, errorMessage='" + errorMessage + "'}";
            }
        }
    }

    /**
     * Schema信息类
     */
    public static class SchemaInfo {
        private final String title;
        private final String version;
        private final String id;
        private final String description;

        public SchemaInfo(String title, String version, String id, String description) {
            this.title = title;
            this.version = version;
            this.id = id;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getVersion() {
            return version;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }
}
